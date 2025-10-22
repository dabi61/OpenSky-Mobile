package com.dabi.opensky.core.data.repository.voucher

import com.dabi.opensky.core.data.remote.api.VoucherService
import com.dabi.opensky.core.model.voucher.SaveVoucherRequest
import com.dabi.opensky.core.model.voucher.VoucherListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepositoryImpl @Inject constructor(
    private val service: VoucherService
) : VoucherRepository {

    override suspend fun getActive(page: Int, size: Int): Result<VoucherListResponse> {
        return try {
            val res = service.getActiveVouchers(page, size)
            if (!res.isSuccessful) Result.failure(Exception("Voucher get failed: ${res.code()}"))
            else res.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty body"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveVoucher(voucherID: String): Result<Unit> {
        return try {
            val res = service.saveVoucher(SaveVoucherRequest(voucherID))
            if (!res.isSuccessful) Result.failure(Exception("Save voucher failed: ${res.code()}"))
            else Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}