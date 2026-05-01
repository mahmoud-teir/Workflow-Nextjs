<a name="phase-m5"></a>
# 📌 MOBILE PHASE M5: AUTHENTICATION & SECURITY (Security Expert)

> **Security First:** Mobile authentication has unique attack vectors — insecure storage, jailbreak bypass, reverse engineering, and network interception. Every decision must be security-driven.

---

### Prompt M5.1: Mobile Authentication System

```text
You are a rigorous Mobile Security Expert. Implement a production-grade authentication system for a React Native / Expo app.

Auth options — choose ONE:
- Option A: Supabase Auth (recommended — managed, supports OAuth, MFA)
- Option B: Clerk React Native SDK (managed — fastest to implement)
- Option C: Custom JWT (maximum control — complex to implement securely)

Constraints:
- NEVER store tokens in AsyncStorage — use expo-secure-store (Keychain/Keystore).
- Implement automatic token refresh before expiry.
- Support biometric re-authentication for sensitive actions.
- Handle auth state changes globally (logout on 401, session expiry).

Required Output Format: Provide complete code for:

1. Token management `lib/auth/token.ts`:
```typescript
import * as SecureStore from 'expo-secure-store'

const KEYS = {
  ACCESS_TOKEN: 'auth_access_token',
  REFRESH_TOKEN: 'auth_refresh_token',
  USER_ID: 'auth_user_id',
} as const

export async function getToken(key: keyof typeof KEYS): Promise<string | null> {
  return SecureStore.getItemAsync(KEYS[key])
}

export async function setToken(key: keyof typeof KEYS, value: string): Promise<void> {
  await SecureStore.setItemAsync(KEYS[key], value, {
    keychainAccessible: SecureStore.AFTER_FIRST_UNLOCK,  // iOS Keychain access level
  })
}

export async function clearTokens(): Promise<void> {
  await Promise.all(Object.values(KEYS).map((key) => SecureStore.deleteItemAsync(key)))
}
```

2. Auth Zustand store `lib/store/auth.ts`:
```typescript
import { create } from 'zustand'
import { clearTokens, getToken } from '@/lib/auth/token'
import { router } from 'expo-router'

interface User {
  id: string
  email: string
  name: string
  avatarUrl?: string
}

interface AuthState {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  setUser: (user: User) => void
  logout: () => Promise<void>
  initialize: () => Promise<void>
}

export const useAuthStore = create<AuthState>()((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,

  setUser: (user) => set({ user, isAuthenticated: true, isLoading: false }),

  logout: async () => {
    await clearTokens()
    set({ user: null, isAuthenticated: false })
    router.replace('/(auth)/login')
  },

  initialize: async () => {
    try {
      const token = await getToken('ACCESS_TOKEN')
      if (!token) {
        set({ isLoading: false })
        return
      }
      // Validate token + fetch user...
      const user = await fetchCurrentUser()
      set({ user, isAuthenticated: true })
    } catch {
      await clearTokens()
    } finally {
      set({ isLoading: false })
    }
  },
}))
```

3. Login screen with Supabase (Option A):
```typescript
import { supabase } from '@/lib/db/supabase'
import { useAuthStore } from '@/lib/store/auth'

export async function loginWithEmail(email: string, password: string) {
  const { data, error } = await supabase.auth.signInWithPassword({ email, password })
  if (error) throw new ApiError(error.message, 'AUTH_FAILED', 401)

  const { setUser } = useAuthStore.getState()
  setUser({
    id: data.user.id,
    email: data.user.email!,
    name: data.user.user_metadata.name,
  })
}

// OAuth (Google/Apple) — uses native browser:
export async function loginWithGoogle() {
  await supabase.auth.signInWithOAuth({
    provider: 'google',
    options: { redirectTo: 'myapp://auth-callback' },
  })
}
```

4. Session listener (in `app/_layout.tsx`):
```typescript
useEffect(() => {
  const { data: listener } = supabase.auth.onAuthStateChange(async (event, session) => {
    if (event === 'SIGNED_IN' && session) {
      useAuthStore.getState().setUser(/* map session.user */)
    } else if (event === 'SIGNED_OUT') {
      useAuthStore.getState().logout()
    } else if (event === 'TOKEN_REFRESHED') {
      // Token auto-refreshed — no action needed
    }
  })
  return () => listener.subscription.unsubscribe()
}, [])
```

⚠️ Common Pitfalls:
- Pitfall: Storing JWT in AsyncStorage — readable by any app with root access.
- Solution: Always use expo-secure-store (maps to iOS Keychain / Android Keystore).
- Pitfall: Not handling the app going to background during auth flow.
- Solution: Use `AppState` listener to re-validate session when app returns to foreground.
```

✅ **Verification Checklist:**
- [ ] Tokens stored in SecureStore (verify with Expo debugging tools).
- [ ] Logout clears ALL token keys from SecureStore.
- [ ] Expired session redirects to login without showing raw error.
- [ ] OAuth flow returns to app correctly via deep link.

---

### Prompt M5.2: Biometric Authentication

```text
You are a Mobile Biometric Security Engineer. Add Face ID / Touch ID / Fingerprint authentication.

Use cases:
- Re-authenticate before sensitive actions (payments, data export, account deletion)
- App lock (require biometrics after [N] minutes in background)
- Quick login (skip password for returning users)

Required Output Format: Provide complete code for:

1. Installation:
```bash
npx expo install expo-local-authentication
```

2. `lib/auth/biometric.ts`:
```typescript
import * as LocalAuthentication from 'expo-local-authentication'

export async function checkBiometricSupport() {
  const hasHardware = await LocalAuthentication.hasHardwareAsync()
  const isEnrolled = await LocalAuthentication.isEnrolledAsync()
  const supportedTypes = await LocalAuthentication.supportedAuthenticationTypesAsync()

  return {
    isSupported: hasHardware && isEnrolled,
    hasFaceID: supportedTypes.includes(LocalAuthentication.AuthenticationType.FACIAL_RECOGNITION),
    hasTouchID: supportedTypes.includes(LocalAuthentication.AuthenticationType.FINGERPRINT),
  }
}

export async function authenticateWithBiometric(reason: string): Promise<boolean> {
  const { isSupported } = await checkBiometricSupport()
  if (!isSupported) return false

  const result = await LocalAuthentication.authenticateAsync({
    promptMessage: reason,
    fallbackLabel: 'Use Passcode',
    cancelLabel: 'Cancel',
    disableDeviceFallback: false,  // Allow PIN/passcode fallback
  })

  return result.success
}
```

3. Biometric gate component:
```tsx
import { authenticateWithBiometric } from '@/lib/auth/biometric'

export function BiometricGate({
  children,
  reason = 'Authenticate to continue',
  fallback,
}: {
  children: React.ReactNode
  reason?: string
  fallback?: React.ReactNode
}) {
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  const authenticate = async () => {
    const success = await authenticateWithBiometric(reason)
    setIsAuthenticated(success)
  }

  if (!isAuthenticated) {
    return (
      <View>
        {fallback ?? (
          <Pressable onPress={authenticate}>
            <Text>Authenticate with Face ID / Touch ID</Text>
          </Pressable>
        )}
      </View>
    )
  }

  return <>{children}</>
}
```

4. App lock (re-authenticate after background):
```typescript
import { AppState, AppStateStatus } from 'react-native'

export function useAppLock(lockAfterSeconds = 300) {
  const [backgroundedAt, setBackgroundedAt] = useState<Date | null>(null)
  const [isLocked, setIsLocked] = useState(false)

  useEffect(() => {
    const subscription = AppState.addEventListener('change', (state: AppStateStatus) => {
      if (state === 'background') setBackgroundedAt(new Date())
      if (state === 'active' && backgroundedAt) {
        const elapsed = (Date.now() - backgroundedAt.getTime()) / 1000
        if (elapsed > lockAfterSeconds) setIsLocked(true)
        setBackgroundedAt(null)
      }
    })
    return () => subscription.remove()
  }, [backgroundedAt, lockAfterSeconds])

  const unlock = async () => {
    const success = await authenticateWithBiometric('Unlock [AppName]')
    if (success) setIsLocked(false)
  }

  return { isLocked, unlock }
}
```

⚠️ Common Pitfalls:
- Pitfall: Not adding `NSFaceIDUsageDescription` to `app.json` — Apple rejects the build.
- Solution: Add to infoPlist: `{ "NSFaceIDUsageDescription": "Authenticate to access your account" }`.
- Pitfall: Biometric auth succeeds but you're not verifying server-side.
- Solution: Biometrics are a UI gate only. Always verify session tokens server-side.
```

✅ **Verification Checklist:**
- [ ] Face ID / Touch ID prompt appears with correct reason string.
- [ ] Fallback to passcode works when biometric fails.
- [ ] `NSFaceIDUsageDescription` added to app.json infoPlist (iOS).
- [ ] App lock activates after correct background timeout.

---

### Prompt M5.3: Mobile Security Hardening

```text
You are an OWASP Mobile Application Security Expert. Harden [AppName] against OWASP Mobile Top 10.

OWASP Mobile Top 10 (2024):
M1: Improper Credential Usage
M2: Inadequate Supply Chain Security
M3: Insecure Authentication/Authorization
M4: Insufficient Input/Output Validation
M5: Insecure Communication
M6: Inadequate Privacy Controls
M7: Insufficient Binary Protections
M8: Security Misconfiguration
M9: Insecure Data Storage
M10: Insufficient Cryptography

Required Output Format: Implement mitigations for each:

1. M1 — Credential Security:
- All credentials in expo-secure-store ✅
- No credentials in app.json, source code, or logs ✅
- Environment variables via EAS Secrets ✅
- Certificate pinning for sensitive APIs (optional, adds complexity):
```bash
npm install react-native-ssl-pinning
```

2. M5 — Secure Communication:
```typescript
// Verify HTTPS only in production
if (process.env.NODE_ENV === 'production') {
  if (!config.apiUrl.startsWith('https://')) {
    throw new Error('Production API must use HTTPS')
  }
}
```

3. M7 — Binary Protections (via EAS):
```json
// eas.json production profile
{
  "build": {
    "production": {
      "android": { "buildType": "app-bundle" },  // AAB is harder to reverse-engineer than APK
      "ios": { "buildConfiguration": "Release" }
    }
  }
}
```

4. M9 — Secure Data Storage audit checklist:
```typescript
// ❌ WRONG — insecure storage
AsyncStorage.setItem('token', accessToken)
MMKV.set('password', password)

// ✅ CORRECT
SecureStore.setItemAsync('auth_access_token', accessToken)

// Audit: search for insecure patterns
// grep -r "AsyncStorage.setItem" src/ — should not contain tokens/passwords
```

5. Input validation with Zod on all user inputs:
```typescript
import { z } from 'zod'

export const LoginSchema = z.object({
  email: z.string().email('Invalid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

// Validate before API call:
const result = LoginSchema.safeParse({ email, password })
if (!result.success) throw new ValidationError(result.error.flatten())
```

6. Root/jailbreak detection (optional — for financial/healthcare apps):
```bash
npm install react-native-device-info
```
```typescript
import DeviceInfo from 'react-native-device-info'

async function checkDeviceSecurity() {
  const isEmulator = await DeviceInfo.isEmulator()
  // Note: React Native doesn't have a reliable jailbreak detector without native modules
  // Expo Managed Workflow: use expo-device for basic checks
  return { isEmulator }
}
```
```

✅ **Verification Checklist:**
- [ ] No secrets in source code (`grep -r "secret\|password\|api_key" src/` returns nothing sensitive).
- [ ] All API calls use HTTPS in production.
- [ ] Zod validation on all user-facing input forms.
- [ ] SecureStore used for all sensitive data (no AsyncStorage for tokens).
- [ ] Production build uses Release configuration (not Debug).

---

📎 **Related Phases:**
- Prerequisites: [Phase M4: Database & Offline](./MOBILE_PHASE_4_DATABASE_OFFLINE_STORAGE_Mobile_Architect.md)
- Proceeds to: [Phase M6: UI Components](./MOBILE_PHASE_6_UI_COMPONENTS_DESIGN_SYSTEM_Frontend_Developer.md)
