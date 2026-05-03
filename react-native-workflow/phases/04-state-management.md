# Phase 4: State Management Setup

## Overview
Implement a scalable and maintainable state management solution using Zustand (recommended) or Redux Toolkit for global state, combined with React Query for server state.

## Key Activities

### 4.1 Choose State Management Strategy

#### Recommended Stack
- **Zustand**: For client/global state (simple, lightweight, no boilerplate)
- **TanStack Query (React Query)**: For server state (caching, background updates, optimistic updates)
- **React Context**: For infrequently changing values (theme, auth status)

### 4.2 Install Dependencies
```bash
# Zustand for global state
npm install zustand

# TanStack Query for server state
npm install @tanstack/react-query

# Optional: Persist middleware for AsyncStorage
npm install zustand-persist @react-native-async-storage/async-storage
```

### 4.3 Zustand Store Setup

Create `src/store/useAppStore.ts`:
```typescript
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import type { User, AppState } from '../types';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  updateProfile: (updates: Partial<User>) => void;
}

interface UIState {
  theme: 'light' | 'dark' | 'system';
  setTheme: (theme: 'light' | 'dark' | 'system') => void;
  isSidebarOpen: boolean;
  toggleSidebar: () => void;
}

interface NotificationState {
  notifications: Array<{ id: string; message: string; read: boolean }>;
  unreadCount: number;
  addNotification: (notification: { id: string; message: string }) => void;
  markAsRead: (id: string) => void;
  clearAll: () => void;
}

type AppStore = AuthState & UIState & NotificationState;

export const useAppStore = create<AppStore>()(
  persist(
    (set, get) => ({
      // Auth State
      user: null,
      token: null,
      isAuthenticated: false,
      
      login: (token, user) => set({ token, user, isAuthenticated: true }),
      logout: () => set({ token: null, user: null, isAuthenticated: false }),
      updateProfile: (updates) => {
        const user = get().user;
        if (user) {
          set({ user: { ...user, ...updates } });
        }
      },

      // UI State
      theme: 'system',
      setTheme: (theme) => set({ theme }),
      isSidebarOpen: false,
      toggleSidebar: () => set((state) => ({ isSidebarOpen: !state.isSidebarOpen })),

      // Notification State
      notifications: [],
      unreadCount: 0,
      addNotification: (notification) =>
        set((state) => ({
          notifications: [
            { ...notification, read: false },
            ...state.notifications,
          ],
          unreadCount: state.unreadCount + 1,
        })),
      markAsRead: (id) =>
        set((state) => ({
          notifications: state.notifications.map((n) =>
            n.id === id ? { ...n, read: true } : n
          ),
          unreadCount: Math.max(0, state.unreadCount - 1),
        })),
      clearAll: () => set({ notifications: [], unreadCount: 0 }),
    }),
    {
      name: 'app-storage',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (state) => ({
        token: state.token,
        user: state.user,
        theme: state.theme,
      }),
    }
  )
);

// Selectors for performance optimization
export const selectIsAuthenticated = (state: AppStore) => state.isAuthenticated;
export const selectCurrentUser = (state: AppStore) => state.user;
export const selectUnreadCount = (state: AppStore) => state.unreadCount;
```

### 4.4 React Query Setup

Create `src/services/queryClient.ts`:
```typescript
import { QueryClient } from '@tanstack/react-query';
import { Platform } from 'react-native';
import * as Network from 'expo-network';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        // Don't retry on 4xx errors
        if (error instanceof Error && error.message.includes('4')) {
          return false;
        }
        return failureCount < 3;
      },
      staleTime: Platform.OS === 'web' ? 1000 * 60 * 5 : 1000 * 60 * 10, // 5-10 minutes
      gcTime: 1000 * 60 * 30, // 30 minutes
      refetchOnWindowFocus: Platform.OS !== 'web',
      refetchOnReconnect: true,
    },
  },
});

// Network-aware query utilities
export const isOnline = async (): Promise<boolean> => {
  const networkState = await Network.getNetworkStateAsync();
  return networkState.isConnected ?? false;
};
```

Create `src/providers/QueryProvider.tsx`:
```typescript
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { queryClient } from '../services/queryClient';
import { PropsWithChildren } from 'react';

export function QueryProvider({ children }: PropsWithChildren) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
      {__DEV__ && <ReactQueryDevtools />}
    </QueryClientProvider>
  );
}
```

### 4.5 Custom Hooks for Data Fetching

Create `src/hooks/usePosts.ts`:
```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { postsApi } from '../services/api/postsApi';
import type { Post, CreatePostInput } from '../types';

export function usePosts(filters?: { userId?: string; limit?: number }) {
  return useQuery({
    queryKey: ['posts', filters],
    queryFn: () => postsApi.getPosts(filters),
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
}

export function usePost(postId: string) {
  return useQuery({
    queryKey: ['post', postId],
    queryFn: () => postsApi.getPostById(postId),
    enabled: !!postId,
  });
}

export function useCreatePost() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (input: CreatePostInput) => postsApi.createPost(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
    },
  });
}

export function useUpdatePost() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, ...input }: { id: string } & Partial<Post>) =>
      postsApi.updatePost(id, input),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
      queryClient.invalidateQueries({ queryKey: ['post', variables.id] });
    },
  });
}

export function useDeletePost() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (postId: string) => postsApi.deletePost(postId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
    },
  });
}
```

### 4.6 Optimistic Updates Example

Create `src/hooks/useLikePost.ts`:
```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { postsApi } from '../services/api/postsApi';

export function useLikePost(postId: string) {
  const queryClient = useQueryClient();
  const queryKey = ['post', postId];

  return useMutation({
    mutationFn: () => postsApi.likePost(postId),
    onMutate: async () => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey });

      // Snapshot the previous value
      const previousPost = queryClient.getQueryData(queryKey);

      // Optimistically update
      queryClient.setQueryData(queryKey, (old: any) => ({
        ...old,
        likes: old.likes + 1,
        isLiked: true,
      }));

      return { previousPost };
    },
    onError: (err, variables, context) => {
      // Rollback on error
      if (context?.previousPost) {
        queryClient.setQueryData(queryKey, context.previousPost);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey });
    },
  });
}
```

### 4.7 Combine Providers in App Root

Update `App.tsx`:
```typescript
import { QueryProvider } from './src/providers/QueryProvider';
import { RootNavigator } from './src/navigation/RootNavigator';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <QueryProvider>
          <RootNavigator />
        </QueryProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
```

## Deliverables
- [ ] Zustand store configured with persistence
- [ ] React Query client setup
- [ ] Custom data-fetching hooks created
- [ ] Optimistic updates implemented
- [ ] Store selectors for performance
- [ ] Providers integrated in app root

## Best Practices
- Keep Zustand stores focused and modular
- Use React Query for all server state
- Implement proper error boundaries
- Add loading states for all async operations
- Use selectors to prevent unnecessary re-renders
- Persist only essential data to AsyncStorage
- Invalidate queries strategically after mutations
