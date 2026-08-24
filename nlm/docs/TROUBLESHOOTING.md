# NLM Pipeline Troubleshooting Guide

> **For `pipeline.py` (menu 0-8) scenarios** — the unified manager auto-handles most
> of the situations below. This guide covers what the automation does for you, plus
> the cases that still need a human. Architecture reference:
> [`PIPELINE_V2.md`](./PIPELINE_V2.md).

## Common Issues & Solutions

### ✅ AUTO-HANDLED by `pipeline.py` (no action needed)

| Symptom | What the pipeline does automatically |
|---------|--------------------------------------|
| `Authentication expired or invalid` on one account | Marks it **BLOCKED** in `auth_state.json`, skips it everywhere (incl. rotation), keeps running on the rest. Clear with option 7 re-auth. |
| `RateLimitError` with `retry_after` (HTTP 429) | Waits `min(retry_after, 300s)`, retries the same lecture once. |
| `RateLimitError` `USER_DISPLAYABLE_ERROR` (daily quota) | Parks the account in `quota_state.json` until the configured reset; RUN ALL auto-sleeps with a live countdown and resumes. |
| Daily cap reached (3 std / 20 pro) | **Pre-stops locally** — the 4th server request is never sent. |
| `pipeline_state.csv` locked by Excel | **Kills the locking process** (Restart Manager + taskkill, announced in the log), retries atomically. Unkillable lock → buffers to `pipeline_state.recover.csv`, auto-loaded next start. |
| Ran DOWNLOAD but "nothing new" right after triggering | Mode 4 **auto-polls `generating` rows** against the server first — freshly finished videos download immediately, no separate CHECK needed. |
| Videos finished while RUN ALL was quota-parked | The quota-wait wakes every ~10 min (`quota_wait_checkpoint_sec`) to CHECK + DOWNLOAD — completions are banked continuously, never stranded overnight. |
| Ctrl+C | Clean exit: *"Stopped — progress saved."* Incremental saves mean at most one row reverts; restart self-heals via artifact re-checks. |
| Storage cookies die mid-run | **L3 silent headless re-mint** from the persistent browser profile (`NOTEBOOKLM_HEADLESS_REAUTH=1` is set automatically). No popup. Fails only if the profile's Google SSO is dead too → see AUTH ladder below. |
| Timestamps look GMT/wrong | Fixed — the pipeline pops the `TZ` env var (Windows CRT mis-parses IANA names). |
| Blocked account's videos stranded | Option `8. REASSIGN` proposes permanent ownership transfer to alive accounts (weighted ∝ daily caps), chains CREATE, and queues old notebooks for cleanup. |

### AUTH recovery ladder (option 7) — in order
1. **L3 silent headless re-mint** — uses the profile's persistent browser SSO. No
   popup. Fails with *"persisted browser profile's Google session is expired"* when a
   revocation wave killed SSO too.
2. Browser-cookies extraction — currently unavailable (needs `rookiepy`, no wheel for
   Python 3.14).
3. **Fresh login popup** — the reliable path. **Space logins** (never 7 in a few
   minutes — rapid-login patterns invite session-revocation waves). Work each account
   immediately after it heals.
4. Master token — only if `master_token.json` exists (none do currently).

### Session revocation waves (2026-08-23/24 incident pattern)
Google killed sessions **while idle**, twice in two days, across all accounts. What
worked: option 7 fresh logins → immediate work per healed account. What's armed:
L3 self-heal (works when SSO survives the wave — save the log next wave to decide
whether the password vault is needed).

### Manual recovery sequence (all accounts dead)
```
1. 7. AUTH  → complete popups one at a time (space them)
2. 4. DOWNLOAD  → bank completed videos immediately
3. 2. CREATE    → rebuild anything pending on new/moved owners
4. 6. RUN ALL   → resume autonomous operation
```

### CSV state reference
- `download_status`: '' → downloaded / failed. `artifact_url` always keeps the CLOUD
  URL (local path is derivable; re-download source preserved).
- `cleanup_queue.json`: orphan notebooks awaiting deletion on their old owner; drained
  automatically when that owner is next alive. Divergence-guarded — never deletes a
  notebook the CSV still owns.

## Common Issues & Solutions (legacy scripts)

---

## 1. Authentication Issues

### Problem: `Authentication expired or invalid`
```
ERROR: Authentication expired or invalid. Final URL: https://accounts.google.com/...
Run 'notebooklm login' to re-authenticate.
```

**Cause**: OAuth token expired (typically after 1-2 hours)

**Solution**:
```bash
# Re-authenticate specific account
notebooklm login --browser chrome --fresh --storage "C:/Users/Atul/.notebooklm/profiles/<profile>/storage_state.json"

# Or logout and re-login
notebooklm auth logout
notebooklm login --browser chrome --fresh --storage "path/to/storage_state.json"
```

**Prevention**: 
- Run `notebooklm login --fresh` before long batch runs
- Check token validity before starting batch

---

### Problem: `FATAL: Authentication expired...` mid-batch

**Cause**: Token expired during long-running batch

**Solution**:
```bash
# Re-auth and restart
notebooklm login --browser chrome --fresh --storage "path/to/storage_state.json"

# Then restart script
python sequential_retry.py
```

---

## 2. Quota/Rate Limit Issues

### Problem: `RPC CREATE_NOTEBOOK failed... rpc_code=8` (Resource Exhausted)

**Cause**: Hit daily/hourly quota limit for video generation or notebook creation

**Symptoms**:
- `RPCError: The server rejected this request (resource exhausted)`
- Repeated failures after initial success
- All accounts failing simultaneously

**Immediate Action**:
```bash
# 1. Stop all scripts immediately
Ctrl+C

# 2. Wait for quota reset (typically midnight UTC)
# 3. Reduce concurrency
# 4. Resume with single account
```

**Prevention**:
```python
# Use controlled concurrency
MAX_CONCURRENT_PER_ACCOUNT = 1
GLOBAL_MAX_CONCURRENT = 2
DELAY_BETWEEN_REQUESTS = 60  # seconds
DELAY_BETWEEN_ACCOUNTS = 300  # 5 minutes
```

**Recovery**:
```bash
# Wait 30-60 minutes, then retry with single account
python sequential_retry.py
```

---

### Problem: `RPC CREATE_NOTEBOOK failed... rpc_code=16` (Unauthenticated)

**Cause**: Auth token expired during operation

**Solution**:
```bash
# Re-authenticate the specific account
notebooklm login --browser chrome --fresh --storage "C:/Users/Atul/.notebooklm/profiles/<profile>/storage_state.json"
```

---

### Problem: `RateLimitError` repeated after retries

**Cause**: Hitting quota faster than backoff

**Solution**: Increase delays
```python
# In script configuration
BASE_DELAY = 60      # Start at 60s
MAX_DELAY = 300      # Cap at 5 minutes
RESOURCE_EXHAUSTED_DELAYS = [60, 120, 300, 600]  # 1m, 2m, 5m, 10m
```

---

## 3. File/Path Issues

### Problem: `File not found: lecture_X_X_X.md`

```
FAIL 1.11.2: File not found: C:\Users\Atul\AndroidStudioProjects\TradeLab\app\src\main\assets\lectures\course_1\lecture_1_11_2.md
```

**Cause**: Lecture `.md` files not extracted for Courses 2-6

**Solution**:
```bash
# Run extraction for all courses
python extract_all_courses_fixed.py
```

**Verification**:
```bash
ls nlm/lectures/course_*/lecture_*.md | wc -l
# Should show 204 files
```

---

### Problem: `.md` files exist but different naming

**Cause**: Lecture code format mismatch

**Check**: 
```bash
# Expected format: lecture_1_11_2.md (from "Lecture 1.11.2: Title")
# Actual: check actual filename
ls nlm/lectures/course_1/lecture_1_11_2.md
```

**Fix**: Ensure extraction uses consistent naming:
```python
code = lecture["title"].split(":")[0].replace("Lecture ", "").strip()
filename = f"lecture_{code.replace('.', '_')}.md"
```

---

## 4. Video Generation Issues

### Problem: Video generation stuck at "pending" or "in_progress"

**Cause**: NotebookLM processing queue backed up

**Solution**:
```bash
# Wait longer (up to 45 min per video)
# Check batch_progress.json for status
# If stuck > 60 min, may need to recreate notebook
```

### Problem: Video generated but `is_completed = False`

**Cause**: Video artifact status not updated

**Check**:
```python
# Check artifact status
for artifact in artifacts:
    if artifact.kind == ArtifactType.VIDEO:
        print(f"Status: {artifact.status_str}")
        print(f"is_completed: {artifact.is_completed}")
```

---

## 5. Download Issues

### Problem: `ArtifactsAPI object has no attribute 'download_artifact'`

**Cause**: Wrong method name

**Fix**:
```python
# Wrong
await client.artifacts.download_artifact(nb_id, artifact.id, output)

# Correct - use download_video for VIDEO artifacts
await client.artifacts.download_video(notebook_id, output_path)
```

### Problem: Downloaded file is 0 bytes or corrupt

**Cause**: Download interrupted or artifact not ready

**Solution**:
```python
# Check file size after download
if output.exists() and output.stat().st_size > 1024*1024:  # > 1MB
    print("Download OK")
else:
    output.unlink()  # Delete corrupt file
    # Retry download
```

---

## 6. FFmpeg/Processing Issues

### Problem: `ffmpeg not found` or `command not found`

**Solution**:
```bash
# Install ffmpeg
# Windows: Download from ffmpeg.org, add to PATH
# Or use chocolatey:
choco install ffmpeg

# Verify
ffmpeg -version
```

### Problem: `ffmpeg concat failed` / audio sync issues

**Solution**: Use explicit concat filter
```bash
ffmpeg -y \
  -i intro.mp4 -i lecture.mp4 -i outro.mp4 \
  -filter_complex "[0:v][1:v][2:v]concat=n=3:v=1:a=0[outv]" \
  -map "[outv]" -c:v libx264 -pix_fmt yuv420p output.mp4
```

---

## 7. Firebase Upload Issues

### Problem: `gcloud storage cp` fails / permission denied

**Solution**:
```bash
# Authenticate gcloud
gcloud auth login
gcloud auth application-default login

# Set project
gcloud config set project tradelab-4f858

# Test
gcloud storage ls gs://tradelab-4f858.firebasestorage.app/
```

### Problem: Upload succeeds but URL not accessible

**Cause**: Object not public

**Fix**:
```bash
# Make object publicly readable
gcloud storage objects update gs://bucket/video.mp4 --add-acl-grant=allUsers:READER

# Or use signed URLs for private access
```

---

## 8. Debugging Commands

### Check Current Status
```bash
# Audit all accounts
python audit_notebooks.py

# Check specific account
python audit_notebooks.py --account atulkpal@gmail.com

# Dry run delete duplicates
python audit_notebooks.py --delete  # (dry run first!)

# Actually delete
python audit_notebooks.py --delete
```

### View Progress
```bash
# Real-time monitor
powershell -File monitor_progress.ps1

# Quick stats
python -c "
import json
d=json.load(open('nlm/batch_results.json'))
c=sum(1 for v in d['lectures'].values() if v.get('status')=='complete')
print(f'Complete: {c}/{len(d[\"lectures\"])}')
"
```

### View Logs
```bash
# Generation logs
Get-Content nlm/batch_generation_log.txt -Tail 50

# Output logs
Get-Content nlm/batch_output.log -Tail 50

# Error logs
Get-Content nlm/batch_error.log -Tail 50
```

---

## 9. Emergency Procedures

### Quota Exhausted Mid-Batch
1. **Stop** all scripts (`Ctrl+C`)
2. **Wait** 30-60 minutes (or until midnight UTC)
3. **Reduce** concurrency to single account
4. **Resume** with `sequential_retry.py`

### Multiple Accounts Failed Auth
```bash
# Re-auth all
for profile in default ashwathai boss_studio hi_jumpdroid iiidem_km promptwala paulritu; do
    notebooklm login --browser chrome --fresh --storage "C:/Users/Atul/.notebooklm/profiles/$profile/storage_state.json"
done
```

### Complete Reset
```bash
# 1. Delete all notebooks (keep first copy)
python audit_notebooks.py --delete

# 2. Re-extract lectures
python extract_all_courses_fixed.py

# 3. Recreate notebooks
python create_missing_notebooks.py
```

---

## Quick Reference Card

| Error | Code | Fix |
|-------|------|-----|
| Auth expired | 16 | `notebooklm login --fresh` |
| Quota exhausted | 8 | Wait 30-60m, reduce concurrency |
| Deadline exceeded | 4 | Increase timeout |
| File not found | - | Run `extract_all_courses_fixed.py` |
| Download failed | - | Use `download_video()`, check size |
| Auth logout | - | `notebooklm auth logout` |

---

## Useful Aliases (Add to PowerShell Profile)

```powershell
function nlm-login { 
    param($profile="default")
    & "C:\Users\Atul\AndroidStudioProjects\TradeLab\nlm\venv_nlm\Scripts\notebooklm.exe" login --browser chrome --fresh --storage "C:/Users/Atul/.notebooklm/profiles/$profile/storage_state.json"
}

function nlm-audit { 
    python nlm\audit_notebooks.py 
}

function nlm-download { 
    python nlm\download_all_videos.py 
}

function nlm-monitor { 
    powershell -File nlm\monitor_progress.ps1 
}
```

---

*Last updated: 2026-08-22*
*Add new issues as they're discovered*