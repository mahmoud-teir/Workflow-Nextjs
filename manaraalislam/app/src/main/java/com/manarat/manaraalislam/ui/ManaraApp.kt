package com.manarat.manaraalislam.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.NavBackStack
import androidx.navigation3.NavDisplay
import com.manarat.manaraalislam.data.local.AppDatabase
import com.manarat.manaraalislam.data.repository.UserRepository
import com.manarat.manaraalislam.ui.navigation.NavRoute
import com.manarat.manaraalislam.ui.screens.InterestSelectionScreen
import com.manarat.manaraalislam.ui.screens.LoginScreen
import com.manarat.manaraalislam.ui.viewmodel.AuthViewModel
import com.manarat.manaraalislam.ui.viewmodel.ViewModelFactory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ManaraApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(userRepository))

    val currentUser by authViewModel.currentUser.collectAsState()
    
    val backstack = remember { 
        mutableStateListOf<NavRoute>(NavRoute.Login) 
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null && backstack.last() == NavRoute.Login) {
            if (currentUser!!.interests.isEmpty()) {
                backstack.add(NavRoute.InterestSelection)
            } else {
                backstack.add(NavRoute.Home)
            }
        }
    }

    NavDisplay(
        backstack = backstack,
        onBack = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) }
    ) { route ->
        NavBackStack(route) {
            when (route) {
                is NavRoute.Login -> LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        // Handled by LaunchedEffect
                    }
                )
                is NavRoute.InterestSelection -> InterestSelectionScreen(
                    viewModel = authViewModel,
                    onSelectionComplete = {
                        backstack.add(NavRoute.Home)
                    }
                )
                is NavRoute.Home -> HomeScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        backstack.clear()
                        backstack.add(NavRoute.Login)
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(authViewModel: AuthViewModel, onLogout: () -> Unit) {
    val user by authViewModel.currentUser.collectAsState()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Welcome, ${user?.name ?: "User"}!")
            androidx.compose.material3.Button(onClick = { 
                authViewModel.logout()
                onLogout()
            }) {
                Text("Logout")
            }
        }
    }
}
