<a name="phase-17"></a>
# 📌 PHASE 17: MOBILE & PWA (Frontend Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 17.1: Progressive Web App (PWA) Setup

```text
You are a Mobile Web Expert. Transform the Next.js target architecture into an installable PWA.

Library: **Serwist** (Actively maintained successor to next-pwa)

Constraints:
- `next.config.ts` must apply `withSerwist` as the outermost wrapper to prevent Webpack config overwrites.
- The default cache strategy for APIs must be `NetworkFirst`, while static assets should be `CacheFirst`.

Required Output Format: Provide complete code for:
1. `next.config.ts`: Integration with `withSerwist`.
2. `app/sw.ts`: Establishing the Service Worker.
3. `public/manifest.webmanifest`: App Identity payload including Shortcuts.
4. Custom Install Prompt logic (wrapping A2HS) to guide users natively.

⚠️ Common Pitfalls:
- **Pitfall:** Missing Apple-specific meta tags (`apple-mobile-web-app-capable`). iOS Safari will not treat the site as a PWA without them.
- **Solution:** Add appleWebApp properties explicitly inside the Next.js `metadata` object in `app/layout.tsx`.
```

✅ **Verification Checklist:**
- [ ] Open the app in Chrome DevTools -> Application -> Service Workers. Verify it registered successfully.
- [ ] Check the "Offline" checkbox in DevTools, reload the page. It MUST route to a graceful fallback offline page.

---

### Prompt 17.2: Background Sync (Offline Patterns)

```text
You are an Offline-First Architect. Implement Background Sync so users can submit forms without network connectivity.

Constraints:
- You must leverage `SyncManager` API if supported by the browser.
- Fallback to `localStorage` request queues for unsupported environments (like iOS).

Required Output Format: Provide complete code for:
1. A queueing utility capturing failed POST requests locally.
2. A Service Worker `sync` event handler popping elements off the queue and retrying the fetch once connectivity returns.
```

✅ **Verification Checklist:**
- [ ] Disconnect internet, submit a form. The UI should acknowledge the submission as "Saved Offline".
- [ ] Reconnect internet. The Service Worker should silently flush the queue to the database.

---

### Prompt 17.3: Native Wrapper (Capacitor)

```text
You are a Native App Bundler. Convert the application to native iOS/Android binaries using Capacitor.

Constraints:
- Next.js must be configured for complete static export (`output: 'export'`) locally for the build.
- Native APIs (Camera, Haptics) must gracefully fallback if invoked from standard desktop browsers.

Required Output Format:
1. Bash commands to initialize Capacitor.
2. `capacitor.config.ts` setup pointing to Next.js's `out` directory.
3. A wrapper utility demonstrating how to gracefully check `Capacitor.isNativePlatform()` before calling APIs.
```

✅ **Verification Checklist:**
- [ ] Run `npx cap sync ios`. Open XCode and run the simulator. Ensure there are no white-screen CORS errors on boot.

---
📎 **Related Phases:**
- Prerequisites: [Phase 5: Frontend Development](./PHASE_5_FRONTEND_DEVELOPMENT_Frontend_Developer.md)
- Proceeds to: N/A
