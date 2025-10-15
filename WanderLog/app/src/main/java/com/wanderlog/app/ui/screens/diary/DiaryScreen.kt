package com.wanderlog.app.ui.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.data.model.TravelDiary
import com.wanderlog.app.navigation.Screen
import com.wanderlog.app.ui.theme.Primary
import com.wanderlog.app.ui.theme.Secondary
import com.wanderlog.app.ui.theme.Surface
import com.wanderlog.app.ui.theme.OnSurface
import com.wanderlog.app.ui.viewmodel.AuthViewModel
import com.wanderlog.app.ui.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    diaryViewModel: DiaryViewModel = hiltViewModel()
) {
    android.util.Log.d("DiaryScreen", "DiaryScreen composable started")
    
    val authState by authViewModel.authState.collectAsState()
    val diaries by diaryViewModel.diaries.collectAsState()
    val isLoading by diaryViewModel.isLoading.collectAsState()
    val error by diaryViewModel.error.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    
    android.util.Log.d("DiaryScreen", "Current auth state: $authState")
    android.util.Log.d("DiaryScreen", "Diaries count: ${diaries.size}, isLoading: $isLoading, error: $error")
    
    // 只在用户已认证时加载日记数据
    LaunchedEffect(authState) {
        android.util.Log.d("DiaryScreen", "LaunchedEffect triggered with authState: $authState")
        try {
            val currentAuthState = authState
            when (currentAuthState) {
                is AuthState.Authenticated -> {
                    android.util.Log.d("DiaryScreen", "User authenticated: ${currentAuthState.user.id}, loading diaries")
                    try {
                        diaryViewModel.loadDiaries(currentAuthState.user.id)
                    } catch (e: Exception) {
                        android.util.Log.e("DiaryScreen", "Failed to load diaries for user: ${currentAuthState.user.id}", e)
                    }
                }
                else -> {
                    android.util.Log.d("DiaryScreen", "Auth state is not authenticated: $currentAuthState")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DiaryScreen", "Exception in LaunchedEffect", e)
        }
    }
    
    // 处理错误信息
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            android.util.Log.e("DiaryScreen", "Diary error occurred: $errorMessage")
            try {
                // 可以显示 Snackbar 或其他错误提示
                diaryViewModel.clearError()
            } catch (e: Exception) {
                android.util.Log.e("DiaryScreen", "Failed to clear error", e)
            }
        }
    }
    
    // 如果用户未认证，显示需要登录的提示
    val currentAuthState = authState
    when (currentAuthState) {
        is AuthState.Unauthenticated -> {
            android.util.Log.d("DiaryScreen", "User is unauthenticated, showing login required message")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "请先登录",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "登录后即可查看和创建旅行日记",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return
        }
        is AuthState.Loading -> {
            android.util.Log.d("DiaryScreen", "Auth state is loading, showing loading indicator")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }
        is AuthState.Error -> {
            android.util.Log.d("DiaryScreen", "Auth state has error: ${currentAuthState.message}, showing login required message")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "认证出错",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentAuthState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            return
        }
        is AuthState.RegistrationSuccess -> {
            android.util.Log.d("DiaryScreen", "Registration successful, but user not authenticated yet")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "注册成功",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "请登录以查看日记",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return
        }
        is AuthState.Authenticated -> {
            android.util.Log.d("DiaryScreen", "User is authenticated: ${currentAuthState.user.id}, showing diary content")
            // 继续显示日记内容
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
                    text = "旅行日记",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = { 
                        android.util.Log.d("DiaryScreen", "Add diary button clicked, navigating to trip selection")
                        try {
                            navController.navigate(Screen.TripSelection.route)
                        } catch (e: Exception) {
                            android.util.Log.e("DiaryScreen", "Failed to navigate to trip selection", e)
                        }
                    }
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
        
        // 搜索栏
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                val currentAuthState = authState
                if (currentAuthState is AuthState.Authenticated) {
                    android.util.Log.d("DiaryScreen", "Search query changed: $query for user: ${currentAuthState.user.id}")
                    try {
                        diaryViewModel.searchDiaries(currentAuthState.user.id, query)
                    } catch (e: Exception) {
                        android.util.Log.e("DiaryScreen", "Failed to search diaries", e)
                    }
                }
            },
            label = { Text("搜索日记") },
            placeholder = { Text("搜索标题、内容、地点或标签...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            val currentAuthState = authState
                            if (currentAuthState is AuthState.Authenticated) {
                                android.util.Log.d("DiaryScreen", "Clear search clicked, reloading diaries for user: ${currentAuthState.user.id}")
                                try {
                                    diaryViewModel.loadDiaries(currentAuthState.user.id)
                                } catch (e: Exception) {
                                    android.util.Log.e("DiaryScreen", "Failed to reload diaries after clearing search", e)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除搜索"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )
        
        // 加载状态
        if (isLoading && diaries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // 日记列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (diaries.isEmpty()) {
                    item {
                        EmptyDiaryState(
                            onCreateDiary = {
                                android.util.Log.d("DiaryScreen", "Empty state create diary clicked")
                                try {
                                    navController.navigate(Screen.TripSelection.route)
                                } catch (e: Exception) {
                                    android.util.Log.e("DiaryScreen", "Failed to navigate to trip selection from empty state", e)
                                }
                            }
                        )
                    }
                } else {
                    items(diaries) { diary ->
                        DiaryCard(
                            diary = diary,
                            onClick = { 
                                android.util.Log.d("DiaryScreen", "Diary card clicked: ${diary.id}")
                                try {
                                    navController.navigate(Screen.DiaryDetail.createDiaryDetailRoute(diary.id))
                                } catch (e: Exception) {
                                    android.util.Log.e("DiaryScreen", "Failed to navigate to diary detail: ${diary.id}", e)
                                }
                            }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // 为底部导航栏留出空间
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryCard(
    diary: TravelDiary,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(diary.createdAt))
    
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = diary.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.weight(1f)
                )
                
                if (diary.mood.isNotBlank()) {
                    Text(
                        text = diary.mood,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = diary.content,
                fontSize = 14.sp,
                color = OnSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp,
                maxLines = 2
            )
            
            if (diary.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    diary.tags.take(3).forEach { tag ->
                        Surface(
                            color = Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (diary.tags.size > 3) {
                        Text(
                            text = "+${diary.tags.size - 3}",
                            fontSize = 10.sp,
                            color = OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (diary.location.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = diary.location,
                            fontSize = 12.sp,
                            color = OnSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    if (diary.weather.isNotBlank()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = diary.weather,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (diary.photos.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = null,
                            tint = OnSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${diary.photos.size}",
                            fontSize = 12.sp,
                            color = OnSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = OnSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDiaryState(
    onCreateDiary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            tint = OnSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "还没有旅行日记",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "记录你的精彩旅程，留下美好回忆",
            fontSize = 14.sp,
            color = OnSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateDiary,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("创建第一篇日记")
        }
    }
}