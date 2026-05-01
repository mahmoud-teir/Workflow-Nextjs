<a name="phase-m4"></a>
# 📌 MOBILE PHASE M4: DATABASE & OFFLINE STORAGE (Offline-First Architect)

> **Rule:** Native Android offline storage requires Room Database for relational data and Jetpack DataStore (Preferences) for simple key-value pairs. DO NOT use raw SharedPreferences for new projects.

---

### Prompt M4.1: Room Database Setup

```text
You are an Android Offline-First Architect. Set up Room Database for [AppName].

Requirements:
- Define a Room `@Entity` with a client-generated UUID as the primary key.
- Create a `@Dao` with basic CRUD operations.
- Expose read operations as Kotlin `Flow<List<T>>` to ensure UI automatically updates when the DB changes.
- Provide a Dagger Hilt module to inject the DAO.

Required Output Format: Provide complete code for:

1. Entity `data/local/entity/ItemEntity.kt`:
```kotlin
package com.example.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String, // UUID generated client-side
    val title: String,
    val description: String,
    val createdAt: Long,
    val syncedAt: Long? = null, // Null if pending sync
    val isDeleted: Boolean = false // Soft delete for sync
)
```

2. DAO `data/local/dao/ItemDao.kt`:
```kotlin
package com.example.app.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    // Flow automatically emits new data whenever the table changes
    @Query("SELECT * FROM items WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getItems(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    // Soft delete
    @Query("UPDATE items SET isDeleted = 1 WHERE id = :id")
    suspend fun markAsDeleted(id: String)
    
    @Query("SELECT * FROM items WHERE syncedAt IS NULL OR isDeleted = 1")
    suspend fun getUnsyncedItems(): List<ItemEntity>
}
```

3. Database Definition `data/local/AppDatabase.kt`:
```kotlin
package com.example.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.app.data.local.dao.ItemDao
import com.example.app.data.local.entity.ItemEntity

@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract val itemDao: ItemDao
}
```

4. Hilt Module `di/DatabaseModule.kt`:
```kotlin
package com.example.app.di

import android.app.Application
import androidx.room.Room
import com.example.app.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_database.db"
        )
        // .fallbackToDestructiveMigration() // Use only in early dev
        .build()
    }

    @Provides
    @Singleton
    fun provideItemDao(db: AppDatabase) = db.itemDao
}
```
```

---

### Prompt M4.2: Single Source of Truth (Offline-First Repository)

```text
You are a Data Architect. Implement the "Single Source of Truth" pattern in the Repository.

Requirements:
- The UI should ONLY observe data from the local Room database (via Flow).
- The network fetch function should download data, insert it into Room, and return nothing (or just a status).
- The Room database acts as the single source of truth.

Required Output Format: Provide complete code for `data/repository/ItemRepositoryImpl.kt`:

```kotlin
package com.example.app.data.repository

import com.example.app.data.local.dao.ItemDao
import com.example.app.data.remote.ApiService
import com.example.app.domain.model.Item
import com.example.app.domain.repository.ItemRepository
import com.example.app.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val dao: ItemDao,
    private val api: ApiService
) : ItemRepository {

    // UI observes this Flow. Automatically updates when local DB changes.
    override fun getItemsFlow(): Flow<List<Item>> {
        return dao.getItems().map { entities -> 
            entities.map { it.toDomainModel() } 
        }
    }

    // Call this manually (e.g., Pull-to-refresh) or on a worker
    override suspend fun refreshItems(): Resource<Unit> {
        return try {
            val response = api.getItems()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                // Insert into local DB. This triggers getItemsFlow() to emit.
                dao.insertItems(dtos.map { it.toEntity() })
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message() ?: "Error")
            }
        } catch (e: Exception) {
            Resource.Error("Sync failed: ${e.message}")
        }
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: Returning network data directly to the UI, while also trying to save it to Room. This creates two sources of truth and causes UI synchronization bugs.
- Solution: The network layer should ONLY write to Room. The UI layer should ONLY read from Room via Flow.
```

---

### Prompt M4.3: Preferences DataStore (Key-Value)

```text
You are an Android Developer. Implement Jetpack DataStore for user preferences (replacing legacy SharedPreferences).

Requirements:
- Create a DataStore wrapper using Hilt.
- Expose preferences as Kotlin `Flow`.
- Store simple things like `isFirstLaunch`, `themePreference`.

Required Output Format: Provide complete code for `data/local/SettingsDataStore.kt`:

```kotlin
package com.example.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore @Inject constructor(private val context: Context) {

    companion object {
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val THEME_MODE = intPreferencesKey("theme_mode") // 0=Auto, 1=Light, 2=Dark
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }
    
    // Theme mode flow...
}
```
```

---

✅ **Verification Checklist:**
- [ ] Room Database compiles without schema errors.
- [ ] DAOs return `Flow` for reactive UI updates.
- [ ] The Repository implements the Single Source of Truth pattern (UI only reads from DB).
- [ ] Jetpack DataStore is configured using the top-level property delegate.

---

📎 **Related Phases:**
- Prerequisites: [Phase M3: Backend & API](./MOBILE_PHASE_3_BACKEND_API_INTEGRATION_Full_Stack.md)
- Proceeds to: [Phase M5: Authentication & Security](./MOBILE_PHASE_5_AUTHENTICATION_SECURITY_Security_Expert.md)
