# Phase 7: Offline-First Architecture

## Overview
Implement offline capabilities with local data persistence, background sync, and seamless online/offline transitions.

## Key Activities

### 7.1 Install Dependencies
```bash
# Database options
npm install @react-native-async-storage/async-storage
npm install watermelondb @nozbe/watermelondb  # For complex queries
# OR
npm install realm react-native-realm          # For object database

# Network detection
npm install @react-native-community/netinfo

# Background sync (Expo)
npm install expo-task-manager expo-background-fetch
```

### 7.2 AsyncStorage Setup
Create `src/utils/storage.ts`:
```typescript
import AsyncStorage from '@react-native-async-storage/async-storage';

export const storage = {
  async set<T>(key: string, value: T): Promise<void> {
    await AsyncStorage.setItem(key, JSON.stringify(value));
  },

  async get<T>(key: string): Promise<T | null> {
    const value = await AsyncStorage.getItem(key);
    return value ? JSON.parse(value) : null;
  },

  async remove(key: string): Promise<void> {
    await AsyncStorage.removeItem(key);
  },

  async clear(): Promise<void> {
    await AsyncStorage.clear();
  },

  async getAllKeys(): Promise<string[]> {
    return AsyncStorage.getAllKeys();
  },

  async multiGet(keys: string[]): Promise<Array<{ key: string; value: any }>> {
    const result = await AsyncStorage.multiGet(keys);
    return result.map(([key, value]) => ({
      key,
      value: value ? JSON.parse(value) : null,
    }));
  },
};
```

### 7.3 Network Status Hook
Create `src/hooks/useNetworkStatus.ts`:
```typescript
import { useEffect, useState } from 'react';
import NetInfo from '@react-native-community/netinfo';

interface NetworkState {
  isConnected: boolean;
  isInternetReachable: boolean | null;
  type: string | null;
}

export function useNetworkStatus() {
  const [networkState, setNetworkState] = useState<NetworkState>({
    isConnected: false,
    isInternetReachable: null,
    type: null,
  });

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener((state) => {
      setNetworkState({
        isConnected: state.isConnected ?? false,
        isInternetReachable: state.isInternetReachable,
        type: state.type,
      });
    });

    return () => unsubscribe();
  }, []);

  return networkState;
}
```

### 7.4 Offline Queue for Mutations
Create `src/services/offline/OfflineQueue.ts`:
```typescript
import { storage } from '../../utils/storage';

interface QueuedRequest {
  id: string;
  endpoint: string;
  method: string;
  body?: any;
  timestamp: number;
  retries: number;
}

const QUEUE_KEY = 'offline_queue';
const MAX_RETRIES = 3;

export class OfflineQueue {
  private static instance: OfflineQueue;
  private queue: QueuedRequest[] = [];

  private constructor() {}

  static getInstance(): OfflineQueue {
    if (!OfflineQueue.instance) {
      OfflineQueue.instance = new OfflineQueue();
    }
    return OfflineQueue.instance;
  }

  async initialize(): Promise<void> {
    const savedQueue = await storage.get<QueuedRequest[]>(QUEUE_KEY);
    this.queue = savedQueue || [];
  }

  async enqueue(request: Omit<QueuedRequest, 'id' | 'timestamp' | 'retries'>): Promise<void> {
    const queuedRequest: QueuedRequest = {
      ...request,
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      timestamp: Date.now(),
      retries: 0,
    };

    this.queue.push(queuedRequest);
    await this.persist();
  }

  async dequeue(): Promise<QueuedRequest | null> {
    if (this.queue.length === 0) return null;
    const request = this.queue.shift();
    await this.persist();
    return request || null;
  }

  async retry(requestId: string): Promise<void> {
    const index = this.queue.findIndex((r) => r.id === requestId);
    if (index !== -1) {
      this.queue[index].retries += 1;
      if (this.queue[index].retries >= MAX_RETRIES) {
        this.queue.splice(index, 1);
      }
      await this.persist();
    }
  }

  async clear(): Promise<void> {
    this.queue = [];
    await storage.remove(QUEUE_KEY);
  }

  private async persist(): Promise<void> {
    await storage.set(QUEUE_KEY, this.queue);
  }

  getLength(): number {
    return this.queue.length;
  }

  getAll(): QueuedRequest[] {
    return [...this.queue];
  }
}
```

### 7.5 Sync Manager
Create `src/services/offline/SyncManager.ts`:
```typescript
import { OfflineQueue } from './OfflineQueue';
import apiClient from '../api/axiosInstance';
import { useAppStore } from '../../store/useAppStore';

export class SyncManager {
  private static instance: SyncManager;
  private isSyncing = false;
  private queue: OfflineQueue;

  private constructor() {
    this.queue = OfflineQueue.getInstance();
  }

  static getInstance(): SyncManager {
    if (!SyncManager.instance) {
      SyncManager.instance = new SyncManager();
    }
    return SyncManager.instance;
  }

  async sync(): Promise<{ success: number; failed: number }> {
    if (this.isSyncing) return { success: 0, failed: 0 };

    this.isSyncing = true;
    let success = 0;
    let failed = 0;

    try {
      while (true) {
        const request = await this.queue.dequeue();
        if (!request) break;

        try {
          await apiClient({
            method: request.method,
            url: request.endpoint,
            data: request.body,
          });
          success++;
        } catch (error) {
          await this.queue.retry(request.id);
          failed++;
        }
      }
    } finally {
      this.isSyncing = false;
    }

    return { success, failed };
  }

  scheduleBackgroundSync(): void {
    // Implement background sync using expo-task-manager
    console.log('Background sync scheduled');
  }
}
```

### 7.6 Data Persistence Layer
Create `src/services/cache/DataCache.ts`:
```typescript
import { storage } from '../../utils/storage';

interface CacheEntry<T> {
  data: T;
  timestamp: number;
  expiresAt: number;
}

const DEFAULT_TTL = 1000 * 60 * 30; // 30 minutes

export class DataCache {
  static async get<T>(key: string): Promise<T | null> {
    const entry = await storage.get<CacheEntry<T>>(key);
    
    if (!entry) return null;
    
    if (Date.now() > entry.expiresAt) {
      await this.remove(key);
      return null;
    }
    
    return entry.data;
  }

  static async set<T>(key: string, data: T, ttl: number = DEFAULT_TTL): Promise<void> {
    const entry: CacheEntry<T> = {
      data,
      timestamp: Date.now(),
      expiresAt: Date.now() + ttl,
    };
    await storage.set(key, entry);
  }

  static async remove(key: string): Promise<void> {
    await storage.remove(key);
  }

  static async clear(): Promise<void> {
    await storage.clear();
  }

  static async invalidate(pattern: string): Promise<void> {
    const keys = await storage.getAllKeys();
    const matchingKeys = keys.filter((key) => key.includes(pattern));
    
    for (const key of matchingKeys) {
      await storage.remove(key);
    }
  }
}
```

## Deliverables
- [ ] AsyncStorage utility configured
- [ ] Network status hook implemented
- [ ] Offline queue for mutations
- [ ] Sync manager created
- [ ] Data caching layer implemented
- [ ] Background sync scheduled

## Best Practices
- Cache frequently accessed data
- Implement exponential backoff for retries
- Show offline indicator to users
- Queue mutations when offline
- Sync automatically when back online
- Handle conflicts gracefully
- Clear old cache entries periodically
