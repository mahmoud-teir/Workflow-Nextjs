package com.manarat.manaraalislam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manarat.manaraalislam.data.local.BookmarkEntity
import com.manarat.manaraalislam.data.models.AthkarItem
import com.manarat.manaraalislam.data.models.Ayah
import com.manarat.manaraalislam.data.models.Surah
import com.manarat.manaraalislam.data.repository.LibraryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: LibraryRepository) : ViewModel() {

    val surahs: List<Surah> = repository.getSurahs()
    
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getAyahs(surahNumber: Int): List<Ayah> = repository.getAyahs(surahNumber)

    fun getAthkarCategories(): List<String> = repository.getAthkarCategories()

    fun getAthkar(category: String): List<AthkarItem> = repository.getAthkar(category)

    fun addBookmark(surahNumber: Int, surahName: String, ayahNumber: Int, text: String) {
        viewModelScope.launch {
            repository.addBookmark(BookmarkEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                text = text
            ))
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
        }
    }

    fun isBookmarked(surahNumber: Int, ayahNumber: Int) = repository.isBookmarked(surahNumber, ayahNumber)
}
