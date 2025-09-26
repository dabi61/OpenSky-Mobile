package com.dabi.opensky.feature.hotel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.HotelRepository
import com.dabi.opensky.core.model.hotel.Hotel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HotelDetailViewModel @Inject constructor(
    private val hotelRepository: HotelRepository
) : ViewModel() {
    
    private val _hotelDetail = MutableStateFlow<Resource<Hotel>>(Resource.Loading)
    val hotelDetail: StateFlow<Resource<Hotel>> = _hotelDetail.asStateFlow()
    
    fun loadHotelDetail(hotelId: String) {
        viewModelScope.launch {
            println("HotelDetailViewModel: Loading hotel detail for ID: $hotelId")
            _hotelDetail.value = Resource.Loading
            
            hotelRepository.getHotelById(hotelId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            println("HotelDetailViewModel: Successfully loaded hotel: ${resource.data.hotelName}")
                        }
                        is Resource.Error -> {
                            println("HotelDetailViewModel: Error loading hotel: ${resource.cause.message}")
                        }
                        is Resource.Loading -> {
                            println("HotelDetailViewModel: Loading hotel detail...")
                        }
                    }
                    _hotelDetail.value = resource
                }
        }
    }
    
    fun retry(hotelId: String) {
        loadHotelDetail(hotelId)
    }
}
