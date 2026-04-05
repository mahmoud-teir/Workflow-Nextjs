<a name="phase-18"></a>📌 PHASE 18: ANALYTICS & FEATURE FLAGS (Product Engineer)

> **Next.js Version:** This phase uses Next.js (latest stable). See Phase 0, Prompt 0.7 for the version compatibility table.

### Prompt 18.1: Analytics & Event Tracking

You are a Product Engineer. Implementing product analytics in a Next.js application.

**Option A: PostHog** (Open Source, Self-hostable, Feature Complete)
**Option B: Plausible** (Privacy-first, No cookies, GDPR-compliant by default)
**Option C: Umami** (Open Source, Lightweight, Self-hostable)

Features (Option A — PostHog):
1. Page View Tracking (next/navigation)
2. Custom Event Tracking (Server Action & Client Component)
3. Session Recording (Debug UI issues)
4. User Identification (Auth integration)
5. Consent-aware initialization

---

#### Option A: PostHog (Full-featured)

```typescript
// lib/posthog-server.ts — Singleton server client
import { PostHog } from 'posthog-node'

const globalForPostHog = globalThis as unknown as { posthog: PostHog | undefined }

export const posthogServer =
  globalForPostHog.posthog ??
  new PostHog(process.env.NEXT_PUBLIC_POSTHOG_KEY!, {
    host: process.env.NEXT_PUBLIC_POSTHOG_HOST,
    flushAt: 1,
    flushInterval: 0,
  })

if (process.env.NODE_ENV !== 'production') {
  globalForPostHog.posthog = posthogServer
}
```

```tsx
// app/providers.tsx
'use client'

import posthog from 'posthog-js'
import { PostHogProvider as PHProvider, usePostHog } from 'posthog-js/react'
import { useEffect, useState } from 'react'
import { usePathname, useSearchParams } from 'next/navigation'

function PostHogInit() {
  const [initialized, setInitialized] = useState(false)

  useEffect(() => {
    // Only initialize if user has given consent
    const consent = document.cookie
      .split('; ')
      .find((c) => c.startsWith('analytics_consent='))
      ?.split('=')[1]

    if (consent === 'granted' && !initialized) {
      posthog.init(process.env.NEXT_PUBLIC_POSTHOG_KEY!, {
        api_host: process.env.NEXT_PUBLIC_POSTHOG_HOST,
        person_profiles: 'identified_only',
        capture_pageview: false, // We capture manually for SPA navigation
        capture_pageleave: true,
        persistence: 'localStorage+cookie',
        loaded: (ph) => {
          if (process.env.NODE_ENV === 'development') ph.debug()
        },
      })
      setInitialized(true)
    } else if (consent === 'denied') {
      posthog.opt_out_capturing()
    }
  }, [initialized])

  return null
}

function PostHogPageView() {
  const pathname = usePathname()
  const searchParams = useSearchParams()
  const ph = usePostHog()

  useEffect(() => {
    if (pathname && ph) {
      let url = window.origin + pathname
      if (searchParams.toString()) {
        url += '?' + searchParams.toString()
      }
      ph.capture('$pageview', { $current_url: url })
    }
  }, [pathname, searchParams, ph])

  return null
}

export function AnalyticsProvider({ children }: { children: React.ReactNode }) {
  return (
    <PHProvider client={posthog}>
      <PostHogInit />
      <PostHogPageView />
      {children}
    </PHProvider>
  )
}
```

```tsx
// app/layout.tsx
import { AnalyticsProvider } from './providers'
import { ConsentBanner } from '@/components/consent-banner'

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <AnalyticsProvider>
          {children}
          <ConsentBanner />
        </AnalyticsProvider>
      </body>
    </html>
  )
}
```

```typescript
// lib/analytics-events.ts — Custom event helpers with typed taxonomy
type EventMap = {
  signup_started: { method: 'email' | 'google' | 'github' }
  signup_completed: { method: 'email' | 'google' | 'github'; referral?: string }
  feature_used: { feature: string; context?: string }
  subscription_changed: { plan: string; action: 'upgrade' | 'downgrade' | 'cancel' }
  search_performed: { query: string; results_count: number }
  error_encountered: { error_type: string; page: string }
}

export function trackEvent<K extends keyof EventMap>(event: K, properties: EventMap[K]) {
  if (typeof window !== 'undefined') {
    const posthog = (await import('posthog-js')).default
    posthog.capture(event, properties)
  }
}
```

```typescript
// Server-side event tracking from Server Actions
// app/actions/signup.ts
'use server'

import { posthogServer } from '@/lib/posthog-server'

export async function signupAction(formData: FormData) {
  // ... create user logic ...

  // Track server-side (useful when client JS might not execute)
  posthogServer.capture({
    distinctId: newUser.id,
    event: 'signup_completed',
    properties: {
      method: 'email',
      $set: { email: newUser.email, name: newUser.name },
    },
  })

  // Ensure event is sent before response
  await posthogServer.flush()

  return { success: true }
}
```

#### Option B: Plausible (Privacy-first)

```tsx
// app/layout.tsx — Plausible (no cookies, GDPR-compliant without consent banner)
import Script from 'next/script'

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <Script
          defer
          data-domain="myapp.com"
          src="https://plausible.io/js/script.js"
          strategy="afterInteractive"
        />
      </head>
      <body>{children}</body>
    </html>
  )
}
```

```typescript
// lib/plausible.ts — Custom event tracking
declare global {
  interface Window {
    plausible: (event: string, options?: { props?: Record<string, string | number> }) => void
  }
}

export function trackEvent(name: string, props?: Record<string, string | number>) {
  if (typeof window !== 'undefined' && window.plausible) {
    window.plausible(name, { props })
  }
}

// Usage: trackEvent('Signup', { method: 'google' })
```

#### Option C: Umami (Self-hosted, Lightweight)

```tsx
// app/layout.tsx — Umami
import Script from 'next/script'

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <Script
          defer
          src="https://analytics.myapp.com/script.js"
          data-website-id={process.env.NEXT_PUBLIC_UMAMI_WEBSITE_ID}
          strategy="afterInteractive"
        />
      </head>
      <body>{children}</body>
    </html>
  )
}
```

```typescript
// lib/umami.ts — Custom event tracking
export function trackEvent(name: string, data?: Record<string, string | number>) {
  if (typeof window !== 'undefined' && (window as any).umami) {
    ;(window as any).umami.track(name, data)
  }
}
```

---

### Prompt 18.2: Consent Management (GDPR/CCPA)

> ⚠️ **Required before enabling analytics** (except cookie-free solutions like Plausible).
> If using PostHog, Mixpanel, Google Analytics, or any tool that sets cookies or collects PII, you must obtain consent first.

```tsx
// components/consent-banner.tsx
'use client'

import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import posthog from 'posthog-js'

type ConsentStatus = 'granted' | 'denied' | 'pending'

function getConsent(): ConsentStatus {
  if (typeof document === 'undefined') return 'pending'
  const cookie = document.cookie
    .split('; ')
    .find((c) => c.startsWith('analytics_consent='))
    ?.split('=')[1]
  return (cookie as ConsentStatus) || 'pending'
}

function setConsent(status: 'granted' | 'denied') {
  // Set cookie with 1-year expiry
  const expires = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toUTCString()
  document.cookie = `analytics_consent=${status}; expires=${expires}; path=/; SameSite=Lax`

  if (status === 'granted') {
    posthog.opt_in_capturing()
  } else {
    posthog.opt_out_capturing()
    // Clear existing tracking cookies
    document.cookie.split(';').forEach((c) => {
      const name = c.trim().split('=')[0]
      if (name.startsWith('ph_') || name.startsWith('_ga')) {
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`
      }
    })
  }
}

export function ConsentBanner() {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    setVisible(getConsent() === 'pending')
  }, [])

  if (!visible) return null

  return (
    <div className="fixed bottom-0 left-0 right-0 z-50 border-t bg-background p-4 shadow-lg">
      <div className="mx-auto flex max-w-4xl flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex-1">
          <p className="text-sm font-medium">We use cookies to improve your experience</p>
          <p className="text-xs text-muted-foreground">
            We use analytics to understand how you use our app and improve it.{' '}
            <a href="/privacy" className="underline">Privacy Policy</a>
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              setConsent('denied')
              setVisible(false)
            }}
          >
            Decline
          </Button>
          <Button
            size="sm"
            onClick={() => {
              setConsent('granted')
              setVisible(false)
              window.location.reload() // Re-init analytics
            }}
          >
            Accept
          </Button>
        </div>
      </div>
    </div>
  )
}
```

```tsx
// app/settings/privacy/page.tsx — Allow users to change consent later
'use client'

import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'

export default function PrivacySettings() {
  const [consent, setConsentState] = useState<string>('pending')

  useEffect(() => {
    const cookie = document.cookie
      .split('; ')
      .find((c) => c.startsWith('analytics_consent='))
      ?.split('=')[1]
    setConsentState(cookie || 'pending')
  }, [])

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold">Privacy Settings</h2>
      <p className="text-sm text-muted-foreground">
        Current status: <strong>{consent === 'granted' ? 'Analytics enabled' : 'Analytics disabled'}</strong>
      </p>
      <div className="flex gap-2">
        <Button
          variant={consent === 'granted' ? 'default' : 'outline'}
          onClick={() => {
            document.cookie = `analytics_consent=granted; max-age=${365 * 86400}; path=/; SameSite=Lax`
            setConsentState('granted')
            window.location.reload()
          }}
        >
          Enable Analytics
        </Button>
        <Button
          variant={consent === 'denied' ? 'default' : 'outline'}
          onClick={() => {
            document.cookie = `analytics_consent=denied; max-age=${365 * 86400}; path=/; SameSite=Lax`
            setConsentState('denied')
            window.location.reload()
          }}
        >
          Disable Analytics
        </Button>
      </div>
    </div>
  )
}
```

---

### Prompt 18.3: Feature Flags (PostHog / Vercel Edge Config)

Implement Feature Flags for safe deployment and A/B testing.

**Option A: PostHog** (server-side evaluation)
**Option B: Vercel Edge Config** (ultra-low latency at edge)

Strategy: **Server-side Evaluation** (Prevent flash of wrong content)

#### Option A: PostHog Feature Flags

```tsx
// app/dashboard/page.tsx
import { cookies } from 'next/headers'
import { posthogServer } from '@/lib/posthog-server'

export default async function Dashboard() {
  const cookieStore = await cookies()
  const distinctId = cookieStore.get('ph_distinct_id')?.value || 'anonymous'

  const flags = await posthogServer.getAllFlags(distinctId)

  if (flags['new-dashboard-ui']) {
    return <NewDashboard />
  }

  return <OldDashboard />
}
```

```typescript
// lib/feature-flags.ts — Type-safe flag helper
import { posthogServer } from '@/lib/posthog-server'
import { cookies } from 'next/headers'

type FeatureFlags = {
  'new-dashboard-ui': boolean
  'pricing-v2': boolean
  'ai-chat-enabled': boolean
  'signup-button-color': 'control' | 'test-green' | 'test-orange'
}

export async function getFlag<K extends keyof FeatureFlags>(
  flag: K
): Promise<FeatureFlags[K]> {
  const cookieStore = await cookies()
  const distinctId = cookieStore.get('ph_distinct_id')?.value || 'anonymous'
  const value = await posthogServer.getFeatureFlag(flag, distinctId)
  return value as FeatureFlags[K]
}

export async function getAllFlags(): Promise<Partial<FeatureFlags>> {
  const cookieStore = await cookies()
  const distinctId = cookieStore.get('ph_distinct_id')?.value || 'anonymous'
  return posthogServer.getAllFlags(distinctId) as Promise<Partial<FeatureFlags>>
}
```

#### Option B: Vercel Edge Config

```typescript
// lib/edge-flags.ts
import { createClient } from '@vercel/edge-config'

const edgeConfig = createClient(process.env.EDGE_CONFIG!)

export async function getEdgeFlag(flag: string): Promise<boolean> {
  return (await edgeConfig.get<boolean>(flag)) ?? false
}

export async function getEdgeFlagVariant(flag: string): Promise<string> {
  return (await edgeConfig.get<string>(flag)) ?? 'control'
}
```

#### Gradual Rollout Pattern

```typescript
// lib/gradual-rollout.ts — Percentage-based rollout without external service
import { createHash } from 'crypto'

export function isInRollout(userId: string, feature: string, percentage: number): boolean {
  // Deterministic hash ensures same user always gets same result
  const hash = createHash('md5').update(`${feature}:${userId}`).digest('hex')
  const value = parseInt(hash.slice(0, 8), 16) / 0xffffffff
  return value < percentage / 100
}

// Usage in Server Component:
// if (isInRollout(user.id, 'new-checkout', 25)) { ... } // 25% rollout
```

```typescript
// Gradual rollout schedule example:
// Day 1: 5% (internal team + early adopters)
// Day 3: 25% (if no errors spike)
// Day 7: 50%
// Day 10: 100% (full rollout)
// Day 14: Remove flag, clean up code
```

---

### Prompt 18.4: A/B Testing Experiments

Run an A/B test on a specific feature with proper conversion tracking.

Experiment: **"Sign Up Button Color"** (Control: Blue vs Test: Green)
Metric: **Conversion Rate** (Button Click → Registration Completed)

1. Define Experiment in PostHog Dashboard (or code)
2. Implement Variant Logic (Client Component)
3. Track Goal Event (Conversion)
4. Analyze results with statistical significance

```tsx
// components/signup-button.tsx
'use client'

import { useFeatureFlagVariantKey, usePostHog } from 'posthog-js/react'
import { useRouter } from 'next/navigation'

export function SignUpButton() {
  const variant = useFeatureFlagVariantKey('signup-button-color')
  const posthog = usePostHog()
  const router = useRouter()

  // Variant mapping
  const styles: Record<string, string> = {
    control: 'bg-blue-500 hover:bg-blue-600',
    'test-green': 'bg-green-500 hover:bg-green-600',
    'test-orange': 'bg-orange-500 hover:bg-orange-600',
  }

  const buttonStyle = styles[variant || 'control'] || styles.control

  return (
    <button
      className={`${buttonStyle} rounded-lg px-6 py-3 font-semibold text-white transition-colors`}
      onClick={() => {
        // Track the click event with variant info
        posthog.capture('signup_button_clicked', {
          variant: variant || 'control',
        })
        router.push('/signup')
      }}
    >
      Sign Up Free
    </button>
  )
}
```

```typescript
// Track conversion in signup Server Action
// app/actions/auth.ts
'use server'

import { posthogServer } from '@/lib/posthog-server'

export async function completeSignup(formData: FormData) {
  // ... create user logic ...

  // Track conversion event — this is what the A/B test measures
  posthogServer.capture({
    distinctId: newUser.id,
    event: 'signup_completed',
    properties: {
      method: 'email',
      // PostHog automatically correlates this with the feature flag variant
    },
  })

  await posthogServer.flush()
  return { success: true }
}
```

---

### Prompt 18.5: Custom Event Taxonomy Design

> Design your event taxonomy BEFORE implementing tracking. A consistent naming convention prevents analytics debt.

#### Naming Convention

```
[object]_[action] — snake_case, past tense for completed actions
```

| Category | Event Name | Properties |
|----------|-----------|------------|
| **Auth** | `signup_started` | `{ method: 'email' \| 'google' \| 'github' }` |
| **Auth** | `signup_completed` | `{ method, referral_source? }` |
| **Auth** | `login_completed` | `{ method, is_returning: boolean }` |
| **Navigation** | `page_viewed` | `{ path, referrer? }` |
| **Feature** | `feature_used` | `{ feature_name, context? }` |
| **Search** | `search_performed` | `{ query, results_count, filters? }` |
| **Commerce** | `checkout_started` | `{ plan, price, currency }` |
| **Commerce** | `subscription_changed` | `{ from_plan, to_plan, action }` |
| **Error** | `error_encountered` | `{ error_type, page, message? }` |
| **Engagement** | `feedback_submitted` | `{ rating, category? }` |

#### Rules
1. **Use `object_action`** not `action_object` (`signup_completed` not `completed_signup`)
2. **Past tense for completed actions**, present for ongoing (`file_uploaded` not `file_upload`)
3. **Keep properties flat** — avoid nested objects
4. **Never include PII** in event properties (no emails, names, or IPs)
5. **Use consistent types** — booleans for flags, strings for categories, numbers for counts
6. **Document every event** in a shared spreadsheet or Notion table before implementation

```typescript
// lib/analytics-schema.ts — Runtime validation for events (optional, dev only)
import { z } from 'zod'

const eventSchemas = {
  signup_started: z.object({ method: z.enum(['email', 'google', 'github']) }),
  signup_completed: z.object({ method: z.enum(['email', 'google', 'github']), referral_source: z.string().optional() }),
  feature_used: z.object({ feature_name: z.string(), context: z.string().optional() }),
  search_performed: z.object({ query: z.string(), results_count: z.number() }),
  checkout_started: z.object({ plan: z.string(), price: z.number(), currency: z.string() }),
} as const

export function validateEvent<K extends keyof typeof eventSchemas>(
  event: K,
  properties: unknown
) {
  if (process.env.NODE_ENV === 'development') {
    const schema = eventSchemas[event]
    const result = schema.safeParse(properties)
    if (!result.success) {
      console.warn(`[Analytics] Invalid properties for "${event}":`, result.error.flatten())
    }
  }
}
```

Implement data-driven development workflow with consent-aware analytics, safe feature rollout, statistically valid A/B testing, and a consistent event taxonomy.
