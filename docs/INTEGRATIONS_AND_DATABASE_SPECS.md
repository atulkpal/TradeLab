# Ashwath AI: Trade Lab Core Integrations & Database Reference

This master document outlines all dynamic configurations, table IDs, keys, ad unit mappings, and code integrations for **AdMob, Firebase, and the local Room Database** in Trade Lab. 

---

## 1. AdMob Integration Reference

Trade Lab integrates **Google Mobile Ads SDK (AdMob)** using Rewarded Video Ads to reward users with virtual capital, premium indicators, emergency cash, and options tokens.

### A. Initialization & Setup
The SDK is initialized in the `onCreate` method of `MainActivity.kt`:
```kotlin
import com.google.android.gms.ads.MobileAds

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    MobileAds.initialize(this) {}
}
```

### B. AdMob IDs & Reward Map
*   **Test Rewarded Ad Unit ID:** `ca-app-pub-3940256099942544/5224354917` (Used automatically in debug builds via `BuildConfig.DEBUG` checks).
*   **Client Production Publisher / App ID:** Set in `AndroidManifest.xml` (e.g. `ca-app-pub-4153575596488132`).

#### Production Ad Unit Mappings and Reward Outcomes:

| AdType (Enum) | Ad Unit ID (Production) | Screen / Trigger Source | Reward Points / Amount | Target Method (ViewModel) |
| :--- | :--- | :--- | :--- | :--- |
| **`ACADEMY_DOUBLE`** | `ca-app-pub-4153575596488132/9800275450` | Academy (Quiz Completion) | **2x Virtual Capital** (Doubles current quiz reward) | `viewModel.completeTutorial(quizId, rewardAmt * 2)` |
| **`PORTFOLIO_SHIELD`** | `ca-app-pub-4153575596488132/8679333696` | Portfolio Screen (Shield Toggle) | **50 Brokerage Credits** | `viewModel.earnBrokerageCredits(50)` |
| **`PROFILE_EMERGENCY_CASH`** | `ca-app-pub-4153575596488132/5777269963` | Profile / Cash Card | **₹500 / $500** Virtual Cash | `viewModel.earnEmergencyCash(500.0)` |
| **`PROFILE_SHIELD_MAX`** | `ca-app-pub-4153575596488132/2225726124` | Profile / Subscription Panel | **100 Brokerage Credits** | `viewModel.earnBrokerageCredits(100)` |
| **`PROFILE_AI_ADVISOR`** | `ca-app-pub-4153575596488132/5861598661` | Profile / AI Coach panel | **+1 AI Audit Credit** | `viewModel.earnAiAuditCredit()` |
| **`PROFILE_INDICATORS`** | `ca-app-pub-4153575596488132/9988545931` | Profile / SMA & RSI Toggles | **24-Hour Premium Indicator Access** | `viewModel.unlockPremiumIndicators(24)` |

### C. Standard Invocation Code Snippet
All ads are dynamically requested, pre-cached, and shown using `MainActivity`'s unified handler:
```kotlin
fun loadAndShowRewardedAd(
    adType: AdType,
    onAdLoaded: () -> Unit,
    onAdFailed: (String) -> Unit,
    onUserEarnedReward: () -> Unit
) {
    val adUnitId = getAdUnitId(adType)
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
        this,
        adUnitId,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                onAdFailed(adError.message)
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                onAdLoaded()
                rewardedAd.show(this@MainActivity) {
                    onUserEarnedReward()
                }
            }
        }
    )
}
```

---

## 2. Firebase Integration Reference

Trade Lab uses Firebase for secure user registration, email-password authentication, and dynamic state bindings. 

### A. Authentication Providers
To operate successfully, configure the following inside your **Firebase Console**:
1.  **Email/Password Provider:** Enable inside the **Authentication -> Sign-in Method** tab.
2.  **Google Sign-In:** Uses the modern **Credential Manager API**. Requires Web Client ID and SHA-1 registration.
3.  **Phone Authentication:** Enabled for SMS OTP verification.

### B. Production Authentication Implementation
The app uses a hybrid logic in `AuthScreen.kt` to ensure seamless development and secure production:
*   **Debug builds:** Trigger a **Sandbox Simulation Dialog** for instant UI verification without real OTPs.
*   **Release builds:** Trigger the **Real SDKs** (Credential Manager for Google, PhoneAuthProvider for SMS).

```kotlin
// Hybrid toggle example
if (firebaseAuth == null || BuildConfig.DEBUG) {
    // Show Simulation Dialog
} else {
    // Launch Real SDK Flow
}
```

### C. Registration & Login Pipeline
The credentials handler operates inside a unidirectional coroutine launch flow:

```kotlin
coroutineScope.launch {
    if (firebaseAuth == null) {
        // Fallback: Activate offline simulated Profile Sandbox
        sandboxUserToSimulate = Pair(nameInput.ifBlank { emailInput.substringBefore("@") }, emailInput)
        showSandboxDialog = true
    } else {
        try {
            if (isRegisterMode) {
                firebaseAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                    .addOnSuccessListener { authResult ->
                        val displayName = nameInput.ifBlank { emailInput.substringBefore("@") }
                        viewModel.registerOrLogin(displayName, emailInput)
                    }
            } else {
                firebaseAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                    .addOnSuccessListener { authResult ->
                        val displayName = authResult.user?.displayName ?: emailInput.substringBefore("@")
                        viewModel.registerOrLogin(displayName, emailInput)
                    }
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Authentication operation failed."
        }
    }
}
```

---

## 3. Room SQLite Local Database Reference

Trade Lab stores all local transactions, watchlists, active options portfolios, custom indices, and quiz histories on the device using a high-performance **Room Database Engine** styled with modern Kotlin Coroutines (`Flow`).

*   **Database Class Location:** `com.ashwathai.tradelab.data.AppDatabase`
*   **Initialization:** `Room.databaseBuilder(context, AppDatabase::class.java, "tradelab_database")`

### Database Table Schemas & Properties

The database holds 9 unique table entities mapped directly from `Entities.kt`:

#### 1. `user_profile` (Holds account parameters and earned currency credits)
*   **Primary Key:** `id: Int = 1` (Single-user client constraint)
*   **Fields:**
    *   `cash: Double` (Current virtual spending budget in ₹ or $)
    *   `startingCash: Double` (Starting portfolio limit, e.g. ₹10,000)
    *   `riskPreference: String` ("Conservative", "Moderate", or "Aggressive" - sets limits on leverage)
    *   `currency: String` ("INR" or "USD")
    *   `completedLevels: String` (Comma-separated list of quiz level IDs cleared)
    *   `isArcadeMode: Boolean` (Bypasses rules to practice reckless trades)
    *   `trialActionsCount: Int` (Limits operations for free trial bounds)
    *   `isLoggedIn: Boolean` (Sets custom visual interface state)
    *   `userName: String` (Display name of user)
    *   `userEmail: String` (Associated account email)
    *   `isPremium: Boolean` (Enables F&O premium features)
    *   `brokerageCredits: Int` (Allows fee-free transactions)
    *   `indicatorsUnlockedUntil: Long` (Epoch timestamp for advanced SMA/RSI charts)
    *   `aiAuditCredits: Int` (Determines count of remaining AI Advisor queries)
    *   `fnoTokens: Int` (Tokens available to trade options)

#### 2. `holdings` (Active asset ownership)
*   **Primary Key:** `symbol: String` (e.g. "RELIANCE", "AAPL")
*   **Fields:**
    *   `shares: Double` (Quantity owned. Supports fractionals)
    *   `averagePrice: Double` (Cost basis for dynamic PnL calculations)

#### 3. `transactions` (Historic audit ledger)
*   **Primary Key:** `id: Int` (Auto-generated auto-incrementing integer)
*   **Fields:**
    *   `symbol: String` (Ticker)
    *   `type: String` ("BUY" or "SELL")
    *   `shares: Double` (Volume)
    *   `price: Double` (Execution price)
    *   `timestamp: Long` (Time of transaction in milliseconds)

#### 4. `watchlist` (Fast tracking references)
*   **Primary Key:** `symbol: String`
*   **Fields:**
    *   *No additional fields. Acts as a raw filter list.*

#### 5. `watchlist_names` (Multiple watchlist support)
*   **Primary Key:** `id: Int` (Valid values: 1 to 5)
*   **Fields:**
    *   `name: String` (Custom watchlist category headers, e.g., "Tech Stocks", "My Faves")

#### 6. `watchlist_items_v2` (Multi-watchlist items association)
*   **Composite Primary Key:** `[watchlistId: Int, symbol: String]`
*   **Fields:**
    *   *Direct relational join table.*

#### 7. `stock_prices` (Live simulated pricing parameters)
*   **Primary Key:** `symbol: String`
*   **Fields:**
    *   `companyName: String`
    *   `currentPrice: Double`
    *   `dailyChangePct: Double`
    *   `previousClose: Double`
    *   `highPrice: Double`
    *   `lowPrice: Double`
    *   `historyData: String` (Comma-separated historical values, e.g. `"152.0,154.2,153.1"`, dynamically rendered by native Canvas charts)

#### 8. `pending_orders` (Advanced order types)
*   **Primary Key:** `id: Int` (Auto-generated)
*   **Fields:**
    *   `symbol: String`
    *   `type: String` ("BUY" or "SELL")
    *   `orderType: String` ("Limit", "GTT", or "Stop-Loss")
    *   `shares: Double`
    *   `triggerPrice: Double`
    *   `status: String` ("PENDING", "EXECUTED", "CANCELLED")
    *   `timestamp: Long`

#### 9. `app_notifications` (Localized trade logs and updates)
*   **Primary Key:** `id: Int` (Auto-generated)
*   **Fields:**
    *   `message: String`
    *   `timestamp: Long`
    *   `isRead: Boolean`

---

## 4. Google Play Billing Integration

Trade Lab implements the **Google Play Billing SDK (v7.1.1+)** to manage premium subscriptions and digital entitlements.

### A. Subscription Configuration
*   **Target Product ID:** `tradelab_pro_monthly`
*   **Manager Class:** `com.ashwathai.tradelab.billing.BillingManager`

### B. Billing Lifecycle
1.  **Initialization:** The `BillingManager` is instantiated in `MainActivity` and automatically establishes a connection to Google Play.
2.  **Product Query:** The app queries `queryProductDetails` to fetch the latest localized pricing and trial information.
3.  **Purchase Flow:** In **Release builds**, `billingManager.startBillingFlow()` launches the official Google Play sheet. In **Debug builds**, the app displays a high-fidelity **Simulated Billing Dialog** with mock UPI/Card inputs.
4.  **Entitlement Granting:** Upon a successful `PURCHASED` response (or mock success), the app calls `viewModel.completePremiumPurchase()` which updates the local `user_profile.isPremium` flag.
5.  **Acknowledgement:** Real purchases are acknowledged via `acknowledgePurchase` within 3 days to prevent automatic refunds.

### C. Pro Entitlements Map
| Feature | Entitlement Check |
| :--- | :--- |
| **Zero Brokerage** | `if (isPremium) // Waive 20 credit fee` |
| **F&O Desk** | `if (isPremium) // Unlock Derivatives tab` |
| **AI Advisor** | `if (isPremium) // Unlimited Gemini queries` |
| **Watchlists** | `if (isPremium) // Support up to 5 renamable sheets` |
