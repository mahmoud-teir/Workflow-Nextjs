---
phase: 19
title: API Documentation & Versioning
role: Backend Engineer
dependencies: [Phase 2]
estimated_time: 2-3 hours
---

# Phase 19: API Documentation & Versioning — Agent Workflow

## Prerequisites
- [ ] Phase 2 completed (API routes exist)
- [ ] Required packages: `@scalar/nextjs-api-reference`, `openapi-fetch`

## Step-by-Step Execution

### Step 1: Install Dependencies
**Action:** `run_command`
```bash
pnpm add @scalar/nextjs-api-reference openapi-fetch
pnpm add -D openapi-typescript
```

### Step 2: Write OpenAPI Specification
**Action:** `write_to_file`
**File:** `lib/openapi.ts`
**Description:** OpenAPI 3.1 document with info, servers, security schemes (bearer + cookie), reusable schemas (Error, Pagination, User), and path definitions for all API endpoints.

### Step 3: Write Spec Endpoint
**Action:** `write_to_file`
**File:** `app/api/docs/route.ts`
**Description:** GET handler returning `openApiSpec` as JSON.

### Step 4: Write Documentation UI
**Action:** `write_to_file`
**File:** `app/api/docs/ui/route.ts`
**Description:** Scalar API Reference UI mounted at `/api/docs/ui`. Configure theme, dark mode, and spec URL.

### Step 5: Set Up API Versioning
**Action:** `write_to_file`
**Files:** `app/api/v1/users/route.ts`, `app/api/v2/users/route.ts`
**Description:** URL-prefix versioned routes. V1 with offset pagination, V2 with cursor pagination. Shared service layer underneath.

### Step 6: Write Deprecation Headers Utility
**Action:** `write_to_file`
**File:** `lib/api-versioning.ts`
**Description:** `withDeprecationHeaders()` function that adds `Sunset`, `Deprecation`, and `Link` headers to sunset versions.

### Step 7: Generate TypeScript Client
**Action:** `run_command`
```bash
pnpm openapi-typescript http://localhost:3000/api/docs -o types/api.d.ts
```

### Step 8: Write Type-Safe API Client
**Action:** `write_to_file`
**File:** `lib/api-client.ts`
**Description:** `openapi-fetch` client with generated types. Fully typed request params and response data.

### Step 9: Write API Changelog
**Action:** `write_to_file`
**File:** `CHANGELOG-API.md`
**Description:** Keep a Changelog format adapted for API versions. Document breaking changes, additions, and deprecations.

### Step 10: Add Breaking Change Detection to CI
**Action:** `write_to_file`
**File:** `.github/workflows/api-compat.yml`
**Description:** Compare OpenAPI spec between base and PR branch. Comment on PR if breaking changes detected.

## Verification
- [ ] `/api/docs` returns valid OpenAPI JSON
- [ ] `/api/docs/ui` renders Scalar documentation
- [ ] V1 and V2 endpoints return different response shapes
- [ ] Generated types match API responses
- [ ] Type-safe client catches type errors at compile time
- [ ] CI detects breaking API changes

## Troubleshooting
- **Issue:** Scalar UI blank page
  **Fix:** Verify spec URL is accessible. Check CSP allows Scalar CDN assets.
- **Issue:** Generated types out of date
  **Fix:** Run `pnpm generate:api-types` after any API change. Add to CI pipeline.
