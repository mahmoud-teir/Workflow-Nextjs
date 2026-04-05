---
phase: 9
title: Accessibility & Internationalization
role: UI/UX Designer, Frontend Developer
dependencies: [Phase 5]
estimated_time: 3-4 hours
---

# Phase 9: Accessibility & Internationalization — Agent Workflow

## Prerequisites
- [ ] Phase 5 completed (frontend components)
- [ ] Required packages: `next-intl`
- [ ] Translation files prepared (at minimum `en.json`)

## Step-by-Step Execution

### Step 1: Install next-intl
**Action:** `run_command`
```bash
pnpm add next-intl
```

### Step 2: Write i18n Configuration
**Action:** `write_to_file`
**File:** `i18n/config.ts`
**Description:** Shared locale config with supported locales, default locale, and RTL detection helper.

### Step 3: Write i18n Request Handler
**Action:** `write_to_file`
**File:** `i18n/request.ts`
**Description:** `getRequestConfig` using latest next-intl API (`requestLocale`, `hasLocale`).

### Step 4: Write Translation Files
**Action:** `write_to_file`
**Files:** `messages/en.json`, `messages/ar.json` (if RTL needed)
**Description:** Translation messages organized by page/feature namespace.

### Step 5: Write Locale Middleware
**Action:** `edit_file`
**File:** `middleware.ts`
**Description:** Add `createMiddleware` from next-intl to existing middleware. Handle locale detection and routing.

### Step 6: Write Locale Layout
**Action:** `write_to_file`
**File:** `app/[locale]/layout.tsx`
**Description:** Root layout that awaits `params` (Promise in Next.js 15+), sets `lang` and `dir` attributes on `<html>`. RTL support with `dir="rtl"` for Arabic/Hebrew.

### Step 7: Write Locale Switcher
**Action:** `write_to_file`
**File:** `components/locale-switcher.tsx`
**Description:** Dynamic locale switcher using `useRouter` and `usePathname` from next-intl — switches without full page reload.

### Step 8: Add Skip Navigation Link
**Action:** `write_to_file`
**File:** `components/skip-nav.tsx`
**Description:** Keyboard-accessible skip-to-content link. Visible on focus, hidden otherwise.

### Step 9: Write LiveAnnouncer Component
**Action:** `write_to_file`
**File:** `components/live-announcer.tsx`
**Description:** ARIA live region component for announcing dynamic content changes to screen readers.

### Step 10: Add Automated a11y Testing
**Action:** `write_to_file`
**File:** `tests/e2e/accessibility.spec.ts`
**Description:** Playwright tests using `@axe-core/playwright` to scan pages for WCAG 2.2 AA violations.

### Step 11: Document Translation Workflow
**Action:** `write_to_file`
**File:** `docs/TRANSLATION_WORKFLOW.md`
**Description:** Guide for content translation: ICU message syntax, Crowdin/i18n Ally integration, pluralization rules.

## Verification
- [ ] Locale switching works without page reload
- [ ] RTL layout renders correctly for Arabic/Hebrew
- [ ] `dir` attribute changes on `<html>` element
- [ ] Skip nav link visible on keyboard focus
- [ ] axe-core tests pass with no critical violations
- [ ] `pnpm build` passes

## Troubleshooting
- **Issue:** `params` type error in locale layout
  **Fix:** In Next.js 15+, `params` is a Promise: `const { locale } = await params`.
- **Issue:** Translations not loading
  **Fix:** Verify `messages/[locale].json` exists and `getRequestConfig` returns correct messages.
