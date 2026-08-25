# Trade Lab â€” Epics & Sprints Backlog

This document defines the development roadmap for **Trade Lab**. It breaks down our long-term vision into concrete, manageable **Epics**, **Sprints**, and granular **Tasks**. Use this document to track active progress, coordinate parallel streams, and select your next engineering assignment.

---

## Roadmap Overview

```
 â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
 â”‚       EPIC 1: Local MVP Persistence & Engine [100% COMPLETE]â”‚
 â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                                â–¼
 â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
 â”‚  EPIC 2: Advanced Order Types & Trade Desk   [100% COMPLETE]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 3: Learn-to-Earn Financial Academy     [100% COMPLETE]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 8: On-Demand Ads & Gamified Monetization [100% COMP.] â”‚
 â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                                â–¼
 â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
 â”‚  EPIC 4: Technical Charts & Indicators       [100% COMPLETE]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 5: Serverless AI Portfolio Diagnostics [100% COMPLETE]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 7: Multi-Watchlists, Auth Gate & Paywalls [100% COMP.]â”‚
 â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                                â–¼
 â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
 â”‚  EPIC 6: Multi-Platform KMP Core Migration   [DEFERRED]     â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 9: Simulated F&O Engine & Premium Paywalls [100% COMP.]â”‚
 â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                                â–¼
 â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
 â”‚  EPIC 10: Codebase Modularization & Refactoring [100% COMP.]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 11: Architectural Modernization & Hilt DI [100% COMP.]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 12: UI Density & Market Realism        [100% COMP.]   â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 13: Institutional Portfolio Analytics  [100% COMP.]   â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 14: Market Sentiments & TV News        [100% COMP.]   â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 15: Real-World News & Option B Influence [100% COMP.] â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 16: Pre-Launch Polish & Advanced Realism [100% COMP.] â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 17: Track A â€” Institutional Visualization [100% COMP.]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 18: Track B â€” Margin & Bracket Orders     [100% COMP.]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 19: Track C â€” Social Discipline Scores     [100% COMP.] â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 20: Next.js Web App Platform               [DEFERRED] â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 22: Academy v2 â€” Varsity-Style Curriculum   [100% COMP.] â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 23: Multi-Format Monetization (AppOpen/Native) [100% COMP.]â”‚
 â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
 â”‚  EPIC 24: Hyper-Personalization & Focus Suite    [100% COMP.]â”‚
 â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
 ```

---


## Epic 25: Academy Experience Redesign (Status: ✅ Complete)
Transform the Academy from a flat, theme-broken card list into a visually distinct, full-screen learning destination with proper video ergonomics — while preserving the accordion CourseDeck users know.

### Sprint 25.1: Theme Engine Compliance
*   [x] **Accent De-hardcoding**: Replace 52× hardcoded `BrandViolet` in `AcademyScreen.kt` with `DynamicPrimary`; 18× `Color.White` → `TextOnAccent`; alpha surfaces → theme-aware getters. Academy respects SERIOUS/VIBRANT/TERMINAL/ARCADE/LIGHT. (Semantic tier colors keep their identity: BEGINNER stays `BrandViolet`.)
*   [x] **Sub-tab state**: `activeSubTab` `remember` → `rememberSaveable` (survives config changes).
*   [x] **Test**: New `AcademyThemeTest` — source-scan guard asserting no raw `Color(0xFF8B5CF6)` / `Color.White` literals in Academy composables.

### Sprint 25.2: Course Identity System
*   [x] **Gradient hero headers**: 6 per-course gradient sets (mapped by course id/tier) behind existing custom vector icons (`ic_course_markets/technical/fundamental/derivatives/psychology/taxation`).
*   [x] **Progress rings**: Replace per-course `ShimmerProgress` bar with animated progress ring + "N of M" inside the ring.
*   [x] **Tier accents**: BEGINNER/INTERMEDIATE/ADVANCED chips upgraded to tinted chips at 10sp minimum (legibility).

### Sprint 25.3: Full-Screen Lecture Destination
*   [x] **New `LectureScreen.kt`** (`ui/academy/`): full-screen destination replacing the modal Dialog for the lecture flow — immersive video slot (`VideoPlayerView`), reading pane, "Start Knowledge Check" CTA.
*   [x] **Quiz migration**: Knowledge-check flow (options, feedback cards, score, claim/double-reward) moves from the `MainActivity` Dialog into `LectureScreen` (~660 lines removed from MainActivity).
*   [x] **Routing**: `activeQuizLevelId`/`activeLectureIndex` state consolidated into a single lecture-session state consumed by `LectureScreen`; `BackHandler` for back navigation.
*   [x] **Video-ready slot**: Player renders when `videoUrl` resolves (manifest lands in Epic 27); elegant "Video coming soon" empty state otherwise (no dead ends).
*   [x] **Test**: `LectureScreenTest` — render states (video/no-video/locked), quiz answer flow, reward-claim callback.

### Sprint 25.4: Gamification Surface
*   [x] **Header stats**: Streak (from `dailyStreak`) + XP surfaced in the Academy hero card.
*   [x] **Celebrations**: `SparkleBurst` extended to chapter completion; certificate flow unchanged.
*   [x] **Manual Verification Protocol**: add Academy section — theme sweep across 5 modes, full-screen lecture flow, streak/XP visibility.

**DoD:** zero hardcoded violets/whites in Academy
### Sprint 25.6: Full UI/UX Polish Pass (PARKED — post-campaign)
*   [ ] **Whole-app visual polish**: audit every screen against the 5-mode theme engine, unify spacing rhythm (4/8dp), typography scale, icon sizing tokens, imagery/hero language, micro-interactions. Course-card v3 (gradient + icon-chip + glow) is the first artifact of this direction.
### Sprint 25.5: Video-First & Shorts Fullscreen (added post-review)
*   [x] **Video-first layout**: `LectureScreen` renders the video full-bleed at the top when present; reading content below. No-video lectures keep the reading card + coming-soon state.
*   [x] **Shorts-style fullscreen**: `VideoPlayerView` intercepts the HTML5 fullscreen request (`WebChromeClient.onShowCustomView`) and renders it edge-to-edge in a dialog with system bars hidden — portrait-immersive for vertical videos. Back exits.
*   [x] **WebView reload fix**: lecture pill switches now reload the WebView source (`AndroidView.update` + tag-guard) — previously the same video kept playing.
*   [x] **Internal video cache**: `VideoCacheManager` moved to `context.cacheDir` (FUSE-proof on emulators; correct evictable-cache semantics).
*   [x] **92 polished videos bundled** for preview: `res/raw/lecture_X_Y_Z_final.mp4` (gitignored — production distributes via Firebase, Epic 27); `academy_data_v2.json` wired to raw-resource names.
*   [x] **Verified on Pixel emulator**: Lec 1 ↔ Lec 2 pill switching plays distinct branded videos; fullscreen immersive confirmed manually.
 · lectures full-screen with video slot · streak/XP visible · all existing tests green + new test files.

---

## Epic 26: Unity LevelPlay Monetization Migration (Status: ✅ Code Complete / ⏳ Awaiting LevelPlay Activation)
Hard-cut migration from AdMob (account banned) to Unity LevelPlay (`appKey 27b051bfd`). Zero AdMob SDK trace. Revenue-critical. Architecture: see `docs/architecture.md` § Multi-Format Monetization Infrastructure.

### Sprint 26.1: Foundation & Rewarded Core
*   [x] **Gradle**: remove `play-services-ads`; add `com.unity3d.ads-mediation:mediation-sdk:9.5.0` + appset/ads-identifier/basement artifacts (bubbles-proven); ProGuard rules; verify `AD_ID` permission.
*   [x] **`LevelPlayAdManager`** (new, `ui/common/`): Application-scoped init with `sdkReady: StateFlow<Boolean>` gate; **preserves `loadAndShowRewardedAd(adType, onAdLoaded, onAdFailed, onUserEarnedReward)` signature** — 10+ call sites unchanged; rewarded preloading; single ad unit + `adType` as placement name.
*   [x] **`AdConfig.kt`** (new): single source for appKey + ad unit IDs + placement names (replaces 3 scattered ID sources).
*   [x] **Test**: `LevelPlayAdManagerTest` — fake SDK seam; reward delivery → callback contract; no-fill → `onAdFailed` path.

### Sprint 26.2: Formats & Fallback Surgery
*   [x] **Native ads REMOVED from UI** (follow-up): LevelPlay natives require mediated network adapters — unavailable (banned AdMob). 6 native spots temporarily removed; fast-follow when a network is onboarded.
*   [x] **App Open → Interstitial-on-foreground**: replace `AppOpenAdManager` with interstitial on lifecycle start (LevelPlay has no app-open format); 4h freshness window preserved.
*   [x] **Fallback surgery** 🚨: remove ALL fake-ad auto-grant fallbacks (Portfolio 50-credit leak, Academy fallback timer, Profile fallback dialog, MainActivity fallback video). No-fill → graceful "No ad available — try again later" state. **No free rewards.**
*   [x] **Simulated-ad flows → real rewarded**: AI-coach credits, F&O tokens, Commodities unlock switch from fake countdowns to `loadAndShowRewardedAd` (revenue on features that currently pay nothing).
*   [x] **Consent**: LevelPlay ConsentView basic GDPR/CCPA flow on first launch.

### Sprint 26.3: AdMob Extirpation
*   [x] **Delete**: `AdMobManager.kt`, `BannerAdView.kt` (dead), `NativeAdView.kt` + `ad_unified_row.xml`, `MobileAds.initialize`, manifest `APPLICATION_ID` meta-data, gradle dep.
*   [x] **Test**: source-scan guard — zero `com.google.android.gms.ads` imports in app module.
*   [x] **Manual Verification Protocol**: ad placements matrix (screen × format × reward), consent first-launch flow, premium-bypass checks.

**Known Pending Issues (resolve when testing on real device):**
1. **Emulator excludes rewarded ads** — Unity Ads returns 1024 no-fill on emulators; test on real device
2. **Account approval in review** — payment + company info submitted (2026-08-25); awaiting Unity review (1-3 business days) → live ads serve automatically, no redeploy
3. **Ad unit propagation** — newly created dashboard ad units take 15-30 min to activate on serving side
4. **Session revocation waves** — Google risk system kills sessions every 2-20h on flagged accounts; L3 self-heal + spaced re-auths manage this
5. **Native ads removed** — need mediated network adapter; fast-follow when a network is onboarded

**DoD:** zero AdMob imports · all 9 reward placements + 6 native spots + foreground interstitial on LevelPlay test ads · fallback leaks closed · consent flow live.
**External dependency:** user creates LevelPlay app `27b051bfd` + ad units in dashboard (names provided by agent); dev runs on LevelPlay test units until then.

---

## Epic 27: Video Manifest & Multi-Language Wiring (Status: ✅ Code Complete — pending SA key + Hindi content)
Runtime video-manifest strategy (new batches appear **without app releases**) + dynamic Hindi toggle (renders per-lecture only when a Hindi variant exists).

### Sprint 27.1: Manifest Repository
*   [x] **`VideoManifestRepository`** (new, `data/`): fetch `videos/manifest.json` from Firebase Storage (`tradelab-4f858`), cache-first with 24h TTL (`cacheDir` file — offline-first: cached manifest of ANY age beats nothing), graceful fallback to bundled `academy_data_v2.json` `videoUrl` on failure/miss. One-shot `fetchIfNeeded()` (no polling loops — Background Tasks Rule).
*   [x] **Schema** (as-built): `{"version": N, "generatedAt": "...", "videos": {"lecture_1_10_1_final": {"en": "https://…", "hi": "https://…|null"}}}` — keys match bundled `videoUrl` values for direct map-hit lookup.
*   [x] **Resolution API**: `lectureMedia(bundledVideoUrl, lang): LectureMedia(resolvedUrl, hasHindi, source)` — layered fallback: remote URL (selected language) → bundled raw (`_hi` suffix for Hindi) → bundled key → blank.

### Sprint 27.2: Dynamic Hindi Toggle (Per-Lecture)
*   [x] **UI**: EN/हिंदी pill toggle in `LectureScreen` — renders ONLY when the current lecture has a Hindi variant (manifest `hi` entry OR bundled `_hi` raw). No global flag; fully dynamic per the No Dead-Ends rule.
*   [x] **Persistence**: `academyLanguage` in `TradingViewModel` (SharedPreferences `academy_prefs.language`); `toggleAcademyLanguage()` EN↔HI.
*   [x] **Cache keys**: no change needed — `VideoPlayerView` derives cache keys from the remote filename (`lecture_X.mp4` vs `lecture_X_HI.mp4` are naturally distinct).

### Sprint 27.3: Pipeline Side (nlm)
*   [x] **`upload_to_firebase.py`** rewritten (firebase-admin, no gcloud): derives `course_N` from lecture code, pairs `_HI_final` Hindi variants, auto-generates + uploads `manifest.json` (version = prev+1), `--dry-run`/`--list`/`--sa PATH`; SA search: arg → `nlm/firebase-service-account.json` → Downloads (warns on project mismatch).
*   [x] **`storage.rules`** (new): `videos/**` public read, zero public writes (uploads via SA bypass rules); wired into `firebase.json`.
*   [x] **Tests**: `VideoManifestRepositoryTest` — 11 cases (parse/malformed, bundled fallback, en/hi resolution, hi→en fallback, hi-only manifest, unknown-key miss, fresh-cache-no-network, stale-cache-offline-survival).

### Pending (external)
*   [ ] **Service account**: jump-droid SA has Play Console access but **zero IAM on tradelab-4f858** (probed: Storage 403). Fix: Firebase Console → tradelab-4f858 → Project Settings → Service Accounts → Generate New Private Key → `nlm/firebase-service-account.json` (gitignored).
*   [ ] **Deploy rules**: `firebase deploy --only storage` after SA works.
*   [ ] **Hindi content**: generate `_HI` variants via NLM pipeline (toggle auto-appears on upload).

**DoD:** dropping polished videos + running upload makes them play in-app with zero app releases; Hindi toggle appears automatically per-lecture when content exists.

---
## Epic 24: Hyper-Personalization & Focus Suite (Status: ðŸŸ¢ Complete)
Expand the theme engine from simple colors to functional and visual modes that adapt to user privacy and focus needs.

### Sprint 24.1: Multi-Theme Engine Revamp
*   [x] **Architecture**: Transitioned from binary Dark/Light toggle to a 5-mode `ThemeMode` system (Serious, Vibrant, Terminal, Arcade, Light).
*   [x] **Color Centralization**: Removed 100+ hardcoded hex colors and replaced them with theme-aware dynamic getters in `Color.kt`.
*   [x] **Persistence**: Migrated UserProfile (DB v25) to store `themeMode`, `isStealthMode`, and `isZenMode`.

### Sprint 24.2: Privacy & Stealth
*   [x] **Stealth Blur**: Implemented `Modifier.stealthBlur()` using Android 12 native blur with a frosted-alpha fallback for older devices.
*   [x] **Global Privacy**: Applied blur masks to sensitive data (Net Worth, Cash, P&L) across all dashboards.

### Sprint 24.3: Pro & Retro Aesthetics
*   [x] **Terminal Mode**: High-density monochromatic theme with custom **JetBrains Mono-inspired Monospace typography**.
*   [x] **Arcade Mode**: 80s Synthwave palette with CRT scanline overlay effects on price charts.
*   [x] **Zen Mode**: Minimalist layout that reactively hides news tickers and movers to reduce trading anxiety.

### Sprint 24.4: Future Roadmap (Backlog)
*   [ ] **Time-Travel Mode**: Prototype historical simulation engine (Locked UI badge added).

---

## Epic 20: Next.js Web App Platform (Status: â¸ï¸ Deferred)
Transition from a static sandbox to a professional-grade, responsive Next.js application with full Android feature parity.

### Sprint 20.1: Institutional Foundation (Next.js & TypeScript)
*   [ ] **Infrastructure**: Initialize Next.js 14+ App Router project with absolute pathing and strict linting.
*   [ ] **Component Atomization**: Port existing static HTML segments into reusable React components (Dashboard, OrderTicket, Watchlist, Sidebar).
*   [ ] **Production State**: Implement **Zustand** with persistence middleware to manage simulation state.

### Sprint 20.2: Professional Execution Engine
*   [ ] **Math Port**: Migrate Random Walk and Steering logic to a dedicated `useMarketSimulation` hook.
*   [ ] **Chart Upgrade**: Replace SVG polylines with **Lightweight Charts** (by TradingView) for Candlestick rendering.
*   [ ] **Bracket Orders**: Implement multi-leg execution logic (Entry + StopLoss + TakeProfit).

### Sprint 20.3: Cross-Platform Synchronization
*   [ ] **Firebase Integration**: Connect Firebase Auth for unified user identity across Android and Web.
*   [ ] **Sync Logic**: Use Firestore to store real-time portfolio snapshots for cross-device parity.
*   [ ] **Secure AI API**: Migrate Gemini diagnostics to Next.js API Routes to protect server-side keys.

### Sprint 20.4: Social & Retention
*   [ ] **Social Scores**: Display quantified Discipline Scores and global leaderboards.
*   [ ] **Market Events**: Implement server-sent events (SSE) for live breaking news notifications.

---

## Epic 17: Track A â€” Institutional Visualization (Status: ðŸŸ¢ Complete)
Transition from simple line charts to professional-grade technical analysis tools.

### Sprint 17.1: Candlestick Engine
*   [x] **OHLC Migration**: Refactor `StockPrice.historyData` to support Open-High-Low-Close data structures.
*   [x] **Native Candlestick Renderer**: Implement a high-performance Canvas renderer for color-coded candles and wicks.
*   [x] **Volume Bars**: Add a bottom-aligned volume histogram to the main chart.

### Sprint 17.2: Advanced Indicators & Zoom
*   [x] **MACD Oscillator**: Add a dedicated sub-graph for Moving Average Convergence Divergence.
*   [x] **Bollinger Bands**: Implement standard deviation overlays on the main price chart.
*   [x] **Interactive Zoom**: Add horizontal scaling gestures to inspect specific timeframes.

---

## Epic 18: Track B â€” Margin & Bracket Orders (Status: ðŸŸ¢ Complete)
Implement professional-grade risk management and institutional order types.

### Sprint 18.1: Leverage & Margin Engine
*   [x] **Buying Power Multipliers**: Implement 5x leverage for Intraday (MIS) trades.
*   [x] **Margin Maintenance Logic**: Track used margin and available buying power in real-time.
*   [x] **Auto-Liquidation Logic**: Implement automatic position square-off if margin falls below safety thresholds.

### Sprint 18.2: Institutional Order Types (BO/CO)
*   [x] **Bracket Orders (BO)**: Create a multi-leg entry system (Entry + Target + StopLoss).
*   [x] **Trailing Stop-Loss**: Implement logic to adjust SL price automatically as profit increases.
*   [x] **OCO Logic**: Support "One Cancels Other" for simultaneous profit and loss triggers.

---

## Epic 19: Track C â€” Social Discipline Scores (Status: ðŸŸ¢ Complete)
Quantify investor maturity and introduce social competition.

### Sprint 19.1: The Discipline Score Algorithm
*   [x] **Quantified Maturity**: Design an algorithm deriving a score (0-100) based on position sizing, audit results, and holding times.
*   [x] **Discipline Badges**: Award dynamic profile badges (e.g., "Sizing Master", "Patience King") based on scores.

### Sprint 19.2: Social Leaderboards & Firebase Sync
*   [x] **Cloud Profile Sync**: Move Discipline Scores and basic profile metadata to Firebase Firestore.
*   [x] **Global Leaderboards**: Display the top 100 most disciplined traders globally with multi-sort (Wealth vs Maturity).
*   [x] **Precision Ledger**: Implement a consolidated tradebook tracking Component breakdowns to 4 decimal places.
*   [x] **Viral Growth**: Support "Discipline Challenge" sharing and friend-invitation hooks.

---

## Epic 16: Pre-Launch Polish & Advanced Realism (Status: ðŸŸ¢ Complete)
Refine the UI for high-information density, fix pre-launch glitches, and implement professional-grade settlement mechanics.

### Sprint 16.1: Viral Sharing & UI Stability
*   [x] **Share My Portfolio**: Implement high-contrast neon snapshot generator for social media.
*   [x] **Fixed Transparency**: Resolved bitmap generation issues and added solid background base to shared images.
*   [x] **Ghost Element Cleanup**: Fixed capture containers appearing behind active UI cards.
*   [x] **GenZ Sharing Hooks**: Added randomized catchy, performance-based phrases to shared portfolio cards.

### Sprint 16.2: Professional Market Mechanics (T+1 & Taxes)
*   [x] **T+1 Settlement Engine**: Implemented `sharesT1` logic where trades stay in "Positions" until the next market day.
*   [x] **Realistic Statutory Charges**: Added STT, Stamp Duty, and SEBI charges logic to trade executions.
*   [x] **Intraday vs. Delivery**: Backend support for distinct trade types and settlement cycles.
*   [x] **Intraday UI Toggle**: Added polished CNC/MIS (Equity) and NRML/MIS (F&O) selectors with educational tooltips to order tickets.

### Sprint 16.3: Adaptive Analytics & Dashboard
*   [x] **Auto-Marquee Ticker**: Replaced manual scroll with `basicMarquee` for a seamless TV-style experience.
*   [x] **Collapsible Heatmap**: Redesigned the sector allocation widget to save vertical space.
*   [x] **On-Demand Equity Curve**: Moved the performance chart to a demand-based pop-up with empty-state logic for new users.
*   [x] **Extended Rewards**: Increased Commodities Desk access duration to 3 hours per ad watch.

### Sprint 16.4: Launch Pricing â€” Real Product IDs & â‚¹49 Early-Bird Promo (Status: ðŸŸ¢ Complete)
Ship the real Google Play subscription products with a time-boxed early-launch offer: â‚¹99 is the true list price, but until **2026-09-01 00:00 IST** every subscriber gets a **7-day free trial + â‚¹49/mo (50% OFF)**. After the cutoff, the app automatically offers the â‚¹99 product. Lock-in is inherent to Play Billing â€” a â‚¹49 subscriber keeps renewing at â‚¹49; a â‚¹99 buyer pays â‚¹99.

*   [x] **SubscriptionConfig:** Centralized `billing/SubscriptionConfig.kt` with the real Play Console product IDs (`tradelab_subs` = â‚¹49 launch, `trade_lab_subs_99` = â‚¹99 regular), `FREE_TRIAL_DAYS = 7`, and promo window ending **2026-09-01 00:00 IST** (Asia/Kolkata).
*   [x] **Dynamic Product Selection:** Replace the placeholder `tradelab_pro_monthly` at the billing call site with `SubscriptionConfig.activeProductId()` â€” â‚¹49 product before Sep 1, â‚¹99 product on/after Sep 1.
*   [x] **Promo Countdown:** Ticking countdown banner ("â‚¹49 launch offer ends in DD:HH:MM:SS") on the Paywall dialog and Premium Hub CTA; UI auto-flips to â‚¹99 when the window closes.
*   [x] **Dynamic Pricing Text:** Replace all hardcoded "â‚¹99 / 15-Day Free Trial" strings with "7-Day Free Trial" + dynamic price (â‚¹49 + strikethrough â‚¹99 during promo).
*   [x] **SubscriptionConfigTest:** Unit tests for promo window, product/price selection, and the Sep 1 cutoff edge.

### Sprint 16.5: Store-Listing Compliance & Release v1.5.0 (Status: ðŸŸ¢ Complete)
Resolve Google Play policy/privacy gaps ahead of the v1.5.0 (Build 5) production release: stop exposing email addresses via the public leaderboard, add the Play-required in-app account deletion path, and align the website privacy policy with real data flows.

*   [x] **Leaderboard Privacy Hardening:** Key the public Firestore `leaderboard` collection by a SHA-256 hash of the user email (`hashUserId`), never the raw email; verified by known-vector unit tests.
*   [x] **In-App Account Deletion:** Add a "Delete Account" entry on the Profile screen that links to the Play-compliant deletion request page (`https://tradelab-4f858.web.app/delete-account.html`).
*   [x] **Privacy Policy Update:** Refresh `website/privacy.html` to disclose the Firestore leaderboard, Crashlytics, FCM, AdMob personalization, Play Billing, and retention/deletion process (website branch).
*   [x] **Release v1.5.0:** Version bump (versionCode 5 / 1.5.0), signed AAB verification, lint gate, Pixel 10 smoke test, and release docs.

---

## Epic 1: Local MVP Persistence & Engine (Status: ðŸŸ¢ Complete)
Build a solid, offline-first client database foundation with responsive Material 3 rendering.

### Sprint 1.1: Local Database Schema & Initialization
*   [x] Create entities for user profile, holdings, transactions, and stocks.
*   [x] Set up Room SQLite database context with structured schema.
*   [x] Implement automatic mock price and initial holdings database seeding.

### Sprint 1.2: Unidirectional Portfolios & Transactions
*   [x] Create centralized `TradingRepository` to validate balances.
*   [x] Implement BUY and SELL trade execution blocks.
*   [x] Expose aggregate calculations (Total Return, Open P/L, Asset distribution).

### Sprint 1.3: Visual Chart Canvas Renderer
*   [x] Design native Android Compose Canvas drawing routine.
*   [x] Map historic prices to high-contrast visual trend lines.
*   [x] Integrate live market fluctuation tick simulator to update graphs instantly.

### Sprint 1.4: Realism Budgets & Cognitive Profiler
*   [x] Implement multi-step questionnaire checking user retail intent.
*   [x] Guide users toward a disciplined starting capital recommendation (â‚¹10,000 to â‚¹100,000).
*   [x] Implement portfolio reset database queries.

---

## Epic 2: Advanced Order Types & Trade Desk (Status: ðŸŸ¢ Complete)
Move beyond simple market executions. Implement realistic broker order types, GTT screens, and a futuristic expandable bottom sheet.

### Sprint 2.1: Database Schema Support for Pending & GTT Orders
*   [x] Add `PendingOrderEntity` table to `Entities.kt` to persist unexecuted Limit, Stop-Loss, and GTT (Good-Till-Triggered) orders.
*   [x] Support order status properties (`PENDING`, `EXECUTED`, `CANCELLED`) and order duration types (`DAY`, `GTT`).
*   [x] Define DAOs to query, update, and delete active pending orders based on simulated price levels.

### Sprint 2.2: Futuristic Slide-Up Order & Detail Sheet
*   [x] Replace the static order view with a sleek, interactive Slide-Up Bottom Sheet for Buy/Sell/GTT operations.
*   [x] Add an expandable caret/arrow button on the top-header of the sheet.
*   [x] When tapped, animate the sheet to expand to a gorgeous full-screen stock detailed view containing:
    *   An expanded canvas-drawn historic line chart.
    *   Detailed stock indicators (High/Low, Open, 52-Week Range, Volume, Market Cap).
    *   Educational tabs explaining Market vs. Limit vs. GTT orders.
*   [x] Embed an order configuration widget directly inside the expanded/collapsed sheet with numeric inputs, position sizing advice, and quick-percent buttons (e.g., "25%", "50%", "100%" of buying power).

### Sprint 2.3: Order Matching & GTT Trigger Engine
*   [x] Integrate GTT and Limit pending-order trigger evaluation into the `TradingRepository`.
*   [x] When the Market Tick Simulator changes prices, evaluate all active pending and GTT entries.
*   [x] Automatically execute qualifying orders, log transactions, and notify the user with an in-app visual toast/alert.

---

## Epic 3: Learn-to-Earn Financial Academy (Status: ðŸŸ¢ Complete)
Reward study habits with additional virtual trading capital.

### Sprint 3.1: Academic Lesson Database
*   [x] Add a `LessonEntity` database table storing financial articles, trading concepts, and risk guidelines.
*   [x] Define lesson categories (e.g., "Position Sizing", "Market Basics", "Order Types").
*   [x] Track user read status and lock/unlock progress flags.

### Sprint 3.2: Interactive Terminology Quizzes
*   [x] Implement a Quiz UI overlay showing multiple-choice questions at the end of each lesson.
*   [x] Provide immediate feedback on correct/incorrect answers with interactive explanations.
*   [x] Store quiz pass/fail histories in the local database.

### Sprint 3.3: Virtual Capital Unlocking Logic
*   [x] Implement the mathematical progression code: passing a quiz unlocks â‚¹5,000 or â‚¹10,000 in virtual capital.
*   [x] Add a "Claim Capital" transaction that appends the unlocked amount to the user's current buying power.
*   [x] Design an aesthetic UI celebration overlay with custom animations for claiming rewards.

---

## Epic 4: Technical Charts & Overlays (Status: ðŸŸ¢ Complete)
Add intermediate indicators and professional metrics to the custom line charts.

### Sprint 4.1: Technical Analysis Algorithms
*   [x] Write pure Kotlin algorithms computing Simple Moving Average (SMA) and Exponential Moving Average (EMA).
*   [x] Implement Relative Strength Index (RSI) calculations based on a stock's historic price entity arrays.
*   [x] Expose calculated indicator arrays inside the `TradingViewModel` state.

### Sprint 4.2: Chart Overlay Renderer
*   [x] Update Canvas rendering code to draw auxiliary SMA/EMA lines on top of the stock line.
*   [x] Add an optional bottom sub-graph pane dedicated to drawing RSI oscillators.
*   [x] Build a drag gesture observer displaying stock prices dynamically as a user drags their finger across the chart.

---

## Epic 5: Serverless AI Portfolio Diagnostics (Status: ðŸŸ¢ Complete)
Leverage Gemini to audit trading behaviors offline and protect retail speculators.

### Sprint 5.1: Secure Gemini SDK Integration
*   [x] Set up safe API credentials storage using AI Studio's Secrets panel.
*   [x] Configure Kotlin's Gemini API client (incorporating error handling and network status checks).

### Sprint 5.2: Portfolio Behavior Audit Engine
*   [x] Structure a structured prompt passing historic transaction logs, current holdings, and overall return metrics to the Gemini model.
*   [x] Query Gemini to diagnose behavioral biases (e.g., "Sunk-Cost Fallacy: holding losing trades too long", "Revenge Trading: excessive transaction volume").
*   [x] Display the generated advisor report in an aesthetic "AI Strategy Hub" dashboard tab.

---

## Epic 7: Multi-Watchlists, Auth Gate & Paywalls (Status: ðŸŸ¢ Complete)
Support multiple persistent watchlists, user registration gating, and premium AI features.

### Sprint 7.1: Multi-Watchlist Schema & Management
*   [x] Implement `WatchlistNameEntity` and link to `WatchlistEntity` in `Entities.kt` to support up to 5 custom, renamable lists.
*   [x] Provide ViewModel states for current active watchlist selection and renaming.
*   [x] Create UI for Watchlist Screen with a dropdown/tab system to switch watchlists, a dialog to rename lists, and quick add/remove.

### Sprint 7.2: Registration Gate & Production Auth
*   [x] Add action counter in local storage or database to track "trial actions" (e.g., executing trades, creating watchlists).
*   [x] Block further core actions after 5-10 trial attempts with an aesthetic registration screen.
*   [x] **Production SDK Integration:** Replaced login simulation with real Firebase Authentication (Google Sign-In via Credential Manager & Phone SMS Auth) for release builds.

### Sprint 7.3: Paywall Gating & AI Analysis Hub
*   [x] Design an interactive premium paywall overlay highlighting locked features (e.g., "Talk to AI", Gemini portfolio diagnostics).
*   [x] Integrate Gemini API into a dedicated chat/audit view, protected behind the premium subscription simulator.

---

## Epic 6: Multi-platform KMP Core Migration (Status: â¸ï¸ Deferred)
*Deferred until >100 downloads on Play Store.*
Scale the local architecture to support iOS and Web applications.

### Sprint 6.1: Shared Library Structuring
*   [x] Create a modular `:shared` project block inside `settings.gradle.kts`.
*   [x] Separate core repository, Room database schemas, and ViewModel flows from Android framework code (using TradingHelper and Platform).

### Sprint 6.2: Room KMP & Platform Adapters
*   [x] Configure Room KMP and multi-platform compilation compiler targets (Android and Wasm WebAssembly).
*   [ ] Configure Room KMP plugins in build dependencies.
*   [ ] Implement native SQLite factories for Android (`androidMain`) and iOS (`iosMain`).
*   [ ] Verify database read/write compilation on both platforms.

### Sprint 6.3: Web Client Setup
*   [ ] Configure Kotlin/Wasm platform compile targets.
*   [ ] Build a lightweight HTML5 canvas layout, or wrap shared states inside a React/JS framework.

---

## Epic 8: On-Demand Ads & Gamified Monetization (Status: ðŸŸ¢ Complete)
Monetize the platform responsibly for the 16â€“35 demographic with a 100% non-intrusive, user-initiated rewarded-ad model ("Watch-to-Earn").

### Sprint 8.1: Rewarded State Architecture & Core Credit Wallet
*   [x] Add `brokerageCredits` (Int), `indicatorsUnlockedUntil` (Long), and `aiAuditCredits` (Int) properties to local user profile/state managers.
*   [x] Modify transaction execution routines in `TradingRepository` to check for `brokerageCredits`. If available, consume 20 credits to shield/waive simulated brokerage fees; if empty, deduct actual simulated cash (0.05% fee) from virtual capital.
*   [x] Link on-demand credit claims (e.g., "+100 Brokerage Credits", "+â‚¹500 Emergency Recharge") to wallet and portfolio state handlers in the Repository.
*   [x] Integrate the Academy quiz state with ad-rewards: include fields for daily quiz limits, cooldown bypass state, and double reward claim triggers.

### Sprint 8.2: Visual Value-Exchange & Dedicated Power-Up Vault UI
*   [x] Design the Dedicated "Power-Up Vault" on the Profile screen allowing on-demand ad pre-watching to claim credits (+100 credits, +â‚¹500 emergency cash, +1 AI credit).
*   [x] Add low-profile neon badges in the Slide-Up Order sheet displaying current credits: `ðŸŽ« Brokerage Wallet: X Credits` and the corresponding active shield status.
*   [x] Create locked indicator toggles (EMA/RSI) that open a premium dialog explaining the 12-hour unlock-on-ad-watch value offer.
*   [x] Design the "AI Strategy Hub" credit widget requesting ad-watches for extra Gemini diagnostics audit credits.
*   [x] Integrate ad booster options directly on the Academy quiz success screen (e.g., "Double My Capital Reward" or "Bypass Cooldown").

### Sprint 8.3: Video Player Simulation & SDK Integration (âœ… Production Ready)
*   [x] Create a modular overlay showing a clean 5-second simulated video player with a progress bar and completion callback for offline testing.
*   [x] Add dependencies and initial bindings for Google Mobile Ads (AdMob) Rewarded Video API to prepare for live production deployment.
*   [x] Integrate official production AdMob App ID (`ca-app-pub-3038055603735419~8287049082`) and custom Rewarded Ad Unit IDs across all placements (Academy Double, Portfolio Shield, Profile Cash, AI Advisor, Indicators, Shield Max Limit).
*   [x] Wrap the integration inside standard, compiler-enforced `BuildConfig.DEBUG` checks using Gradle manifest placeholders and enums, ensuring safety (displaying standard Google test ads during debug builds and rendering live revenue-generating ads on release signed builds).

---

## Epic 9: Simulated F&O (Futures & Options) Engine & Premium Paywalls (Status: ðŸŸ¢ Complete)
Develop a robust, offline-capable derivative simulation engine based on underlying assets, complete with compliance disclaimers, educational gated locks, premium subscriptions, and ad-supported premium "taste-testing."

### Sprint 9.1: Option Chain Generation Engine (Simulated Derivatives)
*   [x] Build an mathematical generator that dynamically structures Option Chains (Call/Put options) based on the underlying stockâ€™s active simulated price.
*   [x] Define Option strike prices with uniform intervals (e.g., Â±1%, Â±2%, Â±5% out-of-the-money/in-the-money).
*   [x] Calculate Option premiums dynamically using simplified Black-Scholes approximations or decaying pricing equations relative to remaining days-to-expiration (DTE).
*   [x] Implement trade execution blocks for Call/Put BUY/SELL, tracking premium expenditure, collateral requirements, and dynamic P/L changes as the underlying price fluctuates.

### Sprint 9.2: Strict Educational Level Locks & Monetization Paywalls
*   [x] Lock the entire F&O and Commodity trading section behind the Academy level gate. Users must complete and pass the "Equity Basics" curriculum to unlock F&O.
*   [x] **Google Play Billing Integration:** Implemented the real Google Play Billing SDK for subscription purchases in production builds while maintaining high-fidelity simulation in debug mode.
*   [x] Create additional paywall lockouts for advanced premium tiers: Unlimited AI Behavioral Diagnostics, customized multi-watchlist profiles (more than 2), and advanced indicator suites (SMA/EMA/RSI combos).

### Sprint 9.3: Ad-Supported Premium "Taste-Tests" (Rewarded Trial Tokens)
*   [x] Implement a "Taste-Testing" rewarded ad value-loop: users who choose not to pay the cash subscription can watch a 30s video ad to receive **3 F&O Free Trade Tokens** or **1 AI Portfolio Audit Pass**.
*   [x] Track premium trial tokens in local preferences / state, permitting active use of paywalled screens for limited runs.
*   [x] Ensure elegant transitions/dialogs showing: `ðŸŽ« Premium Trial Token Active (Remaining: X)` to build high conversion interest without permanently restricting non-paying users.

### Sprint 9.4: Educational Compliance Disclaimers & Offline Error States
*   [x] Implement a **Progressive, Just-In-Time (JIT) Disclaimer Framework**: Disclaimers are never presented as a global app-entry block. Instead, they trigger dynamically *only* when a user actively attempts to use or trade in a specific asset class for the first time.
*   [x] **Capital Markets (NSE/BSE Equity) Disclaimer:** Upon entering the Search, Watchlist, or Order Ticket for the first time, present a bottom-sheet stating:
    > *"Educational Sandbox Disclaimer: Trade Lab utilizes delayed market feed quotes for Indian Equity markets (NSE/BSE). This software is strictly a simulated educational sandbox designed to build healthy risk management and position sizing habits. Prices and executions are delayed, simulated, and do not represent active real-world trading, live brokerage routing, or guaranteed market liquidity. Trade Lab is not a registered financial advisor or broker."*
*   [x] **F&O & Commodity Derivatives Disclaimer:** Upon unlocking and attempting to trade Options/Futures or Commodities for the first time, present a dedicated bottom-sheet stating:
    > *"Simulated Derivatives Compliance Notice: Trade Lab options and futures chains are mathematically synthesized internally based on historical underlying asset trends and localized pricing algorithms. These chains are designed purely for educational walkthroughs; they do NOT pull live contract options data from the National Stock Exchange (NSE) or Bombay Stock Exchange (BSE), nor do they reflect actual market open interest or clearinghouse operations. Derivative trading is highly speculative and carries extreme risk of capital loss."*
*   [x] **Consent Persistence:** Store the user's explicit consent for each disclaimer (`equity_disclaimer_accepted = true`, `fno_disclaimer_accepted = true`) inside Room/SharedPreferences so they only need to agree once, keeping future flows fast and fluid.
*   [x] Implement connectivity-sensing error flows: when the device has no internet connection, display a highly polished inline state banner or toast stating **"Network not available."** to manage user expectations gracefully during network transitions.

### Sprint 9.5: Infinite NSE/BSE Ticker Autocomplete & Dynamic DB Insertion
*   [x] Create a dynamic search auto-complete function that queries the Yahoo Finance Autocomplete API (`https://query2.finance.yahoo.com/v1/finance/search?q={query}&lang=en-IN&region=IN`) to instantly look up any Indian security listed on the NSE (`.NS`) or BSE (`.BO`).
*   [x] Implement a dynamic SQLite/Room injection flow: when a user clicks on an unseeded search result, fetch its latest core quote and write it as a new `StockPrice` row on the fly.
*   [x] Seed a rich, diverse local list of 100 core Indian stocks representing Nifty 50 and key indices, ensuring that if a user is completely offline, they still have an instant, diverse, and robust sandbox across all sectors (IT, Energy, Finance, FMCG).
*   [x] Maintain historical data simulation loops: any newly injected ticker will automatically receive simulated price tick variations in real-time, matching existing assets seamlessly.

### Sprint 9.6: Commodity Trading Desk & Ad-Wall Gate Integration
*   [x] Implement a dedicated **Commodities Desk** screen showing simulated MCX Indian commodities (Gold, Silver, Crude Oil, Natural Gas, Copper) alongside Global COMEX/NYMEX commodity indices.
*   [x] Establish highly realistic real-time price conversions: Indian MCX prices are dynamically converted on-the-fly from global COMEX/NYMEX USD values at a standard exchange rate (â‚¹83/$), adjusted to correct trading units (e.g., per 10g for gold, per kg for silver, per barrel for crude oil).
*   [x] Implement an interactive **Derivatives Ad-Wall Security Gate**: since commodities trade on high leverage, gate access behind a premium-supported ad loop. Non-premium users can watch a simulated 5-second sponsor video with an active timer to unlock the desk for 12 hours.
*   [x] Fully integrate the Commodities Desk into the primary tab navigation (adding a beautiful new "Commodities" tab) and link it directly to the core ViewModel order ticket for buying and selling commodity contracts in real-time.

---

## Epic 10: Codebase Modularization & Refactoring (Status: ðŸŸ¢ Complete)
Deconstruct the monolithic 9,000+ line `MainActivity.kt` into a highly decoupled, modular, feature-by-package structure. This establishes strict single-responsibility boundaries, simplifies future white-labeling configurations, improves compilation speed, and ensures high testability.

### Sprint 10.1: Package Architecture Setup & Core Utilities Isolation
*   [x] Define package structure under `com.ashwathai.tradelab.ui`: `common`, `portfolio`, `charts`, `watchlist`, `academy`, `derivatives`, `commodities`, `profile`, and `diagnostics`.
*   [x] Move common colors, styles, string formatters, global composables, and progressive JIT disclaimers to the `com.ashwathai.tradelab.ui.common` package.
*   [x] Verify empty module shells compile successfully with correct imports.

### Sprint 10.2: Feature Decomposition (Screens and Components)
*   [x] Extract **Portfolio & Dashboard View** (`PortfolioScreen.kt`, `PerformanceCharts.kt`) to `com.ashwathai.tradelab.ui.portfolio`.
*   [x] Extract **Interactive Technical Charting & Canvas System** (`StockChartCanvas.kt`) to `com.ashwathai.tradelab.ui.charts`.
*   [x] Extract **Watchlists, Stock Search & Main Order Tickets** (`WatchlistScreen.kt`, `OrderTicket.kt`) to `com.ashwathai.tradelab.ui.watchlist`.
*   [x] Extract **Academy Hub & Gamified Quiz Systems** (`AcademyScreen.kt`, `QuizViews.kt`) to `com.ashwathai.tradelab.ui.academy`.
*   [x] Extract **Derivatives Chain, Greeks & Option Order Ticket** (`FoDeskScreen.kt`, `OptionOrderTicket.kt`) to `com.ashwathai.tradelab.ui.derivatives`.
*   [x] Extract **Commodities Desk View** (`CommodityDeskScreen.kt`) to `com.ashwathai.tradelab.ui.commodities`.
*   [x] Extract **Profile Screen, Subscription Purchase & Habits Profiler** (`ProfileScreen.kt`, `ProfilerFlow.kt`) to `com.ashwathai.tradelab.ui.profile`.
*   [x] Extract **AI Behavioral Diagnostics View** (`DiagnosticsScreen.kt`) to `com.ashwathai.tradelab.ui.diagnostics`.

### Sprint 10.3: Root Shell Wiring & Whitelabel Toggle Layer
*   [x] Refactor `MainActivity.kt` to act as a slim router hosting only the `MainActivity` activity class and the core `TradeLabApp` bottom navigation shell.
*   [x] Create a whitelabel features configuration toggle class (`WhitelabelConfig.kt`) enabling fast runtime or compile-time whitelabel features masking (e.g., enable/disable options trading, toggle academy access).
*   [x] Perform comprehensive local JVM compilation and lint checks to ensure all imported packages compile flawlessly.

---

## Epic 11: Architectural Modernization & Hilt DI (Status: ðŸŸ¢ Complete)
Transition the app to modern dependency injection patterns to improve testability, solve build hangs, and prepare for future scale.

### Sprint 11.1: Hilt Dependency Injection Integration
*   [x] Configure Hilt Gradle plugins and add KSP-based dependencies to the project.
*   [x] Establish `TradeLabApplication` as the Hilt app entry point.
*   [x] Create DI modules (`DatabaseModule`, `FirebaseModule`, `DispatcherModule`) for global resource management.
*   [x] Refactor `TradingViewModel`, `TradingRepository`, and `LeaderboardManager` for constructor injection, removing all static singleton calls (`getInstance()`).

### Sprint 11.2: Test Stabilization & Idleness Deadlock Fix
*   [x] Diagnose and resolve the "never-idle" deadlock hanging the build by moving infinite `while(true)` loops out of the `init` block to `startBackgroundTasks()`.
*   [x] Modernize `TradingViewModelTest` and `LeaderboardManagerTest` to use direct constructor injection with mocks, resulting in millisecond execution times.
*   [x] Establish "Mandatory Testing" and "No Infinite Loops in Init" architectural rules to prevent regression.

### Sprint 11.4: Authentication Stabilization & Diagnostics
*   [x] Implement robust error diagnostics for Google Sign-In and Phone Auth.
*   [x] Add explicit `Sign Out` flow and button on the Profile screen.
*   [x] Ensure loading state safety in `AuthScreen` to prevent UI hangs during cancelled logins.

### Sprint 11.3: SDK & Framework Modernization
*   [x] Update project to **Android API 37** to support the latest Hilt and Lifecycle features.
*   [x] Stabilize `MainActivityLaunchTest` using Hilt-compatible mocking strategies.

---

## Epic 12: UI Density & Market Realism (Status: ðŸŸ¢ Complete)
Optimize the user experience for high-information density and authentic Indian market behaviors.

### Sprint 12.1: Ultra-Dense Watchlist
*   [x] Move multi-watchlist switcher to the top for faster access.
*   [x] Implement a collapsible Search Lens in the HeaderBar to maximize vertical screen space.
*   [x] Implement "Vanishing Tickers" logic: automatically hide suggested tickers once the user has added 5+ stocks.
*   [x] Create dual-mode (Classic/Compact) Watchlist rows with persistent database storage (DB v9).

### Sprint 12.2: Gesture-Based Navigation
*   [x] Integrate `HorizontalPager` as the main navigation shell.
*   [x] Synchronize pager state with `BottomNavBar` and `TradingViewModel.currentTab`.
*   [x] Enable pre-loading of adjacent screens for a lag-free premium feel.

### Sprint 12.3: Indian Market Logic & Enforcement
*   [x] Implement strict session hour enforcement: Indian stocks stop wiggling after 3:30 PM IST in Live mode.
*   [x] Integrate the 2026-2027 NSE/BSE holiday calendar.
*   [x] Add a visual "MARKET CLOSED" red badge in the Watchlist header for immediate session feedback.

---

## Epic 13: Institutional Portfolio Analytics (Status: ðŸŸ¢ Complete)
Bring professional-grade risk management tools to the retail simulator.

### Sprint 13.1: Sectoral Heatmap Visualization
*   [x] Create an industry mapping registry for all seeded tickers (Banking, Tech, Energy, etc.).
*   [x] Implement a multi-segment heatbar on the Home screen to visualize concentration risk.
*   [x] Update heatmap dynamically as the user buys or sells across different sectors.

### Sprint 13.2: Portfolio Equity Curve
*   [x] Create the `AccountSnapshot` database system (DB v12) to track daily performance.
*   [x] Build a professional line chart widget displaying account value growth over time.
*   [x] Implement a "Simulate Trading Day" debug action to instantly generate historical snapshots for verification.

---

## Epic 14: Market Sentiments & TV News (Status: ðŸŸ¢ Complete)
Enhance immersion with contextual news feeds and a sensationalized TV-style presentation.

### Sprint 14.1: Contextual News Engine
*   [x] Create the `MarketNews` data infrastructure and DAO.
*   [x] Implement an intelligent news trigger: price wiggles > 1.5% automatically generate localized sentiment headlines.
*   [x] Build the "Market Pulse" scrolling widget on the Home screen.

### Sprint 14.2: Sensationalized TV Ticker
*   [x] Design the `BreakingNewsTicker` globally pinned bar with pulsing "BREAKING" tags.
*   [x] Implement dynamic channel branding for Indian brands (CNBC Awaaz, Zee News, ET Now).
*   [x] Integrate ticker-specific insights into the expanded order ticket sheet.

---

## Epic 15: Real-World News & Option B Influence (Status: ðŸŸ¢ Complete)
Bridge the gap between simulation and reality with actual market headlines and math-driven news impact.

### Sprint 15.1: Yahoo Finance News Sync
*   [x] Integrate the real-world Yahoo Finance News API to fetch actual headlines by ticker symbol.
*   [x] Map global publishers to local Indian financial brands for deep simulator immersion.

### Sprint 15.2: Sentiment-Driven Math (Option B)
*   [x] Implement `sentimentBias` in the price simulation engine.
*   [x] **Option B Math:** Stocks with strong "Bullish" real-world news drift toward their anchors **3x faster** than neutral stocks.
*   [x] **Pro AI Summary:** Exclusively for Paid Members, use Google Gemini to read real news and generate an "Executive Impact Brief" and sentiment score.

---

## Epic 21: Developer Review Sprint (Status: ðŸŸ¢ Complete)
Comprehensive review and improvement sprint addressing 10 product areas including GTT orders, quick exit workflows, ledger redesign, adaptive guidance, charts, commodities, F&O accounting, premium experience, and cross-cutting improvements.

### Sprint 21.1: Buy/Sell Dialog â€” GTT Fix & Quick Exit
*   [x] Add `validUntil` field to `PendingOrder` entity for GTT persistence
*   [x] Differentiate GTT (survives EOD) from Limit (cancelled at market close) in `matchPendingOrders()`
*   [x] Add daily expiry cleanup for expired Limit orders in `simulateMarketTick()`
*   [x] Add "Exit Position (Market)" button in BuySellBottomSheet when user holds the stock
*   [x] Add GTT badge in PendingOrdersList UI

### Sprint 21.2: Positions â€” Quick Exit
*   [x] Add inline "SQUARE OFF" button per equity position row in `PositionsList`
*   [x] Ensure one-tap market exit via `viewModel.sellStock()`

### Sprint 21.3: Ledger Redesign
*   [x] Group ledger entries by date bucket (Today, Yesterday, This Week, Older)
*   [x] Add summary card at top: Total Credits, Total Debits, Net Flow
*   [x] Add trade-type icons for scannability (BUY/SELL/FEE)
*   [x] Show P&L per line item with improved information hierarchy

### Sprint 21.4: Adaptive Risk/Action/Discipline Guidance
*   [x] Replace static watchlist tip with behaviour-adaptive guidance
*   [x] Add "X points from next badge" hint in Profile

### Sprint 21.5: Charts â€” Historical Data & Timeframes
*   [x] Add `CandleEntry` Room entity with proper schema
*   [x] Generate 30 days of historical candle data on first launch
*   [x] Add timeframe selector (15m, 1H, 4H, 1D, 1W) to ChartScreen
*   [x] Add Y-axis price labels, X-axis time labels, grid lines
*   [x] Add simulated data disclosure banner on chart screen

### Sprint 21.6: Commodities Realism
*   [x] Add lot size enforcement for MCX commodities
*   [x] Add MCX-specific STT rate (0.01%)
*   [x] Add commodity-specific volatility tuning (Â±0.8%)
*   [x] Display lot size info on commodity quote cards
*   [x] Add educational tooltip about MCX contract specifications

### Sprint 21.7: F&O Accounting & Fixes
*   [x] Add `OptionContract` entity with strike, expiry, lotSize, optionType fields
*   [x] Fix premium accounting: track as asset, not cash expense
*   [x] Implement expiry handling (auto-settle expired contracts)
*   [x] Add real NSE strike intervals for major tickers
*   [x] Add dynamic lot sizes per underlying

### Sprint 21.8: F&O Exit â€” Dedicated Workflow
*   [x] Add dedicated `exitOptionPosition()` method with proper P&L settlement
*   [x] Add "Close All F&O Positions" batch button
*   [x] Add expiry countdown badge on option holdings
*   [x] Add auto-expiry settlement (value â†’ 0 at expiry, P&L realized)

### Sprint 21.9: Premium Ad-Free Experience
*   [x] Audit all 10+ ad gates for `isPremium` bypass
*   [x] Ensure premium users see "PRO" badges instead of ad/watch buttons
*   [x] Remove stale `_showGoogleBilling` state

### Sprint 21.10: Cross-cutting Improvements
*   [x] Tiered simulation volatility: equities Â±0.3%, commodities Â±0.8%, crypto Â±1.5%
*   [x] Holdings sort toggle (by P&L, value, alphabetical)
*   [x] Time-decay discipline score (1pt/week after 30 days inactivity)

---

## Epic 23: Multi-Format Monetization (Status: ðŸŸ¢ Complete)
Diversify revenue streams by introducing non-intrusive App Open and Native ad formats.

### Sprint 23.1: Global Ad Infrastructure
*   [x] **App Open Ad Manager**: Implement `AppOpenAdManager` to load and show ads when user returns to the app.
*   [x] **Native Ad Loader**: Centralized logic to fetch and cache native ads for inline list display.
*   [x] **Hilt/Lifecycle Binding**: Use `ProcessLifecycleOwner` to monitor app-wide background/foreground events.

### Sprint 23.2: Native Ad Blending
*   [x] **Custom Native Layout**: Design a dark-themed XML layout for native ads with BrandViolet highlights.
*   [x] **Watchlist Injection**: Injected a native ad row every 5 tickers.
*   [x] **Portfolio & Academy Cards**: Added "Sponsored" native cards to high-traffic dashboards.
*   [x] **Premium Bypass**: Enforce ad-free experience for `isPremium == true` users across all new formats.
Expand the Learn-to-Earn Academy from 8 flat single-question modules into a two-level, Varsity-inspired curriculum: **Courses â†’ Chapters**, each chapter containing 3â€“4 lectures and a 3â€“5 question knowledge check. Content is **100% original** (structure inspired by Zerodha Varsity; no copyrighted text reproduced).

> Authoring contract: [`docs/ACADEMY_CONTENT_SPEC.md`](docs/ACADEMY_CONTENT_SPEC.md)

### Sprint 22.1: Content Authoring Contract & Schema
*   [x] Write the content authoring spec (`docs/ACADEMY_CONTENT_SPEC.md`) defining the v2 JSON schema, reserved chapter IDs (101â€“610), tiered rewards, tone rules, and verification checklist.
*   [x] Author the 6-core-course curriculum (`academy_data_v2.json`): Stock Market Basics (12 ch), Technical Analysis (12 ch), Fundamental Analysis (10 ch), Futures & Options (12 ch), Risk Management & Trading Psychology (12 ch), Markets & Taxation (10 ch).

### Sprint 22.2: Data Model & Content Engine
*   [x] Add `AcademyCourse`, `ChapterModule`, and `QuizQuestion` data classes to the shared model.
*   [x] Upgrade the academy loader in `TradingViewModel` to parse v2 JSON (courses â†’ chapters â†’ lectures + quizzes).
*   [x] Provide a backward-compatible fallback mapping legacy `academy_data.json` `QuizModule`s into a synthetic "Stock Market Basics" course so existing progress and rewards keep working.
*   [x] Expose `academyCourses` StateFlow and derived course/chapter progress helpers.

### Sprint 22.3: Chapter Completion & Rewards
*   [x] Implement multi-question scoring with a pass threshold (â‰¥60% correct to pass a chapter).
*   [x] Record chapter completion in the existing `completedLevels` CSV via `completeTutorialLevel` so the F&O unlock gate, missions, and certificate logic remain intact.
*   [x] Enforce tiered rewards: BEGINNER â‚¹500 / INTERMEDIATE â‚¹750 / ADVANCED â‚¹1,000 per chapter (later retuned in Sprint 22.9 for realistic position-sizing discipline).
*   [x] Keep reward idempotency (a chapter may only be claimed once).

### Sprint 22.4: Academy UI â€” Course Grouping & Progress
*   [x] Group chapter cards under collapsible course headers with course icon/emoji, tagline, and tier badge.
*   [x] Add per-course progress (chapters completed / total) and overall progress bar across all courses.
*   [x] Surface quiz pass-state ("Earned!" vs "+â‚¹") per chapter card.

### Sprint 22.5: Quiz Dialog â€” Multi-Question Flow
*   [x] Replace the single-question knowledge check with a multi-question stepper (Question X of Y).
*   [x] Show per-question explanations after answering and a final chapter score screen.
*   [x] Unlock the reward claim only on a passing score; offer Review Lectures / Retry on failure.
*   [x] Preserve the rewarded-ad "Double Reward" and premium instant-double flows for passing chapters.
*   [x] Render the `riskDisclosure` footnote (Courses 4 & 6) inside the quiz dialog above the lecture panel.

### Sprint 22.6: Tests & Verification
*   [x] Add content-schema validation tests: unique/contiguous chapter IDs, valid `correctIndex`, 3â€“5 questions/chapter, 3â€“4 lectures/chapter, tiered reward amounts.
*   [x] Add scoring/pass-threshold tests for the multi-question engine.
*   [x] Add backward-compatibility tests for legacy `academy_data.json` parsing.
*   [x] Add `riskDisclosure` coverage checks (required on Courses 4 & 6) to validation and file tests.
*   [x] Update the Authoritative Manual Verification Protocol with the Academy v2 checklist.

### Sprint 22.7: Accordion Deck, Progressive Unlock & Premium Motion
*   [x] Refactor the lessons deck into a single-open accordion (`CourseDeck`) â€” at most one course expanded at a time, chapters slide in/out via `AnimatedVisibility`.
*   [x] Add progressive course unlocking via `AcademyScoring.unlockedCourseIds` (course N requires all chapters of course Nâˆ’1 completed); locked headers dimmed + ðŸ”’.
*   [x] Add a **"read freely, earn when ready" preview model**: locked courses expand to show dimmed chapter cards (ðŸ”’ "Preview" chip instead of a reward); opening a locked chapter shows fully readable lectures with the assessment entry replaced by a locked card naming the course to finish first.
*   [x] Add "Skip Lectures â†’ Take Assessment ðŸ“" outlined shortcut in lecture mode so users can jump straight to the knowledge check.
*   [x] Move the tier badge onto its own line below the course title (long titles are ellipsized) so the badge can never wrap vertically.
*   [x] Build the reusable `PremiumMotion` animation toolkit (`ui/common/PremiumMotion.kt`): staggered entrances, neon glow pulse, shimmer progress, rotating chevron, lock-shake, one-shot sparkle burst, finite confetti â€” all gated behind `LocalPremiumMotionEnabled` for idle-safe tests.
*   [x] Add `AcademyAccordionTest` (8 Robolectric cases covering expand/collapse, single-open, locked preview states, progressive unlock) and `AcademyScreenshotTest` (Roborazzi collapsed vs expanded deck).
*   [x] Extend `AcademyScoringTest` with `unlockedCourseIds` cases (17 total) and document `PremiumMotion` in `docs/architecture.md`.

### Sprint 22.8: Academy-Aligned Missions & Claimable Rewards
*   [x] Upgrade the Missions tab to an Academy-aware reward loop: expanded `missions_data.json` catalog (7 missions) with `targetCount`/`targetCourseId` fields, including course-completion ("Course Crusher", "Derivatives Debut"), progressive-unlock ("Beyond Beginner"), and certificate ("Certified Risk Manager") missions.
*   [x] Add `AcademyScoring.evaluateMission(mission, completedSet, academyCourses, unlockedIds, stats): MissionEvaluation` â€” a pure, testable engine that replaces the inline `when` block, computing per-mission progress and completion from real course/chapter state.
*   [x] Add claimable rewards: `claimedMissions` CSV on `UserProfile`, Room migration 22â†’23 (`ALTER TABLE user_profile ADD COLUMN claimedMissions`), and idempotent `TradingRepository.claimMissionReward()` that credits `cash`/`startingCash` and writes a `ledger_entries` CREDIT row.
*   [x] Add `TradingViewModel.claimedMissions` StateFlow + `claimMission()` (feedback + confetti) and a Claim button on completed-but-unclaimed `MissionRow`s with progress bars and a "Claimed âœ“" state.
*   [x] Update certificate-card total to use the live 68-chapter academy count.
*   [x] Extend `AcademyScoringTest` with 10 `evaluateMission` cases and `TradingRepositoryTest` with 4 claim/idempotency/parse cases; document the mission engine in `docs/architecture.md`.

### Sprint 22.9: Reward Economy Retune (Anti-Inflation)
*   [x] Retune per-chapter rewards to a **mild** scale (BEGINNER â‚¹500 / INTERMEDIATE â‚¹750 / ADVANCED â‚¹1,000) in `academy_data_v2.json` and `TradingViewModel.tierReward()`, cutting the full-academy payout from â‚¹730,000 to **â‚¹53,500** (~2.1Ã— the â‚¹25,000 starting budget instead of ~29Ã—).
*   [x] Normalize legacy `academy_data.json` fallback rewards to â‚¹500 (all map to the synthetic BEGINNER course).
*   [x] Update `tierReward()` unit assertions, chapter fixtures, and the content-spec documentation to the new scale.

### Sprint 22.10: F&O Gate Fix & Vector Icon Migration
*   [x] Fix the **F&O Academic Gate dead-lock**: `AcademyScoring.fnoAcademicUnlocked()` now checks the v2 beginner chapter ids `101,102,103` (which v2 actually awards via `completeTutorialLevel`), while still honoring legacy ids `1,2,3`. Previously the gate checked only legacy ids the v2 curriculum never writes, so F&O could never unlock through normal play.
*   [x] Update the gate checklist UI in `FoDeskScreen.kt` to display the three beginner chapter titles.
*   [x] Add `fnoAcademicUnlocked` unit cases to `AcademyScoringTest` (v2 full/partial, legacy full/partial).
*   [x] Migrate decorative emoji to **26 VectorDrawables** (6 `ic_course_*` + 20 `ic_status_*`) across the quiz dialog, PRO dialog, F&O desk (TOKENS badge, MIS warning, EXECUTE button, bias labels), and Profile (level trophy, heart footer).
*   [x] Add `Trade Lab v1.5.0 (5)` version/build footer line under the Profile branding for bug-triage identification.
*   [x] Add `biasIcon(totalDelta)` drawable-selection helper to `AcademyScoring` with unit coverage.

---

## Post-Launch Maintenance & Stability (v1.8.1)
Continuous improvement based on production telemetry and crash reports.

### Sprint: Deep Stability & R8 Hardening (August 2026)
*   [x] **R8/Reflection Immunity**: Migrated from Moshi Reflection to **Moshi Codegen** (`@JsonClass`) to prevent release-only crashes caused by property renaming.
*   [x] **ProGuard Hardening**: Added explicit protection for the `:shared` module and all internal data models in the `data` and `ui` packages.
*   [x] **In-App Diagnostic Suite**: Implemented a global `UncaughtExceptionHandler` that captures fatal traces to SharedPreferences.
*   [x] **Diagnostic Viewer**: Added a secret long-press gesture on the Profile version footer to view local crash logs on user devices.
*   [x] **Database Corruption Fix**: Explicitly enabled `WRITE_AHEAD_LOGGING` in `AppDatabase` and added `.catch` handlers in `TradingRepository` flows to prevent `SQLiteDatabaseCorruptException` crashes.
*   [x] **NaN Progress Indicator Fix**: Added safety division-by-zero checks in `ProfileScreen` and `MainActivity` to prevent `IllegalArgumentException: current must not be NaN` when data is loading.



