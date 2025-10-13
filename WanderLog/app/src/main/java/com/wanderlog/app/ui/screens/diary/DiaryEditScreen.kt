package com.wanderlog.app.ui.screens.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.data.model.DiaryMood
import com.wanderlog.app.data.model.WeatherType
import com.wanderlog.app.ui.viewmodel.AuthViewModel
import com.wanderlog.app.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditScreen(
    diaryId: String,
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    diaryViewModel: DiaryViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val selectedDiary by diaryViewModel.selectedDiary.collectAsState()
    val isLoading by diaryViewModel.isLoading.collectAsState()
    val error by diaryViewModel.error.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<DiaryMood?>(null) }
    var selectedWeather by remember { mutableStateOf<WeatherType?>(null) }
    var tags by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    
    var showMoodDialog by remember { mutableStateOf(false) }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // 检查用户认证状态
    if (authState !is AuthState.Authenticated) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    
    // 加载日记详情
    LaunchedEffect(diaryId) {
        diaryViewModel.getDiaryById(diaryId)
    }
    
    // 初始化表单数据
    LaunchedEffect(selectedDiary) {
        selectedDiary?.let { diary ->
            if (!isInitialized) {
                title = diary.title
                content = diary.content
                location = diary.location
                tags = diary.tags.joinToString(", ")
                isPublic = diary.isPublic
                
                // 根据emoji查找对应的心情和天气
                selectedMood = DiaryMood.values().find { it.emoji == diary.mood }
                selectedWeather = WeatherType.values().find { it.emoji == diary.weather }
                
                isInitialized = true
            }
        }
    }
    
    // 检查权限：只有日记作者才能编辑
    val currentDiary = selectedDiary
    val currentAuthState = authState
    if (currentDiary != null && currentAuthState is AuthState.Authenticated && 
        currentDiary.userId != currentAuthState.user.id) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部应用栏
        TopAppBar(
            title = {
                Text(
                    text = "编辑日记",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        selectedDiary?.let { diary ->
                            val tagList = tags.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            
                            val updatedDiary = diary.copy(
                                title = title,
                                content = content,
                                location = location,
                                tags = tagList,
                                weather = selectedWeather?.emoji ?: "",
                                mood = selectedMood?.emoji ?: "",
                                isPublic = isPublic,
                                updatedAt = System.currentTimeMillis()
                            )
                            
                            diaryViewModel.updateDiary(
                                diary = updatedDiary,
                                onSuccess = { 
                                    navController.popBackStack()
                                },
                                onError = { error ->
                                    // 可以显示错误提示
                                }
                            )
                        }
                    },
                    enabled = title.isNotBlank() && content.isNotBlank() && !isLoading && isInitialized
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("保存")
                    }
                }
            }
        )
        
        // 内容区域
        when {
            !isInitialized && selectedDiary == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            error != null -> {
                val currentError = error
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
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = currentError ?: "未知错误",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                        Button(
                            onClick = { diaryViewModel.getDiaryById(diaryId) }
                        ) {
                            Text("重试")
                        }
                    }
                }
            }
            
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 标题输入
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题") },
                        placeholder = { Text("给你的旅行起个标题吧...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    
                    // 内容输入
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("内容") },
                        placeholder = { Text("记录下你的精彩旅程...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        minLines = 8,
                        enabled = !isLoading
                    )
                    
                    // 位置输入
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("位置") },
                        placeholder = { Text("你在哪里？") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null
                            )
                        }
                    )
                    
                    // 心情和天气选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 心情选择
                        OutlinedCard(
                            onClick = { showMoodDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mood,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "心情",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = selectedMood?.let { "${it.emoji} ${it.displayName}" } ?: "选择心情",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        
                        // 天气选择
                        OutlinedCard(
                            onClick = { showWeatherDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "天气",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = selectedWeather?.let { "${it.emoji} ${it.displayName}" } ?: "选择天气",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    // 标签输入
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("标签") },
                        placeholder = { Text("用逗号分隔多个标签，如：美食,风景,购物") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null
                            )
                        }
                    )
                    
                    // 公开设置
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isPublic,
                            onCheckedChange = { isPublic = it },
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "公开日记",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "其他用户可以看到这篇日记",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp)) // 为底部导航栏留出空间
                }
            }
        }
    }
    
    // 心情选择对话框
    if (showMoodDialog) {
        AlertDialog(
            onDismissRequest = { showMoodDialog = false },
            title = { Text("选择心情") },
            text = {
                Column {
                    DiaryMood.values().forEach { mood ->
                        TextButton(
                            onClick = {
                                selectedMood = mood
                                showMoodDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mood.emoji,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = mood.displayName,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoodDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 天气选择对话框
    if (showWeatherDialog) {
        AlertDialog(
            onDismissRequest = { showWeatherDialog = false },
            title = { Text("选择天气") },
            text = {
                Column {
                    WeatherType.values().forEach { weather ->
                        TextButton(
                            onClick = {
                                selectedWeather = weather
                                showWeatherDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = weather.emoji,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = weather.displayName,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWeatherDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}