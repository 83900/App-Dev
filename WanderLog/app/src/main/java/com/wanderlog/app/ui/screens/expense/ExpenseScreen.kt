package com.wanderlog.app.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wanderlog.app.ui.theme.Primary
import com.wanderlog.app.ui.theme.Secondary
import com.wanderlog.app.ui.theme.Surface
import com.wanderlog.app.ui.theme.OnSurface
import com.wanderlog.app.ui.theme.Success
import com.wanderlog.app.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = "费用管理",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = { /* TODO: 添加新费用 */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加费用",
                        tint = Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        // 总费用卡片
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
                    text = "总费用",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¥ 15,680",
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
                            text = "预算",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "¥ 20,000",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column {
                        Text(
                            text = "剩余",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "¥ 4,320",
                            color = Success,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // 费用分类统计
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "费用明细",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // 示例费用条目
            items(6) { index ->
                ExpenseItem(
                    title = when(index) {
                        0 -> "机票费用"
                        1 -> "酒店住宿"
                        2 -> "餐饮费用"
                        3 -> "购物消费"
                        4 -> "交通费用"
                        else -> "景点门票"
                    },
                    amount = when(index) {
                        0 -> "¥ 4,500"
                        1 -> "¥ 3,200"
                        2 -> "¥ 2,800"
                        3 -> "¥ 2,100"
                        4 -> "¥ 1,800"
                        else -> "¥ 1,280"
                    },
                    category = when(index) {
                        0 -> "交通"
                        1 -> "住宿"
                        2 -> "餐饮"
                        3 -> "购物"
                        4 -> "交通"
                        else -> "娱乐"
                    },
                    icon = when(index) {
                        0 -> Icons.Default.Flight
                        1 -> Icons.Default.Hotel
                        2 -> Icons.Default.Restaurant
                        3 -> Icons.Default.ShoppingCart
                        4 -> Icons.Default.Flight
                        else -> Icons.Default.AttachMoney
                    },
                    date = "1月${16 + index}日",
                    onClick = { /* TODO: 打开费用详情 */ }
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
fun ExpenseItem(
    title: String,
    amount: String,
    category: String,
    icon: ImageVector,
    date: String,
    onClick: () -> Unit
) {
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
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 费用信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        color = Secondary,
                        modifier = Modifier
                            .background(
                                Secondary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // 金额
            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        }
    }
}