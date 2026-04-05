<a name="phase-4"></a>
# 📌 PHASE 4: AUTHENTICATION & AUTHORIZATION (Security Expert)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 4.1: Implement Authentication System

```text
You are a Security Expert. Implement a comprehensive authentication system for a Next.js application.

Choose ONE:
- **Option A: Better Auth** (recommended — modern, type-safe, full-featured)
- **Option B: Auth.js v5** (NextAuth.js successor — widely adopted)
- **Option C: Clerk** (managed service — fastest to implement)
- **Option D: Custom** (full control — most complex)

Required Features:
1. Email/Password authentication with password hashing (argon2 or bcrypt)
2. Social authentication (Google, GitHub, Discord, etc.)
3. Magic Link authentication (passwordless)
4. Passkeys / WebAuthn support (modern passwordless)
5. Email verification flow
6. Password reset flow (with secure tokens)
7. Two-Factor Authentication (2FA — TOTP)
8. Session management (JWT or database sessions)
9. Account linking (connect multiple providers)
10. Rate limiting for auth endpoints
11. Account deletion (GDPR right to erasure)
```

#### Option A: Better Auth Setup

```typescript
// lib/auth.ts
import { betterAuth } from 'better-auth'
import { prismaAdapter } from 'better-auth/adapters/prisma'
import { twoFactor, passkey, magicLink, openAPI, admin } from 'better-auth/plugins'
import { db } from '@/lib/db'

export const auth = betterAuth({
  database: prismaAdapter(db, {
    provider: 'postgresql',
  }),
  emailAndPassword: {
    enabled: true,
    requireEmailVerification: true,
    minPasswordLength: 8,
  },
  socialProviders: {
    google: {
      clientId: process.env.GOOGLE_CLIENT_ID!,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET!,
    },
    github: {
      clientId: process.env.GITHUB_CLIENT_ID!,
      clientSecret: process.env.GITHUB_CLIENT_SECRET!,
    },
  },
  plugins: [
    twoFactor(),
    passkey(),
    magicLink({
      sendMagicLink: async ({ email, url }) => {
        await sendEmail({ to: email, subject: 'Login Link', url })
      },
    }),
    openAPI(), // Auto-generated API docs at /api/auth/reference
    admin(),   // Admin management endpoints
  ],
  session: {
    expiresIn: 60 * 60 * 24 * 7, // 7 days
    updateAge: 60 * 60 * 24,      // Refresh session daily
    cookieCache: {
      enabled: true,
      maxAge: 60 * 5, // 5 min cache to reduce DB lookups
    },
  },
  rateLimit: {
    window: 60,
    max: 10,
  },
  account: {
    accountLinking: {
      enabled: true,
      trustedProviders: ['google', 'github'],
    },
  },
  // GDPR: Account deletion
  user: {
    deleteUser: {
      enabled: true,
      sendDeleteAccountVerification: async ({ user, url }) => {
        await sendEmail({
          to: user.email,
          subject: 'Confirm Account Deletion',
          url,
        })
      },
    },
  },
})

// Export type for use in components
export type Session = typeof auth.$Infer.Session
```

```typescript
// lib/auth-client.ts — Client-side auth helpers
import { createAuthClient } from 'better-auth/react'
import { twoFactorClient, passkeyClient, magicLinkClient, adminClient } from 'better-auth/client/plugins'

export const authClient = createAuthClient({
  baseURL: process.env.NEXT_PUBLIC_APP_URL, // Required in production
  plugins: [
    twoFactorClient(),
    passkeyClient(),
    magicLinkClient(),
    adminClient(),
  ],
})

// Named exports for convenient imports throughout the app
export const {
  signIn,
  signUp,
  signOut,
  useSession,
  // Additional helpers
  useListSessions,
  revokeSession,
} = authClient
```

```tsx
// Usage in a Client Component:
// import { useSession, signOut } from '@/lib/auth-client'
//
// function UserMenu() {
//   const { data: session, isPending } = useSession()
//   if (isPending) return <Skeleton />
//   if (!session) return <Link href="/login">Sign in</Link>
//   return <button onClick={() => signOut()}>Sign out</button>
// }
```

```typescript
// app/api/auth/[...all]/route.ts
import { auth } from '@/lib/auth'
import { toNextJsHandler } from 'better-auth/next-js'

export const { GET, POST } = toNextJsHandler(auth)
```

#### Option B: Auth.js v5 Setup

```typescript
// auth.ts
import NextAuth from 'next-auth'
import { PrismaAdapter } from '@auth/prisma-adapter'
import Google from 'next-auth/providers/google'
import GitHub from 'next-auth/providers/github'
import Credentials from 'next-auth/providers/credentials'
import Passkey from 'next-auth/providers/passkey'
import { db } from '@/lib/db'

export const { handlers, auth, signIn, signOut } = NextAuth({
  adapter: PrismaAdapter(db),
  providers: [
    Google,
    GitHub,
    Passkey,
    Credentials({
      credentials: {
        email: { label: 'Email', type: 'email' },
        password: { label: 'Password', type: 'password' },
      },
      authorize: async (credentials) => {
        // Validate credentials against database
        // Return user object or null
      },
    }),
  ],
  experimental: { enableWebAuthn: true },
  session: { strategy: 'jwt' },
  pages: {
    signIn: '/login',
    error: '/auth/error',
  },
  callbacks: {
    authorized({ auth, request: { nextUrl } }) {
      const isLoggedIn = !!auth?.user
      const isProtected = nextUrl.pathname.startsWith('/dashboard')
      if (isProtected && !isLoggedIn) return false
      return true
    },
    jwt({ token, user }) {
      if (user) {
        token.role = user.role
        token.id = user.id
      }
      return token
    },
    session({ session, token }) {
      session.user.role = token.role as string
      session.user.id = token.id as string
      return session
    },
  },
})
```

#### Passkey UX Guidance

```text
Passkeys provide the best balance of security and UX, but require fallback flows:

1. **Registration Flow:**
   - Offer passkey creation after email/password registration (not required)
   - Show clear explanation: "Use fingerprint or face to sign in next time"
   - Always keep email/password as fallback

2. **Login Flow:**
   - Auto-suggest passkey if device supports it (conditional mediation)
   - Show "Sign in with passkey" button alongside traditional login
   - Fall back to email/password if passkey fails

3. **Device Compatibility:**
   - iOS 16+, Android 9+, macOS Ventura+, Windows 10+
   - Cross-device: QR code for using phone as authenticator
   - Show "Not supported" message gracefully on older browsers

4. **Implementation:**
   - Use WebAuthn API directly or via auth library (Better Auth/Auth.js)
   - Store public keys in database, never private keys
   - Support multiple passkeys per account
```

#### Auth Pages Structure

```
app/(auth)/
├── login/page.tsx          # Email/password + social + magic link + passkey
├── register/page.tsx       # Registration form
├── forgot-password/page.tsx # Password reset request
├── reset-password/page.tsx  # Password reset form (with token)
├── verify-email/page.tsx    # Email verification
├── two-factor/page.tsx      # 2FA verification
├── delete-account/page.tsx  # Account deletion confirmation
└── layout.tsx               # Auth layout (centered card)
```

---

### Prompt 4.2: Implement Authorization System (RBAC)

```text
You are a Security Expert. Implement Role-Based Access Control with fine-grained permissions.

Required:
1. Role definitions (ADMIN, MODERATOR, USER, VIEWER)
2. Permission definitions (granular: create, read, update, delete per resource)
3. Role-permission mapping
4. Server-side authorization checks (typed, not throwing raw errors)
5. Client-side permission-based UI rendering
6. Middleware-based route protection
7. Server Action authorization
```

```typescript
// lib/auth/permissions.ts
export const PERMISSIONS = {
  // Users
  'users:read': 'View users',
  'users:create': 'Create users',
  'users:update': 'Update users',
  'users:delete': 'Delete users',
  // Posts
  'posts:read': 'View posts',
  'posts:create': 'Create posts',
  'posts:update': 'Update own posts',
  'posts:update:any': 'Update any post',
  'posts:delete': 'Delete own posts',
  'posts:delete:any': 'Delete any post',
  'posts:publish': 'Publish posts',
  // Settings
  'settings:read': 'View settings',
  'settings:update': 'Update settings',
  // Admin
  'admin:access': 'Access admin panel',
} as const

export type Permission = keyof typeof PERMISSIONS

export const ROLE_PERMISSIONS: Record<string, Permission[]> = {
  ADMIN: Object.keys(PERMISSIONS) as Permission[],
  MODERATOR: [
    'users:read', 'posts:read', 'posts:create', 'posts:update:any',
    'posts:delete:any', 'posts:publish', 'admin:access',
  ],
  USER: [
    'posts:read', 'posts:create', 'posts:update', 'posts:delete',
  ],
  VIEWER: ['posts:read', 'users:read'],
}

// Check permission
export function hasPermission(userRole: string, permission: Permission): boolean {
  return ROLE_PERMISSIONS[userRole]?.includes(permission) ?? false
}
```

```typescript
// lib/auth/authorize.ts — Type-safe authorization (returns result, doesn't throw)
import { auth } from '@/lib/auth'
import { hasPermission, type Permission } from './permissions'
import type { Session } from '@/lib/auth'

type AuthResult =
  | { authorized: true; session: Session }
  | { authorized: false; error: 'UNAUTHORIZED' | 'FORBIDDEN' }

export async function authorize(permission?: Permission): Promise<AuthResult> {
  const session = await auth()

  if (!session?.user) {
    return { authorized: false, error: 'UNAUTHORIZED' }
  }

  if (permission && !hasPermission(session.user.role, permission)) {
    return { authorized: false, error: 'FORBIDDEN' }
  }

  return { authorized: true, session }
}

// Usage in Server Actions:
// const result = await authorize('posts:create')
// if (!result.authorized) return { success: false, error: result.error }
// const { session } = result
```

```tsx
// components/auth/permission-gate.tsx — Client-side permission UI
'use client'

import { useSession } from '@/lib/auth-client'
import { hasPermission, type Permission } from '@/lib/auth/permissions'

export function PermissionGate({
  permission,
  children,
  fallback = null,
}: {
  permission: Permission
  children: React.ReactNode
  fallback?: React.ReactNode
}) {
  const { data: session } = useSession()

  if (!session?.user?.role) return fallback
  if (!hasPermission(session.user.role, permission)) return fallback

  return <>{children}</>
}
```

```typescript
// middleware.ts — Combined auth + security headers
// This is the SINGLE middleware file. Phase 8 adds security headers here.
import { NextResponse, type NextRequest } from 'next/server'

const protectedRoutes = ['/dashboard', '/settings', '/admin']
const authRoutes = ['/login', '/register']

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  const sessionToken = request.cookies.get('session_token')?.value

  // --- Auth route protection ---

  // Redirect unauthenticated users away from protected routes
  if (protectedRoutes.some(route => pathname.startsWith(route)) && !sessionToken) {
    const loginUrl = new URL('/login', request.url)
    loginUrl.searchParams.set('callbackUrl', pathname)
    return NextResponse.redirect(loginUrl)
  }

  // Redirect authenticated users away from auth routes
  if (authRoutes.some(route => pathname.startsWith(route)) && sessionToken) {
    return NextResponse.redirect(new URL('/dashboard', request.url))
  }

  // --- Security headers (consolidated from Phase 8) ---
  const response = NextResponse.next()

  // CSP nonce for inline scripts
  const nonce = Buffer.from(crypto.randomUUID()).toString('base64')
  const csp = [
    `default-src 'self'`,
    `script-src 'self' 'nonce-${nonce}' 'strict-dynamic'`,
    `style-src 'self' 'unsafe-inline'`,
    `img-src 'self' blob: data:`,
    `font-src 'self'`,
    `connect-src 'self' ${process.env.NEXT_PUBLIC_POSTHOG_HOST || ''} https://*.sentry.io`,
    `frame-ancestors 'none'`,
    `base-uri 'self'`,
    `form-action 'self'`,
  ].join('; ')

  response.headers.set('Content-Security-Policy', csp)
  response.headers.set('X-Content-Type-Options', 'nosniff')
  response.headers.set('X-Frame-Options', 'DENY')
  response.headers.set('X-XSS-Protection', '0') // Disabled — CSP is the modern replacement
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
  response.headers.set('Permissions-Policy', 'camera=(), microphone=(), geolocation=()')
  response.headers.set('Strict-Transport-Security', 'max-age=63072000; includeSubDomains; preload')
  response.headers.set('x-nonce', nonce) // Pass nonce to layout via headers()

  return response
}

export const config = {
  matcher: [
    // Match all paths except static files and API routes
    '/((?!_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt|api).*)',
  ],
}
```

> **Note:** This middleware combines auth protection AND security headers in a single file. Phase 8 provides the detailed security header reference — but the actual implementation lives here. There is only ONE `middleware.ts` in a Next.js project.

---

### Prompt 4.3: Security Best Practices

```text
Implement comprehensive security measures for the Next.js application.

Required:
1. **Input Validation**: Zod schemas for ALL user inputs
2. **Output Encoding**: Prevent XSS (React handles most by default)
3. **CSRF Protection**: SameSite cookies + token validation
4. **Rate Limiting**: Upstash or custom for API and auth endpoints
5. **SQL Injection Prevention**: Parameterized queries via ORM
6. **Server-only Code**: Use `server-only` package to prevent client leaks
7. **Environment Security**: Type-safe env with @t3-oss/env-nextjs (see Phase 1)
8. **Dependency Security**: npm audit, Socket.dev
9. **Supply Chain Security**: npm provenance, lockfile integrity

NOTE: CSP headers and security middleware are consolidated in Phase 8 to avoid duplication.
```

```typescript
// lib/rate-limit.ts — Rate limiting with Upstash
import { Ratelimit } from '@upstash/ratelimit'
import { Redis } from '@upstash/redis'

const redis = Redis.fromEnv()

// General API rate limit: 10 requests per 10 seconds
export const ratelimit = new Ratelimit({
  redis,
  limiter: Ratelimit.slidingWindow(10, '10 s'),
  analytics: true,
})

// Stricter rate limit for auth: 5 requests per minute
export const authRatelimit = new Ratelimit({
  redis,
  limiter: Ratelimit.slidingWindow(5, '1 m'),
  analytics: true,
})

// Usage in Server Action or Route Handler
export async function checkRateLimit(identifier: string, limiter = ratelimit) {
  const { success, limit, reset, remaining } = await limiter.limit(identifier)
  if (!success) {
    const retryAfter = Math.ceil((reset - Date.now()) / 1000)
    throw new Error(`Rate limit exceeded. Try again in ${retryAfter}s`)
  }
  return { limit, remaining, reset }
}
```

```typescript
// lib/server-only-example.ts — Prevent accidental client-side import
import 'server-only'

// This file will cause a build error if imported in a Client Component
export function getSecretConfig() {
  return {
    apiKey: process.env.SECRET_API_KEY,
    dbUrl: process.env.DATABASE_URL,
  }
}
```

```text
Implement complete security system with authentication, authorization, and modern security best practices.
Security headers (CSP, HSTS, etc.) are handled in Phase 8 — do NOT duplicate middleware here.
```
