package com.wanderlog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wanderlog.app.navigation.WanderLogNavigation
import com.wanderlog.app.ui.components.BottomNavigationBar
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
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val routes = com.wanderlog.app.navigation.bottomNavItems.map { it.route }
            if (currentRoute in routes) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        WanderLogNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
