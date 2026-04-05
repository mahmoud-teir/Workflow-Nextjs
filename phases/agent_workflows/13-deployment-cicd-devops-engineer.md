---
phase: 13
title: Deployment & CI/CD
role: DevOps Engineer
dependencies: [Phase 7, Phase 11, Phase 12]
estimated_time: 2-3 hours
---

# Phase 13: Deployment & CI/CD — Agent Workflow

## Prerequisites
- [ ] Phase 7 completed (tests passing)
- [ ] Phase 11 completed (infrastructure ready)
- [ ] GitHub repository set up
- [ ] Deployment target configured (Vercel, AWS, etc.)

## Step-by-Step Execution

### Step 1: Write CI Pipeline
**Action:** `write_to_file`
**File:** `.github/workflows/ci.yml`
**Description:** GitHub Actions workflow with parallel jobs: lint-and-typecheck → test (with coverage) → build → e2e. Use `.nvmrc` for Node version, `pnpm/action-setup@v4`. Add concurrency group to cancel in-progress runs. Include security scanning job (npm audit, CodeQL).

### Step 2: Add Test Coverage Reporting
**Action:** `edit_file`
**File:** `.github/workflows/ci.yml`
**Description:** Add Codecov upload step after test job. Configure coverage thresholds.

### Step 3: Write Deployment Notification
**Action:** `edit_file`
**File:** `.github/workflows/ci.yml`
**Description:** Add Slack webhook notification on deployment success/failure.

### Step 4: Configure Vercel Deployment
**Action:** `write_to_file`
**File:** `vercel.json`
**Description:** Configure cron jobs, headers, redirects, and environment variable references.

### Step 5: Set Up Environment Variables
**Action:** `run_command`
```bash
# Pull env vars from Vercel
vercel env pull .env.local
# Or set them
vercel env add DATABASE_URL production
```

### Step 6: Document Rollback Strategy
**Action:** `write_to_file`
**File:** `docs/ROLLBACK_STRATEGY.md`
**Description:** Vercel: instant rollback via dashboard or `vercel rollback`. ECS: rolling update with health check gates. General: feature flags for instant disable, database migration rollback procedures.

### Step 7: Configure Branch Protection
**Action:** `review`
**Description:** Set up branch protection rules on `main`: require PR reviews, require status checks (CI must pass), require up-to-date branch, no force push.

### Step 8: Add Release Workflow (Optional)
**Action:** `write_to_file`
**File:** `.github/workflows/release.yml`
**Description:** Semantic release workflow triggered on main push. Auto-generates changelog and GitHub release.

## Verification
- [ ] CI pipeline runs on PR and passes all jobs
- [ ] Test coverage report uploads to Codecov
- [ ] Deployment succeeds to staging/production
- [ ] Slack notification fires on deploy
- [ ] Rollback procedure tested and documented
- [ ] Branch protection rules active

## Troubleshooting
- **Issue:** CI fails on pnpm install
  **Fix:** Use `pnpm/action-setup@v4` (not pinned v3). Use `.nvmrc` for Node version.
- **Issue:** E2E tests fail in CI
  **Fix:** Run `pnpm playwright install --with-deps`. Use `docker-compose.test.yml` for database.
