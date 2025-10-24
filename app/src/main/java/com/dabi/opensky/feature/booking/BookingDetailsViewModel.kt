// feature/checkin/BookingDetailViewModel.kt
package com.dabi.opensky.feature.booking

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.model.booking.HotelBookingDetail
import com.dabi.opensky.core.model.booking.SimpleMessageResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class BookingDetailUiState(
    val data: HotelBookingDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val checkInAction: Resource<SimpleMessageResponse>? = null,
    val checkOutAction: Resource<SimpleMessageResponse>? = null
) {
    private fun String?.eq(value: String) = this?.equals(value, ignoreCase = true) == true
    fun isCheckedIn(): Boolean = data?.status.eq("CheckedIn") || data?.status.eq("Ongoing")
    fun isCompleted(): Boolean = !data?.status.eq("Completed")
    fun isCancelled(): Boolean = data?.status.eq("Cancelled")

    @RequiresApi(Build.VERSION_CODES.O)
    fun canCheckIn(
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val d = data ?: return false
        val ci = runCatching { Instant.parse(d.checkInDate).atZone(zone).toLocalDate() }.getOrNull() ?: return false
        val co = runCatching { Instant.parse(d.checkOutDate).atZone(zone).toLocalDate() }.getOrNull() ?: return false

        val withinWindow = today.isEqual(ci) || (today.isAfter(ci) && today.isBefore(co))
        return withinWindow && !isCheckedIn() && !isCompleted() && !isCancelled()
    }

    /** Hiển thị & cho phép check-out chỉ khi đã check-in và chưa completed */
    fun showCheckOutButton(): Boolean = isCheckedIn() && !isCompleted()
    fun canCheckOut(): Boolean = showCheckOutButton()
}

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val repo: BookingRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(BookingDetailUiState())
    val ui: StateFlow<BookingDetailUiState> = _ui

    @RequiresApi(Build.VERSION_CODES.O)
    fun load(bookingId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            repo.getHotelBookingDetail(bookingId).collect { res ->
                when (res) {
                    is Resource.Loading -> _ui.update { it.copy(isLoading = true) }
                    is Resource.Error -> _ui.update { it.copy(isLoading = false, error = res.cause.message) }
                    is Resource.Success -> {
                        _ui.update { it.copy(isLoading = false, data = res.data) }
                        debugEligibility()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun doCheckIn() {
        val id = _ui.value.data?.bookingID ?: return
        viewModelScope.launch {
            _ui.update { it.copy(checkInAction = Resource.Loading) }
            repo.checkIn(id).collect { res ->
                _ui.update { it.copy(checkInAction = res) }
                if (res is Resource.Success) {
                    load(id)            // cập nhật status từ server
                    debugEligibility()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun doCheckOut() {
        val id = _ui.value.data?.bookingID ?: return
        viewModelScope.launch {
            _ui.update { it.copy(checkOutAction = Resource.Loading) }
            repo.checkOut(id).collect { res ->
                _ui.update { it.copy(checkOutAction = res) }
                if (res is Resource.Success) {
                    load(id)
                    debugEligibility()
                }
            }
        }
    }

    fun clearActions() {
        _ui.update { it.copy(checkInAction = null, checkOutAction = null) }
    }

    /** Log để kiểm tra điều kiện hiển thị nút */
    @RequiresApi(Build.VERSION_CODES.O)
    fun debugEligibility(tag: String = "BookingDetail") {
        val d = _ui.value.data ?: run {
            Log.d(tag, "debugEligibility: NO DATA")
            return
        }
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val ci = runCatching { Instant.parse(d.checkInDate).atZone(zone).toLocalDate() }.getOrNull()
        val co = runCatching { Instant.parse(d.checkOutDate).atZone(zone).toLocalDate() }.getOrNull()
        val within = if (ci != null && co != null) today.isEqual(ci) || (today.isAfter(ci) && today.isBefore(co)) else null

        Log.d(tag, buildString {
            appendLine("===== DEBUG ELIGIBILITY =====")
            appendLine("status=${d.status} | paid=${d.paymentStatus} / ${d.billStatus}")
            appendLine("today=$today | checkIn=$ci | checkOut=$co")
            appendLine("isCheckedIn=${_ui.value.isCheckedIn()} | isCompleted=${_ui.value.isCompleted()} | isCancelled=${_ui.value.isCancelled()}")
            appendLine("withinWindow=$within => canCheckIn()=${_ui.value.canCheckIn(today, zone)}")
            appendLine("showCheckOutButton=${_ui.value.showCheckOutButton()}")
            appendLine("================================")
        })
    }
}
