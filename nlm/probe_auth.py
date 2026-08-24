"""Probe real auth status of every blocked account (same test option 7 uses)."""
import asyncio
import sys
from pathlib import Path

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
sys.path.insert(0, str(Path(__file__).parent))
sys.path.insert(0, str(Path(__file__).parent / "venv_nlm" / "Lib" / "site-packages"))

import os
os.environ.pop("TZ", None)

from notebooklm import NotebookLMClient

HOME = Path.home()
ACCOUNTS = [
    ("ashwathai.dev@gmail.com", "ashwathai"),
    ("boss.studio.care@gmail.com", "boss_studio"),
    ("hi.jumpdroid@gmail.com", "hi_jumpdroid"),
    ("iiidem.km@gmail.com", "iiidem_km"),
    ("promptwala.xyz@gmail.com", "promptwala"),
    ("paulritu120@gmail.com", "paulritu"),
]


async def probe(email, profile):
    storage = str(HOME / ".notebooklm" / "profiles" / profile / "storage_state.json")
    try:
        async with NotebookLMClient.from_storage(storage, keepalive=600) as client:
            notebooks = await client.notebooks.list()
            return f"ALIVE  ({len(notebooks)} notebooks)"
    except Exception as e:
        return f"DEAD   ({str(e)[:70]})"


async def main():
    for email, profile in ACCOUNTS:
        result = await probe(email, profile)
        print(f"{email:32s} {result}")

asyncio.run(main())
