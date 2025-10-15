package com.wanderlog.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wanderlog.app.data.model.TravelDiary
import com.wanderlog.app.data.model.DiaryFilter
import com.wanderlog.app.data.model.DiarySortBy
import com.wanderlog.app.data.model.DiaryMood
import com.wanderlog.app.data.model.WeatherType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val diariesFile = File(context.filesDir, "travel_diaries.json")
    
    // 获取所有日记
    suspend fun getAllDiaries(): List<TravelDiary> = withContext(Dispatchers.IO) {
        try {
            if (!diariesFile.exists()) {
                android.util.Log.d("DiaryRepository", "Diaries file does not exist, returning empty list")
                return@withContext emptyList()
            }
            
            val json = diariesFile.readText()
            if (json.isBlank()) {
                android.util.Log.d("DiaryRepository", "Diaries file is empty, returning empty list")
                return@withContext emptyList()
            }
            
            val type = object : TypeToken<List<TravelDiary>>() {}.type
            val diaries = gson.fromJson<List<TravelDiary>>(json, type) ?: emptyList()
            android.util.Log.d("DiaryRepository", "Successfully loaded ${diaries.size} diaries from file")
            diaries
        } catch (e: Exception) {
            android.util.Log.e("DiaryRepository", "getAllDiaries failed: ${e.message}", e)
            emptyList()
        }
    }
    
    // 为特定用户初始化示例数据
    suspend fun initializeSampleDataForUser(userId: String) = withContext(Dispatchers.IO) {
        try {
            val existingDiaries = getAllDiaries().toMutableList()
            
            // 检查是否已经有该用户的示例数据
            if (existingDiaries.any { it.userId == userId }) {
                return@withContext
            }
            
            val sampleDiaries = listOf(
                TravelDiary.create(
                    userId = userId,
                    tripId = "trip_1_$userId",
                    title = "初到东京的第一印象",
                    content = "今天终于到达了东京！从成田机场出来的那一刻，就被这座城市的现代化程度震撼了。地铁系统非常发达，虽然有些复杂，但是非常准时。晚上在新宿逛了逛，霓虹灯闪烁，人来人往，真的是不夜城的感觉。明天计划去浅草寺和东京塔。",
                    location = "新宿, 东京",
                    tags = listOf("初印象", "新宿", "地铁"),
                    weather = WeatherType.CLOUDY.displayName,
                    mood = DiaryMood.EXCITED.displayName,
                    isPublic = true
                ).copy(
                    createdAt = System.currentTimeMillis() - (29L * 24 * 60 * 60 * 1000), // 29天前
                    updatedAt = System.currentTimeMillis() - (29L * 24 * 60 * 60 * 1000)
                ),
                TravelDiary.create(
                    userId = userId,
                    tripId = "trip_1_$userId",
                    title = "浅草寺的宁静时光",
                    content = "今天去了浅草寺，虽然游客很多，但是在寺庙里还是能感受到一种宁静氛围。抽了一个签，是大吉！在仲见世通买了一些纪念品，品尝了传统的人形烧和雷门煎饼。傍晚时分，夕阳西下，整个寺庙都被染成了金黄色，非常美丽。",
                    location = "浅草寺, 东京",
                    tags = listOf("寺庙", "传统", "美食"),
                    weather = WeatherType.SUNNY.displayName,
                    mood = DiaryMood.PEACEFUL.displayName,
                    isPublic = true
                ).copy(
                    createdAt = System.currentTimeMillis() - (28L * 24 * 60 * 60 * 1000), // 28天前
                    updatedAt = System.currentTimeMillis() - (28L * 24 * 60 * 60 * 1000)
                ),
                TravelDiary.create(
                    userId = userId,
                    tripId = "trip_2_$userId",
                    title = "塞纳河畔的浪漫黄昏",
                    content = "今天沿着塞纳河散步，从圣母院一直走到埃菲尔铁塔。河水波光粼粼，两岸的建筑在夕阳下显得格外美丽。在一家小咖啡馆坐下，点了一杯咖啡和一块马卡龙，看着来往的行人，感受着巴黎独有的浪漫氛围。晚上埃菲尔铁塔亮灯的那一刻，真的太震撼了！",
                    location = "塞纳河, 巴黎",
                    tags = listOf("塞纳河", "埃菲尔铁塔", "浪漫"),
                    weather = WeatherType.SUNNY.displayName,
                    mood = DiaryMood.ROMANTIC.displayName,
                    isPublic = true
                ).copy(
                    createdAt = System.currentTimeMillis() - (58L * 24 * 60 * 60 * 1000), // 58天前
                    updatedAt = System.currentTimeMillis() - (58L * 24 * 60 * 60 * 1000)
                ),
                TravelDiary.create(
                    userId = userId,
                    tripId = "trip_2_$userId",
                    title = "卢浮宫的艺术盛宴",
                    content = "今天花了一整天在卢浮宫。蒙娜丽莎的微笑确实很神秘，虽然画作比想象中要小一些。维纳斯雕像的优美线条让人叹为观止。在古埃及文物展区看到了很多珍贵的文物，感受了古代文明的魅力。艺术真的是无国界的语言。",
                    location = "卢浮宫, 巴黎",
                    tags = listOf("艺术", "博物馆", "文化"),
                    weather = WeatherType.RAINY.displayName,
                    mood = DiaryMood.INSPIRED.displayName,
                    isPublic = true
                ).copy(
                    createdAt = System.currentTimeMillis() - (57L * 24 * 60 * 60 * 1000), // 57天前
                    updatedAt = System.currentTimeMillis() - (57L * 24 * 60 * 60 * 1000)
                ),
                TravelDiary.create(
                    userId = userId,
                    tripId = "trip_3_$userId",
                    title = "普吉岛的阳光海滩",
                    content = "终于到了梦寐以求的普吉岛！海水清澈见底，沙滩细腻洁白。今天在巴东海滩晒了一整天的太阳，还尝试了冲浪，虽然摔了好几次，但是很有趣。晚上在海边的餐厅吃了新鲜的海鲜，配上泰式酸辣汤，味道绝了！",
                    location = "巴东海滩, 普吉岛",
                    tags = listOf("海滩", "冲浪", "海鲜"),
                    weather = WeatherType.SUNNY.displayName,
                    mood = DiaryMood.RELAXED.displayName,
                    isPublic = true
                ).copy(
                    createdAt = System.currentTimeMillis() - (88L * 24 * 60 * 60 * 1000), // 88天前
                    updatedAt = System.currentTimeMillis() - (88L * 24 * 60 * 60 * 1000)
                ),
                TravelDiary.create(
                    userId = userId,
                    tripId = "trip_3_$userId",
                    title = "泰式按摩的极致享受",
                    content = "今天体验了正宗的泰式按摩，真的是太舒服了！按摩师的手法非常专业，把这几天旅行的疲劳都按走了。下午去了当地的市场，买了一些泰式香料和手工艺品。晚上参加了海滩派对，和来自世界各地的朋友一起跳舞，感受了泰国人民的热情。",
                    location = "普吉岛市区",
                    tags = listOf("按摩", "市场", "派对"),
                    weather = WeatherType.SUNNY.displayName,
                    mood = DiaryMood.HAPPY.displayName,
                    isPublic = true
                ).copy(
                    createdAt = System.currentTimeMillis() - (85L * 24 * 60 * 60 * 1000), // 85天前
                    updatedAt = System.currentTimeMillis() - (85L * 24 * 60 * 60 * 1000)
                )
            )
            
            existingDiaries.addAll(sampleDiaries)
            val json = gson.toJson(existingDiaries)
            diariesFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 根据用户ID获取日记
    suspend fun getDiariesByUserId(userId: String): List<TravelDiary> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank()) {
                android.util.Log.e("DiaryRepository", "getDiariesByUserId failed: userId is blank")
                return@withContext emptyList()
            }
            
            val allDiaries = getAllDiaries()
            val userDiaries = allDiaries.filter { it.userId == userId }
            android.util.Log.d("DiaryRepository", "Found ${userDiaries.size} diaries for user: $userId")
            userDiaries
        } catch (e: Exception) {
            android.util.Log.e("DiaryRepository", "getDiariesByUserId failed for user: $userId", e)
            emptyList()
        }
    }
    
    // 根据旅行ID获取日记
    suspend fun getDiariesByTrip(userId: String, tripId: String): List<TravelDiary> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank()) {
                android.util.Log.e("DiaryRepository", "getDiariesByTrip failed: userId is blank")
                return@withContext emptyList()
            }
            
            if (tripId.isBlank()) {
                android.util.Log.e("DiaryRepository", "getDiariesByTrip failed: tripId is blank")
                return@withContext emptyList()
            }
            
            val allDiaries = getAllDiaries()
            val tripDiaries = allDiaries.filter { it.userId == userId && it.tripId == tripId }
            android.util.Log.d("DiaryRepository", "Found ${tripDiaries.size} diaries for user: $userId, trip: $tripId")
            tripDiaries
        } catch (e: Exception) {
            android.util.Log.e("DiaryRepository", "getDiariesByTrip failed for user: $userId, trip: $tripId", e)
            emptyList()
        }
    }
    
    // 根据ID获取单个日记
    suspend fun getDiaryById(diaryId: String): TravelDiary? = withContext(Dispatchers.IO) {
        try {
            if (diaryId.isBlank()) {
                android.util.Log.e("DiaryRepository", "getDiaryById failed: diaryId is blank")
                return@withContext null
            }
            
            val diary = getAllDiaries().find { it.id == diaryId }
            if (diary != null) {
                android.util.Log.d("DiaryRepository", "Found diary with ID: $diaryId")
            } else {
                android.util.Log.w("DiaryRepository", "No diary found with ID: $diaryId")
            }
            diary
        } catch (e: Exception) {
            android.util.Log.e("DiaryRepository", "getDiaryById failed for ID: $diaryId", e)
            null
        }
    }
    
    // 保存日记
    suspend fun saveDiary(diary: TravelDiary): Boolean = withContext(Dispatchers.IO) {
        try {
            // 验证日记数据
            if (diary.userId.isBlank()) {
                android.util.Log.e("DiaryRepository", "saveDiary failed: userId is blank")
                return@withContext false
            }
            
            if (diary.title.isBlank()) {
                android.util.Log.e("DiaryRepository", "saveDiary failed: title is blank")
                return@withContext false
            }
            
            if (diary.content.isBlank()) {
                android.util.Log.e("DiaryRepository", "saveDiary failed: content is blank")
                return@withContext false
            }
            
            android.util.Log.d("DiaryRepository", "Attempting to save diary: ${diary.id} for user: ${diary.userId}")
            
            val diaries = getAllDiaries().toMutableList()
            val existingIndex = diaries.indexOfFirst { it.id == diary.id }
            
            if (existingIndex >= 0) {
                // 更新现有日记
                diaries[existingIndex] = diary.copy(updatedAt = System.currentTimeMillis())
                android.util.Log.d("DiaryRepository", "Updated existing diary: ${diary.id}")
            } else {
                // 添加新日记
                diaries.add(diary)
                android.util.Log.d("DiaryRepository", "Added new diary: ${diary.id}")
            }
            
            // 确保父目录存在
            if (!diariesFile.parentFile?.exists()!!) {
                val created = diariesFile.parentFile?.mkdirs()
                android.util.Log.d("DiaryRepository", "Created parent directories: $created")
            }
            
            val json = gson.toJson(diaries)
            diariesFile.writeText(json)
            
            android.util.Log.d("DiaryRepository", "Successfully saved diary to file: ${diariesFile.absolutePath}")
            true
        } catch (e: Exception) {
            android.util.Log.e("DiaryRepository", "saveDiary failed for diary: ${diary.id}", e)
            false
        }
    }
    
    // 删除日记
    suspend fun deleteDiary(diaryId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (diaryId.isBlank()) {
                android.util.Log.e("DiaryRepository", "deleteDiary failed: diaryId is blank")
                return@withContext false
            }
            
            android.util.Log.d("DiaryRepository", "Attempting to delete diary: $diaryId")
            
            val diaries = getAllDiaries().toMutableList()
            val removed = diaries.removeIf { it.id == diaryId }
            
            if (removed) {
                val json = gson.toJson(diaries)
                diariesFile.writeText(json)
                android.util.Log.d("DiaryRepository", "Successfully deleted diary: $diaryId")
            } else {
                android.util.Log.w("DiaryRepository", "No diary found to delete with ID: $diaryId")
            }
            
            removed
        } catch (e: Exception) {
            android.util.Log.e("DiaryRepository", "deleteDiary failed for ID: $diaryId", e)
            false
        }
    }
    
    // 搜索日记
    suspend fun searchDiaries(
        userId: String,
        query: String,
        filter: DiaryFilter = DiaryFilter.ALL,
        sortBy: DiarySortBy = DiarySortBy.DATE_DESC
    ): List<TravelDiary> = withContext(Dispatchers.IO) {
        var diaries = getDiariesByUserId(userId)
        
        // 应用搜索查询
        if (query.isNotBlank()) {
            diaries = diaries.filter { diary ->
                diary.title.contains(query, ignoreCase = true) ||
                diary.content.contains(query, ignoreCase = true) ||
                diary.location.contains(query, ignoreCase = true) ||
                diary.tags.any { it.contains(query, ignoreCase = true) }
            }
        }
        
        // 应用过滤器
        diaries = when (filter) {
            DiaryFilter.ALL -> diaries
            DiaryFilter.TODAY -> {
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)
                val todayStart = today.timeInMillis
                
                today.add(Calendar.DAY_OF_MONTH, 1)
                val todayEnd = today.timeInMillis
                
                diaries.filter { it.createdAt >= todayStart && it.createdAt < todayEnd }
            }
            DiaryFilter.THIS_WEEK -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val weekStart = calendar.timeInMillis
                
                diaries.filter { it.createdAt >= weekStart }
            }
            DiaryFilter.THIS_MONTH -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.timeInMillis
                
                diaries.filter { it.createdAt >= monthStart }
            }
            DiaryFilter.PUBLIC -> diaries.filter { it.isPublic }
            DiaryFilter.FAVORITES -> diaries // 可以后续添加收藏功能
        }
        
        // 应用排序
        when (sortBy) {
            DiarySortBy.DATE_DESC -> diaries.sortedByDescending { it.createdAt }
            DiarySortBy.DATE_ASC -> diaries.sortedBy { it.createdAt }
            DiarySortBy.TITLE_ASC -> diaries.sortedBy { it.title.lowercase() }
            DiarySortBy.TITLE_DESC -> diaries.sortedByDescending { it.title.lowercase() }
            DiarySortBy.LOCATION -> diaries.sortedBy { it.location.lowercase() }
        }
    }
    
    // 获取用户的日记统计信息
    suspend fun getDiaryStats(userId: String): DiaryStats = withContext(Dispatchers.IO) {
        val diaries = getDiariesByUserId(userId)
        val totalDiaries = diaries.size
        val totalPhotos = diaries.sumOf { it.photos.size }
        val uniqueLocations = diaries.map { it.location }.filter { it.isNotBlank() }.distinct().size
        
        val thisMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val thisMonthDiaries = diaries.count { it.createdAt >= thisMonth }
        
        DiaryStats(
            totalDiaries = totalDiaries,
            totalPhotos = totalPhotos,
            uniqueLocations = uniqueLocations,
            thisMonthDiaries = thisMonthDiaries
        )
    }
    
    // 批量操作
    suspend fun deleteDiaries(diaryIds: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            val diaries = getAllDiaries().toMutableList()
            val removed = diaries.removeAll { it.id in diaryIds }
            
            if (removed) {
                val json = gson.toJson(diaries)
                diariesFile.writeText(json)
            }
            
            removed
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // 导出用户数据
    suspend fun exportUserDiaries(userId: String): String = withContext(Dispatchers.IO) {
        val diaries = getDiariesByUserId(userId)
        gson.toJson(diaries)
    }
    
    // 导入用户数据
    suspend fun importUserDiaries(userId: String, jsonData: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val type = object : TypeToken<List<TravelDiary>>() {}.type
            val importedDiaries: List<TravelDiary> = gson.fromJson(jsonData, type)
            
            // 更新用户ID并保存
            importedDiaries.forEach { diary ->
                saveDiary(diary.copy(userId = userId))
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// 日记统计数据类
data class DiaryStats(
    val totalDiaries: Int,
    val totalPhotos: Int,
    val uniqueLocations: Int,
    val thisMonthDiaries: Int
)