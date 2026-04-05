<a name="phase-20"></a>📌 PHASE 20: ERROR HANDLING & RESILIENCE (Full-Stack Engineer)

> **Next.js Version:** This phase uses Next.js (latest stable). See Phase 0, Prompt 0.7 for the version compatibility table.

### Prompt 20.1: Unified Error Handling Strategy

You are a Full-Stack Engineer. Implement a comprehensive error handling strategy across the Next.js application using error boundaries, typed errors, and consistent error responses.

#### Error Taxonomy

```typescript
// lib/errors.ts — Typed application errors
export class AppError extends Error {
  constructor(
    message: string,
    public code: string,
    public statusCode: number = 500,
    public isOperational: boolean = true
  ) {
    super(message)
    this.name = 'AppError'
  }
}

export class NotFoundError extends AppError {
  constructor(resource: string, id?: string) {
    super(
      id ? `${resource} with ID "${id}" not found` : `${resource} not found`,
      'NOT_FOUND',
      404
    )
  }
}

export class UnauthorizedError extends AppError {
  constructor(message = 'Authentication required') {
    super(message, 'UNAUTHORIZED', 401)
  }
}

export class ForbiddenError extends AppError {
  constructor(message = 'Insufficient permissions') {
    super(message, 'FORBIDDEN', 403)
  }
}

export class ValidationError extends AppError {
  constructor(
    message: string,
    public fields?: Record<string, string[]>
  ) {
    super(message, 'VALIDATION_ERROR', 400)
  }
}

export class RateLimitError extends AppError {
  constructor(public retryAfter: number) {
    super('Too many requests', 'RATE_LIMITED', 429)
  }
}

export class ExternalServiceError extends AppError {
  constructor(service: string, cause?: Error) {
    super(`External service error: ${service}`, 'EXTERNAL_SERVICE_ERROR', 502)
    this.cause = cause
    this.isOperational = false // Not our fault — may need alerting
  }
}
```

#### Route Segment Error Boundaries

```tsx
// app/error.tsx — Default error boundary for all routes
'use client'

import { useEffect } from 'react'
import { Button } from '@/components/ui/button'
import * as Sentry from '@sentry/nextjs'

export default function ErrorPage({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    Sentry.captureException(error)
  }, [error])

  return (
    <div className="flex min-h-[50vh] flex-col items-center justify-center gap-4">
      <h2 className="text-2xl font-bold">Something went wrong</h2>
      <p className="text-muted-foreground">
        {error.message || 'An unexpected error occurred.'}
      </p>
      {error.digest && (
        <p className="text-xs text-muted-foreground">Error ID: {error.digest}</p>
      )}
      <div className="flex gap-2">
        <Button onClick={reset}>Try again</Button>
        <Button variant="outline" onClick={() => window.location.href = '/'}>
          Go home
        </Button>
      </div>
    </div>
  )
}
```

```tsx
// app/global-error.tsx — Root-level error boundary (catches layout errors)
// MUST include <html> and <body> tags since it replaces the entire page
'use client'

import * as Sentry from '@sentry/nextjs'
import { useEffect } from 'react'

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    Sentry.captureException(error)
  }, [error])

  return (
    <html lang="en">
      <body>
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          fontFamily: 'system-ui, sans-serif',
          gap: '1rem',
        }}>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>
            Something went wrong
          </h1>
          <p style={{ color: '#666' }}>
            A critical error occurred. Please try refreshing the page.
          </p>
          <button
            onClick={reset}
            style={{
              padding: '0.5rem 1rem',
              border: '1px solid #ccc',
              borderRadius: '0.375rem',
              cursor: 'pointer',
            }}
          >
            Try again
          </button>
        </div>
      </body>
    </html>
  )
}
```

```tsx
// app/not-found.tsx — Custom 404 page
import Link from 'next/link'
import { Button } from '@/components/ui/button'

export default function NotFound() {
  return (
    <div className="flex min-h-[50vh] flex-col items-center justify-center gap-4">
      <h1 className="text-6xl font-bold text-muted-foreground">404</h1>
      <h2 className="text-xl font-semibold">Page not found</h2>
      <p className="text-muted-foreground">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <Button asChild>
        <Link href="/">Go home</Link>
      </Button>
    </div>
  )
}
```

#### Server Action Error Handling

```typescript
// lib/action-utils.ts — Consistent Server Action error pattern
// (Referenced from Phase 2 — included here for completeness)

export type ActionResult<T = void> =
  | { success: true; data: T }
  | { success: false; error: string; fieldErrors?: Record<string, string[]> }

export function actionError(error: string, fieldErrors?: Record<string, string[]>): ActionResult<never> {
  return { success: false, error, fieldErrors }
}

export function actionSuccess<T>(data: T): ActionResult<T> {
  return { success: true, data }
}
```

```typescript
// Example Server Action with proper error handling
'use server'

import { z } from 'zod'
import { actionError, actionSuccess, type ActionResult } from '@/lib/action-utils'
import { requireAuth } from '@/lib/action-utils'
import { db } from '@/db'
import { posts } from '@/db/schema'
import { revalidatePath } from 'next/cache'

const createPostSchema = z.object({
  title: z.string().min(1, 'Title is required').max(200),
  content: z.string().min(1, 'Content is required'),
})

export async function createPost(formData: FormData): Promise<ActionResult<{ id: string }>> {
  try {
    const { user } = await requireAuth()

    const parsed = createPostSchema.safeParse({
      title: formData.get('title'),
      content: formData.get('content'),
    })

    if (!parsed.success) {
      return actionError('Validation failed', parsed.error.flatten().fieldErrors)
    }

    const [post] = await db.insert(posts).values({
      ...parsed.data,
      authorId: user.id,
    }).returning({ id: posts.id })

    revalidatePath('/posts')
    return actionSuccess({ id: post.id })
  } catch (error) {
    // Log the real error server-side
    console.error('createPost failed:', error)
    // Return safe message to client
    return actionError('Failed to create post. Please try again.')
  }
}
```

#### API Route Error Responses

```typescript
// lib/api-errors.ts — Consistent JSON error responses
import { NextResponse } from 'next/server'
import { AppError } from '@/lib/errors'
import * as Sentry from '@sentry/nextjs'

interface ApiErrorResponse {
  error: string
  message: string
  statusCode: number
  details?: Record<string, unknown>
}

export function handleApiError(error: unknown): NextResponse<ApiErrorResponse> {
  // Known application errors
  if (error instanceof AppError) {
    if (!error.isOperational) {
      Sentry.captureException(error)
    }
    return NextResponse.json(
      {
        error: error.code,
        message: error.message,
        statusCode: error.statusCode,
      },
      { status: error.statusCode }
    )
  }

  // Zod validation errors
  if (error instanceof z.ZodError) {
    return NextResponse.json(
      {
        error: 'VALIDATION_ERROR',
        message: 'Request validation failed',
        statusCode: 400,
        details: { fields: error.flatten().fieldErrors },
      },
      { status: 400 }
    )
  }

  // Unknown errors — log and return generic message
  Sentry.captureException(error)
  console.error('Unhandled API error:', error)

  return NextResponse.json(
    {
      error: 'INTERNAL_ERROR',
      message: 'An internal error occurred',
      statusCode: 500,
    },
    { status: 500 }
  )
}
```

```typescript
// Usage in API route
import { handleApiError } from '@/lib/api-errors'

export async function GET(request: NextRequest) {
  try {
    // ... handler logic
    return Response.json({ data: result })
  } catch (error) {
    return handleApiError(error)
  }
}
```

---

### Prompt 20.2: Retry & Circuit Breaker Patterns

Implement resilience patterns for external API calls to handle transient failures gracefully.

#### Retry with Exponential Backoff

```typescript
// lib/retry.ts
interface RetryOptions {
  maxAttempts?: number
  baseDelayMs?: number
  maxDelayMs?: number
  retryOn?: (error: unknown) => boolean
  onRetry?: (attempt: number, error: unknown) => void
}

export async function withRetry<T>(
  fn: () => Promise<T>,
  options: RetryOptions = {}
): Promise<T> {
  const {
    maxAttempts = 3,
    baseDelayMs = 1000,
    maxDelayMs = 10000,
    retryOn = isRetryable,
    onRetry,
  } = options

  let lastError: unknown

  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error

      if (attempt === maxAttempts || !retryOn(error)) {
        throw error
      }

      // Exponential backoff with jitter
      const delay = Math.min(
        baseDelayMs * Math.pow(2, attempt - 1) + Math.random() * 1000,
        maxDelayMs
      )

      onRetry?.(attempt, error)
      await new Promise((resolve) => setTimeout(resolve, delay))
    }
  }

  throw lastError
}

function isRetryable(error: unknown): boolean {
  // Retry on network errors and 5xx responses
  if (error instanceof TypeError && error.message.includes('fetch')) return true
  if (error instanceof Response && error.status >= 500) return true
  if (error instanceof Error && 'code' in error) {
    const code = (error as any).code
    return ['ECONNRESET', 'ETIMEDOUT', 'ECONNREFUSED'].includes(code)
  }
  return false
}

// Usage:
// const data = await withRetry(() => fetch('https://api.example.com/data'), {
//   maxAttempts: 3,
//   onRetry: (attempt) => console.log(`Retry attempt ${attempt}`),
// })
```

#### Timeout Wrapper

```typescript
// lib/timeout.ts
export async function withTimeout<T>(
  promise: Promise<T>,
  timeoutMs: number,
  message = 'Operation timed out'
): Promise<T> {
  const controller = new AbortController()

  const timeoutId = setTimeout(() => controller.abort(), timeoutMs)

  try {
    const result = await Promise.race([
      promise,
      new Promise<never>((_, reject) => {
        controller.signal.addEventListener('abort', () => {
          reject(new Error(message))
        })
      }),
    ])
    return result
  } finally {
    clearTimeout(timeoutId)
  }
}

// For fetch specifically:
export async function fetchWithTimeout(
  url: string,
  options: RequestInit & { timeoutMs?: number } = {}
): Promise<Response> {
  const { timeoutMs = 10000, ...fetchOptions } = options
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs)

  try {
    return await fetch(url, {
      ...fetchOptions,
      signal: controller.signal,
    })
  } finally {
    clearTimeout(timeoutId)
  }
}
```

#### Circuit Breaker

```typescript
// lib/circuit-breaker.ts
type CircuitState = 'closed' | 'open' | 'half-open'

interface CircuitBreakerOptions {
  failureThreshold?: number   // Failures before opening
  resetTimeoutMs?: number     // How long to wait before half-open
  monitorWindowMs?: number    // Window to count failures in
}

export class CircuitBreaker {
  private state: CircuitState = 'closed'
  private failures = 0
  private lastFailureTime = 0
  private readonly failureThreshold: number
  private readonly resetTimeoutMs: number

  constructor(
    private name: string,
    options: CircuitBreakerOptions = {}
  ) {
    this.failureThreshold = options.failureThreshold ?? 5
    this.resetTimeoutMs = options.resetTimeoutMs ?? 30000
  }

  async execute<T>(fn: () => Promise<T>): Promise<T> {
    if (this.state === 'open') {
      if (Date.now() - this.lastFailureTime >= this.resetTimeoutMs) {
        this.state = 'half-open'
      } else {
        throw new Error(`Circuit breaker "${this.name}" is open — request blocked`)
      }
    }

    try {
      const result = await fn()
      this.onSuccess()
      return result
    } catch (error) {
      this.onFailure()
      throw error
    }
  }

  private onSuccess() {
    this.failures = 0
    this.state = 'closed'
  }

  private onFailure() {
    this.failures++
    this.lastFailureTime = Date.now()

    if (this.failures >= this.failureThreshold) {
      this.state = 'open'
      console.warn(`Circuit breaker "${this.name}" opened after ${this.failures} failures`)
    }
  }

  getState(): CircuitState {
    return this.state
  }
}

// Usage:
// const paymentCircuit = new CircuitBreaker('stripe', { failureThreshold: 3, resetTimeoutMs: 60000 })
//
// try {
//   const charge = await paymentCircuit.execute(() =>
//     stripe.charges.create({ amount: 1000, currency: 'usd' })
//   )
// } catch (error) {
//   if (error.message.includes('Circuit breaker')) {
//     // Stripe is down — show maintenance message
//   }
// }
```

#### Combining Retry + Circuit Breaker + Timeout

```typescript
// lib/resilient-fetch.ts — Composing all patterns
import { withRetry } from './retry'
import { fetchWithTimeout } from './timeout'
import { CircuitBreaker } from './circuit-breaker'

const circuitBreakers = new Map<string, CircuitBreaker>()

function getCircuitBreaker(name: string): CircuitBreaker {
  if (!circuitBreakers.has(name)) {
    circuitBreakers.set(name, new CircuitBreaker(name))
  }
  return circuitBreakers.get(name)!
}

export async function resilientFetch(
  serviceName: string,
  url: string,
  options?: RequestInit & { timeoutMs?: number; maxRetries?: number }
): Promise<Response> {
  const circuit = getCircuitBreaker(serviceName)
  const { timeoutMs = 10000, maxRetries = 3, ...fetchOptions } = options || {}

  return circuit.execute(() =>
    withRetry(
      () => fetchWithTimeout(url, { ...fetchOptions, timeoutMs }),
      { maxAttempts: maxRetries }
    )
  )
}

// Usage:
// const response = await resilientFetch('github', 'https://api.github.com/users/me', {
//   headers: { Authorization: `Bearer ${token}` },
//   timeoutMs: 5000,
//   maxRetries: 2,
// })
```

---

### Prompt 20.3: Graceful Degradation

Design fallback behaviors when services are unavailable or the user is offline.

#### Offline Detection Hook

```tsx
// hooks/use-online-status.ts
'use client'

import { useSyncExternalStore } from 'react'

function subscribe(callback: () => void) {
  window.addEventListener('online', callback)
  window.addEventListener('offline', callback)
  return () => {
    window.removeEventListener('online', callback)
    window.removeEventListener('offline', callback)
  }
}

function getSnapshot() {
  return navigator.onLine
}

function getServerSnapshot() {
  return true // Assume online during SSR
}

export function useOnlineStatus(): boolean {
  return useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot)
}
```

```tsx
// components/offline-indicator.tsx
'use client'

import { useOnlineStatus } from '@/hooks/use-online-status'

export function OfflineIndicator() {
  const isOnline = useOnlineStatus()

  if (isOnline) return null

  return (
    <div className="fixed bottom-0 left-0 right-0 z-50 bg-yellow-500 px-4 py-2 text-center text-sm font-medium text-yellow-900">
      You're offline. Some features may be unavailable.
    </div>
  )
}
```

#### Feature Degradation Matrix

```typescript
// lib/feature-availability.ts
type Feature = 'search' | 'create' | 'edit' | 'delete' | 'notifications' | 'ai-chat' | 'payments'

type DegradationLevel = 'full' | 'limited' | 'unavailable'

const OFFLINE_FEATURES: Record<Feature, DegradationLevel> = {
  search: 'limited',        // Search cached data only
  create: 'limited',        // Queue for sync when online
  edit: 'limited',          // Queue for sync when online
  delete: 'unavailable',    // Too risky to queue
  notifications: 'unavailable',
  'ai-chat': 'unavailable', // Requires API
  payments: 'unavailable',  // Requires Stripe
}

const DEGRADED_FEATURES: Record<Feature, DegradationLevel> = {
  search: 'full',
  create: 'full',
  edit: 'full',
  delete: 'full',
  notifications: 'limited', // Show cached, don't fetch new
  'ai-chat': 'unavailable', // AI service down
  payments: 'unavailable',  // Stripe down
}

export function getFeatureStatus(
  feature: Feature,
  context: { isOnline: boolean; degradedServices: string[] }
): DegradationLevel {
  if (!context.isOnline) {
    return OFFLINE_FEATURES[feature]
  }

  if (context.degradedServices.length > 0) {
    return DEGRADED_FEATURES[feature]
  }

  return 'full'
}
```

```tsx
// components/feature-gate.tsx
'use client'

import { useOnlineStatus } from '@/hooks/use-online-status'
import { getFeatureStatus } from '@/lib/feature-availability'

interface FeatureGateProps {
  feature: Parameters<typeof getFeatureStatus>[0]
  children: React.ReactNode
  fallback?: React.ReactNode
  limitedFallback?: React.ReactNode
}

export function FeatureGate({ feature, children, fallback, limitedFallback }: FeatureGateProps) {
  const isOnline = useOnlineStatus()
  const status = getFeatureStatus(feature, { isOnline, degradedServices: [] })

  if (status === 'unavailable') {
    return fallback ?? (
      <div className="rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground">
        This feature is currently unavailable.
      </div>
    )
  }

  if (status === 'limited') {
    return limitedFallback ?? children
  }

  return children
}
```

#### Stale Data Serving

```typescript
// lib/stale-cache.ts — Serve stale data when fresh fetch fails
import { unstable_cache } from 'next/cache'

// Note: unstable_cache may be renamed in future Next.js versions.
// See Phase 0 version compatibility table for status.

export async function fetchWithStaleCache<T>(
  key: string,
  fetcher: () => Promise<T>,
  options: { revalidate?: number; tags?: string[] } = {}
): Promise<{ data: T; stale: boolean }> {
  const cachedFetcher = unstable_cache(
    fetcher,
    [key],
    { revalidate: options.revalidate ?? 60, tags: options.tags }
  )

  try {
    // Try fresh data first
    const data = await fetcher()
    return { data, stale: false }
  } catch {
    // Fall back to cached version
    try {
      const data = await cachedFetcher()
      return { data, stale: true }
    } catch {
      throw new Error(`No data available for "${key}" — both live and cache failed`)
    }
  }
}

// Usage in a Server Component:
// const { data: products, stale } = await fetchWithStaleCache(
//   'products-list',
//   () => db.query.products.findMany(),
//   { revalidate: 300, tags: ['products'] }
// )
// if (stale) { /* show "data may be outdated" banner */ }
```

---

### Prompt 20.4: Error Reporting Pipeline

Build a structured pipeline: **Capture → Classify → Alert → Resolve**

#### Error Classification

```typescript
// lib/error-classifier.ts
type ErrorSeverity = 'P0' | 'P1' | 'P2' | 'P3'

interface ClassifiedError {
  severity: ErrorSeverity
  category: 'user' | 'system' | 'external'
  alertChannel: 'pagerduty' | 'slack-urgent' | 'slack-alerts' | 'daily-digest'
  requiresImmediate: boolean
}

export function classifyError(error: unknown): ClassifiedError {
  // P0: System down / data loss risk
  if (error instanceof Error && error.message.includes('database')) {
    return {
      severity: 'P0',
      category: 'system',
      alertChannel: 'pagerduty',
      requiresImmediate: true,
    }
  }

  // P1: Feature broken for many users
  if (error instanceof AppError && !error.isOperational) {
    return {
      severity: 'P1',
      category: 'system',
      alertChannel: 'slack-urgent',
      requiresImmediate: true,
    }
  }

  // P2: External service degraded
  if (error instanceof ExternalServiceError) {
    return {
      severity: 'P2',
      category: 'external',
      alertChannel: 'slack-alerts',
      requiresImmediate: false,
    }
  }

  // P3: User errors, validation, expected failures
  if (error instanceof AppError && error.isOperational) {
    return {
      severity: 'P3',
      category: 'user',
      alertChannel: 'daily-digest',
      requiresImmediate: false,
    }
  }

  // Unknown errors default to P1
  return {
    severity: 'P1',
    category: 'system',
    alertChannel: 'slack-urgent',
    requiresImmediate: true,
  }
}
```

#### Sentry Integration with Custom Context

```typescript
// lib/error-reporter.ts
import * as Sentry from '@sentry/nextjs'
import { classifyError, type ClassifiedError } from './error-classifier'

export function reportError(error: unknown, context?: Record<string, unknown>) {
  const classification = classifyError(error)

  Sentry.withScope((scope) => {
    scope.setLevel(classificationToSentryLevel(classification))
    scope.setTag('error.severity', classification.severity)
    scope.setTag('error.category', classification.category)
    scope.setTag('error.alert_channel', classification.alertChannel)

    if (context) {
      scope.setContext('custom', context)
    }

    // Add breadcrumb for debugging
    Sentry.addBreadcrumb({
      category: 'error-pipeline',
      message: `Classified as ${classification.severity} (${classification.category})`,
      level: 'info',
    })

    Sentry.captureException(error)
  })

  // Trigger immediate alert for P0/P1
  if (classification.requiresImmediate) {
    triggerAlert(classification, error)
  }
}

function classificationToSentryLevel(c: ClassifiedError): Sentry.SeverityLevel {
  switch (c.severity) {
    case 'P0': return 'fatal'
    case 'P1': return 'error'
    case 'P2': return 'warning'
    case 'P3': return 'info'
  }
}

async function triggerAlert(classification: ClassifiedError, error: unknown) {
  const message = error instanceof Error ? error.message : 'Unknown error'

  if (classification.alertChannel === 'pagerduty') {
    // PagerDuty Events API v2
    await fetch('https://events.pagerduty.com/v2/enqueue', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        routing_key: process.env.PAGERDUTY_ROUTING_KEY,
        event_action: 'trigger',
        payload: {
          summary: `[${classification.severity}] ${message}`,
          severity: 'critical',
          source: process.env.NEXT_PUBLIC_APP_URL,
        },
      }),
    }).catch(() => {}) // Don't let alert failure crash the app
  }

  if (classification.alertChannel.startsWith('slack')) {
    await fetch(process.env.SLACK_WEBHOOK_URL!, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        text: `🚨 *${classification.severity}* — ${message}`,
        blocks: [
          {
            type: 'section',
            text: {
              type: 'mrkdwn',
              text: `*${classification.severity} Alert*\n\`${message}\`\n\nCategory: ${classification.category}\nChannel: ${classification.alertChannel}`,
            },
          },
        ],
      }),
    }).catch(() => {})
  }
}
```

#### Error Budget Tracking (SLO/SLI)

```typescript
// lib/error-budget.ts — Simple error budget tracking
// SLI: Percentage of successful requests
// SLO: 99.9% availability (allows ~43 minutes of downtime per month)

interface ErrorBudgetConfig {
  sloTarget: number        // e.g., 0.999 (99.9%)
  windowDays: number       // Rolling window (e.g., 30 days)
  totalRequests: number    // Estimated monthly requests
}

export function calculateErrorBudget(config: ErrorBudgetConfig, currentErrors: number) {
  const allowedErrors = Math.floor(config.totalRequests * (1 - config.sloTarget))
  const budgetRemaining = allowedErrors - currentErrors
  const budgetPercentUsed = (currentErrors / allowedErrors) * 100

  return {
    allowedErrors,
    currentErrors,
    budgetRemaining,
    budgetPercentUsed: Math.round(budgetPercentUsed * 100) / 100,
    isExhausted: budgetRemaining <= 0,
    status: budgetPercentUsed < 50 ? 'healthy' :
            budgetPercentUsed < 80 ? 'warning' :
            budgetPercentUsed < 100 ? 'critical' : 'exhausted',
  }
}

// Usage:
// const budget = calculateErrorBudget(
//   { sloTarget: 0.999, windowDays: 30, totalRequests: 1_000_000 },
//   currentErrors: 850
// )
// budget.status → 'warning' (850/1000 = 85% used)
```

```typescript
// app/api/health/route.ts — Health endpoint with error budget
import { calculateErrorBudget } from '@/lib/error-budget'

export async function GET() {
  // In production, read currentErrors from your metrics store (Prometheus, etc.)
  const budget = calculateErrorBudget(
    { sloTarget: 0.999, windowDays: 30, totalRequests: 1_000_000 },
    0 // Replace with actual error count from metrics
  )

  return Response.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    errorBudget: {
      status: budget.status,
      percentUsed: budget.budgetPercentUsed,
      remaining: budget.budgetRemaining,
    },
  })
}
```

Implement unified error handling with typed errors, resilience patterns (retry, circuit breaker, timeout), graceful degradation for offline/degraded states, and a structured error reporting pipeline with severity-based alerting.
