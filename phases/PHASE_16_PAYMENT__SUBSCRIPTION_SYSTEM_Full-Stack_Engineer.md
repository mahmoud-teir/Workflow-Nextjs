<a name="phase-16"></a>
# 📌 PHASE 16: PAYMENT & SUBSCRIPTION SYSTEM (Full-Stack Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 16.1: Stripe Integration

```text
You are a Full-Stack Engineer. Integrate Stripe for payments and subscriptions.

Required:
1. Checkout sessions (one-time + subscription)
2. Webhook handling with secure signature verification
3. Customer portal for self-service
4. Apple Pay / Google Pay via Payment Element
5. Idempotent webhook processing
6. Stripe Radar for fraud detection
```

```typescript
// app/actions/stripe.ts
'use server'

import Stripe from 'stripe'
import { requireAuth } from '@/lib/action-utils'
import { db } from '@/lib/db'

const stripe = new Stripe(process.env.STRIPE_SECRET_KEY!)

export async function createCheckoutSession(priceId: string) {
  const authResult = await requireAuth()
  if ('error' in authResult) return authResult

  const { user } = authResult

  // Get or create Stripe customer
  let customerId = user.stripeCustomerId
  if (!customerId) {
    const customer = await stripe.customers.create({
      email: user.email,
      metadata: { userId: user.id },
    })
    customerId = customer.id
    await db.user.update({
      where: { id: user.id },
      data: { stripeCustomerId: customerId },
    })
  }

  const session = await stripe.checkout.sessions.create({
    customer: customerId,
    mode: 'subscription',
    payment_method_types: ['card'],
    line_items: [{ price: priceId, quantity: 1 }],
    success_url: `${process.env.NEXT_PUBLIC_APP_URL}/billing?success=true`,
    cancel_url: `${process.env.NEXT_PUBLIC_APP_URL}/billing?canceled=true`,
    metadata: { userId: user.id },
    subscription_data: {
      metadata: { userId: user.id },
    },
    payment_method_collection: 'always',
    tax_id_collection: { enabled: true },
  })

  return { success: true, data: { url: session.url } }
}

export async function createCustomerPortalSession() {
  const authResult = await requireAuth()
  if ('error' in authResult) return authResult

  const { user } = authResult
  if (!user.stripeCustomerId) {
    return { success: false, error: 'No billing account found' }
  }

  const session = await stripe.billingPortal.sessions.create({
    customer: user.stripeCustomerId,
    return_url: `${process.env.NEXT_PUBLIC_APP_URL}/billing`,
  })

  return { success: true, data: { url: session.url } }
}
```

```typescript
// app/api/webhooks/stripe/route.ts
// ⚠️ Next.js 15+: use req.text() for rawBody — do NOT use next/headers here.
// next/headers returns a Promise in Next.js 15+ and adds unnecessary overhead.
// The Web Request API (req.headers.get) is synchronous and always available in Route Handlers.
import Stripe from 'stripe'
import { db } from '@/lib/db'

const stripe = new Stripe(process.env.STRIPE_SECRET_KEY!)

export async function POST(req: Request) {
  const rawBody = await req.text()
  const signature = req.headers.get('stripe-signature')!

  let event: Stripe.Event

  try {
    event = stripe.webhooks.constructEvent(
      rawBody,
      signature,
      process.env.STRIPE_WEBHOOK_SECRET!
    )
  } catch (err) {
    console.error('Webhook signature verification failed:', err)
    return new Response('Invalid signature', { status: 400 })
  }

  // Idempotency: Check if already processed
  const existing = await db.webhookEvent.findUnique({
    where: { stripeEventId: event.id },
  })
  if (existing) {
    return new Response('Already processed', { status: 200 })
  }

  await db.webhookEvent.create({
    data: { stripeEventId: event.id, type: event.type, processedAt: new Date() },
  })

  try {
    switch (event.type) {
      case 'checkout.session.completed': {
        const session = event.data.object as Stripe.Checkout.Session
        // ⚠️ CRITICAL: Use metadata.userId, NOT customer_details.email
        const userId = session.metadata?.userId
        if (!userId) { console.error('No userId in metadata'); break }
        await db.user.update({
          where: { id: userId },
          data: {
            subscriptionStatus: 'ACTIVE',
            stripeSubscriptionId: session.subscription as string,
          },
        })
        break
      }

      case 'customer.subscription.updated': {
        const subscription = event.data.object as Stripe.Subscription
        const userId = subscription.metadata?.userId
        if (!userId) break
        await db.user.update({
          where: { id: userId },
          data: {
            subscriptionStatus: subscription.status === 'active' ? 'ACTIVE' : 'PAST_DUE',
            currentPeriodEnd: new Date(subscription.current_period_end * 1000),
          },
        })
        break
      }

      case 'customer.subscription.deleted': {
        const subscription = event.data.object as Stripe.Subscription
        const userId = subscription.metadata?.userId
        if (!userId) break
        await db.user.update({
          where: { id: userId },
          data: { subscriptionStatus: 'CANCELED', stripeSubscriptionId: null },
        })
        break
      }

      case 'invoice.payment_failed': {
        const invoice = event.data.object as Stripe.Invoice
        const userId = invoice.subscription_details?.metadata?.userId
        if (userId) await sendPaymentFailedEmail(userId)
        break
      }
    }
  } catch (error) {
    console.error(`Error processing webhook ${event.type}:`, error)
    return new Response('Webhook processing error', { status: 500 })
  }

  return new Response('OK', { status: 200 })
}
```

---

### Prompt 16.2: Payment UI Components

```tsx
// components/pricing-card.tsx
'use client'

import { createCheckoutSession } from '@/app/actions/stripe'
import { useTransition } from 'react'
import { toast } from 'sonner'

export function PricingCard({ plan }: { plan: PricingPlan }) {
  const [isPending, startTransition] = useTransition()

  function handleSubscribe() {
    startTransition(async () => {
      const result = await createCheckoutSession(plan.priceId)
      if (result.success && result.data.url) {
        window.location.href = result.data.url
      } else {
        toast.error('Failed to start checkout')
      }
    })
  }

  return (
    <div className={cn('rounded-lg border p-6 space-y-4', plan.popular && 'border-primary ring-2 ring-primary')}>
      <h3 className="text-xl font-bold">{plan.name}</h3>
      <p className="text-3xl font-bold">{plan.price}<span className="text-sm text-muted-foreground">/mo</span></p>
      <ul className="space-y-2">
        {plan.features.map(f => (
          <li key={f} className="flex items-center gap-2 text-sm">✓ {f}</li>
        ))}
      </ul>
      <button onClick={handleSubscribe} disabled={isPending}
        className="w-full px-4 py-2 bg-primary text-primary-foreground rounded-md disabled:opacity-50">
        {isPending ? 'Processing...' : 'Subscribe'}
      </button>
    </div>
  )
}
```

---

### Prompt 16.3: Subscription Management

```typescript
// app/actions/subscription.ts
'use server'

import Stripe from 'stripe'
import { requireAuth } from '@/lib/action-utils'

const stripe = new Stripe(process.env.STRIPE_SECRET_KEY!)

export async function changePlan(newPriceId: string) {
  const authResult = await requireAuth()
  if ('error' in authResult) return authResult
  const { user } = authResult

  if (!user.stripeSubscriptionId) return { success: false, error: 'No active subscription' }

  const subscription = await stripe.subscriptions.retrieve(user.stripeSubscriptionId)
  await stripe.subscriptions.update(user.stripeSubscriptionId, {
    items: [{ id: subscription.items.data[0].id, price: newPriceId }],
    proration_behavior: 'create_prorations',
  })

  return { success: true, data: undefined }
}

export async function cancelSubscription() {
  const authResult = await requireAuth()
  if ('error' in authResult) return authResult
  const { user } = authResult

  if (!user.stripeSubscriptionId) return { success: false, error: 'No active subscription' }

  await stripe.subscriptions.update(user.stripeSubscriptionId, {
    cancel_at_period_end: true,
  })

  return { success: true, data: undefined }
}

export async function getInvoiceHistory() {
  const authResult = await requireAuth()
  if ('error' in authResult) return authResult
  const { user } = authResult

  if (!user.stripeCustomerId) return { success: true, data: [] }

  const invoices = await stripe.invoices.list({ customer: user.stripeCustomerId, limit: 12 })

  return {
    success: true,
    data: invoices.data.map(inv => ({
      id: inv.id,
      amount: inv.amount_paid / 100,
      currency: inv.currency,
      status: inv.status,
      date: new Date(inv.created * 1000).toISOString(),
      pdfUrl: inv.invoice_pdf,
    })),
  }
}
```

---

### Prompt 16.4: PayPal Integration (Alternative)

```tsx
// components/paypal-button.tsx
'use client'

import { PayPalScriptProvider, PayPalButtons } from '@paypal/react-paypal-js'

export function PayPalPayment({ amount }: { amount: string }) {
  return (
    <PayPalScriptProvider options={{ clientId: process.env.NEXT_PUBLIC_PAYPAL_CLIENT_ID! }}>
      <PayPalButtons
        createOrder={(data, actions) => {
          return actions.order.create({
            intent: 'CAPTURE',
            purchase_units: [{ amount: { value: amount, currency_code: 'USD' } }],
          })
        }}
        onApprove={async (data, actions) => {
          const details = await actions.order!.capture()
          await fetch('/api/paypal/capture', {
            method: 'POST',
            body: JSON.stringify({ orderId: details.id }),
          })
        }}
      />
    </PayPalScriptProvider>
  )
}
```

```text
Implement complete payment system with Stripe (primary), subscription lifecycle, and PayPal alternative.
```
