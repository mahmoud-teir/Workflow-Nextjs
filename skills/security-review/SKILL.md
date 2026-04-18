---
name: security-review
description: Use this skill when adding authentication, handling user input, working with secrets, creating API endpoints, or implementing payment/sensitive features. Provides comprehensive security checklist and patterns.
origin: ECC
stack: Next.js 15/16+, Server Actions, Zod
---

# Security Review Skill

This skill ensures all code follows security best practices and identifies potential vulnerabilities before they reach production.

## When to Activate

- Implementing authentication or authorization
- Handling user input or file uploads
- Creating new API endpoints or Server Actions
- Working with secrets or credentials
- Implementing payment features (Stripe)
- Storing or transmitting sensitive data
- Integrating third-party APIs
- Before any production deployment

## Security Checklist

### 1. Secrets Management

#### NEVER Do This
```typescript
const apiKey = "sk-proj-xxxxx"     // Hardcoded secret
const dbPassword = "password123"   // In source code
const stripeKey = "sk_live_..."    // Stripe secret in codebase
```

#### ALWAYS Do This
```typescript
const apiKey = process.env.OPENAI_API_KEY
const dbUrl = process.env.DATABASE_URL

// Validate presence at startup via env.ts (Phase 1)
if (!apiKey) {
  throw new Error('OPENAI_API_KEY not configured')
}
```

#### Next.js-Specific: NEXT_PUBLIC_ Audit
```typescript
// DANGER: These are bundled into client-side JavaScript!
// Only pure-public values should use this prefix.
const publicKey = process.env.NEXT_PUBLIC_POSTHOG_KEY      // ✅ OK — public analytics key
const secretKey = process.env.NEXT_PUBLIC_STRIPE_SECRET     // ❌ CRITICAL — exposed to client!
const dbUrl = process.env.NEXT_PUBLIC_DATABASE_URL           // ❌ CRITICAL — exposed to client!
```

#### Verification Steps
- [ ] No hardcoded API keys, tokens, or passwords
- [ ] All secrets in environment variables
- [ ] `.env.local` and `.env.production.local` in `.gitignore`
- [ ] No secrets in Git history (`git log -p | grep -i "sk-\|password\|secret"`)
- [ ] No `NEXT_PUBLIC_` variables contain sensitive values
- [ ] Required env vars validated at build time via `env.ts`

### 2. Server Action Security

#### Every Server Action MUST:
```typescript
'use server'

import { z } from 'zod'
import { getSession } from '@/lib/auth'
import { db } from '@/lib/db'

const UpdateProfileSchema = z.object({
  name: z.string().min(1).max(100),
  bio: z.string().max(500).optional(),
})

export async function updateProfile(formData: FormData) {
  // 1. AUTHENTICATE — Never rely on middleware alone
  const session = await getSession()
  if (!session?.user) {
    return { success: false, error: 'Unauthorized' }
  }

  // 2. VALIDATE — All inputs via Zod
  const parsed = UpdateProfileSchema.safeParse({
    name: formData.get('name'),
    bio: formData.get('bio'),
  })
  if (!parsed.success) {
    return { success: false, errors: parsed.error.flatten().fieldErrors }
  }

  // 3. AUTHORIZE — Verify ownership/permissions
  const profile = await db.profiles.findUnique({
    where: { userId: session.user.id },
  })
  if (!profile) {
    return { success: false, error: 'Forbidden' }
  }

  // 4. EXECUTE — Use Data Access Layer, never raw SQL in actions
  try {
    await db.profiles.update({
      where: { userId: session.user.id },
      data: parsed.data,
    })
    return { success: true }
  } catch (error) {
    console.error('Profile update failed:', error)
    return { success: false, error: 'An error occurred' }
  }
}
```

#### Verification Steps
- [ ] Every Server Action performs its own auth check
- [ ] All inputs validated with Zod schemas
- [ ] Data Access Layer (DAL) centralizes database queries
- [ ] Rate limiting on mutation-heavy actions (login, registration, payments)
- [ ] No raw SQL or direct ORM calls — all through service/repository layer

### 3. Input Validation

#### Always Validate with Zod
```typescript
import { z } from 'zod'

const CreateUserSchema = z.object({
  email: z.string().email(),
  name: z.string().min(1).max(100).trim(),
  age: z.number().int().min(0).max(150),
})

type CreateUserInput = z.infer<typeof CreateUserSchema>
```

#### File Upload Validation
```typescript
function validateFileUpload(file: File) {
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) throw new Error('File too large (max 5MB)')

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/avif']
  if (!allowedTypes.includes(file.type)) throw new Error('Invalid file type')

  const ext = file.name.toLowerCase().match(/\.[^.]+$/)?.[0]
  const allowedExts = ['.jpg', '.jpeg', '.png', '.webp', '.avif']
  if (!ext || !allowedExts.includes(ext)) throw new Error('Invalid extension')

  return true
}
```

### 4. SQL Injection Prevention

#### NEVER Concatenate SQL
```typescript
// ❌ DANGEROUS — SQL Injection
const query = `SELECT * FROM users WHERE email = '${userEmail}'`
```

#### ALWAYS Use Parameterized Queries
```typescript
// ✅ Safe — parameterized
const user = await db.users.findUnique({ where: { email: userEmail } })

// ✅ Safe — raw SQL with parameters
await db.$queryRaw`SELECT * FROM users WHERE email = ${userEmail}`
```

### 5. Authentication & Authorization

#### Token Storage
```typescript
// ❌ WRONG: localStorage (XSS vulnerable)
localStorage.setItem('token', token)

// ✅ CORRECT: httpOnly cookies
cookies().set('session', token, {
  httpOnly: true,
  secure: true,
  sameSite: 'strict',
  maxAge: 60 * 60 * 24 * 7, // 7 days
})
```

### 6. XSS Prevention

#### Content Security Policy (middleware.ts)
```typescript
import { NextResponse } from 'next/server'

export function middleware(request: Request) {
  const nonce = Buffer.from(crypto.randomUUID()).toString('base64')
  const csp = [
    `default-src 'self'`,
    `script-src 'self' 'nonce-${nonce}'`,
    `style-src 'self' 'unsafe-inline'`,
    `img-src 'self' data: https:`,
    `font-src 'self'`,
    `connect-src 'self' https://*.supabase.co`,
  ].join('; ')

  const response = NextResponse.next()
  response.headers.set('Content-Security-Policy', csp)
  response.headers.set('X-Content-Type-Options', 'nosniff')
  response.headers.set('X-Frame-Options', 'DENY')
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
  return response
}
```

### 7. Rate Limiting

```typescript
import { Ratelimit } from '@upstash/ratelimit'
import { Redis } from '@upstash/redis'

const ratelimit = new Ratelimit({
  redis: Redis.fromEnv(),
  limiter: Ratelimit.slidingWindow(10, '60 s'), // 10 requests per minute
})

export async function POST(request: Request) {
  const ip = request.headers.get('x-forwarded-for') ?? '127.0.0.1'
  const { success } = await ratelimit.limit(ip)

  if (!success) {
    return Response.json({ error: 'Too many requests' }, { status: 429 })
  }
  // Process request...
}
```

### 8. Security Headers (next.config.ts)

```typescript
const securityHeaders = [
  { key: 'X-Content-Type-Options', value: 'nosniff' },
  { key: 'X-Frame-Options', value: 'DENY' },
  { key: 'X-XSS-Protection', value: '1; mode=block' },
  { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
  { key: 'Permissions-Policy', value: 'camera=(), microphone=(), geolocation=()' },
]

export default {
  poweredByHeader: false, // Remove X-Powered-By
  headers: async () => [{ source: '/(.*)', headers: securityHeaders }],
}
```

### 9. Error Message Safety

```typescript
// ❌ WRONG: Exposes internals
catch (error) {
  return Response.json({ error: error.message, stack: error.stack }, { status: 500 })
}

// ✅ CORRECT: Generic user message, detailed server log
catch (error) {
  console.error('Internal error:', error)
  return Response.json({ error: 'An error occurred. Please try again.' }, { status: 500 })
}
```

## Pre-Deployment Security Checklist

Before ANY production deployment:

- [ ] **Secrets**: No hardcoded secrets, all in env vars
- [ ] **NEXT_PUBLIC_ audit**: No sensitive values exposed to client
- [ ] **Input Validation**: All user inputs validated with Zod
- [ ] **Server Actions**: Each has auth + validation + DAL
- [ ] **SQL Injection**: All queries parameterized
- [ ] **XSS**: CSP headers configured, user content sanitized
- [ ] **CSRF**: SameSite cookies, CSRF tokens on mutations
- [ ] **Rate Limiting**: Enabled on all public endpoints
- [ ] **HTTPS**: Enforced in production
- [ ] **Security Headers**: CSP, X-Frame-Options, nosniff configured
- [ ] **Error Handling**: No sensitive data in error responses
- [ ] **Logging**: No passwords, tokens, or PII in logs
- [ ] **Dependencies**: `npm audit --audit-level=high` clean
- [ ] **`poweredByHeader: false`**: Configured in `next.config.ts`

## Agent Support

- **security-reviewer** agent — Comprehensive security audit
- **code-reviewer** agent — Catches security issues during code review

## Security Response Protocol

If a security issue is found:
1. **STOP** immediately
2. Use **security-reviewer** agent for assessment
3. Fix CRITICAL issues before any other work
4. Rotate any exposed secrets
5. Review entire codebase for similar vulnerabilities
6. Add regression tests for the vulnerability

---

**Remember**: Security is not optional. One vulnerability can compromise the entire platform. When in doubt, err on the side of caution.
