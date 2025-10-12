package com.wanderlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.data.model.User
import com.wanderlog.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkCurrentUser()
    }
    
    private fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                _authState.value = AuthState.Authenticated(currentUser)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("用户名和密码不能为空")
            return
        }
        
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                val user = userRepository.validateLogin(username, password)
                if (user != null) {
                    val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis())
                    userRepository.saveCurrentUser(updatedUser)
                    _authState.value = AuthState.Authenticated(updatedUser)
                } else {
                    _authState.value = AuthState.Error("用户名或密码错误")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("登录失败：${e.message}")
            }
        }
    }
    
    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("用户名和密码不能为空")
            return
        }
        
        if (username.length < 3) {
            _authState.value = AuthState.Error("用户名长度至少3个字符")
            return
        }
        
        if (password.length < 6) {
            _authState.value = AuthState.Error("密码长度至少6个字符")
            return
        }
        
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                if (userRepository.isUsernameExists(username)) {
                    _authState.value = AuthState.Error("用户名已存在")
                    return@launch
                }
                
                val newUser = User(
                    username = username,
                    password = password,
                    createdAt = System.currentTimeMillis()
                )
                
                val success = userRepository.saveUser(newUser)
                if (success) {
                    userRepository.saveCurrentUser(newUser)
                    _authState.value = AuthState.Authenticated(newUser)
                } else {
                    _authState.value = AuthState.Error("注册失败，请重试")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("注册失败：${e.message}")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    fun getCurrentUserId(): String? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user.id
            else -> null
        }
    }
    
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}