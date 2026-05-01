<a name="phase-m7"></a>
# 📌 MOBILE PHASE M7: NATIVE FEATURES & APIs (Mobile Developer)

> **Key Principle:** Always request permissions at the moment they are needed — NEVER at app launch. Always handle the "denied" state gracefully.

---

### Prompt M7.1: Camera & Media

```text
You are a React Native Native Features Engineer. Implement camera and media access for [AppName].

Features needed: [Photo capture / Video recording / QR scanner / Image picker from gallery]

Constraints:
- Request camera permission only when the user initiates a camera action.
- Provide a custom pre-permission explanation screen before the native dialog.
- Handle the "permanently denied" state (guide users to Settings).
- Compress images before upload (mobile cameras produce 5-20MB images).
- Support both iOS and Android camera conventions.

Required Output Format: Provide complete code for:

1. Installation:
```bash
npx expo install expo-camera expo-image-picker expo-media-library
```

2. Permission handling pattern (reusable):
```typescript
// lib/native/permissions.ts
import { Alert, Linking } from 'react-native'

export async function requestPermissionWithFallback(
  requestFn: () => Promise<{ status: string }>,
  permissionName: string,
): Promise<boolean> {
  const { status } = await requestFn()

  if (status === 'granted') return true

  if (status === 'denied') {
    Alert.alert(
      `${permissionName} Required`,
      `Please enable ${permissionName} access in Settings to use this feature.`,
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Open Settings', onPress: () => Linking.openSettings() },
      ]
    )
  }

  return false
}
```

3. Image picker with compression:
```tsx
import * as ImagePicker from 'expo-image-picker'
import * as ImageManipulator from 'expo-image-manipulator'

export async function pickAndCompressImage(): Promise<string | null> {
  const granted = await requestPermissionWithFallback(
    ImagePicker.requestMediaLibraryPermissionsAsync,
    'Photo Library'
  )
  if (!granted) return null

  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    allowsEditing: true,
    aspect: [1, 1],
    quality: 1,  // Keep original — we compress manually
  })

  if (result.canceled) return null
  const uri = result.assets[0].uri

  // Compress to max 1MB
  const compressed = await ImageManipulator.manipulateAsync(uri, [], {
    compress: 0.7,
    format: ImageManipulator.SaveFormat.JPEG,
  })

  return compressed.uri
}
```

4. QR Code scanner:
```tsx
import { CameraView, useCameraPermissions } from 'expo-camera'
import { useState } from 'react'

export function QRScanner({ onScan }: { onScan: (data: string) => void }) {
  const [permission, requestPermission] = useCameraPermissions()
  const [scanned, setScanned] = useState(false)

  if (!permission?.granted) {
    return (
      <View className="flex-1 items-center justify-center gap-4">
        <Text>Camera access is needed to scan QR codes</Text>
        <Button onPress={requestPermission}>Allow Camera</Button>
      </View>
    )
  }

  return (
    <CameraView
      style={{ flex: 1 }}
      facing="back"
      barcodeScannerSettings={{ barcodeTypes: ['qr'] }}
      onBarcodeScanned={({ data }) => {
        if (!scanned) {
          setScanned(true)
          onScan(data)
        }
      }}
    />
  )
}
```

5. `app.json` permission strings (required for App Store review):
```json
{
  "expo": {
    "ios": {
      "infoPlist": {
        "NSCameraUsageDescription": "Allow [AppName] to take photos for your profile and posts.",
        "NSPhotoLibraryUsageDescription": "Allow [AppName] to select photos from your library.",
        "NSPhotoLibraryAddUsageDescription": "Allow [AppName] to save photos to your library.",
        "NSMicrophoneUsageDescription": "Allow [AppName] to record audio for videos."
      }
    }
  }
}
```

⚠️ Common Pitfalls:
- Pitfall: Not providing permission description strings — Apple rejects the build.
- Solution: Every permission usage must have a corresponding NSXxxUsageDescription.
- Pitfall: Not handling the case where the user taps "Don't Allow" twice (permanently denied on iOS).
- Solution: Check permission status, and if `status === 'denied'`, show Settings deep link.
```

✅ **Verification Checklist:**
- [ ] Camera permission prompts at the moment of use (not app launch).
- [ ] "Permanently denied" state shows "Open Settings" alert.
- [ ] Image compression reduces file size to <1MB.
- [ ] QR scanner stops scanning after first successful read.
- [ ] All `NSXxxUsageDescription` strings are in app.json.

---

### Prompt M7.2: Location Services

```text
You are a React Native Location Services Engineer. Implement location features for [AppName].

Location features needed: [Map display / User location / Geofencing / Background location]

Constraints:
- Request ONLY "When in Use" location permission unless background is strictly required.
- Apple and Google BOTH require strong justification for background location — use it sparingly.
- Show a custom explanation screen before the native permission dialog.
- On iOS 14+, request "Precise" vs "Approximate" location deliberately.

Required Output Format: Provide complete code for:

1. Installation:
```bash
npx expo install expo-location react-native-maps
```

2. Location permission + current position:
```typescript
import * as Location from 'expo-location'

export async function getCurrentLocation(): Promise<Location.LocationObject | null> {
  const { status } = await Location.requestForegroundPermissionsAsync()
  if (status !== 'granted') return null

  return Location.getCurrentPositionAsync({
    accuracy: Location.Accuracy.Balanced,  // Balance accuracy vs battery
    timeInterval: 10000,
    distanceInterval: 50,
  })
}

export function useUserLocation() {
  const [location, setLocation] = useState<Location.LocationObject | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let subscriber: Location.LocationSubscription | null = null

    Location.requestForegroundPermissionsAsync().then(({ status }) => {
      if (status !== 'granted') {
        setError('Location permission denied')
        return
      }
      Location.watchPositionAsync(
        { accuracy: Location.Accuracy.Balanced, timeInterval: 5000 },
        (loc) => setLocation(loc)
      ).then((sub) => { subscriber = sub })
    })

    return () => { subscriber?.remove() }
  }, [])

  return { location, error }
}
```

3. Map with user location:
```tsx
import MapView, { Marker, PROVIDER_GOOGLE } from 'react-native-maps'
import { useUserLocation } from '@/lib/hooks/useLocation'
import { Platform } from 'react-native'

export function AppMap() {
  const { location } = useUserLocation()

  return (
    <MapView
      style={{ flex: 1 }}
      provider={Platform.OS === 'android' ? PROVIDER_GOOGLE : undefined}
      showsUserLocation
      showsMyLocationButton={false}  // Custom button instead
      initialRegion={{
        latitude: location?.coords.latitude ?? 37.7749,
        longitude: location?.coords.longitude ?? -122.4194,
        latitudeDelta: 0.05,
        longitudeDelta: 0.05,
      }}
    />
  )
}
```

4. `app.json` location permission strings:
```json
{
  "expo": {
    "ios": {
      "infoPlist": {
        "NSLocationWhenInUseUsageDescription": "Allow [AppName] to show nearby [items] on the map.",
        "NSLocationAlwaysAndWhenInUseUsageDescription": "Allow [AppName] to track your [activity] in the background."
      }
    }
  }
}
```
```

✅ **Verification Checklist:**
- [ ] Location requested only on screens that need it.
- [ ] Map renders correctly with user's real location.
- [ ] Permission denied state shows helpful explanation.
- [ ] Background location (if used) has strong App Store justification.

---

### Prompt M7.3: Haptics & System Integration

```text
You are a Mobile UX Engineer. Implement haptic feedback and system integrations for [AppName].

Haptics enhance touch interactions — they are a key differentiator between a native app and a web wrapper.

Required Output Format: Provide complete code for:

1. Haptic feedback utility `lib/native/haptics.ts`:
```typescript
import * as Haptics from 'expo-haptics'

export const haptics = {
  // Light — for UI interactions (button taps, toggles)
  light: () => Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light),
  // Medium — for confirmations (save, submit)
  medium: () => Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium),
  // Heavy — for destructive actions (delete, error)
  heavy: () => Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Heavy),
  // Success — for completed actions
  success: () => Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success),
  // Error — for failures
  error: () => Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error),
  // Warning
  warning: () => Haptics.notificationAsync(Haptics.NotificationFeedbackType.Warning),
  // Selection — for picker/list selection
  selection: () => Haptics.selectionAsync(),
}
```

2. Share API:
```typescript
import { Share, Platform } from 'react-native'

export async function shareContent(title: string, url: string, message?: string) {
  await Share.share(
    Platform.select({
      ios: { url, title, message },
      android: { message: `${message ?? title}\n${url}` },
    })!
  )
}
```

3. Clipboard:
```typescript
import * as Clipboard from 'expo-clipboard'

export async function copyToClipboard(text: string) {
  await Clipboard.setStringAsync(text)
  haptics.success()
  // Show toast: "Copied to clipboard"
}
```

4. Barcode generation (for sharing codes):
```bash
npm install react-native-qrcode-svg react-native-svg
```
```tsx
import QRCode from 'react-native-qrcode-svg'

export function ShareQR({ value }: { value: string }) {
  return (
    <View className="items-center p-6 bg-white rounded-2xl">
      <QRCode value={value} size={200} color="#000000" backgroundColor="transparent" />
    </View>
  )
}
```
```

✅ **Verification Checklist:**
- [ ] Haptics fire on button press (heavy devices feel the difference).
- [ ] Share sheet opens correctly on iOS and Android.
- [ ] Clipboard copy shows success toast.
- [ ] Haptics disabled gracefully on devices without haptic engines.

---

📎 **Related Phases:**
- Prerequisites: [Phase M6: UI Components](./MOBILE_PHASE_6_UI_COMPONENTS_DESIGN_SYSTEM_Frontend_Developer.md)
- Proceeds to: [Phase M8: State Management](./MOBILE_PHASE_8_STATE_MANAGEMENT_Full_Stack_Mobile.md)
