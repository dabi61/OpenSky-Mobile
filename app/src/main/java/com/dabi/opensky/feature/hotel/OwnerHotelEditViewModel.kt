package com.dabi.opensky.feature.ownerhotel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.HotelRepository
import com.dabi.opensky.core.model.hotel.MyHotelImageDto
import com.dabi.opensky.core.model.hotel.MyHotelResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class OwnerHotelEditState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastMessage: String? = null,

    val hotelId: String = "",
    val hotelName: String = "",
    val description: String = "",
    val address: String = "",
    val province: String = "",
    val latitude: String = "",
    val longitude: String = "",

    // ảnh cũ từ API my-hotels: có id + url
    val existingImages: List<MyHotelImageDto> = emptyList(),
    val deleteImageIds: Set<Int> = emptySet(),   // imageId muốn xoá

    // ảnh mới chọn (uri + file tạm)
    val newImageUris: List<Uri> = emptyList(),
    val cachedNewFiles: List<File> = emptyList()
)

@HiltViewModel
class OwnerHotelEditViewModel @Inject constructor(
    private val repo: HotelRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerHotelEditState())
    val state: StateFlow<OwnerHotelEditState> = _state

    /** Prefill từ MyHotelResponse (màn trước truyền qua) */
    fun startEdit(h: MyHotelResponse) {
        _state.update {
            it.copy(
                hotelId = h.hotelID,
                hotelName = h.hotelName,
                description = h.description.orEmpty(),
                address = h.address.orEmpty(),
                province = h.province.orEmpty(),
                latitude = h.latitude?.toString().orEmpty(),
                longitude = h.longitude?.toString().orEmpty(),
                existingImages = h.images
            )
        }
    }

    /** Trường hợp chỉ có hotelId (muốn fetch lại một mình) */
    fun loadByHotelId(hotelId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, hotelId = hotelId, errorMessage = null) }
            repo.getMyHotelById(hotelId).collect { res ->
                when (res) {
                    Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val h = res.data
                        if (h == null) {
                            _state.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy khách sạn") }
                        } else {
                            startEdit(h)
                            _state.update { it.copy(isLoading = false) }
                        }
                    }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, errorMessage = res.cause.message) }
                }
            }
        }
    }

    // Cập nhật field
    fun onFieldChange(
        name: String? = null,
        description: String? = null,
        address: String? = null,
        province: String? = null,
        latitude: String? = null,
        longitude: String? = null
    ) {
        _state.update {
            it.copy(
                hotelName = name ?: it.hotelName,
                description = description ?: it.description,
                address = address ?: it.address,
                province = province ?: it.province,
                latitude = latitude ?: it.latitude,
                longitude = longitude ?: it.longitude
            )
        }
    }

    fun toggleDeleteImage(imageId: Int) {
        _state.update {
            val next = it.deleteImageIds.toMutableSet().apply {
                if (contains(imageId)) remove(imageId) else add(imageId)
            }
            it.copy(deleteImageIds = next)
        }
    }

    fun addNewImages(
        resolver: ContentResolver,
        cacheDir: File,
        uris: List<Uri>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = uris.mapNotNull { uri ->
                try {
                    val name = "img_${System.currentTimeMillis()}.jpg"
                    val f = File(cacheDir, name)
                    resolver.openInputStream(uri)?.use { input ->
                        f.outputStream().use { output -> input.copyTo(output) }
                    }
                    f
                } catch (_: Throwable) { null }
            }
            _state.update {
                it.copy(
                    newImageUris = it.newImageUris + uris,
                    cachedNewFiles = it.cachedNewFiles + files
                )
            }
        }
    }

    fun removeNewImageAt(index: Int) {
        _state.update {
            val newUris = it.newImageUris.toMutableList().apply { if (index in indices) removeAt(index) }
            val newFiles = it.cachedNewFiles.toMutableList().apply { if (index in indices) removeAt(index) }
            it.copy(newImageUris = newUris, cachedNewFiles = newFiles)
        }
    }

    fun clearMessage() { _state.update { it.copy(lastMessage = null) } }

    /** Lưu: chỉ gửi phần thay đổi + hình ảnh */
    fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            // ID ảnh giữ lại = ảnh cũ KHÔNG bị tick xoá
            val existingIds = s.existingImages
                .map { it.imageId }
                .filter { it !in s.deleteImageIds }

            val deleteIds = s.deleteImageIds.toList()

            val existingCsv: String? = existingIds.takeIf { it.isNotEmpty() }?.joinToString(",")
            val deleteCsv: String? = deleteIds.takeIf { it.isNotEmpty() }?.joinToString(",")

            // Gửi null cho field trống để BE giữ nguyên
            repo.updateOwnerHotel(
                hotelId = s.hotelId,
                hotelName = s.hotelName.takeIf { it.isNotBlank() },
                description = s.description.takeIf { it.isNotBlank() },
                address = s.address.takeIf { it.isNotBlank() },
                province = s.province.takeIf { it.isNotBlank() },
                latitude = s.latitude.takeIf { it.isNotBlank() },
                longitude = s.longitude.takeIf { it.isNotBlank() },
                existingImageIdsCsv = existingCsv,
                deleteImageIdsCsv = deleteCsv,
                newImageFiles = s.cachedNewFiles.takeIf { it.isNotEmpty() }
            ).collect { res ->
                when (res) {
                    Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Success -> _state.update {
                        it.copy(
                            isLoading = false,
                            lastMessage = res.data.message ?: "Cập nhật thành công"
                        )
                    }
                    is Resource.Error -> _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.cause.message ?: "Cập nhật thất bại"
                        )
                    }
                }
            }
        }
    }
}
