# Authoritative Manual Verification Protocol (AMVP)

This protocol defines the checklist required for human testers or automated screenshot agents to verify the functional and visual integrity of Trade Lab features.

---

## 1. Hyper-Personalization & Focus Suite (Epic 24)

### 🕶️ Stealth Mode (Privacy)
- [ ] **Toggle**: Go to Profile -> Focus & Privacy. Enable "Stealth Mode".
- [ ] **Verification (Portfolio)**: Main "Total Portfolio Value" and P&L numbers must be blurred.
- [ ] **Verification (Watchlist)**: Ticker prices and order values in the Buy/Sell sheet must be blurred.
- [ ] **Persistence**: Restart the app. Stealth Mode must remain enabled.

### 🧘 Zen Mode (Focus)
- [ ] **Toggle**: Go to Profile -> Focus & Privacy. Enable "Zen Mode".
- [ ] **Verification (Cleanliness)**: The News Ticker (below header) must disappear.
- [ ] **Verification (Movers)**: The "Movers Marquee" must be hidden from the Home/Portfolio screen.
- [ ] **Persistence**: Navigate between tabs. News/Movers must remain hidden.

### 📟 Terminal Mode (Monospace)
- [ ] **Selection**: Go to Profile -> Theme Mode. Select "Terminal".
- [ ] **Verification (Fonts)**: All text across the app must switch to a Monospace font.
- [ ] **Verification (Vibe)**: Background must be pure black (`#000000`) with neon green text.
- [ ] **Legibility**: Ensure tickers and numbers are perfectly aligned and readable.

### 🕹️ Arcade Mode (Synthwave)
- [ ] **Selection**: Go to Profile -> Theme Mode. Select "Arcade".
- [ ] **Verification (Palette)**: App should shift to a Pink/Blue/Purple neon aesthetic.
- [ ] **Verification (Charts)**: Open any stock chart. Horizontal "CRT Scanlines" must be visible as an overlay.

---

## 2. Core Trading & Realism (Regressions)

### 📈 Buy/Sell Execution
- [ ] **Market Order**: Execute a 100-share BUY Market order. Verify Cash decreases and Holding appears.
- [ ] **Square Off**: Use the "Quick Square Off" button in the position row. Verify position is removed and cash settled.

### settlement
- [ ] **T+1 Logic**: Buy a stock in CNC mode. Verify it appears in "Positions" (T1) and not yet in "Holdings" (Settled).

---

## 3. Academy & Missions

### 🎓 Quiz Flow
- [ ] **Multi-Question**: Complete a chapter quiz. Verify Question 1/3 -> 2/3 flow.
- [ ] **Reward**: Claim a reward upon passing. Verify confetti trigger and cash increase.
