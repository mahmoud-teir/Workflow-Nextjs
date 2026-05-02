package com.manarat.manaraalislam.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manarat.manaraalislam.data.models.Surah
import com.manarat.manaraalislam.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onSurahClick: (Surah) -> Unit,
    onAthkarClick: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Quran", "Athkar")

    Scaffold(
        topBar = {
            Column {
                LargeTopAppBar(title = { Text("Library") })
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> QuranList(viewModel.surahs, onSurahClick)
                1 -> AthkarCategories(viewModel.getAthkarCategories(), onAthkarClick)
            }
        }
    }
}

@Composable
fun QuranList(surahs: List<Surah>, onSurahClick: (Surah) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(surahs) { surah ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSurahClick(surah) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "${surah.number}. ${surah.englishName}", style = MaterialTheme.typography.titleMedium)
                        Text(text = surah.englishNameTranslation, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = surah.name,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun AthkarCategories(categories: List<String>, onAthkarClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAthkarClick(category) }
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.AutoStories, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = category, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
