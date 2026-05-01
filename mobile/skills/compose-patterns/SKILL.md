---
name: compose-patterns
description: Use this skill when writing Jetpack Compose UI, ViewModels, or handling state. Enforces MVVM, UDF, and Material Design 3.
origin: Mobile ECC
stack: Jetpack Compose, Material 3, ViewModel, StateFlow
---

# Jetpack Compose Patterns Skill

## Core Architecture Principles

### Unidirectional Data Flow (UDF)
State flows down (from ViewModel to UI), events flow up (from UI to ViewModel).

### ViewModel Structure
```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: ExampleEvent) {
        when (event) {
            is ExampleEvent.OnClick -> _uiState.update { it.copy(count = it.count + 1) }
        }
    }
}
```

## Compose UI Patterns

### State Hoisting
```kotlin
@Composable
fun ExampleRoute(viewModel: ExampleViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExampleScreen(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
fun ExampleScreen(uiState: ExampleUiState, onEvent: (ExampleEvent) -> Unit) {
    Button(onClick = { onEvent(ExampleEvent.OnClick) }) { Text("Count: ${uiState.count}") }
}
```

### Modifiers
Always provide `Modifier` as the first optional parameter.
```kotlin
@Composable
fun CustomCard(
    title: String,
    modifier: Modifier = Modifier
) { ... }
```

## Performance Rules
1. **Use `remember`** for expensive computations in composition.
2. **Use `key`** for `LazyColumn` items.
3. **Pass primitives** or stable classes to Composables. Avoid passing interfaces or standard `List`.
