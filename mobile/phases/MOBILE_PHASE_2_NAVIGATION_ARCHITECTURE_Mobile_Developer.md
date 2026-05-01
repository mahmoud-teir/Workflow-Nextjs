<a name="phase-m2"></a>
# 📌 MOBILE PHASE M2: NAVIGATION ARCHITECTURE (Android Architect)

> **Core Philosophy:** Android Navigation in Compose should be Type-Safe (using `kotlinx.serialization`). Avoid string-based routes whenever possible. Use a Single Activity architecture.

---

### Prompt M2.1: Type-Safe Navigation Setup

```text
You are an Android Navigation Expert. Implement the core Navigation Compose architecture for [AppName] using type-safe routing.

Requirements:
- Use `kotlinx.serialization` for route definitions.
- Implement a `NavHost` inside the main layout.
- Separate navigation into graphs (e.g., AuthGraph, MainGraph).
- Provide a `BottomNavigationBar` setup that syncs with the current destination.

Required Output Format: Provide complete code for:

1. Route definitions `ui/navigation/Routes.kt`:
```kotlin
package com.example.app.ui.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable data object AuthGraph : Route()
    @Serializable data object Login : Route()
    @Serializable data object Register : Route()

    @Serializable data object MainGraph : Route()
    @Serializable data object Home : Route()
    @Serializable data object Search : Route()
    @Serializable data object Profile : Route()
    @Serializable data class ItemDetail(val itemId: String) : Route()
}
```

2. Main Navigation Component `ui/navigation/AppNavigation.kt`:
```kotlin
package com.example.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.AuthGraph,
        modifier = modifier
    ) {
        // Auth Graph
        navigation<Route.AuthGraph>(startDestination = Route.Login) {
            composable<Route.Login> {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Route.Register) },
                    onLoginSuccess = {
                        navController.navigate(Route.MainGraph) {
                            popUpTo(Route.AuthGraph) { inclusive = true }
                        }
                    }
                )
            }
            composable<Route.Register> {
                RegisterScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Main App Graph
        navigation<Route.MainGraph>(startDestination = Route.Home) {
            composable<Route.Home> {
                HomeScreen(
                    onNavigateToDetail = { id -> navController.navigate(Route.ItemDetail(id)) }
                )
            }
            composable<Route.Search> {
                SearchScreen()
            }
            composable<Route.Profile> {
                ProfileScreen()
            }
            composable<Route.ItemDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.ItemDetail>()
                ItemDetailScreen(
                    itemId = args.itemId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
```

3. Main Screen with Bottom Navigation `ui/MainScreen.kt`:
```kotlin
package com.example.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.navigation.AppNavigation
import com.example.app.ui.navigation.Route

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Show bottom bar only on specific root screens
    val showBottomBar = currentDestination?.hierarchy?.any { 
        it.hasRoute<Route.Home>() || it.hasRoute<Route.Search>() || it.hasRoute<Route.Profile>() 
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentDestination?.hierarchy?.any { it.hasRoute<Route.Home>() } == true,
                        onClick = {
                            navController.navigate(Route.Home) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    // Repeat for Search and Profile...
                }
            }
        }
    ) { paddingValues ->
        AppNavigation(
            modifier = Modifier.padding(paddingValues),
            navController = navController
        )
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: Defining string-based routes with arguments (`"details/{itemId}"`).
- Solution: Use type-safe Kotlin Serialization routes (`@Serializable data class ItemDetail(val itemId: String)`). This prevents runtime crashes from malformed arguments.
- Pitfall: Passing `NavController` deep into child composables.
- Solution: Pass navigation lambdas (callbacks) down to child composables. Keep `NavController` usage contained to the top-level navigation graph.
```

---

### Prompt M2.2: Deep Links & Intent Handling

```text
You are an Android Navigation Expert. Implement Deep Linking for Navigation Compose.

Required Output Format: Provide complete code for:

1. Modifying the `AndroidManifest.xml` to support the scheme:
```xml
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" android:host="example.com" />
        <data android:scheme="myapp" android:host="example.com" />
    </intent-filter>
</activity>
```

2. Adding `deepLinks` to the Composable route:
```kotlin
composable<Route.ItemDetail>(
    deepLinks = listOf(
        navDeepLink<Route.ItemDetail>(basePath = "https://example.com/item")
    )
) { backStackEntry ->
    val args = backStackEntry.toRoute<Route.ItemDetail>()
    ItemDetailScreen(itemId = args.itemId)
}
```

⚠️ Common Pitfalls:
- Pitfall: Deep links crashing because the app isn't authenticated yet.
- Solution: Check authentication state at the Root level. If unauthenticated, redirect to the Login route, passing the deep link as an argument to process after login.
```

---

✅ **Verification Checklist:**
- [ ] Screens transition without crashing.
- [ ] Bottom Navigation highlights the correct active tab.
- [ ] Pressing "Back" from a bottom tab returns to the Start Destination (e.g., Home) before exiting the app.
- [ ] Type-safe arguments (`toRoute<Route.X>()`) compile correctly.
- [ ] Testing a deep link via adb opens the correct screen: `adb shell am start -W -a android.intent.action.VIEW -d "https://example.com/item/123" com.example.app`

---

📎 **Related Phases:**
- Prerequisites: [Phase M1: Project Structure](./MOBILE_PHASE_1_PROJECT_STRUCTURE_CONFIGURATION_Full_Stack_Mobile.md)
- Proceeds to: [Phase M3: Backend & API Integration](./MOBILE_PHASE_3_BACKEND_API_INTEGRATION_Full_Stack.md)
