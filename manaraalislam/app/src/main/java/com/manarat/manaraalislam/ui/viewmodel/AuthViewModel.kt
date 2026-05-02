package com.manarat.manaraalislam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manarat.manaraalislam.data.local.UserEntity
import com.manarat.manaraalislam.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = userRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loginAsGuest() {
        viewModelScope.launch {
            userRepository.loginAsGuest()
        }
    }

    fun loginWithEmail(email: String, name: String) {
        viewModelScope.launch {
            userRepository.loginWithEmail(email, name)
        }
    }

    fun updateInterests(interests: List<String>) {
        viewModelScope.launch {
            userRepository.updateInterests(interests)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}
