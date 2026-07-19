# Firebase Authentication Integration Guide: TradeLab

This document outlines the detailed step-by-step instructions for setting up the **Firebase** project and configuring Google Sign-In, Email/Password, and Phone (OTP) authentication for the TradeLab application.

TradeLab is pre-configured with **Firebase Auth SDKs** (Email/Password, Google Sign-In, Phone Auth, and Android Credentials Manager). It also contains a graceful **Sandbox Simulation fallback mode** so you can test all user journeys immediately even before connecting your live Firebase project!

---

## Part 1: Firebase Console Setup

To configure authentication, you will need to create a project in the Firebase Console:

### Step 1: Create a Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click on **Add project** (or **Create a project**).
3. Enter your project name (e.g., `TradeLab` or `tradelab-simulator`).
4. Select whether to enable Google Analytics (recommended but optional) and click **Create project**.

### Step 2: Register the Android App
1. Inside your Firebase project home dashboard, click on the **Android icon** to add a new Android application.
2. Enter the **Android Package Name**: 
   ```text
   com.ashwathai.tradelab
   ```
   *(This must match the exact namespace specified in your `app/build.gradle.kts`)*.
3. Enter an optional App nickname (e.g., `TradeLab Android`).
4. **(CRITICAL FOR GOOGLE & PHONE AUTH)** Add your **SHA-1** fingerprint:
   - In your development environment or terminal, run the Gradle signing report command to find your local development SHA-1 certificate fingerprint:
     ```bash
     gradle :app:signingReport
     ```
   - Copy the `SHA-1` hex string and paste it into the **Debug signing certificate SHA-1** field.
5. Click **Register app**.

### Step 3: Download & Replace `google-services.json`
1. Download the generated `google-services.json` configuration file from the wizard.
2. In your TradeLab codebase explorer, navigate to the following folder:
   ```text
   /app/google-services.json
   ```
3. Overwrite the dummy template file with your downloaded `google-services.json`.

### Step 4: Add the Firebase Gradle Plugins (Already Configured!)
*Note: This is already completed for you in the codebase!* 
* The `com.google.gms.google-services` plugin is activated in `/app/build.gradle.kts`.
* We've also set up `googleServices.missing.passthrough = true` inside `/gradle.properties` to ensure the project compiles successfully regardless of whether your real `google-services.json` has been placed yet.

---

## Part 2: Enable Sign-In Providers in Firebase Auth

1. In the left-hand navigation sidebar of the Firebase Console, go to **Build** > **Authentication**.
2. Click **Get Started** to initialize Firebase Authentication.
3. Select the **Sign-in method** tab.

### 1. Configure Email/Password Provider
1. Under **Native providers**, click on **Email/Password**.
2. Enable **Email/Password** (keep *Email link (passwordless sign-in)* disabled unless needed).
3. Click **Save**.

### 2. Configure Google Sign-In Provider
1. Under **Additional providers**, click on **Google**.
2. Toggle **Enable**.
3. Configure your public-facing support email.
4. **Web SDK Configuration:**
   - Under *Web SDK configuration*, Firebase automatically generates a **Web Client ID** and **Web Client Secret** because of your SHA-1.
   - Note down this Web Client ID! You will need it to configure Google Sign-In with Android Credential Manager.
5. Click **Save**.

### 3. Configure Phone Auth (OTP) Provider
1. Click **Add new provider** and choose **Phone**.
2. Toggle **Enable**.
3. *(Optional for Testing)* Expand **Phone numbers for testing** to configure custom mock OTP numbers (e.g., `+91 9999999999` with code `123456`) so you can run end-to-end tests without wasting real SMS credits.
4. Click **Save**.

---

## Part 3: Codebase Verification and Execution Details

TradeLab is fully architected to utilize **Unidirectional State Flow (MVI/MVVM)**. 

### How Authentication Integrates in Code:

1. **User Profile Persistence:**
   The `UserProfile` Room Entity in `Entities.kt` keeps track of the active user session locally.
   ```kotlin
   @Entity(tableName = "user_profiles")
   data class UserProfile(
       @PrimaryKey val id: Int = 1,
       val name: String,
       val email: String,
       val currency: String = "INR",
       val isPremium: Boolean = false,
       val isLoggedIn: Boolean = false,
       val isArcadeMode: Boolean = false,
       val currentBalance: Double = 10000.0,
       // ... other simulation values
   )
   ```

2. **Triggering the Auth Gate:**
   In `MainActivity.kt`, the application listens to `viewModel.userProfile` and `viewModel.hasDismissedAuthScreen`. If the user is not authenticated and has not chosen Guest Mode, it intercepts the app launch and presents the premium `AuthScreen`:
   ```kotlin
   if (userProfile?.isLoggedIn != true && !hasDismissedAuthScreen) {
       AuthScreen(viewModel = viewModel)
   } else {
       MainContent(viewModel = viewModel)
   }
   ```

3. **Sandbox Mode Fallback (Simulated Mode):**
   If you launch the app without having fully registered the live Google Firebase client on your console yet, `AuthScreen.kt` catches the exception gracefully and opens a **Firebase Sandbox Dialog**. This allows you to simulate successful authentication instantly so that:
   - Your layouts remain 100% interactive.
   - Your trial limits get successfully unlocked.
   - You can test premium indicators (EMA/RSI), custom live vector charts, and transaction ticket orders without any backend blocker!

---

## Part 4: Testing Your Setup

Once you replace the `google-services.json` file with your real one:

1. Run the compilation check to verify there are no configuration errors:
   ```bash
   gradle :app:assembleDebug
   ```
2. Launch the TradeLab app on your device or streaming emulator.
3. Test registering a user via **Email/Password** or logging in with **Phone (OTP)**. The user profile will synchronize locally, allowing real-time trading instantly!
