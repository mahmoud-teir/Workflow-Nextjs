# Phase 6: API Integration & Backend Communication

## Overview
Set up robust API communication with proper error handling, authentication, request/response interceptors, and type safety.

## Key Activities

### 6.1 Install HTTP Client Dependencies
```bash
npm install axios
npm install -D @types/axios
```

### 6.2 Axios Instance Configuration

Create `src/services/api/axiosInstance.ts`:
```typescript
import axios, { 
  AxiosInstance, 
  AxiosRequestConfig, 
  AxiosError, 
  InternalAxiosRequestConfig,
  AxiosResponse 
} from 'axios';
import { useAppStore } from '../../store/useAppStore';
import { navigationHelpers } from '../../utils/navigationHelpers';

const BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL || 'https://api.example.com';
const TIMEOUT = 30000; // 30 seconds

export const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAppStore.getState().token;
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add request ID for tracking
    config.headers['X-Request-ID'] = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response Interceptor
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Handle 401 Unauthorized
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Attempt token refresh
        const refreshToken = useAppStore.getState().token; // Get refresh token from store
        if (refreshToken) {
          const response = await axios.post(`${BASE_URL}/auth/refresh`, {
            refreshToken,
          });

          const { token: newToken } = response.data;
          useAppStore.getState().login(newToken, useAppStore.getState().user!);

          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
          }

          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, logout user
        useAppStore.getState().logout();
        navigationHelpers.navigateAndClearHistory('Auth');
        return Promise.reject(refreshError);
      }
    }

    // Handle other errors
    const errorMessage = getErrorMessage(error);
    console.error('API Error:', errorMessage);

    return Promise.reject({
      ...error,
      message: errorMessage,
    });
  }
);

function getErrorMessage(error: AxiosError): string {
  if (error.response) {
    const data = error.response.data as any;
    return data?.message || data?.error || 'An unexpected error occurred';
  } else if (error.request) {
    return 'Network error. Please check your connection.';
  } else {
    return error.message || 'An error occurred';
  }
}

export default apiClient;
```

### 6.3 API Service Layer Pattern

Create `src/services/api/postsApi.ts`:
```typescript
import apiClient from './axiosInstance';
import type { Post, CreatePostInput, UpdatePostInput, PaginatedResponse } from '../../types';

export const postsApi = {
  async getPosts(params?: { userId?: string; limit?: number; offset?: number }) {
    const response = await apiClient.get<PaginatedResponse<Post>>('/posts', { params });
    return response.data;
  },

  async getPostById(id: string) {
    const response = await apiClient.get<Post>(`/posts/${id}`);
    return response.data;
  },

  async createPost(input: CreatePostInput) {
    const response = await apiClient.post<Post>('/posts', input);
    return response.data;
  },

  async updatePost(id: string, input: UpdatePostInput) {
    const response = await apiClient.put<Post>(`/posts/${id}`, input);
    return response.data;
  },

  async deletePost(id: string) {
    await apiClient.delete(`/posts/${id}`);
  },

  async likePost(id: string) {
    const response = await apiClient.post<Post>(`/posts/${id}/like`);
    return response.data;
  },

  async unlikePost(id: string) {
    const response = await apiClient.post<Post>(`/posts/${id}/unlike`);
    return response.data;
  },
};
```

Create `src/services/api/authApi.ts`:
```typescript
import apiClient from './axiosInstance';
import type { User, LoginInput, SignupInput, AuthResponse } from '../../types';

export const authApi = {
  async login(input: LoginInput) {
    const response = await apiClient.post<AuthResponse>('/auth/login', input);
    return response.data;
  },

  async signup(input: SignupInput) {
    const response = await apiClient.post<AuthResponse>('/auth/signup', input);
    return response.data;
  },

  async logout() {
    await apiClient.post('/auth/logout');
  },

  async forgotPassword(email: string) {
    await apiClient.post('/auth/forgot-password', { email });
  },

  async resetPassword(token: string, newPassword: string) {
    await apiClient.post('/auth/reset-password', { token, newPassword });
  },

  async getCurrentUser() {
    const response = await apiClient.get<User>('/auth/me');
    return response.data;
  },

  async updateProfile(updates: Partial<User>) {
    const response = await apiClient.patch<User>('/auth/profile', updates);
    return response.data;
  },

  async changePassword(currentPassword: string, newPassword: string) {
    await apiClient.post('/auth/change-password', { currentPassword, newPassword });
  },
};
```

### 6.4 TypeScript Types for API

Create `src/types/api.ts`:
```typescript
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: 'success' | 'error';
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    total: number;
    page: number;
    limit: number;
    totalPages: number;
    hasNextPage: boolean;
    hasPreviousPage: boolean;
  };
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string[]>;
}

export interface UploadProgress {
  loaded: number;
  total: number;
  percentage: number;
}
```

Create `src/types/auth.ts`:
```typescript
export interface User {
  id: string;
  email: string;
  name: string;
  avatar?: string;
  role: 'user' | 'admin';
  createdAt: string;
  updatedAt: string;
}

export interface LoginInput {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface SignupInput {
  email: string;
  password: string;
  name: string;
  acceptTerms: boolean;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}
```

### 6.5 File Upload Handling

Create `src/services/api/uploadApi.ts`:
```typescript
import apiClient from './axiosInstance';
import * as FileSystem from 'expo-file-system';
import * as ImagePicker from 'expo-image-picker';

export interface UploadResult {
  url: string;
  key: string;
  size: number;
}

export const uploadApi = {
  async pickImage(options?: ImagePicker.ImagePickerOptions) {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
      ...options,
    });

    if (!result.canceled && result.assets[0]) {
      return result.assets[0];
    }

    return null;
  },

  async takePhoto(options?: ImagePicker.ImagePickerOptions) {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    
    if (status !== 'granted') {
      throw new Error('Camera permission is required');
    }

    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
      ...options,
    });

    if (!result.canceled && result.assets[0]) {
      return result.assets[0];
    }

    return null;
  },

  async uploadFile(uri: string, onProgress?: (progress: number) => void) {
    const formData = new FormData();
    
    const file: any = {
      uri,
      type: 'image/jpeg',
      name: `upload-${Date.now()}.jpg`,
    };

    formData.append('file', file);

    const response = await apiClient.post<UploadResult>('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percentage = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percentage);
        }
      },
    });

    return response.data;
  },

  async uploadAvatar(imageUri: string) {
    return this.uploadFile(imageUri);
  },
};
```

### 6.6 Environment Variables

Create `.env.example`:
```bash
EXPO_PUBLIC_API_BASE_URL=https://api.example.com
EXPO_PUBLIC_APP_NAME=MyApp
EXPO_PUBLIC_SENTRY_DSN=
EXPO_PUBLIC_MIXPANEL_TOKEN=
EXPO_PUBLIC_FIREBASE_API_KEY=
```

Create `src/config/env.ts`:
```typescript
import { Constants } from 'expo-constants';

const env = Constants.expoConfig?.extra || {};

export const config = {
  apiUrl: process.env.EXPO_PUBLIC_API_BASE_URL || env.apiUrl,
  appName: process.env.EXPO_PUBLIC_APP_NAME || 'MyApp',
  sentryDsn: process.env.EXPO_PUBLIC_SENTRY_DSN,
  mixpanelToken: process.env.EXPO_PUBLIC_MIXPANEL_TOKEN,
  firebaseApiKey: process.env.EXPO_PUBLIC_FIREBASE_API_KEY,
  isDev: __DEV__,
};

export function validateEnv() {
  const requiredVars = ['EXPO_PUBLIC_API_BASE_URL'];
  const missing = requiredVars.filter(
    (varName) => !process.env[varName] && !env[varName.replace('EXPO_PUBLIC_', '').toLowerCase()]
  );

  if (missing.length > 0) {
    console.warn(`Missing environment variables: ${missing.join(', ')}`);
  }
}
```

### 6.7 API Hook Utilities

Create `src/hooks/useApi.ts`:
```typescript
import { useState, useCallback } from 'react';

interface ApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

interface UseApiReturn<T> extends ApiState<T> {
  execute: (...args: any[]) => Promise<T>;
  reset: () => void;
}

export function useApi<T>(
  apiFunction: (...args: any[]) => Promise<T>,
  options?: {
    onSuccess?: (data: T) => void;
    onError?: (error: string) => void;
  }
): UseApiReturn<T> {
  const [state, setState] = useState<ApiState<T>>({
    data: null,
    loading: false,
    error: null,
  });

  const execute = useCallback(async (...args: any[]): Promise<T> => {
    setState((prev) => ({ ...prev, loading: true, error: null }));

    try {
      const data = await apiFunction(...args);
      setState({ data, loading: false, error: null });
      options?.onSuccess?.(data);
      return data;
    } catch (error: any) {
      const errorMessage = error.message || 'An error occurred';
      setState((prev) => ({ ...prev, loading: false, error: errorMessage }));
      options?.onError?.(errorMessage);
      throw error;
    }
  }, [apiFunction, options]);

  const reset = useCallback(() => {
    setState({ data: null, loading: false, error: null });
  }, []);

  return {
    ...state,
    execute,
    reset,
  };
}
```

## Deliverables
- [ ] Axios instance configured with interceptors
- [ ] API service layer implemented
- [ ] TypeScript types for all API responses
- [ ] File upload functionality
- [ ] Environment variables setup
- [ ] Custom API hooks created
- [ ] Error handling strategy implemented

## Best Practices
- Always use TypeScript for API types
- Implement request/response interceptors centrally
- Handle token refresh automatically
- Use meaningful error messages
- Implement request cancellation for unmounted components
- Log API errors for debugging
- Use environment variables for configuration
- Implement rate limiting awareness
