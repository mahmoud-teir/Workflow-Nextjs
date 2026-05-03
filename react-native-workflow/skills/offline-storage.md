# Skill: Offline Storage & Data Persistence

## Overview
Implement robust offline-first data storage with synchronization capabilities.

## Storage Options

### 1. AsyncStorage (Simple Key-Value)
```typescript
// services/storage/asyncStorage.ts
import AsyncStorage from '@react-native-async-storage/async-storage';

class AsyncStorageService {
  async setItem<T>(key: string, value: T): Promise<void> {
    await AsyncStorage.setItem(key, JSON.stringify(value));
  }

  async getItem<T>(key: string): Promise<T | null> {
    const value = await AsyncStorage.getItem(key);
    return value ? JSON.parse(value) : null;
  }

  async removeItem(key: string): Promise<void> {
    await AsyncStorage.removeItem(key);
  }

  async clear(): Promise<void> {
    await AsyncStorage.clear();
  }

  async getAllKeys(): Promise<string[]> {
    return await AsyncStorage.getAllKeys();
  }

  async multiGet(keys: string[]): Promise<[string, string][]> {
    return await AsyncStorage.multiGet(keys);
  }

  async multiSet(data: [string, any][]): Promise<void> {
    const parsed = data.map(([key, value]) => [key, JSON.stringify(value)]);
    await AsyncStorage.multiSet(parsed);
  }
}

export const asyncStorage = new AsyncStorageService();
```

### 2. WatermelonDB (Relational Database)
```typescript
// services/database/schema.ts
import { appSchema, tableSchema } from '@nozbe/watermelondb';

export default appSchema({
  version: 1,
  tables: [
    tableSchema({
      name: 'posts',
      columns: [
        { name: 'title', type: 'string' },
        { name: 'content', type: 'string' },
        { name: 'user_id', type: 'string' },
        { name: 'created_at', type: 'number' },
        { name: 'updated_at', type: 'number' },
        { name: 'synced', type: 'boolean' },
      ],
    }),
    tableSchema({
      name: 'users',
      columns: [
        { name: 'email', type: 'string' },
        { name: 'name', type: 'string' },
        { name: 'avatar_url', type: 'string' },
      ],
    }),
  ],
});
```

```typescript
// services/database/models/Post.ts
import { Model } from '@nozbe/watermelondb';
import { field, relation, children } from '@nozbe/watermelondb/decorators';

export default class Post extends Model {
  static table = 'posts';

  @field('title') title!: string;
  @field('content') content!: string;
  @field('user_id') userId!: string;
  @field('created_at') createdAt!: number;
  @field('updated_at') updatedAt!: number;
  @field('synced') synced!: boolean;

  @relation('users', 'user_id') user!: any;
}
```

### 3. Sync Engine
```typescript
// services/sync/syncEngine.ts
import { database } from './database';

interface SyncOperation {
  type: 'create' | 'update' | 'delete';
  table: string;
  recordId: string;
  data?: any;
  timestamp: number;
}

class SyncEngine {
  private isSyncing = false;
  private queue: SyncOperation[] = [];

  async addToQueue(operation: SyncOperation): Promise<void> {
    this.queue.push(operation);
    await this.processQueue();
  }

  private async processQueue(): Promise<void> {
    if (this.isSyncing || this.queue.length === 0) return;

    this.isSyncing = true;

    try {
      while (this.queue.length > 0) {
        const operation = this.queue.shift()!;
        await this.syncOperation(operation);
      }
    } catch (error) {
      console.error('Sync failed:', error);
      // Re-queue failed operations
    } finally {
      this.isSyncing = false;
    }
  }

  private async syncOperation(operation: SyncOperation): Promise<void> {
    switch (operation.type) {
      case 'create':
        await apiClient.post(`/${operation.table}`, operation.data);
        break;
      case 'update':
        await apiClient.put(`/${operation.table}/${operation.recordId}`, operation.data);
        break;
      case 'delete':
        await apiClient.delete(`/${operation.table}/${operation.recordId}`);
        break;
    }

    // Mark as synced in local database
    await database.write(async () => {
      const record = await database.get(operation.table).find(operation.recordId);
      await record.update(r => {
        r.synced = true;
      });
    });
  }

  async pullChanges(): Promise<void> {
    const lastSync = await asyncStorage.getItem<number>('lastSync');
    const response = await apiClient.get('/sync', { since: lastSync || 0 });

    await database.write(async () => {
      for (const change of response.changes) {
        const collection = database.get(change.table);
        
        if (change.type === 'delete') {
          await collection.find(change.id).then(r => r.destroyPermanently());
        } else {
          await collection.create(record => {
            Object.assign(record, change.data);
          });
        }
      }
    });

    await asyncStorage.setItem('lastSync', Date.now());
  }
}

export const syncEngine = new SyncEngine();
```

## Best Practices
- Use WatermelonDB for complex relational data
- Implement conflict resolution strategies
- Queue offline changes for later sync
- Handle network connectivity changes
- Encrypt sensitive data at rest
