---
phase: 2
title: Backend Setup — API Routes & Server Actions
role: Full-Stack Developer
dependencies: [Phase 1]
estimated_time: 2-3 hours
---

# Phase 2: Backend Setup — Agent Workflow

## Prerequisites
- [ ] Phase 1 completed (project structure, config)
- [ ] Database provider chosen (Neon, PlanetScale, Supabase, or Convex)
- [ ] Required env vars: `DATABASE_URL`

## Step-by-Step Execution

### Step 1: Set Up Database Client
**Action:** `write_to_file`
**File:** `db/index.ts`
**Description:** Database connection using chosen ORM. Option A: Drizzle, Option B: Prisma, Option C: Convex.
```bash
# Option A: Drizzle
pnpm add drizzle-orm @neondatabase/serverless
pnpm add -D drizzle-kit
```

### Step 2: Write Database Health Check
**Action:** `write_to_file`
**File:** `lib/db-health.ts`
**Description:** Connection health check utility that returns `{ healthy, latencyMs, error }`.

### Step 3: Write API Utilities
**Action:** `write_to_file`
**File:** `lib/api-utils.ts`
**Description:** `withErrorHandler` wrapper (typed `RouteContext`, not `any`), `composeMiddleware` for chaining, and standard JSON response helpers.

### Step 4: Write Health Check Route
**Action:** `write_to_file`
**File:** `app/api/health/route.ts`
**Description:** GET handler returning app status, database health, and version info.

### Step 5: Write API Versioning Pattern
**Action:** `write_to_file`
**File:** `app/api/v1/example/route.ts`
**Description:** URL-prefix versioned route handler demonstrating the `/api/v1/` pattern.

### Step 6: Write Server Action Utilities
**Action:** `write_to_file`
**File:** `lib/action-utils.ts`
**Description:** `ActionResult<T>` type, `requireAuth()` helper, `actionError()` and `actionSuccess()` helpers for consistent Server Action responses.

### Step 7: Write Example Server Actions
**Action:** `write_to_file`
**File:** `app/actions/example.ts`
**Description:** Server Action using `'use server'`, Zod validation, `requireAuth()`, and returning `ActionResult<T>`.

### Step 8: Write Form Component
**Action:** `write_to_file`
**File:** `components/example-form.tsx`
**Description:** Client component using `useActionState` (React 19) to call the Server Action with loading/error states.

### Step 9: Add Background Task Example
**Action:** `write_to_file`
**File:** `app/api/v1/with-background/route.ts`
**Description:** Route using `after()` API for post-response background work (analytics, logging). Include version compat note.

## Verification
- [ ] `GET /api/health` returns 200 with database status
- [ ] Server Action validates input and returns typed result
- [ ] Form component shows loading state during action execution
- [ ] `pnpm build` succeeds with no type errors

## Troubleshooting
- **Issue:** `after()` API not available
  **Fix:** Requires Next.js 15+. Check `next.config.ts` has `experimental: { after: true }` for Next.js 15 (stable in 16+).
- **Issue:** Server Action returns generic error
  **Fix:** Check `requireAuth()` — ensure auth is configured (Phase 4) or stub it for now.
