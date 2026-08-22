#!/usr/bin/env python3
"""
Upload processed videos (with intro/outro) to Firebase Storage.
Usage:
    python upload_to_firebase.py                    # Upload all final videos
    python upload_to_firebase.py lecture_1_1_1      # Upload single video
    python upload_to_firebase.py --list             # List uploaded videos
"""

import os
import sys
import json
from pathlib import Path

# Check for gcloud CLI
def check_gcloud():
    """Check if gcloud CLI is available."""
    result = os.system("gcloud --version >nul 2>&1")
    if result != 0:
        print("ERROR: gcloud CLI not found. Install it from:")
        print("  https://cloud.google.com/sdk/docs/install")
        sys.exit(1)

def upload_video(local_path: str, remote_path: str, bucket: str):
    """Upload a video file to Firebase Storage."""
    cmd = f'gcloud storage cp "{local_path}" "gs://{bucket}/{remote_path}"'
    print(f"Uploading: {local_path} -> gs://{bucket}/{remote_path}")
    result = os.system(cmd)
    if result == 0:
        print(f"  OK")
    else:
        print(f"  FAILED")
    return result == 0

def main():
    PROJECT_ID = "tradelab-4f858"
    BUCKET = f"{PROJECT_ID}.firebasestorage.app"
    ASSETS_DIR = Path(__file__).parent / "assets"
    
    if len(sys.argv) > 1 and sys.argv[1] == "--list":
        print(f"Listing files in gs://{BUCKET}/videos/")
        os.system(f'gcloud storage ls -r "gs://{BUCKET}/videos/"')
        return

    check_gcloud()

    # Find all final videos
    final_videos = list(ASSETS_DIR.glob("*_final.mp4"))
    
    if not final_videos:
        print("No final videos found in nlm/assets/")
        print("Run process_videos.py first to generate final videos.")
        return

    print(f"Found {len(final_videos)} videos to upload")
    
    success = 0
    failed = 0
    
    for video_path in final_videos:
        # Extract lecture code from filename: lecture_1_1_1_final.mp4 -> 1_1_1
        lecture_code = video_path.stem.replace("lecture_", "").replace("_final", "")
        remote_path = f"videos/course_1/lecture_{lecture_code}.mp4"
        
        if upload_video(str(video_path), remote_path, BUCKET):
            success += 1
        else:
            failed += 1

    print(f"\nUpload complete: {success} success, {failed} failed")
    
    if success > 0:
        print(f"\nFirebase Storage URLs:")
        print(f"  gs://{BUCKET}/videos/course_1/lecture_*.mp4")
        print(f"\nPublic download URLs:")
        print(f"  https://firebasestorage.googleapis.com/v0/b/{BUCKET}/o/videos%2Fcourse_1%2Flecture_*.mp4?alt=media")

if __name__ == "__main__":
    main()
