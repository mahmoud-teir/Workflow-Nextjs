---
phase: 14
title: Pre-Launch Checklist
role: All Roles
dependencies: [Phase 0-13]
estimated_time: 2-4 hours
---

# Phase 14: Pre-Launch Checklist — Agent Workflow

## Prerequisites
- [ ] All previous phases completed (0-13)
- [ ] Staging environment deployed
- [ ] All tests passing

## Step-by-Step Execution

### Step 1: Run Security Audit
**Action:** `review`
**Description:** Execute P0 security checklist:
- [ ] All Server Actions verify auth/authorization
- [ ] Security headers present (CSP, HSTS) — `curl -I https://staging.myapp.com`
- [ ] SSL/TLS grade A+ — check ssllabs.com
- [ ] Input validation (Zod) on ALL forms and API routes
- [ ] No secrets in client bundle — `grep NEXT_PUBLIC .env`
- [ ] Dependency scan clean — `pnpm audit`

### Step 2: Run Performance Audit
**Action:** `run_command`
```bash
# Run Lighthouse CI
npx lighthouse https://staging.myapp.com --output json --chrome-flags="--headless"
```
**Description:** Verify Core Web Vitals (LCP < 2.5s, INP < 200ms, CLS < 0.1), Lighthouse > 90, bundle < 200KB gzipped.

### Step 3: Run Functionality Checks
**Action:** `review`
**Description:** Test all user flows: signup, login, CRUD operations, file uploads, search. Cross-browser test (Chrome, Firefox, Safari, Edge). Mobile responsiveness (320px-2560px).

### Step 4: Verify Error Pages
**Action:** `review`
**Description:** Check custom `not-found.tsx`, `error.tsx`, `global-error.tsx` render correctly. Test by navigating to non-existent routes and triggering errors.

### Step 5: Run Legal & Compliance Check
**Action:** `review`
**Description:** Verify Privacy Policy and Terms of Service are published. Cookie consent banner appears. Account deletion works. Analytics respects opt-out.

### Step 6: Verify Monitoring
**Action:** `review`
**Description:** Confirm Sentry captures errors, uptime monitoring is active, alerting rules are configured, rollback procedure is tested.

### Step 7: Run AI Safety Checks (If Applicable)
**Action:** `review`
**Description:** Test prompt injection prevention, content moderation, token budgets, model fallback chain.

### Step 8: Run Automated Validation Script
**Action:** `run_command`
```bash
bash scripts/pre-launch-check.sh
```
**Description:** Execute the automated pre-launch script from Prompt 14.3. Fix any failures before proceeding.

### Step 9: SEO Verification
**Action:** `review`
**Description:** Verify sitemap.xml, robots.txt, meta tags (title, description, OG image), structured data (JSON-LD), favicon.

### Step 10: Final Build & Deploy
**Action:** `run_command`
```bash
pnpm build
# Deploy to production
```

## Verification
- [ ] All P0 checklist items pass
- [ ] Lighthouse scores > 90
- [ ] Automated validation script passes
- [ ] Monitoring active and alerting works
- [ ] Rollback tested

## Troubleshooting
- **Issue:** Hydration mismatch errors
  **Fix:** Check for browser-only code (dates, window), use `suppressHydrationWarning` or `useEffect` for client-only rendering.
- **Issue:** `searchParams` type errors after upgrade
  **Fix:** In Next.js 15+, `searchParams` is a Promise — must `await` it.
- **Issue:** `useFormStatus` always returns `pending: false`
  **Fix:** Must be used in a child component of `<form>`, not the same component.
