package com.wanderlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.data.model.User
import com.wanderlog.app.data.repository.UserRepository
import com.wanderlog.app.data.repository.TripRepository
import com.wanderlog.app.data.repository.ExpenseRepository
import com.wanderlog.app.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // 启用自动登录检查，恢复用户登录状态
        android.util.Log.d("AuthViewModel", "AuthViewModel initialized - checking for existing user session")
        checkCurrentUser()
    }
    
    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    android.util.Log.d("AuthViewModel", "Found current user: ${currentUser.username}")
                    _authState.value = AuthState.Authenticated(currentUser)
                } else {
                    android.util.Log.d("AuthViewModel", "No current user found")
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error checking current user", e)
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun login(username: String, password: String) {
        android.util.Log.d("AuthViewModel", "=== LOGIN ATTEMPT START ===")
        android.util.Log.d("AuthViewModel", "Username: '$username', Password: '$password'")
        
        if (username.isBlank() || password.isBlank()) {
            android.util.Log.d("AuthViewModel", "Login failed - empty credentials")
            _authState.value = AuthState.Error("用户名和密码不能为空")
            return
        }
        
        android.util.Log.d("AuthViewModel", "Setting state to Loading")
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Attempting login for user: $username")
                val user = userRepository.validateLogin(username, password)
                android.util.Log.d("AuthViewModel", "Validation result: ${if (user != null) "SUCCESS - User found: ${user.username}" else "FAILED - No user found"}")
                
                if (user != null) {
                    val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis())
                    android.util.Log.d("AuthViewModel", "Updated user with new login time: ${updatedUser.lastLoginAt}")
                    
                    // 保存当前用户
                    val saveSuccess = userRepository.saveCurrentUser(updatedUser)
                    android.util.Log.d("AuthViewModel", "Save current user success: $saveSuccess")
                    
                    // 如果是test用户，初始化示例数据
                    if (user.username == "test") {
                        android.util.Log.d("AuthViewModel", "Initializing sample data for test user")
                        tripRepository.initializeSampleDataForUser(user.id)
                        expenseRepository.initializeSampleDataForUser(user.id)
                        diaryRepository.initializeSampleDataForUser(user.id)
                    }
                    
                    android.util.Log.d("AuthViewModel", "=== SETTING AUTHENTICATED STATE ===")
                    android.util.Log.d("AuthViewModel", "User: ${updatedUser.username}, ID: ${updatedUser.id}")
                    _authState.value = AuthState.Authenticated(updatedUser)
                    android.util.Log.d("AuthViewModel", "Current auth state: ${_authState.value}")
                } else {
                    android.util.Log.d("AuthViewModel", "Login failed - invalid credentials")
                    _authState.value = AuthState.Error("用户名或密码错误")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login error", e)
                _authState.value = AuthState.Error("登录失败：${e.message}")
            }
        }
        android.util.Log.d("AuthViewModel", "=== LOGIN ATTEMPT END ===")
    }
    
    fun register(username: String, displayName: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("用户名和密码不能为空")
            return
        }
        
        if (username.length < 3) {
            _authState.value = AuthState.Error("用户名长度至少3个字符")
            return
        }
        
        if (password.length < 4) {
            _authState.value = AuthState.Error("密码长度至少4个字符")
            return
        }
        
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                // 检查用户名是否已存在
                if (userRepository.isUsernameExists(username)) {
                    _authState.value = AuthState.Error("用户名已存在")
                    return@launch
                }
                
                // 创建新用户
                val newUser = User(
                    id = "user_${System.currentTimeMillis()}",
                    username = username,
                    displayName = displayName,
                    password = password,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                
                val success = userRepository.saveUser(newUser)

                if (success) {
                    // 注册成功后设置为RegistrationSuccess状态
                    android.util.Log.d("AuthViewModel", "Registration successful for user: $username")
                    _authState.value = AuthState.RegistrationSuccess
                } else {
                    _authState.value = AuthState.Error("注册失败")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Registration error", e)
                _authState.value = AuthState.Error("注册失败：${e.message}")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.clearCurrentUser()
                android.util.Log.d("AuthViewModel", "User logged out")
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Logout error", e)
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun getCurrentUserId(): String? {
        val currentState = _authState.value
        return if (currentState is AuthState.Authenticated) {
            currentState.user.id
        } else {
            null
        }
    }
    
    fun clearError() {
        val currentState = _authState.value
        if (currentState is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}