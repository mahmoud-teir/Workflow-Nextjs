<a name="phase-m9"></a>
# 📌 MOBILE PHASE M9: TESTING & QA (QA Engineer)

> **Rule:** Unit tests MUST cover ViewModels and Repositories (JUnit + MockK). UI Tests MUST cover core Composables (Compose Test Rule). E2E Tests MUST cover critical user flows (Maestro).

---

### Prompt M9.1: ViewModel Unit Testing (JUnit + MockK)

```text
You are an Android QA Engineer. Write Unit Tests for the ViewModel using MockK and Turbine.

Requirements:
- Add test dependencies: `mockk`, `turbine` (for flow testing), and `kotlinx-coroutines-test`.
- Write a test verifying that `HomeViewModel` emits `isLoading = true`, fetches data, and emits `isLoading = false` with data.
- Setup the `MainDispatcherRule` to replace the Main thread in unit tests.

Required Output Format: Provide complete code for `test/java/.../HomeViewModelTest.kt`:

```kotlin
package com.example.app.ui.features.home

import app.cash.turbine.test
import com.example.app.domain.model.Item
import com.example.app.domain.repository.ItemRepository
import com.example.app.domain.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val repository: ItemRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Default mock behavior
        coEvery { repository.getItemsFlow() } returns flowOf(emptyList())
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when data is fetched successfully, state updates with items`() = runTest {
        // Arrange
        val mockItems = listOf(Item("1", "Title", "Subtitle"))
        coEvery { repository.getItemsFlow() } returns flowOf(mockItems)
        
        // Re-initialize to trigger the init block with new mock
        viewModel = HomeViewModel(repository)

        // Act & Assert using Turbine
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(true, initialState.isLoading)
            
            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertEquals(1, loadedState.items.size)
            assertEquals("Title", loadedState.items[0].title)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: "Module with the Main dispatcher had failed to initialize" error.
- Solution: Android's Main thread (Looper) doesn't exist in local JUnit tests. Always use `Dispatchers.setMain()` to inject a TestDispatcher.
```

---

### Prompt M9.2: Compose UI Testing

```text
You are an Android SDET. Write UI Tests for the Jetpack Compose screens.

Requirements:
- Use `createComposeRule()`.
- Test that the screen displays a loading spinner when `isLoading` is true.
- Test that clicking an item fires the correct callback.

Required Output Format: Provide code for `androidTest/java/.../HomeScreenTest.kt`:

```kotlin
package com.example.app.ui.features.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.app.domain.model.Item
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsProgressIndicator() {
        // Arrange
        val state = HomeUiState(isLoading = true, items = emptyList())

        // Act
        composeTestRule.setContent {
            HomeScreen(uiState = state, onEvent = {}, onNavigateToDetail = {})
        }

        // Assert
        // We find the progress indicator by asserting it exists (usually needs a testTag in production)
        composeTestRule.onNode(isSystemInProgressIndicator()).assertExists()
        // Or using a tag: composeTestRule.onNodeWithTag("loading_spinner").assertIsDisplayed()
    }

    @Test
    fun loadedState_showsItemsAndHandlesClicks() {
        // Arrange
        var clickedItemId: String? = null
        val items = listOf(Item("1", "Test Item", "Desc"))
        val state = HomeUiState(isLoading = false, items = items)

        // Act
        composeTestRule.setContent {
            HomeScreen(
                uiState = state, 
                onEvent = {}, 
                onNavigateToDetail = { clickedItemId = it }
            )
        }

        // Assert
        composeTestRule.onNodeWithText("Test Item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Item").performClick()
        
        assert(clickedItemId == "1")
    }
}

// Helper matcher for CircularProgressIndicator (which has the ProgressBar semantics)
fun isSystemInProgressIndicator() = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
```
```

---

### Prompt M9.3: E2E Automation (Maestro)

```text
You are an Automation Engineer. Create Maestro flows for the critical user journeys on Android.

Requirements:
- Create a YAML file targeting the Android `appId`.
- Create a flow that logs in, navigates to Home, and clicks an item.

Required Output Format: Provide complete code for `.maestro/login_to_home.yaml`:

```yaml
appId: com.example.app
---
- launchApp
- assertVisible: "Welcome Back"  # Assuming text on screen

# Login Flow
- tapOn: "Email"
- inputText: "test@example.com"
- tapOn: "Password"
- inputText: "password123"
- tapOn: "Login"

# Verify Home Screen
- assertVisible: "Home"
- assertVisible: ".*Test Item.*" # Regex match

# Navigate to Detail
- tapOn: "Test Item"
- assertVisible: "Item Details"
- tapOn: "Back" # Using contentDescription for navigation icon

- stopApp
```
```

---

✅ **Verification Checklist:**
- [ ] ViewModel unit tests pass locally without an emulator (`./gradlew testDebugUnitTest`).
- [ ] Compose UI tests pass on an emulator (`./gradlew connectedDebugAndroidTest`).
- [ ] Maestro flow successfully executes on a running emulator (`maestro test .maestro/login_to_home.yaml`).

---

📎 **Related Phases:**
- Prerequisites: [Phase M8: State Management](./MOBILE_PHASE_8_STATE_MANAGEMENT_Full_Stack_Mobile.md)
- Proceeds to: [Phase M10: Performance Optimization](./MOBILE_PHASE_10_PERFORMANCE_OPTIMIZATION_Mobile_Developer.md)
