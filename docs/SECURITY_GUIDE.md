# Security Guide

> Comprehensive security reference for Next.js projects using the ECC agent harness.

## Overview

This guide consolidates all security patterns, rules, and automated checks from the ECC framework. It is the canonical security reference for projects built with this workflow library.

**Related ECC Components:**
- Skill: [`security-review`](../skills/security-review/SKILL.md)
- Agent: [`security-reviewer`](../agents/security-reviewer.md)
- Hook: [`pre-edit-security-scan`](../hooks/pre-edit-security-scan.json)
- Rules: [`rules/common/security.md`](../rules/common/security.md) · [`rules/typescript/security.md`](../rules/typescript/security.md)
- Phase: [Phase 8: Security & Automation](../phases/PHASE_8_SECURITY_AUTOMATION_DevSecOps.md)

---

## 1. Server Action Security

Server Actions are **public HTTP endpoints**. They must be secured individually — never rely on middleware alone.

### Auth-First Pattern (MANDATORY)

```typescript
'use server';

import { auth } from '@/lib/auth';
import { redirect } from 'next/navigation';
import { z } from 'zod';

const UpdateProfileSchema = z.object({
  name: z.string().min(1).max(100),
  bio: z.string().max(500).optional(),
});

export async function updateProfile(formData: FormData) {
  // 1. AUTHENTICATE — Always first
  const session = await auth();
  if (!session?.user) {
    redirect('/login');
  }

  // 2. VALIDATE — Parse and validate all inputs
  const result = UpdateProfileSchema.safeParse({
    name: formData.get('name'),
    bio: formData.get('bio'),
  });

  if (!result.success) {
    return { error: result.error.flatten().fieldErrors };
  }

  // 3. AUTHORIZE — Check ownership/permissions
  const profile = await db.profile.findUnique({
    where: { userId: session.user.id },
  });

  if (!profile) {
    return { error: { general: 'Profile not found' } };
  }

  // 4. EXECUTE — Perform the mutation
  await db.profile.update({
    where: { userId: session.user.id },
    data: result.data,
  });

  // 5. REVALIDATE — Clear relevant caches
  revalidatePath('/profile');
  return { success: true };
}
```

### Key Rules

- ✅ Every Server Action calls `auth()` or `getSession()` before any database operation
- ✅ All inputs validated with Zod schemas (`.safeParse()`, not `.parse()`)
- ✅ Authorization checks (ownership, roles) after authentication
- ❌ Never trust `formData` values without validation
- ❌ Never expose internal error details to the client

---

## 2. Environment Variables

### `NEXT_PUBLIC_` Audit

Only variables that **MUST** be visible in the browser should use the `NEXT_PUBLIC_` prefix.

```bash
# ✅ Safe for NEXT_PUBLIC_
NEXT_PUBLIC_APP_URL=https://myapp.com
NEXT_PUBLIC_POSTHOG_KEY=phc_abc123
NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_live_abc123

# ❌ NEVER use NEXT_PUBLIC_ for these
DATABASE_URL=postgresql://...
STRIPE_SECRET_KEY=sk_live_abc123
AUTH_SECRET=super-secret-auth-key
OPENAI_API_KEY=sk-abc123
```

### Build-Time Validation

```typescript
// env.ts — Validate at build time
import { z } from 'zod';

const envSchema = z.object({
  DATABASE_URL: z.string().url(),
  AUTH_SECRET: z.string().min(32),
  STRIPE_SECRET_KEY: z.string().startsWith('sk_'),
  NEXT_PUBLIC_APP_URL: z.string().url(),
});

export const env = envSchema.parse(process.env);
```

---

## 3. Content Security Policy (CSP)

### Middleware Implementation

```typescript
// middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const nonce = Buffer.from(crypto.randomUUID()).toString('base64');
  
  const csp = [
    `default-src 'self'`,
    `script-src 'self' 'nonce-${nonce}' 'strict-dynamic'`,
    `style-src 'self' 'unsafe-inline'`,
    `img-src 'self' blob: data: https:`,
    `font-src 'self'`,
    `connect-src 'self' https://*.vercel.app`,
    `frame-ancestors 'none'`,
    `base-uri 'self'`,
    `form-action 'self'`,
  ].join('; ');

  const response = NextResponse.next();
  response.headers.set('Content-Security-Policy', csp);
  response.headers.set('X-Content-Type-Options', 'nosniff');
  response.headers.set('X-Frame-Options', 'DENY');
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');
  response.headers.set('X-Nonce', nonce);

  return response;
}

export const config = {
  matcher: [
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};
```

---

## 4. Rate Limiting

### Upstash Rate Limiting Pattern

```typescript
import { Ratelimit } from '@upstash/ratelimit';
import { Redis } from '@upstash/redis';

const ratelimit = new Ratelimit({
  redis: Redis.fromEnv(),
  limiter: Ratelimit.slidingWindow(10, '60 s'), // 10 requests per 60 seconds
  analytics: true,
});

// In Server Action or API Route:
const identifier = session.user.id || ip;
const { success, limit, remaining } = await ratelimit.limit(identifier);

if (!success) {
  return { error: 'Too many requests. Please try again later.' };
}
```

### Recommended Limits

| Endpoint | Window | Max Requests |
|----------|--------|-------------|
| Login | 60s | 5 |
| Registration | 3600s | 3 |
| Password Reset | 3600s | 3 |
| API Mutations | 60s | 30 |
| File Upload | 60s | 5 |

---

## 5. Security Headers Checklist

| Header | Value | Purpose |
|--------|-------|---------|
| `Content-Security-Policy` | (see above) | Prevent XSS and injection |
| `X-Content-Type-Options` | `nosniff` | Prevent MIME sniffing |
| `X-Frame-Options` | `DENY` | Prevent clickjacking |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | Control referrer info |
| `Strict-Transport-Security` | `max-age=63072000; includeSubDomains` | Force HTTPS |
| `Permissions-Policy` | `camera=(), microphone=()` | Restrict browser APIs |
| `X-Powered-By` | (removed) | `poweredByHeader: false` in next.config |

---

## 6. Automated Security Scanning

### ECC Pre-Edit Hook

The `pre-edit-security-scan` hook automatically blocks writes containing:
- Hardcoded secrets matching known patterns (API keys, tokens, passwords)
- `eval()` or `new Function()` calls
- `dangerouslySetInnerHTML` usage
- Direct SQL queries (must use ORM/prepared statements)

### CI/CD Security Pipeline

```yaml
# .github/workflows/security.yml
name: Security Scan
on: [push, pull_request]
jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Dependency Audit
        run: npm audit --audit-level=high
      - name: Secret Scan
        uses: trufflesecurity/trufflehog@main
        with:
          path: ./
      - name: CodeQL Analysis
        uses: github/codeql-action/analyze@v3
```

---

## 7. Agent-Driven Security Audit

To run a comprehensive security audit, invoke the security-reviewer agent:

```
/security-reviewer

The agent will:
1. Scan all Server Actions for auth-first pattern compliance
2. Audit NEXT_PUBLIC_ variables for secret exposure
3. Verify Zod validation on all inputs
4. Check CSP and security headers
5. Scan for rate limiting on mutation endpoints
6. Produce a P0/P1/P2 prioritized findings report
```

---

## 8. OWASP Top 10 — Next.js Mapping

| OWASP Risk | Next.js Mitigation | ECC Check |
|------------|-------------------|-----------|
| A01: Broken Access Control | Server Action auth, middleware | Security Reviewer Agent |
| A02: Cryptographic Failures | `AUTH_SECRET` in env, HTTPS | Pre-edit Hook |
| A03: Injection | Zod validation, ORM (no raw SQL) | Security Reviewer Agent |
| A04: Insecure Design | Threat modeling in Phase 0 | Planner Agent |
| A05: Security Misconfiguration | CSP, headers, `poweredByHeader: false` | Verification Loop |
| A06: Vulnerable Components | `npm audit`, Dependabot | CI/CD Pipeline |
| A07: Auth Failures | Better Auth / NextAuth, rate limiting | Security Reviewer Agent |
| A08: Data Integrity | SRI, CSP `strict-dynamic` | Middleware |
| A09: Logging Failures | `instrumentation.ts`, Sentry | Deployment Skill |
| A10: SSRF | URL validation, allowlisted domains | Code Review Agent |
