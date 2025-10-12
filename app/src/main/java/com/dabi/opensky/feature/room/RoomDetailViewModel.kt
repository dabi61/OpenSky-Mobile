// feature/roomdetail/RoomDetailViewModel.kt
package com.dabi.opensky.feature.room

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.data.repository.room.RoomRepository
import com.dabi.opensky.core.model.availability.AvailabilityRequest
import com.dabi.opensky.core.model.availability.AvailabilityResponse
import com.dabi.opensky.core.model.booking.BookingRequest
import com.dabi.opensky.core.model.booking.BookingResponse
import com.dabi.opensky.core.model.booking.BookingRoomRef
import com.dabi.opensky.core.model.room.RoomDetailResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId
import javax.inject.Inject
// feature/roomdetail/RoomDetailViewModel.kt

data class RoomDetailUiState(
    val detail: Resource<RoomDetailResponse> = Resource.Loading,
    val checkInIso: String? = null,
    val checkOutIso: String? = null,
    val booking: Resource<BookingResponse>? = null,

    // NEW: kết quả check-availability gần nhất
    val availability: Resource<AvailabilityResponse>? = null
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

    fun setCheckIn(dateIso: String?) = _ui.update { it.copy(checkInIso = dateIso, availability = null) }
    fun setCheckOut(dateIso: String?) = _ui.update { it.copy(checkOutIso = dateIso, availability = null) }

    /** Gọi server để kiểm tra tình trạng theo range hiện tại */
    fun checkAvailability(): Boolean {
        val state = _ui.value
        val detail = (state.detail as? Resource.Success)?.data ?: return false
        val ci = state.checkInIso
        val co = state.checkOutIso
        if (ci.isNullOrBlank() || co.isNullOrBlank()) {
            _ui.update { it.copy(availability = Resource.Error(IllegalArgumentException("Vui lòng chọn ngày nhận/trả"))) }
            return false
        }
        viewModelScope.launch {
            _ui.update { it.copy(availability = Resource.Loading) }
            val req = AvailabilityRequest(roomID = detail.roomID, fromDate = ci, toDate = co)
            bookingRepo.checkAvailability(req).collect { res ->
                _ui.update { it.copy(availability = res) }
            }
        }
        return true
    }

    /** Chỉ đặt phòng khi availability == Success && isAvailable == true */
    fun bookIfAvailable() {
        val state = _ui.value
        val detail = (state.detail as? Resource.Success)?.data ?: return
        val ci = state.checkInIso
        val co = state.checkOutIso
        val ok = (state.availability as? Resource.Success)?.data?.isAvailable == true
        if (ci.isNullOrBlank() || co.isNullOrBlank()) {
            _ui.update { it.copy(booking = Resource.Error(IllegalArgumentException("Vui lòng chọn ngày"))) }
            return
        }
        if (!ok) {
            _ui.update { it.copy(booking = Resource.Error(IllegalStateException("Vui lòng kiểm tra tình trạng phòng trước khi đặt"))) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(booking = Resource.Loading) }
            val req = BookingRequest(
                rooms = listOf(BookingRoomRef(roomID = detail.roomID)),
                checkInDate = ci,
                checkOutDate = co
            )
            bookingRepo.createBooking(req).collect { res ->
                _ui.update { it.copy(booking = res) }
            }
        }
    }

    fun clearBookingResult() = _ui.update { it.copy(booking = null) }
}
