<a name="phase-5"></a>
# 📌 PHASE 5: FRONTEND DEVELOPMENT (Frontend Developer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 5.1: UI Components & Design System

```text
You are a Senior Frontend Developer. Set up the UI component system for a Next.js application with React 19.

Tech Stack:
- React 19 (Server Components by default)
- Tailwind CSS v4 (CSS-based config with @theme directive)
- Shadcn/ui (latest — composable primitives based on Radix UI)
- Framer Motion (animations)
- Lucide React (icons)
- Sonner (toast notifications)

Required:
1. Tailwind CSS v4 setup with CSS-based theming
2. Shadcn/ui integration with custom theme
3. Dark mode toggle (next-themes — class-based strategy)
4. Responsive design (mobile-first)
5. Accessible components (WCAG 2.2)
6. React Compiler compatibility (no manual memoization needed)
7. Skeleton components for loading states
```

```json
// components.json — shadcn/ui configuration
{
  "$schema": "https://ui.shadcn.com/schema.json",
  "style": "new-york",
  "rsc": true,
  "tsx": true,
  "tailwind": {
    "config": "",
    "css": "app/globals.css",
    "baseColor": "neutral",
    "cssVariables": true,
    "prefix": ""
  },
  "aliases": {
    "components": "@/components",
    "utils": "@/lib/utils",
    "ui": "@/components/ui",
    "lib": "@/lib",
    "hooks": "@/hooks"
  },
  "iconLibrary": "lucide"
}
```

```css
/* app/globals.css — Tailwind CSS v4 Configuration */
@import "tailwindcss";
@import "tw-animate-css";

/* Custom theme using @theme directive (Tailwind v4) */
@theme {
  --color-primary: oklch(0.6 0.25 265);
  --color-primary-foreground: oklch(0.98 0 0);
  --color-secondary: oklch(0.7 0.15 180);
  --color-secondary-foreground: oklch(0.1 0 0);
  --color-accent: oklch(0.75 0.2 50);
  --color-destructive: oklch(0.55 0.25 25);
  --color-muted: oklch(0.95 0.01 265);
  --color-muted-foreground: oklch(0.5 0.02 265);
  --color-background: oklch(0.99 0 0);
  --color-foreground: oklch(0.1 0 0);
  --color-card: oklch(0.99 0 0);
  --color-border: oklch(0.9 0.01 265);
  --color-input: oklch(0.9 0.01 265);
  --color-ring: oklch(0.6 0.25 265);

  --radius-sm: 0.25rem;
  --radius-md: 0.5rem;
  --radius-lg: 0.75rem;
  --radius-xl: 1rem;

  --font-sans: 'Inter Variable', system-ui, sans-serif;
  --font-mono: 'JetBrains Mono Variable', monospace;

  --animate-fade-in: fade-in 0.3s ease-out;
  --animate-slide-up: slide-up 0.3s ease-out;
}

/* Dark mode — class-based for next-themes compatibility */
.dark {
  --color-background: oklch(0.15 0.01 265);
  --color-foreground: oklch(0.95 0 0);
  --color-card: oklch(0.18 0.01 265);
  --color-border: oklch(0.3 0.02 265);
  --color-muted: oklch(0.25 0.01 265);
  --color-muted-foreground: oklch(0.65 0.02 265);
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Respect reduced motion preference */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

#### Server vs Client Component Strategy:

```
Server Components (default — no directive needed):
├── Page components (app/**/page.tsx)
├── Layout components (app/**/layout.tsx)
├── Data display components
├── Static content
└── SEO-critical content

Client Components ('use client'):
├── Interactive forms (with useActionState)
├── Event handlers (onClick, onChange)
├── Browser API usage (localStorage, window)
├── React hooks (useState, useEffect, useOptimistic)
├── Third-party client libraries
├── Animations (Framer Motion)
└── Theme toggle (next-themes)
```

> **React Compiler Note:** In Next.js with React Compiler enabled, you do NOT need `useMemo`, `useCallback`, or `React.memo`. The compiler handles memoization automatically. Remove manual memoization from existing code.

#### Skeleton Components for Loading States:

```tsx
// components/ui/skeleton.tsx
export function Skeleton({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn('animate-pulse rounded-md bg-muted', className)}
      {...props}
    />
  )
}

// components/skeletons/card-skeleton.tsx
export function CardSkeleton() {
  return (
    <div className="rounded-lg border p-4 space-y-3">
      <Skeleton className="h-4 w-3/4" />
      <Skeleton className="h-4 w-1/2" />
      <Skeleton className="h-20 w-full" />
    </div>
  )
}
```

#### Toast Notifications (Sonner):

```tsx
// app/layout.tsx — Add Toaster to root layout
import { Toaster } from 'sonner'

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body>
        {children}
        <Toaster richColors position="bottom-right" />
      </body>
    </html>
  )
}

// Usage anywhere in a Client Component:
import { toast } from 'sonner'
toast.success('Post published!')
toast.error('Something went wrong')
toast.promise(saveData(), {
  loading: 'Saving...',
  success: 'Saved!',
  error: 'Failed to save',
})
```

---

### Prompt 5.2: Data Fetching Patterns (React 19 + Next.js)

```text
Implement modern data fetching patterns.
```

#### Server Component Data Fetching (Primary Pattern):

```tsx
// app/users/page.tsx — Server Component (default)
import { Suspense } from 'react'
import { getUsers } from '@/lib/services/user-service'
import { UserList } from '@/components/user-list'
import { CardSkeleton } from '@/components/skeletons/card-skeleton'

// SEO metadata
export async function generateMetadata() {
  return {
    title: 'Users',
    description: 'Manage your users',
  }
}

// Note: searchParams is a Promise in Next.js 15+ (must await)
export default async function UsersPage({
  searchParams,
}: {
  searchParams: Promise<{ page?: string; search?: string }>
}) {
  return (
    <div>
      <h1>Users</h1>
      <Suspense fallback={<CardSkeleton />}>
        <UserListWrapper searchParams={searchParams} />
      </Suspense>
    </div>
  )
}

// Async component (streaming)
async function UserListWrapper({
  searchParams,
}: {
  searchParams: Promise<{ page?: string; search?: string }>
}) {
  const params = await searchParams
  const page = Number(params.page) || 1
  const search = params.search || ''

  const users = await getUsers(page, 10, search)
  return <UserList users={users} />
}
```

#### Client-Side Data Fetching with TanStack Query v5:

```tsx
// hooks/use-users.ts
'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'

export function useUsers(page = 1) {
  return useQuery({
    queryKey: ['users', page],
    queryFn: () => fetch(`/api/users?page=${page}`).then(res => res.json()),
    staleTime: 60_000,
  })
}

export function useCreateUser() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: CreateUserInput) =>
      fetch('/api/users', {
        method: 'POST',
        body: JSON.stringify(data),
      }).then(res => res.json()),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
    },
  })
}
```

#### React 19 `use()` Hook for Promises:

```tsx
// components/user-profile.tsx
'use client'

import { use } from 'react'

export function UserProfile({ userPromise }: { userPromise: Promise<User> }) {
  // use() unwraps the promise (replaces useEffect + useState pattern)
  const user = use(userPromise)

  return (
    <div>
      <h2>{user.name}</h2>
      <p>{user.email}</p>
    </div>
  )
}
```

---

### Prompt 5.3: Form Handling with React 19

```text
Implement form handling using React 19 primitives + Server Actions.
```

#### Option A: Native React 19 Forms (Recommended for simple forms):

```tsx
// components/forms/contact-form.tsx
'use client'

import { useActionState } from 'react'
import { submitContact } from '@/app/actions/contact'

export function ContactForm() {
  const [state, formAction, isPending] = useActionState(submitContact, null)

  return (
    <form action={formAction}>
      <input name="name" required placeholder="Your name" />
      <input name="email" type="email" required placeholder="Your email" />
      <textarea name="message" required placeholder="Your message" />

      <button type="submit" disabled={isPending}>
        {isPending ? 'Sending...' : 'Send Message'}
      </button>

      {state?.success && <p className="text-green-500">Message sent!</p>}
      {state?.error && <p className="text-destructive">{state.error}</p>}
    </form>
  )
}
```

#### Option B: React Hook Form + Zod (Recommended for complex forms):

```tsx
// components/forms/registration-form.tsx
'use client'

import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { registerSchema, type RegisterInput } from '@/lib/validations/auth'
import { registerUser } from '@/app/actions/auth'
import { useActionState } from 'react'

export function RegistrationForm() {
  const {
    register: field,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterInput>({
    resolver: zodResolver(registerSchema),
  })

  const [state, action, isPending] = useActionState(registerUser, null)

  return (
    <form action={action}>
      <div>
        <label htmlFor="email">Email</label>
        <input {...field('email')} id="email" />
        {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
      </div>

      <div>
        <label htmlFor="password">Password</label>
        <input {...field('password')} id="password" type="password" />
        {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
      </div>

      <button type="submit" disabled={isPending}>
        {isPending ? 'Registering...' : 'Register'}
      </button>
    </form>
  )
}
```

#### Option C: Conform (Best integration with Server Actions):

```tsx
// components/forms/post-form.tsx
'use client'

import { useForm } from '@conform-to/react'
import { parseWithZod } from '@conform-to/zod'
import { useActionState } from 'react'
import { createPost } from '@/app/actions/post'
import { postSchema } from '@/lib/validations/post'

export function PostForm() {
  const [lastResult, action] = useActionState(createPost, undefined)

  const [form, fields] = useForm({
    lastResult,
    onValidate({ formData }) {
      return parseWithZod(formData, { schema: postSchema })
    },
    shouldValidate: 'onBlur',
    shouldRevalidate: 'onInput',
  })

  return (
    <form id={form.id} onSubmit={form.onSubmit} action={action} noValidate>
      <input
        key={fields.title.key}
        name={fields.title.name}
        defaultValue={fields.title.initialValue}
      />
      {fields.title.errors && <p className="text-sm text-destructive">{fields.title.errors}</p>}

      <button type="submit">Create Post</button>
    </form>
  )
}
```

---

### Prompt 5.4: State Management (React 19 — Less State Needed)

```text
With React 19 + Server Components, you need MUCH less client-side state.
```

**State Management Strategy:**

```
1. Server State → Server Components (async/await — NO state management needed)
2. URL State → useSearchParams / nuqs (type-safe URL state)
3. Form State → useActionState (React 19)
4. Optimistic State → useOptimistic (React 19)
5. Context State → React Context with use() hook (React 19)
6. Global Client State → Zustand (when truly needed)
```

#### Zustand v5 (when you need global client state):

```typescript
// stores/ui-store.ts
import { create } from 'zustand'
import { persist, devtools } from 'zustand/middleware'

interface UIState {
  sidebarOpen: boolean
  theme: 'light' | 'dark' | 'system'
  toggleSidebar: () => void
  setTheme: (theme: UIState['theme']) => void
}

export const useUIStore = create<UIState>()(
  devtools(
    persist(
      (set) => ({
        sidebarOpen: true,
        theme: 'system',
        toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
        setTheme: (theme) => set({ theme }),
      }),
      { name: 'ui-store' }
    )
  )
)
```

#### nuqs — Type-safe URL State (for search, filters, pagination):

```tsx
// components/data-table.tsx
'use client'

import { useQueryState, parseAsInteger, parseAsString } from 'nuqs'

export function DataTable() {
  const [page, setPage] = useQueryState('page', parseAsInteger.withDefault(1))
  const [search, setSearch] = useQueryState('search', parseAsString.withDefault(''))
  const [sort, setSort] = useQueryState('sort', parseAsString.withDefault('createdAt'))

  return (
    <div>
      <input
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search..."
      />
      {/* Table component */}
    </div>
  )
}
```

---

### Prompt 5.5: Advanced UI Patterns

```text
Implement modern Next.js UI patterns.
```

#### Parallel Routes (Split-view layouts):

```
app/
├── @sidebar/
│   └── default.tsx
├── @main/
│   └── default.tsx
└── layout.tsx
```

```tsx
// app/layout.tsx
export default function Layout({
  sidebar,
  main,
}: {
  sidebar: React.ReactNode
  main: React.ReactNode
}) {
  return (
    <div className="flex">
      <aside className="w-64">{sidebar}</aside>
      <main className="flex-1">{main}</main>
    </div>
  )
}
```

#### Intercepting Routes (Modal pattern):

```
app/
├── feed/
│   └── page.tsx          # Feed page
├── @modal/
│   └── (.)photo/[id]/
│       └── page.tsx      # Photo modal (intercepted)
├── photo/[id]/
│   └── page.tsx          # Full photo page (direct navigation)
└── layout.tsx
```

#### Streaming with Suspense:

```tsx
// app/dashboard/page.tsx
import { Suspense } from 'react'
import { CardSkeleton } from '@/components/skeletons/card-skeleton'

export default function Dashboard() {
  return (
    <div className="grid grid-cols-3 gap-4">
      <Suspense fallback={<CardSkeleton />}>
        <RevenueCard />
      </Suspense>
      <Suspense fallback={<CardSkeleton />}>
        <UsersCard />
      </Suspense>
      <Suspense fallback={<CardSkeleton />}>
        <OrdersCard />
      </Suspense>
    </div>
  )
}

// Each card fetches its own data — they load independently via streaming
async function RevenueCard() {
  const revenue = await getRevenue() // Slow fetch doesn't block other cards
  return <Card title="Revenue" value={revenue} />
}
```

#### View Transitions API (Experimental):

```tsx
// components/page-transition.tsx
'use client'

import { useRouter } from 'next/navigation'
import { useTransition } from 'react'

export function LinkWithTransition({
  href,
  children,
}: {
  href: string
  children: React.ReactNode
}) {
  const router = useRouter()
  const [isPending, startTransition] = useTransition()

  function handleClick(e: React.MouseEvent) {
    e.preventDefault()

    // Use View Transitions API if available
    if ('startViewTransition' in document) {
      (document as any).startViewTransition(() => {
        startTransition(() => {
          router.push(href)
        })
      })
    } else {
      startTransition(() => {
        router.push(href)
      })
    }
  }

  return (
    <a href={href} onClick={handleClick} className={isPending ? 'opacity-50' : ''}>
      {children}
    </a>
  )
}
```

```text
Implement comprehensive frontend with React 19 patterns, Tailwind CSS v4, and modern Next.js features.
```
