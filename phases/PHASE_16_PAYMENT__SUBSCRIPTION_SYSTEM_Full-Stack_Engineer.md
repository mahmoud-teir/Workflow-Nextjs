<a name="phase-16"></a>
# 📌 PHASE 16: PAYMENT & SUBSCRIPTION SYSTEM (Full-Stack Engineer)

> **Next.js Version:** See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 16.1: Stripe Integration

```text
You are a FinTech Full-Stack Engineer. Integrate Stripe robustly for payments and subscriptions.

Constraints:
- Stripe Checkout Sessions and Customer Portal sessions MUST only trigger from Server Actions to keep Secret Keys strictly isolated.
- Webhooks MUST verify the Stripe Signature.
- Webhooks MUST be entirely Idempotent (safe to retry infinitely).

Required Output Format: Provide complete code for:
1. `app/actions/stripe.ts`: Logic to create Checkout and Customer Portal sessions.
2. `app/api/webhooks/stripe/route.ts`: Secure webhook receiver validating signatures.
3. Database design (Prisma or Drizzle) detailing how to store `stripeCustomerId` and `subscriptionStatus`.

⚠️ Common Pitfalls:
- **Pitfall:** Returning `NextResponse.json({ error: '...' }, { status: 500 })` inside the webhook instead of `new Response()`. Stripe webhooks in Next.js 15+ expect standard WinterCG Response objects.
- **Solution:** Use standard `new Response('OK', { status: 200 })` to acknowledge receipts.
```

✅ **Verification Checklist:**
- [ ] Attempt to trigger the webhook endpoint locally with fake data (e.g., via Postman). It MUST return a 400 with a signature verification error.
- [ ] Complete a test transaction using Stripe CLI; verify the database updates.

---

### Prompt 16.2: Payment UI Components

```text
You are a Conversion Rate Optimization (CRO) Developer. Build the Pricing UI.

Constraints:
- Use React `useTransition` to track the loading state while the Server Action communicates with Stripe.
- Disable the submit button immediately upon click to prevent double-billing.

Required Output Format: Provide complete code for:
1. `<PricingCard>` Component connecting UI to the Stripe Server Action.
2. Pricing Model configuration array detailing features and Price IDs.
```

✅ **Verification Checklist:**
- [ ] Click subscribe; verify the button instantly switches to "Processing..." and is disabled before the page redirects.

---

### Prompt 16.3: Subscription Lifecycle Management

```text
You are a Subscription Economics Engineer. Handle the edge cases of subscription management.

Constraints:
- You must account for prorations if users upgrade/downgrade mid-cycle.
- You must build an "Invoice History" view pulling past receipts.

Required Output Format: Provide the Server Action logic for:
1. `changePlan(newPriceId)`: Implementing Stripe's proration behavior.
2. `cancelSubscription()`: Implementing `cancel_at_period_end: true` so they retain access until their paid time ends.
3. `getInvoiceHistory()`: Fetching PDFs for the user.

⚠️ Common Pitfalls:
- **Pitfall:** Canceling a subscription immediately (`cancel()`) instead of at period end, resulting in angry customers who lost 25 days of paid access.
- **Solution:** Always use `cancel_at_period_end: true`.
```

✅ **Verification Checklist:**
- [ ] Click Cancel Subscription. Verify the database `subscriptionStatus` remains `ACTIVE` but the End Date is flagged.

---
📎 **Related Phases:**
- Prerequisites: [Phase 14: Pre-Launch Checklist](./PHASE_14_PRE-LAUNCH_CHECKLIST_All_Roles.md)
- Proceeds to: [Phase 18: Analytics & Feature Flags](./PHASE_18_ANALYTICS__FEATURE_FLAGS_Product_Engineer.md)
