# Walkthrough - Global Leaderboard with Firestore

I have successfully integrated **Cloud Firestore** to power a real-time, global leaderboard for **TradeLab**. Your testers can now compete and see their ranks updated instantly as they complete quizzes and grow their portfolios.

## Key Accomplishments

### 1. Dynamic Leaderboard Engine
I created [LeaderboardManager.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/data/LeaderboardManager.kt) which handles:
- **Stat Syncing:** Automatically pushes the user's name, XP (level progress), and total portfolio value to the `leaderboard` collection in Firestore.
- **Real-time Ranking:** Fetches the top 20 players globally and assigns ranks (Rank #1, #2, etc.) based on their XP.

### 2. ViewModel Integration
I updated [TradingViewModel.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/TradingViewModel.kt) to:
- Monitor the user's progress and portfolio changes.
- Automatically trigger a sync to the cloud whenever progress is made.
- Expose the global leaderboard data as a reactive stream for the UI.

### 3. UI Update (Academy Tab)
I refactored the **Leaderboard** sub-tab in [AcademyScreen.kt](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/app/src/main/java/com/ashwathai/tradelab/ui/academy/AcademyScreen.kt) to replace the old static list with the live data from Firestore. It now highlights the user in purple so they can easily find themselves in the global rankings.

## REQUIRED: Firestore Security Rules

To ensure your database is secure in Production Mode, please follow these steps:

1.  Open the [Firebase Console](https://console.firebase.google.com/).
2.  Go to **Firestore Database** > **Rules** tab.
3.  **Paste** the following rules and click **Publish**:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // Leaderboard rules
    match /leaderboard/{userId} {
      // Anyone can see the leaderboard
      allow read: if true;

      // Only the user themselves can update their own score
      // Note: We are using the email as the document ID for simplicity in this version
      allow write: if request.auth != null && request.auth.token.email == userId;
    }
  }
}
```

## Verification Results
- **Build Status:** Success (`assembleDebug` passed).
- **Data Integrity:** Users who are not logged in (Guests) will still see their local progress, but only authenticated users will appear on the global leaderboard.

> [!TIP]
> This setup provides a strong incentive for testers to register an account so they can claim their spot on the global TradeLab Arena!
