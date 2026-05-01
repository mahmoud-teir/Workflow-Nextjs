<a name="phase-m12"></a>
# 📌 MOBILE PHASE M12: PAYMENTS & IN-APP PURCHASES (Full-Stack Engineer)

> **Critical:** In-App Purchases (IAP) on iOS MUST use Apple's StoreKit — no external payment links allowed. Violating this rule is an App Store rejection. Use RevenueCat to abstract StoreKit 2 and Google Play Billing.

---

## 💰 IAP Architecture

```
RevenueCat SDK (react-native-purchases)
├── iOS: StoreKit 2 (Apple)
└── Android: Google Play Billing
    ├── Products: One-time purchases
    ├── Subscriptions: Auto-renewing
    └── Consumables: Redeemable credits/tokens
```

**Why RevenueCat?**
- Unified API across iOS and Android
- Handles receipt validation server-side
- Provides entitlements system (feature flags based on subscription)
- Dashboard for revenue analytics
- Handles subscription lifecycle (renewal, cancellation, grace period)

---

### Prompt M12.1: RevenueCat Setup

```text
You are a Mobile In-App Purchase Engineer. Implement RevenueCat for [AppName].

Subscription/Purchase model:
- [Describe your pricing: free tier / monthly $X.99 / annual $XX.99 / one-time $X.99]
- Features unlocked by premium:
  [List premium features]

Constraints:
- NEVER ask users to pay outside of App Store / Play Store payment flows.
- Show pricing from RevenueCat (not hardcoded) — it adapts to user's local currency.
- Implement "Restore Purchases" — required by App Store guidelines.
- Handle subscription status changes via webhook (not just on app open).

Required Output Format: Provide complete code for:

1. Installation:
```bash
npx expo install react-native-purchases react-native-purchases-ui
```

2. RevenueCat initialization `lib/payments/revenuecat.ts`:
```typescript
import Purchases, { LOG_LEVEL, PurchasesPackage } from 'react-native-purchases'
import { Platform } from 'react-native'

export async function initializeRevenueCat(userId?: string) {
  const apiKey = Platform.select({
    ios: process.env.EXPO_PUBLIC_REVENUECAT_IOS_KEY!,
    android: process.env.EXPO_PUBLIC_REVENUECAT_ANDROID_KEY!,
  })!

  if (__DEV__) Purchases.setLogLevel(LOG_LEVEL.DEBUG)

  await Purchases.configure({ apiKey })

  if (userId) {
    await Purchases.logIn(userId)
  }
}

// Check if user has an active entitlement
export async function checkPremiumAccess(): Promise<boolean> {
  try {
    const customerInfo = await Purchases.getCustomerInfo()
    return customerInfo.entitlements.active['premium'] !== undefined
  } catch {
    return false
  }
}

// Get available packages
export async function getOfferings(): Promise<PurchasesPackage[]> {
  const offerings = await Purchases.getOfferings()
  return offerings.current?.availablePackages ?? []
}

// Purchase a package
export async function purchasePackage(pkg: PurchasesPackage): Promise<boolean> {
  try {
    const { customerInfo } = await Purchases.purchasePackage(pkg)
    return customerInfo.entitlements.active['premium'] !== undefined
  } catch (error: any) {
    if (!error.userCancelled) throw error
    return false
  }
}

// Restore purchases (required by App Store)
export async function restorePurchases(): Promise<boolean> {
  const customerInfo = await Purchases.restorePurchases()
  return customerInfo.entitlements.active['premium'] !== undefined
}
```

3. Paywall screen using RevenueCat UI:
```tsx
import RevenueCatUI, { PAYWALL_RESULT } from 'react-native-purchases-ui'
import { useCallback } from 'react'
import { router } from 'expo-router'

export function PaywallModal() {
  const handleResult = useCallback(async (result: PAYWALL_RESULT) => {
    if (result === PAYWALL_RESULT.PURCHASED || result === PAYWALL_RESULT.RESTORED) {
      // User is now premium
      router.back()
      haptics.success()
    }
  }, [])

  return (
    <RevenueCatUI.Paywall
      onDismiss={() => router.back()}
      onPurchaseCompleted={handleResult}
    />
  )
}
```

4. Custom paywall component:
```tsx
export function CustomPaywall() {
  const [packages, setPackages] = useState<PurchasesPackage[]>([])
  const [isPurchasing, setIsPurchasing] = useState(false)
  const [isPremium, setIsPremium] = useState(false)

  useEffect(() => {
    getOfferings().then(setPackages)
    checkPremiumAccess().then(setIsPremium)
  }, [])

  const handlePurchase = async (pkg: PurchasesPackage) => {
    setIsPurchasing(true)
    try {
      const success = await purchasePackage(pkg)
      if (success) {
        setIsPremium(true)
        haptics.success()
        router.back()
      }
    } finally {
      setIsPurchasing(false)
    }
  }

  return (
    <View className="flex-1 p-6">
      <Text variant="h1" className="text-center mb-2">Go Premium</Text>
      <Text variant="body" color="muted" className="text-center mb-8">
        Unlock all features
      </Text>

      {packages.map((pkg) => (
        <Pressable
          key={pkg.identifier}
          onPress={() => handlePurchase(pkg)}
          className="border border-primary rounded-2xl p-4 mb-3"
        >
          <Text variant="label">{pkg.product.title}</Text>
          <Text variant="h2" className="text-primary">{pkg.product.priceString}</Text>
          {pkg.packageType === 'ANNUAL' && (
            <Badge variant="success">Best Value — Save 40%</Badge>
          )}
        </Pressable>
      ))}

      <Button variant="ghost" onPress={restorePurchases} className="mt-4">
        Restore Purchases
      </Button>

      <Text variant="caption" color="muted" className="text-center mt-4">
        Subscriptions auto-renew unless cancelled.
        {' '}
        <Text variant="caption" className="underline" onPress={() => Linking.openURL('[privacy-url]')}>
          Privacy Policy
        </Text>
        {' · '}
        <Text variant="caption" className="underline" onPress={() => Linking.openURL('[terms-url]')}>
          Terms
        </Text>
      </Text>
    </View>
  )
}
```

5. Backend webhook (Supabase Edge Function / Next.js API):
```typescript
// Handle RevenueCat webhooks to keep entitlements in sync
export async function POST(req: Request) {
  const body = await req.json()
  const { event } = body

  switch (event.type) {
    case 'INITIAL_PURCHASE':
    case 'RENEWAL':
      await db.update(users)
        .set({ isPremium: true, premiumExpiresAt: new Date(event.expiration_at_ms) })
        .where(eq(users.id, event.app_user_id))
      break
    case 'CANCELLATION':
    case 'EXPIRATION':
      await db.update(users)
        .set({ isPremium: false })
        .where(eq(users.id, event.app_user_id))
      break
  }

  return Response.json({ status: 'ok' })
}
```

⚠️ Common Pitfalls:
- Pitfall: Linking to external payment on iOS (e.g., "Subscribe on our website").
- Solution: All purchases on iOS MUST go through StoreKit. External link = App Store rejection.
- Pitfall: Not implementing Restore Purchases — Apple requires this button.
- Solution: Always include a visible "Restore Purchases" button on the paywall.
- Pitfall: Hardcoding prices ($9.99) — prices vary by region and can change.
- Solution: Always display `pkg.product.priceString` from RevenueCat.
```

✅ **Verification Checklist:**
- [ ] "Restore Purchases" button is visible on paywall.
- [ ] Prices displayed from RevenueCat API (not hardcoded).
- [ ] Subscription status synced via webhook on backend.
- [ ] Premium features locked for free users.
- [ ] Privacy Policy and Terms links visible near purchase buttons (App Store requirement).

---

📎 **Related Phases:**
- Prerequisites: [Phase M5: Authentication](./MOBILE_PHASE_5_AUTHENTICATION_SECURITY_Security_Expert.md)
- Proceeds to: [Phase M13: CI/CD](./MOBILE_PHASE_13_CICD_BUILD_PIPELINE_DevOps_Engineer.md)
