#!/usr/bin/env python3
"""
Master Notebook Manager - Complete Pipeline
1. Checks what notebooks already exist
2. Creates required notebooks for each lecture
3. Deletes duplicates if any
4. Populates notebook with source document
5. Starts video generation on all notebooks one by one with 5 sec delay
"""

import asyncio
import json
import sys
from pathlib import Path
from datetime import datetime

SCRIPT_DIR = Path(__file__).parent
VENV_PATH = SCRIPT_DIR / "venv_nlm" / "Lib" / "site-packages"
sys.path.insert(0, str(VENV_PATH))

from notebooklm import NotebookLMClient
from notebooklm.types import VideoFormat, VideoStyle

# All 7 accounts
ACCOUNTS = [
    {"email": "atulkpal@gmail.com", "profile": "default"},
    {"email": "ashwathai.dev@gmail.com", "profile": "ashwathai"},
    {"email": "boss.studio.care@gmail.com", "profile": "boss_studio"},
    {"email": "hi.jumpdroid@gmail.com", "profile": "hi_jumpdroid"},
    {"email": "iiidem.km@gmail.com", "profile": "iiidem_km"},
    {"email": "promptwala.xyz@gmail.com", "profile": "promptwala"},
    {"email": "paulritu120@gmail.com", "profile": "paulritu"},
]

# Already done lectures
DONE_LECTURES = {
    "1.1.1", "1.1.2",
    "1.11.1",
    "2.8.3",
    "3.6.2",
    "4.6.1",
    "5.3.3",
    "6.1.2",
}

SCRIPT_DIR = Path(__file__).parent
DATA_FILE = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "academy_data_v2.json"
LECTURES_DIR = SCRIPT_DIR / "lectures"
ALLOCATION_FILE = SCRIPT_DIR / "pending_allocation.json"


# Spinner for "alive" indication
_spinner_chars = ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"]
_spinner_idx = 0

def log(msg: str, level="INFO"):
    timestamp = datetime.now().strftime("%H:%M:%S")
    prefix = {
        "INFO": "[INFO]",
        "OK": "[+ OK]",
        "FAIL": "[X FAIL]",
        "WAIT": "[WAIT]",
        "PROGRESS": "[PROGRESS]",
        "VIDEO": "[VIDEO]",
        "NOTEBOOK": "[NOTEBOOK]",
        "SOURCE": "[SOURCE]",
    }.get(level, "[INFO]")
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {prefix} {msg}")

# Global spinner for "alive" indication
_spinner_chars = ["|", "/", "-", "\\"]
_spinner_idx = 0

def spin(msg="Working"):
    global _spinner_idx
    char = _spinner_chars[_spinner_idx % len(_spinner_chars)]
    _spinner_idx += 1
    print(f"\r[{datetime.now().strftime('%H:%M:%S')}] {_spinner_chars[_spinner_idx % len(_spinner_chars)]} {msg}", end="", flush=True)


def load_all_lectures():
    """Load all 204 lectures from academy_data_v2.json"""
    DATA_FILE = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "academy_data_v2.json"
    with open(DATA_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    lectures = {}
    for course in data["courses"]:
        for chapter in course["chapters"]:
            for lecture in chapter["lectures"]:
                title = lecture["title"]
                code = title.split(":")[0].replace("Lecture ", "").strip()
                lectures[code] = {
                    "code": code,
                    "title": lecture["title"],
                    "md_path": str(SCRIPT_DIR / "lectures" / f"course_{course['id']}" / f"lecture_{code.replace('.', '_')}.md"),
                }
    return lectures


def load_allocation():
    """Load pending_allocation.json"""
    with open(ALLOCATION_FILE, 'r') as f:
        return json.load(f)


async def get_existing_notebooks(client):
    """Get all existing TradeLab notebooks"""
    spin("Fetching existing notebooks...")
    notebooks = await client.notebooks.list()
    tradelab_notebooks = []
    for nb in notebooks:
        title = getattr(nb, 'title', '')
        if 'TradeLab' in title:
            tradelab_notebooks.append(nb)
    log(f"  Found {len(tradelab_notebooks)} existing TradeLab notebooks", "OK")
    return tradelab_notebooks


async def get_notebook_artifacts(client, notebook_id):
    """Get video artifacts for a notebook"""
    try:
        spin("Fetching artifacts...")
        artifacts = await client.artifacts.list(notebook_id)
        videos = []
        for artifact in artifacts:
            if artifact.kind == 3:  # VIDEO = 3
                videos.append(artifact)
        log(f"  Found {len(videos)} video artifact(s)", "OK")
        return videos
    except Exception as e:
        log(f"  Error getting artifacts: {e}", "FAIL")
        return []


async def create_or_get_notebook(client, lecture, existing_notebooks):
    """Create notebook if not exists, or return existing one - also delete duplicates"""
    lecture_code = lecture["code"]
    title = f"TradeLab - {lecture['title']}"
    
    # Check if already exists (match by lecture code in title)
    matching_notebooks = []
    for nb in existing_notebooks:
        if getattr(nb, 'title', '') == title:
            matching_notebooks.append(nb)
    
    if matching_notebooks:
        # Keep the first one, delete duplicates
        keep_nb = matching_notebooks[0]
        log(f"  Already exists: {title} ({keep_nb.id})", "OK")
        # Delete duplicates
        for dup in matching_notebooks[1:]:
            try:
                await client.notebooks.delete(dup.id)
                log(f"  Deleted duplicate: {dup.id}", "OK")
            except Exception as e:
                log(f"  Failed to delete duplicate {dup.id}: {e}", "FAIL")
        return keep_nb
    
    # Create new
    spin("Creating notebook...")
    nb = await client.notebooks.create(f"TradeLab - {lecture['title']}")
    log(f"  Created: {lecture['title']} ({nb.id})", "OK")
    return nb


async def add_source_if_needed(client, notebook, lecture, sources):
    """Add source document if not already present"""
    title = f"TradeLab - {lecture['title']}"
    
    # Check if source already exists
    for source in sources:
        if getattr(source, 'title', '') == title:
            log(f"  Source already exists: {title}", "OK")
            return True
    
    # Add source
    try:
        spin("Adding source document...")
        source = await client.sources.add_file(notebook.id, lecture['md_path'], wait=True)
        log(f"  Added source: {lecture['md_path']}", "SOURCE")
        return True
    except Exception as e:
        log(f"  Error adding source: {e}", "FAIL")
        return False


async def generate_video_if_needed(client, notebook_id, lecture):
    """Trigger video generation (non-blocking, fire & forget)"""
    spin("Fetching artifacts...")
    artifacts = await client.artifacts.list(notebook_id)
    
    # Check if video already complete OR is generating
    for artifact in artifacts:
        if artifact.kind == 3:  # VIDEO = 3
            if artifact.is_completed:
                log(f"  Video already complete for {notebook_id}", "OK")
                return True
            # Check if video is currently generating
            if getattr(artifact, 'status', '') == 'generating':
                log(f"  Video is already generating for {notebook_id}", "WAIT")
                return True
    
    # Trigger video generation (fire & forget - NO wait)
    log(f"  Triggering video generation for {notebook_id}...", "VIDEO")
    try:
        status = await client.artifacts.generate_video(
            notebook_id,
            instructions=f"Create an engaging educational video about: {lecture['title']}",
            video_format=VideoFormat.SHORT,
            video_style=VideoStyle.AUTO_SELECT,
            language="en"
        )
        log(f"  Video generation triggered (task: {status.task_id})", "VIDEO")
        return True
            
    except Exception as e:
        log(f"  Error triggering video: {e}", "FAIL")
        return False


async def process_account(account, lectures):
    """Process all lectures for one account"""
    profile_path = f"C:/Users/Atul/.notebooklm/profiles/{account['profile']}/storage_state.json"
    
    log(f"\n{'='*60}")
    log(f"[{account['email']}] Processing {len(lectures)} lectures...", "PROGRESS")
    log(f"{'='*60}")
    
    try:
        client = await NotebookLMClient.from_storage(profile_path)
        async with client:
            # Get existing notebooks
            spin("Fetching existing notebooks...")
            existing_notebooks = await get_existing_notebooks(client)
            log(f"  Found {len(existing_notebooks)} existing TradeLab notebooks", "OK")
            
            created = 0
            updated = 0
            videos_started = 0
            skipped = 0
            
            for idx, lecture in enumerate(lectures, 1):
                code = lecture["code"]
                
                if code in DONE_LECTURES:
                    log(f"  [{idx}/{len(lectures)}] SKIP (already done): {code}", "WAIT")
                    skipped += 1
                    continue
                
                log(f"  [{idx}/{len(lectures)}] PROCESSING: {code} - {lecture['title'][:50]}...", "PROGRESS")
                
                # Step 1-4: Create notebook, add source
                try:
                    notebook = await create_or_get_notebook(client, lecture, existing_notebooks)
                    
                    # Get existing sources
                    sources = await client.sources.list(notebook.id)
                    
                    if await add_source_if_needed(client, notebook, lecture, sources):
                        updated += 1
                    
                    # Refresh notebooks list in case we created new one
                    existing_notebooks = await get_existing_notebooks(client)
                    
                except Exception as e:
                    log(f"  Error with notebook/source: {e}", "FAIL")
                    continue
                
                # Step 5: Generate video
                try:
                    if await generate_video_if_needed(client, notebook.id, lecture):
                        videos_started += 1
                        log(f"  Video generation started for {code}", "VIDEO")
                    else:
                        log(f"  Video generation failed for {code}", "FAIL")
                except Exception as e:
                    log(f"  Error generating video: {e}", "FAIL")
                
                # 5 minute delay between lectures (avoid rate limits)
                log(f"  Waiting 5min before next lecture...", "WAIT")
                await asyncio.sleep(300)
            
            log(f"\n  SUMMARY: {updated} updated, {videos_started} videos started, {skipped} skipped", "PROGRESS")
            
    except Exception as e:
        log(f"  FATAL ERROR: {e}", "FAIL")
        import traceback
        traceback.print_exc()


async def main():
    log("=" * 60)
    log("MASTER NOTEBOOK MANAGER - Complete Pipeline", "PROGRESS")
    log("=" * 60)
    log("1. Check existing notebooks")
    log("2. Create required notebooks")
    log("3. Delete duplicates")
    log("4. Populate with source documents")
    log("5. Start video generation (5 sec delay)")
    log("=" * 60)
    
    # Load allocation
    with open(ALLOCATION_FILE, 'r') as f:
        allocation = json.load(f)
    
    # Load all lectures
    all_lectures = {}
    DATA_FILE = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "academy_data_v2.json"
    with open(DATA_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    for course in data["courses"]:
        for chapter in course["chapters"]:
            for lecture in chapter["lectures"]:
                title = lecture["title"]
                code = title.split(":")[0].replace("Lecture ", "").strip()
                all_lectures[code] = {
                    "code": code,
                    "title": lecture["title"],
                    "md_path": str(SCRIPT_DIR / "lectures" / f"course_{course['id']}" / f"lecture_{code.replace('.', '_')}.md"),
                }
    
    # Process all 7 accounts sequentially
    for account in [
        {"email": "atulkpal@gmail.com", "profile": "default"},
        {"email": "ashwathai.dev@gmail.com", "profile": "ashwathai"},
        {"email": "boss.studio.care@gmail.com", "profile": "boss_studio"},
        {"email": "hi.jumpdroid@gmail.com", "profile": "hi_jumpdroid"},
        {"email": "iiidem.km@gmail.com", "profile": "iiidem_km"},
        {"email": "promptwala.xyz@gmail.com", "profile": "promptwala"},
        {"email": "paulritu120@gmail.com", "profile": "paulritu"},
    ]:
        if account['email'] not in allocation:
            continue
        
        codes = allocation[account['email']]
        lectures = [all_lectures[c] for c in codes if c in all_lectures and c not in DONE_LECTURES]
        
        if not lectures:
            log(f"[{account['email']}] No lectures to process", "WAIT")
            continue
        
        log(f"Starting account: {account['email']} with {len(lectures)} lectures", "PROGRESS")
        await process_account(account, lectures)
        
        # Wait between accounts
        if account != {"email": "promptwala.xyz@gmail.com", "profile": "promptwala"}:
            log("\nWaiting 30s before next account...", "WAIT")
            await asyncio.sleep(30)
    
    log("\n=== PIPELINE COMPLETE ===", "OK")


if __name__ == "__main__":
    asyncio.run(main())