#!/usr/bin/env python3
"""
Upload academy videos to Firebase Storage + generate the video manifest (Epic 27).

Uses firebase-admin (service account). Writes are authorized via the SA which
BYPASSES storage rules; public READ comes from storage.rules (videos/** read:true).

Usage:
    python upload_to_firebase.py                  # upload all + regenerate manifest
    python upload_to_firebase.py --dry-run        # plan only, no uploads
    python upload_to_firebase.py --list           # list remote videos/
    python upload_to_firebase.py --sa PATH        # explicit service account JSON

Service account search order (first hit wins):
    1. --sa argument
    2. nlm/firebase-service-account.json
    3. Downloads/*firebase-adminsdk*.json  (warns if project_id != tradelab-4f858)

Naming conventions:
    Local polished : nlm/assets/out/lecture_<code>_final.mp4        (en)
                     nlm/assets/out/lecture_<code>_HI_final.mp4     (hi, optional)
                     also accepts lecture_<code>_final_HI.mp4
    Remote         : videos/course_<n>/lecture_<code>.mp4
                     videos/course_<n>/lecture_<code>_HI.mp4
    Manifest keys  : "lecture_<code>_final"  (matches academy_data_v2.json videoUrl)

Hindi availability drives the app's dynamic EN/HI toggle: a lecture shows the
toggle ONLY when its manifest entry has a "hi" URL (or a bundled raw _hi exists).
"""

import io
import json
import sys
from pathlib import Path
from urllib.parse import quote

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")

PROJECT_ID = "tradelab-4f858"
BUCKET = f"{PROJECT_ID}.firebasestorage.app"
ASSETS_OUT = Path(__file__).parent / "assets" / "out"
MANIFEST_REMOTE = "videos/manifest.json"

PUBLIC_BASE = f"https://firebasestorage.googleapis.com/v0/b/{BUCKET}/o"


def find_service_account(explicit: str | None) -> Path | None:
    """Locate the SA JSON; warn when it belongs to a different project."""
    import os

    candidates = []
    if explicit:
        candidates.append(Path(explicit))
    candidates.append(Path(__file__).parent / "firebase-service-account.json")
    downloads = Path(os.environ.get("USERPROFILE", "")) / "Downloads"
    if downloads.exists():
        candidates.extend(sorted(downloads.glob("*firebase-adminsdk*.json")))

    for c in candidates:
        if c.exists():
            try:
                project = json.loads(c.read_text(encoding="utf-8")).get("project_id", "?")
            except Exception:
                project = "?"
            if project != PROJECT_ID:
                print(f"WARNING: {c.name} has project_id='{project}' but app needs '{PROJECT_ID}'.")
                print(f"         This key CANNOT access gs://{BUCKET} (403). Generate a key from")
                print(f"         Firebase Console -> {PROJECT_ID} -> Project Settings -> Service Accounts.")
            return c
    return None


def remote_url(remote_path: str) -> str:
    return f"{PUBLIC_BASE}/{quote(remote_path, safe='')}?alt=media"


def plan_uploads() -> list[dict]:
    """Map local polished files -> remote paths (+ hindi pairing)."""
    if not ASSETS_OUT.exists():
        print(f"No assets dir: {ASSETS_OUT}")
        return []

    en_files = {}
    hi_files = {}
    for f in sorted(ASSETS_OUT.glob("lecture_*_final*.mp4")):
        stem = f.stem  # e.g. lecture_1_10_1_final | lecture_1_10_1_HI_final | lecture_1_10_1_final_HI
        if "_HI" in stem.upper():
            base = (
                stem.upper()
                .replace("_HI_FINAL", "")
                .replace("_FINAL_HI", "")
                .replace("LECTURE_", "lecture_")
                .lower()
            )
            hi_files[base] = f
        else:
            en_files[stem] = f

    plan = []
    for stem, f in en_files.items():
        code = stem.replace("lecture_", "").replace("_final", "")  # 1_10_1
        course = code.split("_")[0]
        remote_en = f"videos/course_{course}/lecture_{code}.mp4"
        hi_local = hi_files.get(stem)
        remote_hi = f"videos/course_{course}/lecture_{code}_HI.mp4" if hi_local else None
        plan.append({
            "key": stem,               # manifest key == bundled videoUrl
            "code": code,
            "course": course,
            "en_local": f,
            "en_remote": remote_en,
            "hi_local": hi_local,
            "hi_remote": remote_hi,
        })
    return plan


def main():
    args = sys.argv[1:]
    dry_run = "--dry-run" in args
    list_only = "--list" in args
    sa_path = None
    if "--sa" in args:
        i = args.index("--sa")
        if i + 1 < len(args):
            sa_path = args[i + 1]

    import firebase_admin
    from firebase_admin import credentials, storage

    sa = find_service_account(sa_path)
    if not sa:
        print("ERROR: no service account JSON found. See --sa option.")
        print("Fix: Firebase Console -> tradelab-4f858 -> Project Settings ->")
        print("     Service Accounts -> Generate New Private Key ->")
        print("     save as nlm/firebase-service-account.json")
        sys.exit(1)
    print(f"Service account: {sa}")

    cred = credentials.Certificate(str(sa))
    app = firebase_admin.initialize_app(cred, {"storageBucket": BUCKET})
    bucket = storage.bucket(app=app)

    if list_only:
        print(f"== gs://{BUCKET}/videos/")
        for b in bucket.list_blobs(prefix="videos/"):
            print(f"  {b.name}  ({b.size / 1e6:.1f} MB)")
        return

    plan = plan_uploads()
    if not plan:
        print("No lecture_*_final.mp4 files found. Run polish first.")
        return

    hi_count = sum(1 for p in plan if p["hi_local"])
    print(f"Found {len(plan)} EN videos ({hi_count} with HI variants)")

    manifest = {"version": 1, "generatedAt": "", "videos": {}}
    ok = fail = 0

    for p in plan:
        entry = {}
        try:
            if dry_run:
                hi_note = f" + HI({p['hi_local'].name})" if p["hi_local"] else ""
                print(f"  [dry] {p['en_local'].name} -> {p['en_remote']}{hi_note}")
                ok += 1
            else:
                blob = bucket.blob(p["en_remote"])
                blob.upload_from_filename(str(p["en_local"]), content_type="video/mp4")
                entry["en"] = remote_url(p["en_remote"])
                ok += 1
                print(f"  OK {p['en_remote']}")
            if p["hi_local"]:
                if dry_run:
                    print(f"  [dry] {p['hi_local'].name} -> {p['hi_remote']}")
                else:
                    blob = bucket.blob(p["hi_remote"])
                    blob.upload_from_filename(str(p["hi_local"]), content_type="video/mp4")
                    entry["hi"] = remote_url(p["hi_remote"])
                    print(f"  OK {p['hi_remote']}")
        except Exception as e:
            fail += 1
            print(f"  FAILED {p['key']}: {type(e).__name__} {str(e)[:120]}")
        if entry:
            manifest["videos"][p["key"]] = entry

    if dry_run:
        print(f"\n[dry-run] would upload {ok}, manifest entries: {len(manifest['videos'])}")
        return

    # Manifest version = previous remote version + 1 (cache-busting for clients)
    try:
        prev = bucket.blob(MANIFEST_REMOTE)
        if prev.exists():
            manifest["version"] = json.loads(prev.download_as_text()).get("version", 1) + 1
    except Exception:
        pass
    from datetime import datetime, timezone
    manifest["generatedAt"] = datetime.now(timezone.utc).isoformat(timespec="seconds")

    try:
        bucket.blob(MANIFEST_REMOTE).upload_from_string(
            json.dumps(manifest, ensure_ascii=False, indent=1),
            content_type="application/json",
        )
        print(f"\nManifest v{manifest['version']} uploaded: {MANIFEST_REMOTE}")
    except Exception as e:
        print(f"\nMANIFEST UPLOAD FAILED: {e}")
        Path(__file__).parent.joinpath("manifest.local.json").write_text(
            json.dumps(manifest, ensure_ascii=False, indent=1), encoding="utf-8"
        )
        print("Saved locally as nlm/manifest.local.json — upload manually.")

    print(f"\nDone: {ok} uploaded, {fail} failed")
    print("Reminder: deploy storage.rules (firebase deploy --only storage) so videos/** is public-read.")


if __name__ == "__main__":
    main()
