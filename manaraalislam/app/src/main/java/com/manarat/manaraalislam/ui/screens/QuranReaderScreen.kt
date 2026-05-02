package com.manarat.manaraalislam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manarat.manaraalislam.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    surahName: String,
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    val ayahs = remember(surahNumber) { viewModel.getAyahs(surahNumber) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(surahName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ayahs) { ayah ->
                val isBookmarked by viewModel.isBookmarked(surahNumber, ayah.numberInSurah).collectAsState(initial = false)
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "(${ayah.numberInSurah})", style = MaterialTheme.typography.labelSmall)
                            IconButton(onClick = {
                                if (isBookmarked) {
                                    // Normally we would find the entity to remove, but for MVP let's just add
                                    // repository should handle deletion by key
                                } else {
                                    viewModel.addBookmark(surahNumber, surahName, ayah.numberInSurah, ayah.text)
                                }
                            }) {
                                Icon(
                                    if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark"
                                )
                            }
                        }
                        Text(
                            text = ayah.text,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ayah.translation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
