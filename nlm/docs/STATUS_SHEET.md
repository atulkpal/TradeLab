# NLM Pipeline Status Sheet

## Current Status Summary (as of 2026-08-23)

| Metric | Count | Percentage |
|--------|-------|------------|
| **Total Lectures** | 204 | 100% |
| **Notebooks Created** | 204 | 100% |
| **Videos Generated** | ~32 | ~15.7% |
| **Videos Downloaded** | 32 | 15.7% |
| **Unique Lectures with Video** | 11 | 5.4% |
| **Videos Processed (branded)** | 23 | 11.3% |

---

## Pipeline Status

| Stage | Status | Notes |
|-------|--------|-------|
| Lecture Extraction | ✅ Complete | 204 .md files in `nlm/lectures/` |
| Notebook Creation | ✅ Complete | All 204 notebooks on NotebookLM (7 accounts, some duplicates) |
| Video Generation | 🔄 In Progress | 5-min delay between requests, ~38 videos/day capacity |
| Video Download | 🔄 In Progress | 32 raw videos in `nlm/assets/` |
| Video Processing | ✅ 23/23 done | Branding applied in `nlm/assets/out/` |
| Firebase Upload | ⏳ Pending | Requires Firebase Storage enabled |
| App Integration | ✅ Ready | VideoPlayerView + VideoCacheManager ready |

---

## Account Status

| Account | Tier | Notebooks | Videos/Day | Auth Status |
|---------|------|-----------|------------|-------------|
| atulkpal@gmail.com | Pro | 36 (28 alloc + 8 extras) | 20 | ⚠️ Expired |
| ashwathai.dev@gmail.com | Standard | 32 (28 alloc + 4 extras) | 3 | ⚠️ Expired |
| boss.studio.care@gmail.com | Standard | 32 (28 alloc + 4 extras) | 3 | ⚠️ Expired |
| hi.jumpdroid@gmail.com | Standard | 32 (28 alloc + 4 extras) | 3 | ⚠️ Expired |
| iiidem.km@gmail.com | Standard | 32 (28 alloc + 4 extras) | 3 | ⚠️ Expired |
| promptwala.xyz@gmail.com | Standard | 32 (28 alloc + 4 extras) | 3 | ⚠️ Expired |
| paulritu120@gmail.com | Standard | 32 (28 alloc + 4 extras) | 3 | ⚠️ Expired |

**Total capacity:** 38 videos/day (1 Pro + 6 Standard)
**Estimated time to complete:** ~5 days

---

## Duplicate Notebooks

Some lectures have multiple notebooks across accounts (from parallel creation). This is acceptable — duplicates with videos are kept.

| Lecture | Accounts | Copies |
|---------|----------|--------|
| 1.1.1 | atulkpal | 7 |
| 1.11.1 | ashwathai | 4 |
| 2.8.3 | boss_studio | 4 |
| 3.6.2 | hi_jumpdroid | 4 |
| 4.6.1 | iiidem_km | 4 |
| 5.3.3 | promptwala | 4 |
| 6.1.2 | paulritu | 4 |

---

## Pending Allocation

`pending_allocation.json` has 196/204 lectures allocated (28 per account × 7). The 8 "missing" lectures (1.1.1, 1.1.2, 1.11.1, 2.8.3, 3.6.2, 4.6.1, 5.3.3, 6.1.2) exist on NotebookLM but are not in the allocation file. This is fine — they have notebooks with videos.

---

## Next Actions

1. **Re-authenticate all 7 accounts** — run `python check_auth.py` interactively
2. **Run `master_notebook_manager.py`** — generates videos for remaining notebooks (5-min delay)
3. **Run `download_all_videos.py`** — download all completed videos
4. **Enable Firebase Storage** — in Firebase Console for `tradelab-4f858`
5. **Run `upload_to_firebase.py`** — upload processed videos
6. **Update `academy_data_v2.json`** — add Firebase Storage URLs

---

*Last updated: 2026-08-23*
