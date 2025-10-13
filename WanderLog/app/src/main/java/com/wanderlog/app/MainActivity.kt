package com.wanderlog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.wanderlog.app.navigation.WanderLogNavigation
import com.wanderlog.app.ui.theme.WanderLogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WanderLogTheme {
                WanderLogApp()
            }
        }
    }
}

@Composable
fun WanderLogApp() {
    val navController = rememberNavController()
    
    WanderLogNavigation(
        navController = navController,
        modifier = Modifier.fillMaxSize()
    )
}