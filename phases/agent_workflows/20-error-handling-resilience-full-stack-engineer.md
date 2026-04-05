---
phase: 20
title: Error Handling & Resilience
role: Full-Stack Engineer
dependencies: [Phase 2, Phase 5, Phase 12]
estimated_time: 3-4 hours
---

# Phase 20: Error Handling & Resilience — Agent Workflow

## Prerequisites
- [ ] Phase 2 completed (Server Actions and API routes)
- [ ] Phase 12 completed (Sentry configured)
- [ ] Required packages: `@sentry/nextjs` (already from Phase 12)

## Step-by-Step Execution

### Step 1: Write Error Taxonomy
**Action:** `write_to_file`
**File:** `lib/errors.ts`
**Description:** Typed error classes: `AppError` (base), `NotFoundError`, `UnauthorizedError`, `ForbiddenError`, `ValidationError`, `RateLimitError`, `ExternalServiceError`. Each with `code`, `statusCode`, and `isOperational` flag.

### Step 2: Write Route Error Boundary
**Action:** `write_to_file`
**File:** `app/error.tsx`
**Description:** Default `'use client'` error boundary. Capture to Sentry, show error message + digest ID, retry and go-home buttons.

### Step 3: Write Global Error Boundary
**Action:** `write_to_file`
**File:** `app/global-error.tsx`
**Description:** Root-level error boundary — MUST include `<html>` and `<body>` tags. Inline styles only (no CSS imports). Capture to Sentry.

### Step 4: Write Custom 404 Page
**Action:** `write_to_file`
**File:** `app/not-found.tsx`
**Description:** Styled 404 page with link back to home.

### Step 5: Write API Error Handler
**Action:** `write_to_file`
**File:** `lib/api-errors.ts`
**Description:** `handleApiError()` function that converts any error to consistent JSON response: `{ error, message, statusCode, details? }`. Handles `AppError`, `ZodError`, and unknown errors. Reports non-operational errors to Sentry.

### Step 6: Write Retry Utility
**Action:** `write_to_file`
**File:** `lib/retry.ts`
**Description:** `withRetry()` — exponential backoff with jitter, configurable max attempts, retryable error detection (network errors, 5xx responses).

### Step 7: Write Timeout Wrapper
**Action:** `write_to_file`
**File:** `lib/timeout.ts`
**Description:** `fetchWithTimeout()` using AbortController. Default 10s timeout. Clean up on completion.

### Step 8: Write Circuit Breaker
**Action:** `write_to_file`
**File:** `lib/circuit-breaker.ts`
**Description:** `CircuitBreaker` class with closed → open → half-open state machine. Configurable failure threshold and reset timeout.

### Step 9: Write Resilient Fetch Composition
**Action:** `write_to_file`
**File:** `lib/resilient-fetch.ts`
**Description:** Compose circuit breaker + retry + timeout into `resilientFetch(serviceName, url, options)`. Per-service circuit breakers via Map.

### Step 10: Write Offline Detection Hook
**Action:** `write_to_file`
**File:** `hooks/use-online-status.ts`
**Description:** `useOnlineStatus()` using `useSyncExternalStore` with `online`/`offline` events. Server snapshot returns `true`.

### Step 11: Write Offline Indicator
**Action:** `write_to_file`
**File:** `components/offline-indicator.tsx`
**Description:** Fixed banner shown when offline: "You're offline. Some features may be unavailable."

### Step 12: Write Feature Degradation Config
**Action:** `write_to_file`
**File:** `lib/feature-availability.ts`
**Description:** Feature degradation matrix — what works offline (limited), what's unavailable (payments, AI), and `FeatureGate` component for conditional rendering.

### Step 13: Write Stale Cache Utility
**Action:** `write_to_file`
**File:** `lib/stale-cache.ts`
**Description:** `fetchWithStaleCache()` — try fresh data first, fall back to `unstable_cache` version on failure. Returns `{ data, stale }` flag.

### Step 14: Write Error Classifier
**Action:** `write_to_file`
**File:** `lib/error-classifier.ts`
**Description:** Classify errors by severity (P0-P3) and category (user/system/external). Map to alert channels (PagerDuty, Slack, daily digest).

### Step 15: Write Error Reporter
**Action:** `write_to_file`
**File:** `lib/error-reporter.ts`
**Description:** `reportError()` — classify, set Sentry scope (level, tags, context), capture exception, trigger immediate alerts for P0/P1.

### Step 16: Write Error Budget Tracker
**Action:** `write_to_file`
**File:** `lib/error-budget.ts`
**Description:** `calculateErrorBudget()` — given SLO target (99.9%), window, and total requests, calculate budget remaining and status (healthy/warning/critical/exhausted).

### Step 17: Add Error Budget to Health Endpoint
**Action:** `edit_file`
**File:** `app/api/health/route.ts`
**Description:** Include error budget status in health check response.

## Verification
- [ ] `error.tsx` renders for route-level errors
- [ ] `global-error.tsx` renders for root errors (includes html/body)
- [ ] `not-found.tsx` renders for 404s
- [ ] API errors return consistent JSON shape
- [ ] Retry utility retries on 5xx, stops on 4xx
- [ ] Circuit breaker opens after threshold failures
- [ ] Offline indicator shows when disconnected
- [ ] Stale cache serves data when live fetch fails

## Troubleshooting
- **Issue:** `global-error.tsx` not rendering
  **Fix:** Must include `<html>` and `<body>` tags. Cannot import CSS files — use inline styles only.
- **Issue:** Circuit breaker stays open
  **Fix:** Check `resetTimeoutMs` — after this period, it enters half-open state and allows one test request through.
