---
name: nextjs-patterns
description: Next.js 15/16+ patterns — App Router, Server Components, Server Actions, Turbopack, PPR, and modern React patterns.
origin: ECC
stack: Next.js 15/16+, React 19
---

# Next.js Patterns Skill

Canonical patterns for building production-grade Next.js applications.

## When to Activate

- Building with Next.js App Router
- Creating Server Components or Server Actions
- Configuring Turbopack or caching
- Working with React 19 features
- Optimizing Next.js performance

## Core Architecture

### Server vs Client Components

```typescript
// ✅ Server Component (default in App Router) — no directive needed
// Runs on the server, zero JS sent to client
export default async function DashboardPage() {
  const data = await db.analytics.findMany()
  return <DashboardChart data={data} />
}

// ✅ Client Component — explicit directive required
'use client'
import { useState } from 'react'
export function Counter() {
  const [count, setCount] = useState(0)
  return <button onClick={() => setCount(c => c + 1)}>{count}</button>
}
```

### Decision Guide
| Need | Component Type |
|------|---------------|
| Fetch data, access DB | Server Component |
| Use `useState`, `useEffect` | Client Component |
| Use event handlers (`onClick`) | Client Component |
| Display static content | Server Component |
| Use browser APIs (`window`) | Client Component |
| Access filesystem or secrets | Server Component |

### Composition Pattern: Server Wraps Client
```typescript
// page.tsx (Server)
import { ClientInteractive } from './ClientInteractive'
export default async function Page() {
  const data = await fetchData() // Server-side
  return <ClientInteractive initialData={data} /> // Pass as props
}
```

## Server Actions

### Pattern: Form Action with Validation
```typescript
'use server'
import { z } from 'zod'
import { revalidatePath } from 'next/cache'
import { redirect } from 'next/navigation'

const schema = z.object({
  title: z.string().min(1).max(200),
  content: z.string().min(1),
})

export async function createPost(formData: FormData) {
  const parsed = schema.safeParse({
    title: formData.get('title'),
    content: formData.get('content'),
  })

  if (!parsed.success) {
    return { errors: parsed.error.flatten().fieldErrors }
  }

  await db.posts.create({ data: parsed.data })
  revalidatePath('/posts')
  redirect('/posts')
}
```

### Pattern: useActionState for Progressive Enhancement
```typescript
'use client'
import { useActionState } from 'react'
import { createPost } from './actions'

export function PostForm() {
  const [state, action, pending] = useActionState(createPost, null)

  return (
    <form action={action}>
      <input name="title" required />
      {state?.errors?.title && <p className="error">{state.errors.title}</p>}
      <textarea name="content" required />
      <button type="submit" disabled={pending}>
        {pending ? 'Creating...' : 'Create Post'}
      </button>
    </form>
  )
}
```

## Data Fetching

### Pattern: Parallel Data Fetching
```typescript
export default async function DashboardPage() {
  // ✅ Parallel — both requests fire simultaneously
  const [users, analytics] = await Promise.all([
    db.users.count(),
    db.analytics.getRecent(),
  ])

  return <Dashboard users={users} analytics={analytics} />
}
```

### Pattern: Streaming with Suspense
```typescript
import { Suspense } from 'react'

export default function Page() {
  return (
    <div>
      <h1>Dashboard</h1> {/* Renders immediately */}
      <Suspense fallback={<ChartSkeleton />}>
        <SlowChart />  {/* Streams in when ready */}
      </Suspense>
    </div>
  )
}
```

## Routing

### Dynamic Routes with Async Params (Next.js 15+)
```typescript
// app/posts/[id]/page.tsx
export default async function PostPage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = await params  // params is now a Promise
  const post = await db.posts.findUnique({ where: { id } })
  if (!post) notFound()
  return <PostView post={post} />
}
```

### Metadata
```typescript
import type { Metadata } from 'next'

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>
}): Promise<Metadata> {
  const { id } = await params
  const post = await db.posts.findUnique({ where: { id } })
  return {
    title: post?.title ?? 'Not Found',
    description: post?.excerpt,
  }
}
```

## Layout Patterns

### Root Layout with Providers
```typescript
// app/layout.tsx
import { Inter } from 'next/font/google'
import { ThemeProvider } from './providers'

const inter = Inter({ subsets: ['latin'] })

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={inter.className}>
        <ThemeProvider>{children}</ThemeProvider>
      </body>
    </html>
  )
}
```

### Loading and Error States
```
app/
├── layout.tsx       # Root layout
├── loading.tsx      # Root loading state
├── error.tsx        # Root error boundary
├── not-found.tsx    # 404 page
└── dashboard/
    ├── layout.tsx   # Dashboard layout
    ├── loading.tsx  # Dashboard loading
    ├── error.tsx    # Dashboard error boundary
    └── page.tsx     # Dashboard page
```

## Turbopack (Next.js 16+)

- **Default in dev** — `next dev` uses Turbopack automatically
- **File-system caching** — Restarts reuse previous work (much faster)
- **Disable if needed** — `next dev --webpack` for legacy plugins
- **Production** — Check docs for your Next.js version

## Performance Patterns

### Image Optimization
```typescript
import Image from 'next/image'

<Image
  src="/hero.jpg"
  alt="Hero image"
  width={1200}
  height={600}
  priority  // Above-fold images
  placeholder="blur"
  blurDataURL="/hero-blur.jpg"
/>
```

### Route Handlers with Caching
```typescript
// app/api/products/route.ts
export const revalidate = 3600 // Cache for 1 hour

export async function GET() {
  const products = await db.products.findMany()
  return Response.json(products)
}
```

## Common Mistakes to Avoid

| Mistake | Correct Approach |
|---------|-----------------|
| Using `useState` in Server Components | Move to Client Component or use server-side state |
| Passing functions as props to Client Components | Use Server Actions instead |
| Using `useEffect` for data fetching | Fetch in Server Components |
| Not handling `loading.tsx` | Always create loading states |
| Ignoring `error.tsx` | Always create error boundaries |
| Large client bundles | Push logic to Server Components |

---

**Remember**: Server Components are the default for a reason. Start server-first and only add `'use client'` when you need interactivity.
