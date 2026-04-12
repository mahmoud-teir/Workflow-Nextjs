<a name="phase-4"></a>
# 📌 PHASE 4: AUTHENTICATION & AUTHORIZATION (Security Expert)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 4.1: Implement Authentication System

```text
You are a rigorous Application Security Expert. Implement a battle-tested authentication system for a Next.js application.

Choose ONE based on the Decision Guide, then write the implementation:
- **Option A: Better Auth** (recommended — modern, type-safe, full-featured)
- **Option B: Auth.js v5** (NextAuth.js successor — widely adopted)
- **Option C: Clerk** (managed service — fastest to implement)

Decision Guide:
- Use **Better Auth** if you want full data ownership, framework agnosticism, and cutting-edge features (Passkeys, 2FA) without lock-in.
- Use **Clerk** if you have zero time for auth architecture and are okay with pricing scales and third-party data residence.
- Use **Auth.js v5** if maintaining a legacy NextAuth system or you rely on its massive provider ecosystem.

Constraints:
- You must implement session invalidation logic.
- Passwords (if used) MUST be hashed with Argon2 or Bcrypt.
- Provide secure token generation for password resets.
- Auth endpoints must have hard rate-limits (Upstash or in-memory fallback).
- Provide explicit hooks into the UI components (e.g., session status).

Required Output Format: Provide complete code for:
1. Core Library Config (`lib/auth.ts`)
2. Client Provider/Hooks (`lib/auth-client.ts`)
3. Auth Route Handlers (`app/api/auth/[...alias]/route.ts`)
4. Middleware session check for rote protection.
5. Example Login form with Passkey UX fallback guidance.

⚠️ Common Pitfalls:
- **Pitfall:** `middleware.ts` intercepting all traffic including `_next/static` assets, drastically slowing down site rendering.
- **Solution:** Ensure the middleware `matcher` regex strictly excludes static files, fonts, and images.
- **Pitfall (Better Auth + Drizzle):** `[# Drizzle Adapter]: The model "user" was not found in the schema object. Please pass the schema directly to the adapter options.`
- **Solution:** When using Better Auth with Drizzle ORM, you **MUST** explicitly pass your Drizzle schema tables to the adapter. Do not rely on auto-discovery. Configure it like this:
  ```ts
  import { betterAuth } from "better-auth";
  import { drizzleAdapter } from "better-auth/adapters/drizzle";
  import * as schema from "@/db/schema";

  export const auth = betterAuth({
    database: drizzleAdapter(db, {
      schema: {
        user: schema.user,
        session: schema.session,
        account: schema.account,
        verification: schema.verification,
      },
    }),
  });
  ```
  The `schema` object keys (`user`, `session`, etc.) are the internal model names Better Auth expects. The values are your actual Drizzle table exports.
- **Pitfall (Better Auth + Drizzle):** `The field "createdAt" does not exist in the "account" Drizzle schema.`
- **Solution:** Better Auth requires specific columns on all auth tables. Either **auto-generate** the correct schema or add the missing fields manually:

  **Recommended — Auto-generate:**
  ```bash
  npx auth@latest generate
  ```

  **Manual — Add missing fields to your Drizzle schema:**
  ```ts
  // db/schema.ts — account table
  export const account = pgTable("account", {
    // ... existing fields ...
    createdAt: timestamp("created_at").defaultNow(),  // ← Required by Better Auth
    updatedAt: timestamp("updated_at").defaultNow(),  // ← Required by Better Auth
  });
  ```

  Similarly, ensure `user`, `session`, and `verification` tables also include `createdAt` / `updatedAt` if Better Auth reports missing fields on those models.
```

✅ **Verification Checklist:**
- [ ] If using Better Auth + Drizzle: run `npx auth@latest generate` to scaffold the correct schema, OR verify all auth tables (`user`, `session`, `account`, `verification`) include `createdAt` and `updatedAt` fields.
- [ ] If using Better Auth + Drizzle: the `drizzleAdapter` config includes an explicit `schema` object mapping all required models (`user`, `session`, `account`, `verification`).
- [ ] Create an account.
- [ ] Verify database contains hashed password, not plaintext.
- [ ] Log out / invalidate session; verify token cookie is deleted.

---

### Prompt 4.2: Implement Authorization System (RBAC / ABAC)

```text
You are an Identity and Access Management (IAM) Specialist. Implement granular access control.

Constraints:
- Guard clauses must exist at the Server Action / Route level, *not merely hidden in the UI*.
- Do NOT use raw HTTP exceptions for unprivileged actions in Server Actions; return typed authorization errors.
- Ensure admin panels enforce a double-check of session validity.

Required Output Format: Provide complete code for:
1. `lib/auth/permissions.ts`: Mapped permission constants (e.g., `posts:delete:any`).
2. `lib/auth/authorize.ts`: A type-safe server function returning discriminated unions (`{ authorized: true } | { error: 'FORBIDDEN' }`).
3. `components/auth/permission-gate.tsx`: A client-side wrapper that visually hides components if the user lacks roles.
4. If scaling: Attribute-Based Access Control (ABAC) patterns (e.g., checking if `post.authorId === user.id`).

⚠️ Common Pitfalls:
- **Pitfall:** Hiding an "Edit" button in the frontend layout using RBAC, but failing to check RBAC in the backend Server Action, allowing API-level abuse.
- **Solution:** Enforce Zero-Trust. Always validate permissions immediately inside the backend mutating function.
```

✅ **Verification Checklist:**
- [ ] Access an admin route directly via URL as a standard user; verify a 403 or redirect occurs.
- [ ] Submit a Server Action payload manually (via cURL) designed for admins while logged in as a normal user; verify the server rejects it.

---

### Prompt 4.3: Security Best Practices & Headers

```text
You are an OWASP Top-10 Compliance Officer. Fortify the application against common web vulnerabilities.

Constraints:
- Implement a strict Content Security Policy (CSP) blocking unauthorized inline scripts.
- Ensure all forms use CSRF protection (Next.js server actions handle this natively, but verify setup).
- Do not let sensitive server configurations leak into the client bundle.

Required Output Format: Provide implementation for:
1. CSP Nonce generation in Middleware and propagation to `app/layout.tsx`.
2. HSTS, X-Frame-Options, X-Content-Type-Options headers standardizations.
3. `server-only` architecture examples to prevent client leaks.
4. Security Header checklist for pre-launch.

⚠️ Common Pitfalls:
- **Pitfall:** CSP blocking legitimate inline scripts needed by Next.js or third-party analytics.
- **Solution:** Use script nonces and strictly manage `unsafe-inline` usage. Test CSP in "Report-Only" mode first.
```

✅ **Verification Checklist:**
- [ ] Use browser DevTools -> Network -> Headers to confirm CSP, HSTS, and X-Content-Type-Options are attached to document responses.
- [ ] Attempt to import a `server-only` marked database file into a client component. The build MUST fail.

---
📎 **Related Phases:**
- Prerequisites: [Phase 3: Database Models](./PHASE_3_DATABASE_MODELS__INTEGRATION_Database_Architect.md)
- Proceeds to: [Phase 5: Frontend Development](./PHASE_5_FRONTEND_DEVELOPMENT_Frontend_Developer.md)
