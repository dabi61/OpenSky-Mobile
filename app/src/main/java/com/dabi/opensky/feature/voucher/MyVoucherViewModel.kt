// feature/voucher/MyVouchersViewModel.kt
package com.dabi.opensky.feature.voucher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.repository.voucher.VoucherRepository
import com.dabi.opensky.core.model.voucher.UserVoucherItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyVouchersUiState(
    val items: List<UserVoucherItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val size: Int = 10,
    val endReached: Boolean = false
) {
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()
}

@HiltViewModel
class MyVouchersViewModel @Inject constructor(
    private val repo: VoucherRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(MyVouchersUiState())
    val ui: StateFlow<MyVouchersUiState> = _ui

    fun start(pageSize: Int = 10) {
        if (_ui.value.items.isNotEmpty()) return
        _ui.update { it.copy(page = 1, size = pageSize) }
        loadPage(1)
    }

    fun refresh() {
        _ui.update { MyVouchersUiState(size = it.size) }
        loadPage(1)
    }

    fun loadNextPage() {
        val s = _ui.value
        if (s.isLoading || s.endReached) return
        loadPage(s.page + 1)
    }

    private fun loadPage(page: Int) {
        val size = _ui.value.size
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }

            val result = repo.getMyVouchers(page, size)
            result.fold(
                onSuccess = { payload ->
                    _ui.update { cur ->
                        cur.copy(
                            isLoading = false,
                            items = if (page == 1) payload.userVouchers else cur.items + payload.userVouchers,
                            page = payload.page,
                            endReached = payload.page >= payload.totalPages,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "Có lỗi xảy ra") }
                }
            )
        }
    }
}
