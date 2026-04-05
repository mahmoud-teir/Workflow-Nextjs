<a name="phase-2"></a>
# 📌 PHASE 2: BACKEND SETUP — API ROUTES & SERVER ACTIONS (Backend Developer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 2.1: Database & ORM Setup

```text
You are a Senior Backend Developer. Set up the database and ORM for my Next.js application.

Database: [PostgreSQL with Neon / Supabase / PlanetScale / MongoDB / Convex]
ORM: [Prisma / Drizzle ORM]

Required:
1. Database connection configuration with connection pooling
2. Schema definition with all models
3. Migration setup and seed scripts
4. Type-safe database client
5. Edge-compatible database driver (for Edge Runtime support)
6. Connection health check utility

Provide COMPLETE CODE for:
```

#### Option A: Prisma Setup

```typescript
// prisma/schema.prisma
generator client {
  provider        = "prisma-client-js"
  previewFeatures = ["driverAdapters"] // For Edge Runtime support
}

datasource db {
  provider  = "postgresql"
  url       = env("DATABASE_URL")
  directUrl = env("DIRECT_DATABASE_URL") // For connection pooling (Neon/Supabase)
}

// Models defined in Phase 3...
```

```typescript
// lib/db.ts - Database client with singleton pattern
import { PrismaClient } from '@prisma/client'
import { withAccelerate } from '@prisma/extension-accelerate'

const globalForPrisma = globalThis as unknown as {
  prisma: ReturnType<typeof createPrismaClient> | undefined
}

function createPrismaClient() {
  return new PrismaClient({
    log: process.env.NODE_ENV === 'development' ? ['query', 'error', 'warn'] : ['error'],
  }).$extends(withAccelerate())
}

export const db = globalForPrisma.prisma ?? createPrismaClient()

if (process.env.NODE_ENV !== 'production') globalForPrisma.prisma = db
```

```bash
# Required for Edge Runtime / Accelerate support
pnpm add @prisma/extension-accelerate
```

> **Edge Runtime:** If deploying to Edge (Vercel Edge Functions, Cloudflare Workers), use Prisma Accelerate or the Neon serverless driver adapter. The `driverAdapters` preview feature in `schema.prisma` enables this. See the [Prisma Edge docs](https://www.prisma.io/docs/orm/prisma-client/deployment/edge) for adapter-specific setup.

```typescript
// lib/db-health.ts - Connection health check
import { db } from '@/lib/db'

export async function checkDatabaseHealth(): Promise<{
  status: 'ok' | 'error'
  latencyMs: number
  message?: string
}> {
  const start = performance.now()
  try {
    await db.$queryRaw`SELECT 1`
    return { status: 'ok', latencyMs: Math.round(performance.now() - start) }
  } catch (error) {
    return {
      status: 'error',
      latencyMs: Math.round(performance.now() - start),
      message: error instanceof Error ? error.message : 'Unknown error',
    }
  }
}
```

#### Option B: Drizzle ORM Setup

```typescript
// drizzle/schema.ts
import { pgTable, text, timestamp, uuid, boolean, integer } from 'drizzle-orm/pg-core'

export const users = pgTable('users', {
  id: uuid('id').primaryKey().defaultRandom(),
  email: text('email').notNull().unique(),
  name: text('name'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
  updatedAt: timestamp('updated_at').$onUpdate(() => new Date()),
})
```

```typescript
// lib/db.ts - Drizzle with Neon serverless driver
import { drizzle } from 'drizzle-orm/neon-http'
import { neon } from '@neondatabase/serverless'
import * as schema from '@/drizzle/schema'

const sql = neon(process.env.DATABASE_URL!)
export const db = drizzle(sql, { schema })
```

```typescript
// drizzle.config.ts
import type { Config } from 'drizzle-kit'

export default {
  schema: './drizzle/schema.ts',
  out: './drizzle/migrations',
  dialect: 'postgresql',
  dbCredentials: {
    url: process.env.DATABASE_URL!,
  },
} satisfies Config
```

#### Option C: Convex Setup (Real-time database — no ORM needed)

```typescript
// convex/schema.ts
import { defineSchema, defineTable } from "convex/server"
import { v } from "convex/values"

export default defineSchema({
  users: defineTable({
    email: v.string(),
    name: v.optional(v.string()),
    role: v.union(v.literal("admin"), v.literal("user")),
  }).index("by_email", ["email"]),
})
```

```text
Include:
- Connection pooling configuration (PgBouncer/Neon pooler)
- Edge Runtime compatibility
- Type-safe queries
- Error handling
- Seed scripts
- Migration commands
- Database branching for preview deployments (Neon)
```

---

### Prompt 2.2: Route Handlers (API Routes)

```text
You are a Backend Developer. Create Route Handlers for the Next.js application using App Router.

Project: [ProjectName]
Database: [your database]
Auth: [your auth system]

Required:
1. Route Handler structure using app/api/[route]/route.ts
2. Support for HTTP methods: GET, POST, PUT, PATCH, DELETE
3. TypeScript types for request/response
4. Error handling middleware pattern
5. Zod validation for request bodies
6. Edge Runtime support where applicable
```

```typescript
// app/api/health/route.ts — Health check endpoint
import { NextResponse } from 'next/server'
import { checkDatabaseHealth } from '@/lib/db-health'

export const runtime = 'edge' // Optional: Edge Runtime for faster cold starts

export async function GET() {
  try {
    const dbHealth = await checkDatabaseHealth()

    return NextResponse.json({
      status: dbHealth.status === 'ok' ? 'healthy' : 'degraded',
      timestamp: new Date().toISOString(),
      version: process.env.npm_package_version || '1.0.0',
      checks: { database: dbHealth },
    })
  } catch (error) {
    return NextResponse.json(
      { status: 'error', message: 'Service unavailable' },
      { status: 503 }
    )
  }
}
```

```typescript
// lib/api-utils.ts — Shared API utilities
import { NextRequest, NextResponse } from 'next/server'
import { ZodSchema, ZodError } from 'zod'

// Type-safe route context (replaces `any`)
type RouteContext = {
  params: Promise<Record<string, string>>
}

// Type-safe request parser
export async function parseBody<T>(
  request: NextRequest,
  schema: ZodSchema<T>
): Promise<T> {
  const body = await request.json()
  return schema.parse(body)
}

// Standardized error response
export function apiError(message: string, status: number, details?: unknown) {
  return NextResponse.json(
    { error: message, details, timestamp: new Date().toISOString() },
    { status }
  )
}

// Standardized success response
export function apiSuccess<T>(data: T, status = 200) {
  return NextResponse.json(
    { data, timestamp: new Date().toISOString() },
    { status }
  )
}

// Route handler wrapper with error handling
export function withErrorHandler(
  handler: (req: NextRequest, ctx: RouteContext) => Promise<NextResponse>
) {
  return async (req: NextRequest, ctx: RouteContext) => {
    try {
      return await handler(req, ctx)
    } catch (error) {
      if (error instanceof ZodError) {
        return apiError('Validation error', 422, error.flatten())
      }
      console.error('API Error:', error)
      return apiError('Internal server error', 500)
    }
  }
}

// Middleware composition — chain multiple middleware functions
export function composeMiddleware(
  ...middlewares: Array<
    (req: NextRequest, ctx: RouteContext, next: () => Promise<NextResponse>) => Promise<NextResponse>
  >
) {
  return (handler: (req: NextRequest, ctx: RouteContext) => Promise<NextResponse>) => {
    return async (req: NextRequest, ctx: RouteContext) => {
      let index = 0
      const next = async (): Promise<NextResponse> => {
        if (index < middlewares.length) {
          const middleware = middlewares[index++]
          return middleware(req, ctx, next)
        }
        return handler(req, ctx)
      }
      return next()
    }
  }
}
```

```text
Include:
- Rate limiting with Upstash (@upstash/ratelimit)
- CORS configuration
- OpenAPI/Swagger documentation with Scalar or next-swagger-doc
- Pagination helpers
- Authentication middleware integration
- API versioning pattern (URL prefix or header-based)
```

#### API Versioning Pattern (URL prefix):

```typescript
// app/api/v1/users/route.ts — Versioned API
import { withErrorHandler, apiSuccess } from '@/lib/api-utils'

export const GET = withErrorHandler(async (req) => {
  // v1 implementation
  const users = await getUsers()
  return apiSuccess(users)
})

// app/api/v2/users/route.ts — New version with breaking changes
export const GET = withErrorHandler(async (req) => {
  // v2 implementation with different response shape
  const users = await getUsersV2()
  return apiSuccess(users)
})
```

---

### Prompt 2.3: Server Actions (Primary Mutation Pattern)

```text
You are a Senior Full-Stack Developer. Create Server Actions for data mutations in the Next.js application.

Server Actions are the PRIMARY approach for data mutations — prefer them over API routes for form handling and mutations.

Project: [ProjectName]
Database: [your database]
Forms Library: [React Hook Form / Conform / native form actions]

Required:
1. Server Actions in dedicated files (app/actions/ directory)
2. Zod validation for all inputs
3. useActionState for form state management (React 19)
4. useOptimistic for optimistic updates (React 19)
5. Proper revalidation with revalidatePath/revalidateTag
6. Error handling and type-safe return values
7. Rate limiting for sensitive actions
```

```typescript
// lib/action-utils.ts — Shared action utilities
'use server'

import { auth } from '@/lib/auth'
import { ZodSchema, ZodError } from 'zod'

// Type-safe action result
export type ActionResult<T = void> =
  | { success: true; data: T }
  | { success: false; error: string; fieldErrors?: Record<string, string[]> }

// Validate form data against a Zod schema
export function validateFormData<T>(
  formData: FormData,
  schema: ZodSchema<T>
): ActionResult<T> | T {
  const raw = Object.fromEntries(formData.entries())
  const parsed = schema.safeParse(raw)

  if (!parsed.success) {
    return {
      success: false,
      error: 'Validation failed',
      fieldErrors: parsed.error.flatten().fieldErrors as Record<string, string[]>,
    }
  }

  return parsed.data
}

// Auth guard for server actions — returns typed result
type AuthSuccess = { success: true; user: { id: string; email: string; role: string } }
type AuthFailure = { success: false; error: string }
type AuthResult = AuthSuccess | AuthFailure

export async function requireAuth(): Promise<AuthResult> {
  const session = await auth()
  if (!session?.user) {
    return { success: false, error: 'Unauthorized' }
  }
  return { success: true, user: session.user }
}
```

```typescript
// app/actions/user.ts
'use server'

import { z } from 'zod'
import { db } from '@/db'
import { revalidatePath } from 'next/cache'
import type { ActionResult } from '@/lib/action-utils'
import { requireAuth } from '@/lib/action-utils'

// Validation schemas
const createUserSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
})

// Create user action
export async function createUser(
  prevState: ActionResult,
  formData: FormData
): Promise<ActionResult> {
  // Auth check
  const authResult = await requireAuth()
  if (!authResult.success) return { success: false, error: authResult.error }

  // Validate input
  const parsed = createUserSchema.safeParse({
    name: formData.get('name'),
    email: formData.get('email'),
  })

  if (!parsed.success) {
    return {
      success: false,
      error: 'Validation failed',
      fieldErrors: parsed.error.flatten().fieldErrors as Record<string, string[]>,
    }
  }

  try {
    await db.user.create({ data: parsed.data })
    revalidatePath('/users')
    return { success: true, data: undefined }
  } catch (error) {
    return { success: false, error: 'Failed to create user' }
  }
}
```

```tsx
// components/forms/create-user-form.tsx
'use client'

import { useActionState, useOptimistic } from 'react'
import { createUser } from '@/app/actions/user'

export function CreateUserForm() {
  const [state, action, isPending] = useActionState(createUser, {
    success: false,
    error: '',
  })

  return (
    <form action={action}>
      <div>
        <label htmlFor="name">Name</label>
        <input id="name" name="name" required />
        {!state.success && state.fieldErrors?.name && (
          <p className="text-sm text-destructive">{state.fieldErrors.name[0]}</p>
        )}
      </div>

      <div>
        <label htmlFor="email">Email</label>
        <input id="email" name="email" type="email" required />
        {!state.success && state.fieldErrors?.email && (
          <p className="text-sm text-destructive">{state.fieldErrors.email[0]}</p>
        )}
      </div>

      <button type="submit" disabled={isPending}>
        {isPending ? 'Creating...' : 'Create User'}
      </button>

      {!state.success && state.error && (
        <p className="text-sm text-destructive">{state.error}</p>
      )}
    </form>
  )
}
```

```text
Include:
- File upload actions
- Delete with confirmation
- Bulk operations
- Optimistic updates with useOptimistic
- Progressive enhancement (works without JavaScript)
- after() API for background tasks (logging, analytics) — check version compat
- Rate limiting for sensitive actions (using Upstash or in-memory)
```

#### Background Tasks with `after()` API:

```typescript
// app/actions/post.ts
'use server'

import { after } from 'next/server'
import { db } from '@/lib/db'
import { revalidateTag } from 'next/cache'
import { trackEvent } from '@/lib/analytics'

export async function publishPost(postId: string) {
  const authResult = await requireAuth()
  if (!authResult.success) return { success: false, error: authResult.error }

  const post = await db.post.update({
    where: { id: postId },
    data: { status: 'PUBLISHED', publishedAt: new Date() },
  })

  revalidateTag('posts')

  // Run after the response is sent to the client
  after(async () => {
    await trackEvent('post_published', { postId, userId: authResult.user.id })
    await sendNotificationToSubscribers(post)
  })

  return { success: true, data: post }
}
```

```text
Write production-ready, type-safe code with comprehensive error handling.
```
