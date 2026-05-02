package com.manarat.manaraalislam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manarat.manaraalislam.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    lessonId: String,
    levelNumber: Int,
    userId: String,
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    onQuizComplete: () -> Unit
) {
    val level = viewModel.levels.find { it.number == levelNumber }
    val lesson = level?.lessons?.find { it.id == lessonId } ?: return
    val quiz = lesson.quiz
    
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    val currentQuestion = quiz.questions[currentQuestionIndex]

    if (isFinished) {
        LaunchedEffect(Unit) {
            viewModel.completeLesson(userId, lessonId, score)
        }
        
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Quiz Complete!", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "You earned $score points", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onQuizComplete) {
                    Text("Back to School")
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quiz: ${lesson.title}") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / quiz.questions.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentQuestion.text, style = MaterialTheme.typography.headlineSmall)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                currentQuestion.options.forEachIndexed { index, option ->
                    OutlinedButton(
                        onClick = { selectedOptionIndex = index },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = if (selectedOptionIndex == index) 
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(text = option)
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        if (selectedOptionIndex == currentQuestion.correctAnswerIndex) {
                            score += currentQuestion.points
                        }
                        
                        if (currentQuestionIndex < quiz.questions.size - 1) {
                            currentQuestionIndex++
                            selectedOptionIndex = null
                        } else {
                            isFinished = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedOptionIndex != null
                ) {
                    Text(if (currentQuestionIndex < quiz.questions.size - 1) "Next Question" else "Finish Quiz")
                }
            }
        }
    }
}
