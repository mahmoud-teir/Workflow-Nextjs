<a name="phase-m6"></a>
# 📌 MOBILE PHASE M6: UI COMPONENTS & DESIGN SYSTEM (Frontend Developer)

> **Styling:** NativeWind v4 (Tailwind CSS for React Native) is the recommended approach. Tamagui is the alternative for maximum performance.

---

## 🎨 Design Pathways (same as web — adapted for mobile)

1. **Pathway A (Prototype-First):** Phase M0B prototype is the reference. Implement pixel-perfect.
2. **Pathway B (Stitch AI Design):** Use Google Stitch MCP with `deviceType: MOBILE` to generate screens, then translate to RN components.
3. **Pathway C (Code-First):** AI generates components from `MOBILE_DESIGN.md` design tokens.
4. **Pathway D (User-Provided):** User provides Figma designs or images — implement exactly.

> ⚠️ **Anti-Generic Design Rule:** Mobile UIs must NOT look like AI-generated templates. No Inter font with pure white backgrounds. No "3 equal cards" grids. No emojis in headers. The result must feel like a real, hand-crafted native product.

---

### Prompt M6.1: Core UI Component Library

```text
You are a Lead React Native Design System Engineer. Build the foundational UI component library for [AppName].

Design System: [Reference MOBILE_DESIGN.md tokens]
Styling: NativeWind v4
Platform: [iOS / Android / Both]

Constraints:
- Every interactive component MUST meet ≥44pt (iOS) / ≥48dp (Android) touch target.
- All components MUST support dark mode via `useColorScheme()`.
- Use `Pressable` instead of `TouchableOpacity` for all interactive elements.
- Never use hardcoded colors — always reference design tokens.
- Animations MUST use Reanimated 3 (not the deprecated `Animated` API).
- Support `accessibilityLabel`, `accessibilityRole`, and `accessibilityHint` on all interactive elements.

Required Output Format: Provide complete code for:

1. `components/ui/Text.tsx` — Typography component:
```tsx
import { Text as RNText, TextProps } from 'react-native'
import { cn } from '@/lib/utils/cn'

const variants = {
  display: 'text-[34px] font-bold leading-[40px] tracking-tight',
  h1: 'text-[28px] font-bold leading-[34px]',
  h2: 'text-[22px] font-semibold leading-[28px]',
  body: 'text-[17px] font-normal leading-[24px]',
  caption: 'text-[13px] font-normal leading-[18px]',
  label: 'text-[15px] font-medium leading-[20px]',
} as const

interface AppTextProps extends TextProps {
  variant?: keyof typeof variants
  color?: 'primary' | 'secondary' | 'muted' | 'error' | 'success'
}

export function Text({ variant = 'body', color = 'primary', className, ...props }: AppTextProps) {
  const colorClass = {
    primary: 'text-gray-900 dark:text-gray-50',
    secondary: 'text-gray-700 dark:text-gray-300',
    muted: 'text-gray-500 dark:text-gray-400',
    error: 'text-red-600 dark:text-red-400',
    success: 'text-green-600 dark:text-green-400',
  }[color]

  return (
    <RNText
      className={cn(variants[variant], colorClass, className)}
      {...props}
    />
  )
}
```

2. `components/ui/Button.tsx` — Primary button with Reanimated 3:
```tsx
import { Pressable, View, ActivityIndicator } from 'react-native'
import Animated, { useSharedValue, useAnimatedStyle, withTiming } from 'react-native-reanimated'
import { Text } from './Text'
import { cn } from '@/lib/utils/cn'

const AnimatedPressable = Animated.createAnimatedComponent(Pressable)

const variants = {
  primary: 'bg-primary active:bg-primary-dark',
  secondary: 'bg-gray-100 dark:bg-gray-800 active:bg-gray-200',
  destructive: 'bg-red-600 active:bg-red-700',
  ghost: 'bg-transparent',
  outline: 'border border-gray-300 dark:border-gray-600 bg-transparent',
} as const

const sizes = {
  sm: { container: 'h-8 px-3 rounded-lg', text: 'text-[13px]' },
  md: { container: 'h-11 px-5 rounded-xl', text: 'text-[15px]' },  // 44pt iOS minimum
  lg: { container: 'h-14 px-6 rounded-2xl', text: 'text-[17px]' },
} as const

interface ButtonProps {
  variant?: keyof typeof variants
  size?: keyof typeof sizes
  onPress?: () => void
  isLoading?: boolean
  isDisabled?: boolean
  children: React.ReactNode
  className?: string
}

export function Button({
  variant = 'primary',
  size = 'md',
  onPress,
  isLoading = false,
  isDisabled = false,
  children,
  className,
}: ButtonProps) {
  const scale = useSharedValue(1)
  const animStyle = useAnimatedStyle(() => ({
    transform: [{ scale: withTiming(scale.value, { duration: 100 }) }],
  }))

  return (
    <AnimatedPressable
      style={animStyle}
      onPressIn={() => { scale.value = 0.96 }}
      onPressOut={() => { scale.value = 1 }}
      onPress={isDisabled || isLoading ? undefined : onPress}
      accessibilityRole="button"
      accessibilityState={{ disabled: isDisabled || isLoading }}
      className={cn(
        'items-center justify-center flex-row gap-2',
        variants[variant],
        sizes[size].container,
        (isDisabled || isLoading) && 'opacity-50',
        className,
      )}
    >
      {isLoading && <ActivityIndicator size="small" color="white" />}
      <Text variant="label" className={cn(sizes[size].text, variant === 'primary' && 'text-white')}>
        {children}
      </Text>
    </AnimatedPressable>
  )
}
```

3. `components/ui/Input.tsx` — Form input:
```tsx
import { TextInput, TextInputProps, View } from 'react-native'
import { useRef } from 'react'
import { Text } from './Text'

interface InputProps extends TextInputProps {
  label?: string
  error?: string
  hint?: string
  leftIcon?: React.ReactNode
  rightIcon?: React.ReactNode
}

export function Input({ label, error, hint, leftIcon, rightIcon, className, ...props }: InputProps) {
  return (
    <View className="gap-1.5">
      {label && <Text variant="label" color="secondary">{label}</Text>}
      <View className={cn(
        'flex-row items-center h-12 px-4 rounded-xl border',  // 48dp height
        'bg-white dark:bg-gray-900',
        error ? 'border-red-500' : 'border-gray-300 dark:border-gray-600',
        'focus-within:border-primary',
      )}>
        {leftIcon}
        <TextInput
          className="flex-1 text-[17px] text-gray-900 dark:text-gray-50"
          placeholderTextColor="#9CA3AF"
          accessibilityLabel={label}
          {...props}
        />
        {rightIcon}
      </View>
      {error && <Text variant="caption" color="error">{error}</Text>}
      {!error && hint && <Text variant="caption" color="muted">{hint}</Text>}
    </View>
  )
}
```

4. `components/ui/Card.tsx` — Surface card:
```tsx
import { View, ViewProps, Platform, StyleSheet } from 'react-native'
import { cn } from '@/lib/utils/cn'

interface CardProps extends ViewProps {
  elevated?: boolean
}

export function Card({ elevated = true, className, style, ...props }: CardProps) {
  return (
    <View
      className={cn('bg-white dark:bg-gray-900 rounded-2xl overflow-hidden', className)}
      style={[elevated && styles.shadow, style]}
      {...props}
    />
  )
}

const styles = StyleSheet.create({
  shadow: Platform.select({
    ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.08, shadowRadius: 8 },
    android: { elevation: 3 },
  })!,
})
```

5. `components/ui/Skeleton.tsx` — Content loading placeholder:
```tsx
import { View } from 'react-native'
import { MotiView } from 'moti'  // npm install moti

export function Skeleton({ width, height, borderRadius = 8, className }: {
  width: number | string
  height: number
  borderRadius?: number
  className?: string
}) {
  return (
    <MotiView
      from={{ opacity: 0.3 }}
      animate={{ opacity: 0.8 }}
      transition={{ type: 'timing', duration: 800, loop: true, repeatReverse: true }}
      style={{ width, height, borderRadius }}
      className={cn('bg-gray-200 dark:bg-gray-700', className)}
    />
  )
}

// Usage: <Skeleton width="100%" height={80} />
```
```

✅ **Verification Checklist:**
- [ ] All button touch targets are ≥44pt height.
- [ ] Dark mode colors render correctly without brightness inversion.
- [ ] Button scale animation plays smoothly at 60fps.
- [ ] Input shows error state in red border + red text below.
- [ ] Skeleton pulses at correct speed without JS thread blocking.

---

### Prompt M6.2: Reanimated 3 — Animation Patterns

```text
You are a React Native Animation Expert. Implement production-grade animations for [AppName].

Rules:
- ALL animations must run on the UI thread (Reanimated 3 worklets).
- NEVER use the legacy `Animated` API for new code.
- Respect `AccessibilityInfo.isReduceMotionEnabled()` — skip animations if true.

Required Output Format: Provide complete code for:

1. Fade + slide entrance animation:
```tsx
import Animated, { FadeInDown, FadeOutUp } from 'react-native-reanimated'

// Entering prop — auto animates on mount
<Animated.View entering={FadeInDown.delay(100).duration(400).springify()}>
  <Card>...</Card>
</Animated.View>
```

2. Shared element transition (React Native 0.76+ New Architecture):
```tsx
import { SharedTransition, withSpring } from 'react-native-reanimated'

const customTransition = SharedTransition.custom((values) => {
  'worklet'
  return {
    width: withSpring(values.targetWidth),
    height: withSpring(values.targetHeight),
  }
})

// On list screen:
<Animated.Image sharedTransitionTag={`product-image-${id}`} source={...} />

// On detail screen:
<Animated.Image sharedTransitionTag={`product-image-${id}`} source={...} />
```

3. Gesture-driven swipe-to-delete:
```tsx
import { Swipeable } from 'react-native-gesture-handler'
import Animated, { useAnimatedStyle, interpolate, runOnJS } from 'react-native-reanimated'

export function SwipeableRow({ onDelete, children }: SwipeableRowProps) {
  const renderRightAction = (progress: Animated.SharedValue<number>) => {
    const animStyle = useAnimatedStyle(() => ({
      opacity: interpolate(progress.value, [0, 1], [0, 1]),
      transform: [{ translateX: interpolate(progress.value, [0, 1], [80, 0]) }],
    }))

    return (
      <Animated.View style={animStyle} className="bg-red-500 justify-center px-5">
        <Text className="text-white font-semibold">Delete</Text>
      </Animated.View>
    )
  }

  return (
    <Swipeable renderRightActions={renderRightAction} onSwipeableOpen={() => onDelete()}>
      {children}
    </Swipeable>
  )
}
```

4. Pull-to-refresh with custom animation:
```tsx
import { RefreshControl, ScrollView } from 'react-native'
import { useState, useCallback } from 'react'

export function RefreshableScrollView({ onRefresh, children }: Props) {
  const [refreshing, setRefreshing] = useState(false)

  const handleRefresh = useCallback(async () => {
    setRefreshing(true)
    await onRefresh()
    setRefreshing(false)
  }, [onRefresh])

  return (
    <ScrollView
      refreshControl={
        <RefreshControl
          refreshing={refreshing}
          onRefresh={handleRefresh}
          tintColor="#6366F1"       // iOS spinner color
          colors={['#6366F1']}       // Android spinner colors
        />
      }
    >
      {children}
    </ScrollView>
  )
}
```
```

✅ **Verification Checklist:**
- [ ] All animations run at 60fps (verify with Flipper or React DevTools Profiler).
- [ ] Animations disabled when `AccessibilityInfo.isReduceMotionEnabled()` returns true.
- [ ] Swipe-to-delete requires a deliberate swipe (not accidental tap).
- [ ] Pull-to-refresh completes and updates data visibly.

---

📎 **Related Phases:**
- Prerequisites: [Phase M5: Authentication](./MOBILE_PHASE_5_AUTHENTICATION_SECURITY_Security_Expert.md)
- Proceeds to: [Phase M7: Native Features](./MOBILE_PHASE_7_NATIVE_FEATURES_APIs_Mobile_Developer.md)
