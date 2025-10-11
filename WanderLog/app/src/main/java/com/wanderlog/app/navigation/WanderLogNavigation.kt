package com.wanderlog.app.navigation

import androidx.compose.runtime.Composable
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

@Composable
fun WanderLogNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
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