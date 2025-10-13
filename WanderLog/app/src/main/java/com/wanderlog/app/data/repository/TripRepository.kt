package com.wanderlog.app.data.repository

import android.content.Context
import com.wanderlog.app.data.model.Trip
import com.wanderlog.app.data.model.TripFilter
import com.wanderlog.app.data.model.TripSortBy
import com.wanderlog.app.data.model.TripStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val tripsFile = File(context.filesDir, "trips.json")
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    
    init {
        loadTrips()
    }
    
    fun getTripsFlow(): Flow<List<Trip>> = _trips.asStateFlow()
    
    fun getTripsByUserFlow(userId: String): Flow<List<Trip>> {
        return _trips.map { trips ->
            trips.filter { it.userId == userId }
        }
    }
    
    fun getFilteredTripsFlow(
        userId: String,
        filter: TripFilter = TripFilter.ALL,
        sortBy: TripSortBy = TripSortBy.START_DATE_DESC
    ): Flow<List<Trip>> {
        return getTripsByUserFlow(userId).map { trips ->
            val filtered = when (filter) {
                TripFilter.ALL -> trips
                TripFilter.PLANNED -> trips.filter { it.status == TripStatus.PLANNED }
                TripFilter.ONGOING -> trips.filter { it.status == TripStatus.ONGOING }
                TripFilter.COMPLETED -> trips.filter { it.status == TripStatus.COMPLETED }
                TripFilter.THIS_YEAR -> {
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val yearStart = java.util.Calendar.getInstance().apply {
                        set(currentYear, 0, 1, 0, 0, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val yearEnd = java.util.Calendar.getInstance().apply {
                        set(currentYear, 11, 31, 23, 59, 59)
                        set(java.util.Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    trips.filter { it.startDate >= yearStart && it.startDate <= yearEnd }
                }
                TripFilter.FAVORITES -> trips.filter { it.isPublic } // 暂时用isPublic表示收藏
            }
            
            when (sortBy) {
                TripSortBy.START_DATE_DESC -> filtered.sortedByDescending { it.startDate }
                TripSortBy.START_DATE_ASC -> filtered.sortedBy { it.startDate }
                TripSortBy.NAME_ASC -> filtered.sortedBy { it.name }
                TripSortBy.NAME_DESC -> filtered.sortedByDescending { it.name }
                TripSortBy.CREATED_DATE_DESC -> filtered.sortedByDescending { it.createdAt }
            }
        }
    }
    
    suspend fun getTripById(tripId: String): Trip? {
        return withContext(Dispatchers.IO) {
            _trips.value.find { it.id == tripId }
        }
    }
    
    suspend fun createTrip(trip: Trip): Result<Trip> {
        return withContext(Dispatchers.IO) {
            try {
                val currentTrips = _trips.value.toMutableList()
                currentTrips.add(trip)
                _trips.value = currentTrips
                saveTrips()
                Result.success(trip)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateTrip(trip: Trip): Result<Trip> {
        return withContext(Dispatchers.IO) {
            try {
                val currentTrips = _trips.value.toMutableList()
                val index = currentTrips.indexOfFirst { it.id == trip.id }
                if (index != -1) {
                    currentTrips[index] = trip.copy(updatedAt = System.currentTimeMillis())
                    _trips.value = currentTrips
                    saveTrips()
                    Result.success(currentTrips[index])
                } else {
                    Result.failure(Exception("Trip not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun deleteTrip(tripId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentTrips = _trips.value.toMutableList()
                val removed = currentTrips.removeAll { it.id == tripId }
                if (removed) {
                    _trips.value = currentTrips
                    saveTrips()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Trip not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun searchTrips(userId: String, query: String): List<Trip> {
        return withContext(Dispatchers.IO) {
            if (query.isBlank()) {
                _trips.value.filter { it.userId == userId }
            } else {
                _trips.value.filter { trip ->
                    trip.userId == userId && (
                        trip.name.contains(query, ignoreCase = true) ||
                        trip.description.contains(query, ignoreCase = true) ||
                        trip.destination.contains(query, ignoreCase = true) ||
                        trip.tags.any { it.contains(query, ignoreCase = true) }
                    )
                }
            }
        }
    }
    
    private fun loadTrips() {
        try {
            if (tripsFile.exists()) {
                val jsonString = tripsFile.readText()
                if (jsonString.isNotBlank()) {
                    val trips = json.decodeFromString<List<Trip>>(jsonString)
                    _trips.value = trips
                } else {
                    // 如果文件为空，初始化为空列表
                    _trips.value = emptyList()
                }
            } else {
                // 如果文件不存在，初始化为空列表
                _trips.value = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果加载失败，初始化为空列表
            _trips.value = emptyList()
        }
    }
    
    // 为特定用户初始化示例数据
    suspend fun initializeSampleDataForUser(userId: String) {
        // 检查该用户是否已有旅行数据
        val existingTrips = _trips.value.filter { it.userId == userId }
        if (existingTrips.isNotEmpty()) {
            return // 如果已有数据，不再初始化
        }
        
        val sampleTrips = listOf(
            Trip(
                id = "trip_1_$userId",
                userId = userId,
                name = "日本东京之旅",
                description = "探索东京的现代与传统，品尝地道日料，体验日本文化。",
                destination = "东京, 日本",
                startDate = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000), // 30天前
                endDate = System.currentTimeMillis() - (23L * 24 * 60 * 60 * 1000), // 23天前
                status = TripStatus.COMPLETED,
                tags = listOf("文化", "美食", "城市"),
                budget = 15000.0,
                currency = "CNY",
                isPublic = true
            ),
            Trip(
                id = "trip_2_$userId",
                userId = userId,
                name = "巴黎浪漫之行",
                description = "漫步塞纳河畔，参观卢浮宫，在埃菲尔铁塔下许愿。",
                destination = "巴黎, 法国",
                startDate = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000), // 60天前
                endDate = System.currentTimeMillis() - (53L * 24 * 60 * 60 * 1000), // 53天前
                status = TripStatus.COMPLETED,
                tags = listOf("浪漫", "艺术", "历史"),
                budget = 18000.0,
                currency = "CNY",
                isPublic = true
            ),
            Trip(
                id = "trip_3_$userId",
                userId = userId,
                name = "泰国海岛度假",
                description = "在普吉岛享受阳光沙滩，体验泰式按摩和热带风情。",
                destination = "普吉岛, 泰国",
                startDate = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000), // 90天前
                endDate = System.currentTimeMillis() - (83L * 24 * 60 * 60 * 1000), // 83天前
                status = TripStatus.COMPLETED,
                tags = listOf("海滩", "度假", "热带"),
                budget = 8000.0,
                currency = "CNY",
                isPublic = true
            )
        )
        
        // 将示例数据添加到现有数据中
        val currentTrips = _trips.value.toMutableList()
        currentTrips.addAll(sampleTrips)
        _trips.value = currentTrips
        saveTrips()
    }
    
    private fun initializeSampleData() {
        // 这个方法现在不再使用，保留以防兼容性问题
        _trips.value = emptyList()
    }
    
    private fun saveTrips() {
        try {
            val jsonString = json.encodeToString(_trips.value)
            tripsFile.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}