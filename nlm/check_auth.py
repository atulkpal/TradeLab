#!/usr/bin/env python3
"""Check auth for all accounts. Re-authenticate expired/missing ones."""

import subprocess
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent
VENV_PATH = SCRIPT_DIR / "venv_nlm" / "Lib" / "site-packages"
PYTHON = str(SCRIPT_DIR / "venv_nlm" / "Scripts" / "python.exe")
PROFILES_DIR = Path("C:/Users/Atul/.notebooklm/profiles")
sys.path.insert(0, str(VENV_PATH))

ACCOUNTS = [
    ("atulkpal@gmail.com", "default"),
    ("ashwathai.dev@gmail.com", "ashwathai"),
    ("boss.studio.care@gmail.com", "boss_studio"),
    ("hi.jumpdroid@gmail.com", "hi_jumpdroid"),
    ("iiidem.km@gmail.com", "iiidem_km"),
    ("promptwala.xyz@gmail.com", "promptwala"),
    ("paulritu120@gmail.com", "paulritu"),
]


def test_auth(profile: str) -> tuple[bool, str]:
    storage_path = str(PROFILES_DIR / profile / "storage_state.json")
    try:
        result = subprocess.run(
            [PYTHON, "-c", f"""
import sys; sys.path.insert(0, r'{VENV_PATH}')
from notebooklm import NotebookLMClient
import asyncio

async def test():
    client = await NotebookLMClient.from_storage(r'{storage_path}')
    async with client:
        notebooks = await client.notebooks.list()
        print(len(notebooks))

asyncio.run(test())
"""],
            capture_output=True, text=True, timeout=30,
            env={**__import__("os").environ, "PYTHONIOENCODING": "utf-8"}
        )
        if result.returncode == 0:
            count = result.stdout.strip()
            return True, f"{count} notebooks"
        else:
            err = result.stderr.strip().split("\n")[-1] if result.stderr else "unknown"
            return False, err
    except Exception as e:
        return False, str(e)


def re_authenticate_browser(profile: str, email: str):
    print(f"\n  Opening browser for {email}...")
    print(f"  Login as: {email}, then come back here.")
    storage_path = str(PROFILES_DIR / profile / "storage_state.json")
    subprocess.run(
        [PYTHON, "-m", "notebooklm", "login", "--storage", storage_path, "--fresh"],
        cwd=str(SCRIPT_DIR),
    )


def re_authenticate_cookies(profile: str, email: str):
    print(f"\n  Extracting cookies from Chrome for {email}...")
    print(f"  Make sure you're logged into {email} in Chrome.")
    storage_path = str(PROFILES_DIR / profile / "storage_state.json")
    result = subprocess.run(
        [PYTHON, "-m", "notebooklm", "login",
         "--storage", storage_path,
         "--browser-cookies", "chrome",
         "--account", email],
        cwd=str(SCRIPT_DIR),
        capture_output=True, text=True,
    )
    if result.returncode == 0:
        print(f"  OK: Cookies extracted")
    else:
        print(f"  FAIL: {result.stderr.strip()}")


def main():
    print("=" * 60)
    print("  TRADELAB AUTH CHECKER")
    print("=" * 60)

    failed = []
    for email, profile in ACCOUNTS:
        ok, msg = test_auth(profile)
        if ok:
            print(f"  OK  {email}: {msg}")
        else:
            print(f"FAIL {email}: {msg}")
            failed.append((email, profile))

    if not failed:
        print(f"\nAll {len(ACCOUNTS)} accounts authenticated!")
        return

    print(f"\n{len(failed)} account(s) need re-authentication:")
    for i, (email, profile) in enumerate(failed, 1):
        print(f"  {i}. {email} ({profile})")

    print(f"\nRe-auth method:")
    print(f"  1. Chrome cookies (extract from logged-in Chrome)")
    print(f"  2. Browser login (opens new browser window)")
    print(f"  3. Skip")
    choice = input("> ").strip()

    if choice == "1":
        for email, profile in failed:
            re_authenticate_cookies(profile, email)
    elif choice == "2":
        for email, profile in failed:
            re_authenticate_browser(profile, email)
    else:
        print("Skipping.")
        return

    print(f"\n{'='*60}")
    print("  VERIFYING")
    print(f"{'='*60}")
    for email, profile in failed:
        ok, msg = test_auth(profile)
        if ok:
            print(f"  OK  {email}: {msg}")
        else:
            print(f"FAIL {email}: {msg}")


if __name__ == "__main__":
    main()
