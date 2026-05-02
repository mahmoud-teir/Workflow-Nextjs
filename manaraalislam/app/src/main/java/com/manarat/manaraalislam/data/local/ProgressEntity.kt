package com.manarat.manaraalislam.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "user_progress")
data class ProgressEntity(
    @PrimaryKey val userId: String,
    val completedLessonIds: Set<String>,
    val totalPoints: Int,
    val badges: Set<String>
)

class ProgressConverters {
    @TypeConverter
    fun fromSet(value: Set<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toSet(value: String): Set<String> = Json.decodeFromString(value)
}
