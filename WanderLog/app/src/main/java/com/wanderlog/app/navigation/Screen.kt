package com.wanderlog.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Login : Screen(
        route = "login",
        title = "登录",
        icon = Icons.Default.AccountCircle
    )
    object Home : Screen(
        route = "home",
        title = "首页",
        icon = Icons.Default.Home
    )
    
    object Diary : Screen(
        route = "diary",
        title = "日记",
        icon = Icons.Default.Book
    )
    
    object Map : Screen(
        route = "map",
        title = "地图",
        icon = Icons.Default.Map
    )
    
    object Expense : Screen(
        route = "expense",
        title = "费用",
        icon = Icons.Default.AttachMoney
    )
    
    object Profile : Screen(
        route = "profile",
        title = "我的",
        icon = Icons.Default.AccountCircle
    )
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Diary,
    Screen.Map,
    Screen.Expense,
    Screen.Profile
)
