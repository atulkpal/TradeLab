#!/usr/bin/env python3
"""
TradeLab NLM Video Generation Pipeline Manager
Unified script for Check, Create, Generate, Download, and Continuous Loop modes.

Configurable via config.json for any NotebookLM project.
"""

import asyncio
import csv
import json
import os
import re
import subprocess
import sys
import io
import time
from datetime import datetime, date, timedelta, timezone
from pathlib import Path

# Windows CRT mis-parses IANA TZ names (e.g. TZ=Asia/Kolkata -> bogus +01:00 offset),
# shifting every datetime.now() by hours. Popping TZ makes Python use the true system locale.
os.environ.pop("TZ", None)

# L3 headless re-auth: when storage cookies die mid-session, the library silently
# re-mints them from the persistent browser profile's Google SSO session (no popup,
# no password) and retries the call. Sessions on this farm die regularly — this
# makes recovery automatic instead of manual.
os.environ.setdefault("NOTEBOOKLM_HEADLESS_REAUTH", "1")

# Restart Manager API (rstrtmgr.dll) — finds which process locks a file (Windows only)
IS_WINDOWS = sys.platform == "win32"
if IS_WINDOWS:
    import ctypes
    from ctypes import wintypes

# Force UTF-8 output on Windows
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')
# Ensure output is flushed immediately (prevents buffered print before browser popups)
sys.stdout.reconfigure(line_buffering=True)

# Ensure notebooklm venv packages are importable
_venv_site = str(Path(__file__).parent / "venv_nlm" / "Lib" / "site-packages")
if _venv_site not in sys.path:
    sys.path.insert(0, _venv_site)

from colorama import Fore, Style, init

init()  # Windows support

# ──────────────────────────────────────────────
# Color palette
# ──────────────────────────────────────────────
CYAN   = Fore.CYAN + Style.BRIGHT
GREEN  = Fore.GREEN + Style.BRIGHT
YELLOW = Fore.YELLOW + Style.BRIGHT
RED    = Fore.RED + Style.BRIGHT
MAGENTA = Fore.MAGENTA + Style.BRIGHT
WHITE  = Fore.WHITE + Style.BRIGHT
RESET  = Style.RESET_ALL

# ──────────────────────────────────────────────
# Helpers
# ──────────────────────────────────────────────
SCRIPT_DIR = Path(__file__).parent
CONFIG_FILE = SCRIPT_DIR / "config.json"
ACCOUNTS_FILE = SCRIPT_DIR / "accounts.json"
STATE_FILE = SCRIPT_DIR / "pipeline_state.csv"
RECOVER_FILE = SCRIPT_DIR / "pipeline_state.recover.csv"
STATE_TMP = SCRIPT_DIR / "pipeline_state.csv.tmp"
QUOTA_STATE_FILE = SCRIPT_DIR / "quota_state.json"

CSV_FIELDS = ['lecture_code', 'title', 'course_id', 'assigned_email',
              'notebook_id', 'source_status', 'video_status',
              'download_status', 'artifact_url', 'last_checked']

_last_lock_notice = 0.0  # dedupe "still locked" warnings (max once/min)

now = lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def log(msg, level="INFO"):
    """Print timestamped log line with color."""
    prefixes = {
        "INFO":     f"{CYAN}[INFO]{RESET}",
        "OK":       f"{GREEN}[+ OK]{RESET}",
        "FAIL":     f"{RED}[X FAIL]{RESET}",
        "WAIT":     f"{YELLOW}[WAIT]{RESET}",
        "PROGRESS": f"{MAGENTA}[>]{RESET}",
        "VIDEO":    f"{CYAN}[VIDEO]{RESET}",
        "NOTEBOOK": f"{CYAN}[NB]{RESET}",
        "SOURCE":   f"{CYAN}[SRC]{RESET}",
        "DOWNLOAD": f"{GREEN}[DL]{RESET}",
    }
    prefix = prefixes.get(level, f"{CYAN}[INFO]{RESET}")
    print(f"[{now()}] {prefix} {msg}", flush=True)


def load_config():
    """Load configuration from config.json."""
    if CONFIG_FILE.exists():
        with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    # Return defaults if no config
    return {
        "project_name": "TradeLab",
        "notebook_prefix": "TradeLab",
        "delay_between_lectures_sec": 60,
        "delay_between_accounts_sec": 30,
        "rotate_before_session": True,
        "rotate_min_interval_sec": 600,
        "quota_reset": {"timezone": "UTC", "hour": 0},
        "daily_limits": {"default": 3, "pro": 20},
        "pro_accounts": ["atulkpal@gmail.com"]
    }


def load_accounts():
    """Load accounts from accounts.json."""
    if ACCOUNTS_FILE.exists():
        with open(ACCOUNTS_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    # Default TradeLab accounts
    return [
        {"email": "atulkpal@gmail.com", "profile": "default", "tier": "pro"},
        {"email": "ashwathai.dev@gmail.com", "profile": "ashwathai", "tier": "default"},
        {"email": "boss.studio.care@gmail.com", "profile": "boss_studio", "tier": "default"},
        {"email": "hi.jumpdroid@gmail.com", "profile": "hi_jumpdroid", "tier": "default"},
        {"email": "iiidem.km@gmail.com", "profile": "iiidem_km", "tier": "default"},
        {"email": "promptwala.xyz@gmail.com", "profile": "promptwala", "tier": "default"},
        {"email": "paulritu120@gmail.com", "profile": "paulritu", "tier": "default"},
    ]


def _read_csv_rows(path):
    """Read a state CSV into a dict keyed by lecture_code.

    Migration-safe: rows from older schemas get defaults for columns added
    later (e.g. download_status) so first save writes the new header."""
    state = {}
    with open(path, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            if row.get('lecture_code'):
                row.setdefault('download_status', '')
                state[row['lecture_code']] = row
    return state


def load_state():
    """Load pipeline state from CSV. Returns dict keyed by lecture_code.

    If a recovery snapshot is newer than the main CSV (main was locked last
    run and we fell back), load from the recovery file instead — nothing lost.
    """
    source = STATE_FILE
    try:
        if RECOVER_FILE.exists() and not STATE_FILE.exists():
            log("!! Main CSV missing — loading recovery file instead", "WAIT")
            source = RECOVER_FILE
        elif RECOVER_FILE.exists() and STATE_FILE.exists():
            if RECOVER_FILE.stat().st_mtime > STATE_FILE.stat().st_mtime:
                log(f"!! {RECOVER_FILE.name} is newer than main CSV "
                    f"(last run hit an unkillable lock) — loading it", "WAIT")
                source = RECOVER_FILE
    except OSError:
        pass

    if source.exists():
        try:
            return _read_csv_rows(source)
        except (PermissionError, OSError) as e:
            log(f"!! Cannot read {source.name} ({e}) — trying recovery file", "WAIT")
            if source != RECOVER_FILE and RECOVER_FILE.exists():
                try:
                    rows = _read_csv_rows(RECOVER_FILE)
                    log(f"+ Loaded {RECOVER_FILE.name} instead", "OK")
                    return rows
                except (PermissionError, OSError):
                    pass
            log("!! All state files unreadable — starting with empty state", "FAIL")
    return {}


# ──────────────────────────────────────────────
# CSV LOCK ENFORCEMENT (kill whatever holds the sheet)
# ──────────────────────────────────────────────

def _write_state_to(path, state):
    """Write full state to a specific CSV path atomically-ready."""
    tmp = path.with_name(path.name + ".tmp") if path == STATE_FILE else path
    with open(tmp, 'w', encoding='utf-8', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=CSV_FIELDS)
        writer.writeheader()
        for row in state.values():
            writer.writerow(row)
    if tmp != path:
        os.replace(tmp, path)


def _find_lockers(target_path):
    """Return [(pid, app_name)] of processes holding target_path open.

    Uses the Windows Restart Manager API (rstrtmgr.dll) — the same mechanism
    MSI installers use to detect file locks. No admin rights required.
    """
    if not IS_WINDOWS:
        return []

    ERROR_MORE_DATA = 234
    CCH_RM_SESSION_KEY = 32

    class RM_PROCESS_INFO(ctypes.Structure):
        _fields_ = [
            ("dwProcessId", wintypes.DWORD),
            ("ProcessStartTime", wintypes.FILETIME),
            ("strAppName", ctypes.c_wchar * 256),
            ("strServiceShortName", ctypes.c_wchar * 64),
            ("ApplicationType", wintypes.DWORD),
            ("AppStatus", wintypes.ULONG),
            ("TSSessionId", wintypes.DWORD),
            ("bRestartable", wintypes.BOOL),
        ]

    rm = ctypes.WinDLL("rstrtmgr")
    session = wintypes.DWORD()
    session_key = ctypes.create_unicode_buffer(CCH_RM_SESSION_KEY + 1)

    if rm.RmStartSession(ctypes.byref(session), 0, session_key) != 0:
        return []

    lockers = []
    try:
        path_arr = (ctypes.c_wchar_p * 1)(str(target_path))
        if rm.RmRegisterResources(session, 1, path_arr, 0, None, 0, None) != 0:
            return []

        needed = wintypes.UINT(0)
        count = wintypes.UINT(0)
        reasons = wintypes.DWORD(0)

        # Probe call: returns ERROR_MORE_DATA + fills `needed` with array size
        rm.RmGetList(session, ctypes.byref(needed), ctypes.byref(count), None, ctypes.byref(reasons))
        if needed.value == 0:
            return []  # nobody holds it

        proc_array = (RM_PROCESS_INFO * needed.value)()
        count = wintypes.UINT(needed.value)
        info_ptr = ctypes.POINTER(RM_PROCESS_INFO)(proc_array)
        if rm.RmGetList(session, ctypes.byref(needed), ctypes.byref(count), info_ptr, ctypes.byref(reasons)) != 0:
            return []

        for i in range(count.value):
            pid = int(proc_array[i].dwProcessId)
            name = str(proc_array[i].strAppName or "").strip() or f"PID {pid}"
            lockers.append((pid, name))
    except Exception:
        pass
    finally:
        rm.RmEndSession(session)
    return lockers


def _pid_image_name(pid):
    """Best-effort exe name for a PID via tasklist (e.g. EXCEL.EXE)."""
    try:
        out = subprocess.run(
            ["tasklist", "/FI", f"PID eq {pid}", "/FO", "CSV", "/NH"],
            capture_output=True, text=True, timeout=10,
        ).stdout.strip()
        first = out.splitlines()[0] if out else ""
        if '"' in first:
            return first.split('"')[1]
    except Exception:
        pass
    return None


def _kill_lockers(target_path):
    """Force-terminate every process locking target_path. Announces each kill."""
    lockers = _find_lockers(target_path)
    killed_any = False
    for pid, app_name in lockers:
        exe = _pid_image_name(pid) or app_name
        try:
            result = subprocess.run(
                ["taskkill", "/F", "/PID", str(pid)],
                capture_output=True, text=True, timeout=15,
            )
            if result.returncode == 0:
                killed_any = True
                log(f"══ KILLED SHEET LOCKER ══ {exe} (PID {pid}) had {target_path.name} locked — terminated", "FAIL")
            else:
                err = (result.stderr or result.stdout or "").strip()[:80]
                log(f"  Could not kill {exe} (PID {pid}): {err}", "WAIT")
        except Exception as e:
            log(f"  Kill attempt failed for {exe} (PID {pid}): {e}", "WAIT")
    return killed_any


def save_state(state):
    """Save pipeline state to CSV.

    Atomic write; if the file is locked (open in Excel etc.), force-kills the
    locking process and retries — announcing every kill. If it still can't
    write (e.g. elevated locker), buffers to pipeline_state.recover.csv
    instead of crashing. Never raises.
    """
    global _last_lock_notice

    # Safety guard: never overwrite a populated CSV with an EMPTY state
    # (e.g. load_state failed on a lock and returned {}). This pipeline's
    # row set is fixed (204); an empty write here is always an accident.
    try:
        if not state and STATE_FILE.exists() and STATE_FILE.stat().st_size > 200:
            log("!! REFUSING to overwrite populated CSV with empty state "
                "(load likely failed) — nothing written", "FAIL")
            return False
    except OSError:
        pass

    def _finish_ok():
        try:
            if RECOVER_FILE.exists():
                RECOVER_FILE.unlink()
        except OSError:
            pass
        return True

    try:
        _write_state_to(STATE_FILE, state)
        return _finish_ok()
    except PermissionError:
        pass
    except OSError as e:
        log(f"!! State write error ({e}) — buffering to recovery file", "WAIT")

    # Locked → hunt down and kill the holder(s)
    if IS_WINDOWS:
        _kill_lockers(STATE_FILE)

    # Retry with short backoff (handle release can lag the kill slightly)
    for _ in range(3):
        time.sleep(0.4)
        try:
            _write_state_to(STATE_FILE, state)
            return _finish_ok()
        except (PermissionError, OSError):
            continue

    # Still locked (unkillable/elevated) → recovery buffer, never crash
    now_ts = time.time()
    if now_ts - _last_lock_notice > 60:
        log(f"!! {STATE_FILE.name} STILL locked after kill attempt — "
            f"buffering progress to {RECOVER_FILE.name}. Close it manually!", "WAIT")
        _last_lock_notice = now_ts
    try:
        _write_state_to(RECOVER_FILE, state)
    except OSError as e:
        log(f"!! Failed writing even the recovery file: {e}", "FAIL")
    return False


def init_csv_from_json():
    """Initialize pipeline_state.csv from config.json's lecture_data_file.
    
    This runs automatically the first time the pipeline starts for any project.
    If no config.json exists, falls back to TradeLab defaults.
    User is prompted for confirmation on first run with a new config.
    """
    # Determine if this is a first run (no CSV or config is new)
    csv_exists = STATE_FILE.exists()
    config_exists = CONFIG_FILE.exists()
    
    # If CSV already has 204 lectures, nothing to do
    state = load_state()
    if csv_exists:
        try:
            with open(STATE_FILE, 'r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                existing_count = sum(1 for _ in reader)
        except (PermissionError, OSError) as e:
            # Locked at startup — don't regenerate (that would wipe live state);
            # load_state already recovered what it could.
            log(f"!! Cannot count rows in {STATE_FILE.name} ({e}) — skipping init check", "WAIT")
            return True
        if existing_count >= 204:
            log("+ pipeline_state.csv already has 204 lectures, skipping init", "OK")
            return True
    
    # No config.json → use TradeLab defaults (backward compatible)
    if not config_exists:
        log("ℹ No config.json found — using TradeLab defaults", "INFO")
        # Generate CSV from TradeLab's academy_data_v2.json
        data_file = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "academy_data_v2.json"
        _init_csv_from_data(data_file, is_tradelab_default=True)
        return True
    
    # Config exists → read project settings
    with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
            config_data = json.load(f)
            lecture_data_file = config_data.get('lecture_data_file', '')
    
    # Build the data file path
    if lecture_data_file:
        data_path = Path(lecture_data_file)
        if not data_path.is_absolute():
            data_path = SCRIPT_DIR.parent / data_path
    else:
        data_path = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "academy_data_v2.json"
    
    # Check if data file exists
    if not data_path.exists():
        log(f"!! Lecture data file not found: {data_path}", "WAIT")
        # Fall back to TradeLab defaults
        data_path = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "academy_data_v2.json"
        log(f">> Falling back to TradeLab defaults", "INFO")
        _init_csv_from_data(data_path, is_tradelab_default=True)
        return True
    
    # Ask for confirmation on first run with a new config
    if not csv_exists:
        project_name = config_data.get('project_name', 'unknown')
        data_basename = data_path.name
        prompt = (
            f"!! FIRST RUN DETECTED\n"
            f"config.json found: yes\n"
            f"project_name: {project_name}\n"
            f"lecture_data_file: {data_basename}\n"
            f"This will initialize pipeline_state.csv with {_count_lectures(data_path)} lectures from this data.\n"
            f"Continue? (Y/n): "
        )
        choice = input(prompt).strip().lower()
        if choice and choice[0] != 'y':
            log("ℹ Using TradeLab defaults (cancelled by user)", "INFO")
            _init_csv_from_data(SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "academy_data_v2.json", is_tradelab_default=True)
            return True
    
    # Initialize CSV from the project's data file
    log(f"[CC] Initializing pipeline_state.csv from {data_path}...", "PROGRESS")
    _init_csv_from_data(data_path, is_tradelab_default=False)
    return True


def _count_lectures(data_path):
    """Count lectures in a JSON file."""
    try:
        with open(data_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        return sum(len(ch['lectures']) for c in data['courses'] for ch in c['chapters'])
    except:
        return 204  # fallback


def _init_csv_from_data(data_path, is_tradelab_default=False):
    """Core CSV initialization from lecture JSON data."""
    with open(data_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Load allocation mapping (lecture code -> email)
    alloc_file = SCRIPT_DIR / "pending_allocation.json"
    code_to_email = {}
    if alloc_file.exists():
        with open(alloc_file, 'r', encoding='utf-8') as f:
            allocation = json.load(f)
        for email, codes in allocation.items():
            for code in codes:
                code_to_email[code] = email
    
    # Build lectures dict: code -> {code, title, course_id}
    lectures = {}
    for course in data['courses']:
        for ch in course['chapters']:
            for l in ch['lectures']:
                title = l['title']
                code = title.split(':')[0].replace('Lecture ', '').strip()
                lectures[code] = {
                    'code': code,
                    'title': title,
                    'course_id': course['id']
                }
    
    needed = len(lectures)
    rows = []
    for code in sorted(lectures.keys(), key=lambda x: int(x.split('.')[0])):
        row = {
            'lecture_code': code,
            'title': lectures[code]['title'],
            'course_id': lectures[code]['course_id'],
            'assigned_email': code_to_email.get(code, ''),
            'notebook_id': '',
            'source_status': 'missing',
            'video_status': 'missing',
            'download_status': '',
            'artifact_url': '',
            'last_checked': ''
        }
        rows.append(row)
    
    with open(STATE_FILE, 'w', encoding='utf-8', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=CSV_FIELDS)
        writer.writeheader()
        for row in rows:
            writer.writerow(row)
    
    label = "TradeLab defaults" if is_tradelab_default else "project data"
    log(f"+ Generated {needed} lecture entries from {label}", "OK")
    
    log(f"+ Generated {needed} lecture entries in pipeline_state.csv", "OK")
    return True


def get_course_id(lecture_code):
    """Extract course ID from lecture code (e.g., '1.1.1' → '1')."""
    return lecture_code.split('.')[0]


# ──────────────────────────────────────────────
# COOKIE ROTATION (prevents PSIDTS expiry)
# ──────────────────────────────────────────────

def force_rotate_cookies(account):
    """Force a RotateCookies POST to refresh PSIDTS tokens before each session.

    PSIDTS tokens expire silently within minutes-to-hours. The library's
    keepalive mechanism only rotates while the client is OPEN, but our
    pipeline clients live ~10-30 seconds (too short for the 600s keepalive
    interval). This function fires the rotation POST BEFORE opening the client,
    so from_storage() loads fresh PSIDTS from disk.

    Returns True if rotation succeeded or was rate-limited (safe to proceed).
    Returns False only on actual failure.
    """
    import httpx as _httpx
    storage_path = Path.home() / ".notebooklm" / "profiles" / account['profile'] / "storage_state.json"

    if not storage_path.exists():
        log(f"  Storage file not found: {storage_path}", "FAIL")
        return False

    try:
        from notebooklm._auth.cookies import build_httpx_cookies_from_storage
        from notebooklm._auth.storage import save_cookies_to_storage
        from notebooklm._auth.keepalive import _rotate_post_sync

        jar = build_httpx_cookies_from_storage(storage_path)

        with _httpx.Client(cookies=jar, follow_redirects=True, timeout=15.0) as sync_client:
            from notebooklm._auth.storage import snapshot_cookie_jar
            snapshot = snapshot_cookie_jar(sync_client.cookies)
            _rotate_post_sync(sync_client)
            save_cookies_to_storage(sync_client.cookies, path=storage_path, original_snapshot=snapshot)

        log(f"  PSIDTS rotated for {account['email']}", "OK")
        return True

    except Exception as e:
        log(f"  PSIDTS rotation failed for {account['email']}: {str(e)[:80]}", "FAIL")
        return False


def rotate_all_accounts(accounts, state):
    """Pre-flight: rotate PSIDTS for accounts whose storage is stale enough
    to need it. Throttled by config.rotate_min_interval_sec (default 600s —
    Google's own declared rotation cadence) so routine mode runs stop firing
    7 rotations apiece. Disable entirely via config.rotate_before_session."""
    blocked = set(get_blocked_emails())

    if not config.get('rotate_before_session', True):
        log("  Pre-flight rotation disabled (config.rotate_before_session=false)", "INFO")
        return True

    min_interval = int(config.get('rotate_min_interval_sec', 600))
    ok = fail = skip_blocked = skip_fresh = 0
    log(f"Pre-flight: rotating stale PSIDTS (throttle {min_interval}s)...", "PROGRESS")

    for i, acc in enumerate(accounts):
        if acc['email'] in blocked:
            skip_blocked += 1
            continue
        sp = Path.home() / ".notebooklm" / "profiles" / acc['profile'] / "storage_state.json"
        if sp.exists() and (time.time() - sp.stat().st_mtime) < min_interval:
            skip_fresh += 1
            continue
        if force_rotate_cookies(acc):
            ok += 1
        else:
            fail += 1
        # Small delay only between actual rotations
        if i < len(accounts) - 1:
            time.sleep(1)

    msg = f"  Rotation: {ok} rotated, {skip_fresh} fresh (skipped), {skip_blocked} blocked (skipped)"
    if fail:
        msg += f", {fail} FAILED"
    log(msg, "OK" if fail == 0 else "WAIT")
    return fail == 0


# ──────────────────────────────────────────────
# NOTESHEET OPERATIONS
# ──────────────────────────────────────────────

async def get_existing_notebooks(client):
    """Get all TradeLab notebooks from an account."""
    try:
        notebooks = await client.notebooks.list()
        tradelab = [nb for nb in notebooks if 'TradeLab' in getattr(nb, 'title', '')]
        return tradelab
    except Exception as e:
        log(f"Error listing notebooks: {e}", "FAIL")
        return []


async def get_notebook_artifacts(client, notebook_id):
    """Get video artifacts for a notebook."""
    try:
        artifacts = await client.artifacts.list(notebook_id)
        videos = [a for a in artifacts if a.kind.name == "VIDEO"]
        return videos
    except Exception as e:
        log(f"Error listing artifacts: {e}", "FAIL")
        return []


async def create_or_get_notebook(client, lecture_code, title):
    """Create notebook if not exists, or return existing one."""
    target_title = f"{config['notebook_prefix']} - {title}"
    existing = await get_existing_notebooks(client)
    
    matching = [nb for nb in existing if getattr(nb, 'title', '') == target_title]
    
    if matching:
        # Keep first, delete duplicates — BUT never let dedupe destroy finished
        # work: prefer keeping a duplicate that already holds a completed video.
        keep = matching[0]
        if len(matching) > 1:
            for nb in matching:
                try:
                    arts = await client.artifacts.list(nb.id)
                    if any(a.kind.name == "VIDEO" and a.is_completed for a in arts):
                        keep = nb
                        break
                except Exception:
                    continue
        duplicates = [nb for nb in matching if nb.id != keep.id]
        log(f"  Notebook exists: {title} ({keep.id})", "OK")
        for dup in duplicates:
            try:
                await client.notebooks.delete(dup.id)
                log(f"  Deleted duplicate: {dup.id}", "OK")
            except Exception as e:
                log(f"  Failed to delete duplicate: {e}", "FAIL")
        return keep
    
    # Create new
    log(f"  Creating notebook: {title}", "NOTEBOOK")
    nb = await client.notebooks.create(target_title)
    log(f"  Created: {nb.id}", "OK")
    return nb


async def add_source(client, notebook, md_path):
    """Add source document to notebook."""
    try:
        source = await client.sources.add_file(notebook.id, md_path, wait=True)
        log(f"  Source added", "SOURCE")
        return True
    except Exception as e:
        log(f"  Source error: {e}", "FAIL")
        return False


async def trigger_video(client, notebook_id):
    """Trigger video generation (non-blocking)."""
    from notebooklm.exceptions import RateLimitError
    try:
        from notebooklm import VideoFormat, VideoStyle

        VIDEO_FORMAT_MAP = {
            'explainer': VideoFormat.EXPLAINER,
            'brief':     VideoFormat.BRIEF,
            'cinematic': VideoFormat.CINEMATIC,
            'short':     VideoFormat.SHORT,
        }

        VIDEO_STYLE_MAP = {
            'auto_select': VideoStyle.AUTO_SELECT,
            'custom':      VideoStyle.CUSTOM,
            'classic':     VideoStyle.CLASSIC,
            'whiteboard':  VideoStyle.WHITEBOARD,
            'kawaii':      VideoStyle.KAWAII,
            'anime':       VideoStyle.ANIME,
            'watercolor':  VideoStyle.WATERCOLOR,
            'retro_print': VideoStyle.RETRO_PRINT,
            'heritage':    VideoStyle.HERITAGE,
            'paper_craft': VideoStyle.PAPER_CRAFT,
        }

        fmt = VIDEO_FORMAT_MAP.get(config.get('video_format', 'short'), VideoFormat.SHORT)
        style = VIDEO_STYLE_MAP.get(config.get('video_style', 'auto_select'), VideoStyle.AUTO_SELECT)

        status = await client.artifacts.generate_video(
            notebook_id,
            instructions="Create an engaging educational video about this lecture",
            video_format=fmt,
            video_style=style,
            language=config.get('language', 'en'),
        )
        log(f"  Video generation triggered (task: {getattr(status, 'task_id', 'unknown')})", "VIDEO")
        return True
    except RateLimitError:
        # Propagate so mode_generate can mark the account rate-limited
        raise
    except Exception as e:
        log(f"  Video trigger error: {e}", "FAIL")
        return False


# ──────────────────────────────────────────────
# ACCOUNT QUOTA TRACKING
# ──────────────────────────────────────────────
class QuotaTracker:
    """Persistent daily video-generation quota tracker.

    State lives in quota_state.json so restarts never re-attack exhausted
    accounts. Pre-stops at the configured daily cap (default 3 standard /
    20 pro) BEFORE provoking the server's 4th refusal, and parks accounts
    that hit a real USER_DISPLAYABLE_ERROR until the configured reset.
    """

    def __init__(self, config):
        self.config = config
        self.limits = config.get('daily_limits', {'default': 3, 'pro': 20})
        self.tz, self.reset_hour = self._load_reset_cfg()
        self.state = self._load_state()
        self._rollover_if_new_day()

    # ── reset-clock config ──
    def _load_reset_cfg(self):
        qr = self.config.get('quota_reset', {}) or {}
        tz_name = qr.get('timezone', 'UTC')
        hour = int(qr.get('hour', 0))
        try:
            from zoneinfo import ZoneInfo
            return ZoneInfo(tz_name), hour
        except Exception:
            return timezone.utc, hour

    def _today_key(self):
        return datetime.now(self.tz).strftime('%Y-%m-%d')

    def next_reset_utc(self):
        """Aware UTC datetime of the next daily reset."""
        now_tz = datetime.now(self.tz)
        candidate = now_tz.replace(hour=self.reset_hour, minute=0, second=0, microsecond=0)
        if candidate <= now_tz:
            candidate += timedelta(days=1)
        return candidate.astimezone(timezone.utc)

    # ── persistence ──
    @staticmethod
    def _load_state():
        if QUOTA_STATE_FILE.exists():
            try:
                with open(QUOTA_STATE_FILE, 'r', encoding='utf-8') as f:
                    st = json.load(f)
                st.setdefault('used', {})
                st.setdefault('exhausted_until', {})
                return st
            except Exception as e:
                log(f"!! quota_state.json unreadable ({e}) — starting fresh", "WAIT")
        return {'reset_key': None, 'used': {}, 'exhausted_until': {}}

    def _save_state(self):
        try:
            with open(QUOTA_STATE_FILE, 'w', encoding='utf-8') as f:
                json.dump(self.state, f, indent=2, ensure_ascii=False)
        except Exception as e:
            log(f"!! Failed saving quota_state.json: {e}", "WAIT")

    def _rollover_if_new_day(self):
        key = self._today_key()
        if self.state.get('reset_key') != key:
            self.state = {'reset_key': key, 'used': {}, 'exhausted_until': {}}
            self._save_state()

    # ── queries / mutations ──
    def limit_for(self, account):
        tier = account.get('tier', 'default')
        return self.limits.get(tier, self.limits.get('default', 3))

    def used(self, email):
        return int(self.state['used'].get(email, 0))

    def exhausted_until_local(self, email):
        """Aware-local datetime the account unlocks, or None."""
        eu = self.state['exhausted_until'].get(email)
        if not eu:
            return None
        try:
            dt = datetime.fromisoformat(eu)
            return dt.astimezone(self.tz)
        except Exception:
            return None

    def can_generate_detail(self, account):
        """(ok, detail) — detail explains remaining quota or unlock ETA."""
        self._rollover_if_new_day()
        email = account['email']

        eu = self.state['exhausted_until'].get(email)
        if eu:
            try:
                dt = datetime.fromisoformat(eu)
                if datetime.now(timezone.utc) < dt:
                    local = dt.astimezone(self.tz).strftime('%Y-%m-%d %H:%M')
                    return False, f"exhausted until {local}"
                del self.state['exhausted_until'][email]
                self._save_state()
            except Exception:
                self.state['exhausted_until'].pop(email, None)

        limit = self.limit_for(account)
        used = self.used(email)
        if used >= limit:
            return False, f"daily cap reached ({used}/{limit})"
        return True, f"{used}/{limit}"

    def record_generation(self, account):
        self.state['used'][account['email']] = self.used(account['email']) + 1
        self._save_state()

    def mark_exhausted(self, email):
        """Park account until next reset. If the expected reset already
        passed and the server STILL refuses, slide the window 1h (calibration)."""
        nr = self.next_reset_utc()
        if nr <= datetime.now(timezone.utc) + timedelta(seconds=90):
            nr = datetime.now(timezone.utc) + timedelta(hours=1)
            log("  !! Server refusing past expected reset — parking 1h (recalibrating)", "WAIT")
        self.state['exhausted_until'][email] = nr.isoformat()
        self._save_state()

    def any_active(self, accounts):
        """True if at least one account may still generate right now."""
        return any(self.can_generate_detail(a)[0] for a in accounts)

    async def wait_for_reset(self):
        """Sleep until the daily reset with a live HH:MM:SS countdown."""
        nr = self.next_reset_utc()
        eta = (nr - datetime.now(timezone.utc)).total_seconds()
        local_target = nr.astimezone(self.tz).strftime('%Y-%m-%d %H:%M')
        log(f"  All accounts exhausted — auto-resuming at reset {local_target} "
            f"(in {fmt_eta(eta)}). Ctrl+C stops; progress saved.", "WAIT")
        secs = int(eta) + 5  # small buffer past the boundary
        while secs > 0:
            h, rem = divmod(secs, 3600)
            m, s = divmod(rem, 60)
            print(f"\r  {CYAN}[WAIT]{RESET} Reset countdown: {h:02d}:{m:02d}:{s:02d}   ",
                  end="", flush=True)
            await asyncio.sleep(1)
            secs -= 1
        print()
        self.state['exhausted_until'] = {}
        self._save_state()
        log("  Reset reached — quota windows refreshed, resuming", "OK")


def fmt_eta(seconds):
    s = int(max(0, seconds))
    d, s = divmod(s, 86400)
    h, s = divmod(s, 3600)
    m, _ = divmod(s, 60)
    parts = []
    if d:
        parts.append(f"{d}d")
    if h:
        parts.append(f"{h}h")
    if m or not parts:
        parts.append(f"{m}m")
    return " ".join(parts)


def classify_rate_limit(e):
    """'transient' (HTTP 429 w/ Retry-After) vs 'daily' (USER_DISPLAYABLE_ERROR)."""
    ra = getattr(e, 'retry_after', None)
    if ra:
        return 'transient'
    code = str(getattr(e, 'rpc_code', '') or '')
    if 'USER_DISPLAYABLE' in code.upper():
        return 'daily'
    if 'rate limit or quota' in str(e).lower():
        return 'daily'
    return 'unknown'


# ──────────────────────────────────────────────
# ACCOUNT BLOCKING (auth_state.json is the single health store)
# ──────────────────────────────────────────────
AUTH_STATE_FILE = SCRIPT_DIR / "auth_state.json"


def load_auth_state():
    """Load auth_state.json; tolerant of missing/corrupt file."""
    if AUTH_STATE_FILE.exists():
        try:
            with open(AUTH_STATE_FILE, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            log(f"!! auth_state.json unreadable ({e}) — treating as empty", "WAIT")
    return {"accounts": {}}


def save_auth_state(auth_state):
    """Persist auth_state.json (pretty, UTF-8)."""
    with open(AUTH_STATE_FILE, 'w', encoding='utf-8') as f:
        json.dump(auth_state, f, indent=2, ensure_ascii=False)


def get_blocked_emails():
    """Emails currently marked blocked in auth_state.json."""
    st = load_auth_state()
    return [e for e, a in st.get('accounts', {}).items() if a.get('blocked')]


def mark_account_blocked(email, error):
    """Persist a mid-run auth death: account stops processing until re-auth."""
    st = load_auth_state()
    acc = st.setdefault('accounts', {}).setdefault(email, {})
    acc.setdefault('email', email)
    acc['auth_status'] = 'expired'
    acc['blocked'] = True
    acc['last_auth_fail'] = now()
    acc['last_error'] = str(error)[:200]
    save_auth_state(st)
    log(f"⛔ {email} marked BLOCKED until re-auth (run option 7)", "FAIL")


def unmark_account_blocked(email):
    """Clear the block after successful re-auth."""
    st = load_auth_state()
    acc = st.get('accounts', {}).get(email)
    if acc is not None:
        acc['blocked'] = False
        save_auth_state(st)


def is_auth_error(e):
    """Detect library auth-expiry errors regardless of wording drift."""
    s = str(e).lower()
    return ('authentication expired' in s
            or 're-authenticate' in s
            or 'authentication required' in s)


# ──────────────────────────────────────────────
# REASSIGNMENT ENGINE (ownership transfer + orphan cleanup queue)
# ──────────────────────────────────────────────
CLEANUP_FILE = SCRIPT_DIR / "cleanup_queue.json"


def load_cleanup_queue():
    if CLEANUP_FILE.exists():
        try:
            with open(CLEANUP_FILE, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            log(f"!! cleanup_queue.json unreadable ({e}) — starting empty", "WAIT")
    return []


def save_cleanup_queue(queue):
    with open(CLEANUP_FILE, 'w', encoding='utf-8') as f:
        json.dump(queue, f, indent=2, ensure_ascii=False)


def ask(prompt, default_yes=True):
    """CLI yes/no prompt. EOF (piped input) resolves to the default."""
    suffix = " (Y/n): " if default_yes else " (y/N): "
    try:
        ans = input(prompt + suffix).strip().lower()
    except EOFError:
        return False
    if not ans:
        return default_yes
    return ans[0] == 'y'


def _pending_codes(state, email=None):
    """Lecture codes still needing generation (missing/failed), optionally
    filtered to one owner. Unallocated rows (no owner) are always excluded."""
    out = []
    for c, r in state.items():
        owner = r.get('assigned_email', '')
        if not owner:
            continue
        if email is not None and owner != email:
            continue
        if r.get('video_status') not in ('complete', 'generating'):
            out.append(c)
    return sorted(out)


def find_stranded(state, blocked_set):
    """Pending lectures owned by BLOCKED accounts."""
    return [c for c in _pending_codes(state) if state[c]['assigned_email'] in blocked_set]


def _limit_of(quota_tracker, account):
    try:
        return quota_tracker.limit_for(account)
    except Exception:
        return int((quota_tracker.config.get('daily_limits') or {}).get(account.get('tier', 'default'), 3))


def plan_reassignment(state, accounts, stranded_codes, quota_tracker, blocked_set):
    """Weighted round-robin distribution of stranded codes across ALIVE
    accounts, proportional to daily limits. Returns {target: [codes]}."""
    alive = [a for a in accounts if a['email'] not in blocked_set]
    if not alive or not stranded_codes:
        return {}
    pool = []
    for a in alive:
        pool += [a['email']] * max(1, _limit_of(quota_tracker, a))
    plan = {}
    for i, code in enumerate(sorted(stranded_codes)):
        plan.setdefault(pool[i % len(pool)], []).append(code)
    return plan


def plan_rebalance(state, accounts, quota_tracker, blocked_set, min_improvement=0.1):
    """Equalize finish times across ALIVE accounts by moving pending lectures
    from over-loaded to under-loaded owners.

    Returns (plan, meta):
      plan = {receiver_email: [codes]} or None when no worthwhile moves exist
      meta = (current_days, balanced_days, total_pending, total_capacity)
    Donor loss is spread round-robin so no single donor keeps a laggard tail.
    """
    import math
    alive = [a for a in accounts if a['email'] not in blocked_set]
    if not alive:
        return None, None

    pend = {a['email']: _pending_codes(state, a['email']) for a in alive}
    total = sum(len(v) for v in pend.values())
    capacity = sum(max(1, _limit_of(quota_tracker, a)) for a in alive)
    if total == 0 or capacity == 0:
        return None, None

    cur_days = max(len(pend[a['email']]) / max(1, _limit_of(quota_tracker, a)) for a in alive)
    bal_days = math.ceil(total / capacity)
    if bal_days >= cur_days * (1 - min_improvement):
        return None, (cur_days, bal_days, total, capacity)

    # Target pending per owner, proportional to daily limit (rounded water-fill;
    # rounding drift absorbed by the largest target so sums stay exact)
    raw = {a['email']: max(1, _limit_of(quota_tracker, a)) * total / capacity for a in alive}
    targets = {em: int(round(v)) for em, v in raw.items()}
    drift = total - sum(targets.values())
    if drift:
        em_max = max(targets, key=targets.get)
        targets[em_max] += drift

    excess_by_owner, deficit_slots = [], []
    for a in alive:
        em = a['email']
        have = pend[em]
        t = targets[em]
        if len(have) > t:
            excess_by_owner.append((em, have[t:]))
        elif len(have) < t:
            deficit_slots.extend([em] * (t - len(have)))

    # Spread donor loss round-robin so no donor keeps a laggard tail
    excess = []
    idx = 0
    donors = [list(codes) for _, codes in excess_by_owner]
    while any(donors):
        d = donors[idx % len(donors)]
        if d:
            excess.append(d.pop(0))
        idx += 1

    plan = {}
    for em, code in zip(deficit_slots, excess):
        plan.setdefault(em, []).append(code)
    return (plan or None), (cur_days, bal_days, total, capacity)


def apply_reassignment(state, plan):
    """Transfer ownership permanently: CSV ledger updated, old notebooks
    enqueued for deletion (drained when the old owner is next alive).
    Only ever called with pending (missing/failed) lectures."""
    queue = load_cleanup_queue()
    moved = 0
    for target_email, codes in plan.items():
        for code in codes:
            row = state.get(code)
            if not row or row.get('video_status') in ('complete', 'generating'):
                continue  # structural safety: never move finished work
            old_owner = row.get('assigned_email', '')
            old_nb = row.get('notebook_id', '')
            row['assigned_email'] = target_email
            if old_nb:
                row['notebook_id'] = ''
                queue.append({
                    "account": old_owner,
                    "notebook_id": old_nb,
                    "lecture_code": code,
                    "queued": now(),
                    "reason": "reassigned",
                })
            row['source_status'] = 'missing'
            if row.get('video_status') == 'failed':
                row['video_status'] = 'missing'
            row['last_checked'] = now()
            moved += 1
    save_cleanup_queue(queue)
    save_state(state)
    return moved


async def drain_cleanup_queue(accounts, state, delay=0.4, client_factory=None):
    """Delete queued orphan notebooks on ALIVE accounts.

    Divergence guard: an entry is skipped (kept) if the CSV row for its
    lecture still references that notebook_id. Missing notebooks (404)
    count as cleaned. Returns number of notebooks deleted.
    client_factory: test seam — defaults to the real NotebookLMClient."""
    if client_factory is None:
        from notebooklm import NotebookLMClient as client_factory

    queue = load_cleanup_queue()
    if not queue:
        return 0

    blocked = set(get_blocked_emails())
    alive = {a['email']: a for a in accounts}
    by_account = {}
    for entry in queue:
        by_account.setdefault(entry['account'], []).append(entry)

    remaining, deleted = [], 0
    for acct_email, entries in by_account.items():
        acc = alive.get(acct_email)
        if not acc or acct_email in blocked:
            remaining.extend(entries)
            continue
        profile_path = str(Path.home() / ".notebooklm" / "profiles" / acc['profile'] / "storage_state.json")
        try:
            async with client_factory.from_storage(profile_path, keepalive=600) as client:
                for entry in entries:
                    row = state.get(entry['lecture_code'])
                    if row and row.get('notebook_id') == entry['notebook_id']:
                        remaining.append(entry)  # still owned — never delete
                        continue
                    try:
                        await client.notebooks.delete(entry['notebook_id'])
                        deleted += 1
                        log(f"  🧹 Deleted orphan notebook {entry['notebook_id']} "
                            f"({entry['lecture_code']}) from {acct_email}", "OK")
                    except Exception as ex:
                        msg = str(ex).lower()
                        if '404' in msg or 'not found' in msg or 'does not exist' in msg:
                            deleted += 1  # already gone — queue entry served its purpose
                            log(f"  🧹 Orphan {entry['lecture_code']} already gone on {acct_email}", "OK")
                        else:
                            entry['last_error'] = str(ex)[:120]
                            remaining.append(entry)
                    await asyncio.sleep(delay)
        except Exception as e:
            if is_auth_error(e):
                mark_account_blocked(acct_email, e)
            else:
                log(f"  Cleanup drain error on {acct_email}: {str(e)[:80]}", "FAIL")
            remaining.extend(entries)

    save_cleanup_queue(remaining)
    return deleted


# ──────────────────────────────────────────────
# MODE: CHECK
# ──────────────────────────────────────────────
async def mode_check(state):
    """Check all accounts, update state CSV."""
    log("Running CHECK mode...", "PROGRESS")
    
    accounts = load_accounts()
    rotate_all_accounts(accounts, state)
    blocked_set = set(get_blocked_emails())
    updated = 0
    total_duplicates = 0
    total_unallocated = 0
    
    # Per-account summary tracking
    account_summary = {}
    
    for account in accounts:
        email = account['email']
        profile_path = str(Path.home() / ".notebooklm" / "profiles" / account['profile'] / "storage_state.json")

        if email in blocked_set:
            log(f"⛔ Skipping {email} — BLOCKED (run option 7 to re-auth)", "WAIT")
            continue

        # Count allocated lectures for this account
        allocated = sum(1 for r in state.values() if r['assigned_email'] == email)
        
        acc_notebooks = 0
        acc_sources = 0
        acc_videos = 0
        acc_duplicates = 0
        
        try:
            from notebooklm import NotebookLMClient
            async with NotebookLMClient.from_storage(profile_path, keepalive=600) as client:
                notebooks = await get_existing_notebooks(client)
                
                # Sort notebooks by lecture code
                def extract_code(nb):
                    m = re.search(r'Lecture\s+([\d.]+)', getattr(nb, 'title', ''))
                    if m:
                        parts = m.group(1).split('.')
                        return tuple(int(p) for p in parts)
                    return (999,)
                notebooks.sort(key=extract_code)
                
                log(f"{email} — {len(notebooks)} notebooks found, {allocated} allocated", "OK")
                
                # Track seen lecture codes to skip duplicate notebooks
                seen_codes = set()
                
                # Check each notebook
                for nb in notebooks:
                    nb_id = getattr(nb, 'id', '')
                    nb_title = getattr(nb, 'title', '')
                    
                    # Find lecture code from title
                    code_match = re.search(r'Lecture\s+([\d.]+)', nb_title)
                    code = code_match.group(1) if code_match else None
                    
                    if not code or code not in state:
                        continue
                    
                    # Skip duplicate notebooks for same lecture (silent)
                    if code in seen_codes:
                        acc_duplicates += 1
                        total_duplicates += 1
                        continue
                    seen_codes.add(code)
                    
                    # Skip unallocated lectures
                    if not state[code]['assigned_email']:
                        total_unallocated += 1
                        continue

                    # Ownership guard: after reassignment, old owners still
                    # carry orphan notebooks for lectures they no longer own.
                    # Never relink those — the new owner's notebook is truth.
                    if state[code]['assigned_email'] != email:
                        continue
                    
                    row = state[code]
                    # Update notebook_id (use first found)
                    if not row['notebook_id']:
                        row['notebook_id'] = nb_id
                        updated += 1
                    row['last_checked'] = now()
                    acc_notebooks += 1
                    
                    # Check for videos
                    video_status = "missing"
                    if nb_id:
                        videos = await get_notebook_artifacts(client, nb_id)
                        if videos:
                            completed = [v for v in videos if v.is_completed]
                            if completed:
                                video_status = "complete"
                                for v in completed:
                                    if v.url:
                                        row['artifact_url'] = v.url
                            else:
                                for v in videos:
                                    if v.status_str == 'in_progress':
                                        video_status = "generating"
                                    elif v.status_str == 'failed':
                                        video_status = "failed"
                        row['video_status'] = video_status
                        if video_status == "complete":
                            acc_videos += 1
                    
                    # Check source status
                    source_status = "missing"
                    if nb_id:
                        try:
                            sources = await client.sources.list(nb_id)
                            ready = [s for s in sources if s.is_ready]
                            if ready:
                                source_status = "uploaded"
                            elif sources:
                                source_status = "processing"
                            row['source_status'] = source_status
                        except Exception as e:
                            log(f"  {code:8s}  Source check failed: {str(e)[:60]}", "FAIL")
                        if source_status == "uploaded":
                            acc_sources += 1
                    
                    # One-line progress per notebook
                    v_icon = {"complete": "V", "generating": "~", "failed": "X", "missing": "-"}.get(video_status, "?")
                    s_icon = {"uploaded": "S", "processing": "~", "missing": "-"}.get(source_status, "?")
                    log(f"  {code:8s}  [{v_icon}] video={video_status:10s} [{s_icon}] src={source_status}", "INFO")
                
                await asyncio.sleep(1)
                
        except Exception as e:
            if is_auth_error(e):
                mark_account_blocked(email, e)
            else:
                log(f"Error with {email}: {e}", "FAIL")
        
        account_summary[email] = {
            "allocated": allocated,
            "notebooks": acc_notebooks,
            "sources": acc_sources,
            "videos": acc_videos,
            "duplicates": acc_duplicates,
        }
        # Live save after each account so progress hits disk continuously
        save_state(state)
    
    # Save updated state
    save_state(state)
    
    # Print summary
    print(f"\n{CYAN}{'='*60}{RESET}")
    print(f"{CYAN}  CHECK SUMMARY{RESET}")
    print(f"{CYAN}{'='*60}{RESET}")
    
    total_alloc = 0
    total_nb = 0
    total_src = 0
    total_vid = 0
    for email, s in account_summary.items():
        print(f"{WHITE}  {email}{RESET}")
        print(f"    Allocated: {s['allocated']:3d}  Found: {s['notebooks']:3d}  Sources: {s['sources']:3d}  Videos: {s['videos']:3d}  Dupes: {s['duplicates']:3d}")
        total_alloc += s['allocated']
        total_nb += s['notebooks']
        total_src += s['sources']
        total_vid += s['videos']
    
    print(f"{CYAN}{'-'*60}{RESET}")
    print(f"{GREEN}  Total: {total_alloc} allocated | {total_nb} found | {total_src} sources | {total_vid} videos | {total_duplicates} dupes{RESET}")
    if total_unallocated > 0:
        print(f"{YELLOW}  Skipped {total_unallocated} unallocated lectures{RESET}")
    if updated > 0:
        print(f"{YELLOW}  Updated {updated} new notebook IDs in CSV{RESET}")
    print(f"{CYAN}{'='*60}{RESET}\n")
    
    log("CHECK complete.", "OK")
    return state


# ──────────────────────────────────────────────
# MODE: CREATE
# ──────────────────────────────────────────────
async def mode_create(state):
    """Create missing notebooks and upload sources."""
    log("> Running CREATE mode...", "PROGRESS")
    
    accounts = load_accounts()
    rotate_all_accounts(accounts, state)
    blocked_set = set(get_blocked_emails())
    created = 0
    
    for account in accounts:
        if account['email'] in blocked_set:
            log(f"⛔ Skipping {account['email']} — BLOCKED (run option 7 to re-auth)", "WAIT")
            continue
        
        profile_path = str(Path.home() / ".notebooklm" / "profiles" / account['profile'] / "storage_state.json")
        
        try:
            from notebooklm import NotebookLMClient
            async with NotebookLMClient.from_storage(profile_path, keepalive=600) as client:
                # Get lectures needing notebooks
                needing = [
                    code for code, row in state.items()
                    if row['assigned_email'] == account['email'] and (not row['notebook_id'] or row['source_status'] != 'uploaded')
                ]
                
                log(f"  [{account['email']}] Need to create {len(needing)} notebooks/sources", "PROGRESS")
                
                for code in needing:
                    row = state[code]
                    lecture_title = row['title']
                    course_id = row.get('course_id', get_course_id(code))
                    md_path = f"{SCRIPT_DIR}/lectures/course_{course_id}/lecture_{code.replace('.', '_')}.md"
                    
                    if not os.path.exists(md_path):
                        log(f"  !! MD not found: {md_path}", "WAIT")
                        continue
                    
                    # Create or get notebook
                    nb = await create_or_get_notebook(client, code, lecture_title)
                    row['notebook_id'] = nb.id
                    
                    # Add source
                    success = await add_source(client, nb, md_path)
                    if success:
                        row['source_status'] = 'uploaded'
                        created += 1
                        save_state(state)  # Live update after each source
                    else:
                        row['source_status'] = 'failed'
                    
                    row['last_checked'] = now()
                    await asyncio.sleep(1)  # Small delay
                
        except Exception as e:
            if is_auth_error(e):
                mark_account_blocked(account['email'], e)
            else:
                log(f"  Error with {account['email']}: {e}", "FAIL")
    
    save_state(state)
    log(f"+ CREATE complete. Created/updated {created} entries.", "OK")
    return state


# ──────────────────────────────────────────────
# MODE: GENERATE
# ──────────────────────────────────────────────
async def mode_generate(state, quota_tracker, auto_wait=False):
    """Trigger video generation with daily-cap pre-stop and error classification.

    transient (HTTP 429 + Retry-After)  → wait, retry same lecture once
    daily    (USER_DISPLAYABLE_ERROR)   → park account until quota reset
    Pre-stops at config.daily_limits so the server's 4th refusal never fires.
    """
    from notebooklm.exceptions import RateLimitError

    log("> Running GENERATE mode...", "PROGRESS")

    accounts = load_accounts()
    rotate_all_accounts(accounts, state)
    blocked_set = set(get_blocked_emails())
    delay_sec = config.get('delay_between_lectures_sec', 60)

    def quota_summary(pending_count):
        log("══ DAILY QUOTA REACHED ══", "WAIT")
        for a in accounts:
            ok, detail = quota_tracker.can_generate_detail(a)
            tag = "available" if ok else "EXHAUSTED"
            log(f"    {a['email']:32s} [{tag}] {detail}", "INFO")
        nr_utc = quota_tracker.next_reset_utc()
        eta = (nr_utc - datetime.now(timezone.utc)).total_seconds()
        local = nr_utc.astimezone(quota_tracker.tz).strftime('%Y-%m-%d %H:%M')
        log(f"  Next reset: {local} (in {fmt_eta(eta)})", "WAIT")
        log(f"  {pending_count} video(s) waiting for quota.", "WAIT")

    while True:
        # Filter videos that need generating
        to_generate = [
            code for code, row in state.items()
            if row['video_status'] not in ('complete', 'generating') and row['notebook_id']
        ]

        if not to_generate:
            log("  No videos need generating right now.", "OK")
            break

        # Group by account (skip blocked accounts)
        by_account = {}
        for code in to_generate:
            row = state[code]
            acct_email = row['assigned_email']
            if acct_email and acct_email not in blocked_set:
                by_account.setdefault(acct_email, []).append(code)

        if not by_account:
            if blocked_set and to_generate:
                log(f"⛔ {len(to_generate)} videos stranded — their accounts are BLOCKED "
                    f"({', '.join(sorted(e.split('@')[0] for e in blocked_set))}).", "WAIT")
                stranded = find_stranded(state, blocked_set)
                plan = plan_reassignment(state, accounts, stranded, quota_tracker, blocked_set)
                if plan:
                    for tgt, codes in plan.items():
                        log(f"   propose → {tgt}: {len(codes)} lecture(s)", "INFO")
                    if ask("Reassign stranded lectures to alive accounts", default_yes=True):
                        moved = apply_reassignment(state, plan)
                        log(f"+ Reassigned {moved} lecture(s) — old notebooks queued for cleanup", "OK")
                        if ask("Run CREATE now to build notebooks on the new owners", default_yes=True):
                            state = await mode_create(state)
                        continue  # re-filter: new owners now have (or will have) notebooks
            elif to_generate:
                if pending_downloads_exist(state):
                    log(f"  {len(to_generate)} videos quota-waiting, but downloads are "
                        f"pending — skipping reset sleep so they complete first", "WAIT")
                    break
                quota_summary(len(to_generate))
                if auto_wait:
                    await quota_tracker.wait_for_reset()
                    continue
                log("  Standalone GENERATE stops here — rerun after reset, or use RUN ALL to auto-resume.", "WAIT")
            else:
                log("  No assigned accounts found for remaining videos.", "WAIT")
            break

        log(f"  {len(to_generate)} videos remaining across {len(by_account)} accounts", "PROGRESS")

        any_triggered_this_cycle = False
        daily_exhausted = set()

        for email, codes in by_account.items():
            account = next((a for a in accounts if a['email'] == email), None)
            if not account:
                continue

            # Pre-stop: skip BEFORE provoking the server once cap is hit
            ok, qdetail = quota_tracker.can_generate_detail(account)
            if not ok:
                log(f"  [{email}] Quota: {qdetail} — skipping", "WAIT")
                continue

            profile_path = str(Path.home() / ".notebooklm" / "profiles" / account['profile'] / "storage_state.json")
            account_triggered = 0
            account_skipped = 0

            for code in codes:
                row = state[code]
                nb_id = row['notebook_id']

                if not nb_id:
                    continue

                success = False
                try:
                    from notebooklm import NotebookLMClient
                    async with NotebookLMClient.from_storage(profile_path, keepalive=600) as client:

                        # ── Check artifacts for status ──
                        videos = await get_notebook_artifacts(client, nb_id)

                        # Already complete?
                        completed = [v for v in videos if v.is_completed]
                        if completed:
                            row['video_status'] = 'complete'
                            for v in completed:
                                if v.url:
                                    row['artifact_url'] = v.url
                            save_state(state)  # Live update
                            log(f"  [{code}] Already complete — skipping", "OK")
                            account_skipped += 1
                            continue

                        # Already generating/pending?
                        active = [v for v in videos if v.is_processing or v.is_pending]
                        if active:
                            status_label = active[0].status_str
                            row['video_status'] = 'generating'
                            save_state(state)  # Live update
                            log(f"  [{code}] Already {status_label} — skipping", "OK")
                            account_skipped += 1
                            continue

                        # ── Trigger generation (with transient retry) ──
                        log(f"  [{code}] Triggering video generation", "VIDEO")
                        attempt = 0
                        while True:
                            try:
                                success = await trigger_video(client, nb_id)
                                break
                            except RateLimitError as e:
                                kind = classify_rate_limit(e)
                                if kind == 'transient' and attempt == 0:
                                    wait_s = min(int(getattr(e, 'retry_after', 0) or 30), 300)
                                    log(f"  [{code}] Transient rate limit — waiting {wait_s}s, retrying once", "WAIT")
                                    attempt += 1
                                    await asyncio.sleep(wait_s)
                                    continue
                                if kind == 'daily':
                                    quota_tracker.mark_exhausted(email)
                                    daily_exhausted.add(email)
                                    log(f"  [{code}] DAILY quota exhausted — {email} parked until reset", "FAIL")
                                    raise
                                log(f"  [{code}] Rate limited ({kind}): {str(e)[:70]}", "FAIL")
                                success = False
                                break

                        if success:
                            row['video_status'] = 'generating'
                            quota_tracker.record_generation(account)
                            account_triggered += 1
                            any_triggered_this_cycle = True
                            save_state(state)  # Live update
                            log(f"  [{code}] Video generation started", "VIDEO")
                        else:
                            log(f"  [{code}] Failed to trigger", "FAIL")

                except RateLimitError:
                    # Daily exhaustion already parked above — abandon this account
                    break

                except Exception as e:
                    if is_auth_error(e):
                        mark_account_blocked(email, e)
                        break
                    else:
                        log(f"  [{code}] Error: {str(e)[:80]}", "FAIL")
                    success = False

                # Delay between lectures (only after success)
                if success:
                    log(f"  Waiting {delay_sec}s before next generation...", "WAIT")
                    await asyncio.sleep(delay_sec)

            # Per-account summary
            if account_triggered > 0 or account_skipped > 0:
                log(f"  [{email}] Triggered: {account_triggered}  Skipped: {account_skipped}", "INFO")

        # ── End-of-cycle decision ──
        pending = [
            c for c, r in state.items()
            if r['video_status'] not in ('complete', 'generating') and r['notebook_id']
        ]
        if not pending:
            break

        active_exists = quota_tracker.any_active(
            [a for a in accounts if a['email'] not in blocked_set])

        if not active_exists:
            if pending_downloads_exist(state):
                log("  Downloads pending — skipping reset sleep so they complete first", "WAIT")
                break
            quota_summary(len(pending))
            if auto_wait:
                await quota_tracker.wait_for_reset()
                continue
            log("  Standalone GENERATE stops here — rerun after reset, or use RUN ALL to auto-resume.", "WAIT")
            break

        if not any_triggered_this_cycle:
            log("  Nothing triggered this cycle (all remaining skipped or failing).", "WAIT")
            break

    save_state(state)
    log("+ GENERATE complete.", "OK")
    return state


# ──────────────────────────────────────────────
# MODE: DOWNLOAD
# ──────────────────────────────────────────────
def pending_downloads_exist(state):
    """True if any completed video still needs download bookkeeping or the
    actual file. Rows with a cloud URL but no download_status count as
    pending even if a file exists — mode_download self-heals them, and the
    reset-sleep must never start before that pass has run."""
    output_dir = Path(config.get('output_dir', 'assets'))
    for row in state.values():
        if row.get('video_status') != 'complete' or not row.get('artifact_url'):
            continue
        if row.get('download_status') == 'downloaded':
            out_file = output_dir / f"lecture_{row['lecture_code'].replace('.', '_')}.mp4"
            if not out_file.exists():
                return True  # marked but vanished → needs re-download
            continue
        return True  # complete on cloud, not marked downloaded yet
    return False


async def mode_download(state):
    """Download completed videos.

    CSV download_status is the source of truth:
      'downloaded'        → skip (self-heals if the file vanished)
      '' / 'failed'       → (re)download; artifact_url keeps the CLOUD URL
    """
    log("> Running DOWNLOAD mode...", "PROGRESS")
    
    accounts = load_accounts()
    rotate_all_accounts(accounts, state)
    blocked_set = set(get_blocked_emails())
    
    output_dir = Path(config.get('output_dir', 'assets'))
    output_dir.mkdir(exist_ok=True)
    
    downloaded = 0
    current_email = None
    
    for code, row in state.items():
        if row['video_status'] != 'complete' or not row.get('artifact_url'):
            continue
        
        out_file = output_dir / f"lecture_{code.replace('.', '_')}.mp4"
        status = row.get('download_status', '')
        
        # CSV-driven skip (self-heal if the file vanished)
        if status == 'downloaded':
            if out_file.exists():
                downloaded += 1
                continue
            log(f"  [{code}] Marked downloaded but file missing — re-downloading", "WAIT")
            row['download_status'] = ''
        
        # Self-heal: file already on disk (manual copy / earlier run) → mark it
        if out_file.exists():
            row['download_status'] = 'downloaded'
            downloaded += 1
            log(f"  [{code}] File already on disk — marked downloaded", "OK")
            save_state(state)
            continue
        
        # Resolve account first
        acc = next((a for a in accounts if a['email'] == row['assigned_email']), None)
        if not acc:
            continue
        
        if acc['email'] in blocked_set:
            log(f"⛔ [{code}] Skipping — {acc['email']} BLOCKED (run option 7)", "WAIT")
            continue
        
        # Announce account changes so downloads are attributable
        if acc['email'] != current_email:
            log(f"{'─'*58}", "PROGRESS")
            log(f"ACCOUNT: {acc['email']}", "PROGRESS")
            current_email = acc['email']
        
        log(f"  ↓ [{code}] Downloading via {acc['email'].split('@')[0]}", "DOWNLOAD")
        
        profile_path = str(Path.home() / ".notebooklm" / "profiles" / acc['profile'] / "storage_state.json")
        
        try:
            from notebooklm import NotebookLMClient
            async with NotebookLMClient.from_storage(profile_path, keepalive=600) as client:
                artifacts = await client.artifacts.list(row['notebook_id'])
                if not any(a.kind.name == "VIDEO" for a in artifacts):
                    log(f"  [{code}] No video artifact present — skipping", "WAIT")
                    continue
                try:
                    await client.artifacts.download_video(row['notebook_id'], str(out_file))
                    size_mb = out_file.stat().st_size / (1024*1024) if out_file.exists() else 0
                    log(f"  ↓ DONE: {out_file.name} ({size_mb:.1f} MB)", "DOWNLOAD")
                    # artifact_url intentionally KEEPS the cloud URL (re-download source)
                    row['download_status'] = 'downloaded'
                    downloaded += 1
                    save_state(state)  # Live update after each download
                except Exception as e:
                    row['download_status'] = 'failed'
                    save_state(state)
                    log(f"  ↓ Download error: {e}", "FAIL")
        except Exception as e:
            if is_auth_error(e):
                mark_account_blocked(acc['email'], e)
            else:
                log(f"  - Error: {e}", "FAIL")
        
        await asyncio.sleep(1)
    
    save_state(state)
    log(f"+ DOWNLOAD complete. {downloaded} videos downloaded.", "OK")
    return state


# ──────────────────────────────────────────────
# MODE: STATUS
# ──────────────────────────────────────────────
def mode_status(state):
    """Show pipeline status summary."""
    total = len(state)
    complete = sum(1 for r in state.values() if r['video_status'] == 'complete')
    generating = sum(1 for r in state.values() if r['video_status'] == 'generating')
    missing = sum(1 for r in state.values() if r['video_status'] == 'missing')
    failed = sum(1 for r in state.values() if r['video_status'] == 'failed')
    uploaded = sum(1 for r in state.values() if r['source_status'] == 'uploaded')
    
    print(f"\n{CYAN}╔════════════════════════════════════════════════════════════════════════════════════╗{RESET}")
    print(f"{CYAN}║  PIPELINE STATUS                                                   ║{RESET}")
    print(f"{CYAN}╠════════════════════════════════════════════════════════════════════════════════════╣{RESET}")
    print(f"{CYAN}║  Total lectures:  {total:4d}  |  Complete: {complete:3d}  |  Generating: {generating:3d}  ║{RESET}")
    print(f"{CYAN}║  Missing: {missing:3d}  |  Failed: {failed:3d}  |  Sources uploaded: {uploaded:3d}  ║{RESET}")
    print(f"{CYAN}╚════════════════════════════════════════════════════════════════════════════════════╝{RESET}")
    
    # Per-account breakdown
    print(f"\n{WHITE}Per-Account Breakdown:{RESET}")
    accounts = load_accounts()
    dl_total = sum(1 for r in state.values() if r.get('download_status') == 'downloaded')
    print(f"{GREEN}  ⬇ Downloaded: {dl_total:3d} of {complete:3d} completed videos{RESET}")
    for acc in accounts:
        email = acc['email']
        acc_complete = sum(1 for r in state.values() if r['assigned_email'] == email and r['video_status'] == 'complete')
        acc_gen = sum(1 for r in state.values() if r['assigned_email'] == email and r['video_status'] == 'generating')
        acc_miss = sum(1 for r in state.values() if r['assigned_email'] == email and r['video_status'] == 'missing')
        acc_src = sum(1 for r in state.values() if r['assigned_email'] == email and r['source_status'] == 'uploaded')
        acc_dl = sum(1 for r in state.values() if r['assigned_email'] == email and r.get('download_status') == 'downloaded')
        print(f"  {email:30s} Complete={acc_complete:3d}  DL={acc_dl:3d}  Gen={acc_gen:3d}  Miss={acc_miss:3d}  Src={acc_src:3d}")

    # Quota snapshot (persistent, from quota_state.json)
    try:
        qt = QuotaTracker(config)
        reset_local = qt.next_reset_utc().astimezone(qt.tz).strftime('%Y-%m-%d %H:%M')
        print(f"\n{WHITE}Quota Today (resets {reset_local} {qt._today_key()}):{RESET}")
        for acc in accounts:
            ok, detail = qt.can_generate_detail(acc)
            tag = "OK  " if ok else "FULL"
            print(f"  {acc['email']:30s} [{tag}] {detail}")
    except Exception as e:
        print(f"  Quota snapshot unavailable: {e}")

    # Cleanup queue snapshot
    try:
        q = load_cleanup_queue()
        print(f"{WHITE}Cleanup queue:{RESET} {len(q)} orphan notebook(s) awaiting deletion "
              f"(menu 8 to process)")
    except Exception:
        pass


async def mode_reassign(state, quota_tracker):
    """Ownership reassignment: stranded rescue + capacity rebalance + cleanup."""
    log("> Running REASSIGN mode...", "PROGRESS")
    accounts = load_accounts()
    blocked_set = set(get_blocked_emails())
    alive = [a for a in accounts if a['email'] not in blocked_set]

    stranded = find_stranded(state, blocked_set)
    total_pending = len(_pending_codes(state))
    queue = load_cleanup_queue()

    print(f"\n{CYAN}══ REASSIGNMENT ANALYSIS ══{RESET}")
    print(f"{WHITE}  Blocked accounts : {len(blocked_set)}{RESET}")
    print(f"{WHITE}  Alive accounts   : {len(alive)}{RESET}")
    print(f"{WHITE}  Pending lectures : {total_pending}{RESET}")
    print(f"{WHITE}  Stranded (blocked owners): {len(stranded)}{RESET}")
    print(f"{WHITE}  Cleanup queue    : {len(queue)} orphan notebook(s){RESET}")

    any_applied = False

    # ── 1. Stranded rescue (blocked owners) ──
    if stranded:
        plan = plan_reassignment(state, accounts, stranded, quota_tracker, blocked_set)
        for tgt, codes in plan.items():
            print(f"  propose → {tgt}: {len(codes)} stranded lecture(s)")
        if ask("Reassign stranded lectures", default_yes=True):
            moved = apply_reassignment(state, plan)
            log(f"+ Reassigned {moved} stranded lecture(s)", "OK")
            any_applied = True
    elif blocked_set:
        log("  Stranded: none (blocked accounts have no pending work).", "INFO")

    # ── 2. Rebalance (idle capacity vs backlog) ──
    plan, meta = plan_rebalance(state, accounts, quota_tracker, blocked_set)
    if plan and meta:
        cur, bal, total, cap = meta
        print(f"\n  REBALANCE available: finish ~{cur:.1f} days → ~{bal} days")
        for tgt, codes in plan.items():
            print(f"    propose → {tgt}: +{len(codes)} lecture(s)")
        if ask("Apply rebalance", default_yes=False):
            moved = apply_reassignment(state, plan)
            log(f"+ Rebalanced {moved} lecture(s)", "OK")
            any_applied = True
    elif meta:
        cur, bal, total, cap = meta
        log(f"  Rebalance: not worthwhile (current ~{cur:.1f}d vs balanced ~{bal}d).", "INFO")

    # ── 3. Chain CREATE for new owners ──
    if any_applied and ask("Run CREATE now to build notebooks on new owners", default_yes=True):
        state = await mode_create(state)
        log("  Next: run GENERATE (or RUN ALL) to produce the moved lectures.", "INFO")

    # ── 4. Cleanup queue ──
    queue = load_cleanup_queue()
    if queue:
        if ask(f"Process cleanup queue now ({len(queue)} orphan(s) on alive accounts)", default_yes=True):
            deleted = await drain_cleanup_queue(accounts, state)
            log(f"🧹 Cleanup: {deleted} orphan notebook(s) deleted.", "OK")
    else:
        log("  Cleanup queue empty.", "INFO")

    save_state(state)
    log("+ REASSIGN complete.", "OK")
    return state


async def mode_auth(state, quota_tracker):
    """Validate & refresh account auth status."""
    from notebooklm import NotebookLMClient
    
    auth_file = SCRIPT_DIR / "auth_state.json"
    accounts = load_accounts()
    
    # Load or create auth_state.json
    if auth_file.exists():
        with open(auth_file, 'r', encoding='utf-8') as f:
            auth_state = json.load(f)
    else:
        auth_state = {
            "last_full_check": now(),
            "accounts": {}
        }
    
    print(f"\n{CYAN}╔════════════════════════════════════════════════════════════════════════════════════╗{RESET}")
    print(f"{CYAN}║  AUTH VALIDATION & REFRESH                                        ║{RESET}")
    print(f"{CYAN}╠═══════════════════════════════════════════════════════════════════════════════════╣{RESET}")
    
    any_changed = False
    
    for acc in accounts:
        email = acc['email']
        profile = acc.get('profile', email.split('@')[0])
        profile_path = Path.home() / ".notebooklm" / "profiles" / profile / "storage_state.json"
        
        print(f"\n{CYAN}--- Checking: {email} (profile: {profile}) ---{RESET}", flush=True)
        
        # Initialize account auth state
        if email not in auth_state['accounts']:
            auth_state['accounts'][email] = {
                "email": email,
                "profile": profile,
                "tier": acc.get('tier', 'standard'),
                "auth_status": "unknown",
                "blocked": False,
                "last_check": None,
                "last_error": None,
                "error_count_today": auth_state['accounts'].get(email, {}).get('error_count_today', 0),
                "last_reauth": None,
                "reauth_method": None,
                "notebook_count": 0
            }
        
        acc_auth = auth_state['accounts'][email]
        
        # Test auth by trying to list notebooks
        try:
            async with NotebookLMClient.from_storage(
                str(profile_path),
                keepalive=600,
                keepalive_min_interval=60,
            ) as client:
                notebooks = await client.notebooks.list()
                acc_auth['auth_status'] = "valid"
                acc_auth['blocked'] = False
                acc_auth['notebook_count'] = len(notebooks)
                acc_auth['last_check'] = now()
                acc_auth['last_error'] = None
                acc_auth['error_count_today'] = 0
                acc_auth['reauth_method'] = None
                acc_auth['last_reauth'] = now()
                log(f"✓ {email}: auth valid ({len(notebooks)} notebooks)", "OK")
        except Exception as e:
            acc_auth['auth_status'] = "expired"
            acc_auth['last_error'] = str(e)[:100]
            acc_auth['blocked'] = True
            acc_auth['error_count_today'] = auth_state['accounts'].get(email, {}).get('error_count_today', 0) + 1
            
            # Attempt re-auth: Method 1 - Browser cookie extraction
            log(f"⚠ {email}: auth failed - {str(e)[:80]}...", "WAIT")
            log(f"  Attempting browser cookie re-auth...", "PROGRESS")
            
            reauth_success = False
            reauth_method = None

            # Method 0: L3 headless re-mint (SILENT — persistent browser profile,
            # no popup, no password). Cheapest recovery; try before anything loud.
            log(f"  Attempting silent headless re-mint (L3)...", "PROGRESS")
            try:
                async with NotebookLMClient.from_storage(
                    str(profile_path), keepalive=600,
                ) as client:
                    await client.refresh_auth(allow_headless=True)
                    notebooks = await client.notebooks.list()
                    reauth_success = True
                    reauth_method = "headless_l3"
                    acc_auth['auth_status'] = "valid"
                    acc_auth['blocked'] = False
                    acc_auth['notebook_count'] = len(notebooks)
                    acc_auth['last_check'] = now()
                    acc_auth['last_error'] = None
                    acc_auth['error_count_today'] = 0
                    acc_auth['reauth_method'] = "headless_l3"
                    acc_auth['last_reauth'] = now()
                    log(f"✓ {email}: silent headless re-mint OK ({len(notebooks)} notebooks)", "OK")
            except Exception as e0:
                log(f"  Headless re-mint failed (SSO likely dead): {str(e0)[:70]}", "WAIT")

            # Method 1: Browser cookie extraction (fast, no UI)
            if not reauth_success:
                log(f"  Trying browser cookies for: {email} (profile: {profile})", "INFO")
                try:
                    import subprocess
                    result = subprocess.run(
                        [str(SCRIPT_DIR / 'venv_nlm' / 'Scripts' / 'notebooklm'), 'login', '--storage', str(profile_path), '--browser-cookies', 'chrome', '--account', email],
                        capture_output=True, text=True, timeout=60
                    )
                    if result.returncode == 0:
                        # Re-test auth
                        async with NotebookLMClient.from_storage(
                            str(profile_path),
                            keepalive=600,
                            keepalive_min_interval=60,
                        ) as client:
                            notebooks = await client.notebooks.list()
                            reauth_success = True
                            reauth_method = "browser_cookies"
                            acc_auth['auth_status'] = "valid"
                            acc_auth['blocked'] = False
                            acc_auth['notebook_count'] = len(notebooks)
                            acc_auth['last_check'] = now()
                            acc_auth['last_error'] = None
                            acc_auth['error_count_today'] = 0
                            acc_auth['reauth_method'] = "browser_cookies"
                            acc_auth['last_reauth'] = now()
                            log(f"✓ {email}: re-auth successful via browser cookies ({len(notebooks)} notebooks)", "OK")
                    else:
                        log(f"  Browser cookie re-auth failed: {result.stderr[:200]}", "FAIL")
                except Exception as e2:
                    log(f"  Browser cookie re-auth error: {str(e2)[:200]}", "FAIL")
            
            # Method 2: Fresh login (opens browser)
            if not reauth_success:
                print(f"\n{YELLOW}{'='*60}{RESET}", flush=True)
                print(f"{YELLOW}  BROWSER LOGIN REQUIRED: {email}{RESET}", flush=True)
                print(f"{YELLOW}  A browser window will open for Google login.{RESET}", flush=True)
                print(f"{YELLOW}  Log in with: {email}{RESET}", flush=True)
                print(f"{YELLOW}  After login, the browser will close automatically.{RESET}", flush=True)
                print(f"{YELLOW}{'='*60}{RESET}\n", flush=True)
                try:
                    result = subprocess.run(
                        [str(SCRIPT_DIR / 'venv_nlm' / 'Scripts' / 'notebooklm'), 'login', '--storage', str(profile_path), '--fresh'],
                        capture_output=True, text=True, timeout=120
                    )
                    if result.returncode == 0:
                        # Re-test auth
                        async with NotebookLMClient.from_storage(
                            str(profile_path),
                            keepalive=600,
                            keepalive_min_interval=60,
                        ) as client:
                            notebooks = await client.notebooks.list()
                            reauth_success = True
                            reauth_method = "fresh_login"
                            acc_auth['auth_status'] = "valid"
                            acc_auth['blocked'] = False
                            acc_auth['notebook_count'] = len(notebooks)
                            acc_auth['last_check'] = now()
                            acc_auth['last_error'] = None
                            acc_auth['error_count_today'] = 0
                            acc_auth['reauth_method'] = "fresh_login"
                            acc_auth['last_reauth'] = now()
                            log(f"✓ {email}: re-auth successful via fresh login ({len(notebooks)} notebooks)", "OK")
                    else:
                        log(f"  Fresh login failed: {result.stderr[:200]}", "FAIL")
                except Exception as e3:
                    log(f"  Fresh login error: {str(e3)[:200]}", "FAIL")
            
            # Method 3: Master token (if available)
            if not reauth_success:
                log(f"  Trying master token for: {email}", "INFO")
                log(f"  Attempting master token re-mint...", "PROGRESS")
                try:
                    # Check if master_token.json exists
                    mt_path = str(profile_path).replace('storage_state.json', 'master_token.json')
                    if os.path.exists(mt_path):
                        async with NotebookLMClient.from_storage(
                            str(profile_path),
                            keepalive=600,
                            keepalive_min_interval=60,
                        ) as client:
                            notebooks = await client.notebooks.list()
                            reauth_success = True
                            reauth_method = "master_token"
                            acc_auth['auth_status'] = "valid"
                            acc_auth['blocked'] = False
                            acc_auth['notebook_count'] = len(notebooks)
                            acc_auth['last_check'] = now()
                            acc_auth['last_error'] = None
                            acc_auth['error_count_today'] = 0
                            acc_auth['reauth_method'] = "master_token"
                            acc_auth['last_reauth'] = now()
                            log(f"✓ {email}: re-auth successful via master token ({len(notebooks)} notebooks)", "OK")
                    else:
                        log(f"  No master_token.json found for {email}", "WAIT")
                except Exception as e4:
                    log(f"  Master token re-mint error: {str(e4)[:200]}", "FAIL")
            
            if not reauth_success:
                acc_auth['auth_status'] = "error"
                acc_auth['blocked'] = True
                log(f"✗ {email}: ALL re-auth methods failed - manual intervention required", "FAIL")
                log(f"  Run: notebooklm login --storage <path> --fresh for: {email}", "INFO")
            else:
                # Account just came alive — drain its orphan-notebook queue
                drained = await drain_cleanup_queue([acc], state)
                if drained:
                    log(f"  🧹 Cleanup: deleted {drained} orphan notebook(s) from {email}", "OK")
        
        any_changed = True
    
    # Save auth_state.json
    auth_state['last_full_check'] = now()
    with open(auth_file, 'w', encoding='utf-8') as f:
        json.dump(auth_state, f, indent=2, ensure_ascii=False)
    log(f"💾 Saved auth state to {auth_file}", "OK")
    
    print(f"{CYAN}╚═══════════════════════════════════════════════════════════════════════════════════╝{RESET}")
    
    # Summary
    valid = sum(1 for a in auth_state['accounts'].values() if a['auth_status'] == 'valid')
    errors = sum(1 for a in auth_state['accounts'].values() if a['auth_status'] == 'error')
    print(f"\n{GREEN}Summary: {valid} valid, {errors} error{RESET}\n")
    
    return auth_state


def should_auto_auth(accounts, config):
    """Check if auto-auth should run based on auth state."""
    auth_file = SCRIPT_DIR / "auth_state.json"
    if not auth_file.exists():
        return True
    if not config:
        return False
    try:
        with open(auth_file, 'r', encoding='utf-8') as f:
            auth_state = json.load(f)
        for email, acc in auth_state.get('accounts', {}).items():
            if acc.get('auth_status') in ('expired', 'error'):
                return True
            if acc.get('error_count_today', 0) >= config.get('auth_reauth_threshold', 3):
                return True
    except Exception:
        return True
    return False


# ──────────────────────────────────────────────
# MODE: RUN ALL (Continuous Loop)
# ──────────────────────────────────────────────
async def mode_run_all(state, quota_tracker):
    """Continuous loop until all videos are complete and downloaded."""
    log("> RUN ALL mode — running until all 204 videos are complete and downloaded", "PROGRESS")
    log("⏹ Press Ctrl+C to stop gracefully and save state", "WAIT")
    
    total_lectures = 204
    cycle = 0
    
    while True:
        cycle += 1
        log(f"\n{'='*60}")
        log(f"CYCLE {cycle}", "PROGRESS")
        log(f"{'='*60}", "PROGRESS")
        
        # Rotate cookies at the start of each cycle
        accounts = load_accounts()
        rotate_all_accounts(accounts, state)
        
        # Abort cleanly if every account is blocked
        blocked_now = set(get_blocked_emails())
        if blocked_now and blocked_now >= {a['email'] for a in accounts}:
            log(f"⛔ ALL {len(accounts)} accounts are BLOCKED — stopping RUN ALL.", "FAIL")
            log("  Re-auth via option 7, then relaunch RUN ALL. Progress saved.", "WAIT")
            break
        
        # Drain queued orphan-notebook deletions for alive accounts
        swept = await drain_cleanup_queue(accounts, state)
        if swept:
            log(f"  🧹 Cleanup sweep: {swept} orphan notebook(s) deleted", "OK")
        
        # 1. CHECK
        state = await mode_check(state)
        
        # Count status
        complete = sum(1 for r in state.values() if r['video_status'] == 'complete')
        missing = sum(1 for r in state.values() if r['video_status'] == 'missing')
        
        # 2. CREATE (only if notebooks missing)
        missing_notebooks = sum(1 for r in state.values() if not r.get('notebook_id'))
        if missing_notebooks > 0:
            log(f"  Creating {missing_notebooks} missing notebooks...", "PROGRESS")
            state = await mode_create(state)
        
        # 3. DOWNLOAD completed videos (BEFORE generate — quota sleep must
        #    never starve downloads of videos that are already finished)
        state = await mode_download(state)
        
        # 4. GENERATE (respecting quotas; auto-sleeps to next reset only
        #    when nothing else useful is pending)
        to_generate = [code for code, r in state.items() 
                       if r['video_status'] not in ('complete', 'generating') and r.get('notebook_id')]
        if to_generate:
            state = await mode_generate(state, quota_tracker, auto_wait=True)
        
        # 5. STATUS
        mode_status(state)
        
        # Done only when everything is generated AND downloaded
        if complete >= total_lectures and not pending_downloads_exist(state):
            log(f"+ All {total_lectures} lectures complete and downloaded!", "OK")
            break
        
        # Wait before next cycle
        wait_sec = config.get('delay_between_accounts_sec', 30)
        log(f"  Waiting {wait_sec}s before next cycle...", "WAIT")
        await asyncio.sleep(wait_sec)
    
    log(f"\n{'='*60}", "OK")
    log(f"+ ALL {total_lectures} LECTURES COMPLETE — Pipeline finished!", "OK")
    log(f"{'='*60}", "OK")
    
    # Final save
    save_state(state)
    return state


# ──────────────────────────────────────────────
# MAIN MENU
# ──────────────────────────────────────────────
def show_menu():
    """Show the main menu."""
    print(f"\n{CYAN}{'='*70}{RESET}")
    print(f"{CYAN}  TradeLab NLM Pipeline Manager v2{RESET}")
    print(f"{CYAN}{'='*70}{RESET}")
    print(f"{WHITE}  1. CHECK     — Audit notebooks & update state{RESET}")
    print(f"{WHITE}  2. CREATE    — Create missing notebooks/sources{RESET}")
    print(f"{WHITE}  3. GENERATE  — Trigger video generation{RESET}")
    print(f"{WHITE}  4. DOWNLOAD  — Download completed videos{RESET}")
    print(f"{WHITE}  5. STATUS    — Show pipeline status{RESET}")
    print(f"{WHITE}  6. RUN ALL   — Continuous loop until all done{RESET}")
    print(f"{WHITE}  7. AUTH      — Validate & refresh account auth{RESET}")
    print(f"{WHITE}  8. REASSIGN  — Move stranded/backlog lectures + cleanup{RESET}")
    print(f"{WHITE}  0. EXIT                                          {RESET}")
    print(f"{CYAN}{'='*70}{RESET}")


# ──────────────────────────────────────────────
# ENTRY POINT
# ──────────────────────────────────────────────
def main():
    global config
    
    # Load configuration
    config = load_config()
    accounts = load_accounts()
    state = load_state()
    # Auto-initialize CSV from JSON if missing or incomplete
    init_csv_from_json()
    
    # Initialize quota tracker
    quota_tracker = QuotaTracker(config)
    
    # Warn about blocked accounts up front
    blocked = get_blocked_emails()
    if blocked:
        log(f"⚠ {len(blocked)} account(s) BLOCKED: "
            f"{', '.join(b.split('@')[0] for b in blocked)}", "WAIT")
        log("  These are skipped everywhere until re-auth — run option 7 (AUTH).", "WAIT")
    
    # Auto-check & refresh auth if needed (before CREATE/GENERATE operations)
    if should_auto_auth(accounts, config):
        log("Auto-checking auth status...", "PROGRESS")
        asyncio.run(mode_auth(state, quota_tracker))
    
    while True:
        show_menu()
        choice = input(f"\n{WHITE}Enter choice (0-8): {RESET}").strip()
        
        if choice == '0':
            log("👋 Exiting. State saved.", "OK")
            save_state(state)
            break
        
        elif choice == '1':
            asyncio.run(mode_check(state))
        
        elif choice == '2':
            asyncio.run(mode_create(state))
        
        elif choice == '3':
            asyncio.run(mode_generate(state, quota_tracker))
        
        elif choice == '4':
            asyncio.run(mode_download(state))
        
        elif choice == '5':
            mode_status(state)
        
        elif choice == '6':
            asyncio.run(mode_run_all(state, quota_tracker))
        
        elif choice == '7':
            asyncio.run(mode_auth(state, quota_tracker))
        
        elif choice == '8':
            state = asyncio.run(mode_reassign(state, quota_tracker))
            state = load_state()  # reload: reassignment/CREATE may have rewritten CSV
        
        else:
            log("fail Invalid choice. Enter 0-8.", "FAIL")


if __name__ == "__main__":
    main()