// feature/checkin/BookingDetailViewModel.kt
package com.dabi.opensky.feature.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.model.booking.HotelBookingDetail
import com.dabi.opensky.core.model.booking.SimpleMessageResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import javax.inject.Inject

data class BookingDetailUiState(
    val data: HotelBookingDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val checkInAction: Resource<SimpleMessageResponse>? = null,
    val checkOutAction: Resource<SimpleMessageResponse>? = null
) {
    fun canCheckIn(now: ZonedDateTime = ZonedDateTime.now()): Boolean {
        val d = data ?: return false
        val isPaid = d.paymentStatus.equals("Paid", true) || d.billStatus.equals("Paid", true)
        val notCancelled = !d.status.equals("Cancelled", true)
        val from = runCatching { Instant.parse(d.checkInDate).atZone(now.zone).toLocalDate() }.getOrNull() ?: return false
        val today = now.toLocalDate()
        val dateOk = !today.isBefore(from) // hôm nay >= check-in
        return isPaid && notCancelled && dateOk && !d.status.equals("Completed", true)
    }

    fun canCheckOut(now: ZonedDateTime = ZonedDateTime.now()): Boolean {
        val d = data ?: return false
        val notCancelled = !d.status.equals("Cancelled", true)
        val from = runCatching { Instant.parse(d.checkInDate).atZone(now.zone).toLocalDate() }.getOrNull() ?: return false
        val to = runCatching { Instant.parse(d.checkOutDate).atZone(now.zone).toLocalDate() }.getOrNull() ?: return false
        val today = now.toLocalDate()
        val dateOk = !today.isBefore(from) // >= check-in
        return notCancelled && dateOk && !d.status.equals("Completed", true)
    }
}

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val repo: BookingRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(BookingDetailUiState())
    val ui: StateFlow<BookingDetailUiState> = _ui

    fun load(bookingId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            repo.getHotelBookingDetail(bookingId).collect { res ->
                when (res) {
                    is Resource.Loading -> _ui.update { it.copy(isLoading = true) }
                    is Resource.Error -> _ui.update { it.copy(isLoading = false, error = res.cause.message) }
                    is Resource.Success -> _ui.update { it.copy(isLoading = false, data = res.data) }
                }
            }
        }
    }

    fun doCheckIn() {
        val id = _ui.value.data?.bookingID ?: return
        viewModelScope.launch {
            _ui.update { it.copy(checkInAction = Resource.Loading) }
            repo.checkIn(id).collect { res ->
                _ui.update { it.copy(checkInAction = res) }
                if (res is Resource.Success) {
                    // refresh detail để cập nhật status
                    load(id)
                }
            }
        }
    }

    fun doCheckOut() {
        val id = _ui.value.data?.bookingID ?: return
        viewModelScope.launch {
            _ui.update { it.copy(checkOutAction = Resource.Loading) }
            repo.checkOut(id).collect { res ->
                _ui.update { it.copy(checkOutAction = res) }
                if (res is Resource.Success) {
                    load(id)
                }
            }
        }
    }

    fun clearActions() {
        _ui.update { it.copy(checkInAction = null, checkOutAction = null) }
    }
}
