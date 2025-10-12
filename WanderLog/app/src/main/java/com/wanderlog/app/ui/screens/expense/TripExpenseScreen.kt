package com.wanderlog.app.ui.screens.expense

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wanderlog.app.data.model.Expense
import com.wanderlog.app.data.model.ExpenseType
import com.wanderlog.app.navigation.Screen
import com.wanderlog.app.ui.theme.*
import com.wanderlog.app.ui.viewmodel.AuthViewModel
import com.wanderlog.app.ui.viewmodel.ExpenseViewModel
import com.wanderlog.app.ui.viewmodel.TripViewModel
import com.wanderlog.app.data.model.Trip
import com.wanderlog.app.data.model.AuthState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripExpenseScreen(
    tripId: String,
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val expenses by expenseViewModel.expenses.collectAsStateWithLifecycle()
    val uiState by expenseViewModel.uiState.collectAsStateWithLifecycle()
    val tripUiState by tripViewModel.uiState.collectAsStateWithLifecycle()
    
    val currentTrip = tripUiState.trips.find { trip: Trip -> trip.id == tripId }
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    
    LaunchedEffect(tripId, authState) {
        val currentAuthState = authState
        if (currentAuthState is AuthState.Authenticated) {
            tripViewModel.loadTrips(currentAuthState.user.id)
            expenseViewModel.loadExpensesByTrip(tripId)
            expenseViewModel.loadTripStatistics(tripId)
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
                    text = currentTrip?.name ?: "旅行账单",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navController.navigate(Screen.AddExpense.createAddExpenseRoute(tripId))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加账单",
                        tint = Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        // 统计卡片
        uiState.tripStatistics?.let { statistics ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "账单统计",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = numberFormat.format(statistics.expenseTotal),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "预算总额",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = numberFormat.format(statistics.budgetTotal),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column {
                            Text(
                                text = if (statistics.difference >= 0) "超支" else "结余",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = numberFormat.format(kotlin.math.abs(statistics.difference)),
                                color = if (statistics.difference >= 0) Warning else Success,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        // 账单列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "账单明细",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(expenses) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    onClick = { 
                        navController.navigate(Screen.EditExpense.createEditExpenseRoute(expense.id))
                    },
                    onIconClick = { 
                        if (expense.type == ExpenseType.BUDGET && !expense.isCompleted) {
                            navController.navigate(Screen.ConvertBudget.createConvertBudgetRoute(expense.id))
                        }
                    }
                )
            }
            
            if (expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无账单记录\n点击右上角 + 添加账单",
                            color = OnSurface.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // 为底部导航栏留出空间
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItemCard(
    expense: Expense,
    onClick: () -> Unit,
    onIconClick: () -> Unit
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())
    
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
            // 类型图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onIconClick() },
                contentAlignment = Alignment.Center
            ) {
                when (expense.type) {
                    ExpenseType.BILL -> {
                        // 实心圆圈
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Primary, CircleShape)
                        )
                    }
                    ExpenseType.BUDGET -> {
                        if (expense.isCompleted) {
                            // 已完成的预算显示实心圆圈
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Success, CircleShape)
                            )
                        } else {
                            // 虚线圆圈
                            Canvas(modifier = Modifier.size(16.dp)) {
                                drawCircle(
                                    color = Primary,
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 账单信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = expense.type.displayName,
                        fontSize = 12.sp,
                        color = if (expense.type == ExpenseType.BUDGET) Primary else Secondary,
                        modifier = Modifier
                            .background(
                                if (expense.type == ExpenseType.BUDGET) Primary.copy(alpha = 0.1f) else Secondary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = expense.category,
                        fontSize = 12.sp,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateFormat.format(Date(expense.date)),
                        fontSize = 12.sp,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                }
                
                // 显示差价（如果有）
                if (expense.shouldShowDifference()) {
                    val difference = expense.getDifference()!!
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (difference >= 0) "超支 ${numberFormat.format(difference)}" else "节省 ${numberFormat.format(-difference)}",
                        fontSize = 12.sp,
                        color = if (difference >= 0) Warning else Success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 金额
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = numberFormat.format(expense.getDisplayAmount()),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                if (expense.type == ExpenseType.BUDGET && !expense.isCompleted) {
                    Text(
                        text = "预算",
                        fontSize = 10.sp,
                        color = Primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}