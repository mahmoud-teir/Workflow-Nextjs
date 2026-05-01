<a name="phase-m5"></a>
# 📌 MOBILE PHASE M5: AUTHENTICATION & SECURITY (Security Expert)

> **Rule:** Never store authentication tokens, passwords, or PII in `SharedPreferences`, `DataStore`, or plaintext SQLite. Always use `EncryptedSharedPreferences` or the Android Keystore system.

---

### Prompt M5.1: EncryptedSharedPreferences (Token Storage)

```text
You are an Android Security Expert. Implement secure token storage for [AppName] using the AndroidX Security library.

Requirements:
- Add the `androidx.security:security-crypto` dependency.
- Create a `TokenManager` class that wraps `EncryptedSharedPreferences`.
- Provide Hilt integration.
- Include methods for saving, retrieving, and clearing tokens.

Required Output Format: Provide complete code for:

1. Setup in `gradle/libs.versions.toml`:
```toml
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version = "1.1.0-alpha06" }
```

2. Secure Storage Manager `data/local/TokenManager.kt`:
```kotlin
package com.example.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: Hardcoding encryption keys.
- Solution: Let `MasterKey.Builder` manage the key in the Android Keystore system.
- Pitfall: Using alpha versions of the crypto library that crash on specific OEMs.
- Solution: Test on Samsung devices (especially Android 9/10), which sometimes have Keystore quirks, or use `1.1.0-alpha06` which is generally stable.
```

---

### Prompt M5.2: Biometric Authentication

```text
You are a Mobile Security Engineer. Implement Android BiometricPrompt to protect sensitive areas of the app.

Requirements:
- Use `androidx.biometric:biometric`.
- Handle success, error, and fallback scenarios.
- Wrap the logic in a reusable Coroutine or Callback-based helper.

Required Output Format: Provide complete code for `util/BiometricHelper.kt`:

```kotlin
package com.example.app.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            onError("Biometrics not available or not enrolled.")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Usually handled automatically by the system UI
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
```
*Note: MainActivity must inherit from `FragmentActivity` (or `AppCompatActivity`) instead of `ComponentActivity` for BiometricPrompt to work.*
```

---

### Prompt M5.3: Security Best Practices Check (OWASP)

```text
You are a Mobile Security Auditor. Review the Android implementation against OWASP Mobile Top 10 standards.

Output a checklist confirming compliance for:
1. Network Security Configuration (XML) to prevent cleartext traffic.
2. Root detection or basic Play Integrity API usage (if applicable).
3. FlagSecure to prevent screenshots on sensitive screens.

Required Output:
Provide the setup for `res/xml/network_security_config.xml` to strictly enforce HTTPS:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.example.com</domain>
    </domain-config>
    <!-- Allow cleartext for localhost debugging -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

And in `AndroidManifest.xml`:
```xml
<application
    ...
    android:networkSecurityConfig="@xml/network_security_config">
```

For Compose screen protection (prevent screenshots):
```kotlin
@Composable
fun SecureScreen() {
    val activity = LocalContext.current as Activity
    DisposableEffect(Unit) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    // ... UI
}
```
```

---

✅ **Verification Checklist:**
- [ ] `EncryptedSharedPreferences` compiles and saves/retrieves data correctly.
- [ ] Token is attached to Retrofit requests via interceptor.
- [ ] `NetworkSecurityConfig` enforces HTTPS for production domains.
- [ ] Attempting a screenshot on a `SecureScreen` results in a black image.
- [ ] App prompts for Face/Fingerprint when `BiometricHelper` is triggered.

---

📎 **Related Phases:**
- Prerequisites: [Phase M4: Database](./MOBILE_PHASE_4_DATABASE_OFFLINE_STORAGE_Mobile_Architect.md)
- Proceeds to: [Phase M6: UI Components](./MOBILE_PHASE_6_UI_COMPONENTS_DESIGN_SYSTEM_Frontend_Developer.md)
