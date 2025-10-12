package com.wanderlog.app.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val description: String = "",
    val destination: String,
    val startDate: Long,
    val endDate: Long,
    val coverPhoto: String = "", // 封面图片路径
    val budget: Double = 0.0,
    val currency: String = "CNY",
    val status: TripStatus = TripStatus.PLANNED,
    val tags: List<String> = emptyList(),
    val participants: List<String> = emptyList(), // 参与者用户ID列表
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false
)

// 旅行状态枚举
enum class TripStatus(val displayName: String) {
    PLANNED("计划中"),
    ONGOING("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消")
}

// 旅行类型枚举
enum class TripType(val displayName: String, val emoji: String) {
    LEISURE("休闲旅行", "🏖️"),
    BUSINESS("商务出行", "💼"),
    ADVENTURE("冒险旅行", "🏔️"),
    CULTURAL("文化之旅", "🏛️"),
    FOOD("美食之旅", "🍜"),
    PHOTOGRAPHY("摄影之旅", "📸"),
    FAMILY("家庭旅行", "👨‍👩‍👧‍👦"),
    SOLO("独自旅行", "🚶‍♂️")
}

// 旅行过滤选项
enum class TripFilter {
    ALL,
    PLANNED,
    ONGOING,
    COMPLETED,
    THIS_YEAR,
    FAVORITES
}

// 旅行排序选项
enum class TripSortBy {
    START_DATE_DESC,
    START_DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    CREATED_DATE_DESC
}