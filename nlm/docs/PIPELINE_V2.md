# NLM Pipeline Manager v2 (`pipeline.py`)

**The authoritative doc for the unified pipeline.** Legacy script docs live in `PIPELINE.md`.

Manager for generating 204 lecture videos (6 courses × 68 chapters) across 7 Google
NotebookLM accounts — menu-driven, fully autonomous in RUN ALL mode, self-healing,
quota-aware, and multi-project ready via `config.json`.

---

## Quick Start

```
cd nlm
python pipeline.py          # interactive menu (0-8)
python pipeline.py --run-all   # (parked feature — see Backlog)
```

Typical campaign flow:
```
7. AUTH      → verify/refresh all accounts (silent L3 heal first, popups last)
2. CREATE    → build missing notebooks + upload sources
6. RUN ALL   → autonomous until 204/204 generated AND downloaded
```

---

## Menu Modes

| # | Mode | What it does |
|---|------|--------------|
| 1 | CHECK | Audit every account's notebooks; relink `notebook_id`s; refresh video/source statuses. Read-only against the cloud. |
| 2 | CREATE | Build missing notebooks + upload lecture sources. Dedupe-safe (prefers keeping video-bearing notebooks). Live-saves per source. |
| 3 | GENERATE | Trigger video generation. Daily-cap pre-stop, rate-limit classification, transient retry, daily parking with auto-resume. |
| 4 | DOWNLOAD | Download completed videos. CSV `download_status` is the truth; self-heals manual copies and vanished files. Cloud `artifact_url` is never overwritten. |
| 5 | STATUS | Totals, per-account breakdown, quota snapshot, cleanup-queue count. |
| 6 | RUN ALL | Autonomous loop: CHECK → CREATE → DOWNLOAD → GENERATE → STATUS until 204/204 **complete and downloaded**. Auto-sleeps at quota resets. Sweeps cleanup queue each cycle. |
| 7 | AUTH | Validate + refresh all accounts. Ladder: **L3 silent headless re-mint** → browser-cookies → fresh login → master token. Clears `blocked` on success; drains that account's cleanup queue. |
| 8 | REASSIGN | Ownership engine: stranded-rescue + capacity rebalance + cleanup-queue processing. Always asks Y/n before moving anything. |
| 0 | EXIT | Save + quit. |

---

## State Files (the pipeline's brain)

| File | Written by | Contents |
|------|-----------|----------|
| `pipeline_state.csv` | All modes | One row per lecture. **The single source of truth for ownership + progress.** |
| `auth_state.json` | AUTH, failures | Per-account health: `auth_status`, **`blocked`**, `last_auth_fail`, `last_reauth`, `reauth_method`. |
| `quota_state.json` | GENERATE | Daily usage + `exhausted_until` parking. Survives restarts. |
| `cleanup_queue.json` | REASSIGN | Orphan notebooks awaiting deletion on their old owner accounts. |
| `config.json` | You (hand-edit) | All knobs — see Config Reference. |
| `accounts.json` | You | Account roster: email / profile / tier. |
| `pending_allocation.json` | You | Original lecture→account allocation (28 each, 8 unallocated). |
| `pipeline_state.recover.csv` | (fallback) | Written only when the CSV is unwritable even after force-kill. Auto-loaded on next start if newer. |

### `pipeline_state.csv` schema

| Column | Values | Meaning |
|--------|--------|---------|
| `lecture_code` | `1.2.3` | Primary key |
| `assigned_email` | email / '' | **Owner.** '' = unallocated (8 legacy rows, already generated, ignored). |
| `notebook_id` | id / '' | Notebook on the owner account. Cleared on reassignment. |
| `source_status` | missing / uploaded / processing / failed | Source upload state. |
| `video_status` | missing / generating / complete / failed | **Cloud** generation lifecycle. |
| `download_status` | '' / downloaded / failed | **Local** download lifecycle (independent axis). |
| `artifact_url` | URL | **Cloud URL — never overwritten** by downloads (re-download source preserved). |
| `last_checked` | timestamp | Last audit touch. |

Older CSVs migrate transparently: missing columns get defaults on load; first save writes the new header.

---

## Core Subsystems

### 1. Cookie Rotation (throttled)
Before every mode, each account's `storage_state.json` PSIDTS tokens are refreshed via
Google's `RotateCookies` endpoint — **only if the file is older than
`rotate_min_interval_sec` (default 600s)**. Google's own cadence; prevents the
robot-like 7-rotations-per-run signature. Disable entirely:
`"rotate_before_session": false`. Blocked accounts are never rotated.

### 2. L3 Silent Self-Heal (`NOTEBOOKLM_HEADLESS_REAUTH=1`, set automatically)
Each profile keeps a persistent Playwright browser whose Google SSO outlives the API
cookies. When an API call dies mid-session, the library silently re-mints cookies from
that browser and retries — no popup, no password. **Works only while the profile's SSO
is alive.** If a revocation wave kills SSO too, L3 fails loudly and fresh logins are
needed (see AUTH ladder). AUTH also tries L3 explicitly as Method 0.

### 3. Auth Blocking
Any auth-expiry failure anywhere → the account is instantly marked
`blocked: true` in `auth_state.json` and **skipped by every mode** (including rotation)
until a successful option-7 re-auth clears it. Mid-mode deaths are visible to the
running loop **immediately** (runtime blocked set unioned with the file) — dead
accounts are skipped on their very next iteration, never retried. Startup prints a
banner listing blocked accounts. RUN ALL aborts cleanly if *all* accounts become
blocked. Ctrl+C anywhere exits cleanly with *"Stopped — progress saved"*.

### 4. Rate-Limit Classification + Quota Tracking
Two distinct server refusals, handled differently:

| Type | Signature | Response |
|------|-----------|----------|
| **Transient** (HTTP 429) | `retry_after` present | Wait `min(retry_after, 300s)`, retry same lecture once |
| **Daily exhaustion** | `rpc_code=USER_DISPLAYABLE_ERROR` | Park account until quota reset |

`QuotaTracker` (backed by `quota_state.json`):
- **Pre-stops** at the configured daily cap (`daily_limits`: standard 3, pro 20) — the
  server's 4th refusal is never provoked.
- **Parks** accounts that hit a real daily refusal: `exhausted_until` = next reset.
- **Auto-calibrates**: if the server still refuses past the expected reset, the window
  slides +1h and logs it — the true reset time is learned empirically.
- **Rollover**: counters wipe automatically at the configured reset
  (`quota_reset.timezone/hour`, default UTC 00:00).
- **RUN ALL**: when everything is exhausted (and nothing is pending download), sleeps to
  reset with a live `HH:MM:SS` countdown — **waking every ~10 min
  (`quota_wait_checkpoint_sec`) to CHECK + DOWNLOAD videos that finished while we
  waited**, so completed work is banked continuously instead of sitting on the cloud
  overnight. Standalone GENERATE prints the ETA and exits instead.

### 5. Reassignment Engine (menu 8 + auto-prompt in GENERATE)
Permanent **ownership transfer** in the CSV ledger:

- **Stranded rescue** — pending lectures owned by BLOCKED accounts are redistributed
  across alive accounts, weighted ∝ daily limits.
- **REBALANCE** — when idle capacity vs others' backlog makes finishing faster
  (≥10% improvement), proposes water-fill targets that equalize finish times
  (e.g. 8.3 days → 4 days). Donor loss is spread round-robin so no account keeps a
  laggard tail.
- **Apply** = ownership moves + old notebook severed + enqueued to `cleanup_queue.json`.
- **Structural invariant**: only `missing`/`failed` lectures ever move. Completed videos
  are never orphaned, so queue deletions are always lossless.
- **Divergence guard**: the drain deletes a queued notebook only if the CSV row no
  longer references it. 404s count as cleaned; other errors stay queued.
- **Drain hooks**: after successful re-auth (option 7) · RUN ALL cycle start · menu 8.
- **CHECK ownership guard**: CHECK never relinks a notebook whose lecture's owner is a
  different account (prevents post-reassignment orphans from corrupting ownership).
- After any apply: offers to chain CREATE (rebuilds notebooks + re-uploads sources on
  the new owner) so GENERATE can proceed immediately.

### 6. CSV Lock Enforcement ("sheet killer")
`save_state` writes atomically (tmp + `os.replace`). If Excel (or anything) holds the
CSV: the **Restart Manager API** identifies the exact locking process → `taskkill /F` →
loud announcement → retry. If it still can't write (elevated locker), progress buffers
to `pipeline_state.recover.csv` (auto-loaded on next start if newer). **Never crashes.**
A guard refuses to overwrite a populated CSV with an empty state (accident-proofing).
All modes live-save continuously (per account / per upload / per status change /
per download).

### 7. Self-Healing Downloads
`download_status` drives everything: `downloaded` skips; vanished files re-download;
files present but unmarked are recognized (manual-copy friendly); failures retry next
cycle. Mode 4 opens with a **readiness poll** — `generating` rows are polled against
the server first, so freshly finished videos download immediately without a separate
CHECK run. `pending_downloads_exist()` gates GENERATE's quota-sleep — **downloads are
never starved by generation waits** — and RUN ALL only declares victory when
everything is downloaded, not merely generated. After a successful AUTH run, option 7
offers to **bank immediately** (DOWNLOAD + CREATE) with the freshly-alive accounts.

### 8. Duplicate-Dedupe Hardening
CREATE's dedupe prefers keeping the duplicate that holds a completed video, so dedupe
can never destroy finished work.

### 9. Locale Fix
`TZ` env var is popped at startup — Windows' CRT mis-parses IANA names and was shifting
all timestamps by hours. All logs now use true system locale.

---

## Config Reference (`config.json`)

| Key | Default | Meaning |
|-----|---------|---------|
| `project_name` / `notebook_prefix` | TradeLab | Naming. |
| `lecture_data_file` | `../app/src/main/assets/academy_data_v2.json` | Lecture source of truth. |
| `delay_between_lectures_sec` | 60 | Pause between generation triggers (successes only). |
| `delay_between_accounts_sec` | 30 | RUN ALL inter-cycle pause. |
| `rotate_before_session` | true | Master switch for pre-flight rotation. |
| `rotate_min_interval_sec` | 600 | Skip rotation if storage file younger than this. |
| `quota_reset.timezone` / `.hour` | UTC / 0 | When daily quotas refresh. |
| `quota_wait_checkpoint_sec` | 600 | Quota-wait banking checkpoint interval (CHECK + DOWNLOAD). |
| `daily_limits` | `{"default": 3, "pro": 20}` | Confirmed server caps (empirical 2026-08-23/24). |
| `video_format` / `video_style` / `language` | short / auto_select / en | Generation params. |
| `output_dir` | assets | Download target. |

---

## Runbooks

### Daily / fresh-session flow
```
7. AUTH  (silent heals expected; popups only if SSO died)
4. DOWNLOAD   → bank anything completed
2. CREATE     → build anything missing
6. RUN ALL    → autonomous: triggers, downloads, quota-parks, cleanup sweeps
```

### After a revocation wave (sessions die while idle)
1. Menu 8 → answer N at cleanup (accounts blocked → nothing to drain).
2. `7. AUTH` → L3 attempts silent heal; popups only where SSO died. **Space logins**
   (no 7-in-7-minutes — that pattern invites waves). Work each account immediately
   after it heals (DOWNLOAD / CREATE / GENERATE).
3. Blocked flags clear automatically on each success; cleanup queue drains via hooks.

### Rebalance check
Menu 8 → analysis prints current vs balanced finish estimate. Applies only with Y.
After big moves, chain CREATE when prompted.

---

## Diagnostics

- `probe_auth.py` — instant per-account alive/dead check (no state changes beyond
  normal cookie refresh).
- `5. STATUS` — totals + quota + cleanup queue at a glance.
- `nlm\logs\` — *(parked)* persistent log files; console history is currently the record.

---

## Parked Backlog (post-campaign)

1. **Decoupling** — per-project folders, one codebase (config keys already exist).
2. **Dashboard server (Phase B/C)** — `dashboard.py` job runner + SSE logs + click-to-
   re-auth + full Lectures table. `dashboard.html` (static) already covers Accounts/
   Config/Progress viewing+editing via File System Access API.
3. **Resilience kit** — `--run-all` flag, `.pipeline.lock`, watchdog bat, log files.
4. **Password vault (Plan B)** — DPAPI-encrypted creds + TOTP; build only if L3 fails a
   revocation wave (save logs from the next wave to decide).
5. **VPS migration** — datacenter-IP risk; between campaigns only.

---

*Last updated: 2026-08-24 · pipeline.py v2 with reassignment engine, quota tracking,
lock-killer, L3 self-heal, and download lifecycle.*
