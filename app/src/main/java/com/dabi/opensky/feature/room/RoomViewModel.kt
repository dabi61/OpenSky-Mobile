package com.dabi.opensky.feature.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.repository.room.RoomRepository
import com.dabi.opensky.core.model.room.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UiResult<out T> {
    object Loading : UiResult<Nothing>
    data class Success<T>(val data: T) : UiResult<T>
    data class Error(val throwable: Throwable) : UiResult<Nothing>
}

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val repo: RoomRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UiResult.Success(RoomsUiState()) as UiResult<RoomsUiState>)
    val state : StateFlow<UiResult<RoomsUiState>> = _state

    data class RoomsUiState(
        val items: List<Room> = emptyList(),
        val page: Int = 1,
        val pageSize: Int = 10,
        val hasNext: Boolean = true,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false
    )

    fun load(hotelId: String, firstPageSize: Int = 10) {
        viewModelScope.launch {
            _state.value = UiResult.Loading
            runCatching {
                repo.getRoomsOfHotel(hotelId, 1, firstPageSize)
            }.onSuccess { page ->
                _state.value = UiResult.Success(
                    RoomsUiState(
                        items = page.rooms,
                        page = page.currentPage,
                        pageSize = page.pageSize,
                        hasNext = page.hasNextPage,
                    )
                )
            }.onFailure { _state.value = UiResult.Error(it) }
        }
    }

    fun loadMore(hotelId: String) {
        val current = (_state.value as? UiResult.Success)?.data ?: return
        if (!current.hasNext || current.isLoadingMore) return
        _state.value = UiResult.Success(current.copy(isLoadingMore = true))
        viewModelScope.launch {
            runCatching {
                repo.getRoomsOfHotel(hotelId, current.page + 1, current.pageSize)
            }.onSuccess { page ->
                _state.value = UiResult.Success(
                    current.copy(
                        items = current.items + page.rooms,
                        page = page.currentPage,
                        hasNext = page.hasNextPage,
                        isLoadingMore = false
                    )
                )
            }.onFailure {
                _state.value = UiResult.Success(current.copy(isLoadingMore = false))
            }
        }
    }

    fun refresh(hotelId: String) {
        val current = (_state.value as? UiResult.Success)?.data
        _state.value = UiResult.Loading
        viewModelScope.launch {
            runCatching { repo.getRoomsOfHotel(hotelId, 1, current?.pageSize ?: 10) }
                .onSuccess { page ->
                    _state.value = UiResult.Success(
                        RoomsUiState(
                            items = page.rooms,
                            page = page.currentPage,
                            pageSize = page.pageSize,
                            hasNext = page.hasNextPage,
                            isRefreshing = false
                        )
                    )
                }
                .onFailure { _state.value = UiResult.Error(it) }
        }
    }
}