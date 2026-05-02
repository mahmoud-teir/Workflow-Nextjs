package com.manarat.manaraalislam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manarat.manaraalislam.data.local.ProgressEntity
import com.manarat.manaraalislam.data.models.Level
import com.manarat.manaraalislam.data.repository.SchoolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SchoolViewModel(
    private val repository: SchoolRepository,
    userId: String
) : ViewModel() {

    val levels: List<Level> = repository.getLevels()

    val progress: StateFlow<ProgressEntity?> = repository.getProgress(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun completeLesson(userId: String, lessonId: String, points: Int) {
        viewModelScope.launch {
            repository.completeLesson(userId, lessonId, points)
        }
    }
}
