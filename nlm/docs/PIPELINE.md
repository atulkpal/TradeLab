# NLM Video Generation Pipeline (LEGACY SCRIPTS)

> **⚠️ SUPERSEDED for day-to-day operation** — the unified manager **`pipeline.py`**
> (menu modes 0-8) replaces the manual script sequence below. Its architecture,
> subsystems, state files, config reference, and runbooks live in
> **[`PIPELINE_V2.md`](./PIPELINE_V2.md) — read that first.**
>
> This doc remains accurate for the legacy one-shot scripts
> (`extract_all_courses_fixed.py`, `polish_videos.py`, `upload_to_firebase.py`)
> and the original Firebase/FFmpeg stages, which `pipeline.py` does not cover.

## Overview

This document describes the complete pipeline for generating educational video content for the TradeLab Academy using Google NotebookLM.

**Goal**: Generate 204 lecture videos (6 courses, 68 chapters) with branded intro/outro, upload to Firebase Storage, and integrate into the TradeLab Android app.

---

## Architecture Overview

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Lecture Data   │────▶│  NotebookLM API  │────▶│  Video Files    │
│  (academy_data  │     │  (7 Accounts)    │     │  (MP4)          │
│   _v2.json)     │     │                  │     │                 │
└─────────────────┘     └──────────────────┘     └────────┬────────┘
                                                           │
                                                           ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Firebase       │◀────│  FFmpeg Process  │◀────│  Downloaded     │
│  Storage        │     │  (Intro/Outro)   │     │  Videos         │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                                           │
                                                           ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  TradeLab App   │◀────│  Academy Data    │◀────│  Tracker CSV    │
│  Integration    │     │  Update          │     │  (Tracker)      │
└─────────────────┘     └──────────────────┘     └─────────────────┘
```

---

## Pipeline Stages

### Stage 1: Lecture Extraction
**Script**: `extract_all_courses_fixed.py`  
**Input**: `app/src/main/assets/academy_data_v2.json`  
**Output**: `nlm/lectures/course_{1..6}/lecture_{code}.md` (204 files)  
**Status**: ✅ Complete

### Stage 2: Notebook Creation & Video Generation
**Script**: `master_notebook_manager.py`  
**Input**: Lecture `.md` files + `pending_allocation.json`  
**Output**: NotebookLM notebooks with sources + video artifacts  
**Status**: 🔄 In Progress (all 204 notebooks exist, generating videos)  
**Features**:
- Creates missing notebooks on 7 accounts
- Uploads `.md` source files
- Triggers video generation (SHORT format, ~60-90s)
- 5-minute delay between requests (rate limit safe)
- 30-second delay between accounts

### Stage 3: Video Download
**Script**: `download_all_videos.py`  
**Input**: Completed video artifacts  
**Output**: `nlm/assets/lecture_{code}_{notebook_id}.mp4`  
**Status**: 🔄 In Progress (32 videos downloaded)

### Stage 4: Video Processing
**Script**: `polish_videos.py`  
**Input**: Raw downloaded MP4s  
**Output**: `nlm/assets/out/*.mp4` (branded)  
**Status**: ✅ 23/23 processed  
**Features**:
- 3s intro (logo fade-in, dark bg)
- 3s outro ("Learn on TradeLab" / "No Risk. Real Learning.")
- Branding bar at bottom (80px)
- Random watermark every 5 seconds
- Preserves audio

### Stage 5: Firebase Upload
**Script**: `upload_to_firebase.py`  
**Input**: Processed MP4s  
**Output**: `gs://tradelab-4f858.firebasestorage.app/videos/`  
**Status**: ⏳ Pending (requires Firebase Storage enabled)

### Stage 6: App Integration
**Components**: `VideoPlayerView.kt` + `VideoCacheManager.kt`  
**Status**: ✅ Ready  
**Model**: Download-once, play-locally with external cache

---

## Account Management

### 7 Google Accounts

| Email | Profile | Tier | Videos/Day |
|-------|---------|------|------------|
| atulkpal@gmail.com | default | Pro | 20 |
| ashwathai.dev@gmail.com | ashwathai | Standard | 3 |
| boss.studio.care@gmail.com | boss_studio | Standard | 3 |
| hi.jumpdroid@gmail.com | hi_jumpdroid | Standard | 3 |
| iiidem.km@gmail.com | iiidem_km | Standard | 3 |
| promptwala.xyz@gmail.com | promptwala | Standard | 3 |
| paulritu120@gmail.com | paulritu | Standard | 3 |

**Total capacity**: 38 videos/day  
**Estimated time**: ~5 days for all 204 lectures

### Auth Management
```bash
# Check auth status
python check_auth.py

# Re-authenticate (interactive)
python check_auth.py
# Choose: 1 (Chrome cookies) or 2 (browser login)

# Manual login
notebooklm login --storage "C:/Users/Atul/.notebooklm/profiles/{profile}/storage_state.json" --fresh
```

---

## Rate Limits

| Resource | Limit |
|---|---|
| Video generation (Standard) | 3/day/account |
| Video generation (Pro) | 20/day/account |
| Between video generations | 5 minutes |
| Between accounts | 30 seconds |
| After rpc_code=8 | Wait 2min, retry |

---

## Data Flow

```
academy_data_v2.json (204 lectures)
    │
    ▼
extract_all_courses_fixed.py
    │
    ▼
nlm/lectures/course_{1..6}/lecture_{code}.md (204 files)
    │
    ▼
pending_allocation.json (196/204 allocated)
    │
    ▼
master_notebook_manager.py (7 accounts, 5-min delay)
    │
    ▼
204 notebooks on NotebookLM → 38 videos/day
    │
    ▼
download_all_videos.py
    │
    ▼
nlm/assets/*.mp4 (raw videos)
    │
    ▼
polish_videos.py (intro + outro + watermark)
    │
    ▼
nlm/assets/out/*.mp4 (branded videos)
    │
    ▼
upload_to_firebase.py
    │
    ▼
Firebase Storage → TradeLab App
```

---

*Last updated: 2026-08-23*
