// feature/checkin/MyBookingsViewModel.kt
package com.dabi.opensky.feature.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.model.booking.HotelBookingSummary
import com.dabi.opensky.core.model.booking.MyBookingsResponse
import com.dabi.opensky.core.model.booking.TourBookingSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class BookingTab { HOTEL, TOUR }

data class MyBookingsUiState(
    val tab: BookingTab = BookingTab.HOTEL,
    val hotelItems: List<HotelBookingSummary> = emptyList(),
    val tourItems: List<TourBookingSummary> = emptyList(),   // NEW
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val limit: Int = 10,
    val endReached: Boolean = true
) {
    val tourCount: Int get() = tourItems.size
    val isEmpty: Boolean get() =
        !isLoading && error == null && when (tab) {
            BookingTab.HOTEL -> hotelItems.isEmpty()
            BookingTab.TOUR  -> tourItems.isEmpty()
        }
}

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val repo: BookingRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(MyBookingsUiState())
    val ui: StateFlow<MyBookingsUiState> = _ui

    fun start(limit: Int = 10) {
        if (_ui.value.hotelItems.isNotEmpty() || _ui.value.tourItems.isNotEmpty()) return
        _ui.update { it.copy(page = 1, limit = limit) }
        load(1)
    }

    fun switchTab(tab: BookingTab) { _ui.update { it.copy(tab = tab) } }

    fun refresh() { _ui.update { it.copy(page = 1) }; load(1) }

    fun loadNext() {
        val s = _ui.value
        if (s.isLoading || s.endReached) return
        load(s.page + 1)
    }

    private fun load(page: Int) {
        val limit = _ui.value.limit
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            repo.getMyBookings(page, limit).collect { res ->
                when (res) {
                    is Resource.Loading -> _ui.update { it.copy(isLoading = true) }
                    is Resource.Error   -> _ui.update { it.copy(isLoading = false, error = res.cause.message) }
                    is Resource.Success -> applyPage(res.data)
                }
            }
        }
    }

    private fun applyPage(data: MyBookingsResponse) {
        _ui.update { cur ->
            val newHotels = if (data.page == 1) data.hotelBookings else cur.hotelItems + data.hotelBookings
            val newTours  = if (data.page == 1) data.tourBookings  else cur.tourItems  + data.tourBookings

            cur.copy(
                isLoading = false,
                hotelItems = newHotels,
                tourItems = newTours,
                page = data.page,
                endReached = data.page >= data.totalPages ||
                        (data.hotelBookings.isEmpty() && data.tourBookings.isEmpty())
            )
        }
    }
}
