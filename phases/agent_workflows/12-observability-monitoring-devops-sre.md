---
phase: 12
title: Observability & Monitoring
role: DevOps, SRE
dependencies: [Phase 2, Phase 11]
estimated_time: 2-4 hours
---

# Phase 12: Observability & Monitoring — Agent Workflow

## Prerequisites
- [ ] Phase 11 completed (infrastructure configured)
- [ ] Required env vars: `SENTRY_DSN`, `SENTRY_AUTH_TOKEN`, `NEXT_PUBLIC_SENTRY_DSN`
- [ ] Required packages: `@sentry/nextjs`, `pino`, `@opentelemetry/api`

## Step-by-Step Execution

### Step 1: Install OpenTelemetry
**Action:** `run_command`
```bash
pnpm add @opentelemetry/api @opentelemetry/sdk-node @opentelemetry/auto-instrumentations-node @opentelemetry/exporter-otlp-http
```

### Step 2: Write OTel Instrumentation
**Action:** `write_to_file`
**File:** `instrumentation.ts`
**Description:** Next.js instrumentation file that initializes OpenTelemetry with `getNodeAutoInstrumentations()` (not empty array). Configure HTTP, fetch, and database auto-instrumentation. Add graceful shutdown.

### Step 3: Write Correlation ID Middleware
**Action:** `edit_file`
**File:** `middleware.ts`
**Description:** Generate or propagate `x-correlation-id` header on every request. Pass to downstream services and logging.

### Step 4: Write Structured Logger
**Action:** `write_to_file`
**File:** `lib/logger.ts`
**Description:** Pino logger with JSON format, child logger for correlation IDs, and PII redaction (strip emails, tokens from logs).

### Step 5: Install and Configure Sentry
**Action:** `run_command`
```bash
pnpm add @sentry/nextjs
npx @sentry/wizard@latest -i nextjs
```

### Step 6: Write Sentry Configuration
**Action:** `write_to_file`
**Files:** `sentry.client.config.ts`, `sentry.server.config.ts`, `sentry.edge.config.ts`
**Description:** Environment-aware sample rates: `tracesSampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0`. Add feedback integration on client. Add `withSentryConfig` to `next.config.ts`.

### Step 7: Add Vercel Analytics (Lightweight Alternative)
**Action:** `run_command`
```bash
pnpm add @vercel/analytics @vercel/speed-insights
```
**Action:** `edit_file`
**File:** `app/layout.tsx`
**Description:** Add `<Analytics />` and `<SpeedInsights />` components.

### Step 8: Write Alerting Rules
**Action:** `write_to_file`
**File:** `docs/ALERTING_RULES.md`
**Description:** Define alert thresholds: error rate > 1% → Slack, latency p99 > 2s → PagerDuty, memory > 90% → warning, failed deployments → immediate.

## Verification
- [ ] OTel traces appear in collector (Jaeger/Honeycomb/etc.)
- [ ] Correlation IDs propagate through request chain
- [ ] Sentry captures errors with correct sample rates
- [ ] Pino logs are structured JSON with correlation IDs
- [ ] Vercel Analytics dashboard shows data

## Troubleshooting
- **Issue:** OTel instrumentations empty — no traces
  **Fix:** Import `getNodeAutoInstrumentations()` from `@opentelemetry/auto-instrumentations-node` and pass to SDK.
- **Issue:** Sentry capturing too many events in production
  **Fix:** Set `tracesSampleRate: 0.1` for production. Use environment check, not hardcoded value.
