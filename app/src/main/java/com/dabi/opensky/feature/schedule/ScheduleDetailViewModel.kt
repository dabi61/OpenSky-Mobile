// feature/tour/ScheduleDetailViewModel.kt
package com.dabi.opensky.feature.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.data.repository.schedule.ScheduleRepository
import com.dabi.opensky.core.model.booking.TourBookingRequest
import com.dabi.opensky.core.model.booking.TourBookingResponse
import com.dabi.opensky.core.model.schedule.ScheduleDetailResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailViewModel @Inject constructor(
    private val bookingRepo: BookingRepository,
    private val scheduleRepo: ScheduleRepository   // <-- dùng interface có suspend
) : ViewModel() {

    // ------ Detail state (Resource<ScheduleDetailResponse>) ------
    private val _detail = MutableStateFlow<Resource<ScheduleDetailResponse>>(Resource.Loading)
    val detail: StateFlow<Resource<ScheduleDetailResponse>> = _detail.asStateFlow()

    fun load(scheduleId: String) {
        viewModelScope.launch {
            _detail.value = Resource.Loading
            // Repo trả về Resource, gọi trực tiếp
            val res = scheduleRepo.getScheduleDetail(scheduleId)
            _detail.value = res
        }
    }

    // ------ Booking tour ------
    data class BookingUi(
        val isBooking: Boolean = false,
        val success: TourBookingResponse? = null,
        val error: Throwable? = null
    )

    private val _booking = MutableStateFlow(BookingUi())
    val booking: StateFlow<BookingUi> = _booking.asStateFlow()

    fun createBooking(scheduleId: String, guests: Int, notes: String?) {
        if (guests <= 0) {
            _booking.update { it.copy(error = IllegalArgumentException("Số khách phải > 0")) }
            return
        }
        viewModelScope.launch {
            _booking.update { it.copy(isBooking = true, error = null, success = null) }
            // BookingRepository.createTourBooking vẫn là Flow<Resource<...>> ⇒ collect một lần là đủ
            bookingRepo.createTourBooking(
                TourBookingRequest(scheduleID = scheduleId, numberOfGuests = guests, notes = notes)
            ).collect { res ->
                when (res) {
                    is Resource.Loading -> _booking.update { it.copy(isBooking = true) }
                    is Resource.Success -> _booking.value = BookingUi(isBooking = false, success = res.data)
                    is Resource.Error   -> _booking.value = BookingUi(isBooking = false, error = res.cause)
                }
            }
        }
    }

    fun clearBookingMessage() { _booking.value = BookingUi() }
}
