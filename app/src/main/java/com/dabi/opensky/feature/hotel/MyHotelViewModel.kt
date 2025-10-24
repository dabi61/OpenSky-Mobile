// feature/hotel/MyHotelsViewModel.kt
package com.dabi.opensky.feature.hotel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.HotelRepository
import com.dabi.opensky.core.data.repository.room.RoomRepository
import com.dabi.opensky.core.model.hotel.Hotel
import com.dabi.opensky.core.model.room.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MyHotelsUiState(
    val hotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Rooms của hotel đang chọn
    val selectedHotelId: String? = null,
    val rooms: List<Room> = emptyList(),
    val roomsLoading: Boolean = false,
    val roomsError: String? = null,
    val statusFilter: String? = null // null = All
)

@HiltViewModel
class MyHotelsViewModel @Inject constructor(
    private val hotelRepo: HotelRepository,
    private val roomRepo: RoomRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyHotelsUiState(isLoading = true))
    val state: StateFlow<MyHotelsUiState> = _state.asStateFlow()

    init { loadHotels() }

    fun loadHotels() {
        viewModelScope.launch {
            hotelRepo.getMyHotels().collect { res ->
                when (res) {
                    Resource.Loading -> _state.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _state.update { it.copy(hotels = res.data, isLoading = false) }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, hotels = emptyList(), errorMessage = res.cause.message ?: "Có lỗi xảy ra") }
                }
            }
        }
    }

    fun selectHotel(hotelId: String) {
        _state.update { it.copy(selectedHotelId = hotelId) }
        loadRooms(hotelId, status = null)
    }

    fun setRoomStatusFilter(status: String?) {
        val hotelId = _state.value.selectedHotelId ?: return
        _state.update { it.copy(statusFilter = status) }
        loadRooms(hotelId, status)
    }

    private fun loadRooms(hotelId: String, status: String?) {
        viewModelScope.launch {
            _state.update { it.copy(roomsLoading = true, roomsError = null) }

            if (status.isNullOrBlank() || status == "All") {
                roomRepo.getRoomsByHotel(hotelId, page = 1, limit = 50).collect { res ->
                    when (res) {
                        Resource.Loading -> _state.update { it.copy(roomsLoading = true) }
                        is Resource.Error -> _state.update { it.copy(roomsLoading = false, roomsError = res.cause.message) }
                        is Resource.Success -> _state.update { it.copy(roomsLoading = false, rooms = res.data.first) }
                    }
                }
            } else {
                roomRepo.getRoomsByHotelAndStatus(hotelId, status).collect { res ->
                    when (res) {
                        Resource.Loading -> _state.update { it.copy(roomsLoading = true) }
                        is Resource.Error -> _state.update { it.copy(roomsLoading = false, roomsError = res.cause.message) }
                        is Resource.Success -> _state.update {
                            it.copy(roomsLoading = false, rooms = res.data.rooms.map { s -> Room(
                                id = s.roomID,
                                name = s.roomName,
                                type = s.roomType,
                                price = null,
                                capacity = null, address = null,
                                // các field khác nếu cần map thêm… (tùy Room của bạn)
                                imageUrl = null,
                                status = status
                            ) })
                        }
                    }
                }
            }
        }
    }

    // ---- Actions: create / update / delete / status ----

    fun createRoom(
        hotelId: String,
        roomName: String,
        roomType: String,
        address: String,
        price: Double,
        maxPeople: Int,
        files: List<File>
    ) {
        viewModelScope.launch {
            roomRepo.createRoom(hotelId, roomName, roomType, address, price, maxPeople, files).collect {
                when (it) {
                    Resource.Loading -> _state.update { s -> s.copy(roomsLoading = true) }
                    is Resource.Error -> _state.update { s -> s.copy(roomsLoading = false, roomsError = it.cause.message) }
                    is Resource.Success -> {
                        // reload rooms
                        _state.value.selectedHotelId?.let { id -> loadRooms(id, _state.value.statusFilter) }
                    }
                }
            }
        }
    }

    fun updateRoom(
        roomId: String,
        roomName: String? = null,
        roomType: String? = null,
        address: String? = null,
        price: Double? = null,
        maxPeople: Int? = null,
        existingImageIdsCsv: String? = null,
        deleteImageIdsCsv: String? = null,
        files: List<File> = emptyList()
    ) {
        viewModelScope.launch {
            roomRepo.updateRoom(roomId, roomName, roomType, address, price, maxPeople, existingImageIdsCsv, deleteImageIdsCsv, files).collect {
                when (it) {
                    Resource.Loading -> _state.update { s -> s.copy(roomsLoading = true) }
                    is Resource.Error -> _state.update { s -> s.copy(roomsLoading = false, roomsError = it.cause.message) }
                    is Resource.Success -> {
                        _state.value.selectedHotelId?.let { id -> loadRooms(id, _state.value.statusFilter) }
                    }
                }
            }
        }
    }

    fun updateRoomStatus(roomId: String, status: String) {
        viewModelScope.launch {
            roomRepo.updateRoomStatus(roomId, status).collect {
                when (it) {
                    Resource.Loading -> _state.update { s -> s.copy(roomsLoading = true) }
                    is Resource.Error -> _state.update { s -> s.copy(roomsLoading = false, roomsError = it.cause.message) }
                    is Resource.Success -> {
                        _state.value.selectedHotelId?.let { id -> loadRooms(id, _state.value.statusFilter) }
                    }
                }
            }
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            roomRepo.deleteRoom(roomId).collect {
                when (it) {
                    Resource.Loading -> _state.update { s -> s.copy(roomsLoading = true) }
                    is Resource.Error -> _state.update { s -> s.copy(roomsLoading = false, roomsError = it.cause.message) }
                    is Resource.Success -> {
                        _state.value.selectedHotelId?.let { id -> loadRooms(id, _state.value.statusFilter) }
                    }
                }
            }
        }
    }

    fun retryHotels() = loadHotels()
}
