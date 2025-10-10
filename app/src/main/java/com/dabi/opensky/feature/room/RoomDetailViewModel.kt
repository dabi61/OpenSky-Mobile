// feature/roomdetail/RoomDetailViewModel.kt
package com.dabi.opensky.feature.roomdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.data.repository.room.RoomRepository
import com.dabi.opensky.core.model.booking.BookingRequest
import com.dabi.opensky.core.model.booking.BookingResponse
import com.dabi.opensky.core.model.booking.BookingRoomRef
import com.dabi.opensky.core.model.room.RoomDetailResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomDetailUiState(
    val detail: Resource<RoomDetailResponse> = Resource.Loading,
    val checkInIso: String? = null,
    val checkOutIso: String? = null,
    val booking: Resource<BookingResponse>? = null
)

@HiltViewModel
class RoomDetailViewModel @Inject constructor(
    private val roomRepo: RoomRepository,
    private val bookingRepo: BookingRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(RoomDetailUiState())
    val ui: StateFlow<RoomDetailUiState> = _ui

    fun load(roomId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(detail = Resource.Loading) }
            roomRepo.getRoomDetail(roomId).collect { res ->
                _ui.update { it.copy(detail = res) }
            }
        }
    }

    fun setCheckIn(dateIso: String?) = _ui.update { it.copy(checkInIso = dateIso) }
    fun setCheckOut(dateIso: String?) = _ui.update { it.copy(checkOutIso = dateIso) }

    fun book() {
        val state = _ui.value
        val detail = (state.detail as? Resource.Success)?.data ?: return
        val ci = state.checkInIso
        val co = state.checkOutIso
        if (ci.isNullOrBlank() || co.isNullOrBlank()) {
            _ui.update { it.copy(booking = Resource.Error(IllegalArgumentException("Vui lòng chọn ngày"))) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(booking = Resource.Loading) }
            val req = BookingRequest(
                rooms = listOf(BookingRoomRef(roomID = detail.roomID)),
                checkInDate = state.checkInIso!!,   // dạng 2025-10-10T00:00:00Z
                checkOutDate = state.checkOutIso!!
            )
            bookingRepo.createBooking(req).collect { res ->
                _ui.update { it.copy(booking = res) }
            }
        }
    }

    fun clearBookingResult() = _ui.update { it.copy(booking = null) }
}
