# Authoritative Manual Verification Protocol: Track C (Social Discipline)

This document provides a step-by-step checklist for testers to verify the functional integrity of **Track C (Epic 19)** features.

## 1. Investor Maturity Algorithm
- [ ] **Test Case 1.1: Small Trade Reward**
    - **Action**: Execute a BUY order for RELIANCE with quantity such that total cost is < 10% of current cash.
    - **Expected Result**: Discipline Score should increase by ~2 points (viewable on Profile).
- [ ] **Test Case 1.2: Oversized Trade Penalty**
    - **Action**: Execute a BUY order for TCS with quantity such that total cost is > 20% of current cash.
    - **Expected Result**: Discipline Score should decrease (approx -2 to -10 points).
- [ ] **Test Case 1.3: Diversification Bonus**
    - **Action**: Add and BUY stocks from 3 distinct sectors (e.g., RELIANCE - Energy, TCS - IT, HDFCBANK - Banking).
    - **Expected Result**: Discipline Score should receive a +5 diversification bonus.

## 2. Global Leaderboards
- [ ] **Test Case 2.1: Sort by Wealth**
    - **Action**: Open Academy -> Leaderboard. Ensure toggle is on "Wealth (XP)".
    - **Expected Result**: Users are sorted by total XP descending.
- [ ] **Test Case 2.2: Sort by Maturity**
    - **Action**: Click "Maturity (Score)" on the toggle.
    - **Expected Result**: List refreshes. Users are now sorted by Discipline Score descending.
- [ ] **Test Case 2.3: Bot Visibility**
    - **Action**: Ensure "👑 TradeLab Bot" appears if global data is sparse.

## 3. Social & Viral Hooks
- [ ] **Test Case 3.1: Share My Score**
    - **Action**: Go to Profile. Click the Share icon next to the Discipline Score.
    - **Expected Result**: Android Share Sheet opens with a message containing your specific score.
- [ ] **Test Case 3.2: Challenge a Friend**
    - **Action**: Go to Academy -> Leaderboard. Click "Challenge a Friend" card.
    - **Expected Result**: Android Share Sheet opens with a Trade Lab invite link and hype text.

## 4. Discipline Badges
- [ ] **Test Case 4.1: Sizing Master**
    - **Action**: Use the Profiler to reset with ₹10,000. Maintain high discipline until score >= 90.
    - **Expected Result**: "Sizing Master" badge should appear in the Profile badge row.
