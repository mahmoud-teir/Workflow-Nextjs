---
phase: 5
title: Frontend Development
role: Frontend Developer
dependencies: [Phase 1, Phase 4]
estimated_time: 4-6 hours
---

# Phase 5: Frontend Development — Agent Workflow

## Prerequisites
- [ ] Phase 4 completed (auth working)
- [ ] Required packages: `next-themes`, `sonner`, `nuqs`, `zustand`
- [ ] Design system tokens defined (Phase 0)

## Step-by-Step Execution

### Step 1: Write Global CSS with Tailwind v4
**Action:** `write_to_file`
**File:** `app/globals.css`
**Description:** Tailwind v4 `@theme` directive with oklch colors. Dark mode uses `.dark` class selector (NOT `@media prefers-color-scheme`) for next-themes compatibility. Add `prefers-reduced-motion` query.

### Step 2: Write Theme Provider
**Action:** `write_to_file`
**File:** `components/theme-provider.tsx`
**Description:** Wrapper around `next-themes` ThemeProvider with `attribute="class"` and `defaultTheme="system"`.

### Step 3: Update Root Layout
**Action:** `edit_file`
**File:** `app/layout.tsx`
**Description:** Add ThemeProvider, Toaster from sonner, and metadata. Import globals.css.

### Step 4: Write Skeleton Components
**Action:** `write_to_file`
**File:** `components/ui/skeleton.tsx`
**Description:** Reusable Skeleton component for Suspense fallbacks. Include CardSkeleton, TableSkeleton variants.

### Step 5: Write Server Component Pages
**Action:** `write_to_file`
**Files:** `app/(app)/dashboard/page.tsx`, etc.
**Description:** Server Components (default) that fetch data directly. Use `Suspense` with Skeleton fallbacks. Remember: `searchParams` is a Promise in Next.js 15+ — must `await` it.

### Step 6: Write Client Components
**Action:** `write_to_file`
**Description:** Interactive components with `'use client'`. Note: React Compiler handles memoization automatically — no manual `useMemo`/`useCallback` needed.

### Step 7: Write Forms (3 Patterns)
**Action:** `write_to_file`
**Description:** Implement forms using:
- Pattern A: `useActionState` + Server Action (simple forms)
- Pattern B: React Hook Form + Zod + Server Action (complex forms)
- Pattern C: Conform (progressive enhancement)

### Step 8: Set Up Client State Management
**Action:** `write_to_file`
**File:** `lib/store.ts`
**Description:** Zustand store for client-side state. Use `nuqs` for URL state management.
```bash
pnpm add zustand nuqs
```

### Step 9: Add View Transitions (Optional)
**Action:** `write_to_file`
**File:** `lib/view-transitions.ts`
**Description:** `startViewTransition` wrapper for smooth page transitions. Progressive enhancement — falls back gracefully.

### Step 10: Add Toast Notifications
**Action:** `edit_file`
**File:** `app/layout.tsx`
**Description:** Ensure `<Toaster />` from sonner is in root layout. Use `toast.success()`, `toast.error()` in client components.

## Verification
- [ ] Dark mode toggle works (class-based, not media query)
- [ ] Suspense boundaries show skeletons during loading
- [ ] Forms submit correctly with validation errors displayed
- [ ] `searchParams` are awaited in all Server Components
- [ ] `pnpm build` passes with no type errors

## Troubleshooting
- **Issue:** Dark mode flashes wrong theme on load
  **Fix:** Ensure ThemeProvider has `attribute="class"` and is in root layout. `suppressHydrationWarning` on `<html>`.
- **Issue:** `searchParams` type error
  **Fix:** In Next.js 15+, `searchParams` is `Promise<{...}>`. Must `await` it: `const params = await searchParams`.
