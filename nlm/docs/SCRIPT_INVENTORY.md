# NLM Script Inventory

Complete reference of all scripts in the NLM video generation pipeline.

---

## Active Scripts (in `nlm/`)

| Script | Purpose | Usage |
|--------|---------|-------|
| `master_notebook_manager.py` | Create notebooks, add sources, generate videos | `python master_notebook_manager.py` |
| `check_auth.py` | Check auth status, re-authenticate expired accounts | `python check_auth.py` |
| `audit_notebooks.py` | Audit notebooks, find/delete duplicates | `python audit_notebooks.py [--delete]` |
| `download_all_videos.py` | Download completed videos from all accounts | `python download_all_videos.py` |
| `polish_videos.py` | Add intro/outro/watermark branding | `python polish_videos.py` |
| `extract_all_courses_fixed.py` | Extract 204 lectures from academy_data_v2.json | `python extract_all_courses_fixed.py` |
| `upload_to_firebase.py` | Upload processed videos to Firebase Storage | `python upload_to_firebase.py` |

---

## Script Details

### `master_notebook_manager.py`
**Purpose**: Main pipeline script — creates notebooks, adds sources, generates videos  
**Usage**: `python master_notebook_manager.py`  
**Features**:
- Reads `pending_allocation.json` for lecture → account mapping
- Creates missing notebooks on NotebookLM
- Uploads `.md` source files
- Triggers video generation (SHORT format)
- **5-minute delay** between video generation requests (rate limit safe)
- **30-second delay** between accounts
- Skips lectures in `DONE_LECTURES` set

### `check_auth.py`
**Purpose**: Check authentication status for all 7 accounts, re-authenticate if needed  
**Usage**: `python check_auth.py`  
**Features**:
- Tests auth for all accounts
- Detects expired/missing auth
- Offers two re-auth methods:
  1. Chrome cookies (extract from logged-in Chrome)
  2. Browser login (opens new Chromium window)
- Uses `notebooklm login --storage {path} --fresh`

### `audit_notebooks.py`
**Purpose**: Audit all accounts for notebooks, videos, duplicates  
**Usage**: `python audit_notebooks.py [--delete] [--account EMAIL]`  
**Features**:
- Lists all TradeLab notebooks per account
- Checks video status per notebook
- Finds duplicates by title
- `--delete`: Deletes duplicate notebooks without videos
- Keeps duplicates that have videos

### `download_all_videos.py`
**Purpose**: Download completed videos from all 7 accounts  
**Usage**: `python download_all_videos.py`  
**Output**: `nlm/assets/lecture_{code}_{notebook_id}.mp4`  
**Features**:
- Downloads in parallel across all accounts
- Skips already-downloaded files
- Currently: 32 videos downloaded (11 unique lectures)

### `polish_videos.py`
**Purpose**: Add branded intro/outro and watermark to videos  
**Usage**: `python polish_videos.py`  
**Input**: `nlm/assets/*.mp4`  
**Output**: `nlm/assets/out/*.mp4`  
**Features**:
- Creates 3s intro (logo fade-in, dark bg)
- Creates 3s outro ("Learn on TradeLab" / "No Risk. Real Learning.")
- Trims videos to 3 seconds shorter
- Adds branding bar at bottom (80px, TradeLab + copyright)
- Random watermark every 5 seconds
- Preserves audio
- Currently: 23/23 videos processed

### `extract_all_courses_fixed.py`
**Purpose**: Extract all 204 lectures from academy_data_v2.json  
**Usage**: `python extract_all_courses_fixed.py`  
**Output**: `nlm/lectures/course_{1..6}/lecture_{code}.md`  
**Status**: ✅ Complete — 204 .md files created

### `upload_to_firebase.py`
**Purpose**: Upload processed videos to Firebase Storage  
**Usage**: `python upload_to_firebase.py`  
**Input**: `nlm/assets/out/*.mp4`  
**Output**: `gs://tradelab-4f858.firebasestorage.app/videos/`  
**Status**: ⏳ Pending — requires Firebase Storage enabled

---

## Key Files

| File | Purpose |
|------|---------|
| `pending_allocation.json` | Lecture → Account mapping (196/204 allocated) |
| `academy_data_v2_pretty.json` | Source of truth for 204 lectures |
| `nlm/lectures/course_{1..6}/` | 204 lecture .md files |
| `nlm/assets/` | Raw downloaded videos |
| `nlm/assets/out/` | Processed videos with branding |

---

## Execution Order

```bash
# 1. Check/re-auth accounts
python check_auth.py

# 2. Extract lectures (already done)
python extract_all_courses_fixed.py

# 3. Create notebooks & generate videos
python master_notebook_manager.py

# 4. Download completed videos
python download_all_videos.py

# 5. Process videos (add branding)
python polish_videos.py

# 6. Upload to Firebase (requires Storage enabled)
python upload_to_firebase.py
```

---

## Dependencies

- Python 3.10+
- `notebooklm-py` v0.8.1+ (in `venv_nlm/`)
- ffmpeg (for video processing)
- 7 Google accounts authenticated

---

*Last updated: 2026-08-23*
