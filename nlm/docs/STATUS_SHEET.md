# NLM Pipeline Status Sheet

## Current Status Summary (as of 2026-08-24, 17:05 IST)

| Metric | Count | Percentage |
|--------|-------|------------|
| **Total Lectures** | 204 | 100% |
| **Allocated** | 196 | 96% (8 legacy unallocated — already have videos) |
| **Notebooks Created** | 196 | 100% of allocated |
| **Videos Generated** | 46 | 22.5% |
| **Videos Downloaded** | **46** | 22.5% — all completed videos banked locally |
| **Pending Generation** | 150 | 73.5% — rebalanced across all 7 accounts |
| **Sources Uploaded** | 196 | 100% of allocated |

---

## Pipeline Status

| Stage | Status | Notes |
|-------|--------|-------|
| Lecture Extraction | ✅ Complete | 204 .md files in `nlm/lectures/` |
| Notebook Creation | 🔄 196/196 allocated done | **+78 rebuilding on atulkpal** (rebalance) — CREATE pending |
| Video Generation | 🔄 In Progress | 38/day capacity (rebalanced); ~4 days remaining |
| Video Download | ✅ 46 banked | `nlm/assets/*.mp4` — cloud URLs preserved in CSV |
| Video Processing | ⏳ Pending | Legacy `polish_videos.py` (intro/outro) — after campaign |
| Firebase Upload | ⏳ Pending | Legacy `upload_to_firebase.py` — after campaign |
| App Integration | ✅ Ready | VideoPlayerView + VideoCacheManager ready |

---

## Manager (`pipeline.py`) Capability Status

| Subsystem | State |
|-----------|-------|
| Cookie rotation (throttled 600s) | ✅ Live |
| L3 silent headless self-heal | ✅ Live (SSO refreshed by 2026-08-24 logins) |
| Auth blocking + startup banner | ✅ Live |
| Quota pre-stop + `quota_state.json` + auto-resume | ✅ Live |
| Rate-limit classification (transient vs daily) | ✅ Live |
| CSV lock-killer + recovery file + live saves | ✅ Live |
| Reassignment engine (stranded + rebalance + cleanup queue) | ✅ Live |
| `download_status` lifecycle + self-healing | ✅ Live |
| Static dashboard (`dashboard.html`) | ✅ Available (Accounts/Config/Progress) |
| Server phase (jobs/logs/reauth from browser) | 🅿️ Parked |
| Resilience kit (flags/lock/watchdog/logs) | 🅿️ Parked (manual runs OK) |
| Password vault (Plan B) | 🅿️ Decision pending next revocation wave |

---

## Account Status (post re-auth 2026-08-24 16:45-16:56)

| Account | Tier | Pending | Videos/Day | Auth |
|---------|------|---------|------------|------|
| atulkpal@gmail.com | Pro | 78 (rebalanced owner) | 20 | ✅ Valid |
| ashwathai.dev@gmail.com | Standard | 12 | 3 | ✅ Valid |
| boss.studio.care@gmail.com | Standard | 12 | 3 | ✅ Valid |
| hi.jumpdroid@gmail.com | Standard | 12 | 3 | ✅ Valid |
| iiidem.km@gmail.com | Standard | 12 | 3 | ✅ Valid |
| promptwala.xyz@gmail.com | Standard | 12 | 3 | ✅ Valid |
| paulritu120@gmail.com | Standard | 12 | 3 | ✅ Valid |

**Capacity:** 38/day · **Balanced finish:** ~4 days from first trigger
**History note:** two session-revocation waves (08-23, 08-24) — all accounts
re-authed via fresh login; L3 self-heal armed for the next wave.

---

## Cleanup Queue

78 orphan notebooks (source-only, no videos) queued for deletion on their original
standard-tier owners after the 2026-08-24 16:01 rebalance to atulkpal. Drains
automatically via option 8 "process now", option 7 post-reauth hook, or RUN ALL sweep.
Divergence-guarded: a queued notebook is never deleted if the CSV still references it.

---

## Next Actions

1. **Process cleanup queue** — option `8` → Y at the prompt (~1-2 min, 78 deletions)
2. **CREATE** — option `2` builds atulkpal's 78 rebalanced notebooks (~15-25 min)
3. **RUN ALL** — option `6` → autonomous ~4 days (triggers, downloads, quota-parks,
   self-heals, cleanup sweeps)
4. After 204/204: legacy polish + Firebase stages (`PIPELINE.md`)

---

*Last updated: 2026-08-24 · See [`PIPELINE_V2.md`](./PIPELINE_V2.md) for architecture.*
