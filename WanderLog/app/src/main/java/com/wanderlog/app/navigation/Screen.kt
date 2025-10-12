package com.wanderlog.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Diary : Screen("diary", "日记", Icons.Default.Book)
    object Map : Screen("map", "地图", Icons.Default.Map)
    object Expense : Screen("expense", "账单", Icons.Default.Receipt)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
    
    // 认证相关路由
    object Login : Screen("login", "登录", Icons.Default.Login)
    object Register : Screen("register", "注册", Icons.Default.PersonAdd)
    
    // 日记相关路由
    object DiaryDetail : Screen("diary_detail/{diaryId}", "日记详情", Icons.Default.Book)
    object DiaryEdit : Screen("diary_edit/{diaryId}", "编辑日记", Icons.Default.Edit)
    object DiaryCreate : Screen("diary_create", "创建日记", Icons.Default.Add)
    
    fun createDiaryDetailRoute(diaryId: String) = "diary_detail/$diaryId"
    fun createDiaryEditRoute(diaryId: String) = "diary_edit/$diaryId"
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Diary,
    Screen.Map,
    Screen.Expense,
    Screen.Profile
)