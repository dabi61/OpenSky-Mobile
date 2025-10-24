package com.dabi.opensky.feature.voucher

import com.dabi.opensky.core.model.voucher.Voucher

// feature/home/HomeViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.repository.voucher.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoucherUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val vouchers: List<Voucher> = emptyList(),
    val page: Int = 1,
    val size: Int = 10,
    val totalPages: Int = 1,
    val lastSaveMessage: String? = null // để show snackbar
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val voucherRepo: VoucherRepository
) : ViewModel() {

    private val _voucherState = MutableStateFlow(VoucherUiState())
    val voucherState: StateFlow<VoucherUiState> = _voucherState.asStateFlow()

    init {
        loadVouchers(1)
    }

    fun loadVouchers(page: Int, size: Int = _voucherState.value.size) {
        viewModelScope.launch {
            _voucherState.value = _voucherState.value.copy(isLoading = true, errorMessage = null)
            val res = voucherRepo.getActive(page, size)
            _voucherState.value = res.fold(
                onSuccess = {
                    _voucherState.value.copy(
                        isLoading = false,
                        vouchers = it.vouchers,
                        page = it.page,
                        size = it.size,
                        totalPages = it.totalPages
                    )
                },
                onFailure = { e ->
                    _voucherState.value.copy(isLoading = false, errorMessage = e.message ?: "Không tải được ưu đãi")
                }
            )
        }
    }

    fun saveVoucher(voucherID: String) {
        viewModelScope.launch {
            val res = voucherRepo.saveVoucher(voucherID)
            _voucherState.value = res.fold(
                onSuccess = {
                    _voucherState.value.copy(lastSaveMessage = "Đã lưu voucher!")
                },
                onFailure = { e ->
                    _voucherState.value.copy(lastSaveMessage = e.message ?: "Lưu voucher thất bại")
                }
            )
        }
    }



    fun clearVoucherMessage() {
        _voucherState.value = _voucherState.value.copy(lastSaveMessage = null)
    }

    fun clearVoucherError() {
        _voucherState.value = _voucherState.value.copy(errorMessage = null)
    }
}
