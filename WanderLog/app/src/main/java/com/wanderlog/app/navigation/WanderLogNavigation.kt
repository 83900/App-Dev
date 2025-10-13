package com.wanderlog.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.ui.screens.auth.LoginScreen
import com.wanderlog.app.ui.screens.auth.RegisterScreen
import com.wanderlog.app.ui.viewmodel.AuthViewModel

@Composable
fun WanderLogNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    android.util.Log.d("WanderLogNavigation", "=== NAVIGATION RECOMPOSITION ===")
    android.util.Log.d("WanderLogNavigation", "Current auth state: $authState")
    android.util.Log.d("WanderLogNavigation", "State type: ${authState::class.simpleName}")
    android.util.Log.d("WanderLogNavigation", "State hash: ${authState.hashCode()}")
    android.util.Log.d("WanderLogNavigation", "AuthViewModel instance: ${authViewModel.hashCode()}")
    
    // 添加状态变化监听
    LaunchedEffect(authState) {
        android.util.Log.d("WanderLogNavigation", "=== AUTH STATE CHANGED IN NAVIGATION ===")
        android.util.Log.d("WanderLogNavigation", "New state: $authState")
        android.util.Log.d("WanderLogNavigation", "State class: ${authState::class.java.simpleName}")
        android.util.Log.d("WanderLogNavigation", "Is Authenticated: ${authState is AuthState.Authenticated}")
    }
    
    val currentState = authState
    android.util.Log.d("WanderLogNavigation", "About to check state: $currentState")
    
    when (currentState) {
        is AuthState.Authenticated -> {
            android.util.Log.d("WanderLogNavigation", "=== NAVIGATING TO AUTHENTICATED ===")
            android.util.Log.d("WanderLogNavigation", "User: ${currentState.user.username}")
            android.util.Log.d("WanderLogNavigation", "About to render AuthenticatedNavigation")
            
            AuthenticatedNavigation(
                onLogout = {
                    android.util.Log.d("WanderLogNavigation", "Logout callback triggered")
                    authViewModel.logout()
                },
                modifier = modifier
            )
            android.util.Log.d("WanderLogNavigation", "AuthenticatedNavigation rendered successfully")
        }
        else -> {
            android.util.Log.d("WanderLogNavigation", "=== SHOWING LOGIN/REGISTER ===")
            android.util.Log.d("WanderLogNavigation", "Auth state is: $currentState")
            // 未认证状态，显示登录/注册页面
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = modifier
            ) {
                composable(Screen.Login.route) {
                    android.util.Log.d("WanderLogNavigation", "Rendering LoginScreen")
                    LoginScreen(
                        onLoginSuccess = {
                            android.util.Log.d("WanderLogNavigation", "Login success callback triggered")
                            // 登录成功后，AuthState会自动更新，触发重组
                        },
                        onNavigateToRegister = {
                            android.util.Log.d("WanderLogNavigation", "Navigate to register")
                            navController.navigate(Screen.Register.route)
                        },
                        viewModel = authViewModel // 传递同一个AuthViewModel实例
                    )
                }
                
                composable(Screen.Register.route) {
                    android.util.Log.d("WanderLogNavigation", "Rendering RegisterScreen")
                    RegisterScreen(
                        onRegisterSuccess = {
                            android.util.Log.d("WanderLogNavigation", "Register success callback triggered")
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            android.util.Log.d("WanderLogNavigation", "Navigate to login")
                            navController.popBackStack()
                        },
                        viewModel = authViewModel // 传递同一个AuthViewModel实例
                    )
                }
            }
        }
    }
}