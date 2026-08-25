#!/usr/bin/env python3
"""
Upload academy videos to Firebase Storage + generate the video manifest (Epic 27).

Auth (first available wins):
    1. Service account JSON (--sa PATH, nlm/firebase-service-account.json, or
       Downloads/*firebase-adminsdk*.json) via firebase-admin — BYPASSES storage rules.
    2. Firebase CLI login token (firebase-tools refresh token -> OAuth access token,
       REST uploads) — uses the logged-in account's IAM (must allow Storage writes).

Public READ comes from storage.rules (videos/** read:true, writes locked).

Usage:
    python upload_to_firebase.py                  # upload all + regenerate manifest
    python upload_to_firebase.py --dry-run        # plan only, no uploads
    python upload_to_firebase.py --list           # list remote videos/
    python upload_to_firebase.py --sa PATH        # explicit service account JSON
    python upload_to_firebase.py --probe          # write/delete a tiny test object

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

FIREBASE_CLI_CLIENT_ID = (
    "563584335869-fgrhgmd47bqnekij5i8b5pr03ho849e6.apps.googleusercontent.com"
)
FIREBASE_CLI_CLIENT_SECRET = "j9iVZfS8kkCEFUa66jN8L6KJ"
CONFIGSTORE = Path.home() / ".config" / "configstore" / "firebase-tools.json"


# ── Auth ──────────────────────────────────────────────────────────────────────

def find_service_account(explicit):
    """Locate the SA JSON; None if missing or wrong project."""
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
                print(f"WARNING: {c.name} has project_id='{project}' != '{PROJECT_ID}' — skipping.")
                print("         (Play Console keys have no Firebase IAM. Generate one from")
                print(f"          Firebase Console -> {PROJECT_ID} -> Project Settings -> Service Accounts.)")
                continue
            return c
    return None


def get_cli_access_token():
    """Access token from the firebase-tools login session (cached, auto-refresh).

    firebase-tools 15 stores the session in configstore with a cached
    access_token + expires_at. When stale, ANY authenticated CLI command
    (e.g. `firebase apps:list`) refreshes it in-place — we trigger that
    via subprocess and re-read.
    """
    import subprocess
    import time

    def read_cached():
        try:
            tokens = json.loads(CONFIGSTORE.read_text(encoding="utf-8")).get("tokens", {})
            at = tokens.get("access_token", "")
            expires_at = tokens.get("expires_at", 0)
            if at and expires_at > (time.time() * 1000) + 120_000:
                return at
        except Exception:
            pass
        return None

    cached = read_cached()
    if cached:
        return cached

    # Stale/missing → make the CLI refresh its session, then re-read
    try:
        subprocess.run(
            ["firebase", "apps:list", "--project", PROJECT_ID],
            capture_output=True, timeout=120,
        )
    except Exception:
        pass
    cached = read_cached()
    if cached:
        return cached

    print("ERROR: firebase CLI session has no fresh token.")
    print("Fix: run `firebase login` (or any firebase command) and re-run this script.")
    return None


class Auth:
    """Resolved auth context: either firebase-admin app or a REST bearer token."""

    def __init__(self):
        self.sa_path = None
        self.admin_app = None
        self.token = None

    def resolve(self, explicit_sa):
        self.sa_path = find_service_account(explicit_sa)
        if self.sa_path:
            import firebase_admin
            from firebase_admin import credentials
            cred = credentials.Certificate(str(self.sa_path))
            self.admin_app = firebase_admin.initialize_app(
                cred, {"storageBucket": BUCKET, "projectId": PROJECT_ID}
            )
            print(f"Auth: service account ({self.sa_path.name})")
        else:
            self.token = get_cli_access_token()
            if not self.token:
                print("ERROR: no service account AND no firebase CLI login found.")
                print("Fix A: firebase login  (then re-run)")
                print("Fix B: save a tradelab-4f858 SA key as nlm/firebase-service-account.json")
                sys.exit(1)
            print("Auth: firebase CLI login token (REST)")

    @property
    def mode(self):
        return "admin" if self.admin_app else "rest"


# ── Storage ops (dual-mode) ───────────────────────────────────────────────────

def upload_file(auth, local_path, remote_path, content_type):
    if auth.mode == "admin":
        from firebase_admin import storage
        blob = storage.bucket(app=auth.admin_app).blob(remote_path)
        blob.upload_from_filename(str(local_path), content_type=content_type)
    else:
        import urllib.request
        url = f"{PUBLIC_BASE}?uploadType=media&name={quote(remote_path, safe='')}"
        data = Path(local_path).read_bytes()
        req = urllib.request.Request(url, data=data, method="POST", headers={
            "Authorization": f"Bearer {auth.token}",
            "Content-Type": content_type,
        })
        urllib.request.urlopen(req, timeout=600)

def upload_text(auth, text, remote_path, content_type):
    if auth.mode == "admin":
        from firebase_admin import storage
        storage.bucket(app=auth.admin_app).blob(remote_path).upload_from_string(
            text, content_type=content_type
        )
    else:
        import urllib.request
        url = f"{PUBLIC_BASE}?uploadType=media&name={quote(remote_path, safe='')}"
        req = urllib.request.Request(url, data=text.encode("utf-8"), method="POST", headers={
            "Authorization": f"Bearer {auth.token}",
            "Content-Type": content_type,
        })
        urllib.request.urlopen(req, timeout=60)

def download_text(auth, remote_path):
    try:
        if auth.mode == "admin":
            from firebase_admin import storage
            blob = storage.bucket(app=auth.admin_app).blob(remote_path)
            return blob.download_as_text() if blob.exists() else None
        import urllib.request
        url = f"{PUBLIC_BASE}/{quote(remote_path, safe='')}?alt=media"
        req = urllib.request.Request(url, headers={"Authorization": f"Bearer {auth.token}"})
        return urllib.request.urlopen(req, timeout=30).read().decode("utf-8")
    except Exception:
        return None

def list_remote(auth, prefix="videos/"):
    if auth.mode == "admin":
        from firebase_admin import storage
        return [(b.name, b.size) for b in storage.bucket(app=auth.admin_app).list_blobs(prefix=prefix)]
    import urllib.request
    url = f"{PUBLIC_BASE}?prefix={quote(prefix, safe='')}&maxResults=1000"
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {auth.token}"})
    resp = json.loads(urllib.request.urlopen(req, timeout=30).read())
    return [(i["name"], int(i.get("size", 0))) for i in resp.get("items", [])]

def delete_remote(auth, remote_path):
    if auth.mode == "admin":
        from firebase_admin import storage
        storage.bucket(app=auth.admin_app).blob(remote_path).delete()
    else:
        import urllib.request
        url = f"{PUBLIC_BASE}/{quote(remote_path, safe='')}"
        req = urllib.request.Request(url, method="DELETE", headers={
            "Authorization": f"Bearer {auth.token}"
        })
        urllib.request.urlopen(req, timeout=30)


# ── Planning ──────────────────────────────────────────────────────────────────

def remote_url(remote_path):
    return f"{PUBLIC_BASE}/{quote(remote_path, safe='')}?alt=media"


def plan_uploads():
    """Map local polished files -> remote paths (+ hindi pairing)."""
    if not ASSETS_OUT.exists():
        print(f"No assets dir: {ASSETS_OUT}")
        return []

    en_files, hi_files = {}, {}
    for f in sorted(ASSETS_OUT.glob("lecture_*_final*.mp4")):
        stem = f.stem  # lecture_1_10_1_final | lecture_1_10_1_HI_final | lecture_1_10_1_final_HI
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
        hi_local = hi_files.get(stem)
        plan.append({
            "key": stem,
            "code": code,
            "course": course,
            "en_local": f,
            "en_remote": f"videos/course_{course}/lecture_{code}.mp4",
            "hi_local": hi_local,
            "hi_remote": f"videos/course_{course}/lecture_{code}_HI.mp4" if hi_local else None,
        })
    return plan


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    args = sys.argv[1:]
    dry_run = "--dry-run" in args
    list_only = "--list" in args
    probe = "--probe" in args
    sa_path = None
    if "--sa" in args:
        i = args.index("--sa")
        if i + 1 < len(args):
            sa_path = args[i + 1]

    auth = Auth()
    auth.resolve(sa_path)

    if probe:
        try:
            upload_text(auth, "probe-ok", "videos/.probe.txt", "text/plain")
            print("Probe write: OK (videos/.probe.txt)")
            delete_remote(auth, "videos/.probe.txt")
            print("Probe delete: OK — write access confirmed.")
        except Exception as e:
            print(f"PROBE FAILED: {type(e).__name__} {str(e)[:200]}")
        return

    if list_only:
        print(f"== gs://{BUCKET}/{MANIFEST_REMOTE[:-14]}")
        for name, size in list_remote(auth):
            print(f"  {name}  ({size / 1e6:.1f} MB)")
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
                upload_file(auth, p["en_local"], p["en_remote"], "video/mp4")
                entry["en"] = remote_url(p["en_remote"])
                ok += 1
                print(f"  OK {p['en_remote']}")
            if p["hi_local"]:
                if dry_run:
                    print(f"  [dry] {p['hi_local'].name} -> {p['hi_remote']}")
                else:
                    upload_file(auth, p["hi_local"], p["hi_remote"], "video/mp4")
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
        prev = download_text(auth, MANIFEST_REMOTE)
        if prev:
            manifest["version"] = json.loads(prev).get("version", 1) + 1
    except Exception:
        pass
    from datetime import datetime, timezone
    manifest["generatedAt"] = datetime.now(timezone.utc).isoformat(timespec="seconds")

    try:
        upload_text(
            auth,
            json.dumps(manifest, ensure_ascii=False, indent=1),
            MANIFEST_REMOTE,
            "application/json",
        )
        print(f"\nManifest v{manifest['version']} uploaded: {MANIFEST_REMOTE}")
    except Exception as e:
        print(f"\nMANIFEST UPLOAD FAILED: {e}")
        Path(__file__).parent.joinpath("manifest.local.json").write_text(
            json.dumps(manifest, ensure_ascii=False, indent=1), encoding="utf-8"
        )
        print("Saved locally as nlm/manifest.local.json — upload manually.")

    print(f"\nDone: {ok} uploaded, {fail} failed")
    print("Public manifest URL:")
    print(f"  {remote_url(MANIFEST_REMOTE)}")


if __name__ == "__main__":
    main()
