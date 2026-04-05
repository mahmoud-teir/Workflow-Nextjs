---
phase: 4
title: Authentication & Authorization
role: Security Expert
dependencies: [Phase 1, Phase 2, Phase 3]
estimated_time: 3-4 hours
---

# Phase 4: Authentication & Authorization — Agent Workflow

## Prerequisites
- [ ] Phase 3 completed (database schema with users table)
- [ ] Required env vars: `BETTER_AUTH_SECRET`, `BETTER_AUTH_URL`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- [ ] Required packages: `better-auth`

## Step-by-Step Execution

### Step 1: Install Better Auth
**Action:** `run_command`
```bash
pnpm add better-auth
```

### Step 2: Write Auth Configuration
**Action:** `write_to_file`
**File:** `lib/auth.ts`
**Description:** Better Auth server config with database adapter, OAuth providers (GitHub, Google), admin plugin, `cookieCache`, account linking, and GDPR deletion callback.

### Step 3: Write Auth API Route
**Action:** `write_to_file`
**File:** `app/api/auth/[...all]/route.ts`
**Description:** Catch-all route handler using `auth.handler`.

### Step 4: Write Auth Client
**Action:** `write_to_file`
**File:** `lib/auth-client.ts`
**Description:** Client-side auth helpers: `signIn`, `signOut`, `useSession`. Export typed client.

### Step 5: Write Auth Middleware
**Action:** `write_to_file`
**File:** `middleware.ts`
**Description:** Next.js middleware that protects routes. Note: CSP and security headers are handled in Phase 8.

### Step 6: Write Authorization Helper
**Action:** `write_to_file`
**File:** `lib/authorize.ts`
**Description:** `authorize(requiredRole)` function returning typed `AuthResult` (not throwing raw Error). Used in Server Actions and API routes.

### Step 7: Write PermissionGate Component
**Action:** `write_to_file`
**File:** `components/permission-gate.tsx`
**Description:** Client component that conditionally renders children based on user role/permissions.

### Step 8: Write Login/Signup Pages
**Action:** `write_to_file`
**Files:** `app/(marketing)/login/page.tsx`, `app/(marketing)/signup/page.tsx`
**Description:** Auth pages with OAuth buttons, email/password form, and error handling.

### Step 9: Add Passkey Support (Optional)
**Action:** `edit_file`
**File:** `lib/auth.ts`
**Description:** Add passkey plugin to Better Auth config. Include UX guidance for registration flow, login flow, and device compatibility fallbacks.

### Step 10: Add Account Deletion Flow
**Action:** `write_to_file`
**File:** `app/(app)/settings/delete-account/page.tsx`
**Description:** GDPR right to erasure — confirmation dialog, Server Action to delete user data, and redirect.

## Verification
- [ ] OAuth login works (GitHub, Google)
- [ ] Email/password signup and login work
- [ ] Protected routes redirect to login
- [ ] `authorize('admin')` blocks non-admin users
- [ ] Account deletion removes user data
- [ ] `pnpm build` passes

## Troubleshooting
- **Issue:** OAuth redirect fails
  **Fix:** Check `BETTER_AUTH_URL` matches your dev URL (http://localhost:3000). Verify OAuth app callback URLs in GitHub/Google console.
- **Issue:** Session not persisting
  **Fix:** Check `BETTER_AUTH_SECRET` is set. Ensure cookies are enabled and `sameSite` settings match your domain.
