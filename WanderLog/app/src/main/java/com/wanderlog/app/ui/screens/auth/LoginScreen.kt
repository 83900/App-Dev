package com.wanderlog.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    
    // 处理登录成功
    LaunchedEffect(authState) {
        android.util.Log.d("LoginScreen", "=== AUTH STATE CHANGED IN LOGIN SCREEN ===")
        android.util.Log.d("LoginScreen", "New auth state: $authState")
        android.util.Log.d("LoginScreen", "State type: ${authState::class.simpleName}")
        
        val currentState = authState
        when (currentState) {
            is AuthState.Authenticated -> {
                android.util.Log.d("LoginScreen", "=== LOGIN SUCCESS DETECTED ===")
                android.util.Log.d("LoginScreen", "User: ${currentState.user.username}")
                android.util.Log.d("LoginScreen", "Calling onLoginSuccess callback")
                onLoginSuccess()
                android.util.Log.d("LoginScreen", "onLoginSuccess callback completed")
            }
            else -> {
                android.util.Log.d("LoginScreen", "Auth state is not Authenticated: $currentState")
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo 和标题
        Text(
            text = "WanderLog",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "记录你的旅行故事",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 用户名输入
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                viewModel.clearError()
            },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = authState !is AuthState.Loading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 密码输入
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                viewModel.clearError()
            },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                    )
                }
            },
            enabled = authState !is AuthState.Loading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 登录按钮
        Button(
            onClick = {
                android.util.Log.d("LoginScreen", "=== LOGIN BUTTON CLICKED ===")
                android.util.Log.d("LoginScreen", "Username: '$username', Password: '$password'")
                viewModel.login(username, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("登录")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 注册链接
        TextButton(
            onClick = onNavigateToRegister,
            enabled = authState !is AuthState.Loading
        ) {
            Text("还没有账户？点击注册")
        }
        
        // 错误信息显示
        val currentAuthState = authState
        if (currentAuthState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = currentAuthState.message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}