<a name="phase-m11"></a>
# 📌 MOBILE PHASE M11: PUSH NOTIFICATIONS & ANALYTICS (Product Engineer)

> **Rule:** Push notification opt-in MUST be user-initiated. Never request permission automatically on first launch.

---

### Prompt M11.1: Expo Push Notifications

```text
You are a Mobile Push Notification Engineer. Implement push notifications for [AppName] using Expo Notifications.

Notification types needed:
- [List your notification types: transactional, engagement, marketing, etc.]

Constraints:
- Never request notification permission before showing a custom explanation screen.
- Handle both foreground and background notifications.
- Deep link from notification to the correct screen.
- Provide an in-app notification center for notifications received while app was open.
- Respect system notification settings (check if enabled before sending local notifications).

Required Output Format: Provide complete code for:

1. Installation:
```bash
npx expo install expo-notifications expo-device
```

2. Permission flow `lib/notifications/permission.ts`:
```typescript
import * as Notifications from 'expo-notifications'
import * as Device from 'expo-device'
import Constants from 'expo-constants'
import { Platform } from 'react-native'

export async function registerForPushNotifications(): Promise<string | null> {
  // Physical device required — simulators can't receive push
  if (!Device.isDevice) {
    console.warn('Push notifications require a physical device')
    return null
  }

  // Check existing permission
  const { status: existingStatus } = await Notifications.getPermissionsAsync()

  if (existingStatus === 'granted') {
    return getExpoPushToken()
  }

  // Show custom pre-prompt BEFORE native dialog (handled in UI)
  const { status } = await Notifications.requestPermissionsAsync()

  if (status !== 'granted') return null

  return getExpoPushToken()
}

async function getExpoPushToken(): Promise<string> {
  const projectId = Constants.expoConfig?.extra?.eas?.projectId
  const token = await Notifications.getExpoPushTokenAsync({ projectId })
  return token.data
}

// Send the token to your backend
export async function savePushToken(token: string, userId: string) {
  await apiClient.post('/notifications/token', { token, userId, platform: Platform.OS })
}
```

3. Notification handler setup `app/_layout.tsx`:
```typescript
import * as Notifications from 'expo-notifications'
import { useEffect, useRef } from 'react'
import { useRouter } from 'expo-router'

// Handle notifications while app is foreground
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
})

export default function RootLayout() {
  const router = useRouter()
  const notificationListener = useRef<Notifications.EventSubscription>()
  const responseListener = useRef<Notifications.EventSubscription>()

  useEffect(() => {
    // Received while app is open
    notificationListener.current = Notifications.addNotificationReceivedListener((notification) => {
      // Add to in-app notification center
      console.log('Received:', notification)
    })

    // User tapped the notification
    responseListener.current = Notifications.addNotificationResponseReceivedListener((response) => {
      const data = response.notification.request.content.data
      // Deep link based on notification data
      if (data.screen) router.push(data.screen as string)
    })

    // Handle notification that launched the app
    Notifications.getLastNotificationResponseAsync().then((response) => {
      if (response?.notification.request.content.data.screen) {
        router.push(response.notification.request.content.data.screen as string)
      }
    })

    return () => {
      notificationListener.current?.remove()
      responseListener.current?.remove()
    }
  }, [router])

  // ...
}
```

4. Notification opt-in screen component:
```tsx
export function NotificationOptInScreen({ onDismiss }: { onDismiss: () => void }) {
  const [isRequesting, setIsRequesting] = useState(false)

  const handleEnable = async () => {
    setIsRequesting(true)
    const token = await registerForPushNotifications()
    if (token) {
      const user = useAuthStore.getState().user
      if (user) await savePushToken(token, user.id)
    }
    setIsRequesting(false)
    onDismiss()
  }

  return (
    <View className="flex-1 items-center justify-center p-6 gap-6">
      <Text variant="h1" className="text-center">Stay Updated</Text>
      <Text className="text-center text-gray-500">
        Get notified about [specific value: new messages, order updates, etc.]
        We'll only send what matters.
      </Text>
      <Button onPress={handleEnable} isLoading={isRequesting} size="lg" className="w-full">
        Enable Notifications
      </Button>
      <Button variant="ghost" onPress={onDismiss}>Not now</Button>
    </View>
  )
}
```

5. Backend: sending push via Expo Push API:
```typescript
// On your backend (Node.js / Next.js)
import Expo, { ExpoPushMessage } from 'expo-server-sdk'

const expo = new Expo()

export async function sendPushNotification(
  to: string,
  title: string,
  body: string,
  data?: Record<string, unknown>
) {
  if (!Expo.isExpoPushToken(to)) throw new Error(`Invalid push token: ${to}`)

  const message: ExpoPushMessage = {
    to,
    title,
    body,
    data: data ?? {},
    sound: 'default',
    badge: 1,
    // For deep linking in notification tap:
    // data: { screen: '/(tabs)/messages', messageId: '123' }
  }

  const chunks = expo.chunkPushNotifications([message])
  for (const chunk of chunks) {
    await expo.sendPushNotificationsAsync(chunk)
  }
}
```

⚠️ Common Pitfalls:
- Pitfall: Requesting notification permission on app first launch (users deny at 80% rate).
- Solution: Show opt-in screen after user sees value from the app (post-onboarding, after first success).
- Pitfall: Not handling FCM/APNs token rotation — tokens expire.
- Solution: Re-register and re-save token on every app launch.
```

✅ **Verification Checklist:**
- [ ] Physical device receives push notification in foreground.
- [ ] Tapping notification deep links to correct screen.
- [ ] App notification opt-in prompt appears at the right moment (not on launch).
- [ ] Token saved to backend on registration.
- [ ] Notification dismissed gracefully when user taps "Not now".

---

### Prompt M11.2: Analytics (PostHog — Consent-Gated)

```text
You are a Mobile Analytics Engineer. Implement PostHog analytics for [AppName] with proper user consent.

Constraints:
- Analytics MUST be consent-gated — never initialize before user consents.
- Respect "Do Not Track" / app tracking transparency (iOS 14+).
- Collect ONLY what's needed. Avoid excessive user profiling.
- Implement ATT (App Tracking Transparency) dialog for iOS 14+ before any tracking.

Required Output Format: Provide complete code for:

1. Installation:
```bash
npm install posthog-react-native
npx expo install expo-tracking-transparency
```

2. Consent-gated initialization `lib/analytics/posthog.ts`:
```typescript
import PostHog from 'posthog-react-native'

let posthog: PostHog | null = null

export async function initializeAnalytics(hasConsented: boolean) {
  if (!hasConsented) {
    posthog?.optOut()
    return
  }

  posthog = new PostHog(process.env.EXPO_PUBLIC_POSTHOG_KEY!, {
    host: 'https://us.i.posthog.com',
    disabled: false,
  })
}

export function track(event: string, properties?: Record<string, unknown>) {
  posthog?.capture(event, properties)
}

export function identifyUser(userId: string, properties?: Record<string, unknown>) {
  posthog?.identify(userId, properties)
}

export function resetAnalytics() {
  posthog?.reset()  // Call on logout
}
```

3. iOS App Tracking Transparency (ATT):
```typescript
import { requestTrackingPermissionsAsync } from 'expo-tracking-transparency'

export async function requestTrackingPermission(): Promise<boolean> {
  const { status } = await requestTrackingPermissionsAsync()
  return status === 'granted'
}
```

4. Screen tracking hook:
```typescript
import { usePathname } from 'expo-router'
import { useEffect } from 'react'
import { track } from '@/lib/analytics/posthog'

export function useScreenTracking() {
  const pathname = usePathname()

  useEffect(() => {
    // Sanitize path — remove personal IDs
    const sanitizedPath = pathname.replace(/\/[0-9a-f-]{36}/g, '/[id]')
    track('$screen', { $screen_name: sanitizedPath })
  }, [pathname])
}
```

5. `app.json` for ATT:
```json
{
  "expo": {
    "plugins": [
      ["expo-tracking-transparency", {
        "userTrackingPermission": "This helps us improve [AppName] with anonymous usage data."
      }]
    ]
  }
}
```

⚠️ Common Pitfalls:
- Pitfall: Initializing PostHog before checking ATT consent on iOS 14+ — Apple rejects the app.
- Solution: Always call `requestTrackingPermissionsAsync()` first on iOS.
- Pitfall: Tracking user IDs directly (PII) in analytics events.
- Solution: Use hashed or anonymized user identifiers. Never track email/name/phone directly.
```

✅ **Verification Checklist:**
- [ ] ATT permission dialog appears before any tracking on iOS 14+.
- [ ] Analytics disabled when user declines tracking.
- [ ] Screen events fire on navigation.
- [ ] No PII (email, phone) in analytics events.

---

📎 **Related Phases:**
- Prerequisites: [Phase M10: Performance](./MOBILE_PHASE_10_PERFORMANCE_OPTIMIZATION_Mobile_Developer.md)
- Proceeds to: [Phase M14: App Store Launch](./MOBILE_PHASE_14_APP_STORE_SUBMISSION_LAUNCH_All_Roles.md)
- Optional: [Phase M12: Payments & IAP](./MOBILE_PHASE_12_PAYMENTS_IAP_Full_Stack_Engineer.md)
