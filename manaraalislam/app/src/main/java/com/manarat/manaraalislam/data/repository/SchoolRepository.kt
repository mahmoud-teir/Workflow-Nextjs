package com.manarat.manaraalislam.data.repository

import com.manarat.manaraalislam.data.local.ProgressDao
import com.manarat.manaraalislam.data.local.ProgressEntity
import com.manarat.manaraalislam.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SchoolRepository(private val progressDao: ProgressDao) {

    fun getLevels(): List<Level> {
        return (1..10).map { levelNum ->
            Level(
                number = levelNum,
                title = "Level $levelNum: " + when(levelNum) {
                    1 -> "Foundations of Faith"
                    2 -> "Pillars of Islam"
                    3 -> "Introduction to Quran"
                    else -> "Advanced Islamic Studies $levelNum"
                },
                lessons = listOf(
                    Lesson(
                        id = "L${levelNum}_1",
                        title = "Introduction to Level $levelNum",
                        description = "In this lesson, we will cover the basics of Level $levelNum.",
                        videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        quiz = Quiz(
                            questions = listOf(
                                Question("What is the main topic?", listOf("Faith", "Ethics", "History"), 0),
                                Question("Is this Level $levelNum?", listOf("Yes", "No"), 0)
                            )
                        )
                    ),
                    Lesson(
                        id = "L${levelNum}_2",
                        title = "Deep Dive into Level $levelNum",
                        description = "Continuing our journey in Level $levelNum.",
                        videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                        quiz = Quiz(
                            questions = listOf(
                                Question("Did you learn something new?", listOf("Yes", "Maybe", "No"), 0)
                            )
                        )
                    )
                )
            )
        }
    }

    fun getProgress(userId: String): Flow<ProgressEntity?> = progressDao.getProgress(userId)

    suspend fun completeLesson(userId: String, lessonId: String, points: Int) {
        val currentProgress = progressDao.getProgress(userId).firstOrNull()
            ?: ProgressEntity(userId, emptySet(), 0, emptySet())
        
        if (!currentProgress.completedLessonIds.contains(lessonId)) {
            val newCompleted = currentProgress.completedLessonIds + lessonId
            val newPoints = currentProgress.totalPoints + points
            val newBadges = currentProgress.badges.toMutableSet()
            
            if (newCompleted.size >= 1) newBadges.add("Beginner Learner")
            if (newCompleted.size >= 5) newBadges.add("Lesson Master")
            if (newPoints >= 100) newBadges.add("Point Collector")

            progressDao.updateProgress(currentProgress.copy(
                completedLessonIds = newCompleted,
                totalPoints = newPoints,
                badges = newBadges
            ))
        }
    }
}
