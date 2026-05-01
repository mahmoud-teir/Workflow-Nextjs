<a name="phase-m12"></a>
# 📌 MOBILE PHASE M12: PAYMENTS & IN-APP PURCHASES (Full-Stack Engineer)

> **Critical:** Google Play requires all digital goods and subscriptions to use Google Play Billing. Using external payment links for digital goods is a policy violation and will result in app suspension. We use **RevenueCat** to abstract the complex BillingClient.

---

## 💰 IAP Architecture

```
RevenueCat SDK (purchases-android)
└── Google Play Billing API
    ├── Products: One-time purchases
    ├── Subscriptions: Auto-renewing
    └── Consumables: Redeemable credits
```

---

### Prompt M12.1: RevenueCat Setup for Android

```text
You are a Mobile In-App Purchase Engineer. Implement RevenueCat for [AppName].

Requirements:
- Add `com.revenuecat.purchases:purchases` dependency.
- Initialize RevenueCat in the `Application` class.
- Create a `BillingRepository` or `BillingManager` to abstract RevenueCat calls using Coroutines.

Required Output Format: Provide complete code for:

1. Initialization `App.kt`:
```kotlin
import android.app.Application
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Purchases.debugLogsEnabled = BuildConfig.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_ANDROID_KEY).build()
        )
    }
}
```

2. Billing Manager `data/repository/BillingManager.kt`:
```kotlin
package com.example.app.data.repository

import android.app.Activity
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.example.app.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor() {

    companion object {
        const val ENTITLEMENT_PREMIUM = "premium"
    }

    suspend fun getOfferings(): Resource<List<Package>> = withContext(Dispatchers.IO) {
        try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val packages = offerings.current?.availablePackages ?: emptyList()
            Resource.Success(packages)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch offerings")
        }
    }

    suspend fun purchase(activity: Activity, pkg: Package): Resource<Boolean> = withContext(Dispatchers.Main) {
        try {
            val purchaseResult = Purchases.sharedInstance.awaitPurchase(activity, pkg)
            val isPro = purchaseResult.customerInfo.entitlements.active[ENTITLEMENT_PREMIUM] != null
            Resource.Success(isPro)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Purchase failed")
        }
    }

    suspend fun restorePurchases(): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val customerInfo = Purchases.sharedInstance.awaitRestore()
            val isPro = customerInfo.entitlements.active[ENTITLEMENT_PREMIUM] != null
            Resource.Success(isPro)
        } catch (e: Exception) {
            Resource.Error("Failed to restore: ${e.localizedMessage}")
        }
    }
    
    suspend fun checkPremiumStatus(): Boolean = withContext(Dispatchers.IO) {
        try {
            val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
            customerInfo.entitlements.active[ENTITLEMENT_PREMIUM] != null
        } catch (e: Exception) {
            false
        }
    }
}
```

3. Paywall ViewModel connection:
```kotlin
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {
    // Collect offerings and expose state
    // ...
}
```

⚠️ Common Pitfalls:
- Pitfall: Triggering the `awaitPurchase` function from a background thread (`Dispatchers.IO`).
- Solution: The Google Play Billing overlay requires an `Activity` context and MUST be launched from the Main thread (`Dispatchers.Main`).
- Pitfall: Hardcoding prices instead of reading them from the RevenueCat `Package`.
- Solution: Always display `pkg.product.price.formatted` to respect the user's local currency.
```

---

✅ **Verification Checklist:**
- [ ] Prices displayed are fetched from RevenueCat API (not hardcoded).
- [ ] Tapping "Buy" opens the Google Play test purchase overlay.
- [ ] "Restore Purchases" button is visible and functional on the paywall.
- [ ] Premium features are properly locked behind the `ENTITLEMENT_PREMIUM` check.

---

📎 **Related Phases:**
- Prerequisites: [Phase M5: Security](./MOBILE_PHASE_5_AUTHENTICATION_SECURITY_Security_Expert.md)
- Proceeds to: [Phase M13: CI/CD](./MOBILE_PHASE_13_CICD_BUILD_PIPELINE_DevOps_Engineer.md)
