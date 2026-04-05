<a name="phase-10"></a>
# 📌 PHASE 10: PERFORMANCE OPTIMIZATION (Performance Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 10.1: Core Web Vitals Optimization

```text
You are a Web Performance Engineer. Optimize Core Web Vitals (CWV) for a Next.js application.

Targets:
- LCP (Largest Contentful Paint): < 2.5s
- INP (Interaction to Next Paint): < 200ms
- CLS (Cumulative Layout Shift): < 0.1

Required:
1. **PPR (Partial Prerendering)**: Mix static shell with dynamic content
2. **Image Optimization**: `next/image` with AVIF/WebP, proper sizing/priority
3. **Font Optimization**: `next/font` for zero layout shift
4. **Third-party scripts**: `next/script` with strategy="lazyOnload" / "worker"
5. **Streaming**: Use loading.tsx + Suspense for instant navigation
6. **Resource hints**: Preload, preconnect for critical resources
```

```tsx
// app/layout.tsx — Font optimization
import { Inter } from 'next/font/google'

const inter = Inter({
  subsets: ['latin'],
  display: 'swap', // Prevents FOIT (Flash of Invisible Text)
  variable: '--font-inter',
})

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={inter.variable}>
      <head>
        {/* Preconnect to critical origins */}
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://cdn.yourapp.com" crossOrigin="anonymous" />
        {/* DNS prefetch for less critical origins */}
        <link rel="dns-prefetch" href="https://api.stripe.com" />
      </head>
      <body>{children}</body>
    </html>
  )
}
```

```tsx
// app/page.tsx — Image optimization
import Image from 'next/image'
import heroImage from '@/public/hero.jpg'

export default function Page() {
  return (
    <main>
      {/* Critical image within LCP area — use priority */}
      <Image
        src={heroImage}
        alt="Hero Image"
        priority // Preloads for better LCP
        sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
        placeholder="blur" // Prevents CLS
      />
    </main>
  )
}
```

#### INP Optimization (Interaction to Next Paint):

```tsx
// components/expensive-list.tsx
'use client'

import { useTransition } from 'react'

export function FilterableList({ items }: { items: Item[] }) {
  const [filter, setFilter] = useState('')
  const [isPending, startTransition] = useTransition()

  function handleFilterChange(value: string) {
    // Mark expensive re-render as non-urgent — keeps input responsive
    startTransition(() => {
      setFilter(value)
    })
  }

  const filteredItems = items.filter(item =>
    item.name.toLowerCase().includes(filter.toLowerCase())
  )

  return (
    <div>
      <input
        type="text"
        onChange={(e) => handleFilterChange(e.target.value)}
        placeholder="Filter..."
      />
      <div className={isPending ? 'opacity-50' : ''}>
        {filteredItems.map(item => (
          <div key={item.id}>{item.name}</div>
        ))}
      </div>
    </div>
  )
}
```

```text
INP Best Practices:
1. Use `startTransition` for expensive state updates (filtering, sorting, tab switching)
2. Debounce input handlers that trigger network requests (300ms)
3. Avoid layout thrashing — batch DOM reads before DOM writes
4. Use CSS `content-visibility: auto` for long lists (skip rendering off-screen items)
5. Virtualize long lists with `@tanstack/react-virtual`
6. React Compiler handles memoization automatically — no manual optimization needed
```

#### Third-Party Script Management:

```tsx
// app/layout.tsx — Partytown for third-party scripts
import Script from 'next/script'

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        {children}

        {/* Analytics — load in web worker via Partytown */}
        <Script
          strategy="worker"
          src="https://www.googletagmanager.com/gtag/js?id=G-XXXXX"
        />

        {/* Non-critical scripts — load after page is interactive */}
        <Script
          strategy="lazyOnload"
          src="https://widget.intercom.io/widget/xxxxx"
        />
      </body>
    </html>
  )
}
```

```text
Partytown Setup:
1. Install: npm install @builder.io/partytown
2. Copy Partytown files to public: npx @builder.io/partytown copylib public/~partytown
3. Use strategy="worker" on Script components
4. Add to next.config.ts:
   experimental: { nextScriptWorkers: true }
```

---

### Prompt 10.2: Backend Performance (Database & API)

```text
Optimize backend performance using caching and efficient queries.

1. **Request Memoization**: React `cache()` for same-request deduplication
2. **Data Cache**: `unstable_cache` for cross-request caching
3. **Database Indexing**: Add indexes for frequently filtered/sorted columns
4. **Connection Pooling**: Use PgBouncer / Neon pooler / Supabase pooler
5. **Edge Caching**: Cache-Control headers for CDN caching

⚠️ Note: `unstable_cache` may be renamed — see Phase 0.7 compat table.
```

```typescript
// lib/data.ts
import { cache } from 'react'
import { unstable_cache } from 'next/cache'
import { db } from '@/lib/db'

// Request-level cache (deduplication within a single request)
export const getUser = cache(async (id: string) => {
  return db.user.findUnique({ where: { id } })
})

// Cross-request cache (shared across requests, time-based + tag-based revalidation)
export const getTopPosts = unstable_cache(
  async () => {
    return db.post.findMany({ take: 5, orderBy: { views: 'desc' } })
  },
  ['top-posts'],
  { revalidate: 3600, tags: ['posts'] }
)
```

```typescript
// app/api/public-data/route.ts — CDN caching with stale-while-revalidate
export async function GET() {
  const data = await getPublicData()

  return Response.json(data, {
    headers: {
      // CDN caches for 60s, serves stale for 5min while revalidating
      'Cache-Control': 'public, s-maxage=60, stale-while-revalidate=300',
    },
  })
}
```

#### Database Query Optimization:

```sql
-- Analyze slow queries with EXPLAIN ANALYZE
EXPLAIN ANALYZE SELECT * FROM posts WHERE user_id = 'xxx' ORDER BY created_at DESC LIMIT 10;

-- Add composite index for common query patterns
CREATE INDEX idx_posts_user_created ON posts (user_id, created_at DESC);

-- Partial index for active records only
CREATE INDEX idx_posts_active ON posts (user_id) WHERE deleted_at IS NULL;
```

```typescript
// prisma/schema.prisma — Add indexes for performance
model Post {
  id        String   @id @default(cuid())
  title     String
  userId    String
  status    Status   @default(DRAFT)
  createdAt DateTime @default(now())
  deletedAt DateTime?

  user User @relation(fields: [userId], references: [id])

  // Composite index for user's posts sorted by date
  @@index([userId, createdAt(sort: Desc)])
  // Partial-like index for status queries
  @@index([status, createdAt(sort: Desc)])
}
```

---

### Prompt 10.3: Bundle Optimization

```text
Analyze and reduce JavaScript bundle size.

1. **Lazy Loading**: `next/dynamic` for heavy client components
2. **Library Replacement**: Replace heavy libs (Moment.js → date-fns, Lodash → native)
3. **Tree Shaking**: Named imports only (`import { format } from 'date-fns'`)
4. **Code Splitting**: Automatic per page/layout in App Router
5. **Bundle Analysis**: Identify large dependencies
```

```tsx
// components/heavy-chart.tsx — Dynamic import for heavy components
'use client'

import dynamic from 'next/dynamic'
import { CardSkeleton } from '@/components/skeletons/card-skeleton'

const Chart = dynamic(() => import('@/components/chart-impl'), {
  ssr: false,
  loading: () => <CardSkeleton />,
})

export function DashboardChart({ data }: { data: ChartData }) {
  return <Chart data={data} />
}
```

#### Bundle Analysis Setup:

```typescript
// next.config.ts
import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  experimental: {
    // Partial Prerendering — static shell + dynamic streaming slots
    // ⚠️ Next.js 14: experimental flag required
    // ⚠️ Next.js 15: opt-in per layout/page via `export const experimental_ppr = true`
    // ⚠️ Next.js 16+: stable, `ppr: true` enables globally
    ppr: true,

    // React Compiler — automatic memoization (replaces useMemo/useCallback)
    // Requires: babel-plugin-react-compiler or Rust transform (Next.js 16+)
    reactCompiler: true,

    // after() API — run work after response is sent (Next.js 15+)
    after: true,

    // Partytown — offload third-party scripts to Web Worker
    nextScriptWorkers: true,
  },

  // Opt-in to AVIF image format (slower build, smaller files)
  images: {
    formats: ['image/avif', 'image/webp'],
  },
}

// Wrap with bundle analyzer when ANALYZE=true
export default process.env.ANALYZE === 'true'
  ? (async () => {
      const withBundleAnalyzer = (await import('@next/bundle-analyzer')).default({
        enabled: true,
      })
      return withBundleAnalyzer(nextConfig)
    })()
  : nextConfig
```

```bash
# Run analysis
ANALYZE=true npm run build
```

```text
Common bundle wins:
- Replace `moment` (300KB) with `date-fns` (tree-shakeable) or `dayjs` (2KB)
- Replace `lodash` (70KB) with native Array/Object methods or `es-toolkit`
- Use `import { specific }` not `import *` for tree shaking
- Check for duplicate React versions: `npm ls react`
- Move large client libraries to dynamic imports
- Use Server Components for data display (zero client JS)

Target: First Load JS < 100KB per route
```

#### React Profiler for Debugging Renders:

```tsx
// Wrap components to profile render performance in development
import { Profiler } from 'react'

function onRender(
  id: string,
  phase: 'mount' | 'update',
  actualDuration: number
) {
  if (actualDuration > 16) { // Longer than one frame (60fps)
    console.warn(`Slow render: ${id} took ${actualDuration.toFixed(1)}ms (${phase})`)
  }
}

export function ProfiledComponent({ children }: { children: React.ReactNode }) {
  return (
    <Profiler id="dashboard" onRender={onRender}>
      {children}
    </Profiler>
  )
}
```

```text
Implement full performance strategy targeting 90+ Lighthouse score across all categories.
```
