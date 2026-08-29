#!/usr/bin/env python3
"""
Upload AAB to Google Play Console using Service Account
"""
import sys
import os
from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

# Configuration
SERVICE_ACCOUNT_FILE = 'play-account.json'
PACKAGE_NAME = 'com.ashwathai.tradelab'
AAB_PATH = 'releases/release-aab-2.2.0-13.aab'
TRACK = 'internal'  # internal, alpha, beta, production

SCOPES = ['https://www.googleapis.com/auth/androidpublisher']

def upload_aab():
    # Authenticate
    credentials = service_account.Credentials.from_service_account_file(
        SERVICE_ACCOUNT_FILE, scopes=SCOPES)
    
    # Build the service
    service = build('androidpublisher', 'v3', credentials=credentials)
    
    # Create an edit
    edit = service.edits().insert(packageName=PACKAGE_NAME, body={}).execute()
    edit_id = edit['id']
    print(f"Created edit: {edit_id}")
    
    # Upload the AAB
    print(f"Uploading AAB: {AAB_PATH}")
    media = MediaFileUpload(AAB_PATH, mimetype='application/octet-stream', resumable=True)
    bundle = service.edits().bundles().upload(
        packageName=PACKAGE_NAME,
        editId=edit_id,
        media_body=media
    ).execute()
    version_code = bundle['versionCode']
    print(f"Uploaded AAB version code: {version_code}")
    
    # Assign to track
    track_response = service.edits().tracks().update(
        packageName=PACKAGE_NAME,
        editId=edit_id,
        track=TRACK,
        body={
            'releases': [{
                'versionCodes': [version_code],
                'status': 'completed',
                'releaseNotes': [{
                    'language': 'en-US',
                    'text': 'Profile completion flow with login method tracking, field locking based on login method, email opt-in defaults checked, removed top-right skip button and avatar pencil icon.'
                }]
            }]
        }
    ).execute()
    print(f"Assigned to {TRACK} track: {track_response}")
    
    # Commit the edit
    service.edits().commit(packageName=PACKAGE_NAME, editId=edit_id).execute()
    print("Edit committed successfully!")
    
    return version_code

if __name__ == '__main__':
    try:
        vc = upload_aab()
        print(f"\n✅ Successfully uploaded version {vc} to {TRACK} track!")
        sys.exit(0)
    except Exception as e:
        print(f"\n❌ Upload failed: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)