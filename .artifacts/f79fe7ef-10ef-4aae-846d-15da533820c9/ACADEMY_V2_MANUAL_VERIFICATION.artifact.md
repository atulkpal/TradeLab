# Authoritative Manual Verification Protocol: Academy v2 (Epic 22)

This document provides a step-by-step checklist for testers to verify the functional integrity of the **Academy v2 (Varsity-Style Curriculum)** features.

## 1. Curriculum Loading & Course Grouping
- [ ] **Test Case 1.1: Course Headers**
    - **Action**: Open Academy -> Lessons tab.
    - **Expected Result**: Modules are grouped under course header cards (e.g., "Stock Market Basics", "Technical Analysis"). Each header shows an emoji icon, tier badge (BEGINNER/INTERMEDIATE/ADVANCED), tagline, and per-course progress (e.g., "Progress: 2 of 12 chapters").
- [ ] **Test Case 1.2: Tier Badges & Rewards**
    - **Action**: Compare reward chips on chapter cards across courses.
    - **Expected Result**: BEGINNER chapters show +₹500, INTERMEDIATE +₹750, ADVANCED +₹1,000 (Sprint 22.9 anti-inflation retune; full academy ≈ ₹53,500).
- [ ] **Test Case 1.3: Knowledge Check Label**
    - **Action**: Inspect any chapter card.
    - **Expected Result**: Card displays "📝 Knowledge Check • N Questions" where N matches the chapter's question count.

## 2. Multi-Question Quiz Flow
- [ ] **Test Case 2.1: Question Stepper**
    - **Action**: Open any chapter -> tap "Start Knowledge Check 📝".
    - **Expected Result**: "Question X of Y" header with a progress bar. Answering a question and tapping "Check Answer" reveals CORRECT/INCORRECT feedback plus an explanation.
- [ ] **Test Case 2.2: Next Question Navigation**
    - **Action**: On a question, tap "Next Question →".
    - **Expected Result**: Advances to the next question without resetting prior answers.
- [ ] **Test Case 2.3: Score Summary**
    - **Action**: Answer the final question, tap "See My Results 🎯".
    - **Expected Result**: Score screen shows "Your Score: X / Y", a pass/fail verdict, and a per-question recap with green/red icons.
- [ ] **Test Case 2.4: Risk Disclosure Footnote**
    - **Action**: Open any chapter in Futures & Options (Course 4) and Markets & Taxation (Course 6).
    - **Expected Result**: A muted amber one-liner (e.g., "Risk Disclosure: Derivatives are high-risk and you can lose more than your margin…") appears below the chapter title inside the quiz dialog. Chapters in Courses 1–3 and 5 show no such line.

## 3. Pass / Fail & Rewards
- [ ] **Test Case 3.1: Passing Threshold (≥60%)**
    - **Action**: Answer at least 60% of questions correctly.
    - **Expected Result**: "Chapter passed!" message with the standard claim button and the optional "Double Reward" (ad/premium) button.
- [ ] **Test Case 3.2: Reward Claim**
    - **Action**: Tap "Claim Standard ₹X Capital".
    - **Expected Result**: Virtual capital increases by the chapter's reward; chapter card flips to "Earned!"; ledger logs "Mission Reward: Level {id}".
- [ ] **Test Case 3.3: Reward Idempotency**
    - **Action**: Re-open a completed chapter and pass again.
    - **Expected Result**: "Close Quiz" is shown (no double credit); cash does not increase a second time.
- [ ] **Test Case 3.4: Failure & Retry**
    - **Action**: Answer fewer than 60% correctly.
    - **Expected Result**: Score screen shows a failure message with "Review Lectures" and "Try Again" buttons; no reward is granted.

## 4. Progress Persistence & Integration
- [ ] **Test Case 4.1: Persistence Across Restarts**
    - **Action**: Complete a chapter, then fully close and reopen the app.
    - **Expected Result**: "Earned!" state and per-course progress persist.
- [ ] **Test Case 4.2: F&O Unlock Gate (v2-aware)**
    - **Action**: Complete the first three beginner-curriculum chapters (Chapter 1.1–1.3 → `completedLevels` contains `101,102,103`), then open the F&O desk.
    - **Expected Result**: F&O unlocks — the desk renders (RELIANCE UNDERLYING, option chain) instead of the "Academic Gate Active" card. The gate accepts v2 chapter ids (101/102/103); legacy ids (1/2/3) are still honored for backward compatibility. The gate checklist displays the three beginner chapter titles.
- [ ] **Test Case 4.3: Certificate Gate**
    - **Action**: Open Academy -> Missions.
    - **Expected Result**: Certificate remains locked until all lessons are completed; progress count reflects the new chapter total.
- [ ] **Test Case 4.4: Legacy Fallback (pre-v2 content)**
    - **Action**: With only `academy_data.json` present (v2 missing), open Academy.
    - **Expected Result**: All 8 legacy modules render under a single "Stock Market Basics" course and continue to work as before (single-question knowledge check, claimable rewards).

## 5. Content Integrity (Schema Compliance)
- [ ] **Test Case 5.1: Content Validation (automated)**
    - **Action**: Run `.\gradlew testDebugUnitTest`.
    - **Expected Result**: `AcademyScoringTest` and `AcademyContentFileTest` pass; the merged `academy_data_v2.json` passes `AcademyScoring.validateCourses` (unique/contiguous IDs, 3–5 questions/chapter, 3–4 lectures/chapter, valid correctIndex, tier-matched rewards, and a `riskDisclosure` line on every Course 4 & 6 chapter).
- [ ] **Test Case 5.2: Course Inventory**
    - **Action**: Run `AcademyContentFileTest` on the shipped asset.
    - **Expected Result**: Exactly 6 courses in order 1–6 with chapter counts 12/12/10/12/12/10 (68 total); `riskDisclosure` present only on Courses 4 & 6.

## 6. Accordion Course Deck
- [ ] **Test Case 6.1: Collapsed by Default**
    - **Action**: Open Academy -> Lessons tab.
    - **Expected Result**: All course header cards are collapsed on first open; chapter cards are hidden until a header is tapped.
- [ ] **Test Case 6.2: Expand & Collapse**
    - **Action**: Tap a course header.
    - **Expected Result**: Its chapter cards slide in with a vertical expand + fade animation; the chevron flips 180°. Tapping the same header again slides the chapters out and flips the chevron back.
- [ ] **Test Case 6.3: Single-Open Accordion**
    - **Action**: Expand course A, then tap course B's header.
    - **Expected Result**: Course A's chapters collapse automatically while course B's expand — at most one course is open at a time.
- [ ] **Test Case 6.4: Progressive Course Unlock**
    - **Action**: With a fresh profile (no completions), inspect Courses 2–6.
    - **Expected Result**: Only Course 1 shows a reward/assessment entry; Courses 2–6 headers are dimmed, show a 🔒 lock icon and "Preview — complete the previous course to earn rewards". Complete every chapter of Course 1 and the header unlocks (progress replaces the preview line).
- [ ] **Test Case 6.5: Locked Course Preview (read, don't earn)**
    - **Action**: Tap a locked course header.
    - **Expected Result**: The course expands and its chapters slide in, but each chapter card is dimmed with a "🔒 Preview" chip instead of a reward amount. Tap a locked chapter — its lectures open and are fully readable, but the "Start Knowledge Check 📝" / "Skip Lectures" buttons are replaced by a "Assessment Locked" card explaining which previous course must be completed first. No reward can be claimed.
- [ ] **Test Case 6.6: Course Completion Sparkle**
    - **Action**: Complete all chapters of any course.
    - **Expected Result**: The header's chevron is replaced by a "✓ Course complete" badge with a one-shot sparkle burst.
- [ ] **Test Case 6.7: Long Course Titles & Tier Badge**
    - **Action**: Open the Academy with the "Risk Management & Trading Psychology" (INTERMEDIATE) course visible.
    - **Expected Result**: The course title sits on its own line (ellipsized if too long) and the INTERMEDIATE tier badge renders on its own line below the title — it is never squeezed or wrapped vertically.

## 7. Skip-Lecture Shortcut
- [ ] **Test Case 7.1: Skip Lectures Button**
    - **Action**: Open any chapter with lectures (lecture mode) and scroll to the bottom.
    - **Expected Result**: A secondary outlined "Skip Lectures → Take Assessment 📝" button appears alongside the primary "Start Knowledge Check 📝" button.
- [ ] **Test Case 7.2: Skip Jumps to Quiz**
    - **Action**: Tap "Skip Lectures → Take Assessment 📝".
    - **Expected Result**: The lecture content is skipped and the multi-question knowledge check starts immediately (Question 1 of N); rewards remain claimable on ≥60% pass.

## 8. Premium Motion & Screenshots
- [ ] **Test Case 8.1: Entrance Cascade & Glow**
    - **Action**: Open Academy -> Lessons with multiple courses.
    - **Expected Result**: Course headers cascade in with a staggered fade/slide; the expanded course's header shows a pulsing neon border; the per-course progress bar has a soft shimmer sweep.
- [ ] **Test Case 8.2: Automated Deck Screenshots**
    - **Action**: Run `.\gradlew.bat :app:recordRoborazziDebug --tests "*AcademyScreenshotTest"`.
    - **Expected Result**: `app/src/test/screenshots/academy_deck_collapsed.png` and `academy_deck_expanded.png` are regenerated showing the collapsed and expanded deck states.
- [ ] **Test Case 8.3: Accordion Behavior (automated)**
    - **Action**: Run `.\gradlew testDebugUnitTest`.
    - **Expected Result**: `AcademyAccordionTest` (8 cases: headers render, hidden chapters, expand/collapse, single-open, locked no-expand, progressive unlock, partial-lock, chapter-open callback) passes with `AcademyScoringTest` (27 cases incl. mission evaluation).

## 9. Missions & Claimable Rewards (Sprint 22.8)
- [ ] **Test Case 9.1: Mission Catalog & Progress**
    - **Action**: Open Academy -> Missions with a fresh profile.
    - **Expected Result**: 7 missions listed with descriptions and rewards; missions 1 (first trade), 2 (3 knowledge checks, "0/3"), 4 (Course Crusher, "0/12"), and 7 (certificate, "0/68") show progress bars and counts.
- [ ] **Test Case 9.2: Completion Flipping**
    - **Action**: Execute a first buy, pass 3+ knowledge checks, and complete the profiler.
    - **Expected Result**: Missions 1–3 flip to completed; the "Claim" button appears on each with the reward amount.
- [ ] **Test Case 9.3: Claim Reward**
    - **Action**: Tap "Claim" on a completed mission.
    - **Expected Result**: Confetti + toast confirm the reward; wallet cash and starting cash increase by the reward amount; the row shows "Claimed ✓"; the same reward amount appears as a CREDIT in the Ledger.
- [ ] **Test Case 9.4: Claim Idempotency**
    - **Action**: Force-quit and relaunch the app, then re-open Missions.
    - **Expected Result**: The claimed mission still shows "Claimed ✓" with no Claim button; the wallet has NOT increased a second time (claimed state persisted via `claimedMissions`).
- [ ] **Test Case 9.5: Course-Aware Missions**
    - **Action**: Complete all chapters of the Stock Market Basics course (course 1) and then complete the F&O course (course 4).
    - **Expected Result**: Mission 4 "Course Crusher" completes (12/12); Mission 6 "Derivatives Debut" completes (12/12); Mission 5 "Beyond Beginner" completes once a second course is unlocked.
- [ ] **Test Case 9.6: Certificate Total Uses Live Chapter Count**
    - **Action**: Inspect the certificate card in the Missions tab.
    - **Expected Result**: Locked progress reads "Locked (X/68)" reflecting the live 68-chapter curriculum, not the legacy module count.

## 10. Vector Icon Migration (Decorative Emoji → Drawables)
- [ ] **Test Case 10.1: Quiz Dialog Icons**
    - **Action**: Open any chapter -> "Start Knowledge Check". Answer correctly, tap "Check Answer", advance, and finish with a pass.
    - **Expected Result**: "CORRECT!" shows a green celebrate icon, "INCORRECT" a rose wrong icon, the results CTA shows a target icon, "Chapter passed!" a celebrate icon, and "Double Reward" a pro/ad icon — no emoji in the dialog body.
- [ ] **Test Case 10.2: F&O Desk Icons**
    - **Action**: Unlock F&O, open the desk.
    - **Expected Result**: The TOKENS badge shows a coin icon, the EXECUTE button a rocket icon, the MIS warning an amber warning icon, and the bias labels trend-up/trend-down/balance icons — no emoji.
- [ ] **Test Case 10.3: Profile Footer**
    - **Action**: Open Profile, scroll to the footer.
    - **Expected Result**: The "Made by Ashwat AI" line shows a small rose heart icon; directly below it a version line reads "Trade Lab v1.5.0 (5)".
- [ ] **Test Case 10.4: Profile Level Badge**
    - **Action**: Complete 5+ chapters and open Profile.
    - **Expected Result**: "Level 5: Master Risk Manager" shows a trophy icon (no emoji).
- [ ] **Test Case 10.5: Icon Asset Inventory (automated)**
    - **Action**: Run `.\gradlew testDebugUnitTest`.
    - **Expected Result**: `AcademyScoringTest` covers `courseIcon` (6 unique), `biasIcon` (3 distinct threshold drawables), and `fnoAcademicUnlocked` (v2 + legacy gate); all 27 VectorDrawables referenced by `courseIcon`/`biasIcon` resolve to existing `drawable` resources.
