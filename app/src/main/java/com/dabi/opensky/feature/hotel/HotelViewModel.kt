package com.dabi.opensky.feature.hotel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.HotelRepository
import com.dabi.opensky.core.model.hotel.Hotel
import com.dabi.opensky.core.model.hotel.HotelSearchRequest
import com.dabi.opensky.core.model.hotel.HotelSearchResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- UI State ---
data class HotelUiState(
    val hotels: List<Hotel> = emptyList(),
    val featuredHotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
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


sealed class HotelAction {
    data object LoadAll : HotelAction()
    data object LoadMore : HotelAction()
    data object Refresh : HotelAction()
    data class QuickSearch(val query: String) : HotelAction()
    data class AdvancedSearch(
        val query: String? = null,
        val province: String? = null,
        val stars: Int? = null,
        val minPrice: Double? = null,
        val maxPrice: Double? = null
    ) : HotelAction()
    data class FilterByProvince(val province: String) : HotelAction()
    data class FilterByProvinceId(val provinceId: Int) : HotelAction()
    data object ClearError : HotelAction()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HotelViewModel @Inject constructor(
    private val repo: HotelRepository
) : ViewModel() {

    private val actions = MutableSharedFlow<HotelAction>(extraBufferCapacity = 64)

    private val _state = MutableStateFlow(HotelUiState())
    val state: StateFlow<HotelUiState> = _state.asStateFlow()

    // Expose featured as a StateFlow from VM (VM is the one that shares)
    val featured: StateFlow<List<Hotel>> = repo.getFeaturedHotels()
        .map { res ->
            when (res) {
                is Resource.Success -> res.data
                is Resource.Error -> emptyList()
                Resource.Loading -> _state.value.featuredHotels // keep last
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Debounced search text → emits actions
    private val searchText = MutableStateFlow("")
    init {
// Single pipeline handling all actions to avoid races
        viewModelScope.launch {
            actions
                .onStart { emit(HotelAction.LoadAll) }
                .flatMapLatest { act ->
                    when (act) {
                        HotelAction.LoadAll -> repo.getAllHotels(page = 1, limit = 20)
                        HotelAction.LoadMore ->
                            flowOf(Resource.Error(IllegalStateException("Use Paging3 for load-more")))
                        is HotelAction.QuickSearch ->
                            repo.searchHotels(
                                HotelSearchRequest(
                                    q = act.query,
                                    page = 1,
                                    limit = 20
                                )
                            )
                        is HotelAction.AdvancedSearch ->
                            repo.searchHotels(
                                HotelSearchRequest(
                                    q = act.query,
                                    province = act.province,
                                    stars = act.stars,
                                    minPrice = act.minPrice,
                                    maxPrice = act.maxPrice,
                                    page = 1,
                                    limit = 20
                                )
                            )
                        is HotelAction.FilterByProvince ->
                            repo.getHotelsByProvince(act.province, page = 1, limit = 20)
                        is HotelAction.FilterByProvinceId ->
                            repo.getHotelsByProvinceId(act.provinceId, page = 1, size = 20)
                        HotelAction.Refresh -> flow {
                            _state.update { it.copy(isRefreshing = true) }
                            repo.clearCache()
                            emitAll(repo.getAllHotels(page = 1, limit = 20))
                        }
                        HotelAction.ClearError -> flowOf(Resource.Success(_state.value.toResponse()))
                    }
                }
                .collect { res ->
                    when (res) {
                        Resource.Loading -> _state.update { it.copy(isLoading = true, errorMessage = null) }
                        is Resource.Success -> {
                            val response = res.data
                            _state.update {
                                it.copy(
                                    hotels = response.hotels,
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = null,
                                    currentPage = response.page,
                                    hasNextPage = response.hasNextPage,
                                    totalCount = response.totalCount
                                )
                            }
                        }
                        is Resource.Error -> _state.update {
                            it.copy(isLoading = false, isRefreshing = false, errorMessage = res.cause.message)
                        }
                    }
                }
        }

// Keep featured in state
        viewModelScope.launch {
            featured.collect { list -> _state.update { it.copy(featuredHotels = list) } }
        }
    }
    // --- Public API for UI ---
    fun onSearchTextChanged(q: String) {
        _state.update { it.copy(searchQuery = q) }
        searchText.value = q
    }

    fun dispatch(action: HotelAction) { actions.tryEmit(action) }

    fun clearError() { actions.tryEmit(HotelAction.ClearError) }

    // Helper to adapt UI state to a fake response when clearing error without fetching
    private fun HotelUiState.toResponse(): HotelSearchResponse =
        HotelSearchResponse(
            hotels = hotels,
            page = currentPage,
            limit = 20,
            totalCount = totalCount,
            totalPages = if (totalCount == 0) 0 else (totalCount + 19) / 20,
            hasNextPage = hasNextPage,
            hasPreviousPage = currentPage > 1
        )
}