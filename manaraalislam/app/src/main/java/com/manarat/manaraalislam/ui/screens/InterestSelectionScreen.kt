package com.manarat.manaraalislam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manarat.manaraalislam.ui.viewmodel.AuthViewModel

@Composable
fun InterestSelectionScreen(
    viewModel: AuthViewModel,
    onSelectionComplete: () -> Unit
) {
    val interests = listOf("Quran", "Hadith", "Ethics", "History", "Fiqh", "Athkar")
    val selectedInterests = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("What would you like to learn?") }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        viewModel.updateInterests(selectedInterests.toList())
                        onSelectionComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = selectedInterests.isNotEmpty()
                ) {
                    Text("Continue")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(interests) { interest ->
                FilterChip(
                    selected = selectedInterests.contains(interest),
                    onClick = {
                        if (selectedInterests.contains(interest)) {
                            selectedInterests.remove(interest)
                        } else {
                            selectedInterests.add(interest)
                        }
                    },
                    label = { Text(interest) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
