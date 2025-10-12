package com.wanderlog.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.ui.viewmodel.AuthViewModel
import com.wanderlog.app.ui.theme.Primary
import com.wanderlog.app.ui.theme.Secondary
import com.wanderlog.app.ui.theme.Surface
import com.wanderlog.app.ui.theme.OnSurface
import com.wanderlog.app.ui.theme.Error

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 用户信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 头像
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val currentAuthState = authState
                    val currentUser = when (currentAuthState) {
                        is AuthState.Authenticated -> currentAuthState.user
                        else -> null
                    }
                    
                    Text(
                        text = currentUser?.displayName ?: "旅行者",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "@${currentUser?.username ?: "wanderer"}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 统计信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            title = "旅行次数",
                            value = "12",
                            textColor = Color.White
                        )
                        StatItem(
                            title = "访问城市",
                            value = "28",
                            textColor = Color.White
                        )
                        StatItem(
                            title = "日记数量",
                            value = "156",
                            textColor = Color.White
                        )
                    }
                }
            }
        }
        
        item {
            // 设置选项
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingItem(
                        icon = Icons.Default.Settings,
                        title = "设置",
                        subtitle = "应用设置和偏好",
                        onClick = { 
                            Toast.makeText(context, "设置功能即将推出", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    SettingItem(
                        icon = Icons.Default.Notifications,
                        title = "通知",
                        subtitle = "推送通知设置",
                        onClick = { 
                            Toast.makeText(context, "通知设置功能即将推出", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    SettingItem(
                        icon = Icons.Default.Security,
                        title = "隐私与安全",
                        subtitle = "账户安全设置",
                        onClick = { 
                            Toast.makeText(context, "隐私与安全功能即将推出", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    SettingItem(
                        icon = Icons.Default.Favorite,
                        title = "我的收藏",
                        subtitle = "收藏的地点和日记",
                        onClick = { 
                            Toast.makeText(context, "收藏功能即将推出", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
        
        item {
            // 帮助和支持
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingItem(
                        icon = Icons.Default.Help,
                        title = "帮助与支持",
                        subtitle = "常见问题和客服",
                        onClick = { 
                            Toast.makeText(context, "帮助与支持功能即将推出", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    SettingItem(
                        icon = Icons.Default.Logout,
                        title = "退出登录",
                        subtitle = "安全退出当前账户",
                        onClick = { 
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        textColor = Error
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // 为底部导航栏留出空间
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = textColor.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = OnSurface
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (textColor == Error) Error else Primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}