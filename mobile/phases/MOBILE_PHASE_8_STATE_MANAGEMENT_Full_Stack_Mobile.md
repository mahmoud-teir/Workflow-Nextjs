<a name="phase-m8"></a>
# 📌 MOBILE PHASE M8: STATE MANAGEMENT (Full-Stack Mobile Developer)

> **Golden Rule:** Use the right state type for the right job. Server state ≠ UI state ≠ Persistent state ≠ Form state.

---

## 🗂️ State Management Decision Matrix

| State Type | Tool | Example |
|-----------|------|---------|
| **Server state** (remote data) | TanStack Query | Posts, user profiles, API responses |
| **Global UI state** | Zustand | Theme, modal open/close, sidebar, navigation state |
| **Persistent state** | Zustand + MMKV | Preferences, onboarding flag, draft content |
| **Sensitive state** | Zustand + SecureStore | Auth session, tokens |
| **Local component state** | `useState` | Input values, loading indicators |
| **Form state** | React Hook Form | Multi-field forms with validation |
| **Derived state** | Computed (no store) | Filtered lists, calculated totals |

---

### Prompt M8.1: Zustand — Global State Architecture

```text
You are a React Native State Architect. Design and implement the Zustand state architecture for [AppName].

State requirements:
- Auth state (user, session, logout)
- UI state (theme, active modal, loading flags)
- [Feature-specific state from PRD]

Constraints:
- Each store should have a SINGLE responsibility.
- Persist state using MMKV adapter (from Phase M4).
- Use Zustand devtools in development for debugging.
- Prefer selectors over subscribing to entire store (avoid unnecessary re-renders).

Required Output Format: Provide complete code for:

1. Auth store `lib/store/auth.ts`:
```typescript
import { create } from 'zustand'
import { persist, createJSONStorage, devtools } from 'zustand/middleware'
import { clearTokens } from '@/lib/auth/token'
import { MMKVZustandStorage } from '@/lib/storage/mmkv'

interface User {
  id: string
  email: string
  name: string
  avatarUrl?: string
  role: 'user' | 'admin'
}

interface AuthState {
  // State
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  // Actions
  setUser: (user: User) => void
  updateUser: (updates: Partial<User>) => void
  logout: () => Promise<void>
  setLoading: (loading: boolean) => void
}

export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      (set) => ({
        user: null,
        isAuthenticated: false,
        isLoading: false,

        setUser: (user) => set({ user, isAuthenticated: true }, false, 'setUser'),

        updateUser: (updates) =>
          set((state) => ({
            user: state.user ? { ...state.user, ...updates } : null,
          }), false, 'updateUser'),

        logout: async () => {
          await clearTokens()
          set({ user: null, isAuthenticated: false }, false, 'logout')
        },

        setLoading: (isLoading) => set({ isLoading }, false, 'setLoading'),
      }),
      {
        name: 'auth-store',
        storage: createJSONStorage(() => MMKVZustandStorage),
        // Only persist non-sensitive fields
        partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
      }
    ),
    { name: 'AuthStore' }
  )
)

// Typed selectors to avoid re-renders
export const useUser = () => useAuthStore((s) => s.user)
export const useIsAuthenticated = () => useAuthStore((s) => s.isAuthenticated)
```

2. UI state store `lib/store/ui.ts`:
```typescript
import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import { ColorSchemeName } from 'react-native'

interface UIState {
  theme: 'light' | 'dark' | 'system'
  setTheme: (theme: UIState['theme']) => void
  activeModal: string | null
  openModal: (id: string) => void
  closeModal: () => void
  toasts: Toast[]
  showToast: (toast: Omit<Toast, 'id'>) => void
  dismissToast: (id: string) => void
}

interface Toast {
  id: string
  message: string
  type: 'success' | 'error' | 'info' | 'warning'
  duration?: number
}

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      theme: 'system',
      setTheme: (theme) => set({ theme }),
      activeModal: null,
      openModal: (id) => set({ activeModal: id }),
      closeModal: () => set({ activeModal: null }),
      toasts: [],
      showToast: (toast) =>
        set((state) => ({
          toasts: [...state.toasts, { ...toast, id: generateUUID() }],
        })),
      dismissToast: (id) =>
        set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) })),
    }),
    {
      name: 'ui-store',
      storage: createJSONStorage(() => MMKVZustandStorage),
      partialize: (state) => ({ theme: state.theme }),  // Only persist theme
    }
  )
)

export const useTheme = () => useUIStore((s) => s.theme)
export const useToast = () => ({
  show: useUIStore((s) => s.showToast),
  dismiss: useUIStore((s) => s.dismissToast),
  toasts: useUIStore((s) => s.toasts),
})
```

3. Feature-specific store example `lib/store/feed.ts`:
```typescript
interface FeedState {
  activeFilter: 'all' | 'following' | 'trending'
  setFilter: (filter: FeedState['activeFilter']) => void
  scrollPosition: number
  setScrollPosition: (y: number) => void
}

export const useFeedStore = create<FeedState>()((set) => ({
  activeFilter: 'all',
  setFilter: (activeFilter) => set({ activeFilter }),
  scrollPosition: 0,
  setScrollPosition: (scrollPosition) => set({ scrollPosition }),
}))
```

⚠️ Common Pitfalls:
- Pitfall: Subscribing to the entire Zustand store (`useStore()`) — every state change causes a re-render.
- Solution: Always use selectors (`useStore((s) => s.specificField)`).
- Pitfall: Persisting sensitive data (tokens, passwords) in Zustand + MMKV — MMKV is not encrypted by default.
- Solution: Persist tokens ONLY in SecureStore. Use `partialize` to exclude sensitive fields from MMKV persistence.
```

✅ **Verification Checklist:**
- [ ] Theme persists across app restarts.
- [ ] Logout clears auth store AND SecureStore tokens.
- [ ] Toast notifications appear and auto-dismiss.
- [ ] Store selectors are used (not full store subscriptions).

---

### Prompt M8.2: TanStack Query — Server State Patterns

```text
You are a React Native Server State Engineer. Implement TanStack Query v5 patterns for [AppName].

Required Output Format: Provide complete code for:

1. QueryClient singleton `lib/api/queryClient.ts`:
```typescript
import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      gcTime: 10 * 60 * 1000,
      refetchOnWindowFocus: false,
      refetchOnReconnect: true,
      retry: (count, error: ApiError) => {
        if (error.statusCode >= 400 && error.statusCode < 500) return false
        return count < 2
      },
    },
  },
})
```

2. Query hooks for a feature `lib/api/hooks/usePosts.ts`:
```typescript
import { useQuery, useMutation, useQueryClient, useInfiniteQuery } from '@tanstack/react-query'
import { queryKeys } from '@/lib/api/keys'
import { apiClient } from '@/lib/api/client'
import { useToast } from '@/lib/store/ui'
import { haptics } from '@/lib/native/haptics'

// Fetch post list with infinite scroll
export function useInfinitePosts(filter: string) {
  return useInfiniteQuery({
    queryKey: queryKeys.posts.list({ filter }),
    queryFn: ({ pageParam = 0 }) =>
      apiClient.get('/posts', { params: { offset: pageParam, filter } }).then((r) => r.data),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) =>
      lastPage.hasMore ? allPages.length * 20 : undefined,
  })
}

// Fetch single post
export function usePost(id: string) {
  return useQuery({
    queryKey: queryKeys.posts.detail(id),
    queryFn: () => apiClient.get(`/posts/${id}`).then((r) => r.data),
    enabled: !!id,
  })
}

// Create post with optimistic update
export function useCreatePost() {
  const queryClient = useQueryClient()
  const { show } = useToast()

  return useMutation({
    mutationFn: (data: NewPost) => apiClient.post('/posts', data).then((r) => r.data),

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.posts.all })
      haptics.success()
      show({ message: 'Post created!', type: 'success' })
    },

    onError: (error: ApiError) => {
      haptics.error()
      show({ message: error.message, type: 'error' })
    },
  })
}
```

3. Offline-aware query (uses local SQLite as fallback):
```typescript
export function useOfflineAwarePosts() {
  const [localPosts, setLocalPosts] = useState<Post[]>([])
  const { isOnline } = useNetworkStatus()
  const query = useInfinitePosts('all')

  // Load from SQLite when offline
  useEffect(() => {
    if (!isOnline) {
      getPosts().then(setLocalPosts)
    }
  }, [isOnline])

  return {
    posts: isOnline
      ? query.data?.pages.flatMap((p) => p.items) ?? []
      : localPosts,
    isLoading: isOnline ? query.isLoading : false,
    fetchNextPage: isOnline ? query.fetchNextPage : undefined,
    hasNextPage: isOnline ? query.hasNextPage : false,
  }
}
```

4. React Hook Form integration for mutations:
```typescript
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

const CreatePostSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters'),
  body: z.string().min(10, 'Body must be at least 10 characters'),
})

type CreatePostData = z.infer<typeof CreatePostSchema>

export function CreatePostForm() {
  const { mutate, isPending } = useCreatePost()

  const { control, handleSubmit, formState: { errors } } = useForm<CreatePostData>({
    resolver: zodResolver(CreatePostSchema),
  })

  return (
    <View className="gap-4">
      <Controller
        control={control}
        name="title"
        render={({ field: { onChange, value } }) => (
          <Input
            label="Title"
            value={value}
            onChangeText={onChange}
            error={errors.title?.message}
          />
        )}
      />
      <Button onPress={handleSubmit((data) => mutate(data))} isLoading={isPending}>
        Create Post
      </Button>
    </View>
  )
}
```
```

✅ **Verification Checklist:**
- [ ] Data loads from cache when navigating back to a screen (no spinner flash).
- [ ] Creating/updating a post invalidates the list query.
- [ ] Offline mode shows cached data from SQLite.
- [ ] Form validation prevents submission with invalid data.

---

📎 **Related Phases:**
- Prerequisites: [Phase M7: Native Features](./MOBILE_PHASE_7_NATIVE_FEATURES_APIs_Mobile_Developer.md)
- Proceeds to: [Phase M9: Testing & QA](./MOBILE_PHASE_9_TESTING_QA_QA_Engineer.md)
