# Trade Lab — Agent Onboarding Summary

I have completed the authoritative onboarding process for **Trade Lab** by Ashwath AI. I have internalized the project architecture, features, and workflow rules.

## Core Knowledge internalized

- **Package ID:** `com.ashwathai.tradelab`
- **Philosophy:** Realistic paper trading (₹10,000 start) with a "Learn-to-Earn" loop.
- **Tech Stack:** Android, Kotlin, Jetpack Compose, Room, Hilt, Firebase (Auth/Firestore/AdMob/Crashlytics).
- **Project Structure:**
    - `:app`: Primary Android application.
    - `:shared`: KMP-ready core logic.
    - `docs/`: Extensive documentation (Architecture, Epics, PRD).
- **Monetization:** Hybrid model (Rewarded Ads + Google Play Subscriptions).
- **Aesthetics:** High-contrast **Sophisticated Dark** theme with dynamic sub-modes.

## Current Project Status (v1.7.0 / Build 6)

- **Phase 1 & 2:** 100% Complete. Core engine, orders, and academy are stable.
- **Monetization:** v1.5.0 launched real Google Play products with early-bird promos.
- **Personalization:** v1.7.0 launched the 5-mode Theme Engine and Focus Suite.
- **Hotfixes:** Previous hotfixes addressed DB corruption and NaN crashes, but new stability issues reported in v1.8.0.

## Workflow Commitments

1. **Branch Discipline:** Use `develop` for all engineering work.
2. **Spec-First:** Update `docs/epics_and_sprints.md` before implementation.
3. **Skill-Driven:** Invoke appropriate skills from `skills/` (Activated: `debugging-and-error-recovery`).
4. **Testing:** Maintain high test coverage and avoid infinite loops in `ViewModel.init`.
5. **Release Policy:** No version bumps without explicit user approval.

---

**Onboarding Status:** 🟢 COMPLETE
**Active Task:** Triage and fix Release Crashes + Crashlytics reporting gap.
