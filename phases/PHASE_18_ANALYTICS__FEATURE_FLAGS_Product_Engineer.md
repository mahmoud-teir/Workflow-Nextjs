<a name="phase-18"></a>
# 📌 PHASE 18: ANALYTICS & FEATURE FLAGS (Product Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 18.1: Analytics & Event Tracking

```text
You are a Product Engineer. Implement product analytics in a Next.js App Router application.

Target: **PostHog** (or Plausible / Umami)

**Step 1 — Install Dependencies:**
Run: `npm install posthog-js` (or your chosen SDK). Verify the package appears in your `package.json`.

Constraints:
- You MUST ensure the analytics SDK does not block initial hydration.
- The `PostHogProvider` must be structured so it correctly captures single-page app (SPA) navigations via `usePathname()`.
- Server-side tracking (e.g., inside Server Actions) must properly flush before the Node process exits.

Required Output Format: Provide complete code for:
1. `lib/posthog-server.ts`: The singleton node client for Server Actions.
2. `app/providers.tsx`: The Client-Side Analytics Provider + hook to trace path changes.
3. Demonstrated usage of tracking an event within a Server Action.

⚠️ Common Pitfalls:
- **Pitfall:** `usePostHog()` triggering events before the user has consented to cookies, violating GDPR.
- **Solution:** Configure PostHog to start in a dormant/opt-out state, and only call `opt_in_capturing()` explicitly upon UI consent.
```

✅ **Verification Checklist:**
- [ ] `posthog-js` (or alternative) is listed in `package.json` dependencies.
- [ ] Navigate between 3 internal pages using `next/link`. Check PostHog dashboard to verify it captured 3 distinct page views without full HTML reloads.

---

### Prompt 18.2: Feature Flags (Server-Side Evaluation)

```text
You are a Release Manager. Implement Feature Flags to safely test code in production.

Constraints:
- Do NOT evaluate feature flags on the client side if the flag controls large UI blocks (preventing layout shifts / flashing).
- Flag evaluation must occur in Server Components or Middleware.

Required Output Format: Provide complete code for:
1. Fetching a boolean flag from PostHog or Vercel Edge Config inside `app/page.tsx` (Server Component).
2. Example of swapping out `<NewDashboard />` vs `<OldDashboard />` based on the evaluated flag securely.
```

✅ **Verification Checklist:**
- [ ] Disable the feature flag remotely in the dashboard. Hard refresh the page. The fallback UI must render immediately from the server without flashing the new feature first.

---

### Prompt 18.3: Custom Event Taxonomy Design

```text
You are a Data Architect. Design a consistent event taxonomy to prevent analytics debt.

Constraints:
- Enforce strict `object_action` naming (e.g., `signup_completed`).
- Never allow PII (Emails, Passwords) inside the payload properties.
- Provide a Zod wrapper around your tracking calls to throw warnings in Dev mode if developers send malformed metrics.

Required Output Format: Provide complete code for:
1. A strongly typed `lib/analytics-schema.ts` dictating allowed event names and payload structures.
2. A table outlining 10 core SaaS metrics you plan to track cleanly.
```

✅ **Verification Checklist:**
- [ ] Trigger an event with an invalid property type (e.g., passing a string to a property expecting a number). Verify the Zod dev wrapper prints a console warning.

---
📎 **Related Phases:**
- Prerequisites: [Phase 14: Pre-Launch Checklist](./PHASE_14_PRE-LAUNCH_CHECKLIST_All_Roles.md)
- Proceeds to: [Phase 19: API Documentation](./PHASE_19_API_DOCUMENTATION__VERSIONING_Backend_Engineer.md)
