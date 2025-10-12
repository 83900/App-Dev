package com.wanderlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanderlog.app.data.model.Trip
import com.wanderlog.app.data.model.TripFilter
import com.wanderlog.app.data.model.TripSortBy
import com.wanderlog.app.data.model.TripStatus
import com.wanderlog.app.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()
    
    private val _currentFilter = MutableStateFlow(TripFilter.ALL)
    private val _currentSort = MutableStateFlow(TripSortBy.START_DATE_DESC)
    private val _searchQuery = MutableStateFlow("")
    
    fun loadTrips(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            combine(
                tripRepository.getFilteredTripsFlow(userId, _currentFilter.value, _currentSort.value),
                _searchQuery
            ) { trips, query ->
                if (query.isBlank()) {
                    trips
                } else {
                    trips.filter { trip ->
                        trip.name.contains(query, ignoreCase = true) ||
                        trip.description.contains(query, ignoreCase = true) ||
                        trip.destination.contains(query, ignoreCase = true) ||
                        trip.tags.any { it.contains(query, ignoreCase = true) }
                    }
                }
            }.collect { filteredTrips ->
                _uiState.value = _uiState.value.copy(
                    trips = filteredTrips,
                    isLoading = false,
                    error = null
                )
            }
        }
    }
    
    fun createTrip(trip: Trip) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            tripRepository.createTrip(trip)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            tripRepository.updateTrip(trip)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            tripRepository.deleteTrip(tripId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    suspend fun getTripById(tripId: String): Trip? {
        return tripRepository.getTripById(tripId)
    }
    
    fun setFilter(filter: TripFilter) {
        _currentFilter.value = filter
    }
    
    fun setSortBy(sortBy: TripSortBy) {
        _currentSort.value = sortBy
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun updateTripStatus(tripId: String, status: TripStatus) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)
            if (trip != null) {
                updateTrip(trip.copy(status = status))
            }
        }
    }
}

data class TripUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)