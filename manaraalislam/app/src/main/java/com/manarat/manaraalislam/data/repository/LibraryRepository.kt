package com.manarat.manaraalislam.data.repository

import com.manarat.manaraalislam.data.local.BookmarkDao
import com.manarat.manaraalislam.data.local.BookmarkEntity
import com.manarat.manaraalislam.data.models.AthkarCategory
import com.manarat.manaraalislam.data.models.AthkarItem
import com.manarat.manaraalislam.data.models.Ayah
import com.manarat.manaraalislam.data.models.Surah
import kotlinx.coroutines.flow.Flow

class LibraryRepository(private val bookmarkDao: BookmarkDao) {

    fun getSurahs(): List<Surah> {
        return listOf(
            Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", 7, "Meccan"),
            Surah(2, "البقرة", "Al-Baqarah", "The Cow", 286, "Medinan"),
            Surah(112, "الإخلاص", "Al-Ikhlas", "Sincerity", 4, "Meccan"),
            Surah(113, "الفلق", "Al-Falaq", "The Daybreak", 5, "Meccan"),
            Surah(114, "الناس", "An-Nas", "Mankind", 6, "Meccan")
        )
    }

    fun getAyahs(surahNumber: Int): List<Ayah> {
        // Placeholder ayahs for MVP
        return when (surahNumber) {
            1 -> listOf(
                Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful", 1, 1),
                Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "All praise is due to Allah, Lord of the worlds", 1, 2)
            )
            else -> emptyList()
        }
    }

    fun getAthkarCategories(): List<String> {
        return listOf("Morning", "Evening", "After Prayer", "Before Sleeping")
    }

    fun getAthkar(category: String): List<AthkarItem> {
        return when (category) {
            "Morning" -> listOf(
                AthkarItem("أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ", 1, "Once in the morning", "Muslim 4/2088"),
                AthkarItem("اللَّهُمَّ بِكَ أَصْبَحْنَا", 1, "Once in the morning", "Tirmidhi 3391")
            )
            else -> emptyList()
        }
    }

    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(bookmark: BookmarkEntity) = bookmarkDao.insertBookmark(bookmark)

    suspend fun removeBookmark(bookmark: BookmarkEntity) = bookmarkDao.deleteBookmark(bookmark)

    fun isBookmarked(surahNumber: Int, ayahNumber: Int) = bookmarkDao.isBookmarked(surahNumber, ayahNumber)
}
