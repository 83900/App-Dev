package com.wanderlog.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wanderlog.app.navigation.Screen
import com.wanderlog.app.ui.theme.Primary
import com.wanderlog.app.ui.theme.Surface
import com.wanderlog.app.ui.theme.OnSurface
import com.wanderlog.app.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "WanderLog",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "登录您的账户",
            fontSize = 16.sp,
            color = OnSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.MailOutline, contentDescription = null)
                    },
                    label = { Text("邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    label = { Text("密码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.login(emailState.value, passwordState.value)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(text = "登录")
                }
            }
        }
    }
}

