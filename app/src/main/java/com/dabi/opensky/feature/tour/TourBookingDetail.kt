// feature/tourdetail/TourBookingDetailViewModel.kt
package com.dabi.opensky.feature.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.model.booking.TourBookingDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TourBookingDetailUiState(
    val data: Resource<TourBookingDetail> = Resource.Loading
)

@HiltViewModel
class TourBookingDetailViewModel @Inject constructor(
    private val repo: BookingRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(TourBookingDetailUiState())
    val ui: StateFlow<TourBookingDetailUiState> = _ui

    fun load(bookingId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(data = Resource.Loading) }
            repo.getTourBookingDetail(bookingId).collect { res ->
                _ui.update { it.copy(data = res) }
            }
        }
    }
}
