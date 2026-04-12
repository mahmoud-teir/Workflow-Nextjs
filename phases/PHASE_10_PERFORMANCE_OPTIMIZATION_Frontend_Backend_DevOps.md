<a name="phase-10"></a>
# 📌 PHASE 10: PERFORMANCE OPTIMIZATION (Performance Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 10.1: Core Web Vitals Optimization

```text
You are a Web Performance Engineer. Optimize Core Web Vitals (CWV) for a Next.js application.

Targets:
- LCP (Largest Contentful Paint): < 2.5s
- INP (Interaction to Next Paint): < 200ms
- CLS (Cumulative Layout Shift): < 0.1

Constraints:
- Any images above the fold MUST use `priority={true}` to prevent LCP degradation.
- Ensure fonts do not result in Flash of Unstyled Text (FOUT) or Layout Shifts; use `next/font`.
- Third-party scripts must NEVER block the main thread parser.

Required Output Format: Provide complete code and explanation for:
1. **Image Optimization**: `next/image` with AVIF formats configured in `next.config.ts`, handling Blur Data URLs to eliminate CLS.
2. **Third-party scripts**: `next/script` demonstrating `strategy="worker"` (via Partytown) for heavy analytics trackers.
3. **INP Optimization**: Explain how to use React 19's `startTransition` to unblock the main thread when users type into a heavy filtering input.

⚠️ Common Pitfalls:
- **Pitfall:** Adding `loading="lazy"` or omitting `priority` on the Hero Image, heavily penalizing the LCP score.
- **Solution:** Always apply `priority={true}` to the largest visible asset on initial render.
```

✅ **Verification Checklist:**
- [ ] Run Lighthouse in an incognito window. LCP must be green (<2.5s).
- [ ] Run a heavy CPU throttling test via Chrome DevTools and type in form inputs. Inputs must not freeze (INP optimization).

---

### Prompt 10.2: Backend Query Optimization & Caching

```text
You are a Database Performance Expert. Optimize backend data delivery.

Constraints:
- Do not let the N+1 query problem slip into standard relations. Require explicit `include` (Prisma) or joins (Drizzle).
- Do not hit the database for relatively static global content (e.g., Marketing site navigation).
- Beware of caching personalized user data (like user profiles) globally.

Required Output Format: Provide comprehensive solutions for:
1. **Database Indexing**: Explain `EXPLAIN ANALYZE` and provide SQL scripts for creating composite/partial indexes matching common app routes.
2. **Data Cache**: Differentiate between React `cache()` (per-request dedupe) and Next.js `unstable_cache` / `cacheLife` (cross-request caching).
3. **Edge Caching**: Add Cache-Control headers natively to Route Handlers for generic endpoints (`stale-while-revalidate`).

⚠️ Common Pitfalls:
- **Pitfall:** `unstable_cache` memoizing user-specific data and accidentally leaking it to a different user.
- **Solution:** Only use `unstable_cache` for global data (like categories, top posts). Use `cache()` for request-specific deduplication.
```

✅ **Verification Checklist:**
- [ ] Fetch data, render a page, and review database logs. Only 1 query should be issued, regardless of how many nested components requested the identical data (Request Memoization verified).

---

### Prompt 10.3: Bundle Size Optimization

```text
You are a Build Optimization Engineer. Shrink the browser's JavaScript payload to the absolute minimum.

Constraints:
- Huge monolithic libraries (like `lodash` or `moment`) must be aggressively blocked and replaced with modular alternatives.
- Ensure dynamic heavy client components (like Charts or WYSIWYG editors) do not ship in the initial payload.

Required Output Format:
1. Explain how to configure `@next/bundle-analyzer` in `next.config.ts`.
2. Provide a practical code example utilizing `next/dynamic` to lazy-load a massive graphing library.
3. Provide a list of "Library Replacement" rules (e.g., `moment` -> `date-fns`, `lodash` -> `es-toolkit`).
4. Detail Server Component usage strictly as a bundle-reduction technique (keeping formatting logic out of the browser).

⚠️ Common Pitfalls:
- **Pitfall:** Importing huge libraries (like standard `aws-sdk`) into a file, relying on Tree-Shaking, but the library isn't fully tree-shakeable.
- **Solution:** Audit the final build with the Bundle Analyzer to physically confirm chunks are removed.
```

✅ **Verification Checklist:**
- [ ] Run `ANALYZE=true npm run build`. Review the HTML report.
- [ ] Ensure First Load JS is < 100KB per route. No single chunk should drastically exceed this for standard pages.

---
📎 **Related Phases:**
- Prerequisites: [Phase 9: Accessibility & i18n](./PHASE_9_ACCESSIBILITY__INTERNATIONALIZATION_UIUX_Designer_Frontend_Developer.md)
- Proceeds to: [Phase 11: DevOps & Infrastructure](./PHASE_11_DEVOPS__INFRASTRUCTURE_DevOps_Engineer.md)
