# Skill: API Client Implementation

## Overview
Build a robust, type-safe API client with error handling, caching, and retry logic.

## Implementation

### Axios Client Setup
```typescript
// services/api/client.ts
import axios, { AxiosInstance, AxiosError } from 'axios';
import { analyticsService } from '../analytics';

interface ApiConfig {
  baseURL: string;
  timeout: number;
  retries: number;
}

class ApiClient {
  private client: AxiosInstance;
  private config: ApiConfig;

  constructor(config: ApiConfig) {
    this.config = config;
    this.client = axios.create({
      baseURL: config.baseURL,
      timeout: config.timeout,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        const token = SecureStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as any;

        // Retry logic
        if (error.response?.status === 429 && originalRequest._retryCount < this.config.retries) {
          originalRequest._retryCount = (originalRequest._retryCount || 0) + 1;
          const delay = Math.pow(2, originalRequest._retryCount) * 1000;
          await new Promise(resolve => setTimeout(resolve, delay));
          return this.client(originalRequest);
        }

        // Handle 401 - Token expired
        if (error.response?.status === 401) {
          await this.handleUnauthorized();
        }

        // Log error to analytics
        analyticsService.logEvent({
          name: 'api_error',
          params: {
            status: error.response?.status,
            url: originalRequest.url,
            method: originalRequest.method
          }
        });

        return Promise.reject(error);
      }
    );
  }

  private async handleUnauthorized() {
    await SecureStorage.deleteItem('authToken');
    await SecureStorage.deleteItem('refreshToken');
    // Navigate to login screen
  }

  async get<T>(url: string, params?: any): Promise<T> {
    const response = await this.client.get<T>(url, { params });
    return response.data;
  }

  async post<T>(url: string, data?: any): Promise<T> {
    const response = await this.client.post<T>(url, data);
    return response.data;
  }

  async put<T>(url: string, data?: any): Promise<T> {
    const response = await this.client.put<T>(url, data);
    return response.data;
  }

  async delete<T>(url: string): Promise<T> {
    const response = await this.client.delete<T>(url);
    return response.data;
  }
}

export const apiClient = new ApiClient({
  baseURL: process.env.API_URL!,
  timeout: 30000,
  retries: 3
});
```

### Type-Safe API Endpoints
```typescript
// services/api/endpoints.ts
import { apiClient } from './client';

export interface User {
  id: string;
  email: string;
  name: string;
  avatar?: string;
}

export interface Post {
  id: string;
  title: string;
  content: string;
  userId: string;
  createdAt: string;
}

export const userEndpoints = {
  getCurrentUser: () => apiClient.get<User>('/me'),
  updateUser: (data: Partial<User>) => apiClient.put<User>('/me', data),
  deleteUser: () => apiClient.delete<void>('/me'),
};

export const postEndpoints = {
  getPosts: (page: number = 1) => apiClient.get<Post[]>('/posts', { page }),
  getPost: (id: string) => apiClient.get<Post>(`/posts/${id}`),
  createPost: (data: Omit<Post, 'id' | 'createdAt'>) => apiClient.post<Post>('/posts', data),
  updatePost: (id: string, data: Partial<Post>) => apiClient.put<Post>(`/posts/${id}`, data),
  deletePost: (id: string) => apiClient.delete<void>(`/posts/${id}`),
};
```

## Best Practices
- Use TypeScript for type safety
- Implement retry logic with exponential backoff
- Handle network errors gracefully
- Cache responses when appropriate
- Log API errors for monitoring
