package com.manarat.manaraalislam.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.manarat.manaraalislam.ui.viewmodel.SchoolViewModel

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun LessonDetailScreen(
    lessonId: String,
    levelNumber: Int,
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val level = viewModel.levels.find { it.number == levelNumber }
    val lesson = level?.lessons?.find { it.id == lessonId } ?: return
    
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(lesson.videoUrl))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "About this lesson", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = lesson.description, style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onStartQuiz,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Quiz")
                }
            }
        }
    }
}
