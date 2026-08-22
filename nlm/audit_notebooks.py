#!/usr/bin/env python3
"""
Audit all accounts: list notebooks, check video status, delete duplicates without videos.
"""

import asyncio
import json
import sys
from pathlib import Path
from datetime import datetime
from collections import defaultdict

SCRIPT_DIR = Path(__file__).parent
VENV_PATH = SCRIPT_DIR / "venv_nlm" / "Lib" / "site-packages"
sys.path.insert(0, str(VENV_PATH))

from notebooklm import NotebookLMClient
from notebooklm.types import ArtifactType

ACCOUNTS = [
    {"email": "atulkpal@gmail.com", "profile": "default"},
    {"email": "ashwathai.dev@gmail.com", "profile": "ashwathai"},
    {"email": "boss.studio.care@gmail.com", "profile": "boss_studio"},
    {"email": "hi.jumpdroid@gmail.com", "profile": "hi_jumpdroid"},
    {"email": "iiidem.km@gmail.com", "profile": "iiidem_km"},
    {"email": "promptwala.xyz@gmail.com", "profile": "promptwala"},
    {"email": "paulritu120@gmail.com", "profile": "paulritu"},
]

async def audit_account(account: dict, delete_duplicates: bool = False):
    """Audit a single account's notebooks, optionally delete duplicates without videos."""
    profile_path = f"C:/Users/Atul/.notebooklm/profiles/{account['profile']}/storage_state.json"
    
    print(f"\n{'='*70}")
    print(f"AUDIT: {account['email']}")
    print(f"{'='*70}")
    
    try:
        client = await NotebookLMClient.from_storage(profile_path)
        async with client:
            notebooks = await client.notebooks.list()
            
            if not notebooks:
                print("  No notebooks found")
                return {"email": account['email'], "notebooks": [], "videos": 0, "deleted": 0}
            
            print(f"  Total notebooks: {len(notebooks)}")
            
            # Group TradeLab notebooks by title to find duplicates
            title_to_notebooks = defaultdict(list)
            videos_info = []
            all_tradelab = []
            
            for nb in notebooks:
                title = getattr(nb, 'title', 'Unknown')
                nb_id = getattr(nb, 'id', 'Unknown')
                created = getattr(nb, 'create_time', 'Unknown')
                
                if 'TradeLab' in title:
                    all_tradelab.append(nb)
                    title_to_notebooks[title].append(nb)
            
            print(f"  TradeLab notebooks: {len(all_tradelab)}")
            
            # Check each notebook for video artifacts
            videos_complete = 0
            videos_generating = 0
            notebooks_with_video = set()
            notebooks_without_video = []
            
            for nb in all_tradelab:
                title = getattr(nb, 'title', 'Unknown')
                nb_id = getattr(nb, 'id', 'Unknown')
                
                try:
                    artifacts = await client.artifacts.list(nb_id)
                    
                    has_video = False
                    video_generating = False
                    
                    for artifact in artifacts:
                        if artifact.kind == ArtifactType.VIDEO:
                            has_video = True
                            status = artifact.status_str
                            if artifact.is_completed:
                                videos_complete += 1
                                videos_info.append(f"  [DONE] {title} | ID: {nb_id}")
                            elif artifact.is_processing or artifact.is_pending:
                                videos_generating += 1
                                videos_info.append(f"  [GEN] {title} | ID: {nb_id} | {status}")
                            elif artifact.is_failed:
                                videos_info.append(f"  [FAIL] {title} | ID: {nb_id} | {status}")
                            else:
                                videos_info.append(f"  [?] {status}: {title} | ID: {nb_id}")
                    
                    if has_video:
                        notebooks_with_video.add(nb_id)
                    else:
                        notebooks_without_video.append((title, nb_id))
                        
                except Exception as e:
                    print(f"  ERROR checking artifacts for {title} ({nb_id}): {e}")
                    notebooks_without_video.append((title, nb_id))
            
            print(f"  TradeLab notebooks: {len(all_tradelab)}")
            print(f"  Notebooks with videos: {len(notebooks_with_video)}")
            print(f"  Videos complete: {videos_complete}")
            print(f"  Videos generating: {videos_generating}")
            print(f"  Notebooks without videos: {len(notebooks_without_video)}")
            
            # Find duplicates (same title)
            duplicates_to_delete = []
            for title, notebooks_list in title_to_notebooks.items():
                if len(notebooks_list) > 1:
                    print(f"\n  DUPLICATE TITLE: '{title}' ({len(notebooks_list)} copies)")
                    # Keep the first one (oldest?), delete others if they have no video
                    for i, nb in enumerate(notebooks_list):
                        nb_id = getattr(nb, 'id', 'Unknown')
                        if nb_id not in notebooks_with_video:
                            if i == 0:
                                print(f"    KEEP (first copy, no video): {nb_id}")
                            else:
                                duplicates_to_delete.append((title, nb_id))
                                print(f"    DELETE (duplicate, no video): {nb_id}")
                        else:
                            print(f"    KEEP (has video): {nb_id}")
            
            # Delete duplicates if requested
            deleted_count = 0
            if delete_duplicates and duplicates_to_delete:
                print(f"\n  DELETING {len(duplicates_to_delete)} duplicate notebooks...")
                for title, nb_id in duplicates_to_delete:
                    try:
                        await client.notebooks.delete(nb_id)
                        print(f"  [DELETED] {title} ({nb_id})")
                        deleted_count += 1
                    except Exception as e:
                        print(f"  [ERROR] Failed to delete {title} ({nb_id}): {e}")
            elif duplicates_to_delete:
                print(f"\n  Would delete {len(duplicates_to_delete)} duplicates (run with --delete to execute)")
            
            # Print video status
            if videos_info:
                print(f"\n  VIDEO STATUS:")
                for v in videos_info:
                    print(f"    {v}")
            
            return {
                "email": account['email'],
                "total_notebooks": len(notebooks),
                "tradelab_notebooks": len(all_tradelab),
                "notebooks_with_video": len(notebooks_with_video),
                "videos_complete": videos_complete,
                "videos_generating": videos_generating,
                "deleted": deleted_count,
                "notebooks_without_video": len(notebooks_without_video),
            }
            
    except Exception as e:
        print(f"  ERROR: {e}")
        import traceback
        traceback.print_exc()
        return {"email": account['email'], "error": str(e)}

async def main():
    import argparse
    parser = argparse.ArgumentParser(description="Audit NotebookLM notebooks")
    parser.add_argument("--delete", action="store_true", help="Delete duplicate notebooks without videos")
    parser.add_argument("--account", help="Audit specific account only")
    args = parser.parse_args()
    
    print("="*70)
    print("NOTEBOOK AUDIT - All Accounts")
    print("="*70)
    if args.delete:
        print("MODE: DELETE duplicates without videos")
    else:
        print("MODE: DRY RUN (use --delete to actually delete)")
    print("="*70)
    
    total_notebooks = 0
    total_videos = 0
    total_deleted = 0
    
    for account in ACCOUNTS:
        if args.account and account['email'] != args.account:
            continue
        
        result = await audit_account(account, delete_duplicates=args.delete)
        total_notebooks += result.get('tradelab_notebooks', 0)
        total_videos += result.get('videos_complete', 0)
        total_deleted += result.get('deleted', 0)
    
    # Summary
    print(f"\n{'='*70}")
    print("FINAL SUMMARY")
    print(f"{'='*70}")
    print(f"Total TradeLab notebooks: {total_notebooks}")
    print(f"Total completed videos: {total_videos}")
    print(f"Total deleted: {total_deleted}")
    print(f"{'='*70}")

if __name__ == "__main__":
    asyncio.run(main())