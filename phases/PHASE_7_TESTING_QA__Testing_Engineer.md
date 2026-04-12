<a name="phase-7"></a>
# 📌 PHASE 7: TESTING & QA (QA Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 7.1: Unit & Integration Testing (Vitest)

```text
You are a QA Automation Lead. Set up blazingly fast unit testing for a Next.js application.

Tool: **Vitest** (Faster, uses Vite, native TS support)
Library: **React Testing Library**
Mocking: **MSW (Mock Service Worker)** for API mocking

Constraints:
- Do NOT test implementation details (like state variable names); test behaviors (like what renders on button click).
- Isolate component tests from the database. Use MSW to intercept API calls or mock Server Actions directly.
- Maintain at least an 80% coverage on core business logic functions.

Required Output Format: Provide complete code for:
1. `vitest.config.ts`: Vitest configuration excluding E2E folders and setting up `jsdom` or `happy-dom`.
2. `tests/setup.ts`: Environment setup, global mocks for `next/navigation` (e.g., `useRouter`).
3. `tests/mocks/server.ts`: MSW setup.
4. Two comprehensive tests:
   - A Server Action test mocking the ORM.
   - A Client Component test asserting UI changes after a button click.

⚠️ Common Pitfalls:
- **Pitfall:** Forgetting that Next.js Server Components cannot be unit-tested seamlessly with standard React Testing Library without extensive mocking of `next/headers` and `next/cache`.
- **Solution:** Test Server Components via Playwright (E2E), and restrict Vitest primarily to Client Components, hooks, and plain TS utility/service functions.
```

✅ **Verification Checklist:**
- [ ] Run `npm run test` and verify tests pass.
- [ ] Remove a required mocked router import in `setup.ts` and verify it fails, confirming the mock is active.

---

### Prompt 7.2: End-to-End Testing (Playwright)

```text
You are an E2E Test Architect. Set up Playwright to test critical user flows as they appear in the real browser.

Tool: **Playwright** (Modern, fast, cross-browser, parallel execution)

Constraints:
- Tests must be independent. Test A must not rely on the state of Test B.
- Abstract the login flow to a global setup block. Do not log in via the UI before every single test; inject the auth cookie.
- Use `data-testid` attributes or semantic ARIA accessible roles for locators, never flaky CSS class selectors.

Required Output Format: Provide complete code for:
1. `playwright.config.ts`: Configuration enabling traces, videos on failures, and localhost webserver booting.
2. `e2e/auth.setup.ts`: Global setup to authenticate and save state to `.auth/user.json`.
3. Test Database Setup instructions (e.g., Docker Compose `test-db`).
4. Two critical specs:
   - A smoke test verifying the homepage and successful login redirect.
   - A visual regression test comparing screenshots of a complex dashboard.

⚠️ Common Pitfalls:
- **Pitfall:** E2E tests failing in Next.js because they run against the development server (`next dev`) which compiles pages on-demand, causing timeouts.
- **Solution:** Configure Playwright's `webServer` command to run against the production build: `npm run build && npm run start`.
```

✅ **Verification Checklist:**
- [ ] Run `npx playwright test --ui` and visually watch the tests run in the UI runner.
- [ ] Change a CSS color in your code and run the visual regression test; verify it detects the pixel mismatch.

---

### Prompt 7.3: API Contract & Performance Testing

```text
You are a Test Reliability Engineer. Implement API contract tests and load testing.

Tool: **k6** (Backend load testing)
Tool: **Lighthouse CI** (Frontend performance auditing)

Constraints:
- Do not run load tests against production databases; supply dedicated test staging environments.
- API tests must ensure the shape of JSON responses doesn't randomly change, breaking mobile or generic clients.

Required Output Format: Provide complete code and configs for:
1. `lighthouserc.json`: Setting performance budgets (e.g., minimum score 0.90).
2. `k6/load-test.js`: A script ramping up VUs (Virtual Users) to stress test a public API route.
3. Test Data Factory pattern (e.g., using `faker.js` to dynamically generate test entities like users/posts).
```

✅ **Verification Checklist:**
- [ ] Run `k6 run load-test.js` against a local server and observe the 95th percentile (P95) response times.

---
📎 **Related Phases:**
- Prerequisites: [Phase 6: Advanced Features](./PHASE_6_ADVANCED_FEATURES_Full-Stack_Developer.md)
- Proceeds to: [Phase 8: Security & Automation](./PHASE_8_SECURITY_AUTOMATION_DevSecOps.md)
