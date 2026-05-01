---
name: rn-patterns
description: Use this skill when writing React Native / Expo components, screens, or hooks. Enforces New Architecture patterns, NativeWind styling, Reanimated 3 animations, and Expo Router navigation.
origin: Mobile ECC
stack: React Native 0.76+, Expo SDK 52+, Expo Router 4, NativeWind v4, Reanimated 3
---

# React Native Patterns Skill

## Core Architecture Principles

### New Architecture (React Native 0.76+)
All code must be New Architecture compatible:
- Use Fabric renderer components only
- Use JSI-based libraries (not legacy bridge)
- Reanimated 3 runs natively on UI thread (no bridge)
- Gesture Handler uses Fabric (no bridge)

### File Naming Conventions
```
components/ui/Button.tsx          # PascalCase components
lib/hooks/useColorScheme.ts       # camelCase hooks
lib/store/auth.ts                 # camelCase stores
app/(tabs)/index.tsx              # Expo Router screens
```

## Expo Router Patterns

### File-Based Routing
```
app/
├── _layout.tsx              # Root layout
├── (auth)/                  # Auth group (unauthenticated)
│   ├── _layout.tsx
│   └── login.tsx
├── (tabs)/                  # Tab group (authenticated)
│   ├── _layout.tsx
│   └── index.tsx
└── [id].tsx                 # Dynamic segment
```

### Navigation
```typescript
// Push (stack)
router.push('/screen')
router.push({ pathname: '/[id]', params: { id: '123' } })

// Replace (no back)
router.replace('/(tabs)')

// Back
router.back()

// Link component
<Link href="/screen">Navigate</Link>
```

### Auth Guard Pattern
```typescript
// In (auth)/_layout.tsx
const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
if (isAuthenticated) return <Redirect href="/(tabs)" />
```

## NativeWind v4 Styling

### Setup
```javascript
// tailwind.config.js
module.exports = {
  presets: [require('nativewind/preset')],
  content: ['./app/**/*.tsx', './components/**/*.tsx'],
}
```

### Usage
```tsx
// Works with className prop on RN components
<View className="flex-1 bg-white dark:bg-gray-900 p-4">
  <Text className="text-[17px] font-medium text-gray-900 dark:text-gray-50">
    Hello
  </Text>
</View>
```

### Custom components (non-RN components need cssInterop)
```typescript
import { cssInterop } from 'nativewind'
cssInterop(CustomComponent, { className: 'style' })
```

## Reanimated 3 Animation Patterns

### Basic animation
```typescript
const opacity = useSharedValue(0)
const animStyle = useAnimatedStyle(() => ({
  opacity: withTiming(opacity.value, { duration: 300 }),
}))

// Use in component:
<Animated.View style={animStyle}>...</Animated.View>

// Trigger:
opacity.value = 1  // Animates to 1
```

### Spring animation
```typescript
const scale = useSharedValue(1)
const animStyle = useAnimatedStyle(() => ({
  transform: [{ scale: withSpring(scale.value, { damping: 15, stiffness: 100 }) }],
}))
```

### Entrance animations
```tsx
<Animated.View entering={FadeInDown.delay(100).springify()}>
  <Card />
</Animated.View>
```

### NEVER use legacy Animated API
```typescript
// ❌ WRONG — legacy, runs on JS thread
Animated.timing(value, { toValue: 1, duration: 300, useNativeDriver: true }).start()

// ✅ CORRECT — Reanimated 3, runs on UI thread
const value = useSharedValue(0)
value.value = withTiming(1, { duration: 300 })
```

## Component Architecture

### Screen structure
```tsx
export default function MyScreen() {
  return (
    <SafeAreaView className="flex-1 bg-white dark:bg-gray-900">
      <StatusBar style="auto" />
      {/* Header */}
      {/* Content (FlashList or ScrollView) */}
      {/* Bottom action area */}
    </SafeAreaView>
  )
}
```

### Loading states — Skeleton (never spinner for lists)
```tsx
// ❌ WRONG for list loading
<ActivityIndicator size="large" />

// ✅ CORRECT
{isLoading ? (
  Array.from({ length: 5 }).map((_, i) => (
    <Skeleton key={i} width="100%" height={80} borderRadius={12} className="mb-3" />
  ))
) : (
  <FlashList data={items} ... />
)}
```

### Touch targets (must meet minimum sizes)
```tsx
// iOS: 44pt minimum | Android: 48dp minimum
<Pressable
  className="h-11 items-center justify-center px-4"  // h-11 = 44pt
  accessibilityRole="button"
  accessibilityLabel="Descriptive label"
>
  <Text>Button</Text>
</Pressable>
```

## Performance Rules

1. **FlashList** over FlatList for lists > 20 items
2. **React.memo** on all list item components
3. **useCallback** for all event handlers in list items
4. **useMemo** for expensive computations
5. **Zustand selectors** — never subscribe to full store
6. **expo-image** over RN Image
7. **Reanimated 3** for all animations

## Platform Handling

```typescript
import { Platform } from 'react-native'

// Platform-specific values
const styles = {
  shadow: Platform.select({
    ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 4 },
    android: { elevation: 4 },
  }),
}

// Platform-specific components
const Tab = Platform.OS === 'ios' ? IOSTab : AndroidTab
```

## Accessibility

Every interactive element must have:
```tsx
<Pressable
  accessibilityRole="button"
  accessibilityLabel="Delete post"
  accessibilityHint="Removes this post permanently"
  accessibilityState={{ disabled: isLoading }}
>
```
