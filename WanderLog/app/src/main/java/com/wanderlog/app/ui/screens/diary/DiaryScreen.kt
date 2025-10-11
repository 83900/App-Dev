package com.wanderlog.app.ui.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wanderlog.app.ui.theme.Primary
import com.wanderlog.app.ui.theme.Secondary
import com.wanderlog.app.ui.theme.Surface
import com.wanderlog.app.ui.theme.OnSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
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
                    text = "旅行日记",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = { /* TODO: 添加新日记 */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加日记",
                        tint = Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        // 日记列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 示例日记条目
            items(5) { index ->
                DiaryCard(
                    title = when(index) {
                        0 -> "东京塔的夜景"
                        1 -> "浅草寺的清晨"
                        2 -> "新宿的繁华街道"
                        3 -> "富士山下的温泉"
                        else -> "银座的购物体验"
                    },
                    content = when(index) {
                        0 -> "今天晚上去了东京塔，夜景真的太美了！整个东京都在脚下闪闪发光..."
                        1 -> "早上6点就起床去浅草寺，人很少，可以静静地感受这座古老寺庙的宁静..."
                        2 -> "新宿的街道永远都是那么热闹，霓虹灯闪烁，人来人往..."
                        3 -> "在富士山脚下泡温泉，看着雪山，感觉整个人都放松了..."
                        else -> "在银座逛了一整天，买了很多东西，钱包空了但心情很好..."
                    },
                    location = when(index) {
                        0 -> "东京塔, 东京"
                        1 -> "浅草寺, 东京"
                        2 -> "新宿, 东京"
                        3 -> "河口湖, 富士山"
                        else -> "银座, 东京"
                    },
                    date = "2024年1月${16 + index}日",
                    onClick = { /* TODO: 打开日记详情 */ }
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
fun DiaryCard(
    title: String,
    content: String,
    location: String,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                fontSize = 14.sp,
                color = OnSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        fontSize = 12.sp,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = OnSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}