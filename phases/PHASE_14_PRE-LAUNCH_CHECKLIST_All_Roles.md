<a name="phase-14"></a>
# 📌 PHASE 14: PRE-LAUNCH CHECKLIST (All Roles)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.
> **Source:** Based on the official [Next.js Production Checklist](https://nextjs.org/docs/app/guides/production-checklist).

---

### ⚠️ Rule: Nothing is Left Incomplete

**Every page, subpage, service, function, and Server Action MUST be fully built and operational.** There are no placeholders, no `TODO` stubs, no "coming soon" sections, no mock data in production, and no dead routes.

| What Must Be Complete | What "Done" Means |
|---|---|
| **All main pages** (`app/*/page.tsx`) | Render real content with real data flows, not skeleton text or lorem ipsum |
| **All subpages** | Every route defined in the sitemap has a working `page.tsx` with full UI and data binding |
| **All Server Actions** (`actions/*.ts`, `app/**/actions.ts`) | Connected to real database tables, Zod-validated, error-handled, no `throw new Error("TODO")` |
| **All API Routes** (`app/api/**/route.ts`) | Functional handlers with real responses, no `{ message: "Not implemented" }` |
| **All services / data layer** (`lib/services/*.ts`, `lib/repositories/*.ts`) | Implemented against real database queries, no mock returns |
| **All components** (`components/**/*.tsx`) | Fully wired to data sources — no `const data = [{ fake: "item" }]` mocks left in production |
| **Loading / error states** | Every async route has `loading.tsx` and `error.tsx` — not blank or default-only |
| **Auth flows** | Sign up, sign in, sign out, password reset, session check — all connected and working |
| **Navigation / routing** | All links in nav, footer, breadcrumbs, and sidebars resolve to real pages — no `href="#"` |
| **Forms** | All forms submit to real Server Actions with validation and feedback — no disabled or dummy forms |

**Hard rule before moving past any phase:** If any route, action, or service is left as a stub, placeholder, or mock — it **MUST be completed** before the build is considered valid.

---

### Prompt 14.1: Next.js Automatic Optimizations Audit

```text
You are a Principal Engineer performing a final pre-launch audit. Before checking manual optimizations, verify that the application is correctly leveraging Next.js's AUTOMATIC optimizations — features that work by default when not accidentally broken.

Next.js provides these free, out-of-the-box:
1. **Server Components** — Components render on the server by default, reducing client-side JS bundle. Confirm no accidental `'use client'` directives on pages/layouts that don't need client-side interactivity.
2. **Code-splitting** — Each route segment is automatically code-split. Verify that heavy shared libraries aren't being imported at the root layout (which would bundle them into every route).
3. **Prefetching** — `<Link>` components automatically prefetch routes when they enter the viewport. Confirm you are using `<Link>` (not raw `<a>`) for internal navigation.

Constraints:
- Audit every file in `app/` that contains `'use client'`. For each, justify WHY it needs to be a Client Component. If it doesn't use hooks, events, or browser APIs, remove the directive.
- Confirm no `<a href>` tags are used for internal routes (must be `<Link>`).

Required Output Format:
1. A table of all `'use client'` files with justification (Hook/Event/Browser API used).
2. A list of any raw `<a>` tags found for internal links, with file paths.
3. A root layout import audit showing the total JS cost of shared dependencies.
```

✅ **Verification Checklist:**
- [ ] Every `'use client'` directive has a documented justification.
- [ ] Zero `<a href="/...">` tags exist for internal navigation.
- [ ] Root `layout.tsx` imports are minimal (no heavy chart/editor libraries).

---

### Prompt 14.2: Routing, Rendering & Data Fetching Checklist

```text
You are a Senior Next.js Architect. Audit routing, rendering strategy, and data fetching patterns before launch.

Required Audit Items (from Next.js official production checklist):

**Routing & Rendering:**
- [ ] Layouts are used to share UI across routes (enabling partial rendering).
- [ ] `loading.tsx` files exist for every route segment that performs async data fetching.
- [ ] `error.tsx` files exist at least at the root app level and for critical route groups.
- [ ] `not-found.tsx` exists at the root level with a branded 404 page.
- [ ] Request-time APIs (`cookies()`, `searchParams`) are wrapped in `<Suspense>` boundaries to avoid opting the entire page into synchronous dynamic rendering.
- [ ] Evaluate Partial Prerendering (PPR) for pages mixing static shells with dynamic content.

**Data Fetching:**
- [ ] All data fetching occurs in Server Components (no `useEffect` → `fetch` patterns for initial page data).
- [ ] `<Suspense>` boundaries wrap data-heavy components so the rest of the page streams immediately.
- [ ] Parallel data fetching is used (multiple `Promise.all` or parallel `await`) to eliminate sequential network waterfalls.
- [ ] Data caching is verified: `unstable_cache`/`cacheLife` for cross-request caching, React `cache()` for per-request deduplication.
- [ ] Static assets (favicons, robots, logos) live in `/public`, not fetched dynamically.

Constraints:
- Generate the audit as a checklist with pass/fail per route group.
- Flag any data fetching in Client Components as `⚠️ REVIEW`.

Required Output Format: A markdown checklist grouped by route group (e.g., `(marketing)`, `(dashboard)`, `(auth)`) with pass/fail for each item above.
```

✅ **Verification Checklist:**
- [ ] Every route group has `loading.tsx` and `error.tsx` files.
- [ ] No `useEffect → fetch` patterns exist in page-level components for initial data.

---

### Prompt 14.3: UI, Accessibility & Performance Checklist

```text
You are a Web Performance and Accessibility Engineer. Audit the frontend layer against Next.js official production recommendations.

Required Audit Items:

**Font Optimization:**
- [ ] All fonts use `next/font` (Google or local), eliminating external font requests and preventing FOUT/CLS.
- [ ] No `<link rel="stylesheet" href="https://fonts.googleapis.com/...">` tags exist.

**Image Optimization:**
- [ ] All images use `<Image>` from `next/image` with `width`/`height` or `fill` props.
- [ ] The LCP image (hero/banner) has `priority={true}` set.
- [ ] AVIF format is enabled in `next.config.ts` → `images.formats: ['image/avif', 'image/webp']`.
- [ ] Blur placeholder data URLs are provided for above-the-fold images to eliminate CLS.

**Third-Party Scripts:**
- [ ] All analytics/tracking scripts use `<Script>` from `next/script` with `strategy="afterInteractive"` or `strategy="worker"` (Partytown).
- [ ] No raw `<script>` tags exist in layout files.

**Accessibility:**
- [ ] `eslint-plugin-jsx-a11y` is configured and passes with zero errors.
- [ ] All interactive elements have visible focus indicators.
- [ ] All images have meaningful `alt` text (or empty `alt=""` for decorative images).
- [ ] Color contrast meets WCAG 2.2 AA minimum (4.5:1 for normal text, 3:1 for large text).

Constraints:
- Use `next build` output to verify First Load JS per route. Flag any route exceeding 100KB.
- Run Lighthouse in incognito mode; all scores must be ≥90.

Required Output Format:
1. Font audit table (font name → loading method → pass/fail).
2. Image audit table (component → has width/height → has priority → format).
3. Script audit table (script name → loading strategy → blocking main thread?).
4. Lighthouse score summary: Performance / Accessibility / Best Practices / SEO.
```

✅ **Verification Checklist:**
- [ ] Lighthouse Performance ≥ 90 in incognito mode.
- [ ] Lighthouse Accessibility ≥ 90.
- [ ] Zero layout shift from font loading (CLS < 0.1).
- [ ] LCP < 2.5s on 4G throttled connection.

---

### Prompt 14.4: Security & Data Safety Checklist

```text
You are a Security Engineer performing a final security review before production launch.

Required Audit Items (from Next.js official production checklist):

**Server Actions:**
- [ ] Every Server Action performs its own authentication and authorization checks (never rely on middleware alone).
- [ ] All Server Action inputs are validated with Zod schemas.
- [ ] A Data Access Layer (DAL) centralizes all database queries — no raw SQL or ORM calls inside Server Actions directly.
- [ ] Rate limiting is applied to mutation-heavy Server Actions (login, registration, payments).

**Environment Variables:**
- [ ] `.env`, `.env.local`, `.env.production.local` are in `.gitignore`.
- [ ] Only variables that MUST be visible in the browser use the `NEXT_PUBLIC_` prefix. Audit for accidental exposure of secrets.
- [ ] Required environment variables are validated at build time (see `env.ts` pattern from Phase 1).

**Content Security Policy:**
- [ ] A CSP header is set via `middleware.ts` or `next.config.ts` `headers()`.
- [ ] The CSP blocks inline scripts unless nonce-based (`script-src 'nonce-...'`).

**Data Tainting (Experimental):**
- [ ] If `experimental.taint` is enabled, verify that sensitive server objects (user sessions, tokens) cannot accidentally serialize into Client Component props.

**Headers:**
- [ ] `X-Content-Type-Options: nosniff` is set.
- [ ] `X-Frame-Options: DENY` (or SAMEORIGIN) is set.
- [ ] `Referrer-Policy: strict-origin-when-cross-origin` is set.
- [ ] `poweredByHeader: false` is configured in `next.config.ts` to suppress the `X-Powered-By: Next.js` header.

Constraints:
- Produce a PASS/FAIL report. Any FAIL on a P0 item blocks launch.

Required Output Format: A prioritized security audit report (P0/P1/P2) with pass/fail status and remediation steps for each failure.
```

✅ **Verification Checklist:**
- [ ] Run `curl -I https://staging.example.com` and verify all security headers are present.
- [ ] Attempt to call a Server Action without authentication — it must return 401/403.
- [ ] Search codebase for `NEXT_PUBLIC_` — confirm none contain secrets.

---

### Prompt 14.5: Metadata, SEO & Monitoring Checklist

```text
You are an SEO and Observability Specialist. Audit SEO readiness and monitoring setup before launch.

Required Audit Items:

**Metadata & SEO:**
- [ ] Every page exports a `metadata` object or `generateMetadata()` function with descriptive `title` and `description`.
- [ ] OpenGraph images are configured via `opengraph-image.tsx` / `twitter-image.tsx` (or static files).
- [ ] `sitemap.ts` exists and dynamically generates all public routes.
- [ ] `robots.ts` exists with correct `allow`/`disallow` rules.
- [ ] Structured data (JSON-LD) is implemented for key content pages.
- [ ] `<h1>` is used exactly once per page.

**Type Safety:**
- [ ] TypeScript strict mode is enabled (`"strict": true` in `tsconfig.json`).
- [ ] The Next.js TypeScript plugin is configured in the editor for autocomplete on route params, metadata, etc.
- [ ] `tsc --noEmit` passes with zero errors.

**Post-Launch Monitoring:**
- [ ] `instrumentation.ts` is configured to initialize monitoring tools on server startup.
- [ ] OpenTelemetry is set up to export traces and metrics to an observability platform (e.g., Datadog, New Relic, Sentry).
- [ ] `useReportWebVitals` hook is implemented to capture real-user Core Web Vitals field data.
- [ ] Error tracking (Sentry or equivalent) is integrated with source maps uploaded for production builds.

Constraints:
- Verify SEO tags by rendering the page with `curl` or a fetch-based bot simulator.
- Confirm `robots.txt` doesn't accidentally block critical pages.

Required Output Format:
1. SEO audit table: page route → title → description → OG image → structured data.
2. Monitoring readiness checklist with integration status.
3. `useReportWebVitals` implementation code for the root layout.
```

✅ **Verification Checklist:**
- [ ] `curl https://staging.example.com` returns `<title>`, `<meta name="description">`, and OG tags in the HTML.
- [ ] `curl https://staging.example.com/sitemap.xml` returns a valid XML sitemap.
- [ ] `curl https://staging.example.com/robots.txt` returns expected rules.

---

### Prompt 14.9: Debugging Guide (Pre-Launch Hurdles)

```text
You are a Senior Next.js Troubleshooting Expert. Document the solutions for the most frequent issues encountered during final staging tests.

Constraints:
- Focus on Next.js 15/16+ specific errors (e.g., async params, React Compiler edge cases, `use cache` issues).
- Provide copy-pasteable snippets for fixes.

Required Output Format: Document solutions for:
1. **Hydration Mismatches:** How to track them down using `suppressHydrationWarning` and safe client mounts.
2. **`searchParams` / `params` Async Errors:** Explaining the Next.js 15 breaking change where params are now async.
3. **`use cache` / `cacheLife` / `cacheTag` Issues:** Explaining how to manage the new caching directives (Next.js 16+).
4. **API Route Timeouts:** Vercel limits vs Edge Runtime vs `maxDuration` config.
5. **Server Component Importing Client Code:** Solving the "useState in Server Component" crash.
6. **`<Form>` component progressive enhancement:** Ensuring the `next/form` `<Form>` component works with Server Actions.
7. **View Transitions:** Handling the experimental `viewTransition` config and `startViewTransition` API safely.

⚠️ Common Pitfalls:
- **Pitfall:** Using `useFormStatus` inside the same component that renders the `<form>`, causing it to never trigger.
- **Solution:** `useFormStatus` must ALWAYS be used inside a child component rendered *within* the `<form>` wrapper.
```

---

### Prompt 14.7: Completeness Audit — No Placeholders, No Mocks

```text
You are a QA Lead performing a final completeness audit before launch. Your job is to ensure EVERYTHING is fully built and operational — no stubs, no placeholders, no mock data.

**Scan the entire codebase for these red flags:**

1. **TODO comments:** Search for `TODO`, `FIXME`, `HACK`, `TEMP`, `STUB`, `PLACEHOLDER`. Each must be resolved or have an approved exception.

2. **Mock data in production:** Any file in `lib/`, `components/`, `app/`, or `services/` that defines hardcoded fake data arrays (`const users = [{ name: "John Doe" }]`, `const products = [...]`, lorem ipsum text, placeholder emails). These are only allowed in dedicated `__tests__/` or `seed.ts` files.

3. **Unimplemented routes:** Compare the sitemap (Section 4 of `.stitch/SITE.md` or `app/` route structure) against actual `page.tsx` files. Every route must have a fully functional page — no "Coming Soon" or "Under Construction" pages.

4. **Empty Server Actions:** Any Server Action that returns early, throws `new Error("Not implemented")`, or has an empty body. All actions must have real database operations.

5. **Dead links:** Any `<Link>` or `<a>` with `href="#"`, `href="/"`, or `href="/#"` that doesn't navigate to a real subpage. All internal links must resolve to actual routes.

6. **Unconnected forms:** Any `<form>` that doesn't have a real `action` pointing to a functional Server Action or API route.

7. **Disabled UI elements:** Any button, input, or link with `disabled={true}` that doesn't have a valid business reason (e.g., loading state). No permanently disabled elements.

8. **Missing error handling:** Every async operation must have try/catch, error boundaries, or Zod validation. No raw `await` without error handling.

**Required Output Format:**
1. A completeness report grouped by category (Pages, Actions, Services, Components, Routes).
2. For each category: list every file, its status (✅ Complete / ⚠️ Partial / ❌ Incomplete), and specific missing pieces for any non-complete items.
3. A final verdict: **PASS** (everything complete) or **FAIL** (list what must be finished).

**Verdict rule:** If ANY item is marked ⚠️ or ❌, the audit FAILS and the project cannot ship.
```

✅ **Verification Checklist:**
- [ ] Completeness report generated with zero items marked ⚠️ or ❌.
- [ ] Grep for `TODO|FIXME|mockData|placeholder|coming.soon|lorem.ipsum` returns zero results in source files (excluding tests and seed scripts).

---

### Prompt 14.8: Automated Pre-launch Validation Script

```text
You are a DevOps Engineer. Write a CLI bash script that developers must run before they are allowed to promote code to production.

Constraints:
- The script must run without requiring global dependencies (use `npx` or `pnpm exec`).
- It must halt (`exit 1`) immediately if any critical check fails.
- Include checks derived from the official Next.js production checklist.

Required Output Format: Provide a `bash` script (`scripts/pre-launch-check.sh`) that sequentially executes:
1. Type checking (`tsc --noEmit`).
2. Linting (`biome check .`).
3. Unit Tests (`vitest run`).
4. Production Build (`next build`) — verify it succeeds without errors.
5. Bundle size check (`ANALYZE=true next build` — ensure no route exceeds 100KB First Load JS).
6. Security Audit (`npm audit --audit-level=high`).
7. Environment variables verification (asserting all required keys from `env.ts` exist).
8. SEO verification (assert `sitemap.xml`, `robots.txt`, and `manifest.json` are generated in `.next`).
9. Security headers check (start the server, `curl -I localhost:3000`, verify CSP/X-Frame-Options/etc.).
10. **Completeness check** — grep for `TODO|FIXME|STUB|PLACEHOLDER|coming.soon|lorem.ipsum` in all source files (excluding `__tests__/` and `seed.ts`). Halt with `exit 1` if any found.
11. **Dead link check** — grep for `href="#"` or `href="/#"` in all `.tsx` files (excluding buttons with explicit `onClick` handlers). Halt with `exit 1` if any found.
12. Lighthouse CI (optional — `npx @lhci/cli autorun` if configured).
```

✅ **Verification Checklist:**
- [ ] Run `bash scripts/pre-launch-check.sh`.
- [ ] Ensure it accurately catches a missing environment variable and aborts.
- [ ] Ensure it catches a route with excessive bundle size and warns.

---

### Prompt 14.10: Local Production Smoke Test

```text
You are a QA Lead. Before deploying, perform a local production smoke test to catch issues that only appear in production mode.

Constraints:
- MUST test with `next build && next start` (NOT `next dev`).
- Test on a throttled network (Chrome DevTools 4G simulation).

Required Verification Steps:
1. Run `next build` — confirm zero build errors and review the output table for unexpected dynamic routes.
2. Run `next start` — navigate to every primary route manually.
3. Open Chrome DevTools → Performance → Record a page load. Verify:
   - LCP element loads within 2.5s.
   - No long tasks (>50ms) block the main thread during initial render.
   - INP (click a button/link) responds within 200ms.
4. Open Chrome DevTools → Network. Verify:
   - No external font requests (fonts should be self-hosted via `next/font`).
   - Images are served as AVIF/WebP, not PNG/JPEG.
   - No 404 errors for any static assets.
5. Test error boundaries:
   - Navigate to a non-existent route → branded `not-found.tsx` should render.
   - Trigger a server error → `error.tsx` should render gracefully.
6. Test auth flows:
   - Attempt to access a protected route while logged out → redirect to login.
   - Log in → verify session persists across hard refresh.

Required Output Format: A pass/fail smoke test report with screenshots for any failures.
```

✅ **Verification Checklist:**
- [ ] `next build` output shows zero errors.
- [ ] `next start` renders all primary routes without JavaScript errors in the console.
- [ ] Core Web Vitals pass on throttled 4G connection.
- [ ] **Completeness audit:** Zero `TODO` comments, no mock data (`const data = [...]`), no placeholder strings (`"lorem ipsum"`, `"coming soon"`, `"TBD"`), no `href="#"` links, no empty Server Actions, no unimplemented API routes. Every route defined in the sitemap is fully built.

---
📎 **Related Phases:**
- Prerequisites: [Phase 13: Deployment & CI/CD](./PHASE_13_DEPLOYMENT__CICD_DevOps_Engineer.md)
- Proceeds to: [Phase 15: AI & LLM Integration](./PHASE_15_AI__LLM_INTEGRATION_AI_Engineer.md) (Optional)
