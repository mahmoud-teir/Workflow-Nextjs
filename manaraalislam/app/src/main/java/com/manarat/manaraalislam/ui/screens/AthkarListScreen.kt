package com.manarat.manaraalislam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manarat.manaraalislam.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthkarListScreen(
    category: String,
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    val athkar = remember(category) { viewModel.getAthkar(category) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            items(athkar) { item ->
                var currentCount by remember { mutableIntStateOf(0) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { if (currentCount < item.count) currentCount++ }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (item.description != null) {
                            Text(text = item.description, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.reference ?: "", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${currentCount}/${item.count}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (currentCount == item.count) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
