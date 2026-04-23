---
name: telemetry-integration
description: Enforces the integration of production-grade observability tools (error tracking, analytics, logging) into every new project. Ensures no project ships without Sentry, PostHog, or equivalent.
---

# Telemetry & Analytics Integration Skill

Ensures every production project includes proper error tracking, user analytics, and performance monitoring from Day 1.

## Why This Matters

A project without telemetry is flying blind. You won't know:
- When users hit errors (unless they tell you)
- Which features are actually used
- Where performance bottlenecks exist
- When deployments introduce regressions

## Required Integrations

### 1. Error Tracking (Sentry) — MANDATORY
Every project MUST include Sentry for error tracking.

**Setup:**
```bash
npx @sentry/wizard@latest -i nextjs
```

**Verification:**
- [ ] `sentry.client.config.ts` exists
- [ ] `sentry.server.config.ts` exists
- [ ] `sentry.edge.config.ts` exists
- [ ] `instrumentation.ts` imports Sentry
- [ ] `SENTRY_DSN` is in `.env.local` (not hardcoded)
- [ ] Source maps are uploaded in CI (`sentry-cli`)

**Usage Pattern:**
```typescript
// In Server Actions
import * as Sentry from '@sentry/nextjs';

export async function riskyAction(input: Input) {
  try {
    // ... action logic
  } catch (error) {
    Sentry.captureException(error, {
      tags: { action: 'riskyAction' },
      extra: { input },
    });
    return { success: false, error: 'Something went wrong' };
  }
}
```

### 2. User Analytics (PostHog) — RECOMMENDED
For understanding user behavior and feature adoption.

**Setup:**
```bash
pnpm add posthog-js posthog-node
```

**Verification:**
- [ ] `PostHogProvider` wraps the app in `app/providers.tsx`
- [ ] `NEXT_PUBLIC_POSTHOG_KEY` is in `.env.local`
- [ ] Page views are tracked automatically
- [ ] Key user actions fire custom events

### 3. Performance Monitoring (Vercel Analytics) — RECOMMENDED
For Web Vitals and performance regression detection.

**Setup:**
```bash
pnpm add @vercel/analytics @vercel/speed-insights
```

**Verification:**
- [ ] `<Analytics />` component in root layout
- [ ] `<SpeedInsights />` component in root layout

## Integration Checklist

Before marking a project as "production-ready":

| Tool | Status | Priority |
|---|---|---|
| Sentry (Error Tracking) | ✅/❌ | MANDATORY |
| PostHog (User Analytics) | ✅/❌ | Recommended |
| Vercel Analytics (Performance) | ✅/❌ | Recommended |
| Structured Logging (pino) | ✅/❌ | Optional |

## Rules

1. **Never ship without Sentry** — This is non-negotiable for production
2. **No console.log in production** — Use structured logging (pino) or Sentry breadcrumbs
3. **Anonymize by default** — Do not track PII in analytics without consent
4. **Environment-aware** — Disable analytics in development, enable in production
5. **Budget alerts** — Configure Sentry quotas to avoid surprise billing
