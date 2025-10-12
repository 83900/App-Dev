package com.wanderlog.app.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
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
import com.wanderlog.app.data.model.ExpenseCategory
import com.wanderlog.app.data.model.ExpenseType
import com.wanderlog.app.ui.theme.*
import com.wanderlog.app.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    expenseId: String,
    navController: NavController,
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by expenseViewModel.uiState.collectAsStateWithLifecycle()
    
    var expense by remember { mutableStateOf<Expense?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    // 加载账单数据
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
                    text = "编辑账单",
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
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color.Red
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
            // 类型显示（不可编辑）
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "类型",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = expense?.type?.displayName ?: "账单",
                        fontSize = 14.sp,
                        color = Primary
                    )
                }
            }
            
            // 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 描述输入
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述（可选）") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            // 金额输入
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { 
                    Text(
                        if (expense?.type == ExpenseType.BUDGET) "预算金额" else "实际金额"
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("¥") }
            )
            
            // 如果是已完成的预算，显示差价信息
            expense?.let { exp ->
                if (exp.type == ExpenseType.BUDGET && exp.isCompleted && exp.shouldShowDifference()) {
                    val difference = exp.getDifference()
                    difference?.let { diff ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (diff > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "预算差价",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (diff > 0) {
                                        "超支 ¥${String.format("%.2f", diff)}"
                                    } else {
                                        "节省 ¥${String.format("%.2f", -diff)}"
                                    },
                                    fontSize = 14.sp,
                                    color = if (diff > 0) Color.Red else Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
            
            // 类别选择
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("类别") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    ExpenseCategory.ALL_CATEGORIES.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }
            
            // 日期选择
            OutlinedTextField(
                value = dateFormat.format(Date(selectedDate)),
                onValueChange = { },
                label = { Text("日期") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "选择日期"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 保存按钮
            Button(
                onClick = {
                    if (title.isNotBlank() && amount.isNotBlank()) {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue != null && amountValue > 0) {
                            expense?.let { currentExpense ->
                                expenseViewModel.updateExpense(
                                    currentExpense.copy(
                                        title = title,
                                        description = description,
                                        amount = amountValue,
                                        category = selectedCategory,
                                        date = selectedDate
                                    )
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() && amount.isNotBlank() && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "保存修改",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除账单") },
            text = { Text("确定要删除这条账单记录吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseViewModel.deleteExpense(expenseId)
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}