# NLM Quota Management Guide

## Overview

NotebookLM imposes strict quotas on video generation operations. Understanding and managing these quotas is critical for successful pipeline execution.

---

## Quota Limits (Observed)

| Operation | Limit | Period | Error Code |
|-----------|-------|--------|------------|
| Video Generation | ~5-10/account/day | Daily | rpc_code=8 |
| Notebook Creation | ~50/account/hour | Hourly | rpc_code=8 |
| Source Upload | ~100/account/hour | Hourly | rpc_code=8 |
| Auth Token | ~1-2 hours | Session | rpc_code=16 |

### Error Codes

| Code | Meaning | Action |
|------|---------|--------|
| **rpc_code=8** | Resource Exhausted (quota exceeded) | Wait, reduce concurrency, retry |
| **rpc_code=16** | Unauthenticated (token expired) | Re-authenticate account |
| **rpc_code=4** | Deadline Exceeded | Increase timeout, retry |
| **rpc_code=13** | Internal Error | Retry with backoff |

---

## Rate Limiting Strategies

### 1. Sequential Processing (Most Reliable)
```python
# Process one account at a time
for account in accounts:
    for lecture in account.lectures:
        create_notebook()
        await asyncio.sleep(30)  # 30s between requests
    await asyncio.sleep(300)  # 5 min between accounts
```
**Pros**: Zero quota errors  
**Cons**: Slow (~8 hours for 204 lectures)

### 2. Controlled Parallel (Balanced)
```python
# Max 2 concurrent per account, 4 global
global_sem = asyncio.Semaphore(4)
account_sem = {acc: asyncio.Semaphore(2) for acc in accounts}

async def create_with_limit(account, lecture):
    async with global_sem:
        async with account_sem[account]:
            return await create_notebook(account, lecture)
```

**Pros**: Faster than sequential  
**Cons**: Still hits quota at scale

### 3. Staggered Start (Recommended)
```python
# Stagger account starts by 5-10 minutes
for i, account in enumerate(accounts):
    await asyncio.sleep(i * 300)  # 5 min stagger
    asyncio.create_task(process_account(account))
```

---

## Backoff Algorithms

### Exponential Backoff (Standard)
```python
async def retry_with_backoff(fn, max_retries=3, base_delay=5):
    for attempt in range(max_retries):
        try:
            return await fn()
        except RateLimitError:
            if attempt == max_retries - 1:
                raise
            delay = min(base_delay * (2 ** attempt), 120)
            await asyncio.sleep(delay + random.uniform(0, 5))
```

### Jittered Exponential Backoff
```python
def calculate_delay(attempt, base=5, max_delay=120, multiplier=2, jitter=True):
    delay = min(base * (multiplier ** attempt), max_delay)
    if jitter:
        delay += random.uniform(0, delay * 0.1)  # 0-10% jitter
    return delay
```

### Resource Exhaustion Backoff (rpc_code=8)
```python
# For resource exhaustion, use longer delays
RESOURCE_EXHAUSTED_DELAYS = [60, 120, 300, 600]  # 1m, 2m, 5m, 10m

async def retry_resource_exhausted(fn, max_retries=4):
    for attempt in range(max_retries):
        try:
            return await fn()
        except RPCError as e:
            if e.rpc_code != 8 or attempt == max_retries - 1:
                raise
            delay = RESOURCE_EXHAUSTED_DELAYS[attempt]
            await asyncio.sleep(delay)
```

---

## Account Rotation Strategy

### Daily Rotation (Best for High Volume)
```python
# Rotate accounts daily to spread quota
ACCOUNT_SCHEDULE = {
    "monday": ["atulkpal", "ashwathai", "boss_studio"],
    "tuesday": ["hi_jumpdroid", "iiidem_km", "promptwala"],
    "wednesday": ["paulritu", "atulkpal", "ashwathai"],
    # ...
}
```

### Quota-Aware Scheduling
```python
class QuotaManager:
    def __init__(self):
        self.quotas = {acc: {"videos": 0, "reset": time.time() + 86400} 
                       for acc in ACCOUNTS}
    
    def can_create_video(self, account):
        if time.time() > self.quotas[account]["reset"]:
            self.quotas[account] = {"videos": 0, "reset": time.time() + 86400}
        return self.quotas[account]["videos"] < DAILY_LIMIT
    
    def record_video(self, account):
        self.quotas[account]["videos"] += 1
```

---

## Observed Quota Patterns

### NotebookLM Video Generation Quota (Empirical)

| Account Type | Daily Video Limit | Notes |
|--------------|-------------------|-------|
| Personal (Pro) | 5-10 | Varies by account age |
| Personal (Free) | 3-5 | More restrictive |
| Enterprise | 50+ | If available |

### Observed Behavior

| Time | Quota Available | Notes |
|------|-----------------|-------|
| 00:00 UTC | Full reset | Best time to start batch |
| 06:00 UTC | ~80% | After initial burst |
| 12:00 UTC | ~40% | Mid-day |
| 18:00 UTC | ~10% | Near exhaustion |
| 23:59 UTC | 0% | Exhausted |

---

## Recommended Settings for TradeLab

### For Notebook Creation
```python
MAX_CONCURRENT_PER_ACCOUNT = 1  # Notebook creation is lighter
GLOBAL_MAX_CONCURRENT = 4
DELAY_BETWEEN_REQUESTS = 10  # seconds
```

### For Video Generation
```python
MAX_CONCURRENT_PER_ACCOUNT = 1  # Video generation is heavy
GLOBAL_MAX_CONCURRENT = 2
DELAY_BETWEEN_REQUESTS = 60  # seconds
VIDEO_TIMEOUT = 2700  # 45 minutes
MAX_RETRIES = 3
BASE_DELAY = 60  # seconds
MAX_DELAY = 300  # 5 minutes
```

### For Sequential Account Processing
```python
ACCOUNT_DELAY = 300  # 5 minutes between accounts
LECTURE_DELAY = 30  # seconds between lectures
MAX_RETRIES = 3
BASE_DELAY = 10
MAX_DELAY = 120
```

---

## Monitoring Quota Usage

### Real-time Monitoring
```python
# In your script
quota_used = {"atulkpal": 0, "ashwathai": 0, ...}

async def create_with_quota_check(account, lecture):
    if quota_used[account] >= DAILY_LIMIT:
        raise QuotaExhausted(f"{account} daily limit reached")
    quota_used[account] += 1
    return await create_notebook(account, lecture)
```

### Log Quota Events
```python
def log_quota_event(account, event, count):
    logging.info(f"QUOTA: {account} | {event} | count={count} | limit={DAILY_LIMIT}")
```

---

## Emergency Procedures

### Quota Exhausted Mid-Batch
1. Stop all parallel tasks
2. Wait 30-60 minutes
3. Resume with single account
4. Reduce concurrency to 1

### Auth Token Expired
1. Run `notebooklm login --fresh --profile <name>`
2. Verify `storage_state.json` updated
3. Retry failed operations

### Persistent Quota Issues
1. Reduce to 1 account at a time
2. Increase delays to 60s+ between requests
3. Split batch across multiple days

---

## Configuration Template

```yaml
# quota_config.yaml
accounts:
  atulkpal@gmail.com:
    profile: default
    daily_video_limit: 8
    daily_notebook_limit: 40
    
quotas:
  video_generation:
    daily_limit_per_account: 8
    reset_time_utc: "00:00"
    
  notebook_creation:
    hourly_limit_per_account: 30
    
rate_limits:
  max_concurrent_per_account: 1
  global_max_concurrent: 2
  delay_between_requests: 30
  delay_between_accounts: 300
  
retry:
  max_retries: 3
  base_delay: 10
  max_delay: 120
  exponential_base: 2
  
resource_exhausted_delays: [60, 120, 300, 600]
```

---

*Last updated: 2026-08-22*
*Based on empirical testing with 7 accounts generating 133 notebooks*