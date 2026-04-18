---
name: security
description: Mandatory security checklist for all development.
origin: ECC
---

# Security Rules

## Pre-Commit Security Checklist

Before EVERY commit, verify:

1. [ ] No hardcoded API keys, tokens, or passwords in source code
2. [ ] No secrets in `NEXT_PUBLIC_` environment variables
3. [ ] All user inputs validated with Zod schemas
4. [ ] All Server Actions perform their own auth checks
5. [ ] All database queries are parameterized (no string concatenation)
6. [ ] No sensitive data in error responses sent to clients
7. [ ] No passwords, tokens, or PII in log statements
8. [ ] All file uploads validated for type, size, and extension

## Secret Management

- Store secrets in `.env.local` (local dev)
- Use platform secrets for production (Vercel, AWS, etc.)
- Validate required env vars at build time via `env.ts`
- Never log secrets, even at debug level

## Security Response Protocol

If a secret is exposed:
1. **Rotate immediately** — Generate new credentials
2. **Revoke old credentials** — Disable the exposed key
3. **Audit access** — Check logs for unauthorized usage
4. **Update .gitignore** — Prevent recurrence
5. **Scrub history** — Use `git filter-branch` or BFG if committed
