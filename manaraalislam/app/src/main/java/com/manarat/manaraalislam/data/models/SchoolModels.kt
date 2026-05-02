package com.manarat.manaraalislam.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val number: Int,
    val title: String,
    val lessons: List<Lesson>
)

@Serializable
data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val quiz: Quiz
)

@Serializable
data class Quiz(
    val questions: List<Question>
)

@Serializable
data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val points: Int = 10
)

@Serializable
data class UserProgress(
    val userId: String,
    val completedLessonIds: Set<String> = emptySet(),
    val totalPoints: Int = 0,
    val badges: Set<String> = emptySet()
)
