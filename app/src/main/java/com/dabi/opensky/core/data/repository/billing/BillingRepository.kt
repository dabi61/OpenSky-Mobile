package com.dabi.opensky.core.data.repository.billing

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.billing.BillItem
import com.dabi.opensky.core.model.billing.BillsResponse
import com.dabi.opensky.core.model.payment.CreateQrResponse
import com.dabi.opensky.core.model.payment.ScanQrResponse
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    suspend fun getMyBills(page: Int, size: Int): Flow<Resource<BillsResponse>>
    suspend fun createBillQr(billId: String): Flow<Resource<CreateQrResponse>>
    suspend fun scanQr(qrCode: String): Flow<Resource<ScanQrResponse>>
}