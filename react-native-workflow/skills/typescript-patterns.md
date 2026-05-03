# TypeScript Patterns for React Native

## Overview
Type-safe patterns and best practices for React Native applications using TypeScript 5+.

## Component Patterns

### Typed Functional Components
```typescript
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

// Pattern 1: React.FC with explicit props type (recommended for children)
interface ButtonProps {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  variant?: 'primary' | 'secondary';
  children?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({ 
  title, 
  onPress, 
  disabled = false,
  variant = 'primary',
  children 
}) => {
  return (
    <View style={[styles.button, styles[variant], disabled && styles.disabled]}>
      {children || <Text>{title}</Text>}
    </View>
  );
};

// Pattern 2: Inline type annotation (cleaner for simple components)
type TextProps = {
  size?: 'sm' | 'md' | 'lg';
  color?: string;
  children: React.ReactNode;
};

export const AppText = ({ 
  size = 'md', 
  color = '#000', 
  children 
}: TextProps) => {
  return <Text style={{ fontSize: size === 'sm' ? 12 : size === 'lg' ? 18 : 14, color }}>{children}</Text>;
};
```

### Generic Components
```typescript
// Generic List Component
interface ListProps<T> {
  data: T[];
  renderItem: (item: T, index: number) => React.ReactNode;
  keyExtractor: (item: T, index: number) => string;
  emptyComponent?: React.ReactNode;
}

export function List<T>({ 
  data, 
  renderItem, 
  keyExtractor,
  emptyComponent 
}: ListProps<T>) {
  if (data.length === 0) {
    return <>{emptyComponent}</>;
  }

  return (
    <FlatList
      data={data}
      renderItem={({ item, index }) => renderItem(item, index)}
      keyExtractor={keyExtractor}
    />
  );
}

// Usage
interface User {
  id: string;
  name: string;
}

<List<User>
  data={users}
  renderItem={(user) => <Text>{user.name}</Text>}
  keyExtractor={(user) => user.id}
/>;
```

## Navigation Typing

### React Navigation Type Safety
```typescript
// navigation/types.ts
import { NavigatorScreenParams } from '@react-navigation/native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';

// Define route parameters
export type RootStackParamList = {
  Home: undefined;
  Profile: { userId: string };
  Settings: undefined;
  Article: { articleId: string; fromScreen?: string };
};

export type AuthStackParamList = {
  Login: undefined;
  Register: undefined;
  ForgotPassword: undefined;
};

export type MainTabParamList = {
  HomeTab: NavigatorScreenParams<RootStackParamList>;
  Search: undefined;
  ProfileTab: NavigatorScreenParams<ProfileStackParamList>;
};

export type ProfileStackParamList = {
  MyProfile: undefined;
  EditProfile: undefined;
};

// Type-safe hook usage
declare global {
  namespace ReactNavigation {
    interface RootParamList extends RootStackParamList {}
  }
}

// Screen props types
export type HomeScreenProps = NativeStackScreenProps<RootStackParamList, 'Home'>;
export type ProfileScreenProps = NativeStackScreenProps<RootStackParamList, 'Profile'>;

// Navigation prop types
export type HomeNavigationProp = HomeScreenProps['navigation'];
export type ProfileRouteProp = ProfileScreenProps['route'];
```

### Using Typed Navigation
```typescript
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from './types';

// In a screen component
const ProfileScreen = () => {
  // Typed navigation
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList, 'Profile'>>();
  
  // Typed route
  const route = useRoute<RouteProp<RootStackParamList, 'Profile'>>();
  const { userId } = route.params;

  // Type-safe navigation methods
  const handleNavigate = () => {
    navigation.navigate('Home'); // ✅ Valid
    navigation.navigate('Article', { articleId: '123', fromScreen: 'Profile' }); // ✅ Valid
    // navigation.navigate('NonExistent'); // ❌ TypeScript error
  };

  return <View>{/* ... */}</View>;
};
```

## API Response Typing

### Base Response Types
```typescript
// types/api.ts
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: 'success' | 'error';
  timestamp: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  pagination: {
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
  };
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string[]>;
}

// Specific entity types
export interface User {
  id: string;
  email: string;
  name: string;
  avatar?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Article {
  id: string;
  title: string;
  content: string;
  author: User;
  tags: string[];
  publishedAt: string;
}

// API service types
export type UserResponse = ApiResponse<User>;
export type UsersResponse = PaginatedResponse<User>;
export type ArticlesResponse = PaginatedResponse<Article>;
```

### Typed API Client
```typescript
// api/client.ts
import axios, { AxiosError, AxiosInstance } from 'axios';
import { ApiResponse, ApiError } from '../types/api';

class ApiClient {
  private client: AxiosInstance;

  constructor(baseURL: string) {
    this.client = axios.create({
      baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError<ApiError>) => {
        // Handle typed errors
        if (error.response?.data) {
          console.error('API Error:', error.response.data);
        }
        return Promise.reject(error);
      }
    );
  }

  async get<T>(url: string): Promise<ApiResponse<T>> {
    const { data } = await this.client.get<ApiResponse<T>>(url);
    return data;
  }

  async post<T, P>(url: string, payload: P): Promise<ApiResponse<T>> {
    const { data } = await this.client.post<ApiResponse<T>, ApiResponse<T>, P>(url, payload);
    return data;
  }
}

// Usage
const api = new ApiClient(process.env.API_URL!);

// Type-safe API calls
const getUser = async (userId: string) => {
  const response = await api.get<User>(`/users/${userId}`);
  return response.data; // Type: User
};

const getUsers = async (page: number) => {
  const response = await api.get<PaginatedResponse<User>>(`/users?page=${page}`);
  return response.data; // Type: PaginatedResponse<User>
};
```

## Generic Hooks

### Typed Custom Hooks
```typescript
// hooks/useFetch.ts
import { useState, useEffect, useCallback } from 'react';

interface UseFetchResult<T> {
  data: T | null;
  loading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
}

export function useFetch<T>(
  fetchFn: () => Promise<T>,
  dependencies: any[] = []
): UseFetchResult<T> {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await fetchFn();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Unknown error'));
    } finally {
      setLoading(false);
    }
  }, dependencies);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return { data, loading, error, refetch: fetchData };
}

// Usage
interface Product {
  id: string;
  name: string;
  price: number;
}

const ProductList = () => {
  const { data: products, loading, error, refetch } = useFetch<Product[]>(
    () => fetchProducts(),
    []
  );

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage error={error} />;
  if (!products) return null;

  return (
    <List<Product>
      data={products}
      renderItem={(product) => <ProductCard product={product} />}
      keyExtractor={(product) => product.id}
    />
  );
};
```

### Typed Context
```typescript
// context/AuthContext.tsx
import React, { createContext, useContext, ReactNode } from 'react';
import { User } from '../types/api';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  updateUser: (updates: Partial<User>) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const login = async (email: string, password: string) => {
    // Implementation
  };

  const logout = async () => {
    // Implementation
  };

  const updateUser = async (updates: Partial<User>) => {
    if (user) {
      setUser({ ...user, ...updates });
    }
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
};

// Typed hook for consuming context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// Usage in components
const ProfileScreen = () => {
  const { user, logout, updateUser } = useAuth();
  
  // TypeScript knows user is User | null
  if (!user) return <LoginPrompt />;

  return (
    <View>
      <Text>{user.name}</Text>
      <Button title="Logout" onPress={logout} />
    </View>
  );
};
```

## Utility Types

### Common Utility Patterns
```typescript
// types/utils.ts

// Make specific properties required
type RequiredFields<T, K extends keyof T> = T & Required<Pick<T, K>>;

// Make specific properties optional
type OptionalFields<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;

// Extract only certain fields
type PickFields<T, K extends keyof T> = Pick<T, K>;

// Remove certain fields
type OmitFields<T, K extends keyof T> = Omit<T, K>;

// Make all nested properties mutable
type DeepMutable<T> = {
  -readonly [P in keyof T]: T[P] extends object ? DeepMutable<T[P]> : T[P];
};

// Make all nested properties readonly
type DeepReadonly<T> = {
  readonly [P in keyof T]: T[P] extends object ? DeepReadonly<T[P]> : T[P];
};

// Extract return type of async function
type UnwrapPromise<T> = T extends Promise<infer U> ? U : T;

// Usage examples
interface User {
  readonly id: string;
  name: string;
  email?: string;
}

// Make email required
type CompleteUser = RequiredFields<User, 'email'>;

// Make id mutable
type MutableUser = DeepMutable<User>;

// Get just id and name
type UserName = PickFields<User, 'id' | 'name'>;

// Remove id
type UserWithoutId = OmitFields<User, 'id'>;
```

## Best Practices

1. **Strict Mode**: Enable strict TypeScript in tsconfig.json
2. **No Any**: Avoid `any` type, use `unknown` when necessary
3. **Interface vs Type**: Use interfaces for objects, types for unions
4. **Generics**: Leverage generics for reusable components and hooks
5. **Type Inference**: Let TypeScript infer when possible
6. **Discriminated Unions**: Use for state machines and complex types
7. **Const Assertions**: Use `as const` for literal types

## tsconfig.json Recommendation
```json
{
  "extends": "expo/tsconfig.base",
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noImplicitReturns": true,
    "noFallthroughCasesInSwitch": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "@components/*": ["src/components/*"],
      "@hooks/*": ["src/hooks/*"],
      "@types/*": ["src/types/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.tsx"],
  "exclude": ["node_modules"]
}
```
