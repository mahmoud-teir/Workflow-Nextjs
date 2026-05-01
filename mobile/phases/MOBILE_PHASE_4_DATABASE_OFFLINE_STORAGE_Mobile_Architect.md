<a name="phase-m4"></a>
# 📌 MOBILE PHASE M4: DATABASE & OFFLINE STORAGE (Mobile Architect)

> **Storage Strategy:** Multiple storage layers for different data types. Never a one-size-fits-all approach.

---

## 🗄️ Storage Decision Tree

```
What type of data?
├── Sensitive (tokens, passwords, PII) → expo-secure-store (Keychain/Keystore)
├── Small key-value (preferences, flags, cache) → MMKV (react-native-mmkv)
├── Structured relational data (offline-first) → Expo SQLite + Drizzle ORM
├── Large binary files (images, docs) → expo-file-system + Supabase Storage
└── Complex sync with conflict resolution → WatermelonDB (bare workflow required)
```

---

### Prompt M4.1: Expo SQLite + Drizzle ORM Setup

```text
You are a Mobile Database Architect. Set up Expo SQLite with Drizzle ORM for offline-first data storage.

App data model: [describe your main entities from PRD]

Constraints:
- Use Drizzle ORM (not raw SQL) — type-safe queries, auto-migration.
- Enable WAL (Write-Ahead Logging) mode for better concurrent read performance.
- All schema changes must use Drizzle migrations (never `ALTER TABLE` directly).
- Implement soft deletes (deletedAt timestamp) instead of hard deletes for sync safety.

Required Output Format: Provide complete code for:

1. Installation:
```bash
npx expo install expo-sqlite
npm install drizzle-orm
npm install --save-dev drizzle-kit
```

2. Database client `lib/db/client.ts`:
```typescript
import { openDatabaseSync, SQLiteDatabase } from 'expo-sqlite'
import { drizzle } from 'drizzle-orm/expo-sqlite'
import * as schema from './schema'

const expoDb: SQLiteDatabase = openDatabaseSync('[appname].db', {
  enableChangeListener: true,  // Enable real-time updates
})

// Enable WAL mode for performance
expoDb.execSync('PRAGMA journal_mode = WAL;')
expoDb.execSync('PRAGMA foreign_keys = ON;')

export const db = drizzle(expoDb, { schema })
export type DB = typeof db
```

3. Schema definition `lib/db/schema.ts`:
```typescript
import { sqliteTable, text, integer, real } from 'drizzle-orm/sqlite-core'
import { relations } from 'drizzle-orm'

export const users = sqliteTable('users', {
  id: text('id').primaryKey(),
  email: text('email').notNull().unique(),
  name: text('name').notNull(),
  avatarUrl: text('avatar_url'),
  createdAt: integer('created_at', { mode: 'timestamp' }).notNull().$defaultFn(() => new Date()),
  updatedAt: integer('updated_at', { mode: 'timestamp' }).notNull().$defaultFn(() => new Date()),
  deletedAt: integer('deleted_at', { mode: 'timestamp' }),
  syncedAt: integer('synced_at', { mode: 'timestamp' }),  // For sync tracking
})

export const posts = sqliteTable('posts', {
  id: text('id').primaryKey(),
  userId: text('user_id').notNull().references(() => users.id),
  title: text('title').notNull(),
  body: text('body').notNull(),
  status: text('status', { enum: ['draft', 'published', 'archived'] }).notNull().default('draft'),
  createdAt: integer('created_at', { mode: 'timestamp' }).notNull().$defaultFn(() => new Date()),
  updatedAt: integer('updated_at', { mode: 'timestamp' }).notNull().$defaultFn(() => new Date()),
  deletedAt: integer('deleted_at', { mode: 'timestamp' }),  // Soft delete
  syncedAt: integer('synced_at', { mode: 'timestamp' }),
})

export const usersRelations = relations(users, ({ many }) => ({
  posts: many(posts),
}))

export const postsRelations = relations(posts, ({ one }) => ({
  user: one(users, { fields: [posts.userId], references: [users.id] }),
}))
```

4. Drizzle query examples `lib/db/queries/posts.ts`:
```typescript
import { db } from '@/lib/db/client'
import { posts, users } from '@/lib/db/schema'
import { eq, desc, isNull, and } from 'drizzle-orm'

// Get all non-deleted posts with user
export async function getPosts() {
  return db
    .select()
    .from(posts)
    .leftJoin(users, eq(posts.userId, users.id))
    .where(isNull(posts.deletedAt))
    .orderBy(desc(posts.createdAt))
}

// Create post (generate UUID client-side for offline support)
export async function createPost(data: NewPost) {
  const id = generateUUID()
  await db.insert(posts).values({ ...data, id })
  return id
}

// Soft delete
export async function deletePost(id: string) {
  await db.update(posts)
    .set({ deletedAt: new Date(), updatedAt: new Date() })
    .where(eq(posts.id, id))
}
```

5. Migration setup in `drizzle.config.ts`:
```typescript
import { defineConfig } from 'drizzle-kit'

export default defineConfig({
  schema: './lib/db/schema.ts',
  out: './lib/db/migrations',
  dialect: 'sqlite',
  driver: 'expo',
})
```

6. Run migrations on app start `lib/db/migrate.ts`:
```typescript
import { useMigrations } from 'drizzle-orm/expo-sqlite/migrator'
import { db } from './client'
import migrations from './migrations/migrations'

export function useDatabaseMigration() {
  const { success, error } = useMigrations(db, migrations)
  return { success, error }
}

// In app/_layout.tsx:
const { success: dbReady } = useDatabaseMigration()
if (!dbReady) return <LoadingScreen />
```

⚠️ Common Pitfalls:
- Pitfall: Not enabling WAL mode — concurrent reads block writes on mobile.
- Solution: Always run `PRAGMA journal_mode = WAL` on database open.
- Pitfall: Hard-deleting records breaks sync — the server doesn't know what to delete.
- Solution: Use soft deletes (deletedAt) and sync the deletion event.
```

✅ **Verification Checklist:**
- [ ] Database opens without errors on both iOS and Android.
- [ ] `drizzle-kit generate` creates migration files.
- [ ] Migrations run automatically on app start.
- [ ] WAL mode enabled (verify with `PRAGMA journal_mode`).
- [ ] Soft delete pattern implemented on all synced entities.

---

### Prompt M4.2: MMKV — Fast Key-Value Storage

```text
You are a Mobile Storage Engineer. Set up MMKV for ultra-fast key-value storage.

Use MMKV for: User preferences, feature flags, theme, cached lightweight data, Zustand persistence.
Do NOT use MMKV for: Sensitive data (use SecureStore), large blobs (use FileSystem), relational data (use SQLite).

Required Output Format: Provide complete code for:

1. Installation:
```bash
npx expo install react-native-mmkv
```
⚠️ Requires `expo-build-properties` plugin and a development build (not Expo Go).

2. MMKV instance `lib/storage/mmkv.ts`:
```typescript
import { MMKV } from 'react-native-mmkv'

export const storage = new MMKV({
  id: 'app-storage',
  encryptionKey: undefined,  // Set to a key for encrypted storage
})

// Type-safe storage helpers
export const StorageKeys = {
  THEME: 'theme',
  ONBOARDED: 'onboarded',
  LAST_SYNC: 'last_sync',
  DRAFT_POST: 'draft_post',
} as const

type StorageKey = typeof StorageKeys[keyof typeof StorageKeys]

export const appStorage = {
  getString: (key: StorageKey) => storage.getString(key),
  setString: (key: StorageKey, value: string) => storage.set(key, value),
  getBoolean: (key: StorageKey) => storage.getBoolean(key),
  setBoolean: (key: StorageKey, value: boolean) => storage.set(key, value),
  delete: (key: StorageKey) => storage.delete(key),
  getJSON: <T>(key: StorageKey): T | undefined => {
    const raw = storage.getString(key)
    return raw ? JSON.parse(raw) : undefined
  },
  setJSON: <T>(key: StorageKey, value: T) => storage.set(key, JSON.stringify(value)),
}
```

3. Zustand persist with MMKV:
```typescript
import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import { storage } from '@/lib/storage/mmkv'

const MMKVZustandStorage = {
  getItem: (name: string) => storage.getString(name) ?? null,
  setItem: (name: string, value: string) => storage.set(name, value),
  removeItem: (name: string) => storage.delete(name),
}

interface UIStore {
  theme: 'light' | 'dark' | 'system'
  setTheme: (theme: UIStore['theme']) => void
}

export const useUIStore = create<UIStore>()(
  persist(
    (set) => ({
      theme: 'system',
      setTheme: (theme) => set({ theme }),
    }),
    {
      name: 'ui-store',
      storage: createJSONStorage(() => MMKVZustandStorage),
    }
  )
)
```
```

✅ **Verification Checklist:**
- [ ] MMKV reads/writes are synchronous (no async/await needed).
- [ ] Zustand store persists across app restarts via MMKV.
- [ ] Development build created (MMKV doesn't work in Expo Go).

---

### Prompt M4.3: Offline-First Sync Strategy

```text
You are a Mobile Offline-First Architecture Specialist. Design the sync strategy for [AppName].

Sync requirement: [describe what needs to sync: posts, messages, user profile, etc.]
Conflict strategy: [Last-Write-Wins / Operational Transform / Custom business rules]

Required Output Format: Provide complete code for:

1. Sync queue for offline mutations `lib/sync/queue.ts`:
```typescript
import { db } from '@/lib/db/client'
import { syncQueue } from '@/lib/db/schema'
import { apiClient } from '@/lib/api/client'

export type SyncOperation = {
  id: string
  table: string
  operation: 'create' | 'update' | 'delete'
  payload: unknown
  createdAt: Date
  attempts: number
}

export async function enqueueSync(op: Omit<SyncOperation, 'id' | 'createdAt' | 'attempts'>) {
  await db.insert(syncQueue).values({
    ...op,
    id: generateUUID(),
    createdAt: new Date(),
    attempts: 0,
    payload: JSON.stringify(op.payload),
  })
}

export async function processSyncQueue() {
  const pending = await db.select().from(syncQueue).orderBy(syncQueue.createdAt)

  for (const item of pending) {
    try {
      await apiClient.request({
        method: item.operation === 'create' ? 'POST' : item.operation === 'update' ? 'PUT' : 'DELETE',
        url: `/${item.table}/${item.operation !== 'create' ? item.payload.id : ''}`,
        data: item.payload,
      })
      await db.delete(syncQueue).where(eq(syncQueue.id, item.id))
    } catch {
      await db.update(syncQueue).set({ attempts: item.attempts + 1 }).where(eq(syncQueue.id, item.id))
    }
  }
}
```

2. Network status hook `lib/hooks/useNetworkStatus.ts`:
```typescript
import NetInfo from '@react-native-community/netinfo'
import { useEffect, useState } from 'react'
import { processSyncQueue } from '@/lib/sync/queue'

export function useNetworkStatus() {
  const [isOnline, setIsOnline] = useState(true)

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener((state) => {
      const online = state.isConnected && state.isInternetReachable
      setIsOnline(!!online)

      // Process offline queue when coming back online
      if (online) processSyncQueue()
    })
    return unsubscribe
  }, [])

  return { isOnline }
}
```

3. Offline indicator component:
```tsx
export function OfflineBanner() {
  const { isOnline } = useNetworkStatus()

  if (isOnline) return null

  return (
    <View className="bg-yellow-500 px-4 py-2 items-center">
      <Text className="text-white font-medium text-sm">
        You're offline — changes will sync when reconnected
      </Text>
    </View>
  )
}
```
```

✅ **Verification Checklist:**
- [ ] App works fully offline (create, read, update, delete from local SQLite).
- [ ] Sync queue processes when network restores.
- [ ] Offline banner appears within 2 seconds of going offline.
- [ ] No data loss after airplane mode → reconnect cycle.

---

📎 **Related Phases:**
- Prerequisites: [Phase M3: Backend & API](./MOBILE_PHASE_3_BACKEND_API_INTEGRATION_Full_Stack.md)
- Proceeds to: [Phase M5: Authentication & Security](./MOBILE_PHASE_5_AUTHENTICATION_SECURITY_Security_Expert.md)
