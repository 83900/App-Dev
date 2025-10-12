package com.wanderlog.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wanderlog.app.data.model.Expense
import com.wanderlog.app.data.model.ExpenseType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val expenseFile = File(context.filesDir, "expenses.json")
    
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: Flow<List<Expense>> = _expenses.asStateFlow()

    init {
        loadExpenses()
    }

    /**
     * 加载消费记录
     */
    private fun loadExpenses() {
        try {
            if (expenseFile.exists()) {
                val json = expenseFile.readText()
                val type = object : TypeToken<List<Expense>>() {}.type
                val loadedExpenses = gson.fromJson<List<Expense>>(json, type) ?: emptyList()
                _expenses.value = loadedExpenses
            } else {
                // 如果文件不存在，为 test 用户初始化示例数据
                initializeTestUserData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _expenses.value = emptyList()
        }
    }

    /**
     * 为 test 用户初始化示例数据
     */
    private fun initializeTestUserData() {
        val testUserId = "test_user_id"
        val testTripIds = listOf("trip_1", "trip_2", "trip_3")
        
        // 创建示例消费记录
        val exampleExpenses = mutableListOf<Expense>()
        
        // 东京之旅 (trip_1)
        exampleExpenses.addAll(listOf(
            Expense(
                tripId = "trip_1",
                userId = testUserId,
                type = ExpenseType.BUDGET,
                title = "住宿预算",
                description = "酒店住宿费用预算",
                amount = 3000.0,
                actualAmount = 2800.0,
                category = "住宿",
                isCompleted = true,
                date = System.currentTimeMillis() - 86400000 * 5
            ),
            Expense(
                tripId = "trip_1",
                userId = testUserId,
                type = ExpenseType.BILL,
                title = "机票费用",
                description = "往返机票",
                amount = 2500.0,
                category = "交通",
                date = System.currentTimeMillis() - 86400000 * 7
            ),
            Expense(
                tripId = "trip_1",
                userId = testUserId,
                type = ExpenseType.BUDGET,
                title = "餐饮预算",
                description = "每日餐饮费用",
                amount = 1500.0,
                category = "餐饮",
                date = System.currentTimeMillis() - 86400000 * 3
            )
        ))
        
        // 巴黎之旅 (trip_2)
        exampleExpenses.addAll(listOf(
            Expense(
                tripId = "trip_2",
                userId = testUserId,
                type = ExpenseType.BUDGET,
                title = "购物预算",
                description = "纪念品和购物",
                amount = 2000.0,
                actualAmount = 2500.0,
                category = "购物",
                isCompleted = true,
                date = System.currentTimeMillis() - 86400000 * 10
            ),
            Expense(
                tripId = "trip_2",
                userId = testUserId,
                type = ExpenseType.BILL,
                title = "卢浮宫门票",
                description = "博物馆门票",
                amount = 150.0,
                category = "娱乐",
                date = System.currentTimeMillis() - 86400000 * 12
            ),
            Expense(
                tripId = "trip_2",
                userId = testUserId,
                type = ExpenseType.BUDGET,
                title = "交通预算",
                description = "地铁和出租车费用",
                amount = 800.0,
                category = "交通",
                date = System.currentTimeMillis() - 86400000 * 8
            )
        ))
        
        // 泰国之旅 (trip_3)
        exampleExpenses.addAll(listOf(
            Expense(
                tripId = "trip_3",
                userId = testUserId,
                type = ExpenseType.BILL,
                title = "SPA按摩",
                description = "泰式按摩和SPA",
                amount = 300.0,
                category = "娱乐",
                date = System.currentTimeMillis() - 86400000 * 15
            ),
            Expense(
                tripId = "trip_3",
                userId = testUserId,
                type = ExpenseType.BUDGET,
                title = "海鲜大餐预算",
                description = "海边餐厅用餐",
                amount = 500.0,
                actualAmount = 450.0,
                category = "餐饮",
                isCompleted = true,
                date = System.currentTimeMillis() - 86400000 * 18
            ),
            Expense(
                tripId = "trip_3",
                userId = testUserId,
                type = ExpenseType.BUDGET,
                title = "水上活动预算",
                description = "潜水和水上运动",
                amount = 1200.0,
                category = "娱乐",
                date = System.currentTimeMillis() - 86400000 * 20
            )
        ))

        _expenses.value = exampleExpenses
        saveExpenses()
    }

    /**
     * 保存消费记录到文件
     */
    private fun saveExpenses() {
        try {
            val json = gson.toJson(_expenses.value)
            expenseFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取指定旅行的消费记录
     */
    fun getExpensesByTrip(tripId: String): Flow<List<Expense>> {
        return expenses.map { expenseList ->
            expenseList.filter { it.tripId == tripId }
                .sortedByDescending { it.date }
        }
    }

    /**
     * 获取指定用户的消费记录
     */
    fun getExpensesByUser(userId: String): Flow<List<Expense>> {
        return expenses.map { expenseList ->
            expenseList.filter { it.userId == userId }
                .sortedByDescending { it.date }
        }
    }

    /**
     * 根据ID获取消费记录
     */
    fun getExpenseById(expenseId: String): Expense? {
        return _expenses.value.find { it.id == expenseId }
    }

    /**
     * 添加消费记录
     */
    suspend fun addExpense(expense: Expense): Result<Expense> {
        return try {
            val currentExpenses = _expenses.value.toMutableList()
            currentExpenses.add(expense)
            _expenses.value = currentExpenses
            saveExpenses()
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新消费记录
     */
    suspend fun updateExpense(expense: Expense): Result<Expense> {
        return try {
            val currentExpenses = _expenses.value.toMutableList()
            val index = currentExpenses.indexOfFirst { it.id == expense.id }
            if (index != -1) {
                val updatedExpense = expense.copy(updatedAt = System.currentTimeMillis())
                currentExpenses[index] = updatedExpense
                _expenses.value = currentExpenses
                saveExpenses()
                Result.success(updatedExpense)
            } else {
                Result.failure(Exception("消费记录不存在"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除消费记录
     */
    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            val currentExpenses = _expenses.value.toMutableList()
            val removed = currentExpenses.removeIf { it.id == expenseId }
            if (removed) {
                _expenses.value = currentExpenses
                saveExpenses()
                Result.success(Unit)
            } else {
                Result.failure(Exception("消费记录不存在"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 将预算转换为已完成的账单
     */
    suspend fun convertBudgetToBill(expenseId: String, actualAmount: Double): Result<Expense> {
        return try {
            val expense = getExpenseById(expenseId)
            if (expense == null) {
                return Result.failure(Exception("消费记录不存在"))
            }
            
            if (expense.type != ExpenseType.BUDGET) {
                return Result.failure(Exception("只能转换预算类型的记录"))
            }

            val updatedExpense = expense.copy(
                actualAmount = actualAmount,
                isCompleted = true,
                updatedAt = System.currentTimeMillis()
            )

            updateExpense(updatedExpense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取旅行的总预算
     */
    fun getTripBudgetTotal(tripId: String): Flow<Double> {
        return getExpensesByTrip(tripId).map { expenses ->
            expenses.filter { it.type == ExpenseType.BUDGET }
                .sumOf { it.amount }
        }
    }

    /**
     * 获取旅行的总花费
     */
    fun getTripExpenseTotal(tripId: String): Flow<Double> {
        return getExpensesByTrip(tripId).map { expenses ->
            expenses.sumOf { expense ->
                when {
                    expense.type == ExpenseType.BILL -> expense.amount
                    expense.type == ExpenseType.BUDGET && expense.isCompleted && expense.actualAmount != null -> expense.actualAmount
                    else -> 0.0
                }
            }
        }
    }

    /**
     * 获取旅行的预算差额（实际花费 - 预算）
     */
    fun getTripBudgetDifference(tripId: String): Flow<Double> {
        return getExpensesByTrip(tripId).map { expenses ->
            val budgetTotal = expenses.filter { it.type == ExpenseType.BUDGET }.sumOf { it.amount }
            val actualTotal = expenses.sumOf { expense ->
                when {
                    expense.type == ExpenseType.BILL -> expense.amount
                    expense.type == ExpenseType.BUDGET && expense.isCompleted && expense.actualAmount != null -> expense.actualAmount
                    else -> 0.0
                }
            }
            actualTotal - budgetTotal
        }
    }

    /**
     * 初始化示例数据
     */
    suspend fun initializeExampleData(userId: String, tripIds: List<String>) {
        if (_expenses.value.isNotEmpty()) return

        val exampleExpenses = mutableListOf<Expense>()
        
        // 为每个旅行添加示例消费记录
        tripIds.forEachIndexed { index, tripId ->
            when (index) {
                0 -> { // 东京之旅
                    exampleExpenses.addAll(listOf(
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BUDGET,
                            title = "住宿预算",
                            description = "酒店住宿费用预算",
                            amount = 3000.0,
                            actualAmount = 2800.0,
                            category = "住宿",
                            isCompleted = true,
                            date = System.currentTimeMillis() - 86400000 * 5
                        ),
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BILL,
                            title = "机票费用",
                            description = "往返机票",
                            amount = 2500.0,
                            category = "交通",
                            date = System.currentTimeMillis() - 86400000 * 7
                        ),
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BUDGET,
                            title = "餐饮预算",
                            description = "每日餐饮费用",
                            amount = 1500.0,
                            category = "餐饮",
                            date = System.currentTimeMillis() - 86400000 * 3
                        )
                    ))
                }
                1 -> { // 巴黎之旅
                    exampleExpenses.addAll(listOf(
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BUDGET,
                            title = "购物预算",
                            description = "纪念品和购物",
                            amount = 2000.0,
                            actualAmount = 2500.0,
                            category = "购物",
                            isCompleted = true,
                            date = System.currentTimeMillis() - 86400000 * 10
                        ),
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BILL,
                            title = "卢浮宫门票",
                            description = "博物馆门票",
                            amount = 150.0,
                            category = "娱乐",
                            date = System.currentTimeMillis() - 86400000 * 12
                        ),
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BUDGET,
                            title = "交通预算",
                            description = "地铁和出租车费用",
                            amount = 800.0,
                            category = "交通",
                            date = System.currentTimeMillis() - 86400000 * 8
                        )
                    ))
                }
                2 -> { // 泰国之旅
                    exampleExpenses.addAll(listOf(
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BILL,
                            title = "SPA按摩",
                            description = "泰式按摩和SPA",
                            amount = 300.0,
                            category = "娱乐",
                            date = System.currentTimeMillis() - 86400000 * 15
                        ),
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BUDGET,
                            title = "海鲜大餐预算",
                            description = "海边餐厅用餐",
                            amount = 500.0,
                            actualAmount = 450.0,
                            category = "餐饮",
                            isCompleted = true,
                            date = System.currentTimeMillis() - 86400000 * 18
                        ),
                        Expense(
                            tripId = tripId,
                            userId = userId,
                            type = ExpenseType.BUDGET,
                            title = "水上活动预算",
                            description = "潜水和水上运动",
                            amount = 1200.0,
                            category = "娱乐",
                            date = System.currentTimeMillis() - 86400000 * 20
                        )
                    ))
                }
            }
        }

        _expenses.value = exampleExpenses
        saveExpenses()
    }
}