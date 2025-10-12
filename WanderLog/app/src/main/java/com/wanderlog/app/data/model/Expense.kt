package com.wanderlog.app.data.model

import java.util.*

/**
 * 消费记录数据模型
 * 支持预算和账单两种类型
 */
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val userId: String,
    val type: ExpenseType,
    val title: String,
    val description: String = "",
    val amount: Double,
    val actualAmount: Double? = null, // 实际花费金额（仅当预算转为账单时使用）
    val date: Long = System.currentTimeMillis(),
    val category: String = "其他",
    val isCompleted: Boolean = false, // 预算是否已转为完成的账单
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 计算差价（实际花费与预算的差距）
     * 正数表示超支，负数表示低于预期
     */
    fun getDifference(): Double? {
        return if (type == ExpenseType.BUDGET && actualAmount != null) {
            actualAmount - amount
        } else null
    }

    /**
     * 获取显示金额
     */
    fun getDisplayAmount(): Double {
        return when (type) {
            ExpenseType.BUDGET -> if (isCompleted && actualAmount != null) actualAmount else amount
            ExpenseType.BILL -> amount
        }
    }

    /**
     * 是否显示差价
     */
    fun shouldShowDifference(): Boolean {
        return type == ExpenseType.BUDGET && isCompleted && actualAmount != null
    }
}

/**
 * 消费类型枚举
 */
enum class ExpenseType(val displayName: String) {
    BUDGET("预算"),
    BILL("账单")
}

/**
 * 消费类别
 */
object ExpenseCategory {
    const val FOOD = "餐饮"
    const val TRANSPORT = "交通"
    const val ACCOMMODATION = "住宿"
    const val ENTERTAINMENT = "娱乐"
    const val SHOPPING = "购物"
    const val OTHER = "其他"
    
    val ALL_CATEGORIES = listOf(
        FOOD, TRANSPORT, ACCOMMODATION, 
        ENTERTAINMENT, SHOPPING, OTHER
    )
}