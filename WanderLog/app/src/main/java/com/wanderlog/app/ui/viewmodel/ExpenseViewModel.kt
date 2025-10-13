package com.wanderlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanderlog.app.data.model.Expense
import com.wanderlog.app.data.model.ExpenseType
import com.wanderlog.app.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    /**
     * 加载指定旅行的消费记录
     */
    fun loadExpensesByTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                expenseRepository.getExpensesByTrip(tripId).collect { expenseList ->
                    _expenses.value = expenseList
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentTripId = tripId
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 添加消费记录
     */
    fun addExpense(
        tripId: String,
        userId: String,
        type: ExpenseType,
        title: String,
        description: String,
        amount: Double,
        category: String,
        date: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val expense = Expense(
                tripId = tripId,
                userId = userId,
                type = type,
                title = title,
                description = description,
                amount = amount,
                category = category,
                date = date
            )

            val result = expenseRepository.addExpense(expense)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "添加成功"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * 更新消费记录
     */
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = expenseRepository.updateExpense(expense)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "更新成功"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * 删除消费记录
     */
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = expenseRepository.deleteExpense(expenseId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "删除成功"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * 将预算转换为账单
     */
    fun convertBudgetToBill(expenseId: String, actualAmount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = expenseRepository.convertBudgetToBill(expenseId, actualAmount)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "转换成功"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * 获取旅行统计信息
     */
    fun loadTripStatistics(tripId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.getTripBudgetTotal(tripId).collect { budgetTotal ->
                    expenseRepository.getTripExpenseTotal(tripId).collect { expenseTotal ->
                        expenseRepository.getTripBudgetDifference(tripId).collect { difference ->
                            _uiState.value = _uiState.value.copy(
                                tripStatistics = TripStatistics(
                                    budgetTotal = budgetTotal,
                                    expenseTotal = expenseTotal,
                                    difference = difference
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * 设置选中的消费记录
     */
    fun setSelectedExpense(expense: Expense?) {
        _uiState.value = _uiState.value.copy(selectedExpense = expense)
    }

    /**
     * 设置显示模式
     */
    fun setShowMode(mode: ShowMode) {
        _uiState.value = _uiState.value.copy(showMode = mode)
    }

    /**
     * 根据ID加载特定的消费记录
     */
    fun loadExpenseById(expenseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val expense = expenseRepository.getExpenseById(expenseId)
                _uiState.value = _uiState.value.copy(
                    selectedExpense = expense,
                    isLoading = false,
                    errorMessage = if (expense == null) "账单不存在" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}

/**
 * UI状态数据类
 */
data class ExpenseUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentTripId: String? = null,
    val selectedExpense: Expense? = null,
    val showMode: ShowMode = ShowMode.LIST,
    val tripStatistics: TripStatistics? = null
)

/**
 * 旅行统计信息
 */
data class TripStatistics(
    val budgetTotal: Double = 0.0,
    val expenseTotal: Double = 0.0,
    val difference: Double = 0.0
)

/**
 * 显示模式枚举
 */
enum class ShowMode {
    LIST,           // 列表模式
    ADD,            // 添加模式
    EDIT,           // 编辑模式
    CONVERT         // 转换模式
}