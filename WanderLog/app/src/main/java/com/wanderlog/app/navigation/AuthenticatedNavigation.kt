package com.wanderlog.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wanderlog.app.ui.screens.home.HomeScreen
import com.wanderlog.app.ui.screens.trip.TripScreen
import com.wanderlog.app.ui.screens.trip.TripCreateScreen
import com.wanderlog.app.ui.screens.trip.TripDetailScreen
import com.wanderlog.app.ui.screens.trip.TripEditScreen
import com.wanderlog.app.ui.screens.diary.DiaryScreen
import com.wanderlog.app.ui.screens.diary.DiaryCreateScreen
import com.wanderlog.app.ui.screens.diary.DiaryDetailScreen
import com.wanderlog.app.ui.screens.diary.DiaryEditScreen
import com.wanderlog.app.ui.screens.diary.TripSelectionScreen
import com.wanderlog.app.ui.screens.map.MapScreen
import com.wanderlog.app.ui.screens.expense.ExpenseScreen
import com.wanderlog.app.ui.screens.expense.TripExpenseScreen
import com.wanderlog.app.ui.screens.expense.AddExpenseScreen
import com.wanderlog.app.ui.screens.expense.EditExpenseScreen
import com.wanderlog.app.ui.screens.expense.ConvertBudgetScreen
import com.wanderlog.app.ui.screens.profile.ProfileScreen
import com.wanderlog.app.ui.components.BottomNavigationBar

@Composable
fun AuthenticatedNavigation(
    navController: NavHostController = rememberNavController(),
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    android.util.Log.d("AuthenticatedNavigation", "=== AUTHENTICATED NAVIGATION RENDERED ===")
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                android.util.Log.d("AuthenticatedNavigation", "=== HOME SCREEN COMPOSABLE ===")
                HomeScreen(navController = navController)
            }
            
            composable(Screen.Trip.route) {
                TripScreen(
                    onNavigateToTripDetail = { tripId ->
                        navController.navigate(Screen.TripDetail.createTripDetailRoute(tripId))
                    },
                    onNavigateToCreateTrip = {
                        navController.navigate(Screen.TripCreate.route)
                    }
                )
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
                ProfileScreen(
                    navController = navController,
                    onLogout = onLogout
                )
            }
            
            // 旅行相关路由
            composable(Screen.TripCreate.route) {
                TripCreateScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.TripDetail.route) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                TripDetailScreen(
                    tripId = tripId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEditTrip = { id ->
                        navController.navigate(Screen.TripEdit.createTripEditRoute(id))
                    },
                    onNavigateToDiaryDetail = { diaryId ->
                        navController.navigate(Screen.DiaryDetail.createDiaryDetailRoute(diaryId))
                    },
                    onNavigateToCreateDiary = { tripId ->
                        navController.navigate(Screen.DiaryCreateWithTrip.createDiaryWithTripRoute(tripId))
                    }
                )
            }
            
            composable(Screen.TripEdit.route) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                TripEditScreen(
                    tripId = tripId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 日记相关路由
            composable(Screen.DiaryCreate.route) {
                DiaryCreateScreen(
                    navController = navController
                )
            }
            
            composable(Screen.DiaryCreateWithTrip.route) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId")
                DiaryCreateScreen(
                    navController = navController,
                    tripId = tripId
                )
            }
            
            composable(Screen.TripSelection.route) {
                TripSelectionScreen(
                    navController = navController
                )
            }
            
            composable(Screen.DiaryDetail.route) { backStackEntry ->
                val diaryId = backStackEntry.arguments?.getString("diaryId") ?: ""
                DiaryDetailScreen(
                    diaryId = diaryId,
                    navController = navController
                )
            }
            
            composable(Screen.DiaryEdit.route) { backStackEntry ->
                val diaryId = backStackEntry.arguments?.getString("diaryId") ?: ""
                DiaryEditScreen(
                    diaryId = diaryId,
                    navController = navController
                )
            }
            
            // 账单相关路由
            composable(Screen.TripExpense.route) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                TripExpenseScreen(
                    tripId = tripId,
                    navController = navController
                )
            }
            
            composable(Screen.AddExpense.route) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                AddExpenseScreen(
                    tripId = tripId,
                    navController = navController
                )
            }
            
            composable(Screen.EditExpense.route) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
                EditExpenseScreen(
                    expenseId = expenseId,
                    navController = navController
                )
            }
            
            composable(Screen.ConvertBudget.route) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
                ConvertBudgetScreen(
                    expenseId = expenseId,
                    navController = navController
                )
            }
        }
    }
}