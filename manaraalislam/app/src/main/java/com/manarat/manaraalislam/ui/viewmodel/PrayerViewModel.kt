package com.manarat.manaraalislam.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.batoulapps.adhan2.PrayerTimes
import com.manarat.manaraalislam.data.repository.PrayerTimesRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PrayerViewModel(private val repository: PrayerTimesRepository) : ViewModel() {

    private val _prayerTimes = mutableStateOf<PrayerTimes?>(null)
    val prayerTimes: State<PrayerTimes?> = _prayerTimes

    private val _hijriDate = mutableStateOf("")
    val hijriDate: State<String> = _hijriDate

    init {
        _hijriDate.value = repository.getHijriDate()
    }

    fun updateLocation(lat: Double, lon: Double) {
        _prayerTimes.value = repository.getPrayerTimes(lat, lon)
    }
}
