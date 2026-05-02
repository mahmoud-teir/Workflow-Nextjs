package com.manarat.manaraalislam.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manarat.manaraalislam.data.models.Lesson
import com.manarat.manaraalislam.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolScreen(
    viewModel: SchoolViewModel,
    onLessonSelect: (Lesson, Int) -> Unit
) {
    val levels = viewModel.levels
    val progress by viewModel.progress.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Islamic School")
                        Text(
                            "Points: ${progress?.totalPoints ?: 0}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(levels) { level ->
                LevelItem(
                    level = level,
                    completedLessonIds = progress?.completedLessonIds ?: emptySet(),
                    onLessonSelect = { lesson -> onLessonSelect(lesson, level.number) }
                )
            }
        }
    }
}

@Composable
fun LevelItem(
    level: com.manarat.manaraalislam.data.models.Level,
    completedLessonIds: Set<String>,
    onLessonSelect: (Lesson) -> Unit
) {
    Column {
        Text(
            text = level.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        level.lessons.forEach { lesson ->
            val isCompleted = completedLessonIds.contains(lesson.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onLessonSelect(lesson) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = lesson.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = lesson.description, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                    if (isCompleted) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600))
                    }
                }
            }
        }
    }
}
