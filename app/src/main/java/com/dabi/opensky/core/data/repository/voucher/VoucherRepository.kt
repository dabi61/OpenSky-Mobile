package com.dabi.opensky.core.data.repository.voucher

import com.dabi.opensky.core.model.voucher.VoucherListResponse

interface VoucherRepository {
    suspend fun getActive(page: Int, size: Int): Result<VoucherListResponse>
    suspend fun saveVoucher(voucherID: String): Result<Unit>
}