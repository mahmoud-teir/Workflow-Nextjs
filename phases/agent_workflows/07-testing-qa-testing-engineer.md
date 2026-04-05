---
phase: 7
title: Testing & QA
role: Testing Engineer
dependencies: [Phase 1, Phase 2, Phase 5]
estimated_time: 3-5 hours
---

# Phase 7: Testing & QA — Agent Workflow

## Prerequisites
- [ ] Phase 5 completed (frontend components to test)
- [ ] Required packages: `vitest`, `@testing-library/react`, `playwright`, `msw`

## Step-by-Step Execution

### Step 1: Install Test Dependencies
**Action:** `run_command`
```bash
pnpm add -D vitest @vitejs/plugin-react @testing-library/react @testing-library/jest-dom jsdom msw playwright @playwright/test @axe-core/playwright
```

### Step 2: Write Vitest Configuration
**Action:** `write_to_file`
**File:** `vitest.config.ts`
**Description:** Configure Vitest with React plugin, jsdom environment, path alias `'@': resolve(__dirname, '.')` (no `./src`), coverage with v8 provider and lcov reporter.

### Step 3: Write Vitest Setup File
**Action:** `write_to_file`
**File:** `vitest.setup.ts`
**Description:** Import `@testing-library/jest-dom/vitest`, set up MSW server with `beforeAll`/`afterEach`/`afterAll` lifecycle hooks.

### Step 4: Write MSW Handlers
**Action:** `write_to_file`
**Files:** `tests/mocks/handlers.ts`, `tests/mocks/server.ts`
**Description:** Mock Service Worker handlers for API routes. Server setup for Node environment.

### Step 5: Write Unit Tests
**Action:** `write_to_file`
**File:** `tests/unit/example-service.test.ts`
**Description:** Test service layer functions with mocked database. Follow naming convention: `describe('ServiceName') > it('should [behavior]')`.

### Step 6: Write Server Action Tests
**Action:** `write_to_file`
**File:** `tests/unit/actions.test.ts`
**Description:** Test Server Actions by mocking `db` and `auth` modules. Verify validation, auth checks, and return types.

### Step 7: Write Component Tests
**Action:** `write_to_file`
**File:** `tests/components/example.test.tsx`
**Description:** Test client components with Testing Library. Verify rendering, user interactions, and form submissions.

### Step 8: Write Playwright Configuration
**Action:** `write_to_file`
**File:** `playwright.config.ts`
**Description:** Configure Playwright with webServer, auth setup project using `storageState`, and multiple browsers.

### Step 9: Write Playwright Auth Setup
**Action:** `write_to_file`
**File:** `tests/e2e/auth.setup.ts`
**Description:** Login once and save `storageState` for reuse across E2E tests.

### Step 10: Write E2E Tests
**Action:** `write_to_file`
**File:** `tests/e2e/example.spec.ts`
**Description:** End-to-end tests for critical user flows (signup, dashboard, CRUD operations).

### Step 11: Add Visual Regression Testing
**Action:** `write_to_file`
**File:** `tests/e2e/visual.spec.ts`
**Description:** Playwright screenshot comparison for key pages. Use `toHaveScreenshot()` with threshold.

### Step 12: Write Docker Test Database Config
**Action:** `write_to_file`
**File:** `docker-compose.test.yml`
**Description:** PostgreSQL container with tmpfs for fast ephemeral test database in CI.

### Step 13: Add Email Snapshot Tests
**Action:** `write_to_file`
**File:** `tests/unit/emails.test.tsx`
**Description:** Snapshot test email templates using `@react-email/render`.

### Step 14: Add package.json Scripts
**Action:** `edit_file`
**File:** `package.json`
**Description:** Add test scripts: `test`, `test:ui`, `test:coverage`, `test:e2e`, `test:e2e:ui`.

## Verification
- [ ] `pnpm test` — all unit tests pass
- [ ] `pnpm test:coverage` — coverage report generated
- [ ] `pnpm test:e2e` — all E2E tests pass
- [ ] MSW handlers intercept API calls correctly
- [ ] Visual regression screenshots baselined

## Troubleshooting
- **Issue:** Vitest alias `@/` not resolving
  **Fix:** Ensure alias is `'@': resolve(__dirname, '.')` — no `./src` prefix.
- **Issue:** Playwright tests fail on CI
  **Fix:** Run `pnpm playwright install --with-deps` in CI. Use `docker-compose.test.yml` for database.
