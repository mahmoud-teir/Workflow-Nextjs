# State Management with Zustand

## Overview
Complete guide to implementing state management in React Native using Zustand for local state and TanStack Query (React Query) for server state.

## Installation

```bash
npx expo install zustand @tanstack/react-query
```

## Zustand Basics

### Creating a Simple Store
```typescript
// stores/counterStore.ts
import { create } from 'zustand';

interface CounterState {
  count: number;
  increment: () => void;
  decrement: () => void;
  reset: () => void;
}

export const useCounterStore = create<CounterState>((set) => ({
  count: 0,
  increment: () => set((state) => ({ count: state.count + 1 })),
  decrement: () => set((state) => ({ count: state.count - 1 })),
  reset: () => set({ count: 0 }),
}));

// Usage in component
const CounterScreen = () => {
  const { count, increment, decrement, reset } = useCounterStore();

  return (
    <View>
      <Text>{count}</Text>
      <Button title="+" onPress={increment} />
      <Button title="-" onPress={decrement} />
      <Button title="Reset" onPress={reset} />
    </View>
  );
};
```

### Complex Store with Multiple Actions
```typescript
// stores/userStore.ts
import { create } from 'zustand';
import { User } from '../types/api';

interface UserState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  
  // Actions
  setUser: (user: User) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  updateUser: (updates: Partial<User>) => Promise<void>;
}

export const useUserStore = create<UserState>((set, get) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  setUser: (user) => set({ 
    user, 
    isAuthenticated: true, 
    error: null 
  }),

  logout: () => set({ 
    user: null, 
    isAuthenticated: false, 
    error: null 
  }),

  setLoading: (isLoading) => set({ isLoading }),

  setError: (error) => set({ error }),

  updateUser: async (updates) => {
    set({ isLoading: true, error: null });
    try {
      // Simulate API call
      const currentUser = get().user;
      if (!currentUser) throw new Error('No user logged in');
      
      const updatedUser = { ...currentUser, ...updates };
      
      // In real app: await api.updateUser(updatedUser);
      
      set({ user: updatedUser, isLoading: false });
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : 'Update failed',
        isLoading: false 
      });
    }
  },
}));
```

## Persisting State

### With AsyncStorage
```typescript
// stores/persistedStore.ts
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface SettingsState {
  theme: 'light' | 'dark' | 'system';
  language: string;
  notifications: boolean;
  setTheme: (theme: 'light' | 'dark' | 'system') => void;
  setLanguage: (language: string) => void;
  toggleNotifications: () => void;
}

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      theme: 'system',
      language: 'en',
      notifications: true,

      setTheme: (theme) => set({ theme }),
      setLanguage: (language) => set({ language }),
      toggleNotifications: () => 
        set((state) => ({ notifications: !state.notifications })),
    }),
    {
      name: 'settings-storage',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (state) => ({
        // Only persist specific fields
        theme: state.theme,
        language: state.language,
        notifications: state.notifications,
      }),
      onRehydrateStorage: () => {
        console.log('Rehydrating settings...');
        return (state, error) => {
          if (error) {
            console.error('Rehydration failed:', error);
          } else {
            console.log('Settings rehydrated:', state);
          }
        };
      },
    }
  )
);
```

### With MMKV (Faster Alternative)
```bash
npx expo install react-native-mmkv
```

```typescript
// stores/mmkvStore.ts
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { MMKV } from 'react-native-mmkv';

const storage = new MMKV();

const mmkvStorage = {
  setItem: (key: string, value: string) => {
    storage.set(key, value);
  },
  getItem: (key: string) => {
    const value = storage.getString(key);
    return value ?? null;
  },
  removeItem: (key: string) => {
    storage.delete(key);
  },
};

interface CacheState {
  cachedData: Record<string, any>;
  setCachedData: (key: string, data: any) => void;
  getCachedData: (key: string) => any;
  clearCache: () => void;
}

export const useCacheStore = create<CacheState>()(
  persist(
    (set, get) => ({
      cachedData: {},

      setCachedData: (key, data) =>
        set((state) => ({
          cachedData: { ...state.cachedData, [key]: data },
        })),

      getCachedData: (key) => get().cachedData[key],

      clearCache: () => set({ cachedData: {} }),
    }),
    {
      name: 'cache-storage',
      storage: createJSONStorage(() => mmkvStorage),
    }
  )
);
```

## Selectors and Performance

### Optimizing Re-renders with Selectors
```typescript
// stores/productStore.ts
import { create } from 'zustand';

interface Product {
  id: string;
  name: string;
  price: number;
  category: string;
}

interface ProductState {
  products: Product[];
  selectedCategory: string | null;
  searchQuery: string;
  addProduct: (product: Product) => void;
  setSelectedCategory: (category: string | null) => void;
  setSearchQuery: (query: string) => void;
}

export const useProductStore = create<ProductState>((set) => ({
  products: [],
  selectedCategory: null,
  searchQuery: '',

  addProduct: (product) =>
    set((state) => ({ products: [...state.products, product] })),

  setSelectedCategory: (category) => set({ selectedCategory: category }),
  setSearchQuery: (query) => set({ searchQuery: query }),
}));

// Component with optimized selectors
const ProductList = () => {
  // Bad: Re-renders on any store change
  // const { products, selectedCategory, searchQuery } = useProductStore();

  // Good: Only re-renders when products change
  const products = useProductStore((state) => state.products);
  
  // Good: Only re-renders when category changes
  const selectedCategory = useProductStore((state) => state.selectedCategory);

  return (
    <FlatList
      data={products.filter(p => 
        !selectedCategory || p.category === selectedCategory
      )}
      renderItem={({ item }) => <ProductCard product={item} />}
      keyExtractor={(item) => item.id}
    />
  );
};

// Using shallow comparison for multiple values
import { shallow } from 'zustand/shallow';

const FilterBar = () => {
  const { selectedCategory, searchQuery, setSelectedCategory, setSearchQuery } = 
    useProductStore(
      (state) => ({
        selectedCategory: state.selectedCategory,
        searchQuery: state.searchQuery,
        setSelectedCategory: state.setSelectedCategory,
        setSearchQuery: state.setSearchQuery,
      }),
      shallow
    );

  return (
    <View>
      {/* Filter UI */}
    </View>
  );
};
```

## Combining Zustand with TanStack Query

### Server State Pattern
```typescript
// hooks/useProducts.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useProductStore } from '../stores/productStore';
import { api } from '../api/client';
import { Product } from '../types/api';

export const useProducts = (categoryId?: string) => {
  const queryClient = useQueryClient();
  const setProducts = useProductStore((state) => state.addProduct);

  return useQuery({
    queryKey: ['products', categoryId],
    queryFn: () => api.getProducts(categoryId),
    select: (data) => {
      // Optionally sync with Zustand
      data.items.forEach(product => setProducts(product));
      return data;
    },
  });
};

export const useCreateProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (newProduct: Omit<Product, 'id'>) => 
      api.createProduct(newProduct),
    onSuccess: () => {
      // Invalidate queries
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });
};
```

### Auth State Sync Pattern
```typescript
// hooks/useAuth.ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useUserStore } from '../stores/userStore';
import { api } from '../api/client';
import { User } from '../types/api';

export const useAuth = () => {
  const queryClient = useQueryClient();
  const { user, setUser, logout, isAuthenticated } = useUserStore();

  // Check auth status on app start
  const { isLoading } = useQuery({
    queryKey: ['auth'],
    queryFn: async () => {
      try {
        const userData = await api.getCurrentUser();
        setUser(userData);
        return userData;
      } catch {
        logout();
        return null;
      }
    },
    retry: false,
  });

  const loginMutation = useMutation({
    mutationFn: (credentials: { email: string; password: string }) =>
      api.login(credentials),
    onSuccess: (userData) => {
      setUser(userData);
      queryClient.setQueryData(['auth'], userData);
    },
  });

  const logoutMutation = useMutation({
    mutationFn: () => api.logout(),
    onSuccess: () => {
      logout();
      queryClient.clear();
    },
  });

  return {
    user,
    isAuthenticated,
    isLoading,
    login: loginMutation.mutateAsync,
    logout: logoutMutation.mutateAsync,
    isLoggingIn: loginMutation.isPending,
    isLoggingOut: logoutMutation.isPending,
  };
};
```

## Middleware Patterns

### Logging Middleware
```typescript
// stores/middleware/logger.ts
import { StateCreator } from 'zustand';

export const logger = <T extends object>(
  config: StateCreator<T>,
  name?: string
): StateCreator<T> => (set, get, api) =>
  config(
    (...args) => {
      console.log(`[${name || 'Store'}] Setting:`, args[0]);
      set(...args);
      console.log(`[${name || 'Store'}] New state:`, get());
    },
    get,
    api
  );

// Usage
export const useCounterStore = create<CounterState>()(
  logger(
    (set) => ({
      count: 0,
      increment: () => set((state) => ({ count: state.count + 1 })),
    }),
    'Counter'
  )
);
```

### Devtools Middleware
```typescript
// stores/withDevtools.ts
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

export const useAppStore = create<AppState>()(
  devtools(
    (set) => ({
      // State and actions
    }),
    {
      name: 'AppStore',
      enabled: __DEV__,
    }
  )
);
```

## Best Practices

1. **Separation of Concerns**: Use Zustand for client state, TanStack Query for server state
2. **Selectors**: Always use selectors to prevent unnecessary re-renders
3. **Persistence**: Only persist necessary data
4. **Modular Stores**: Split stores by domain (user, products, settings)
5. **Type Safety**: Define clear interfaces for all stores
6. **Error Handling**: Handle errors in actions properly
7. **Testing**: Test stores independently from components

## Testing Stores

```typescript
// __tests__/userStore.test.ts
import { useUserStore } from '../stores/userStore';

describe('UserStore', () => {
  beforeEach(() => {
    // Reset store before each test
    useUserStore.setState({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    });
  });

  it('should set user on login', () => {
    const mockUser = { id: '1', name: 'John', email: 'john@example.com' };
    
    useUserStore.getState().setUser(mockUser);
    
    expect(useUserStore.getState().user).toEqual(mockUser);
    expect(useUserStore.getState().isAuthenticated).toBe(true);
  });

  it('should clear user on logout', () => {
    useUserStore.getState().setUser({ id: '1', name: 'John', email: 'john@example.com' });
    useUserStore.getState().logout();
    
    expect(useUserStore.getState().user).toBeNull();
    expect(useUserStore.getState().isAuthenticated).toBe(false);
  });
});
```

## Resources
- [Zustand Documentation](https://zustand-demo.pmnd.rs/)
- [TanStack Query Docs](https://tanstack.com/query/latest)
- [Best Practices Guide](https://github.com/pmndrs/zustand#best-practices)
