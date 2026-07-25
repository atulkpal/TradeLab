# Multi-Platform Migration Blueprint (KMP & Next.js)

This document scopes the transition from an Android-first project to a unified multi-platform ecosystem (iOS and Web), as outlined in the deferred Epics 6 and 20.

## 1. Core Logic Portability (KMP)
The goal is to move ~80% of business logic to a shared Kotlin module.

### Layer 1: Data Persistence
- **Room KMP**: Transition `AppDatabase.kt` and all Entities to `commonMain`.
- **Target Drivers**:
    - **Android**: Existing Android driver.
    - **iOS**: Native SQLite driver via Objective-C interop.
    - **Web**: SQLite Wasm (WebAssembly) with OPFS for browser persistence.

### Layer 2: Domain & Repository
- **TradingRepository.kt**: Port directly to `commonMain`.
- **Calculators**: `DisciplineCalculator.kt` and `TradingHelper` are already pure Kotlin and ready for migration.

### Layer 3: Reactive State
- **Shared ViewModels**: Move `TradingViewModel` state machines (StateFlows) to the shared module.
- **Platform specific UIs**: Android uses Compose; iOS can choose between **Compose Multiplatform** (Max reuse) or **SwiftUI** (Max polish).

## 2. Web Sandbox Transition (Next.js)
Replace the current static web sandbox with a professional application.

### Phase A: Architecture
- **Framework**: Next.js 14+ (App Router).
- **State Management**: **Zustand** with persistence middleware.
- **UI System**: Tailwind CSS + Shadcn UI for institutional density.

### Phase B: Feature Parity
- **Chart Upgrade**: Replace SVG line charts with **Lightweight Charts** (by TradingView) for Candlestick rendering.
- **Sync Logic**: Use Firebase Firestore (already integrated) to sync portfolios between Android, iOS, and Web.

## 3. High-Level Roadmap
1.  **Step 1**: Move `Entities.kt` and `Daos.kt` to `:shared`.
2.  **Step 2**: Configure Room KMP Gradle plugin.
3.  **Step 3**: Port `TradingRepository` and verify with shared unit tests.
4.  **Step 4**: Initialize Next.js project in `website/` and hook into Firestore.
