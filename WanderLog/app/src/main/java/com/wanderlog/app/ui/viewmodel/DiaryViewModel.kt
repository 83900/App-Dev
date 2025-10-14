package com.wanderlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanderlog.app.data.model.TravelDiary
import com.wanderlog.app.data.model.DiaryFilter
import com.wanderlog.app.data.model.DiarySortBy
import com.wanderlog.app.data.repository.DiaryRepository
import com.wanderlog.app.data.repository.DiaryStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : ViewModel() {
    
    private val _diaries = MutableStateFlow<List<TravelDiary>>(emptyList())
    val diaries: StateFlow<List<TravelDiary>> = _diaries.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _selectedDiary = MutableStateFlow<TravelDiary?>(null)
    val selectedDiary: StateFlow<TravelDiary?> = _selectedDiary.asStateFlow()
    
    private val _diaryStats = MutableStateFlow<DiaryStats?>(null)
    val diaryStats: StateFlow<DiaryStats?> = _diaryStats.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _currentFilter = MutableStateFlow(DiaryFilter.ALL)
    val currentFilter: StateFlow<DiaryFilter> = _currentFilter.asStateFlow()
    
    private val _currentSort = MutableStateFlow(DiarySortBy.DATE_DESC)
    val currentSort: StateFlow<DiarySortBy> = _currentSort.asStateFlow()
    
    // 加载用户的所有日记
    fun loadDiaries(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val diariesList = diaryRepository.searchDiaries(
                    userId = userId,
                    query = _searchQuery.value,
                    filter = _currentFilter.value,
                    sortBy = _currentSort.value
                )
                _diaries.value = diariesList
                
                // 同时加载统计信息
                val stats = diaryRepository.getDiaryStats(userId)
                _diaryStats.value = stats
                
            } catch (e: Exception) {
                _error.value = "加载日记失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 搜索日记
    fun searchDiaries(userId: String, query: String) {
        _searchQuery.value = query
        loadDiaries(userId)
    }
    
    // 应用过滤器
    fun applyFilter(userId: String, filter: DiaryFilter) {
        _currentFilter.value = filter
        loadDiaries(userId)
    }
    
    // 应用排序
    fun applySorting(userId: String, sortBy: DiarySortBy) {
        _currentSort.value = sortBy
        loadDiaries(userId)
    }
    
    // 获取单个日记详情
    fun getDiaryById(diaryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val diary = diaryRepository.getDiaryById(diaryId)
                _selectedDiary.value = diary
            } catch (e: Exception) {
                _error.value = "获取日记详情失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 加载特定旅行的日记
    fun loadDiariesByTrip(userId: String, tripId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val diariesList = diaryRepository.getDiariesByTrip(userId, tripId)
                _diaries.value = diariesList
            } catch (e: Exception) {
                _error.value = "加载旅行日记失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 搜索特定旅行的日记
    fun searchDiariesByTrip(userId: String, tripId: String, query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val allDiaries = diaryRepository.getDiariesByTrip(userId, tripId)
                val filteredDiaries = if (query.isBlank()) {
                    allDiaries
                } else {
                    allDiaries.filter { diary ->
                        diary.title.contains(query, ignoreCase = true) ||
                        diary.content.contains(query, ignoreCase = true) ||
                        diary.location.contains(query, ignoreCase = true) ||
                        diary.tags.any { it.contains(query, ignoreCase = true) }
                    }
                }
                _diaries.value = filteredDiaries
            } catch (e: Exception) {
                _error.value = "搜索旅行日记失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 创建新日记
    fun createDiary(
        userId: String,
        title: String,
        content: String,
        location: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        photos: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        weather: String = "",
        mood: String = "",
        isPublic: Boolean = false,
        tripId: String? = null,
        onSuccess: (TravelDiary) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // 输入验证
        if (userId.isBlank()) {
            android.util.Log.e("DiaryViewModel", "createDiary failed: userId is blank")
            onError("用户ID不能为空")
            return
        }
        
        if (title.isBlank()) {
            android.util.Log.e("DiaryViewModel", "createDiary failed: title is blank")
            onError("标题不能为空")
            return
        }
        
        if (content.isBlank()) {
            android.util.Log.e("DiaryViewModel", "createDiary failed: content is blank")
            onError("内容不能为空")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                android.util.Log.d("DiaryViewModel", "Creating diary for user: $userId, title: $title")
                
                val newDiary = TravelDiary.create(
                    userId = userId,
                    title = title.trim(),
                    content = content.trim(),
                    location = location.trim(),
                    latitude = latitude,
                    longitude = longitude,
                    photos = photos,
                    tags = tags.map { it.trim() }.filter { it.isNotBlank() },
                    weather = weather.trim(),
                    mood = mood.trim(),
                    isPublic = isPublic,
                    tripId = tripId ?: ""
                )
                
                android.util.Log.d("DiaryViewModel", "Created diary object with ID: ${newDiary.id}")
                
                val success = diaryRepository.saveDiary(newDiary)
                if (success) {
                    android.util.Log.d("DiaryViewModel", "Successfully saved diary: ${newDiary.id}")
                    onSuccess(newDiary)
                    loadDiaries(userId) // 重新加载列表
                } else {
                    android.util.Log.e("DiaryViewModel", "Failed to save diary: ${newDiary.id}")
                    onError("创建日记失败，请稍后重试")
                }
            } catch (e: Exception) {
                android.util.Log.e("DiaryViewModel", "createDiary exception", e)
                onError("创建日记失败：${e.message ?: "未知错误"}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 更新日记
    fun updateDiary(
        diary: TravelDiary,
        onSuccess: (TravelDiary) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (diary.title.isBlank()) {
            onError("标题不能为空")
            return
        }
        
        if (diary.content.isBlank()) {
            onError("内容不能为空")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = diaryRepository.saveDiary(diary)
                if (success) {
                    _selectedDiary.value = diary
                    onSuccess(diary)
                    loadDiaries(diary.userId) // 重新加载列表
                } else {
                    onError("更新日记失败")
                }
            } catch (e: Exception) {
                onError("更新日记失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 更新日记（旧版本，保持向后兼容）
    fun updateDiary(
        userId: String,
        diaryId: String,
        title: String,
        content: String,
        location: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        photos: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        weather: String = "",
        mood: String = "",
        isPublic: Boolean = false,
        onSuccess: (TravelDiary) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (title.isBlank()) {
            onError("标题不能为空")
            return
        }
        
        if (content.isBlank()) {
            onError("内容不能为空")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val existingDiary = diaryRepository.getDiaryById(diaryId)
                if (existingDiary == null) {
                    onError("日记不存在")
                    return@launch
                }
                
                val updatedDiary = existingDiary.copy(
                    title = title.trim(),
                    content = content.trim(),
                    location = location.trim(),
                    latitude = latitude,
                    longitude = longitude,
                    photos = photos,
                    tags = tags.map { it.trim() }.filter { it.isNotBlank() },
                    weather = weather.trim(),
                    mood = mood.trim(),
                    isPublic = isPublic,
                    updatedAt = System.currentTimeMillis()
                )
                
                val success = diaryRepository.saveDiary(updatedDiary)
                if (success) {
                    _selectedDiary.value = updatedDiary
                    onSuccess(updatedDiary)
                    loadDiaries(userId) // 重新加载列表
                } else {
                    onError("更新日记失败")
                }
            } catch (e: Exception) {
                onError("更新日记失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 删除日记
    fun deleteDiary(
        diaryId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val diary = diaryRepository.getDiaryById(diaryId)
                if (diary == null) {
                    onError("日记不存在")
                    return@launch
                }
                
                val success = diaryRepository.deleteDiary(diaryId)
                if (success) {
                    onSuccess()
                    loadDiaries(diary.userId) // 重新加载列表
                    
                    // 如果删除的是当前选中的日记，清空选中状态
                    if (_selectedDiary.value?.id == diaryId) {
                        _selectedDiary.value = null
                    }
                } else {
                    onError("删除日记失败")
                }
            } catch (e: Exception) {
                onError("删除日记失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 删除日记（旧版本，保持向后兼容）
    fun deleteDiary(
        userId: String,
        diaryId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = diaryRepository.deleteDiary(diaryId)
                if (success) {
                    onSuccess()
                    loadDiaries(userId) // 重新加载列表
                    
                    // 如果删除的是当前选中的日记，清空选中状态
                    if (_selectedDiary.value?.id == diaryId) {
                        _selectedDiary.value = null
                    }
                } else {
                    onError("删除日记失败")
                }
            } catch (e: Exception) {
                onError("删除日记失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 批量删除日记
    fun deleteDiaries(
        userId: String,
        diaryIds: List<String>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (diaryIds.isEmpty()) {
            onError("请选择要删除的日记")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = diaryRepository.deleteDiaries(diaryIds)
                if (success) {
                    onSuccess()
                    loadDiaries(userId) // 重新加载列表
                    
                    // 如果删除的包含当前选中的日记，清空选中状态
                    if (_selectedDiary.value?.id in diaryIds) {
                        _selectedDiary.value = null
                    }
                } else {
                    onError("批量删除失败")
                }
            } catch (e: Exception) {
                onError("批量删除失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 导出用户数据
    fun exportDiaries(
        userId: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val jsonData = diaryRepository.exportUserDiaries(userId)
                onSuccess(jsonData)
            } catch (e: Exception) {
                onError("导出数据失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 导入用户数据
    fun importDiaries(
        userId: String,
        jsonData: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = diaryRepository.importUserDiaries(userId, jsonData)
                if (success) {
                    onSuccess()
                    loadDiaries(userId) // 重新加载列表
                } else {
                    onError("导入数据失败")
                }
            } catch (e: Exception) {
                onError("导入数据失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 清空错误状态
    fun clearError() {
        _error.value = null
    }
    
    // 清空选中的日记
    fun clearSelectedDiary() {
        _selectedDiary.value = null
    }
    
    // 重置搜索和过滤条件
    fun resetFilters(userId: String) {
        _searchQuery.value = ""
        _currentFilter.value = DiaryFilter.ALL
        _currentSort.value = DiarySortBy.DATE_DESC
        loadDiaries(userId)
    }
}