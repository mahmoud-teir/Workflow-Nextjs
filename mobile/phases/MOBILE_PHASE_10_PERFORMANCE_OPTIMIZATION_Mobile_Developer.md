<a name="phase-m10"></a>
# 📌 MOBILE PHASE M10: PERFORMANCE OPTIMIZATION (Mobile Developer)

> **Performance Budget:** 60fps animations · <3s cold start · <50MB JS bundle · <200MB memory usage

---

### Prompt M10.1: List Performance — FlashList & FlatList

```text
You are a React Native Performance Engineer. Optimize list rendering for [AppName].

Performance rules:
- Replace ALL ScrollView-with-map with FlatList or FlashList.
- FlashList (Shopify) is 10x faster than FlatList for most use cases.
- Never render more than what's visible — use windowed rendering.

Required Output Format: Provide complete code for:

1. FlashList installation:
```bash
npx expo install @shopify/flash-list
```

2. FlashList implementation (vs FlatList):
```tsx
import { FlashList } from '@shopify/flash-list'

// ✅ CORRECT — FlashList with proper estimatedItemSize
export function PostList({ posts }: { posts: Post[] }) {
  const renderItem = useCallback(({ item }: { item: Post }) => (
    <PostCard post={item} />
  ), [])

  const keyExtractor = useCallback((item: Post) => item.id, [])

  return (
    <FlashList
      data={posts}
      renderItem={renderItem}
      keyExtractor={keyExtractor}
      estimatedItemSize={120}          // Critical: must match actual item height
      showsVerticalScrollIndicator={false}
      contentContainerStyle={{ paddingBottom: 100 }}
      ItemSeparatorComponent={() => <View className="h-px bg-gray-100 dark:bg-gray-800" />}
      ListEmptyComponent={<EmptyState />}
      ListFooterComponent={isLoading ? <LoadingFooter /> : null}
      onEndReached={fetchNextPage}
      onEndReachedThreshold={0.3}      // Load more when 30% from bottom
    />
  )
}
```

3. Infinite scroll FlashList:
```tsx
export function InfinitePostList() {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage } = useInfinitePosts('all')
  const posts = useMemo(() => data?.pages.flatMap((p) => p.items) ?? [], [data])

  return (
    <FlashList
      data={posts}
      renderItem={({ item }) => <PostCard post={item} />}
      estimatedItemSize={120}
      onEndReached={() => { if (hasNextPage) fetchNextPage() }}
      onEndReachedThreshold={0.5}
      ListFooterComponent={isFetchingNextPage ? (
        <ActivityIndicator className="py-4" />
      ) : null}
    />
  )
}
```

4. Image list optimization with `expo-image`:
```tsx
import { Image } from 'expo-image'  // NOT from react-native

// expo-image features:
// - Disk + memory caching
// - Blurhash placeholder
// - Progressive loading
// - Better performance than RN Image

const blurhash = '|rF?hV%2WCj[ayj[a|j[az_NaeWBj@ayfRayfQfQM{M|azj[azf6fQfQfQIpWXofj[ayj[j[fQayWCoeoeaya}j[ayfQa{oLj?j[WVj[ayayj[fQoff7azayj[ayj[j[ayofayayayj[fQj[ayayj[ayfjj[j[ayjuayj['

<Image
  source={post.imageUrl}
  placeholder={blurhash}
  contentFit="cover"
  transition={200}
  style={{ width: '100%', height: 200, borderRadius: 12 }}
  cachePolicy="memory-disk"
/>
```

⚠️ Common Pitfalls:
- Pitfall: Not setting `estimatedItemSize` on FlashList — causes layout jumps.
- Solution: Measure your actual average item height and use that value.
- Pitfall: Creating new functions inside renderItem on each render.
- Solution: Wrap renderItem in `useCallback` and item components in `React.memo`.
```

✅ **Verification Checklist:**
- [ ] Lists render at 60fps (no dropped frames in React DevTools profiler).
- [ ] Infinite scroll loads next page smoothly.
- [ ] Images show blurhash placeholder while loading.
- [ ] No `ScrollView` with `.map()` patterns in the codebase.

---

### Prompt M10.2: Re-render Optimization

```text
You are a React Native Re-render Optimization Specialist. Eliminate unnecessary re-renders in [AppName].

Required Output Format: Provide complete code and analysis for:

1. React.memo for list items:
```tsx
// ❌ WRONG — re-renders on every parent render
function PostCard({ post }: { post: Post }) { ... }

// ✅ CORRECT — only re-renders when post changes
export const PostCard = React.memo(function PostCard({ post, onPress }: {
  post: Post
  onPress: (id: string) => void
}) {
  return (
    <Pressable onPress={() => onPress(post.id)}>
      ...
    </Pressable>
  )
}, (prev, next) => {
  // Custom equality — only re-render if these fields change
  return prev.post.id === next.post.id && prev.post.updatedAt === next.post.updatedAt
})
```

2. useMemo for expensive computations:
```tsx
// ❌ WRONG — filtered list recalculated on every render
const filtered = posts.filter((p) => p.category === activeFilter)

// ✅ CORRECT — only recalculates when dependencies change
const filtered = useMemo(
  () => posts.filter((p) => p.category === activeFilter),
  [posts, activeFilter]
)
```

3. useCallback for event handlers:
```tsx
// ❌ WRONG — new function reference on every render (breaks React.memo)
<PostCard onPress={(id) => router.push(`/post/${id}`)} />

// ✅ CORRECT — stable function reference
const handlePress = useCallback((id: string) => {
  router.push(`/post/${id}`)
}, [router])
<PostCard onPress={handlePress} />
```

4. Zustand selector optimization:
```tsx
// ❌ WRONG — re-renders when ANY store property changes
const { user, isAuthenticated, logout } = useAuthStore()

// ✅ CORRECT — re-renders only when user changes
const user = useAuthStore((s) => s.user)
const logout = useAuthStore((s) => s.logout)
```

5. Profiling with React DevTools:
```bash
# Enable profiling in development
npx react-devtools

# In your app, shake device → Performance Monitor
# Look for: JS Frame Rate (should be 60fps)
# Look for: UI Frame Rate (should be 60fps)
```
```

✅ **Verification Checklist:**
- [ ] React DevTools Profiler shows no unnecessary re-renders on scroll.
- [ ] List item components wrapped in `React.memo`.
- [ ] All event handlers in list items wrapped in `useCallback`.
- [ ] Zustand selectors used for all store subscriptions.

---

### Prompt M10.3: App Startup Performance

```text
You are a React Native Startup Performance Engineer. Optimize [AppName]'s cold start time to <3 seconds.

Required Output Format: Provide complete code for:

1. Splash screen management (avoid layout flash):
```typescript
import * as SplashScreen from 'expo-splash-screen'

// In app/_layout.tsx — prevent auto-hide
SplashScreen.preventAutoHideAsync()

export default function RootLayout() {
  const [appReady, setAppReady] = useState(false)

  useEffect(() => {
    async function prepare() {
      try {
        // Load fonts, check auth, initialize DB
        await Promise.all([
          loadFonts(),
          initializeDatabase(),
          checkAuthSession(),
        ])
      } finally {
        setAppReady(true)
        SplashScreen.hideAsync()
      }
    }
    prepare()
  }, [])

  if (!appReady) return null  // Keep splash screen visible
  return <Stack />
}
```

2. Lazy-load heavy screens:
```tsx
import { lazy, Suspense } from 'react'

// Defer loading heavy screens until needed
const MapScreen = lazy(() => import('@/app/(tabs)/map'))
const CameraScreen = lazy(() => import('@/app/camera'))

// Usage with Suspense
<Suspense fallback={<ScreenSkeleton />}>
  <MapScreen />
</Suspense>
```

3. Bundle analysis with `expo-bundle-analyzer`:
```bash
# Install and run
npm install --save-dev expo-bundle-analyzer

# Add to package.json
"scripts": {
  "analyze": "EXPO_BUNDLE_ANALYZER=true npx expo export"
}

# Run
npm run analyze
```

4. Hermes engine verification:
```json
// app.json — Hermes should be enabled by default in SDK 52+
{
  "expo": {
    "android": { "jsEngine": "hermes" },
    "ios": { "jsEngine": "hermes" }
  }
}
```

5. Image optimization checklist:
- [ ] All app icon and splash images in correct dimensions
- [ ] Use `expo-image` with `cachePolicy: "memory-disk"` everywhere
- [ ] Lazy load images below the fold
- [ ] Use WebP format for all images (supported by `expo-image`)
```

✅ **Verification Checklist:**
- [ ] Cold start < 3 seconds on a mid-range Android device.
- [ ] No layout flash between splash and first screen.
- [ ] Hermes engine enabled in production builds.
- [ ] JS bundle < 50MB (verify with `expo export --platform android`).

---

📎 **Related Phases:**
- Prerequisites: [Phase M9: Testing & QA](./MOBILE_PHASE_9_TESTING_QA_QA_Engineer.md)
- Proceeds to: [Phase M11: Push Notifications & Analytics](./MOBILE_PHASE_11_PUSH_NOTIFICATIONS_ANALYTICS_Product_Engineer.md)
