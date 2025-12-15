package com.wanderlog.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wanderlog.app.ui.screens.home.HomeScreen
import com.wanderlog.app.ui.screens.diary.DiaryScreen
import com.wanderlog.app.ui.screens.map.MapScreen
import com.wanderlog.app.ui.screens.expense.ExpenseScreen
import com.wanderlog.app.ui.screens.profile.ProfileScreen
import com.wanderlog.app.ui.screens.auth.LoginScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.wanderlog.app.ui.viewmodel.AuthViewModel

@Composable
fun WanderLogNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val loggedIn = authViewModel.isLoggedIn.collectAsState().value
    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Diary.route) {
            DiaryScreen(navController = navController)
        }
        
        composable(Screen.Map.route) {
            MapScreen(navController = navController)
        }
        
        composable(Screen.Expense.route) {
            ExpenseScreen(navController = navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}
