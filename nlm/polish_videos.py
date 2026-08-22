#!/usr/bin/env python3
"""
Video Processing Pipeline for TradeLab Academy - Vertical Videos
Processes all 720x1280 vertical lecture videos:
  1. Trim last 3 seconds (Gemini logo)
  2. Branding bar at bottom (covers NotebookLM watermark)
  3. Persistent random-position watermark (logo, shifts every 5s)
  4. Concat intro + processed + outro
"""

import subprocess
import sys
from pathlib import Path

ASSETS_DIR = Path(__file__).parent / "assets"
OUTPUT_DIR = Path(__file__).parent / "assets" / "out"
FONT = "segoeui.ttf"
FONT_BOLD = "segoeuib.ttf"
LOGO = str(Path(__file__).parent / "assets" / "app_logo_premium.png")

W = 720
H = 1280
BAR_H = 80
BAR_Y = H - BAR_H
BAR_COLOR = "0x1A1A2E"
TRIM_END = 3

POSITIONS = [
    (30,  30),
    (630, 30),
    (30,  1100),
    (630, 1100),
    (280, 30),
    (280, 550),
]
WATERMARK_SIZE = 60
WATERMARK_OPACITY = 0.35


def run(cmd, desc=""):
    print(f"  -> {desc}" if desc else f"  -> {' '.join(cmd[:4])}...")
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"    FAILED: {result.stderr[:500]}")
        return False
    return True


def get_video_info(path):
    cmd = [
        "ffprobe", "-v", "quiet",
        "-select_streams", "v:0",
        "-show_entries", "stream=width,height,duration",
        "-of", "csv=p=0",
        str(path),
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    parts = result.stdout.strip().split(",")
    return int(parts[0]), int(parts[1]), float(parts[2])


def build_watermark_expr():
    n = len(POSITIONS)

    def nested_if_x(i):
        if i == n - 1:
            return str(POSITIONS[i][0])
        return f"if(eq(mod(floor(t/5),{n}),{i}),{POSITIONS[i][0]},{nested_if_x(i + 1)})"

    def nested_if_y(i):
        if i == n - 1:
            return str(POSITIONS[i][1])
        return f"if(eq(mod(floor(t/5),{n}),{i}),{POSITIONS[i][1]},{nested_if_y(i + 1)})"

    return nested_if_x(0), nested_if_y(0)


def generate_intro():
    out = OUTPUT_DIR / "intro_new.mp4"
    if out.exists():
        print(f"  Intro exists: {out.name}")
        return out

    print("Generating intro...")
    duration = 3

    fc = (
        f"color=c=0x1A1A2E:s={W}x{H}:d={duration}:r=30[bg];"
        f"[0:v]scale=200:200,format=rgba,"
        f"fade=t=in:st=0:d=1.5:alpha=1,"
        f"fade=t=out:st=2.5:d=0.5:alpha=1[logo];"
        f"[bg][logo]overlay=(W-200)/2:(H-200)/2-60:shortest=1[wl];"
        f"[wl]drawtext=text='TradeLab Academy':"
        f"fontfile={FONT_BOLD}:fontsize=36:fontcolor=white:"
        f"x=(w-text_w)/2:y=(H/2)+60:alpha='if(lt(t\\,1)\\,0\\,if(lt(t\\,2)\\,(t-1)\\,1))'[out]"
    )

    cmd = [
        "ffmpeg", "-y",
        "-loop", "1", "-i", str(LOGO),
        "-f", "lavfi", "-i", "anullsrc=r=44100:cl=mono",
        "-filter_complex", fc,
        "-map", "[out]", "-map", "1:a",
        "-c:v", "libx264", "-crf", "18", "-preset", "fast",
        "-c:a", "aac", "-b:a", "64k",
        "-pix_fmt", "yuv420p",
        "-t", str(duration),
        str(out),
    ]
    if run(cmd, "Generating intro"):
        print(f"  Intro: {out.name}")
    return out


def generate_outro():
    out = OUTPUT_DIR / "outro_new.mp4"
    if out.exists():
        print(f"  Outro exists: {out.name}")
        return out

    print("Generating outro...")
    duration = 3

    fc = (
        f"color=c=0x1A1A2E:s={W}x{H}:d={duration}:r=30[bg];"
        f"[0:v]scale=120:120,format=rgba,"
        f"fade=t=in:st=0:d=1:alpha=1[logo];"
        f"[bg][logo]overlay=(W-120)/2:(H/2)-140:shortest=1[wl];"
        f"[wl]drawtext=text='Learn on TradeLab':"
        f"fontfile={FONT_BOLD}:fontsize=30:fontcolor=white:"
        f"x=(w-text_w)/2:y=(H/2)+20:"
        f"alpha='if(lt(t\\,0.5)\\,0\\,if(lt(t\\,1.5)\\,(t-0.5)\\,1))',"
        f"drawtext=text='No Risk. Real Learning.':"
        f"fontfile={FONT}:fontsize=20:fontcolor=0x8B5CF6:"
        f"x=(w-text_w)/2:y=(H/2)+70:"
        f"alpha='if(lt(t\\,1)\\,0\\,if(lt(t\\,2)\\,(t-1)\\,1))'[out]"
    )

    cmd = [
        "ffmpeg", "-y",
        "-loop", "1", "-i", str(LOGO),
        "-f", "lavfi", "-i", "anullsrc=r=44100:cl=mono",
        "-filter_complex", fc,
        "-map", "[out]", "-map", "1:a",
        "-c:v", "libx264", "-crf", "18", "-preset", "fast",
        "-c:a", "aac", "-b:a", "64k",
        "-pix_fmt", "yuv420p",
        "-t", str(duration),
        str(out),
    ]
    if run(cmd, "Generating outro"):
        print(f"  Outro: {out.name}")
    return out


def process_lecture(input_path, output_path):
    w, h, dur = get_video_info(input_path)
    trimmed_end = dur - TRIM_END

    if trimmed_end <= 0:
        print(f"  Skip {input_path.name}: too short ({dur:.1f}s)")
        return False

    wm_x, wm_y = build_watermark_expr()
    wm_x_esc = wm_x.replace(",", "\\,")
    wm_y_esc = wm_y.replace(",", "\\,")

    filter_complex = (
        f"[0:v]trim=0:end={trimmed_end},setpts=PTS-STARTPTS,"
        f"drawbox=y={BAR_Y}:w={W}:h={BAR_H}:color={BAR_COLOR}:t=fill,"
        f"drawtext=text='TradeLab':"
        f"fontfile={FONT_BOLD}:fontsize=22:fontcolor=white:"
        f"x=30:y={BAR_Y + 28},"
        f"drawtext=text='Academy':"
        f"fontfile={FONT_BOLD}:fontsize=22:fontcolor=0x8B5CF6:"
        f"x={W - 130}:y={BAR_Y + 28},"
        f"drawtext=text='TradeLab Academy':"
        f"fontfile={FONT}:fontsize=14:fontcolor=0x888888:"
        f"x=(w-text_w)/2:y={BAR_Y + 55}"
        f"[trimmed];"
        f"[0:a]atrim=0:end={trimmed_end},asetpts=PTS-STARTPTS[aout];"
        f"[1:v]scale={WATERMARK_SIZE}:{WATERMARK_SIZE},"
        f"format=rgba,"
        f"colorchannelmixer=aa={WATERMARK_OPACITY}"
        f"[logo];"
        f"[trimmed][logo]overlay="
        f"x='{wm_x_esc}':"
        f"y='{wm_y_esc}':"
        f"eof_action=pass[out]"
    )

    cmd = [
        "ffmpeg", "-y",
        "-i", str(input_path),
        "-loop", "1", "-i", str(LOGO),
        "-filter_complex", filter_complex,
        "-map", "[out]", "-map", "[aout]",
        "-c:v", "libx264", "-crf", "23", "-preset", "medium",
        "-c:a", "aac", "-b:a", "64k",
        "-pix_fmt", "yuv420p",
        str(output_path),
    ]
    return run(cmd, f"Processing {input_path.name}")


def concat_segments(lecture_path, intro_path, outro_path, final_path):
    concat_list = OUTPUT_DIR / "_concat.txt"
    # Use forward slashes for ffmpeg concat compatibility
    intro_fwd = str(intro_path.resolve()).replace("\\", "/")
    lecture_fwd = str(lecture_path.resolve()).replace("\\", "/")
    outro_fwd = str(outro_path.resolve()).replace("\\", "/")
    concat_list.write_text(
        f"file '{intro_fwd}'\n"
        f"file '{lecture_fwd}'\n"
        f"file '{outro_fwd}'\n",
        encoding="utf-8",
    )

    cmd = [
        "ffmpeg", "-y",
        "-f", "concat", "-safe", "0",
        "-i", str(concat_list),
        "-c", "copy",
        "-movflags", "+faststart",
        str(final_path),
    ]
    return run(cmd, f"Concat -> {final_path.name}")


def main():
    OUTPUT_DIR.mkdir(exist_ok=True)

    print("=" * 60)
    print("STEP 1: Generate Intro & Outro")
    print("=" * 60)
    intro = generate_intro()
    outro = generate_outro()

    if not intro.exists() or not outro.exists():
        print("Intro/Outro generation failed. Aborting.")
        sys.exit(1)

    print("\n" + "=" * 60)
    print("STEP 2: Find Vertical Lectures")
    print("=" * 60)

    lectures = []
    for f in sorted(ASSETS_DIR.glob("lecture_*.mp4")):
        if "_final" in f.name or "_processed" in f.name:
            continue
        if f.name.endswith(".tmp") or ".tmp." in f.name:
            continue

        w, h, dur = get_video_info(f)
        if w == 720 and h == 1280 and dur > TRIM_END + 1:
            lectures.append((f, w, h, dur))
            print(f"  OK {f.name} ({w}x{h}, {dur:.1f}s)")
        else:
            print(f"  SKIP {f.name} ({w}x{h}, {dur:.1f}s) - not vertical or too short")

    print(f"\nFound {len(lectures)} vertical lectures to process.")

    if not lectures:
        print("No lectures to process. Exiting.")
        sys.exit(0)

    print("\n" + "=" * 60)
    print("STEP 3: Process Lectures")
    print("=" * 60)

    processed = []
    for i, (lecture_file, w, h, dur) in enumerate(lectures, 1):
        print(f"\n[{i}/{len(lectures)}] {lecture_file.name}")

        processed_name = lecture_file.stem + "_processed.mp4"
        processed_path = OUTPUT_DIR / processed_name
        success = process_lecture(lecture_file, processed_path)

        if not success or not processed_path.exists():
            print(f"  Failed to process {lecture_file.name}")
            continue

        final_name = lecture_file.stem + "_final.mp4"
        final_path = OUTPUT_DIR / final_name
        concat_success = concat_segments(processed_path, intro, outro, final_path)

        if concat_success and final_path.exists():
            size_mb = final_path.stat().st_size / (1024 * 1024)
            print(f"  Final: {final_path.name} ({size_mb:.1f}MB)")
            processed.append(final_path)
        else:
            print(f"  Concat failed for {lecture_file.name}")

    print("\n" + "=" * 60)
    print("COMPLETE")
    print("=" * 60)
    print(f"Processed: {len(processed)}/{len(lectures)} lectures")
    print(f"Output: {OUTPUT_DIR.resolve()}")

    concat_list = OUTPUT_DIR / "_concat.txt"
    if concat_list.exists():
        concat_list.unlink()


if __name__ == "__main__":
    main()
