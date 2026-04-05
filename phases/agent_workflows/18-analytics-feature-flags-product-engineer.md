---
phase: 18
title: Analytics & Feature Flags
role: Product Engineer
dependencies: [Phase 5, Phase 8]
estimated_time: 2-3 hours
---

# Phase 18: Analytics & Feature Flags — Agent Workflow

## Prerequisites
- [ ] Phase 5 completed (frontend working)
- [ ] Phase 8 completed (CSP allows analytics domains)
- [ ] Required env vars: `NEXT_PUBLIC_POSTHOG_KEY`, `NEXT_PUBLIC_POSTHOG_HOST` (for Option A)

## Step-by-Step Execution

### Step 1: Write PostHog Server Client (Singleton)
**Action:** `write_to_file`
**File:** `lib/posthog-server.ts`
**Description:** Singleton pattern using `globalThis` cache — prevents creating new instance on every call.

### Step 2: Write Consent Banner
**Action:** `write_to_file`
**File:** `components/consent-banner.tsx`
**Description:** GDPR/CCPA consent banner. Sets `analytics_consent` cookie. Blocks all tracking until user accepts. Provides decline option that clears tracking cookies.

### Step 3: Write Analytics Provider
**Action:** `write_to_file`
**File:** `app/providers.tsx`
**Description:** PostHog client initialization gated by consent cookie. Manual page view capture for SPA navigation. `opt_in_capturing()`/`opt_out_capturing()` based on consent.

### Step 4: Write Privacy Settings Page
**Action:** `write_to_file`
**File:** `app/(app)/settings/privacy/page.tsx`
**Description:** Allow users to change consent preference after initial choice.

### Step 5: Write Event Taxonomy
**Action:** `write_to_file`
**File:** `lib/analytics-events.ts`
**Description:** Typed event map with `trackEvent<K>()` helper. Naming convention: `object_action` in snake_case. Optional dev-only Zod validation.

### Step 6: Add Server-Side Tracking
**Action:** `edit_file`
**Description:** Use `posthogServer.capture()` in Server Actions for events that must fire even if client JS fails. Always `flush()` before returning.

### Step 7: Write Feature Flag Helper
**Action:** `write_to_file`
**File:** `lib/feature-flags.ts`
**Description:** Type-safe `getFlag<K>()` and `getAllFlags()` helpers. Server-side evaluation to prevent flash of wrong content.

### Step 8: Write Gradual Rollout Utility
**Action:** `write_to_file`
**File:** `lib/gradual-rollout.ts`
**Description:** Deterministic percentage-based rollout using MD5 hash of `feature:userId`. No external service needed.

### Step 9: Write A/B Test Component
**Action:** `write_to_file`
**File:** `components/signup-button.tsx`
**Description:** Example A/B test using `useFeatureFlagVariantKey`. Track click event with variant info. Track conversion in Server Action with `posthog.capture('signup_completed')`.

### Step 10: Add Privacy-First Alternatives (Optional)
**Action:** `write_to_file`
**Description:** Option B: Plausible (script tag, no cookies, GDPR-compliant without consent). Option C: Umami (self-hosted, lightweight).

## Verification
- [ ] Consent banner appears on first visit
- [ ] No tracking fires before consent granted
- [ ] PostHog server client is singleton (no multiple instances)
- [ ] Feature flags evaluate server-side
- [ ] A/B test tracks both click and conversion events
- [ ] Privacy settings page allows consent change

## Troubleshooting
- **Issue:** PostHog events not appearing
  **Fix:** Check consent cookie is `granted`. Verify `NEXT_PUBLIC_POSTHOG_KEY` is set. Check CSP allows PostHog host.
- **Issue:** Feature flag always returns default
  **Fix:** Ensure `distinctId` is set (from cookie or auth). Check flag is enabled in PostHog dashboard.
