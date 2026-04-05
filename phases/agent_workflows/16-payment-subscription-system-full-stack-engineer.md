---
phase: 16
title: Payment & Subscription System
role: Full-Stack Engineer
dependencies: [Phase 2, Phase 4, Phase 5]
estimated_time: 4-6 hours
---

# Phase 16: Payment & Subscription System — Agent Workflow

## Prerequisites
- [ ] Phase 4 completed (auth with user identification)
- [ ] Required env vars: `STRIPE_SECRET_KEY`, `NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY`, `STRIPE_WEBHOOK_SECRET`
- [ ] Stripe account with products/prices configured

## Step-by-Step Execution

### Step 1: Install Stripe
**Action:** `run_command`
```bash
pnpm add stripe @stripe/stripe-js
```

### Step 2: Write Stripe Client
**Action:** `write_to_file`
**File:** `lib/stripe.ts`
**Description:** Server-side Stripe instance and helper to create checkout sessions with `metadata.userId` (NOT `customer_details.email`).

### Step 3: Write Checkout API Route
**Action:** `write_to_file`
**File:** `app/api/stripe/checkout/route.ts`
**Description:** Create Stripe Checkout session with `metadata.userId` for secure user identification. Include `tax_id_collection` for Stripe Tax.

### Step 4: Write Webhook Handler
**Action:** `write_to_file`
**File:** `app/api/stripe/webhook/route.ts`
**Description:** Webhook handler with signature verification, idempotent processing (check/record event ID), and `metadata.userId` for user lookup. Handle: `checkout.session.completed`, `customer.subscription.updated`, `customer.subscription.deleted`, `invoice.payment_failed`.

### Step 5: Write Pricing Card Component
**Action:** `write_to_file`
**File:** `components/pricing-card.tsx`
**Description:** Client component with plan details, pricing, and checkout button using `useTransition` for loading state.

### Step 6: Write Subscription Management
**Action:** `write_to_file`
**File:** `app/(app)/settings/billing/page.tsx`
**Description:** Plan change (upgrade/downgrade with proration), cancellation with grace period (`cancel_at_period_end`), and invoice history.

### Step 7: Write Billing Portal Redirect
**Action:** `write_to_file`
**File:** `app/actions/billing.ts`
**Description:** Server Action to create Stripe Billing Portal session for self-service management.

### Step 8: Add PayPal Alternative (Optional)
**Action:** `write_to_file`
**File:** `lib/paypal.ts`
**Description:** PayPal SDK integration as alternative payment method.

## Verification
- [ ] Checkout flow creates subscription successfully
- [ ] Webhook processes events with idempotency
- [ ] User identified by `metadata.userId` (NOT email)
- [ ] Plan changes apply correct proration
- [ ] Cancellation sets `cancel_at_period_end`
- [ ] Invoice history displays correctly

## Troubleshooting
- **Issue:** Webhook signature verification fails
  **Fix:** Use raw body (`request.text()`) for signature verification. Don't parse JSON before verifying.
- **Issue:** User not found in webhook
  **Fix:** Ensure checkout session includes `metadata: { userId }`. NEVER use `customer_details.email` for identification — it's insecure.
