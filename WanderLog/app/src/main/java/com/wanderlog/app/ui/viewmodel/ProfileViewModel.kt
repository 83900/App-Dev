package com.wanderlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanderlog.app.data.repository.DiaryRepository
import com.wanderlog.app.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileStats(
    val tripCount: Int = 0,
    val diaryCount: Int = 0,
    val visitedCities: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {
    
    private val _profileStats = MutableStateFlow(ProfileStats())
    val profileStats: StateFlow<ProfileStats> = _profileStats.asStateFlow()
    
    fun loadUserStats(userId: String) {
        viewModelScope.launch {
            _profileStats.value = _profileStats.value.copy(isLoading = true)
            
            try {
                // 获取用户旅行数据
                tripRepository.getTripsByUserFlow(userId).collect { trips ->
                    val tripCount = trips.size
                    
                    // 计算访问的城市数量（去重）
                    val visitedCities = trips.map { it.destination }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .size
                    
                    // 获取用户日记统计
                    val diaryStats = diaryRepository.getDiaryStats(userId)
                    
                    _profileStats.value = ProfileStats(
                        tripCount = tripCount,
                        diaryCount = diaryStats.totalDiaries,
                        visitedCities = visitedCities,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _profileStats.value = _profileStats.value.copy(isLoading = false)
            }
        }
    }
    
    fun initializeSampleDataForTestUser(userId: String) {
        if (userId == "test") {
            viewModelScope.launch {
                // 为test用户初始化示例数据
                tripRepository.initializeSampleDataForUser(userId)
                diaryRepository.initializeSampleDataForUser(userId)
                
                // 重新加载统计数据
                loadUserStats(userId)
            }
        }
    }
}