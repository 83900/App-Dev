package com.wanderlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanderlog.app.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun login(email: String, password: String) {
        if (email.isNotBlank() && password.length >= 6) {
            viewModelScope.launch {
                authRepository.setLoggedIn(true)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.setLoggedIn(false)
        }
    }
}

