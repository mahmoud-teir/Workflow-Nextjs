---
phase: 17
title: Mobile & PWA
role: Frontend Engineer
dependencies: [Phase 5]
estimated_time: 3-4 hours
---

# Phase 17: Mobile & PWA — Agent Workflow

## Prerequisites
- [ ] Phase 5 completed (frontend working)
- [ ] Required packages: `@serwist/next`, `serwist`

## Step-by-Step Execution

### Step 1: Install Serwist
**Action:** `run_command`
```bash
pnpm add @serwist/next
pnpm add -D serwist
```

### Step 2: Write Service Worker
**Action:** `write_to_file`
**File:** `app/sw.ts`
**Description:** Custom service worker with Serwist: precaching from manifest, runtime caching strategies (CacheFirst for assets, NetworkFirst for API, StaleWhileRevalidate for pages), offline fallback, and notification click handler.

### Step 3: Update Next.js Config
**Action:** `edit_file`
**File:** `next.config.ts`
**Description:** Wrap config with `withSerwist({ swSrc: 'app/sw.ts', swDest: 'public/sw.js' })`.

### Step 4: Write Web App Manifest
**Action:** `write_to_file`
**File:** `public/manifest.webmanifest`
**Description:** Modern manifest with `name`, `short_name`, `icons` (including maskable), `screenshots`, `shortcuts`, and `share_target`.

### Step 5: Write Offline Fallback Page
**Action:** `write_to_file`
**File:** `app/offline/page.tsx`
**Description:** User-friendly offline page with retry button.

### Step 6: Write Install Prompt Component
**Action:** `write_to_file`
**File:** `components/install-prompt.tsx`
**Description:** Typed `BeforeInstallPromptEvent`, detect installed state, show/hide install button.

### Step 7: Write Update Prompt Component
**Action:** `write_to_file`
**File:** `components/update-prompt.tsx`
**Description:** Detect waiting service worker, show update banner, trigger `SKIP_WAITING` and reload.

### Step 8: Write iOS Install Instructions
**Action:** `write_to_file`
**File:** `components/ios-install-instructions.tsx`
**Description:** iOS doesn't fire `beforeinstallprompt` — show manual "Add to Home Screen" instructions for Safari users.

### Step 9: Implement Push Notifications
**Action:** `write_to_file`
**Files:** `app/actions/push.ts`, `components/push-permission.tsx`
**Description:** VAPID keys, web-push server, permission request UI, subscription storage, and expired subscription cleanup.
```bash
pnpm add web-push
```

### Step 10: Implement Background Sync
**Action:** `write_to_file`
**File:** `lib/background-sync.ts`
**Description:** Queue offline form submissions in localStorage, process on sync event in service worker.

### Step 11: Set Up Capacitor (Optional)
**Action:** `run_command`
```bash
pnpm add @capacitor/core
pnpm add -D @capacitor/cli
npx cap init "My App" com.myapp.app --web-dir=out
```
**Action:** `write_to_file`
**File:** `capacitor.config.ts`
**Description:** Capacitor config with plugins (Camera, Haptics, StatusBar), splash screen, and dev server URL.

## Verification
- [ ] App installable via browser prompt
- [ ] Offline fallback page shows when disconnected
- [ ] Service worker caches assets and API responses
- [ ] Update prompt appears when new version deployed
- [ ] Push notifications send and receive
- [ ] iOS install instructions show for Safari users

## Troubleshooting
- **Issue:** Service worker not registering
  **Fix:** Ensure `disable: process.env.NODE_ENV === 'development'` in Serwist config. SW only works in production or with HTTPS.
- **Issue:** Push notifications not working on iOS
  **Fix:** iOS 16.4+ required, and app must be added to Home Screen first. Show guidance to users.
