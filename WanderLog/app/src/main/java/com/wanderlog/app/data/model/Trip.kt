package com.wanderlog.app.data.model

import java.util.UUID

data class Trip(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val destination: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val coverPhoto: String = "", // 封面图片路径
    val budget: Double = 0.0,
    val currency: String = "CNY",
    val status: TripStatus = TripStatus.PLANNED,
    val tags: List<String> = emptyList(),
    val participants: List<String> = emptyList(), // 参与者用户ID列表
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isPublic: Boolean = false
) {
    companion object {
        fun create(
            userId: String,
            name: String,
            description: String = "",
            destination: String,
            startDate: Long,
            endDate: Long,
            coverPhoto: String = "",
            budget: Double = 0.0,
            currency: String = "CNY",
            status: TripStatus = TripStatus.PLANNED,
            tags: List<String> = emptyList(),
            participants: List<String> = emptyList(),
            isPublic: Boolean = false
        ): Trip {
            val currentTime = System.currentTimeMillis()
            return Trip(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                description = description,
                destination = destination,
                startDate = startDate,
                endDate = endDate,
                coverPhoto = coverPhoto,
                budget = budget,
                currency = currency,
                status = status,
                tags = tags,
                participants = participants,
                createdAt = currentTime,
                updatedAt = currentTime,
                isPublic = isPublic
            )
        }
    }
}

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