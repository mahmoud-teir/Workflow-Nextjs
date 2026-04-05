<a name="phase-6"></a>
# 📌 PHASE 6: ADVANCED FEATURES (Full-Stack Developer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 6.1: Search Functionality (Database & AI)

```text
You are a Full-Stack Developer. Implement search functionality in a Next.js application.

Choice:
A) **Database Search** (Prototyping / Small Apps) → Prisma Full-Text Search or Drizzle LIKE/ILIKE
B) **AI-Powered Semantic Search** (Modern / Medium Apps) → pgvector + OpenAI Embeddings
C) **Dedicated Search Engine** (Large Scale Apps) → Algolia or Meilisearch

Required:
1. Server Action for search query
2. Debounced search input (client-side)
3. Optimistic UI updates
4. Results pagination
5. Filter & Sort interface
```

#### Option A: Database Full-Text Search (Drizzle + PostgreSQL)

```typescript
// app/actions/search.ts
'use server'

import { db } from '@/lib/db'
import { users } from '@/drizzle/schema'
import { ilike, or, sql } from 'drizzle-orm'

export async function searchUsers(query: string) {
  if (!query || query.length < 2) return []

  // Sanitize input (ORM handles parameterization)
  const results = await db
    .select()
    .from(users)
    .where(
      or(
        ilike(users.name, `%${query}%`),
        ilike(users.email, `%${query}%`)
      )
    )
    .limit(10)

  return results
}
```

#### Option B: AI Semantic Search (pgvector + Vercel AI SDK)

```typescript
// app/actions/semantic-search.ts
'use server'

import { embed } from 'ai'
import { openai } from '@ai-sdk/openai'
import { db } from '@/lib/db'
import { cosineDistance, desc, gt, sql } from 'drizzle-orm'
import { documents } from '@/drizzle/schema'

export async function semanticSearch(query: string) {
  const { embedding } = await embed({
    model: openai.embedding('text-embedding-3-small'),
    value: query,
  })

  const similarity = sql<number>`1 - (${cosineDistance(
    documents.embedding,
    embedding
  )})`

  const results = await db
    .select({
      id: documents.id,
      content: documents.content,
      similarity,
    })
    .from(documents)
    .where(gt(similarity, 0.5))
    .orderBy(desc(similarity))
    .limit(5)

  return results
}
```

---

### Prompt 6.2: File Upload & Management

```text
Implement file uploads using modern solutions.

Recommended: **UploadThing** (Type-safe, easy setup) or **Vercel Blob** (Simple storage).
```

#### Option A: UploadThing Setup

```typescript
// app/api/uploadthing/core.ts
import { createUploadthing, type FileRouter } from 'uploadthing/next'
import { auth } from '@/lib/auth'

const f = createUploadthing()

export const ourFileRouter = {
  imageUploader: f({ image: { maxFileSize: '4MB', maxFileCount: 1 } })
    .middleware(async () => {
      const session = await auth()
      if (!session) throw new Error('Unauthorized')
      return { userId: session.user.id }
    })
    .onUploadComplete(async ({ metadata, file }) => {
      // Save file reference to database
      await db.file.create({
        data: {
          url: file.url,
          name: file.name,
          size: file.size,
          userId: metadata.userId,
        },
      })
    }),

  documentUploader: f({
    pdf: { maxFileSize: '16MB', maxFileCount: 5 },
  })
    .middleware(async () => {
      const session = await auth()
      if (!session) throw new Error('Unauthorized')
      return { userId: session.user.id }
    })
    .onUploadComplete(async ({ metadata, file }) => {
      console.log('Upload complete:', file.url)
    }),
} satisfies FileRouter

export type OurFileRouter = typeof ourFileRouter
```

```typescript
// lib/uploadthing.ts — client helpers (import these in components)
import { generateReactHelpers } from '@uploadthing/react'
import type { OurFileRouter } from '@/app/api/uploadthing/core'

export const { useUploadThing, uploadFiles } = generateReactHelpers<OurFileRouter>()
```

#### Option B: Vercel Blob (Server Action)

```typescript
// app/actions/upload.ts
'use server'

import { put, del } from '@vercel/blob'
import { revalidatePath } from 'next/cache'
import { requireAuth } from '@/lib/action-utils'

export async function uploadFile(formData: FormData) {
  const session = await requireAuth()
  if ('error' in session) return session

  const file = formData.get('file') as File
  if (!file) return { success: false, error: 'No file provided' }

  // Validate file type and size
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) return { success: false, error: 'File too large' }

  const blob = await put(file.name, file, { access: 'public' })

  revalidatePath('/files')
  return { success: true, data: blob }
}

export async function deleteFile(url: string) {
  const session = await requireAuth()
  if ('error' in session) return session

  await del(url)
  revalidatePath('/files')
  return { success: true, data: undefined }
}
```

---

### Prompt 6.3: Real-time Features (WebSockets / SSE)

```text
Implement real-time updates.

Recommended: **Convex** (built-in realtime) or **PartyKit / Pusher** (easier than custom WS).
```

#### Option A: Convex (Realtime Database Subscription)

```typescript
// convex/schema.ts — define your Convex tables
import { defineSchema, defineTable } from 'convex/server'
import { v } from 'convex/values'

export default defineSchema({
  messages: defineTable({
    text: v.string(),
    userId: v.id('users'),
  }).index('by_user', ['userId']),
})
```

```tsx
// app/chat/page.tsx
'use client'

import { useQuery, useMutation } from 'convex/react'
import { api } from '@/convex/_generated/api'
import { useState } from 'react'

export default function ChatPage() {
  // Realtime subscription - updates automatically!
  const messages = useQuery(api.messages.list) || []
  const sendMessage = useMutation(api.messages.send)
  const [text, setText] = useState('')

  return (
    <div>
      {messages.map(msg => (
        <div key={msg._id}>{msg.text}</div>
      ))}
      <form onSubmit={e => {
        e.preventDefault()
        sendMessage({ text })
        setText('')
      }}>
        <input value={text} onChange={e => setText(e.target.value)} />
        <button>Send</button>
      </form>
    </div>
  )
}
```

#### Option B: Server-Sent Events (SSE) with Proper Cleanup

```typescript
// app/api/sse/route.ts
export const runtime = 'nodejs'

export async function GET(request: Request) {
  const encoder = new TextEncoder()

  const customReadable = new ReadableStream({
    start(controller) {
      let count = 0

      const interval = setInterval(() => {
        const data = `data: ${JSON.stringify({ count: count++, timestamp: Date.now() })}\n\n`
        controller.enqueue(encoder.encode(data))
      }, 1000)

      // Clean up on client disconnect to prevent memory leaks
      request.signal.addEventListener('abort', () => {
        clearInterval(interval)
        controller.close()
      })
    },
    cancel() {
      // Stream was cancelled by the consumer
    },
  })

  return new Response(customReadable, {
    headers: {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache, no-transform',
      'Connection': 'keep-alive',
      'X-Accel-Buffering': 'no', // Disable Nginx buffering
    },
  })
}
```

```tsx
// hooks/use-sse.ts — Client-side SSE hook with reconnection
'use client'

import { useEffect, useState, useRef } from 'react'

export function useSSE<T>(url: string) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const retryCount = useRef(0)

  useEffect(() => {
    let eventSource: EventSource

    function connect() {
      eventSource = new EventSource(url)

      eventSource.onmessage = (event) => {
        setData(JSON.parse(event.data))
        retryCount.current = 0 // Reset on success
      }

      eventSource.onerror = () => {
        eventSource.close()
        // Exponential backoff reconnection
        const delay = Math.min(1000 * 2 ** retryCount.current, 30000)
        retryCount.current++
        setTimeout(connect, delay)
      }
    }

    connect()

    return () => {
      eventSource?.close()
    }
  }, [url])

  return { data, error }
}
```

---

### Prompt 6.4: Caching Strategy

```text
Implement advanced caching patterns.

⚠️ Note: `unstable_cache` may be renamed or replaced by `cacheLife`/`cacheTag` in future
Next.js versions. Check Phase 0.7 compatibility table for your version.
```

```typescript
// lib/cache.ts
import { unstable_cache } from 'next/cache'
import { db } from '@/lib/db'

// Cache function output — revalidates every 60s or on demand via tag
// ⚠️ unstable_cache API may change — see Phase 0.7 compat table
export const getCachedPosts = unstable_cache(
  async (userId: string) => {
    return db.query.posts.findMany({ where: { userId } })
  },
  ['user-posts'],
  {
    revalidate: 60,
    tags: ['posts'],
  }
)
```

```typescript
// app/actions/posts.ts — Cache Invalidation
'use server'

import { revalidateTag } from 'next/cache'
import { db } from '@/lib/db'

export async function createPost(formData: FormData) {
  await db.insert(posts).values({ /* ... */ })

  // Purge cache for all getCachedPosts consumers
  revalidateTag('posts')
}
```

---

### Prompt 6.5: Email System (Resend + React Email)

```text
Implement a modern transactional email system.
```

```tsx
// emails/welcome.tsx
import { Html, Button, Tailwind, Text, Heading, Section } from '@react-email/components'

interface WelcomeEmailProps {
  name: string
  url: string
}

export default function WelcomeEmail({ name, url }: WelcomeEmailProps) {
  return (
    <Html>
      <Tailwind>
        <Section className="bg-white p-10 rounded-lg">
          <Heading className="text-2xl font-bold">Welcome, {name}!</Heading>
          <Text className="text-gray-600">
            Thanks for signing up. Click below to get started.
          </Text>
          <Button
            href={url}
            className="bg-blue-500 text-white px-5 py-3 rounded font-medium"
          >
            Get Started
          </Button>
        </Section>
      </Tailwind>
    </Html>
  )
}
```

```typescript
// lib/email.ts
import { Resend } from 'resend'
import WelcomeEmail from '@/emails/welcome'

const resend = new Resend(process.env.RESEND_API_KEY)

export async function sendWelcomeEmail(email: string, name: string, url: string) {
  const { data, error } = await resend.emails.send({
    from: 'App <noreply@yourdomain.com>',
    to: email,
    subject: 'Welcome to our App',
    react: WelcomeEmail({ name, url }),
  })

  if (error) {
    console.error('Failed to send email:', error)
    throw new Error(`Email send failed: ${error.message}`)
  }

  return data
}
```

---

### Prompt 6.6: Background Jobs

```text
Implement background job processing for tasks that shouldn't block the response.
```

#### Option A: `after()` API (Vercel — check Phase 0.7 compat)

```typescript
// app/actions/order.ts
'use server'

import { after } from 'next/server'
import { sendOrderConfirmation } from '@/lib/email'
import { trackEvent } from '@/lib/analytics'

export async function placeOrder(formData: FormData) {
  const order = await db.order.create({ data: { /* ... */ } })

  // These run AFTER the response is sent to the client
  after(async () => {
    await sendOrderConfirmation(order)
    await trackEvent('order_placed', { orderId: order.id })
    await updateInventory(order.items)
  })

  return { success: true, data: order }
}
```

#### Option B: BullMQ (Self-hosted — for complex job queues)

```typescript
// lib/queue.ts
import { Queue, Worker } from 'bullmq'
import { Redis } from 'ioredis'

const connection = new Redis(process.env.REDIS_URL!)

export const emailQueue = new Queue('emails', { connection })

// Worker (runs in a separate process)
const worker = new Worker('emails', async (job) => {
  switch (job.name) {
    case 'welcome':
      await sendWelcomeEmail(job.data.email, job.data.name, job.data.url)
      break
    case 'reset-password':
      await sendResetPasswordEmail(job.data.email, job.data.token)
      break
  }
}, { connection })

// Usage in Server Action:
await emailQueue.add('welcome', { email, name, url })
```

```text
Implement advanced features using modern integrations (UploadThing, Resend, Convex) and Next.js primitives (unstable_cache, Server Actions, after() API).
```
