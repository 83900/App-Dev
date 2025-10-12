package com.wanderlog.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wanderlog.app.data.model.TravelDiary
import com.wanderlog.app.data.model.DiaryFilter
import com.wanderlog.app.data.model.DiarySortBy
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
                return@withContext emptyList()
            }
            
            val json = diariesFile.readText()
            if (json.isBlank()) {
                return@withContext emptyList()
            }
            
            val type = object : TypeToken<List<TravelDiary>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    // 根据用户ID获取日记
    suspend fun getDiariesByUserId(userId: String): List<TravelDiary> = withContext(Dispatchers.IO) {
        getAllDiaries().filter { it.userId == userId }
    }
    
    // 根据ID获取单个日记
    suspend fun getDiaryById(diaryId: String): TravelDiary? = withContext(Dispatchers.IO) {
        getAllDiaries().find { it.id == diaryId }
    }
    
    // 保存日记
    suspend fun saveDiary(diary: TravelDiary): Boolean = withContext(Dispatchers.IO) {
        try {
            val diaries = getAllDiaries().toMutableList()
            val existingIndex = diaries.indexOfFirst { it.id == diary.id }
            
            if (existingIndex >= 0) {
                // 更新现有日记
                diaries[existingIndex] = diary.copy(updatedAt = System.currentTimeMillis())
            } else {
                // 添加新日记
                diaries.add(diary)
            }
            
            val json = gson.toJson(diaries)
            diariesFile.writeText(json)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // 删除日记
    suspend fun deleteDiary(diaryId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val diaries = getAllDiaries().toMutableList()
            val removed = diaries.removeIf { it.id == diaryId }
            
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