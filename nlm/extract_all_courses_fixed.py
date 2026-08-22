#!/usr/bin/env python3
"""
Extract ALL course lecture .md files from academy_data_v2.json
Creates: nlm/lectures/course_{1..6}/lecture_{code}.md + manifest.json
"""

import json
import re
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent
DATA_FILE = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "academy_data_v2.json"
LECTURES_DIR = SCRIPT_DIR / "lectures"

def slugify(text: str) -> str:
    text = text.lower()
    text = re.sub(r'[^a-z0-9\s]', '', text)
    text = re.sub(r'\s+', '_', text)
    return text

def extract_all_courses():
    print(f"Loading academy data from {DATA_FILE}...")
    
    with open(DATA_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    total_lectures = 0
    
    for course in data["courses"]:
        course_id = course["id"]
        course_title = course["title"]
        course_dir = LECTURES_DIR / f"course_{course_id}"
        course_dir.mkdir(parents=True, exist_ok=True)
        
        print(f"\nProcessing Course {course_id}: {course_title}")
        
        manifest_lectures = []
        lecture_count = 0
        
        for chapter in course["chapters"]:
            chapter_id = chapter["id"]
            chapter_title = chapter["title"]
            
            for lecture_idx, lecture in enumerate(chapter["lectures"]):
                lecture_count += 1
                
                # Extract lecture code from title
                title = lecture["title"]
                code = title.split(":")[0].replace("Lecture ", "").strip()
                
                md_content = f"# {title}\n\n"
                md_content += f"**Course:** {course_id} | **Chapter:** {chapter_title} | **Lecture:** {code}\n\n"
                md_content += f"**Concept:** {chapter.get('concept', '')}\n\n"
                md_content += f"---\n\n"
                md_content += lecture.get("content", "")
                
                # Filename: lecture_1_1_1.md
                filename = f"lecture_{code.replace('.', '_')}.md"
                filepath = course_dir / filename
                
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(md_content)
                
                manifest_lectures.append({
                    "filename": filename,
                    "path": f"lectures/course_{course_id}/{filename}",
                    "lecture_code": code,
                    "title": title,
                    "chapter_id": chapter_id,
                    "chapter_title": chapter_title
                })
                
                print(f"  Created: {filename}")
        
        # Write manifest for this course
        manifest = {
            "course_id": course_id,
            "course_title": course_title,
            "course_tier": course.get("tier", "BEGINNER"),
            "total_lectures": lecture_count,
            "lectures": manifest_lectures
        }
        
        manifest_path = course_dir / "manifest.json"
        with open(manifest_path, 'w', encoding='utf-8') as f:
            json.dump(manifest, f, indent=2, ensure_ascii=False)
        
        print(f"  Course {course_id} manifest: {lecture_count} lectures")
        total_lectures += lecture_count
    
    print(f"\nTotal lectures extracted: {total_lectures}")
    print(f"Output directory: {LECTURES_DIR}")

if __name__ == "__main__":
    extract_all_courses()