package com.wanderlog.app.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TravelDiary(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val content: String,
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photos: List<String> = emptyList(), // 存储图片路径
    val tags: List<String> = emptyList(),
    val weather: String = "",
    val mood: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false
)

@Serializable
data class DiaryLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)

@Serializable
data class DiaryPhoto(
    val id: String = UUID.randomUUID().toString(),
    val path: String,
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// 日记状态枚举
enum class DiaryMood(val displayName: String, val emoji: String) {
    HAPPY("开心", "😊"),
    EXCITED("兴奋", "🤩"),
    PEACEFUL("平静", "😌"),
    ADVENTUROUS("冒险", "🤠"),
    NOSTALGIC("怀念", "🥺"),
    TIRED("疲惫", "😴"),
    AMAZED("惊叹", "😍"),
    GRATEFUL("感恩", "🙏")
}

// 天气状态枚举
enum class WeatherType(val displayName: String, val emoji: String) {
    SUNNY("晴天", "☀️"),
    CLOUDY("多云", "☁️"),
    RAINY("雨天", "🌧️"),
    SNOWY("雪天", "❄️"),
    WINDY("大风", "💨"),
    FOGGY("雾天", "🌫️"),
    STORMY("暴风雨", "⛈️"),
    PARTLY_CLOUDY("晴转多云", "⛅")
}

// 日记过滤和排序选项
enum class DiaryFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    FAVORITES,
    PUBLIC
}

enum class DiarySortBy {
    DATE_DESC,
    DATE_ASC,
    TITLE_ASC,
    TITLE_DESC,
    LOCATION
}