---
name: offline-first
description: Offline-first architecture using Room Database and Repository pattern.
origin: Mobile ECC
stack: Room Database, Retrofit, Kotlin Flow
---

# Android Offline-First Skill

## Core Principle
The Room Database is the Single Source of Truth. The UI observes Room via Kotlin `Flow`. The network layer updates Room.

## Room Database Patterns

### Entity with Sync Tracking
```kotlin
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String, // UUID generated on client
    val title: String,
    val isDeleted: Boolean = false, // Soft delete
    val syncedAt: Long? = null
)
```

### Flow DAO
```kotlin
@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE isDeleted = 0")
    fun getItemsFlow(): Flow<List<ItemEntity>> // Emits automatically on change
}
```

## Repository Pattern
```kotlin
class ItemRepositoryImpl @Inject constructor(
    private val dao: ItemDao,
    private val api: ApiService
) : ItemRepository {

    override fun getItemsFlow(): Flow<List<Item>> {
        return dao.getItemsFlow().map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val dtos = api.fetchItems()
        dao.insertItems(dtos.map { it.toEntity() })
    }
}
```
