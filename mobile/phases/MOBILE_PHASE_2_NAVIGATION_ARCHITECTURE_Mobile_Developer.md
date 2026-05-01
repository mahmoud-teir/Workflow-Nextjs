<a name="phase-m2"></a>
# 📌 MOBILE PHASE M2: NAVIGATION ARCHITECTURE (Mobile Developer)

> **Navigation Framework:** Expo Router 4 (file-based routing — recommended) with React Navigation 7 primitives under the hood.

---

### Prompt M2.1: Expo Router — Core Navigation Structure

```text
You are a Senior React Native Navigation Architect. Implement the complete navigation architecture for [AppName] using Expo Router 4.

App Structure (from Phase M0.5 wireframes):
- [List your screens and navigation hierarchy here]

Platform: [iOS / Android / Both]

Constraints:
- Use Expo Router 4 file-based routing exclusively.
- Enable TypeScript typed routes (`"typedRoutes": true` in app.json `experiments`).
- Auth-gated routes must redirect unauthenticated users using Expo Router's `<Redirect>`.
- Deep links must work for: [list any deep-linkable screens].
- Tab bar must have ≤5 items (iOS HIG recommendation).

Required Output Format: Provide complete code for:

1. Root `app/_layout.tsx` — Auth state check + provider setup:
```tsx
import { Stack, Redirect } from 'expo-router'
import { useAuthStore } from '@/lib/store/auth'

export default function RootLayout() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)

  return (
    <Stack>
      <Stack.Screen name="(auth)" options={{ headerShown: false }} />
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
    </Stack>
  )
}
```

2. Auth stack `app/(auth)/_layout.tsx`:
```tsx
import { Stack } from 'expo-router'
import { useAuthStore } from '@/lib/store/auth'
import { Redirect } from 'expo-router'

export default function AuthLayout() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  if (isAuthenticated) return <Redirect href="/(tabs)" />

  return <Stack screenOptions={{ headerShown: false }} />
}
```

3. Tab navigator `app/(tabs)/_layout.tsx`:
```tsx
import { Tabs } from 'expo-router'
import { useColorScheme } from '@/lib/hooks/useColorScheme'
import { Home, Search, PlusCircle, Bell, User } from 'lucide-react-native'

export default function TabLayout() {
  const { colorScheme } = useColorScheme()
  const isDark = colorScheme === 'dark'

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: isDark ? '#FFFFFF' : '#000000',
        tabBarStyle: {
          backgroundColor: isDark ? '#0f0f0f' : '#ffffff',
          borderTopWidth: 0,
          elevation: 0,
        },
      }}
    >
      <Tabs.Screen name="index" options={{ title: 'Home', tabBarIcon: ({ color }) => <Home size={24} color={color} /> }} />
      <Tabs.Screen name="explore" options={{ title: 'Explore', tabBarIcon: ({ color }) => <Search size={24} color={color} /> }} />
      <Tabs.Screen name="profile" options={{ title: 'Profile', tabBarIcon: ({ color }) => <User size={24} color={color} /> }} />
    </Tabs>
  )
}
```

4. Dynamic route with typed params `app/(tabs)/[id].tsx`:
```tsx
import { useLocalSearchParams, router } from 'expo-router'

type Params = { id: string }

export default function DetailScreen() {
  const { id } = useLocalSearchParams<Params>()

  return (
    // ...
  )
}
```

5. Programmatic navigation patterns:
```tsx
import { router } from 'expo-router'

// Push (adds to stack)
router.push('/detail/123')

// Replace (replaces current, no back)
router.replace('/(tabs)')

// Go back
router.back()

// Navigate with params
router.push({ pathname: '/detail/[id]', params: { id: '123' } })

// Navigate to modal
router.push('/modal/settings')
```

⚠️ Common Pitfalls:
- Pitfall: Using `useRouter().push()` with a hardcoded string like `/home` — no type safety.
- Solution: Enable `typedRoutes: true` in app.json and use the generated types.
- Pitfall: Nested navigators causing double headers.
- Solution: Set `headerShown: false` on the outer layout when using nested Stack.
```

✅ **Verification Checklist:**
- [ ] Auth guard redirects unauthenticated users to `(auth)/login`.
- [ ] Authenticated users are redirected away from auth screens.
- [ ] Tab bar renders correctly on both iOS and Android.
- [ ] Deep link `[scheme]://[route]` opens the correct screen.
- [ ] TypeScript catches invalid route strings.

---

### Prompt M2.2: Deep Links & Universal Links

```text
You are a Mobile Deep Link Architect. Implement deep link handling for [AppName].

Deep links required:
- [scheme]://home → Home screen
- [scheme]://product/[id] → Product detail
- [scheme]://reset-password?token=[token] → Password reset
- https://[domain].com/share/[id] → Universal link (iOS) / App Link (Android)

Required Output Format: Provide complete implementation for:

1. `app.json` scheme configuration:
```json
{
  "expo": {
    "scheme": "[appscheme]",
    "ios": {
      "associatedDomains": ["applinks:[domain].com"]
    },
    "android": {
      "intentFilters": [
        {
          "action": "VIEW",
          "autoVerify": true,
          "data": [{ "scheme": "https", "host": "[domain].com" }],
          "category": ["BROWSABLE", "DEFAULT"]
        }
      ]
    }
  }
}
```

2. Link parsing in `app/_layout.tsx` using `Linking`:
```tsx
import * as Linking from 'expo-linking'
import { useEffect } from 'react'

export default function RootLayout() {
  useEffect(() => {
    const subscription = Linking.addEventListener('url', ({ url }) => {
      // Expo Router handles routing automatically
      // This is for custom parsing if needed
      console.log('Deep link received:', url)
    })
    return () => subscription.remove()
  }, [])
  // ...
}
```

3. Universal Link verification file (for iOS):
   - File to serve at `https://[domain].com/.well-known/apple-app-site-association`
   ```json
   {
     "applinks": {
       "apps": [],
       "details": [{
         "appID": "[TEAM_ID].[BUNDLE_ID]",
         "paths": ["/share/*", "/reset-password"]
       }]
     }
   }
   ```

4. Testing deep links:
```bash
# iOS Simulator
xcrun simctl openurl booted "[appscheme]://product/123"

# Android Emulator
adb shell am start -a android.intent.action.VIEW -d "[appscheme]://product/123"

# Production universal link test
npx uri-scheme open "https://[domain].com/product/123" --ios
```

⚠️ Common Pitfalls:
- Pitfall: Universal links not working because AASA file is not served with correct Content-Type.
- Solution: Serve `apple-app-site-association` with `Content-Type: application/json` (no .json extension).
```

✅ **Verification Checklist:**
- [ ] `[scheme]://home` opens app to home screen.
- [ ] `[scheme]://product/123` opens product detail with id=123.
- [ ] Password reset link works end-to-end.
- [ ] AASA file accessible at `https://[domain]/.well-known/apple-app-site-association`.

---

### Prompt M2.3: Navigation Patterns — Modals, Sheets, Drawers

```text
You are a Mobile UX Navigation Specialist. Implement rich navigation patterns for [AppName].

Required Output Format: Provide complete code for:

1. Bottom Sheet Modal (react-native-bottom-sheet):
```tsx
import BottomSheet, { BottomSheetView } from '@gorhom/bottom-sheet'
import { useRef, useCallback } from 'react'

export function useBottomSheet() {
  const bottomSheetRef = useRef<BottomSheet>(null)

  const open = useCallback(() => bottomSheetRef.current?.expand(), [])
  const close = useCallback(() => bottomSheetRef.current?.close(), [])

  const BottomSheetComponent = () => (
    <BottomSheet
      ref={bottomSheetRef}
      index={-1}
      snapPoints={['25%', '50%', '90%']}
      enablePanDownToClose
      backgroundStyle={{ borderRadius: 24 }}
    >
      <BottomSheetView>
        {/* Content */}
      </BottomSheetView>
    </BottomSheet>
  )

  return { open, close, BottomSheetComponent }
}
```

2. Full-screen modal (Expo Router modal):
```
// app/(modals)/_layout.tsx
import { Stack } from 'expo-router'

export default function ModalLayout() {
  return (
    <Stack>
      <Stack.Screen
        name="filter"
        options={{
          presentation: 'modal',
          title: 'Filter',
          headerLeft: () => <CloseButton />,
        }}
      />
    </Stack>
  )
}
```

3. Sidebar / Drawer (Expo Router drawer):
```tsx
import { Drawer } from 'expo-router/drawer'

export default function DrawerLayout() {
  return (
    <Drawer
      screenOptions={{
        drawerType: 'front',
        drawerStyle: { width: '80%' },
      }}
    >
      <Drawer.Screen name="index" options={{ title: 'Home' }} />
      <Drawer.Screen name="settings" options={{ title: 'Settings' }} />
    </Drawer>
  )
}
```

4. iOS-style action sheet:
```tsx
import { ActionSheetIOS, Platform, Alert } from 'react-native'

export function showActionSheet(options: string[], callback: (index: number) => void) {
  if (Platform.OS === 'ios') {
    ActionSheetIOS.showActionSheetWithOptions({ options, cancelButtonIndex: options.length - 1 }, callback)
  } else {
    // Android: use a custom BottomSheet or Dialog
    // react-native-action-sheet provides a cross-platform solution
  }
}
```

5. Platform-adaptive navigation header:
```tsx
import { Platform } from 'react-native'
import { Stack } from 'expo-router'

<Stack.Screen
  options={{
    headerTitle: 'Screen Title',
    headerBackTitle: '', // iOS only — hide "Back" label
    headerLargeTitle: Platform.OS === 'ios', // iOS 11+ large title
    headerTitleAlign: Platform.OS === 'android' ? 'center' : 'left',
    headerShadowVisible: false,
    headerStyle: { backgroundColor: 'transparent' },
  }}
/>
```
```

✅ **Verification Checklist:**
- [ ] Bottom sheet opens/closes with smooth gesture animation.
- [ ] Modal can be dismissed by swipe down (iOS) and back button (Android).
- [ ] Drawer does not overlap tab bar.
- [ ] Action sheet uses native iOS pattern on iPhone.

---

📎 **Related Phases:**
- Prerequisites: [Phase M1: Project Structure](./MOBILE_PHASE_1_PROJECT_STRUCTURE_CONFIGURATION_Full_Stack_Mobile.md)
- Proceeds to: [Phase M3: Backend & API Integration](./MOBILE_PHASE_3_BACKEND_API_INTEGRATION_Full_Stack.md)
