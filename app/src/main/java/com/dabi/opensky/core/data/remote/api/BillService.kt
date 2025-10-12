package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.billing.BillsResponse
import com.dabi.opensky.core.model.payment.CreateQrRequest
import com.dabi.opensky.core.model.payment.CreateQrResponse
import com.dabi.opensky.core.model.payment.ScanQrRequest
import com.dabi.opensky.core.model.payment.ScanQrResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BillService {
    @GET("bills/my")
    suspend fun getMyBills(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<BillsResponse>

    @POST("bills/qr/create")
    suspend fun createBillQr(
        @Body body: CreateQrRequest
    ): Response<CreateQrResponse>

    @POST("bills/qr/scan")
    suspend fun scanQr(
        @Body body: ScanQrRequest
    ): Response<ScanQrResponse>
}