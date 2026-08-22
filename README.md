# Trade Lab 📈

**Trade Lab** is a realistic, offline-first paper trading simulator designed specifically for beginners and retail practitioners (16–35+ years old). It is designed to teach disciplined position sizing, risk management, and market mechanics using virtual budgets denominated in Indian Rupees (₹) and US Dollars ($).

Unlike traditional trading simulators that encourage reckless "fantasy high-roller" behaviors with millions of mock dollars, Trade Lab instills real-world investing habits by starting users with realistic budgets (e.g., ₹10,000) and enforcing strict transactional boundaries.

---

## 🌟 Key Features

*   **Psychological Profiler:** A tailored 60-second onboarding questionnaire that aligns your virtual capital sizes directly with real-world target budgets, building practical investing habits from day one.
*   **Dynamic Simulation Canvas:** Real-time interactive price fluctuation engine that renders custom, animated vector stock charts on-the-fly using native Android `Canvas`.
*   **Unidirectional State Engine:** A centralized `TradingViewModel` managing real-time states for watchlists, portfolio values, active positions, and mock market prices.
*   **Buy/Sell Order Tickets:** Real-time transactional validation verifying cash-on-hand before execution to prevent over-leveraging.
*   **Learn-to-Earn (Upcoming):** Acquire more virtual capital to trade by completing educational modules and finance quizzes.

---

## 🎨 Visual Identity & Theme

Trade Lab features a premium, high-contrast **Sophisticated Dark** theme. Styled with bright neon accent highlights, modern typography, and generous layout spacing, it is optimized for high-readability and visual scanning.

---

## 🛠️ Technology Stack & Architecture

Built using modern Android development best practices and guidelines:

*   **User Interface:** 100% Kotlin & **Jetpack Compose** with Material Design 3 (M3).
*   **Data Persistence:** Local SQLite database managed securely through **Room Database** with Kotlin Symbol Processing (KSP).
*   **State Management:** Architecture following **MVVM** (Model-View-ViewModel) with structured, unidirectional data streams using Kotlin `StateFlow`.
*   **Local JVM Testing:** Built-in unit and screenshot tests powered by **Robolectric** and **Roborazzi**.

---

## 🚀 Getting Started

To run or build the application locally:

```bash
# Verify compilation and build status
gradle assembleDebug

# Run unit and local integration tests
gradle :app:testDebugUnitTest
```

---

## 🎬 NLM Video Pipeline (Academy Content)

TradeLab's Academy content is powered by an automated video generation pipeline using **Google NotebookLM**:

- **204 lectures** across 6 courses, 68 chapters
- **7 Google accounts** for parallel generation
- **SHORT format** videos (~60-90s) with branded intro/outro
- **Download-once, play-locally** model with external cache
- **Firebase Storage** for hosting, AdMob for monetization

### Pipeline Stages
1. **Extract** → Lecture content from `academy_data_v2.json` → `.md` files
2. **Create** → NotebookLM notebooks + source upload (7 accounts, parallel)
3. **Generate** → SHORT format videos via NotebookLM API
4. **Download** → MP4s to local `nlm/assets/`
5. **Process** → Add branded intro/outro via ffmpeg
6. **Upload** → Firebase Storage with public URLs
7. **Integrate** → Update `academy_data_v2.json` with `videoUrl`

### Current Status (2026-08-22)
| Metric | Count |
|--------|-------|
| Total Lectures | 204 |
| Notebooks Created | 133 / 196 (65%) |
| Videos Downloaded | 25 (8 unique lectures) |
| Unique Lectures with Video | 10 |

### Key Scripts
| Script | Purpose |
|--------|---------|
| `extract_all_courses_fixed.py` | Extract 204 lectures to `.md` |
| `create_missing_notebooks.py` | Create notebooks (parallel, 7 accounts) |
| `batch_generate_multi.py` | Generate videos (quota-aware) |
| `download_all_videos.py` | Download completed videos |
| `process_videos.py` | Add intro/outro via ffmpeg |
| `upload_to_firebase.py` | Upload to Firebase Storage |

### Documentation
- [Pipeline Overview](nlm/docs/PIPELINE.md)
- [Status Sheet](nlm/docs/STATUS_SHEET.md)
- [Script Inventory](nlm/docs/SCRIPT_INVENTORY.md)
- [Quota Management](nlm/docs/QUOTA_MANAGEMENT.md)
- [Troubleshooting](nlm/docs/TROUBLESHOOTING.md)

---

## ❤️ About Ashwath AI

Trade Lab is designed and developed with precision as part of the Ashwath AI suite of tools.

Built with ❤️ by **Ashwath AI**
*Building free, open-source software, AI, and games for everyone.*
