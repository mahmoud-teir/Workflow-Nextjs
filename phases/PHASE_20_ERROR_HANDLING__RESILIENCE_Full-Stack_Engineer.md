<a name="phase-20"></a>
# 📌 PHASE 20: ERROR HANDLING & RESILIENCE (Full-Stack Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 20.1: Unified Error Taxonomy & Boundaries

```text
You are a Resilience Engineer. Institutionalize a unified error taxonomy across the Next.js stack.

Constraints:
- You MUST differentiate between "Operational Errors" (e.g., Validation failed, expected) and "Programming Errors" (e.g., Unhandled Null Exception, unexpected).
- Sentry (or your observability tool) should ONLY alert on Programming Errors, filtering out 400s and 404s to reduce alert fatigue.

Required Output Format: Provide complete code for:
1. `lib/errors.ts`: Base `AppError` class, with specific extensions (`NotFoundError`, `ValidationError`).
2. `app/error.tsx`: The Next.js Error Boundary capturing uncaught component errors.
3. `app/global-error.tsx`: The root Next.js error boundary capturing layout errors (Must include `<html>` tags).
```

✅ **Verification Checklist:**
- [ ] Throw a deliberate validation error. Verify it does NOT trigger an urgent PagerDuty/Sentry alert.
- [ ] Throw a deliberate runtime exception (`obj.methodDoesNotExist()`). Verify it DOES trigger the alert but the UI gracefully shows the `error.tsx` boundary fallback.

---

### Prompt 20.2: Retry & Circuit Breaker Patterns

```text
You are a Backend Reliability Specialist. Harden the application against downstream third-party service failures.

Constraints:
- External network requests (e.g., fetching weather from an external API) must utilize Exponential Backoff.
- Use a Circuit Breaker pattern to fail fast when an external dependency is confirmed down, preventing thread pool exhaustion in Node.js.

Required Output Format: Provide:
1. `lib/retry.ts`: A utility applying exponential backoff to a promise.
2. `lib/circuit-breaker.ts`: A minimal state machine (Open, Closed, Half-Open) wrapping external fetches.

⚠️ Common Pitfalls:
- **Pitfall:** Allowing an external API timeout to block a Server Action, eventually tying up all available Next.js server threads until the application crashes.
- **Solution:** Always apply a strict `.race()` timeout to internal fetch calls utilizing external APIs.
```

✅ **Verification Checklist:**
- [ ] Point an API fetch to a blackhole endpoint. Ensure the timeout correctly aborts the request after 5 seconds instead of hanging infinitely.

---

### Prompt 20.3: Graceful Degradation (Offline Fallbacks)

```text
You are a Frontend Experience Engineer. Handle intermittent client offline states without showing ugly browser "No Internet" dinosaurs.

Constraints:
- Next.js 15 Server Components CANNOT detect client connection status; this must be a Client hook.
- Implement a logical Feature Degradation Matrix (e.g., 'If offline: "Delete Profile" is blocked, but "Read Articles" shows stale cache').

Required Output Format: Provide complete code for:
1. `hooks/use-online-status.ts`: Subscribing to browser network events via `useSyncExternalStore`.
2. `<FeatureGate>` wrapper component that dynamically disables its children or overlays a warning when the user goes offline.
3. Utilizing `unstable_cache` effectively to serve stale data if the fresh database query fails dynamically.
```

✅ **Verification Checklist:**
- [ ] Turn off WiFi. Verify a non-critical component (like a "Submit Review" button) disables itself and says "Unavailable Offline", while the rest of the layout remains intact.

---
📎 **Related Phases:**
- Prerequisites: [Phase 12: Observability & Monitoring](./PHASE_12_OBSERVABILITY__MONITORING_DevOps_SRE.md)
- Proceeds to: Finished.
