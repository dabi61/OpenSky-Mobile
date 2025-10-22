package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.voucher.SaveVoucherRequest
import com.dabi.opensky.core.model.voucher.VoucherListResponse
import retrofit2.Response
import retrofit2.http.*

interface VoucherService {
    @GET("vouchers/active")
    suspend fun getActiveVouchers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<VoucherListResponse>

    @POST("user_vouchers")
    suspend fun saveVoucher(@Body body: SaveVoucherRequest): Response<Unit>
}