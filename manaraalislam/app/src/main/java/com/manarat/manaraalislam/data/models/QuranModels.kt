package com.manarat.manaraalislam.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@Serializable
data class Ayah(
    val number: Int,
    val text: String,
    val translation: String,
    val surahNumber: Int,
    val numberInSurah: Int
)

@Serializable
data class SurahDetail(
    val number: Int,
    val name: String,
    val ayahs: List<Ayah>
)
