#!/usr/bin/env python3
"""
Download all completed videos from all 7 accounts.
"""

import asyncio
import json
import sys
from pathlib import Path

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

SCRIPT_DIR = Path(__file__).parent
OUTPUT_DIR = SCRIPT_DIR / "assets"
OUTPUT_DIR.mkdir(exist_ok=True)

async def download_account_videos(account):
    profile_path = f"C:/Users/Atul/.notebooklm/profiles/{account['profile']}/storage_state.json"
    
    try:
        client = await NotebookLMClient.from_storage(profile_path)
        async with client:
            notebooks = await client.notebooks.list()
            downloaded = 0
            
            for nb in notebooks:
                title = getattr(nb, 'title', '')
                nb_id = getattr(nb, 'id', '')
                
                if 'TradeLab' not in title:
                    continue
                
                # Extract lecture code
                import re
                code_match = re.search(r'Lecture\s+(\d+\.\d+\.\d+)', title)
                if not code_match:
                    continue
                code = code_match.group(1).replace('.', '_')
                
                artifacts = await client.artifacts.list(nb_id)
                for artifact in artifacts:
                    if artifact.kind.name == 'VIDEO' and artifact.is_completed:
                        filename = f"lecture_{code}_{nb_id}.mp4"
                        output = OUTPUT_DIR / filename
                        
                        if output.exists():
                            continue
                        
                        print(f"DOWNLOAD: {filename}")
                        try:
                            await client.artifacts.download_video(nb_id, str(output))
                            size_mb = output.stat().st_size / 1024 / 1024
                            print(f"  DONE: {filename} ({size_mb:.1f} MB)")
                        except Exception as e:
                            print(f"  ERROR: {e}")
                            if output.exists():
                                output.unlink()
                downloaded += 1
            
            return {"email": account['email'], "downloaded": downloaded}
            
    except Exception as e:
        return {"email": account['email'], "error": str(e)}

async def main():
    print("Downloading all completed videos...")
    tasks = [download_account_videos(account) for account in ACCOUNTS]
    results = await asyncio.gather(*tasks, return_exceptions=True)
    
    for r in results:
        if isinstance(r, Exception):
            print(f"ERROR: {r}")
        elif 'error' in r:
            print(f"  {r['email']}: ERROR - {r['error']}")
        else:
            print(f"  {r['email']}: {r['downloaded']} videos")

if __name__ == "__main__":
    asyncio.run(main())