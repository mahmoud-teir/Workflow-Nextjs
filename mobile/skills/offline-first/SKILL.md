---
name: offline-first
description: Use this skill when designing or implementing data persistence and synchronization features for React Native apps. Covers SQLite, sync queues, conflict resolution, and network status handling.
origin: Mobile ECC
stack: Expo SQLite, Drizzle ORM, @react-native-community/netinfo, MMKV, TanStack Query
---

# Offline-First Mobile Development Skill

## Core Principle

Design for offline as the default state. The app must be fully functional without a network connection for all core features. Network is an enhancement, not a requirement.

## Storage Layer Architecture

```
Sensitive Data → expo-secure-store (Keychain/Keystore)
Preferences/Cache → MMKV (react-native-mmkv)
Relational Data → SQLite + Drizzle ORM (expo-sqlite)
Large Files → expo-file-system + remote storage
```

## Drizzle ORM Patterns

### Schema (with sync tracking)
```typescript
// Always include these fields for synced tables:
export const items = sqliteTable('items', {
  id: text('id').primaryKey(),  // UUID — generated client-side
  // ... business fields
  createdAt: integer('created_at', { mode: 'timestamp' }).$defaultFn(() => new Date()),
  updatedAt: integer('updated_at', { mode: 'timestamp' }).$defaultFn(() => new Date()),
  deletedAt: integer('deleted_at', { mode: 'timestamp' }),  // Soft delete
  syncedAt: integer('synced_at', { mode: 'timestamp' }),    // Last server sync
})
```

### Always generate UUIDs client-side for offline creates
```typescript
import { randomUUID } from 'expo-crypto'

async function createItem(data: NewItem) {
  const id = randomUUID()  // NOT server-generated
  await db.insert(items).values({ ...data, id })
  await enqueueSync({ operation: 'create', table: 'items', payload: { ...data, id } })
  return id
}
```

### Soft delete (never hard delete synced records)
```typescript
async function deleteItem(id: string) {
  // Soft delete — server will sync the deletion
  await db.update(items)
    .set({ deletedAt: new Date(), updatedAt: new Date() })
    .where(eq(items.id, id))
  await enqueueSync({ operation: 'delete', table: 'items', payload: { id } })
}
```

## Sync Queue Pattern

### Queue schema
```typescript
export const syncQueue = sqliteTable('sync_queue', {
  id: text('id').primaryKey(),
  table: text('table').notNull(),
  operation: text('operation', { enum: ['create', 'update', 'delete'] }).notNull(),
  payload: text('payload').notNull(),  // JSON stringified
  createdAt: integer('created_at', { mode: 'timestamp' }).$defaultFn(() => new Date()),
  attempts: integer('attempts').default(0).notNull(),
  lastError: text('last_error'),
})
```

### Process queue when online
```typescript
export async function processSyncQueue() {
  const pending = await db.select().from(syncQueue)
    .orderBy(asc(syncQueue.createdAt))
    .limit(50)  // Process in batches

  for (const item of pending) {
    try {
      const payload = JSON.parse(item.payload)
      await apiClient.request({
        method: item.operation === 'create' ? 'POST'
               : item.operation === 'update' ? 'PATCH' : 'DELETE',
        url: `/${item.table}${item.operation !== 'create' ? `/${payload.id}` : ''}`,
        data: item.operation !== 'delete' ? payload : undefined,
      })
      await db.delete(syncQueue).where(eq(syncQueue.id, item.id))
    } catch (error) {
      await db.update(syncQueue)
        .set({
          attempts: item.attempts + 1,
          lastError: error instanceof Error ? error.message : 'Unknown error',
        })
        .where(eq(syncQueue.id, item.id))
    }
  }
}
```

## Network Status Pattern

```typescript
import NetInfo from '@react-native-community/netinfo'

export function useNetworkStatus() {
  const [isOnline, setIsOnline] = useState(true)
  const [connectionType, setConnectionType] = useState<string>('unknown')

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener((state) => {
      const online = Boolean(state.isConnected && state.isInternetReachable)
      setIsOnline(online)
      setConnectionType(state.type)

      if (online) {
        // Process any queued sync operations
        processSyncQueue()
        // Invalidate stale queries
        queryClient.invalidateQueries()
      }
    })
    return unsubscribe
  }, [])

  return { isOnline, connectionType }
}
```

## Conflict Resolution Strategies

### 1. Last-Write-Wins (simplest)
```typescript
// Server timestamp always wins
if (serverRecord.updatedAt > localRecord.updatedAt) {
  await db.update(items).set(serverRecord).where(eq(items.id, serverRecord.id))
}
```

### 2. Client-Wins (for local-first apps)
```typescript
// Local changes always win — used for user preferences
await apiClient.put(`/items/${id}`, localRecord)
await db.update(items).set({ syncedAt: new Date() }).where(eq(items.id, id))
```

### 3. Field-Level Merge (for collaborative apps)
```typescript
// Merge non-conflicting fields, flag conflicts for user resolution
const merged = {
  ...serverRecord,
  ...localChanges,  // Local wins for changed fields
  hasConflict: hasConflictingChanges(serverRecord, localChanges),
}
```

## Offline UI Patterns

### Offline banner
```tsx
export function OfflineBanner() {
  const { isOnline } = useNetworkStatus()
  if (isOnline) return null
  return (
    <Animated.View entering={FadeInDown} exiting={FadeOutUp}
      className="bg-amber-500 px-4 py-2 items-center">
      <Text className="text-white text-sm font-medium">
        Offline — changes saved locally
      </Text>
    </Animated.View>
  )
}
```

### Offline-aware mutation
```typescript
export function useCreateItem() {
  const { isOnline } = useNetworkStatus()

  return useMutation({
    mutationFn: async (data: NewItem) => {
      // Always write to SQLite first
      const id = await createItem(data)

      if (isOnline) {
        // Try to sync immediately
        await processSyncQueue()
      }
      // If offline, sync queue handles it on reconnect

      return id
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['items'] })
    },
  })
}
```

## Checklist

Before marking any data feature as complete:
- [ ] Feature works with airplane mode ON
- [ ] Data persists across app restarts
- [ ] Sync queue processes on reconnect
- [ ] Conflict resolution strategy documented
- [ ] Offline banner shows within 2 seconds of going offline
- [ ] Cached data shown when offline (not empty state)
