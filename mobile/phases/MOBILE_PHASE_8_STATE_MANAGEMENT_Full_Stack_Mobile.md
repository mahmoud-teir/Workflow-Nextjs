<a name="phase-m8"></a>
# 📌 MOBILE PHASE M8: STATE MANAGEMENT (UI Architect)

> **Rule:** State must be driven by Unidirectional Data Flow (UDF). The `ViewModel` holds `StateFlow`. Composables observe via `collectAsStateWithLifecycle()`. Never pass the ViewModel down the composable tree.

---

### Prompt M8.1: MVVM + UDF ViewModel Setup

```text
You are an Android UI Architect. Implement a standardized ViewModel for a feature using `StateFlow` and Dagger Hilt.

Requirements:
- Define a single Data Class representing the UI State.
- Define a Sealed Class for UI Events (actions the user can take).
- Use `MutableStateFlow` and expose it as `StateFlow`.
- Use `.update {}` to atomically modify state.

Required Output Format: Provide complete code for `ui/features/home/HomeViewModel.kt`:

```kotlin
package com.example.app.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.model.Item
import com.example.app.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Define the complete State for this screen
data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

// 2. Define actions the UI can send to the ViewModel
sealed interface HomeEvent {
    data object Refresh : HomeEvent()
    data class DeleteItem(val id: String) : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Observe the single source of truth (Room DB Flow)
        viewModelScope.launch {
            repository.getItemsFlow().collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    // 3. Process events from the UI
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Refresh -> refreshData()
            is HomeEvent.DeleteItem -> deleteItem(event.id)
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.refreshItems()
            if (result is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
            // If Success, the Flow collected in init {} will automatically update the UI
        }
    }
    
    private fun deleteItem(id: String) {
        // Implementation
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: Creating multiple `LiveData` or `StateFlow` variables (e.g., `isLoadingFlow`, `itemsFlow`).
- Solution: Combine them into a single `HomeUiState` data class so the UI always renders a consistent snapshot.
```

---

### Prompt M8.2: State Hoisting in Compose

```text
You are a Compose Developer. Implement the View layer that connects to the ViewModel.

Requirements:
- Use `hiltViewModel()` to get the ViewModel at the Route level.
- Use `collectAsStateWithLifecycle()` to safely observe state.
- Pass the state and an event lambda to the actual UI Composable (State Hoisting).

Required Output Format: Provide complete code for `ui/features/home/HomeScreen.kt`:

```kotlin
package com.example.app.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// 1. The Route Composable (Connected to ViewModel)
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    // Safely collect state aware of Android lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail
    )
}

// 2. The Stateless UI Composable (Previewable, easily testable)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading && uiState.items.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(uiState.error, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = uiState.items,
                        key = { it.id } // ALWAYS use keys in lists
                    ) { item ->
                        ItemCard(
                            item = item,
                            onClick = { onNavigateToDetail(item.id) },
                            onDelete = { onEvent(HomeEvent.DeleteItem(item.id)) }
                        )
                    }
                }
            }
        }
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: Using `.collectAsState()` instead of `.collectAsStateWithLifecycle()`.
- Solution: `collectAsState()` keeps collecting in the background even if the app is minimized, wasting battery. Always use the `lifecycle` version to pause collection when stopped.
```

---

### Prompt M8.3: Process Death (SavedStateHandle)

```text
You are an Android Architect. ViewModels are destroyed when Android kills the app in the background. Implement `SavedStateHandle` to preserve critical UI state (like scroll position, current tab, or form input).

Required Output Format: Provide a code snippet for handling Process Death:

```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ItemRepository
) : ViewModel() {

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
    }

    // 1. Initialize from saved state (persists across process death)
    private val _searchQuery = MutableStateFlow(
        savedStateHandle.get<String>(KEY_SEARCH_QUERY) ?: ""
    )
    val searchQuery = _searchQuery.asStateFlow()

    // 2. Update state AND saved state simultaneously
    fun updateQuery(query: String) {
        _searchQuery.value = query
        savedStateHandle[KEY_SEARCH_QUERY] = query
    }
}
```
```

---

✅ **Verification Checklist:**
- [ ] ViewModel uses `StateFlow`, not `LiveData`.
- [ ] UI collects via `collectAsStateWithLifecycle()`.
- [ ] ViewModel is NOT passed as a parameter to the actual UI drawing components.
- [ ] Changing system language or theme (Configuration Change) preserves state.
- [ ] Killing the process via ADB (`adb shell am kill com.example.app`) and returning restores search queries or tabs via `SavedStateHandle`.

---

📎 **Related Phases:**
- Prerequisites: [Phase M6: UI Components](./MOBILE_PHASE_6_UI_COMPONENTS_DESIGN_SYSTEM_Frontend_Developer.md)
- Proceeds to: [Phase M9: Testing & QA](./MOBILE_PHASE_9_TESTING_QA_QA_Engineer.md)
