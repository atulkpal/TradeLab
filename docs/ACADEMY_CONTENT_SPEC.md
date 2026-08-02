# TradeLab Academy v2 — Content Authoring Spec

**Status:** v1.0 · **For:** Content Authoring Agent · **Output:** `app/src/main/assets/academy_data_v2.json`

## 1. Purpose

TradeLab is an Android paper-trading simulator that teaches disciplined retail trading via a "Learn-to-Earn" Academy: users read bite-size lessons, pass knowledge checks, and earn virtual ₹ capital. We are expanding from 8 flat modules into a **Varsity-style curriculum**: 6 courses → ~68 chapters, each chapter = lectures + multi-question quiz.

> ⚠️ **Copyright:** This content must be **100% original**. The structure is *inspired by* Zerodha Varsity's module layout, but you must NOT copy, paraphrase, or reproduce any Varsity text, examples, or phrasing. Write every sentence yourself. If in doubt, write it fresh.

## 2. Deliverable Schema (JSON — this exact shape, no deviations)

```json
{
  "version": 2,
  "courses": [
    {
      "id": 1,
      "title": "Stock Market Basics",
      "tagline": "How markets work, what a stock is, and how trades settle.",
      "iconEmoji": "📈",
      "tier": "BEGINNER",
      "order": 1,
      "chapters": [
        {
          "id": 101,
          "title": "Chapter 1.1: What is a Stock?",
          "topic": "Equities Fundamentals",
          "concept": "One-sentence summary shown on the chapter card.",
          "lectures": [
            { "title": "Lecture 1.1.1: Fractional Ownership", "content": "… ~75–110 words …" }
          ],
          "quizzes": [
            {
              "question": "What does buying a stock represent?",
              "options": ["A loan the company must repay", "A unit of fractional ownership", "A free-product coupon"],
              "correctIndex": 1,
              "explanation": "A share represents a fractional ownership claim on the company's assets and earnings."
            }
          ],
          "rewardAmt": 500.0
          // "riskDisclosure": "… required on every Course 4 (F&O) and Course 6 (Taxation) chapter; rendered as a quiz-dialog footnote …"
        }
      ]
    }
  ]
}
```

## 3. IDs — reserved numbering (do not change)

| Course | Title | Tier | Chapter IDs |
|---|---|---|---|
| 1 | Stock Market Basics | BEGINNER | 101–112 |
| 2 | Technical Analysis | INTERMEDIATE | 201–212 |
| 3 | Fundamental Analysis | INTERMEDIATE | 301–310 |
| 4 | Futures & Options | ADVANCED | 401–412 |
| 5 | Risk Management & Trading Psychology | INTERMEDIATE | 501–512 |
| 6 | Markets & Taxation | ADVANCED | 601–610 |

- Chapter IDs are **globally unique**, one chapter per ID, contiguous within each course (no gaps, no reordering).
- `rewardAmt`: BEGINNER = `500.0`, INTERMEDIATE = `750.0`, ADVANCED = `1000.0`. Rewards are deliberately modest against the ₹25,000 starting budget (full academy ≈ ₹53,500) to preserve realistic position-sizing discipline; per-chapter claims are tuned by tier, not inflated by course length.

## 4. Chapter structure rules

- **3–4 lectures per chapter.** Each lecture `content` is plain text, **~75–110 words** (concise and mobile-friendly), self-contained (readable without the other lectures).
- **3–5 quiz questions per chapter.** Exactly one `correctIndex` (0-based). Every question must have an `explanation` (2–3 sentences teaching why the answer is correct).
- **Question quality:** distractors must be plausible but clearly wrong; never trivial, never trick questions with ambiguous wording. One question must apply the concept to a concrete ₹/NSE/BSE scenario.
- `concept` ≤ 200 characters; shown on the course/chapter list card.
- `topic` = short category label (e.g., "Order Types", "Oscillators").

## 5. Tone & content rules

- **Audience:** Indian retail beginners 16–35. Simple, confident, non-patronizing.
- **Indian realism everywhere:** use ₹, NSE/BSE, Nifty 50, T+1 settlement, STT, 3:30 PM IST close, Indian company examples (RELIANCE, TCS, HDFCBANK, INFY, ITC — use sparingly and vary them).
- **Discipline-first:** every course should reinforce position sizing, risk limits, and emotional control. **Never** imply guaranteed returns, quick riches, or "sure-shot" setups.
- **Risk disclosure:** Course 4 (F&O) and Course 6 (Taxation) chapters must each carry a `riskDisclosure` one-liner (derivatives are high-risk; tax advice is informational, not professional). The app renders this as a footnote in the chapter's quiz dialog and `AcademyScoring.validateCourses()` rejects chapters that omit it.
- **No markdown/HTML/emoji inside lecture text** (except the `iconEmoji` field on courses). Plain sentences. Use ₹ and % symbols only.
- **Continuity:** chapters within a course build on each other; reference prior chapters ("as we saw in the previous chapter…") without creating hard dependencies.

## 6. Course outlines (author against these; scope must not bleed across courses)

### Course 1 — Stock Market Basics (12 chapters)
1. What is a Stock — fractional ownership, capital appreciation, dividends
2. Why Companies Go Public & IPOs
3. Stock Exchanges & Indices — NSE/BSE, Nifty 50, Sensex, Bank Nifty
4. Brokers, Demat & Trading Accounts — the intermediaries in India
5. Placing Orders — Market vs Limit vs GTT
6. Settlement Cycles — T+1, T2 holdings, intraday vs delivery
7. Reading a Stock Quote — OHLC, volume, 52-week range, market cap
8. Candlestick Anatomy — body, wicks, green/red
9. Market Hours & Trading Sessions — IST timings, auction, holiday calendar
10. Costs of Trading — brokerage, STT, GST, stamp duty (overview)
11. Position Sizing — the 10–15% allocation rule
12. Diversification Across Sectors

### Course 2 — Technical Analysis (12 chapters)
1. What is Technical Analysis (and its limits)
2. Price Action & Timeframes (15m/1H/4H/1D/1W)
3. Support & Resistance
4. Trendlines & Trend Direction
5. Moving Averages — SMA & EMA
6. RSI — Relative Strength Index
7. MACD
8. Candlestick Patterns — hammer, engulfing, doji
9. Volume & Volume Spikes
10. Choosing the Right Timeframe
11. Combining Indicators (avoid indicator soup)
12. Common TA Mistakes & Whipsaws

### Course 3 — Fundamental Analysis (10 chapters)
1. What is Fundamental Analysis
2. The Three Financial Statements
3. The Balance Sheet
4. The Income Statement (P&L)
5. The Cash Flow Statement
6. EPS & Net Profit Quality
7. The P/E Ratio
8. P/B, ROE & Return Ratios
9. Debt & Leverage (Debt-to-Equity)
10. Qualitative Factors & Economic Moats

### Course 4 — Futures & Options (12 chapters)
1. Introduction to Derivatives
2. Futures Contracts Basics
3. Margins & Leverage — MIS, NRML, 5x intraday
4. Futures Pricing & Mark-to-Market
5. Options: Call & Put Basics
6. Option Premium, Intrinsic & Time Value
7. Option Payoffs & Break-even
8. The Greeks — Delta (intro)
9. Option Strategies — covered call, bull call spread
10. Expiry, Square-off & Settlement
11. Risks of Leverage & Auto-Liquidation
12. Hedging vs Speculation in F&O

### Course 5 — Risk Management & Trading Psychology (12 chapters)
1. Why Risk Comes First
2. Position Sizing Models
3. Stop-Loss & Trailing Stops
4. Risk-to-Reward Ratio
5. Drawdowns & Recovery Math
6. The 1% Rule
7. FOMO & Greed Biases
8. Revenge Trading & Overtrading
9. Discipline Score & Habit Loops
10. Journaling & Trade Review
11. Knowing When to Step Away
12. Building a Personal Trading Plan

### Course 6 — Markets & Taxation (10 chapters)
1. Trading vs Investing — different tax treatments
2. STT, Brokerage & Transaction Charges
3. GST & Stamp Duty
4. Short-Term vs Long-Term Capital Gains
5. Intraday vs Delivery taxation (speculative vs business income)
6. Turnover & Bookkeeping for Traders
7. Filing Basics — ITR forms & schedules
8. Set-off & Carry Forward of Losses
9. Options & Futures tax treatment
10. Common Filing Mistakes

## 7. Verification checklist (author must self-check before delivery)

- [ ] All 68 chapters present; IDs contiguous and unique per the reserved table
- [ ] Each chapter: 3–4 lectures, 3–5 quiz questions, explanations on every question
- [ ] `correctIndex` is within bounds and verifiably correct
- [ ] Lecture word counts ~75–110; plain text, no markdown/emoji
- [ ] No copyrighted text from Varsity or any other source (original writing)
- [ ] Indian context (₹/NSE/BSE) in at least one quiz per chapter (a future content pass will close the remaining gaps on conceptual chapters)
- [ ] `rewardAmt` matches tier (500 / 750 / 1000)
- [ ] JSON parses cleanly (validate with any JSON linter)
- [ ] Discipline/risk-messaging tone preserved throughout

---

## 8. Integration note for TradeLab engineers

- The app's loader will read this file at launch and fall back to the legacy `academy_data.json` (single-question format) if it is absent or malformed.
- Legacy `QuizModule` entries are mapped into a synthetic "Stock Market Basics" course so existing progress and rewards keep working.
- `riskDisclosure` (required on Courses 4 & 6) is surfaced as a footnote inside the chapter's quiz dialog; `AcademyScoring.validateCourses()` enforces its presence so CI fails if an author omits it.
- Chapter completion is recorded in the existing `completedLevels` CSV (`completeTutorialLevel`), so the F&O unlock gate and certification logic remain intact.
