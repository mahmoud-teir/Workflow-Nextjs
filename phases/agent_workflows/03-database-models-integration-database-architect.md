---
phase: 3
title: Database Models & Integration
role: Database Architect
dependencies: [Phase 1, Phase 2]
estimated_time: 2-4 hours
---

# Phase 3: Database Models & Integration — Agent Workflow

## Prerequisites
- [ ] Phase 2 completed (DB client configured)
- [ ] Required env vars: `DATABASE_URL`
- [ ] Required packages: `drizzle-orm`, `drizzle-kit` (or Prisma/Convex equivalent)

## Step-by-Step Execution

### Step 1: Write Full Database Schema
**Action:** `write_to_file`
**File:** `db/schema.ts`
**Description:** Complete schema with all models (users, posts, etc.). Use Drizzle array-return index syntax: `(table) => [uniqueIndex(...)]`. Include relations, timestamps, and soft-delete columns.

### Step 2: Write Drizzle Config
**Action:** `write_to_file`
**File:** `drizzle.config.ts`
**Description:** Config pointing to schema, migrations output dir, and database connection.

### Step 3: Write Repository Layer
**Action:** `write_to_file`
**File:** `lib/repositories/base.ts`
**Description:** Base repository with typed CRUD operations. Use generics for reuse across models.

### Step 4: Write Service Layer with Caching
**Action:** `write_to_file`
**File:** `lib/services/example-service.ts`
**Description:** Service functions wrapping repository calls. Use `unstable_cache` with stability caveat comments. Include `revalidateTag` for cache invalidation.

### Step 5: Write Zod Validation Schemas
**Action:** `write_to_file`
**File:** `lib/validations/example.ts`
**Description:** Zod schemas matching DB schema for insert/update operations. Export types with `z.infer<>`.

### Step 6: Write Seed Script
**Action:** `write_to_file`
**File:** `db/seed.ts`
**Description:** Seed script using `@faker-js/faker` for realistic test data. Support idempotent re-runs (upsert or truncate-first).
```bash
pnpm add -D @faker-js/faker tsx
```

### Step 7: Run Initial Migration
**Action:** `run_command`
```bash
pnpm drizzle-kit generate
pnpm drizzle-kit migrate
```

### Step 8: Run Seed
**Action:** `run_command`
```bash
pnpm tsx db/seed.ts
```

### Step 9: Set Up Neon Database Branching (Optional)
**Action:** `write_to_file`
**File:** `scripts/create-preview-db.sh`
**Description:** Script to create Neon branch for preview deployments. Use in CI for PR-specific databases.

### Step 10: Document Migration Rollback Strategy
**Action:** `write_to_file`
**File:** `docs/MIGRATION_STRATEGY.md`
**Description:** Two-step column drop process, backup guidance, and rollback procedures.

## Verification
- [ ] `pnpm drizzle-kit migrate` runs without errors
- [ ] `pnpm tsx db/seed.ts` populates database with test data
- [ ] Service layer functions return correct data
- [ ] `pnpm build` passes with no type errors
- [ ] Database has all expected tables and indexes

## Troubleshooting
- **Issue:** Migration fails with existing tables
  **Fix:** Use `drizzle-kit push` for development, `drizzle-kit migrate` for production. Drop DB and re-migrate if needed in dev.
- **Issue:** Index syntax error in Drizzle
  **Fix:** Use array return: `(table) => [uniqueIndex('idx_name').on(table.column)]`, not object return.
