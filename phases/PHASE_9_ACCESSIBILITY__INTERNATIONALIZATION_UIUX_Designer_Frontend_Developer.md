<a name="phase-9"></a>
# 📌 PHASE 9: ACCESSIBILITY & INTERNATIONALIZATION (UI/UX, Frontend)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 9.1: Internationalization (next-intl)

```text
You are a Frontend Architect. Implement robust Internationalization (i18n) for a Next.js application leveraging Server Components.

Library: **next-intl** (Best support for Next.js App Router).

Constraints:
- Avoid sending massive localization dictionaries to the client. Only load the specific JSON strings necessary for the current page components.
- Rely on App Router middleware for locale-detection and redirects (e.g., `en-US` to `/en`).
- Provide an SEO-friendly URL structure (e.g., `[locale]/...`).

Decision Guide:
- Use **next-intl** for standard server-component heavy apps.
- Use **i18next** if sharing identical translation logic with a React Native app.

Required Output Format: Provide complete code for:
1. Configuration (`i18n.ts` / `request.ts`).
2. The Middleware update (combining locale detection with existing security logic).
3. The root `[locale]/layout.tsx` distributing the language provider.
4. A Language Switcher Client Component that modifies the URL without causing a full HTML refresh.
5. Example usage of the `useTranslations` hook in both a Server Component and a Client Component.

⚠️ Common Pitfalls:
- **Pitfall:** `createMiddleware` from `next-intl` replacing the existing security headers middleware created in Phase 8.
- **Solution:** Chain them together: evaluate the locale response first, then append CSP and HSTS headers to it before returning.
```

✅ **Verification Checklist:**
- [ ] Change browser language preferences to a secondary language, open localhost at `/`, verify it automatically redirects to `/es` (or equivalent).
- [ ] Inspect network tab on a client-side navigation; verify only minimal translation JSONs are fetched, not the whole app dictionary.

---

### Prompt 9.2: Accessibility (A11y) Implementation

```text
You are an Accessibility Specialist (CPACC). Harden the UI to comply with WCAG 2.2 AA standards.

Constraints:
- Focus management must be explicit (e.g., Modal dialogs must trap focus; skip links must exist).
- Do not solely rely on color to convey meaning (e.g., an error state must have an icon or text, not just red borders).
- Respect the OS `prefers-reduced-motion` state to stop all auto-playing animations.

Required Output Format: Provide complete code for:
1. A `<SkipToContent />` invisible link that becomes visible on keyboard focus.
2. An ARIA Live Region component (`<LiveAnnouncer />`) to audibly declare Client-side route changes or form success states to screen readers.
3. A Playwright test configuration specifically executing `@axe-core/playwright` to block the CI pipeline if severe a11y violations are merged.

⚠️ Common Pitfalls:
- **Pitfall:** Radix/Shadcn handles basic accessibility, but dynamic page updates (like Server Actions successfully finishing) remain totally silent to a screen reader.
- **Solution:** Dispatch a custom event to your `<LiveAnnouncer />` instructing it to narrate "Profile Saved Successfully" via `aria-live="polite"`.
```

✅ **Verification Checklist:**
- [ ] Unplug your mouse. Navigate the entire core user flow (login, view item, submit form) using entirely the Tab, Arrow, Space, and Enter keys.
- [ ] Verify `axe-core` in CI passes critical checks (Color Contrast, ARIA valid).

---

### Prompt 9.3: RTL Support (Right-to-Left)

```text
You are a Localization Engineer. Prepare the Next.js app to support Right-to-Left (RTL) languages like Arabic seamlessly.

Constraints:
- Do not write manual CSS overrides like `margin-left` and `margin-right`.
- Rely entirely on Tailwind CSS Logical Properties (`marginStart`, `paddingEnd`).
- Icons indicating direction (like arrows) must auto-flip in RTL mode.

Required Output Format:
1. Explain how to set `dir="rtl"` dynamically in `app/[locale]/layout.tsx`.
2. Provide a Tailwind CSS snippet demonstrating logical properties (`ms-4`, `pe-2`).
3. Provide an Icon wrapper component that automatically flips `rotate-180` when `dir="rtl"`.
4. Provide Font-pairing configuration separating Latin fonts from Arabic/Hebrew fonts to prevent layout breaking.
```

✅ **Verification Checklist:**
- [ ] Load the Arabic/Hebrew locale.
- [ ] The entire layout must flip (left sidebar moves to the right).
- [ ] "Back" arrows now point to the right.

---
📎 **Related Phases:**
- Prerequisites: [Phase 8: Security Automation](./PHASE_8_SECURITY_AUTOMATION_DevSecOps.md)
- Proceeds to: [Phase 10: Performance Optimization](./PHASE_10_PERFORMANCE_OPTIMIZATION_Frontend_Backend_DevOps.md)
