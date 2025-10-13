package com.wanderlog.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Trip : Screen("trip", "旅行", Icons.Default.Flight)
    object Diary : Screen("diary", "日记", Icons.Default.Book)
    object Map : Screen("map", "地图", Icons.Default.Map)
    object Expense : Screen("expense", "账单", Icons.Default.Receipt)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
    
    // 认证相关路由
    object Login : Screen("login", "登录", Icons.Default.Login)
    object Register : Screen("register", "注册", Icons.Default.PersonAdd)
    
    // 旅行相关路由
    object TripDetail : Screen("trip_detail/{tripId}", "旅行详情", Icons.Default.Flight)
    object TripEdit : Screen("trip_edit/{tripId}", "编辑旅行", Icons.Default.Edit)
    object TripCreate : Screen("trip_create", "创建旅行", Icons.Default.Add)
    
    // 日记相关路由
    object DiaryDetail : Screen("diary_detail/{diaryId}", "日记详情", Icons.Default.Book)
    object DiaryEdit : Screen("diary_edit/{diaryId}", "编辑日记", Icons.Default.Edit)
    object DiaryCreate : Screen("diary_create", "创建日记", Icons.Default.Add)
    object DiaryCreateWithTrip : Screen("diary_create/{tripId}", "创建日记", Icons.Default.Add)
    object TripSelection : Screen("trip_selection", "选择旅行", Icons.Default.Flight)
    
    // 账单相关路由
    object TripExpense : Screen("trip_expense/{tripId}", "旅行账单", Icons.Default.Receipt)
    object AddExpense : Screen("add_expense/{tripId}", "添加账单", Icons.Default.Add)
    object EditExpense : Screen("edit_expense/{expenseId}", "编辑账单", Icons.Default.Edit)
    object ConvertBudget : Screen("convert_budget/{expenseId}", "预算转账单", Icons.Default.SwapHoriz)
    
    fun createTripDetailRoute(tripId: String) = "trip_detail/$tripId"
    fun createTripEditRoute(tripId: String) = "trip_edit/$tripId"
    fun createDiaryDetailRoute(diaryId: String) = "diary_detail/$diaryId"
    fun createDiaryEditRoute(diaryId: String) = "diary_edit/$diaryId"
    fun createDiaryWithTripRoute(tripId: String) = "diary_create/$tripId"
    fun createTripExpenseRoute(tripId: String) = "trip_expense/$tripId"
    fun createAddExpenseRoute(tripId: String) = "add_expense/$tripId"
    fun createEditExpenseRoute(expenseId: String) = "edit_expense/$expenseId"
    fun createConvertBudgetRoute(expenseId: String) = "convert_budget/$expenseId"
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Trip,
    Screen.Diary,
    Screen.Map,
    Screen.Expense,
    Screen.Profile
)