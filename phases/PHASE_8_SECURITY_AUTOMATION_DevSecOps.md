<a name="phase-8"></a>
# 📌 PHASE 8: SECURITY & AUTOMATION (DevSecOps)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 8.1: CI/CD Security Scanning

```text
You are a DevSecOps Engineer. Implement automated security scanning in the CI/CD pipeline using GitHub Actions.

Tools:
- **Dependency Scanning**: npm audit, Dependabot, Renovate, Socket.dev
- **SAST (Static Application Security Testing)**: CodeQL
- **Secret Scanning**: TruffleHog

Constraints:
- Security workflows must block PR merges if critical vulnerabilities are found.
- Differentiate between devDependency vulnerabilities (warn) and production dependency vulnerabilities (block).
- Ensure secret scanning runs before codebase pushes (via pre-commit hooks).

Required Output Format: Provide complete code for:
1. `.github/workflows/security.yml`: Action workflow scheduling CodeQL, npm audit, and TruffleHog.
2. `.husky/pre-commit`: Local pre-commit hook to block developers from accidentally committing AWS keys or database secrets.
3. Guidelines on handling false positives in CodeQL.
```

✅ **Verification Checklist:**
- [ ] Push a dummy `.env` file with a fake Stripe secret to an isolated branch; ensure the pre-commit hook catches it and aborts the commit.
- [ ] Review GitHub Actions Security tab; verify CodeQL ran successfully across the Next.js directories.

---

### Prompt 8.2: Content Security Policy & Security Headers

```text
You are an Application Security Engineer. Add defense-in-depth via HTTP headers.

Constraints:
- This logic MUST live in the Next.js `middleware.ts`.
- Content-Security-Policy (CSP) must utilize nonces to explicitly allow specific inline scripts (like Next.js's native hydration scripts).
- Keep `frame-ancestors` restricted to prevent Clickjacking.

Required Output Format: Provide complete code for:
1. CSP Nonce Generation logic inside middleware.
2. The CSP header string compiling `script-src`, `connect-src`, `img-src` specifically tuned for a standard Next.js app (Vercel, Stripe, Analytics).
3. The Root Layout update: Injecting the nonce into the DOM for third-party `<Script>` components.
4. Explanations for HSTS, X-Content-Type-Options, and Referrer-Policy configurations.

⚠️ Common Pitfalls:
- **Pitfall:** Applying the CSP middleware to public assets recursively (`_next/image`), causing images/fonts to break and increasing middleware latency.
- **Solution:** Use the `matcher` in middleware to meticulously exclude `/api`, `_next/static`, and `favicon.ico`.
```

✅ **Verification Checklist:**
- [ ] View page source in the browser; verify `<script nonce="...">` exists and the nonce changes on every page refresh.
- [ ] Ensure external fonts (like Google Fonts) load successfully without being blocked by CSP errors in the console.

---

### Prompt 8.3: Penetration Testing Checklist

```text
You are an Offensive Security Consultant (Red Teamer). Provide a detailed Penetration Testing Checklist specifically tailored for modern Next.js/React applications.

Constraints:
- Focus heavily on Server Actions (as they represent hidden API endpoints).
- Cover Next.js specific hydration vulnerabilities.

Required Output Format: Create a checklist highlighting:
1. **Server Actions Abuse**: (e.g., testing if an attacker can manually craft a multipart form payload invoking an Admin action).
2. **Authentication/Session**: (Token fixation, bypass).
3. **Data Leaks**: (Checking `__NEXT_DATA__` or React 19 RSC payloads for accidentally serialized passwords or PII).
4. **CSRF / XSS**: (Verifying React's auto-escaping is not bypassed via `dangerouslySetInnerHTML`).

⚠️ Common Pitfalls:
- **Pitfall:** Assuming Server Actions are safe just because there is no UI linking to them.
- **Solution:** Remind the team that Server Actions generate public HTTP endpoints. They must be individually penetration tested like standard REST APIs.
```

✅ **Verification Checklist:**
- [ ] Inspect the network payload of a Server Component returning user data; ensure password hashes and internal IDs are strictly stripped.

---
📎 **Related Phases:**
- Prerequisites: [Phase 7: Testing & QA](./PHASE_7_TESTING_QA__Testing_Engineer.md)
- Proceeds to: [Phase 9: Accessibility & i18n](./PHASE_9_ACCESSIBILITY__INTERNATIONALIZATION_UIUX_Designer_Frontend_Developer.md)
