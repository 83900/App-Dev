package com.wanderlog.app.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wanderlog.app.data.model.Expense
import com.wanderlog.app.ui.theme.*
import com.wanderlog.app.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertBudgetScreen(
    expenseId: String,
    navController: NavController,
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by expenseViewModel.uiState.collectAsStateWithLifecycle()
    
    var expense by remember { mutableStateOf<Expense?>(null) }
    var actualAmount by remember { mutableStateOf("") }
    var difference by remember { mutableStateOf(0.0) }
    
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    // 计算差价
    LaunchedEffect(actualAmount, expense) {
        val actual = actualAmount.toDoubleOrNull() ?: 0.0
        val budget = expense?.amount ?: 0.0
        difference = actual - budget
    }
    
    // 加载预算数据
    LaunchedEffect(expenseId) {
        // 这里应该从 ViewModel 加载特定的 expense
        // 暂时使用模拟数据
    }
    
    // 处理成功消息
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            expenseViewModel.clearSuccessMessage()
            navController.navigateUp()
        }
    }
    
    // 处理错误消息
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            expenseViewModel.clearErrorMessage()
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
                    text = "预算转账单",
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 预算信息卡片
            expense?.let { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "预算信息",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "标题:",
                                fontSize = 14.sp,
                                color = OnSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = exp.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "预算金额:",
                                fontSize = 14.sp,
                                color = OnSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "¥${String.format("%.2f", exp.amount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "日期:",
                                fontSize = 14.sp,
                                color = OnSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = dateFormat.format(Date(exp.date)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // 实际金额输入
            OutlinedTextField(
                value = actualAmount,
                onValueChange = { actualAmount = it },
                label = { Text("实际花费金额") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("¥") },
                supportingText = {
                    Text("请输入实际花费的金额")
                }
            )
            
            // 差价显示卡片
            if (actualAmount.isNotBlank() && actualAmount.toDoubleOrNull() != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            difference > 0 -> Color(0xFFFFEBEE) // 超支 - 红色背景
                            difference < 0 -> Color(0xFFE8F5E8) // 节省 - 绿色背景
                            else -> Surface // 刚好 - 默认背景
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "预算差价",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (difference > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (difference > 0) Color.Red else Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        difference > 0 -> "超支 ¥${String.format("%.2f", difference)}"
                                        difference < 0 -> "节省 ¥${String.format("%.2f", -difference)}"
                                        else -> "刚好符合预算"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        difference > 0 -> Color.Red
                                        difference < 0 -> Color(0xFF4CAF50)
                                        else -> OnSurface
                                    }
                                )
                            }
                        }
                        
                        if (difference != 0.0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when {
                                    difference > 0 -> "实际花费超出预算 ${String.format("%.1f", (difference / (expense?.amount ?: 1.0)) * 100)}%"
                                    else -> "实际花费节省了 ${String.format("%.1f", (-difference / (expense?.amount ?: 1.0)) * 100)}%"
                                },
                                fontSize = 12.sp,
                                color = OnSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 转换按钮
            Button(
                onClick = {
                    val actual = actualAmount.toDoubleOrNull()
                    if (actual != null && actual > 0) {
                        expenseViewModel.convertBudgetToBill(
                            expenseId = expenseId,
                            actualAmount = actual
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = actualAmount.isNotBlank() && 
                         actualAmount.toDoubleOrNull() != null && 
                         actualAmount.toDouble() > 0 && 
                         !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "转换为账单",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 说明文字
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "说明",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 转换后，此预算将变为已完成的账单\n• 系统会记录实际花费与预算的差价\n• 超支显示为正数，节省显示为负数\n• 转换后无法撤销，请确认金额无误",
                        fontSize = 12.sp,
                        color = OnSurface.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}