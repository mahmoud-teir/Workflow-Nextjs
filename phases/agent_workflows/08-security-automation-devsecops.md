---
phase: 8
title: Security Automation
role: DevSecOps
dependencies: [Phase 4, Phase 7]
estimated_time: 2-3 hours
---

# Phase 8: Security Automation — Agent Workflow

## Prerequisites
- [ ] Phase 4 completed (auth middleware in place)
- [ ] Required packages: `husky`

## Step-by-Step Execution

### Step 1: Write Unified Security Middleware
**Action:** `write_to_file`
**File:** `middleware.ts`
**Description:** Single middleware combining security headers (CSP with nonce, HSTS, X-Frame-Options), CORS configuration, and auth route protection. This is the single source of truth for all security headers — Phase 4 does NOT handle CSP.

### Step 2: Add CSP Nonce to Layout
**Action:** `edit_file`
**File:** `app/layout.tsx`
**Description:** Read nonce from `headers()` and pass to Script components. Add `connect-src` for Stripe, Sentry, PostHog domains.

### Step 3: Set Up Husky + Secret Scanning
**Action:** `run_command`
```bash
pnpm add -D husky
pnpm exec husky init
```
**Action:** `write_to_file`
**File:** `.husky/pre-commit`
**Description:** Pre-commit hook running Biome check and TruffleHog secret scanning.

### Step 4: Add Security CI Workflow
**Action:** `write_to_file`
**File:** `.github/workflows/security.yml`
**Description:** GitHub Actions workflow with `npm audit`, CodeQL analysis, TruffleHog secret scan, and Socket.dev dependency check.

### Step 5: Add SRI Guidance
**Action:** `edit_file`
**Description:** Document Subresource Integrity (SRI) requirements for any external scripts loaded via `<script>` tags. Use `integrity` attribute with SHA-384 hashes.

### Step 6: Run Penetration Testing Checklist
**Action:** `review`
**Description:** Execute security checklist:
- [ ] All Server Actions verify authorization
- [ ] No SQL injection (parameterized queries via ORM)
- [ ] No XSS (React auto-escapes, CSP blocks inline scripts)
- [ ] Rate limiting on auth and API routes
- [ ] Race condition testing on sensitive operations
- [ ] File upload type/size validation
- [ ] Privacy compliance (consent banner, data deletion)

## Verification
- [ ] Security headers present in response (check with `curl -I`)
- [ ] CSP nonce working (inline scripts execute)
- [ ] Pre-commit hook blocks secrets
- [ ] Security CI workflow runs on push
- [ ] No `any` types in middleware

## Troubleshooting
- **Issue:** CSP blocks legitimate scripts
  **Fix:** Add domain to `connect-src` or `script-src` in CSP policy. Use nonce for inline scripts.
- **Issue:** CORS errors in development
  **Fix:** Add `localhost:3000` to allowed origins in CORS config.
