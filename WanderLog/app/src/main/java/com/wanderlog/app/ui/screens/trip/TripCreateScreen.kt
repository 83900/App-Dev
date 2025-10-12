package com.wanderlog.app.ui.screens.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wanderlog.app.data.model.AuthState
import com.wanderlog.app.data.model.Trip
import com.wanderlog.app.data.model.TripStatus
import com.wanderlog.app.ui.viewmodel.AuthViewModel
import com.wanderlog.app.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCreateScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val tripUiState by tripViewModel.uiState.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis() + 24 * 60 * 60 * 1000) }
    var budget by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TripStatus.PLANNED) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    LaunchedEffect(tripUiState.isLoading) {
        if (!tripUiState.isLoading && isCreating && tripUiState.error == null) {
            onNavigateBack()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部应用栏
        TopAppBar(
            title = { Text("创建旅行") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        val currentAuthState = authState
                        if (currentAuthState is AuthState.Authenticated) {
                            val trip = Trip(
                                userId = currentAuthState.user.id,
                                name = name.trim(),
                                description = description.trim(),
                                destination = destination.trim(),
                                startDate = startDate,
                                endDate = endDate,
                                budget = budget.toDoubleOrNull() ?: 0.0,
                                status = status,
                                tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            )
                            tripViewModel.createTrip(trip)
                            isCreating = true
                        }
                    },
                    enabled = name.isNotBlank() && destination.isNotBlank() && !tripUiState.isLoading
                ) {
                    if (tripUiState.isLoading) {
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 错误提示
            if (tripUiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "错误: ${tripUiState.error}",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // 旅行名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("旅行名称 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 目的地
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("目的地 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 描述
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // 日期选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dateFormat.format(Date(startDate)),
                    onValueChange = { },
                    label = { Text("开始日期") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择开始日期")
                        }
                    }
                )
                
                OutlinedTextField(
                    value = dateFormat.format(Date(endDate)),
                    onValueChange = { },
                    label = { Text("结束日期") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择结束日期")
                        }
                    }
                )
            }
            
            // 预算
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("预算 (CNY)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            // 状态选择
            Column {
                Text(
                    text = "旅行状态",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TripStatus.values().forEach { tripStatus ->
                        FilterChip(
                            onClick = { status = tripStatus },
                            label = { Text(tripStatus.displayName) },
                            selected = status == tripStatus
                        )
                    }
                }
            }
            
            // 标签
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("标签 (用逗号分隔)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如: 海滩, 美食, 摄影") },
                singleLine = true
            )
        }
    }
    
    // 日期选择器
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                startDate = selectedDate
                if (selectedDate > endDate) {
                    endDate = selectedDate + 24 * 60 * 60 * 1000
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false },
            initialDate = startDate
        )
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                if (selectedDate >= startDate) {
                    endDate = selectedDate
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            initialDate = endDate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    initialDate: Long
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}