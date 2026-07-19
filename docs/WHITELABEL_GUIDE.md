# Ashwath AI: Trade Lab Whitelabel Integration Guide
This authoritative guide serves as the step-by-step runbook for engineers who need to customize, brand, and build custom tenant versions of the **Trade Lab** application.

---

## 1. Core Architecture Overview
Trade Lab has been refactored from a monolithic codebase into a highly decoupled, feature-by-package architecture. All consumer-facing components are located inside the `com.ashwathai.tradelab.ui` module. 

A central configuration class controls features, brand details, and feature-flag masks, allowing multi-tenant builds to be compiled with minimal changes to core trading engine mechanics.

---

## 2. Feature-Flagging & Branding Setup (`WhitelabelConfig.kt`)
The primary entry point for branding adjustments and feature toggling is:  
📂 **`app/src/main/java/com/ashwathai/tradelab/ui/common/WhitelabelConfig.kt`**

```kotlin
package com.ashwathai.tradelab.ui.common

object WhitelabelConfig {
    // 1. Branding Attributes
    const val APP_NAME = "Trade Lab"

    // 2. Functional Feature Flags
    const val SHOW_ACADEMY = true      // Set false to hide Learn-to-Earn courses and quizzes
    const val SHOW_DERIVATIVES = true  // Set false to hide F&O Option Chains and Greeks desks
    const val SHOW_COMMODITIES = true  // Set false to hide MCX Commodities trading view
    const val SHOW_DIAGNOSTICS = true  // Set false to hide AI Behavior Diagnostics and profile tools
}
```

When custom variations are compiled, modifying these flags automatically adjusts navigation tabs, bottom bars, and profile configurations across the app dynamically.

---

## 3. Brand Identity Customizations (Colors & Themes)

To re-skin the application for a specific client:

### A. Color Palette (`Color.kt`)
📂 **`app/src/main/java/com/ashwathai/tradelab/ui/theme/Color.kt`**  
Trade Lab employs a premium **Sophisticated Dark** aesthetic. To update client-specific accent colors, replace the hex values for these primary variables:
```kotlin
val BrandViolet = Color(0xFF8B5CF6)  // Main primary highlight (buttons, active navigation)
val AccentGreen = Color(0xFF10B981)  // Positive trends, buys, profit indicators
val AccentRose = Color(0xFFF43F5E)   // Negative trends, sells, loss indicators
val DarkBackground = Color(0xFF09090B) // Scaffold backdrop surface
```

### B. Logo and Launch Icon Refactoring
To override the default adaptive application icon, follow these steps:
1. Generate the custom vector paths or resource files.
2. Replace the launcher icons under:
   * **Foreground vector:** `app/src/main/res/drawable/ic_launcher_foreground.xml`
   * **Background color/vector:** `app/src/main/res/drawable/ic_launcher_background.xml`
3. If necessary, adjust the `app_name` string in `app/src/main/res/values/strings.xml` to match the custom `WhitelabelConfig.APP_NAME`.

---

## 4. Packaging and Application ID Settings (Gradle)
To deploy multiple branded variants of Trade Lab to the Google Play Store concurrently, each app must have a unique `applicationId`.

📂 **`app/build.gradle.kts`**  
Ensure the `namespace` is untouched to maintain the generated `R` class directories, while changing the `applicationId` to match the target client:

```kotlin
android {
    namespace = "com.ashwathai.tradelab" // Unchanged to prevent internal path breaks
    
    defaultConfig {
        // Change this per client deployment
        applicationId = "com.aistudio.tradelab.clientname" 
        
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

---

## 5. Clean Verification Checklist
When creating a new tenant build, run this verification pipeline:

1. **Verify Config Alignment:** Update `WhitelabelConfig.kt` flags & custom strings.
2. **Build Test:** Run the build compilation check in the shell:
   ```bash
   gradle compileDebugKotlin
   ```
3. **Verify Asset Compilations:** Ensure custom vector XML files do not contain syntax errors.
4. **Compile APK:** Generate the output artifact:
   ```bash
   gradle assembleDebug
   ```
