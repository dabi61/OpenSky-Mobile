package com.dabi.opensky.feature.hotel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.repository.hotel.HotelRepository
import com.dabi.opensky.core.model.Hotel
import com.dabi.opensky.core.model.HotelSearchRequest
import com.dabi.opensky.core.model.HotelSearchResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HotelUiState(
    val hotels: List<Hotel> = emptyList(),
    val featuredHotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedProvince: String? = null,
    val selectedStars: Int? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val totalCount: Int = 0
)

@HiltViewModel
class HotelViewModel @Inject constructor(
    private val hotelRepository: HotelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HotelUiState())
    val uiState: StateFlow<HotelUiState> = _uiState.asStateFlow()

    init {
        loadAllHotels()
        // Load featured hotels from regular search (first 5 hotels)
        loadFeaturedHotels()
    }

    /**
     * Load featured hotels cho home screen
     */
    fun loadFeaturedHotels() {
        viewModelScope.launch {
            hotelRepository.getFeaturedHotels(5)
                .onSuccess { hotels ->
                    _uiState.value = _uiState.value.copy(featuredHotels = hotels)
                }
                .onFailure { error ->
                    // Don't show error for featured hotels, just log it
                    println("Failed to load featured hotels: ${error.message}")
                }
        }
    }

    /**
     * Load all hotels
     */
    fun loadAllHotels(page: Int = 1, isLoadMore: Boolean = false) {
        viewModelScope.launch {
            println("HotelViewModel: Loading hotels - page: $page, isLoadMore: $isLoadMore")
            
            if (isLoadMore) {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }

            hotelRepository.getAllHotels(page = page, limit = 10)
                .onSuccess { response ->
                    println("HotelViewModel: Success - got ${response.hotels.size} hotels")
                    val currentHotels = if (isLoadMore) _uiState.value.hotels else emptyList()
                    _uiState.value = _uiState.value.copy(
                        hotels = currentHotels + response.hotels,
                        isLoading = false,
                        isLoadingMore = false,
                        currentPage = response.page,
                        hasNextPage = response.hasNextPage,
                        totalCount = response.totalCount
                    )
                }
                .onFailure { error ->
                    println("HotelViewModel: Error - ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = error.message ?: "Failed to load hotels"
                    )
                }
        }
    }

    /**
     * Search hotels với query đơn giản
     */
    fun searchHotels(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isBlank()) {
            loadAllHotels()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            hotelRepository.quickSearch(query, 20)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        hotels = response.hotels,
                        isLoading = false,
                        currentPage = response.page,
                        hasNextPage = response.hasNextPage,
                        totalCount = response.totalCount
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Search failed"
                    )
                }
        }
    }

    /**
     * Advanced search với filters
     */
    fun advancedSearch(
        query: String? = null,
        province: String? = null,
        stars: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query ?: "",
            selectedProvince = province,
            selectedStars = stars,
            minPrice = minPrice,
            maxPrice = maxPrice
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            hotelRepository.advancedSearch(
                query = query,
                province = province,
                stars = stars,
                minPrice = minPrice,
                maxPrice = maxPrice,
                page = 1,
                limit = 20
            )
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        hotels = response.hotels,
                        isLoading = false,
                        currentPage = response.page,
                        hasNextPage = response.hasNextPage,
                        totalCount = response.totalCount
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Advanced search failed"
                    )
                }
        }
    }

    /**
     * Load more hotels (pagination)
     */
    fun loadMoreHotels() {
        if (!_uiState.value.hasNextPage || _uiState.value.isLoadingMore) return
        
        val nextPage = _uiState.value.currentPage + 1
        loadAllHotels(page = nextPage, isLoadMore = true)
    }

    /**
     * Get hotels by province
     */
    fun getHotelsByProvince(province: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null,
                selectedProvince = province
            )

            hotelRepository.getHotelsByProvince(province)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        hotels = response.hotels,
                        isLoading = false,
                        currentPage = response.page,
                        hasNextPage = response.hasNextPage,
                        totalCount = response.totalCount
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load hotels by province"
                    )
                }
        }
    }

    /**
     * Clear search và reset về all hotels
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedProvince = null,
            selectedStars = null,
            minPrice = null,
            maxPrice = null
        )
        loadAllHotels()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Refresh data
     */
    fun refresh() {
        loadFeaturedHotels()
        if (_uiState.value.searchQuery.isBlank()) {
            loadAllHotels()
        } else {
            searchHotels(_uiState.value.searchQuery)
        }
    }
}
