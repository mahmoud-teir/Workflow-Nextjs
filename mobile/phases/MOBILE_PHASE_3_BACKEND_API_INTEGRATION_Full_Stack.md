<a name="phase-m3"></a>
# 📌 MOBILE PHASE M3: BACKEND & API INTEGRATION (Full-Stack Developer)

> **Backend Options:** Supabase (recommended for rapid dev) · Convex · Custom REST API (Next.js / FastAPI) · tRPC (type-safe end-to-end)

---

### Prompt M3.1: API Client Setup (Axios + Interceptors)

```text
You are a Senior React Native API Engineer. Build a resilient API client with automatic token refresh, retry logic, and error normalization.

Backend: [Supabase REST / Custom REST API / GraphQL / tRPC]
Auth: JWT Bearer tokens stored in expo-secure-store

Constraints:
- All API errors must be normalized into a consistent `ApiError` type.
- Token refresh must happen automatically — the user should never see an auth error for an expired token.
- Network errors must be handled gracefully (show offline state, not raw errors).
- All requests must include correlation IDs for debugging.

Required Output Format: Provide complete code for:

1. `lib/api/client.ts` — Axios instance with interceptors:
```typescript
import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { getToken, setToken, clearTokens } from '@/lib/auth/token'
import { config } from '@/lib/config'

let isRefreshing = false
let refreshQueue: Array<(token: string) => void> = []

export const apiClient: AxiosInstance = axios.create({
  baseURL: config.apiUrl,
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor — attach token
apiClient.interceptors.request.use(async (req: InternalAxiosRequestConfig) => {
  const token = await getToken('access_token')
  if (token) req.headers.Authorization = `Bearer ${token}`
  req.headers['X-Correlation-ID'] = generateId()
  return req
})

// Response interceptor — handle 401 + refresh
apiClient.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config

    if (error.response?.status === 401 && !original._retry) {
      original._retry = true

      if (isRefreshing) {
        return new Promise((resolve) => {
          refreshQueue.push((token) => {
            original.headers.Authorization = `Bearer ${token}`
            resolve(apiClient(original))
          })
        })
      }

      isRefreshing = true
      try {
        const newToken = await refreshAccessToken()
        await setToken('access_token', newToken)
        refreshQueue.forEach((cb) => cb(newToken))
        refreshQueue = []
        original.headers.Authorization = `Bearer ${newToken}`
        return apiClient(original)
      } catch {
        await clearTokens()
        // Navigate to login
        throw new ApiError('Session expired', 'AUTH_EXPIRED', 401)
      } finally {
        isRefreshing = false
      }
    }

    throw normalizeError(error)
  }
)
```

2. `lib/api/errors.ts` — Normalized error types:
```typescript
export class ApiError extends Error {
  constructor(
    message: string,
    public code: string,
    public statusCode: number,
    public details?: unknown,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export function normalizeError(error: unknown): ApiError {
  if (error instanceof ApiError) return error

  if (axios.isAxiosError(error)) {
    const status = error.response?.status ?? 0
    const data = error.response?.data

    if (!error.response) {
      return new ApiError('Network error — check your connection', 'NETWORK_ERROR', 0)
    }

    return new ApiError(
      data?.message ?? error.message,
      data?.code ?? `HTTP_${status}`,
      status,
      data?.details,
    )
  }

  return new ApiError('Unknown error', 'UNKNOWN', 500)
}
```

3. `lib/api/hooks/useQuery.ts` — TanStack Query wrapper with error handling:
```typescript
import { useQuery, useMutation, UseQueryOptions } from '@tanstack/react-query'
import { ApiError } from '@/lib/api/errors'

// Typed query hook with error normalization
export function useApiQuery<T>(
  key: string[],
  fetcher: () => Promise<T>,
  options?: UseQueryOptions<T, ApiError>,
) {
  return useQuery<T, ApiError>({
    queryKey: key,
    queryFn: fetcher,
    retry: (failureCount, error) => {
      // Don't retry on auth or 4xx errors
      if (error.statusCode >= 400 && error.statusCode < 500) return false
      return failureCount < 3
    },
    ...options,
  })
}
```

⚠️ Common Pitfalls:
- Pitfall: Multiple 401 → refresh calls happening in parallel (race condition).
- Solution: Use the `isRefreshing` flag + `refreshQueue` pattern shown above.
- Pitfall: Axios timeout not catching slow responses on mobile networks.
- Solution: Set `timeout: 15_000` and handle `ECONNABORTED` error code.
```

✅ **Verification Checklist:**
- [ ] Request includes `Authorization: Bearer [token]` header.
- [ ] Expired token triggers auto-refresh without user seeing error.
- [ ] Network error shows offline banner (not raw error).
- [ ] API errors are normalized to `ApiError` with consistent structure.

---

### Prompt M3.2: Supabase Integration (Recommended Backend)

```text
You are a Supabase + React Native Integration Specialist. Set up Supabase as the backend for [AppName].

Supabase Services Used:
- Authentication (email/OAuth)
- PostgreSQL Database (via Supabase client)
- Real-time subscriptions
- Storage (user avatars, images)

Constraints:
- Use `@supabase/supabase-js` v2 (compatible with React Native via `react-native-url-polyfill`).
- Store Supabase session tokens in `expo-secure-store`, NOT AsyncStorage.
- Use Row Level Security (RLS) on all tables — users must not access other users' data.

Required Output Format: Provide complete code for:

1. Setup:
```bash
npx expo install @supabase/supabase-js
npx expo install react-native-url-polyfill
```

2. `lib/db/supabase.ts`:
```typescript
import 'react-native-url-polyfill/auto'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { createClient } from '@supabase/supabase-js'
import * as SecureStore from 'expo-secure-store'

// Custom storage adapter using SecureStore for tokens
const SecureStoreAdapter = {
  getItem: (key: string) => SecureStore.getItemAsync(key),
  setItem: (key: string, value: string) => SecureStore.setItemAsync(key, value),
  removeItem: (key: string) => SecureStore.deleteItemAsync(key),
}

export const supabase = createClient(
  process.env.EXPO_PUBLIC_SUPABASE_URL!,
  process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY!,
  {
    auth: {
      storage: SecureStoreAdapter,
      autoRefreshToken: true,
      persistSession: true,
      detectSessionInUrl: false, // Required for React Native
    },
  }
)
```

3. Real-time subscription example:
```typescript
import { useEffect } from 'react'
import { supabase } from '@/lib/db/supabase'
import { useQueryClient } from '@tanstack/react-query'

export function useRealtimeMessages(channelId: string) {
  const queryClient = useQueryClient()

  useEffect(() => {
    const channel = supabase
      .channel(`messages:${channelId}`)
      .on('postgres_changes', {
        event: 'INSERT',
        schema: 'public',
        table: 'messages',
        filter: `channel_id=eq.${channelId}`,
      }, (payload) => {
        queryClient.invalidateQueries({ queryKey: ['messages', channelId] })
      })
      .subscribe()

    return () => { supabase.removeChannel(channel) }
  }, [channelId, queryClient])
}
```

4. RLS policy example (SQL):
```sql
-- Users can only read their own data
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own profile"
  ON profiles FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Users can update own profile"
  ON profiles FOR UPDATE
  USING (auth.uid() = user_id);
```

⚠️ Common Pitfalls:
- Pitfall: Using default AsyncStorage adapter for Supabase in React Native — tokens stored insecurely.
- Solution: Always use the SecureStoreAdapter shown above.
- Pitfall: Missing `detectSessionInUrl: false` — causes errors in React Native.
- Solution: Always set this to false in React Native projects.
```

✅ **Verification Checklist:**
- [ ] Supabase session tokens stored in SecureStore (not AsyncStorage).
- [ ] RLS enabled on all user-data tables.
- [ ] Real-time subscription cleaned up with `supabase.removeChannel()`.
- [ ] `EXPO_PUBLIC_SUPABASE_URL` and `ANON_KEY` are in `.env` (not hardcoded).

---

### Prompt M3.3: TanStack Query — Data Fetching Architecture

```text
You are a React Native Data Layer Architect. Set up TanStack Query v5 for resilient, cached data fetching.

Constraints:
- All server data must go through TanStack Query — no manual useState + useEffect for fetching.
- Use query key factories for cache management.
- Implement optimistic updates for mutations.
- Configure proper stale time for mobile (network is expensive — don't over-fetch).

Required Output Format: Provide complete code for:

1. QueryClient configuration `lib/api/client.ts`:
```typescript
import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,        // 5 minutes — avoid over-fetching on mobile
      gcTime: 10 * 60 * 1000,           // 10 minutes cache retention
      retry: (count, error: ApiError) => {
        if (error.statusCode >= 400 && error.statusCode < 500) return false
        return count < 2
      },
      refetchOnWindowFocus: false,       // Not relevant for mobile
      refetchOnReconnect: true,          // Refetch when back online
    },
    mutations: {
      retry: 0,
    },
  },
})
```

2. Query key factory pattern:
```typescript
// lib/api/keys.ts
export const queryKeys = {
  profile: {
    all: ['profile'] as const,
    detail: (userId: string) => ['profile', userId] as const,
  },
  posts: {
    all: ['posts'] as const,
    list: (filters: PostFilters) => ['posts', 'list', filters] as const,
    detail: (id: string) => ['posts', id] as const,
  },
}
```

3. Custom hook with optimistic update:
```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/lib/api/keys'

export function useLikePost(postId: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => apiClient.post(`/posts/${postId}/like`),

    // Optimistic update — instant UI feedback
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: queryKeys.posts.detail(postId) })
      const previous = queryClient.getQueryData(queryKeys.posts.detail(postId))

      queryClient.setQueryData(queryKeys.posts.detail(postId), (old: Post) => ({
        ...old,
        likeCount: old.likeCount + 1,
        isLiked: true,
      }))

      return { previous }
    },

    // Rollback on error
    onError: (err, _, context) => {
      queryClient.setQueryData(queryKeys.posts.detail(postId), context?.previous)
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.posts.detail(postId) })
    },
  })
}
```

4. Infinite scroll hook (FlashList):
```typescript
export function useInfinitePosts(filters: PostFilters) {
  return useInfiniteQuery({
    queryKey: queryKeys.posts.list(filters),
    queryFn: ({ pageParam = 0 }) => fetchPosts({ ...filters, offset: pageParam }),
    initialPageParam: 0,
    getNextPageParam: (lastPage, pages) => {
      if (lastPage.items.length < 20) return undefined  // No more pages
      return pages.length * 20
    },
  })
}
```
```

✅ **Verification Checklist:**
- [ ] Data is cached for 5+ minutes (not re-fetched on every screen mount).
- [ ] Optimistic like/toggle updates are instant (no waiting for server).
- [ ] Infinite scroll loads next page on reaching list bottom.
- [ ] `refetchOnReconnect: true` re-fetches stale data when network restores.

---

📎 **Related Phases:**
- Prerequisites: [Phase M2: Navigation](./MOBILE_PHASE_2_NAVIGATION_ARCHITECTURE_Mobile_Developer.md)
- Proceeds to: [Phase M4: Database & Offline Storage](./MOBILE_PHASE_4_DATABASE_OFFLINE_STORAGE_Mobile_Architect.md)
