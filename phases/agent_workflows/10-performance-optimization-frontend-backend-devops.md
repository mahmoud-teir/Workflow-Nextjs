---
phase: 10
title: Performance Optimization
role: Frontend Developer, Backend Developer, DevOps
dependencies: [Phase 5, Phase 6]
estimated_time: 2-4 hours
---

# Phase 10: Performance Optimization — Agent Workflow

## Prerequisites
- [ ] Phase 6 completed (features to optimize)
- [ ] Required packages: `@next/bundle-analyzer`

## Step-by-Step Execution

### Step 1: Add Resource Hints
**Action:** `edit_file`
**File:** `app/layout.tsx`
**Description:** Add `<link rel="preconnect">` and `<link rel="dns-prefetch">` for external domains (fonts, analytics, CDN).

### Step 2: Optimize Images
**Action:** `review`
**Description:** Audit all images — ensure `next/image` is used everywhere with proper `width`/`height`, `priority` on above-fold images, and AVIF/WebP format.

### Step 3: Optimize Fonts
**Action:** `review`
**Description:** Verify `next/font` is used with `display: 'swap'`. No external font CDN requests.

### Step 4: Implement INP Optimizations
**Action:** `edit_file`
**Description:** Wrap expensive event handlers in `startTransition`. Add debouncing for search inputs. Use `content-visibility: auto` for long lists. Consider virtualization for 100+ items.

### Step 5: Set Up Partytown for Third-Party Scripts
**Action:** `write_to_file`
**File:** `components/analytics-scripts.tsx`
**Description:** Move non-critical third-party scripts to web worker via Partytown or `strategy="afterInteractive"`.

### Step 6: Add Bundle Analyzer
**Action:** `edit_file`
**File:** `next.config.ts`
**Description:** Add `@next/bundle-analyzer` wrapped with `ANALYZE` env check.
```bash
pnpm add -D @next/bundle-analyzer
```

### Step 7: Optimize Database Queries
**Action:** `review`
**Description:** Run `EXPLAIN ANALYZE` on slow queries. Add composite indexes for common query patterns. Add partial indexes for filtered queries. Fix N+1 queries with joins.

### Step 8: Add Dynamic Imports
**Action:** `edit_file`
**Description:** Use `dynamic(() => import(...))` for heavy components not needed on initial load (charts, editors, modals).

### Step 9: Add React Profiler (Development)
**Action:** `write_to_file`
**File:** `components/dev-profiler.tsx`
**Description:** React Profiler wrapper for identifying slow renders in development. Logs render times to console.

### Step 10: Run Bundle Analysis
**Action:** `run_command`
```bash
ANALYZE=true pnpm build
```
**Description:** Review bundle output. Common wins: tree-shake barrel exports, replace heavy libs (moment→dayjs, lodash→lodash-es).

## Verification
- [ ] Lighthouse Performance score > 90
- [ ] Core Web Vitals: LCP < 2.5s, INP < 200ms, CLS < 0.1
- [ ] Bundle size: initial JS < 200KB gzipped
- [ ] No layout shift on page load
- [ ] Database queries use indexes (no full table scans)

## Troubleshooting
- **Issue:** Large bundle size from barrel exports
  **Fix:** Import from specific paths: `import { Button } from '@/components/ui/button'` not `from '@/components/ui'`.
- **Issue:** INP > 200ms on interactions
  **Fix:** Wrap state updates in `startTransition`. Debounce rapid inputs. Use virtualization for long lists.
