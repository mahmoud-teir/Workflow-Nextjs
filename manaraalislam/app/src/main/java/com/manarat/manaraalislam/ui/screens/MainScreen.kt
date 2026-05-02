package com.manarat.manaraalislam.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigation.suite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import com.manarat.manaraalislam.ui.navigation.MainTab
import com.manarat.manaraalislam.ui.viewmodel.AuthViewModel
import com.manarat.manaraalislam.ui.viewmodel.LibraryViewModel
import com.manarat.manaraalislam.ui.viewmodel.PrayerViewModel
import com.manarat.manaraalislam.data.models.Surah

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel,
    prayerViewModel: PrayerViewModel,
    onSurahClick: (Surah) -> Unit,
    onAthkarClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.LIBRARY) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = selectedTab == MainTab.SCHOOL,
                onClick = { selectedTab = MainTab.SCHOOL },
                icon = { Icon(Icons.Default.School, contentDescription = "School") },
                label = { Text("School") }
            )
            item(
                selected = selectedTab == MainTab.LIBRARY,
                onClick = { selectedTab = MainTab.LIBRARY },
                icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Library") },
                label = { Text("Library") }
            )
            item(
                selected = selectedTab == MainTab.TOOLS,
                onClick = { selectedTab = MainTab.TOOLS },
                icon = { Icon(Icons.Default.Build, contentDescription = "Tools") },
                label = { Text("Tools") }
            )
            item(
                selected = selectedTab == MainTab.PROFILE,
                onClick = { selectedTab = MainTab.PROFILE },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("Profile") }
            )
        }
    ) {
        when (selectedTab) {
            MainTab.SCHOOL -> SchoolScreenPlaceholder()
            MainTab.LIBRARY -> LibraryScreen(
                viewModel = libraryViewModel,
                onSurahClick = onSurahClick,
                onAthkarClick = onAthkarClick
            )
            MainTab.TOOLS -> PrayerTimesScreen(viewModel = prayerViewModel)
            MainTab.PROFILE -> ProfileScreen(authViewModel = authViewModel, onLogout = onLogout)
        }
    }
}

@Composable
fun SchoolScreenPlaceholder() {
    Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("School - Coming Soon (Task 3)", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun ProfileScreen(authViewModel: AuthViewModel, onLogout: () -> Unit) {
    val user by authViewModel.currentUser.collectAsState()
    Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(text = "Profile", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            Text(text = "Name: ${user?.name ?: "Guest"}")
            if (user?.email != null) {
                Text(text = "Email: ${user?.email}")
            }
            Spacer(modifier = androidx.compose.ui.Modifier.height(32.dp))
            Button(onClick = {
                authViewModel.logout()
                onLogout()
            }) {
                Text("Logout")
            }
        }
    }
}
