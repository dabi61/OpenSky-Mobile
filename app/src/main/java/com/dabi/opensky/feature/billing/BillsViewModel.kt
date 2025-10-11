// feature/billing/MyBillsViewModel.kt
package com.dabi.opensky.feature.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.billing.BillingRepository
import com.dabi.opensky.core.model.billing.BillItem
import com.dabi.opensky.core.model.billing.BillsResponse
import com.dabi.opensky.core.model.payment.CreateQrResponse
import com.dabi.opensky.core.model.payment.ScanQrResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.isNotEmpty

data class PaymentUiState(
    val visible: Boolean = false,
    val forBill: BillItem? = null,
    val createQr: Resource<CreateQrResponse>? = null,
    val scan: Resource<ScanQrResponse>? = null
)

data class BillsUiState(
    val items: List<BillItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val size: Int = 10,
    val endReached: Boolean = false,
    val payment: PaymentUiState = PaymentUiState()
) {
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()
}

@HiltViewModel
class MyBillsViewModel @Inject constructor(
    private val repo: BillingRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(BillsUiState())
    val ui: StateFlow<BillsUiState> = _ui

    fun start(pageSize: Int = 10) {
        if (_ui.value.items.isNotEmpty()) return
        _ui.update { it.copy(page = 1, size = pageSize) }
        loadPage(1)
    }

    fun loadNextPage() {
        val s = _ui.value
        if (s.isLoading || s.endReached) return
        loadPage(s.page + 1)
    }

    fun refresh() {
        _ui.update { BillsUiState(size = it.size) }
        loadPage(1)
    }

    private fun loadPage(page: Int) {
        val size = _ui.value.size
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            repo.getMyBills(page, size).collect { res ->
                when (res) {
                    is Resource.Loading -> _ui.update { it.copy(isLoading = true) }
                    is Resource.Error -> _ui.update { it.copy(isLoading = false, error = res.cause.message) }
                    is Resource.Success -> appendPage(res.data)
                }
            }
        }
    }

    private fun appendPage(payload: BillsResponse) {
        _ui.update { cur ->
            cur.copy(
                isLoading = false,
                items = if (payload.page == 1) payload.bills else cur.items + payload.bills,
                page = payload.page,
                endReached = !payload.hasNextPage
            )
        }
    }

    /* ---------- Payment flow ---------- */
    fun openPayment(bill: BillItem) {
        _ui.update {
            it.copy(
                payment = PaymentUiState(
                    visible = true,
                    forBill = bill,
                    createQr = null,
                    scan = null
                )
            )
        }
        createQr(bill.billID)
    }

    fun closePayment() {
        _ui.update { it.copy(payment = PaymentUiState()) }
    }

    private fun createQr(billId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(payment = it.payment.copy(createQr = Resource.Loading)) }
            repo.createBillQr(billId).collect { res ->
                _ui.update { it.copy(payment = it.payment.copy(createQr = res)) }
            }
        }
    }

    fun confirmPayment() {
        val qr = (ui.value.payment.createQr as? Resource.Success)?.data?.qrCode ?: return
        viewModelScope.launch {
            _ui.update { it.copy(payment = it.payment.copy(scan = Resource.Loading)) }
            repo.scanQr(qr).collect { res ->
                _ui.update { state ->
                    val updated = state.copy(payment = state.payment.copy(scan = res))
                    if (res is Resource.Success && res.data.status.equals("Paid", true)) {
                        // Cập nhật bill thành Paid trong danh sách
                        val id = state.payment.forBill?.billID
                        updated.copy(
                            items = state.items.map { b ->
                                if (b.billID == id) b.copy(status = "Paid") else b
                            }
                        )
                    } else updated
                }
            }
        }
    }
}
