package com.wanderlog.app.data.model

import java.util.UUID

data class TravelDiary(
    val id: String = "",
    val userId: String = "",
    val tripId: String = "", // 关联的旅行ID
    val title: String = "",
    val content: String = "",
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photos: List<String> = emptyList(), // 存储图片路径
    val tags: List<String> = emptyList(),
    val weather: String = "",
    val mood: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isPublic: Boolean = false
) {
    companion object {
        fun create(
            userId: String,
            tripId: String = "",
            title: String,
            content: String,
            location: String = "",
            latitude: Double? = null,
            longitude: Double? = null,
            photos: List<String> = emptyList(),
            tags: List<String> = emptyList(),
            weather: String = "",
            mood: String = "",
            isPublic: Boolean = false
        ): TravelDiary {
            val currentTime = System.currentTimeMillis()
            return TravelDiary(
                id = UUID.randomUUID().toString(),
                userId = userId,
                tripId = tripId,
                title = title,
                content = content,
                location = location,
                latitude = latitude,
                longitude = longitude,
                photos = photos,
                tags = tags,
                weather = weather,
                mood = mood,
                createdAt = currentTime,
                updatedAt = currentTime,
                isPublic = isPublic
            )
        }
    }
}

data class DiaryLocation(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)

data class DiaryPhoto(
    val id: String = "",
    val path: String = "",
    val caption: String = "",
    val timestamp: Long = 0L
) {
    companion object {
        fun create(
            path: String,
            caption: String = ""
        ): DiaryPhoto {
            return DiaryPhoto(
                id = UUID.randomUUID().toString(),
                path = path,
                caption = caption,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}

// 日记状态枚举
enum class DiaryMood(val displayName: String, val emoji: String) {
    HAPPY("开心", "😊"),
    EXCITED("兴奋", "🤩"),
    PEACEFUL("平静", "😌"),
    ADVENTUROUS("冒险", "🤠"),
    NOSTALGIC("怀念", "🥺"),
    TIRED("疲惫", "😴"),
    AMAZED("惊叹", "😍"),
    GRATEFUL("感恩", "🙏"),
    ROMANTIC("浪漫", "💕"),
    INSPIRED("受启发", "✨"),
    RELAXED("放松", "😎")
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