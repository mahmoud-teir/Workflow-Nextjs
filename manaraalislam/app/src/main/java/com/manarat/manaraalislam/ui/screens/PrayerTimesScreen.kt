package com.manarat.manaraalislam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manarat.manaraalislam.ui.viewmodel.PrayerViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(viewModel: PrayerViewModel) {
    val prayerTimes by viewModel.prayerTimes
    val hijriDate by viewModel.hijriDate

    // Mock location for MVP
    LaunchedEffect(Unit) {
        viewModel.updateLocation(21.4225, 39.8262) // Mecca
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(title = { Text("Tools") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Hijri Calendar", style = MaterialTheme.typography.titleMedium)
                        Text(text = hijriDate, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            item {
                Text(text = "Prayer Times", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            prayerTimes?.let { times ->
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val zoneId = ZoneId.systemDefault()

                val prayerList = listOf(
                    "Fajr" to times.fajr,
                    "Sunrise" to times.sunrise,
                    "Dhuhr" to times.dhuhr,
                    "Asr" to times.asr,
                    "Maghrib" to times.maghrib,
                    "Isha" to times.isha
                )

                items(prayerList) { (name, time) ->
                    val localTime = time.toInstant().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = localTime.format(timeFormatter),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
