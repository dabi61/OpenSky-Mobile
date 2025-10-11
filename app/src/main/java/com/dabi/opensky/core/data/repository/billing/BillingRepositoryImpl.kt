package com.dabi.opensky.core.data.repository.billing

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.BillService
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.model.billing.BillItem
import com.dabi.opensky.core.model.billing.BillsResponse
import com.dabi.opensky.core.model.payment.CreateQrRequest
import com.dabi.opensky.core.model.payment.CreateQrResponse
import com.dabi.opensky.core.model.payment.ScanQrRequest
import com.dabi.opensky.core.model.payment.ScanQrResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val api: BillService
): BillingRepository {
    override suspend fun getMyBills(page: Int, size: Int): Flow<Resource<BillsResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.getMyBills(page, size) })
    }

    override suspend fun createBillQr(billId: String): Flow<Resource<CreateQrResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.createBillQr(CreateQrRequest(billId)) })
    }

    override suspend fun scanQr(qrCode: String): Flow<Resource<ScanQrResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.scanQr(ScanQrRequest(qrCode)) })
    }


}
