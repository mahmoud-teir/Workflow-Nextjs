---
phase: 6
title: Advanced Features
role: Full-Stack Developer
dependencies: [Phase 2, Phase 3, Phase 5]
estimated_time: 4-6 hours
---

# Phase 6: Advanced Features — Agent Workflow

## Prerequisites
- [ ] Phase 5 completed (frontend components working)
- [ ] Required env vars: `RESEND_API_KEY`, `UPLOADTHING_TOKEN` (or `BLOB_READ_WRITE_TOKEN`)
- [ ] Required packages: `resend`, `@react-email/components`

## Step-by-Step Execution

### Step 1: Implement Search
**Action:** `write_to_file`
**Files:** `lib/services/search.ts`, `app/(app)/search/page.tsx`
**Description:** Full-text search using database (PostgreSQL `ILIKE` or `tsvector`). Add debounced search input with URL state via `nuqs`.

### Step 2: Implement File Upload
**Action:** `write_to_file`
**Files:** `lib/upload.ts`, `components/file-upload.tsx`
**Description:** Option A: UploadThing, Option B: Vercel Blob. Include file type validation, size limits, and delete action.
```bash
# Option A
pnpm add uploadthing @uploadthing/react
# Option B
pnpm add @vercel/blob
```

### Step 3: Implement Real-time (SSE)
**Action:** `write_to_file`
**Files:** `app/api/events/route.ts`, `hooks/use-sse.ts`
**Description:** Server-Sent Events with proper cleanup — add `request.signal.addEventListener('abort', ...)` to prevent memory leaks. Client hook with exponential backoff reconnection.

### Step 4: Implement Caching Strategy
**Action:** `write_to_file`
**File:** `lib/cache.ts`
**Description:** Wrapper around `unstable_cache` with stability caveat comments. Include `revalidateTag` and `revalidatePath` examples. Note: API may change to `cacheLife`/`cacheTag` in future versions.

### Step 5: Implement Email System
**Action:** `write_to_file`
**Files:** `emails/welcome.tsx`, `lib/email.ts`
**Description:** React Email templates + Resend integration. Use `{ data, error }` return pattern (not try/catch) for proper error handling.
```bash
pnpm add resend @react-email/components
```

### Step 6: Implement Background Jobs
**Action:** `write_to_file`
**Files:** `lib/jobs.ts`, example usage in API route
**Description:** Option A: `after()` API (Vercel, Next.js 15+) for lightweight post-response tasks. Option B: BullMQ for heavy/scheduled jobs with Redis queue.

## Verification
- [ ] Search returns relevant results with debouncing
- [ ] File upload works with size/type validation
- [ ] SSE stream connects, receives events, and reconnects on disconnect
- [ ] Email sends successfully (check Resend dashboard)
- [ ] Cache invalidation works (revalidateTag/revalidatePath)
- [ ] `pnpm build` passes

## Troubleshooting
- **Issue:** SSE connection drops without cleanup
  **Fix:** Ensure `request.signal.addEventListener('abort', () => clearInterval(id))` is in the SSE route handler.
- **Issue:** `unstable_cache` not caching
  **Fix:** Ensure you pass a unique key array as the second argument and `revalidate` seconds in options.
