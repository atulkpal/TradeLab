# Authoritative Agent Context & Handover (`AGENTS.md`)

Welcome, Agent! This document is your single source of truth for understanding the **Trade Lab** project from Ashwath AI. Reading this file gives you full context of the project architecture, features, current progress, and next steps without needing to sift through thousands of lines of code.

---

## 1. Project High-Level Overview
**Trade Lab** is an Android-first, realistic, offline-first paper trading simulator designed for beginners and retail practitioners (16–35+ years old). It aims to teach realistic position sizing, risk management, and market mechanics using virtual budgets denominated in Indian Rupees (₹) and US Dollars ($).

*   **Key Philosophy:** Prevent reckless "fantasy high-roller" behaviors by starting with realistic budgets (e.g., ₹10,000) and enforcing disciplined position sizing. Users "earn" more virtual capital by completing educational quizzes ("Learn-to-Earn").
*   **Company & Brand:** Developed by **Ashwath AI** (Package ID: `com.ashwathai.TradeLab`).
*   **Aesthetic Theme:** Premium high-contrast **Sophisticated Dark** theme with neon highlights and custom live vector charts.

---

## 2. Directory Structure & Key Code Entry Points

To avoid searching blindly, here is exactly where the core logic resides:

```
├── AGENTS.md                                # This authoritative instruction file
├── docs/                                    # Documentation directory
│   ├── PRODUCT_STRATEGY_AND_PRD.md          # Original product requirements document
│   ├── VISION_INTERVIEW_LOGS.md             # Qualitative interview logs & strategy notes
│   ├── architecture.md                      # Comprehensive multi-platform architecture spec
│   └── epics_and_sprints.md                 # Product roadmap, epics, sprints, and tasks
├── nlm/                                     # NLM Video Generation Pipeline
│   ├── docs/                                # Pipeline documentation
│   │   ├── PIPELINE.md                      # Full pipeline walkthrough
│   │   ├── STATUS_SHEET.md                  # Current progress
│   │   ├── SCRIPT_INVENTORY.md              # Script reference
│   │   ├── QUOTA_MANAGEMENT.md              # Rate limits
│   │   └── TROUBLESHOOTING.md               # Common issues
│   ├── lectures/course_{1..6}/              # 204 lecture .md files
│   ├── *.py                                 # Pipeline scripts
│   └── pending_allocation.json              # Lecture → Account mapping
├── app/                                     # Android Application Module
│   ├── src/main/java/com/ashwathai/tradelab/ # Core Kotlin code
│   │   ├── MainActivity.kt                  # Entry point (holds the Jetpack Compose navigation & views)
│   │   ├── data/                            # Local database and persistence layer
│   │   │   ├── AppDatabase.kt               # Room database definition
│   │   │   ├── Daos.kt                      # Room DAOs (User, Holdings, Transactions, Watchlist, StockPrices)
│   │   │   ├── Entities.kt                  # SQLite table definitions
│   │   │   └── TradingRepository.kt         # Data-access repository managing trade executions
│   │   └── ui/                              # User Interface
│   │       ├── TradingViewModel.kt          # UI state machine and business logic
│   │       ├── common/                      # Reusable UI components
│   │       │   ├── VideoPlayerView.kt       # WebView-based video player
│   │       │   ├── VideoCacheManager.kt     # Download-once, play-locally cache
│   │       │   └── BannerAdView.kt          # AdMob banner integration
│   │       ├── di/                          # Hilt Dependency Injection Modules
│   │       └── theme/                       # Color, Type, and Theme configurations
│   └── src/test/                            # Local JVM & Robolectric Tests
│       └── java/com/ashwathai/tradelab/      # Tests verifying UI states and robustness
```

---

## 3. What Has Been Done (Phase 1 & 2 Foundations)
The **Architectural Modernization** has been successfully verified:
1.  **Local Database Schema (Room):** Structured local tables initialized with mock Indian equity data (e.g., RELIANCE, TCS, INFOSYS, HDFCBANK). Version 9 supports persistent UI preferences.
2.  **Hilt Dependency Injection:** Fully decoupled the app using Hilt. All components use constructor injection, resolving static singleton bottlenecks.
3.  **Steered Anchored Simulation:** Implemented a realistic price engine where stocks "wiggle" locally but gravitate (5% vector) toward real-world Yahoo Finance anchors.
4.  **Indian Market Realism:** Simulation respects NSE/BSE holidays and strictly stops wiggling at 3:30 PM IST (MCX 11:30 PM).
5.  **Ultra-Dense UI & Swipe Navigation:**
    *   `HorizontalPager` integration allowing fluid swipes between tabs.
    *   Collapsible search lens and "vanishing" tickers to maximize data density.
6.  **Psychological Profiler:** A 60-second questionnaire on the Profile screen that aligns virtual capital sizes to real-world budgets to build realistic trading habits.
7.  **Production Auth & Diagnostics:** Firebase Auth (Google/Phone) with robust Logcat-based diagnostics for production environment configuration.
8.  **Viral Sharing & Professional Mechanics (Epic 16):**
    *   Integrated dynamic performance-based "GenZ" sharing hooks for social media snapshots.
    *   Added institutional-grade product toggles (CNC/MIS for Equity, NRML/MIS for F&O) with educational tooltips.
    *   Standardized T+1 settlement visibility and square-off logic for derivatives.
9.  **Track B — Institutional Order Management (Epic 18):**
    *   Implemented 5x leverage for Intraday (MIS) trades.
    *   Built an **Auto-Liquidation Engine** that triggers a "Margin Call" square-off if equity falls below 50% maintenance.
    *   Implemented **Trailing Stop-Loss** logic that dynamically adjusts trigger prices to lock in profits.
    *   Integrated multi-leg **Bracket Orders (OCO)** into the primary Trade Desk.
10. **Track C — Social Discipline & Precision Ledger (Epic 19):**
    *   Implemented a proprietary **Discipline Score (0-100)** algorithm quantifying retail maturity.
    *   Added dynamic **Discipline Badges** (e.g., "Sizing Master", "Patience King") to the user profile.
    *   Upgraded the **Global Leaderboard** with multi-sort (Wealth vs. Maturity) and 100-user capacity.
    *   Integrated a high-precision **Tradebook (Ledger)** tracking every virtual penny to 4 decimal places.

11. **Varsity Academy (Epic 22):**
    *   Transitioned from flat modules to a **6-Course, 68-Chapter** curriculum.
    *   Implemented a multi-question **Knowledge Check Engine** with $\ge 60\%$ pass threshold.
    *   Added a **Mission Reward Engine** with idempotent virtual capital claims and ledger logging.
12. **Multi-Format Monetization (Epic 23):**
    *   Diversified from "Watch-to-Earn" to include **App Open Ads** (on resume) and **Native Ads** (blended in lists).
    *   Customized native ad styling with **BrandViolet** highlights to match the premium theme.
13. **Institutional Polish (Epic 21):**
    *   Implemented full **GTT Persistence** and **Quick Exit (Square Off)**.
    *   Migrated 100% of UI icons to **VectorDrawables** (removing emojis).
14. **Hyper-Personalization & Focus Suite (Epic 24):**
    *   Transitioned to a **5-mode Theme Engine** (Serious, Vibrant, Terminal, Arcade, Light).
    *   Implemented **Stealth Mode** with dynamic privacy blurring for portfolio values.
    *   Integrated **Zen Mode** to hide distracting news and movers for better focus.
    *   Introduced **Monospace Typography** and **CRT Scanline** effects for thematic immersion.
15. **Deep Stability Recovery (v1.8.1):**
    *   Migrated to **Moshi Codegen** (reflection-free parsing) to resolve R8/Obfuscation crashes on release builds.
    *   Hardened **ProGuard rules** for the `:shared` module and data packages.
    *   Implemented an **In-App Diagnostic Suite** (Crash Logger + Secret Viewer via long-press on version) for field troubleshooting.
16. **v2.0.2 — Banner Ads, Interstitial Pacing & Watchlist Fixes:**
    *   `LevelPlayBanner` composable wired to 10 screens (all gated `!isPremium`).
    *   `USE_TEST_ADS = BuildConfig.DEBUG` — debug builds use test key `25b63cf85`.
    *   Paced chapter-complete interstitial (every 3rd reward, 3-min gap, Pro skips).
    *   Post-video bonus reward card in `LectureScreen`.
    *   In-App Update API (`app-update-ktx:2.1.0`).
    *   BuySellBottomSheet MIS dialog fix: `LocalContext` moved to function top scope, drag-to-dismiss on handle bar only, test key corrected.

---

## 4. Current Work & Next Milestones

We have successfully completed **Phase 1 (Foundations)**, the **Social Arena**, the **Varsity Academy Expansion**, the **Hyper-Personalization Suite**, **Unity LevelPlay monetization**, and **Profile Completion & Login Method Tracking**. The app is now at **v2.2.0 (Build 13)** — pushed to Open Testing on Play Console.

### What is Active Right Now (Post-Launch & Multi-Platform Prep)
*   **v2.2.0 on Open Testing** — Profile completion flow with login-method-aware field locking, email opt-in defaults checked, phone/Google auth bug fixes.
*   **v2.1.1 on Open Testing** — banner ads, interstitial pacing, Watchlist MIS dialog fixes verified.
*   **KMP Migration:** Moving core entities and repositories to Kotlin Multiplatform (KMP) to support iOS.
*   **Time-Travel Mode (Backlog):** Historical simulation engine.

### Parallelizable Engineering Streams
Multiple contributors can work on these tracks in parallel to meet the launch deadline:
*   **Track A:** `docs/epics_and_sprints.md#Epic 17`
*   **Track B:** `docs/epics_and_sprints.md#Epic 18`
*   **Track C:** `docs/epics_and_sprints.md#Epic 19`
*   **Hyper-Personalization:** `docs/epics_and_sprints.md#Epic 24`

---

## 5. Architectural & Implementation Strategy

For a deep dive into how the system is organized and how it will expand to iOS/Web, please read:
👉 **[`docs/architecture.md`](docs/architecture.md)**

For the full breakdown of upcoming epics, sprints, and detailed task boards, please read:
👉 **[`docs/epics_and_sprints.md`](docs/epics_and_sprints.md)**

---

## 6. Authoritative Document Iteration Process

> **Agent Workflow:** Before starting any task, agents MUST read [`agent-skills.md`](./agent-skills.md) and invoke the appropriate skill from [`skills/`](./skills/).

To maintain continuous alignment and prevent code-spec drift, all agents and developers must adhere to the following iteration loop:

```
  ┌─────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐
  │  Step 1: Select Branch  │ ───> │  Step 2: Update Spec    │ ───> │  Step 3: User Approval  │ ───> │  Step 4: Implement Code │
  │ Checkout develop/website│      │ Edit epics/architecture │      │ Confirm plan in chat    │      │ Write Kotlin & Tests    │
  └─────────────────────────┘      └─────────────────────────┘      └─────────────────────────┘      └─────────────────────────┘
```

0.  **Branch Discipline:** You MUST work on the correct branch based on the task type:
    *   **`website` branch**: All changes to the static website (`website/` directory).
    *   **`develop` branch**: All Android App engineering, refactoring, and feature work.
    *   **`main` branch**: Reserved for stable production releases only. NEVER implement features directly on `main`.
1.  **Spec-First Modification:** Before implementing a new feature or modifying existing interfaces, update the target epic/sprint task in `docs/epics_and_sprints.md` or the design details in `docs/architecture.md` to show the proposed design.
2.  **Review & Handshake:** Present the spec changes to the user for feedback. 
3.  **Surgical Execution:** Once agreed, implement the feature, write corresponding unit/integration tests, and mark the task as complete (`[x]`) in the documentation.
4.  **Mandatory Testing Rule:** Always add or update corresponding unit tests whenever new functionality is implemented or modified. Every PR/change should include verification logic.
5.  **Background Tasks Rule:** Infinite loops or periodic background tasks must NEVER be placed in a `ViewModel`'s `init` block. Move these to an explicit `startBackgroundTasks()` function called by the Activity. This prevents the "Never-Idle" deadlock that hangs Android tests.
6.  **Versioning & Release Policy:** NEVER automatically bump version codes or numbers in `app/build.gradle.kts`. Always ask the user for explicit approval before performing a version bump. **`RELEASES.md` and `CHANGELOG.md` MUST ONLY be updated when the project is ready for release and ONLY after explicit user confirmation.**
7.  **No Dead-Ends:** Never add non-functional UI placeholders. Every visual affordance must connect to an active feature or remain omitted.
8.  **Authoritative Manual Verification:** Every feature implementation must include an update to the [Authoritative Manual Verification Protocol](file:///C:/Users/Atul/AppData/Local/Google/AndroidStudio2026.1.2/projects/tradelab.55ea813f/.artifacts/b97c14b5-a8e9-4d28-9834-c46165db831b/MANUAL_VERIFICATION_PROTOCOL.artifact.md) or the checklist to ensure functional integrity can be verified by testers.

---

## Unity LevelPlay Monetization (Epic 26)

> **Status: ✅ ADS LIVE — serving verified on device (2026-08-26)**
> AdMob banned → migrated to Unity LevelPlay (ironSource). Production config live
> (rewarded_v2 unit, ironSource Exchange serving test creatives to the registered
> test device; Unity Ads bidder initialized with valid tokens).
>
> **📚 Knowledge base (replicate to other apps):**
> - [`docs/LEVELPLAY_GUIDE.md`](docs/LEVELPLAY_GUIDE.md) — full integration reference (gradle, init, formats, troubleshooting, logcat signatures, multi-app org notes)
> - [`docs/ADMOB_TO_LEVELPLAY_MIGRATION.md`](docs/ADMOB_TO_LEVELPLAY_MIGRATION.md) — the step-by-step AdMob-removal + LevelPlay-wiring playbook (use for the app that still has AdMob + all future apps)
>
> **Key facts:** appkey `27b051bfd` · SDK 9.5.0 · adapter **5.12.0** + explicit
> **unity-ads 4.20.0** (⚠️ the adapter does NOT bundle the SDK — zero-dep POM;
> missing it = silent zero Unity Ads demand) · ironSource Exchange = default demand ·
> rewarded + interstitial wired (fail-closed, guarded by `AdFailClosedGuardTest`) ·
> natives removed (need mediated adapter) · consent metadata v1
>
> **Pending:** ironSource approval email (gates live demand for non-registered
> devices; test creatives already flow to registered devices) · consent flow
> (ConsentView follow-up) · native ads (need mediated adapter) · post-click
> display timeout (SDK quirk, documented)
>
> **✅ Done (v2.1.1):** banners (10 screens) · paced chapter-complete interstitial ·
> debug=test ads (`USE_TEST_ADS = BuildConfig.DEBUG`) · BuySellBottomSheet MIS
> dialog fix (LocalContext + drag-to-dismiss + test key correction)

---

## Academy Video Manifest & Hindi (Epic 27)

> **Status: ✅ LIVE — 92 videos + manifest v1 in production**
> Remote video delivery so new batches appear **without app releases**.
>
> **Flow:** `nlm/upload_to_firebase.py` (firebase CLI login auth — REST uploads;
> SA optional) → `gs://tradelab-4f858/videos/**` + `videos/manifest.json`
> (public read via deployed `storage.rules`) → app `VideoManifestRepository`
> (cache-first, 24h TTL, offline-surviving) → `LectureScreen` resolves
> remote URL > bundled raw > blank. Dynamic EN/हिंदी toggle renders
> per-lecture only when a Hindi variant exists.
>
> **Ops:** `python nlm/upload_to_firebase.py` (add `--dry-run`/`--list`/`--probe`).
> If the CLI session token goes stale, run any `firebase` command to refresh.
> Hindi: drop `lecture_X_Y_Z_HI_final.mp4` in `nlm/assets/out/` → re-run upload.
>
> ⚠️ **VIDEO WIRING RULE (No Dead-Ends):** Only set `videoUrl` in
> `academy_data_v2.json` when the asset ACTUALLY exists — polished mp4 bundled
> in `res/raw/` AND uploaded to Storage (manifest entry). A non-blank `videoUrl`
> pointing to a missing asset renders a BLACK BROKEN PLAYER, not "coming soon".
> Blank `videoUrl` = "Video lecture coming soon" card. Never pre-wire.

---

## NLM Video Pipeline Documentation

> **⚡ CURRENT SYSTEM: `nlm/pipeline.py` (Manager v2)** — a unified menu-driven manager
> (modes 0-8) that replaces the manual script sequence. It is autonomous (RUN ALL),
> self-healing (L3 silent re-auth), quota-aware (pre-stop at confirmed 3/20 daily caps
> with persistent state), lock-safe (kills CSV lockers), and owns a reassignment engine
> (stranded rescue + capacity rebalance + orphan-notebook cleanup queue).
> **Read [`nlm/docs/PIPELINE_V2.md`](nlm/docs/PIPELINE_V2.md) — the authoritative doc.**

| Document | Path | Purpose |
|----------|------|---------|
| **Pipeline Manager v2** ⭐ | `nlm/docs/PIPELINE_V2.md` | **Authoritative**: architecture, subsystems, state files, config, runbooks |
| **Status Sheet** | `nlm/docs/STATUS_SHEET.md` | Current progress and account status |
| **Troubleshooting** | `nlm/docs/TROUBLESHOOTING.md` | Auto-handled scenarios + manual recovery runbooks |
| **Quota Management** | `nlm/docs/QUOTA_MANAGEMENT.md` | Confirmed caps (3/20), classification, calibration |
| **Legacy Scripts** | `nlm/docs/PIPELINE.md` | Old one-shot scripts (extract/polish/firebase) — superseded for ops |
| **Script Inventory** | `nlm/docs/SCRIPT_INVENTORY.md` | Legacy script reference |

**Manager state files** (all in `nlm/`): `pipeline_state.csv` (ownership+progress
ledger, incl. `download_status`), `auth_state.json` (health + `blocked` flags),
`quota_state.json` (daily usage + parking), `cleanup_queue.json` (orphan notebooks
awaiting deletion), `config.json` (all knobs), plus `dashboard.html` (static
Accounts/Config/Progress panel) and `probe_auth.py` (alive/dead diagnostic).

**Legacy Scripts** (`nlm/`, still valid for their niche):
```
pipeline.py                  # ⭐ Unified manager — CHECK/CREATE/GENERATE/DOWNLOAD/
                             #   STATUS/RUN ALL/AUTH/REASSIGN (modes 0-8)
master_notebook_manager.py   # Legacy: create + generate (superseded)
check_auth.py                # Legacy: auth check (superseded by mode 7)
audit_notebooks.py           # Legacy: duplicate audit (superseded by mode 1 + dedupe)
download_all_videos.py       # Legacy: downloads (superseded by mode 4)
polish_videos.py             # Intro/outro branding — still needed post-campaign
extract_all_courses_fixed.py # Lecture extraction — still valid
upload_to_firebase.py        # Firebase upload — still needed post-campaign
```

**Quick Start (Manager v2):**
```bash
cd nlm
python pipeline.py    # menu 0-8
# Campaign flow: 7 (AUTH) → 2 (CREATE) → 6 (RUN ALL, autonomous ~days)
# Recovery:      7 (AUTH) → 8 (REASSIGN) → 6
# Diagnostics:   5 (STATUS) · probe_auth.py · dashboard.html
```

**7 Google Accounts** (profiles at `C:\Users\Atul\.notebooklm\profiles\`):
- atulkpal@gmail.com (Pro, 20 videos/day)
- ashwathai.dev@gmail.com (Standard, 3/day)
- boss.studio.care@gmail.com (Standard, 3/day)
- hi.jumpdroid@gmail.com (Standard, 3/day)
- iiidem.km@gmail.com (Standard, 3/day)
- promptwala.xyz@gmail.com (Standard, 3/day)
- paulritu120@gmail.com (Standard, 3/day)

**Operational notes (learned 2026-08-23/24):** Google revokes this farm's sessions
roughly daily (two waves so far) — the pipeline self-heals via L3 headless re-mint
when the browser-profile SSO survives, otherwise option 7 fresh logins are required
(space them out). Daily caps are hard: never provoke the 4th request. Keep the CSV
out of Excel while running — the manager kills lockers automatically.

---

## 9. Build & Release Policy

To maintain a reliable history of distributable assets and prevent accidental data loss:
1.  **Archiving Requirement:** Before generating new build artifacts (APKs/AABs), ALL existing artifacts in the `app/build/outputs/` directory must be **moved** to the root `releases/` folder and renamed for archival.
2.  **Safety Rule:** Never leave archived artifacts inside the `app/build/` directory, as Gradle tasks (like `clean` or `assemble`) may delete them during the next build cycle.
3.  **Naming Convention:**
    *   Debug APK: `debug-<versionName>-<versionCode>.apk`
    *   Release APK: `release-apk-<versionName>-<versionCode>.apk`
    *   Release AAB: `release-aab-<versionName>-<versionCode>.aab`
4.  **Versioning Discipline:** Never automatically bump version codes or names without explicit user approval.

---

## 10. Rejected Implementations (Historical Reference)

> **DO NOT USE** - The following implementations were attempted but rejected. They are preserved in their respective branches for historical reference only.

### feature/academy-video-monetization (REJECTED)

**Branch:** `feature/academy-video-monetization`
**Status:** REJECTED - Do not merge or continue work on this implementation.

**What was attempted:**
- Video-first lecture dialog with language selector
- Chapter-level accordion (only one chapter expanded at a time)
- Spring bounce animation via `Modifier.animateItem()`
- Native ad integration between courses
- Rewarded ad gate every 3 lectures for free users
- Centralized `AcademyAdConfig` for monetization

**Why it was rejected:** User found the implementation unsatisfactory.

**Files changed:**
- `AcademyAdConfig.kt` - Centralized ad config
- `AcademyListItem.kt` - Sealed class + flatten function
- `AcademyScreen.kt` - New Lessons tab with accordion
- `MainActivity.kt` - Video-first dialog, ad gating
- `TradingViewModel.kt` - Lecture data class, ad counters
- `Entities.kt` - preferredLanguage field
- `AppDatabase.kt` - v26 migration
- Unit tests: `AcademyAdConfigTest`, `AcademyListItemTest`, `LectureMultiLanguageTest`
