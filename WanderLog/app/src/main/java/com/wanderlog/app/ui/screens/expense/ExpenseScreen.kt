package com.wanderlog.app.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.data.model.Trip
import com.wanderlog.app.ui.theme.Primary
import com.wanderlog.app.ui.theme.Secondary
import com.wanderlog.app.ui.theme.Surface
import com.wanderlog.app.ui.theme.OnSurface
import com.wanderlog.app.ui.theme.Success
import com.wanderlog.app.ui.theme.Warning
import com.wanderlog.app.navigation.Screen
import com.wanderlog.app.ui.viewmodel.AuthViewModel
import com.wanderlog.app.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val tripUiState by tripViewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        if (authState is AuthState.Authenticated) {
            tripViewModel.loadTrips((authState as AuthState.Authenticated).user.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = "账单管理",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        // 旅行列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "选择旅行查看账单",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(tripUiState.trips) { trip ->
                TripExpenseCard(
                    trip = trip,
                    onClick = {
                        navController.navigate(Screen.TripExpense.createTripExpenseRoute(trip.id))
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // 为底部导航栏留出空间
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripExpenseCard(
    trip: Trip,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 旅行图标
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 旅行信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trip.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trip.destination,
                    fontSize = 14.sp,
                    color = OnSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${dateFormat.format(Date(trip.startDate))} - ${dateFormat.format(Date(trip.endDate))}",
                        fontSize = 12.sp,
                        color = Secondary
                    )
                }
            }
            
            // 箭头图标
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = "查看账单",
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}