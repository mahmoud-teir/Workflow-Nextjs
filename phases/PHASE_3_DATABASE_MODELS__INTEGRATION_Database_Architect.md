<a name="phase-3"></a>
# 📌 PHASE 3: DATABASE MODELS & INTEGRATION (Database Architect)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 3.1: Design Database Schema

```text
You are a Database Architect. Design the complete database schema for [ProjectName] — a Next.js application.

Database: [PostgreSQL (Neon Serverless / Supabase) / MySQL (PlanetScale) / MongoDB / Convex]
ORM: [Prisma / Drizzle ORM / Convex schema]

Required Models & Relationships:
1. **User Model**: id, email, name, image, role, emailVerified, password (hashed), createdAt, updatedAt
2. **Core Business Models**: [Describe your business models based on your PRD]
3. **Audit Log**: Track who did what and when
4. **File/Upload Model**: Track uploaded files and assets

Design Principles:
1. Use UUIDs or CUIDs for primary keys (not auto-increment integers for distributed systems)
2. Add proper indexes for frequently queried fields
3. Define relationships (one-to-one, one-to-many, many-to-many)
4. Include soft delete (deletedAt) where appropriate
5. Add optimistic concurrency control (version field) where needed
6. Use enums for status fields
7. Add full-text search indexes where applicable
8. Consider Edge Runtime compatibility
9. Plan for database branching (Neon) in preview deployments
```

#### Prisma Schema:

```prisma
// prisma/schema.prisma
generator client {
  provider        = "prisma-client-js"
  previewFeatures = ["fullTextSearch", "driverAdapters"]
}

datasource db {
  provider  = "postgresql"
  url       = env("DATABASE_URL")
  directUrl = env("DIRECT_DATABASE_URL")
}

enum Role {
  USER
  ADMIN
  MODERATOR
}

model User {
  id            String    @id @default(cuid())
  email         String    @unique
  name          String?
  image         String?
  role          Role      @default(USER)
  emailVerified DateTime?
  password      String?
  createdAt     DateTime  @default(now())
  updatedAt     DateTime  @updatedAt
  deletedAt     DateTime?

  // Relations
  posts     Post[]
  sessions  Session[]
  auditLogs AuditLog[]

  @@index([email])
  @@index([role])
  @@map("users")
}
```

#### Drizzle Schema:

```typescript
// drizzle/schema.ts
import {
  pgTable, uuid, text, timestamp, boolean,
  pgEnum, index, uniqueIndex, integer
} from 'drizzle-orm/pg-core'
import { relations } from 'drizzle-orm'

export const roleEnum = pgEnum('role', ['USER', 'ADMIN', 'MODERATOR'])

export const users = pgTable('users', {
  id: uuid('id').primaryKey().defaultRandom(),
  email: text('email').notNull().unique(),
  name: text('name'),
  image: text('image'),
  role: roleEnum('role').default('USER').notNull(),
  emailVerified: timestamp('email_verified'),
  password: text('password'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
  updatedAt: timestamp('updated_at').$onUpdate(() => new Date()),
  deletedAt: timestamp('deleted_at'),
}, (table) => [
  uniqueIndex('email_idx').on(table.email),
  index('role_idx').on(table.role),
])

export const usersRelations = relations(users, ({ many }) => ({
  posts: many(posts),
  auditLogs: many(auditLogs),
}))
```

> **Note:** Drizzle ORM index syntax uses an array return in latest versions (not object). Check `drizzle-orm` docs for your installed version.

#### Convex Schema:

```typescript
// convex/schema.ts
import { defineSchema, defineTable } from "convex/server"
import { v } from "convex/values"

export default defineSchema({
  users: defineTable({
    email: v.string(),
    name: v.optional(v.string()),
    image: v.optional(v.string()),
    role: v.union(v.literal("USER"), v.literal("ADMIN"), v.literal("MODERATOR")),
    emailVerified: v.optional(v.number()),
    tokenIdentifier: v.string(),
  })
    .index("by_email", ["email"])
    .index("by_token", ["tokenIdentifier"]),
})
```

---

### Prompt 3.2: Create Service Layer (Repository Pattern)

```text
You are a Backend Architect. Create a service layer with the repository pattern for database operations.

Required:
1. Base repository with common CRUD operations
2. Entity-specific repositories with custom queries
3. Service layer for business logic
4. Transaction support
5. Caching integration (React cache() + unstable_cache)
6. Type-safe query builders
```

```typescript
// lib/repositories/base.ts
import { db } from '@/lib/db'
import { cache } from 'react'

// Type-safe pagination
export interface PaginationParams {
  page?: number
  limit?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

export interface PaginatedResult<T> {
  data: T[]
  total: number
  page: number
  limit: number
  totalPages: number
  hasMore: boolean
}

// Base repository pattern
export abstract class BaseRepository<T> {
  abstract findById(id: string): Promise<T | null>
  abstract findMany(params?: PaginationParams): Promise<PaginatedResult<T>>
  abstract create(data: Partial<T>): Promise<T>
  abstract update(id: string, data: Partial<T>): Promise<T>
  abstract delete(id: string): Promise<void>
  abstract softDelete(id: string): Promise<void>
}
```

```typescript
// lib/services/user-service.ts
import { cache } from 'react'
import { unstable_cache } from 'next/cache'
import { db } from '@/lib/db'

// Request-level deduplication using React cache()
// Same query in the same request is only executed once
export const getUserById = cache(async (id: string) => {
  return db.user.findUnique({ where: { id } })
})

// Cross-request caching using unstable_cache
// ⚠️ Note: `unstable_cache` may be renamed to `cache` or replaced by
// `cacheLife`/`cacheTag` in future Next.js versions. Check Phase 0.7 compat table.
export const getUsers = unstable_cache(
  async (page = 1, limit = 10, search?: string) => {
    const where = {
      deletedAt: null,
      ...(search && {
        OR: [
          { name: { contains: search, mode: 'insensitive' as const } },
          { email: { contains: search, mode: 'insensitive' as const } },
        ],
      }),
    }

    const [users, total] = await Promise.all([
      db.user.findMany({
        skip: (page - 1) * limit,
        take: limit,
        orderBy: { createdAt: 'desc' },
        where,
      }),
      db.user.count({ where }),
    ])

    return {
      data: users,
      total,
      page,
      limit,
      totalPages: Math.ceil(total / limit),
      hasMore: page * limit < total,
    }
  },
  ['users-list'],
  {
    revalidate: 60,
    tags: ['users'],
  }
)
```

---

### Prompt 3.3: Data Validation & Transformation

```text
Create comprehensive data validation using Zod with type inference.
```

```typescript
// lib/validations/user.ts
import { z } from 'zod'

// Base schemas (reusable)
export const emailSchema = z.string().email('Invalid email address').toLowerCase().trim()
export const passwordSchema = z
  .string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Must contain uppercase letter')
  .regex(/[a-z]/, 'Must contain lowercase letter')
  .regex(/[0-9]/, 'Must contain number')
  .regex(/[^A-Za-z0-9]/, 'Must contain special character')

// Create user schema
export const createUserSchema = z.object({
  email: emailSchema,
  name: z.string().min(2).max(100),
  password: passwordSchema,
  role: z.enum(['USER', 'ADMIN', 'MODERATOR']).default('USER'),
})

// Update user schema (all fields optional)
export const updateUserSchema = createUserSchema.partial().omit({ password: true })

// TypeScript types inferred from Zod schemas
export type CreateUserInput = z.infer<typeof createUserSchema>
export type UpdateUserInput = z.infer<typeof updateUserSchema>

// Pagination query schema (reusable across endpoints)
export const paginationSchema = z.object({
  page: z.coerce.number().int().positive().default(1),
  limit: z.coerce.number().int().positive().max(100).default(10),
  search: z.string().optional(),
  sortBy: z.string().optional(),
  sortOrder: z.enum(['asc', 'desc']).default('desc'),
})

export type PaginationQuery = z.infer<typeof paginationSchema>
```

---

### Prompt 3.4: Database Seeding & Migrations

```text
Create database seed scripts and migration utilities.
```

```typescript
// prisma/seed.ts (for Prisma)
import { PrismaClient } from '@prisma/client'
import { hash } from 'bcryptjs'
import { faker } from '@faker-js/faker'

const prisma = new PrismaClient()

async function main() {
  console.log('🌱 Seeding database...')

  // Create admin user
  const adminPassword = await hash('Admin123!@#', 12)
  const admin = await prisma.user.upsert({
    where: { email: 'admin@example.com' },
    update: {},
    create: {
      email: 'admin@example.com',
      name: 'Admin User',
      password: adminPassword,
      role: 'ADMIN',
      emailVerified: new Date(),
    },
  })

  // Create test users with realistic data
  const testUsers = await Promise.all(
    Array.from({ length: 20 }, async () => {
      const password = await hash('Test123!@#', 12)
      return prisma.user.create({
        data: {
          email: faker.internet.email().toLowerCase(),
          name: faker.person.fullName(),
          password,
          role: 'USER',
          emailVerified: faker.datatype.boolean() ? new Date() : null,
          createdAt: faker.date.past({ years: 1 }),
        },
      })
    })
  )

  console.log('✅ Seed complete:', {
    admin: admin.email,
    testUsers: testUsers.length,
  })
}

main()
  .catch((e) => {
    console.error('❌ Seed failed:', e)
    process.exit(1)
  })
  .finally(async () => {
    await prisma.$disconnect()
  })
```

#### Migration Commands:

**Prisma:**
```json
{
  "scripts": {
    "db:generate": "prisma generate",
    "db:push": "prisma db push",
    "db:migrate": "prisma migrate dev",
    "db:migrate:prod": "prisma migrate deploy",
    "db:seed": "tsx prisma/seed.ts",
    "db:studio": "prisma studio",
    "db:reset": "prisma migrate reset"
  }
}
```

**Drizzle:**
```json
{
  "scripts": {
    "db:generate": "drizzle-kit generate",
    "db:migrate": "drizzle-kit migrate",
    "db:push": "drizzle-kit push",
    "db:studio": "drizzle-kit studio",
    "db:seed": "tsx drizzle/seed.ts"
  }
}
```

#### Migration Rollback Strategy:

```text
For Prisma:
- `prisma migrate resolve --rolled-back <migration_name>` to mark a migration as rolled back
- Always create a "down" migration manually for critical changes
- Use `prisma migrate diff` to generate rollback SQL

For Drizzle:
- Use `drizzle-kit drop` to remove the last migration
- Keep rollback SQL scripts alongside migrations
- Use database branching (Neon) for safe testing before deploying migrations

Best Practices:
- Never drop columns directly in production — use a two-step process:
  1. Deploy code that doesn't read the column
  2. Drop the column in a subsequent migration
- Always backup before running migrations in production
- Use database branching (Neon) for preview deployments to test migrations safely
```

#### Database Branching (Neon):

```text
Neon allows creating database branches for preview deployments:

1. Create a branch for each PR/preview deployment
2. Branch inherits all data from parent (copy-on-write, instant)
3. Run migrations on the branch safely
4. Delete branch when PR is merged/closed

Setup in CI/CD:
- Use Neon GitHub Integration or Neon API
- Set DATABASE_URL dynamically per preview deployment
- Branch from `main` database for each PR

This eliminates the need for a separate staging database.
```

```text
Implement comprehensive database layer with type safety, caching, and proper error handling.
```
