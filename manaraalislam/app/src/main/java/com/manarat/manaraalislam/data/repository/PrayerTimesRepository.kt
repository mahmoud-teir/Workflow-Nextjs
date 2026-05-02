package com.manarat.manaraalislam.data.repository

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import kotlinx.datetime.*
import java.util.*

class PrayerTimesRepository {

    fun getPrayerTimes(lat: Double, lon: Double, date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date): PrayerTimes {
        val coordinates = Coordinates(lat, lon)
        val dateComponents = DateComponents(date.year, date.monthNumber, date.dayOfMonth)
        val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        return PrayerTimes(coordinates, dateComponents, params)
    }

    fun getHijriDate(): String {
        // Built-in Hijri calendar (java.time.chrono)
        val today = java.time.LocalDate.now()
        val hijriDate = java.time.chrono.HijrahDate.from(today)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())
        return "Hijri: ${formatter.format(hijriDate)}"
    }
}
