// feature/rooms/HotelRoomsViewModel.kt
package com.dabi.opensky.feature.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.room.RoomRepository
import com.dabi.opensky.core.model.room.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomsUiState(
    val hotelId: String = "",
    val items: List<Room> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val pageSize: Int = 10,
    val endReached: Boolean = false
) {
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()
}

@HiltViewModel
class HotelRoomsViewModel @Inject constructor(
    private val repo: RoomRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(RoomsUiState())
    val ui: StateFlow<RoomsUiState> = _ui

    fun start(hotelId: String, pageSize: Int = 10) {
        val cur = _ui.value
        if (cur.items.isNotEmpty() && cur.hotelId == hotelId) return
        _ui.value = RoomsUiState(hotelId = hotelId, page = 1, pageSize = pageSize)
        loadPage(1)
    }

    fun loadNextPage() {
        val s = _ui.value
        if (s.isLoading || s.endReached) return
        loadPage(s.page + 1)
    }

    private fun loadPage(page: Int) {
        val s = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            repo.getRoomsByHotel(s.hotelId, page, s.pageSize).collect { res ->
                when (res) {
                    is Resource.Loading -> _ui.update { it.copy(isLoading = true) }
                    is Resource.Error -> _ui.update { it.copy(isLoading = false, error = res.cause.message) }
                    is Resource.Success -> {
                        val (rooms, meta) = res.data
                        _ui.update {
                            it.copy(
                                isLoading = false,
                                items = if (page == 1) rooms else it.items + rooms,
                                page = meta.currentPage,
                                endReached = !meta.hasNextPage
                            )
                        }
                    }
                }
            }
        }
    }

    fun retry() = loadPage(_ui.value.page.coerceAtLeast(1))
}
