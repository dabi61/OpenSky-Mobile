package com.dabi.opensky.core.model.voucher


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Voucher(
    @Json(name = "voucherID") val voucherID: String,
    @Json(name = "code") val code: String,
    @Json(name = "percent") val percent: Int,
    @Json(name = "tableType") val tableType: String, // "Hotel" | "Tour" | ...
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "description") val description: String?,
    @Json(name = "isDeleted") val isDeleted: Boolean,
    @Json(name = "usedCount") val usedCount: Int,
    @Json(name = "isExpired") val isExpired: Boolean,
    @Json(name = "isAvailable") val isAvailable: Boolean
)

@JsonClass(generateAdapter = true)
data class VoucherListResponse(
    @Json(name = "vouchers") val vouchers: List<Voucher>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalPages") val totalPages: Int
)

// Request save voucher
@JsonClass(generateAdapter = true)
data class SaveVoucherRequest(
    @Json(name = "voucherID") val voucherID: String
)


@JsonClass(generateAdapter = true)
data class MyVouchersResponse(
    @Json(name = "userVouchers") val userVouchers: List<UserVoucherItem>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalPages") val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class UserVoucherItem(
    @Json(name = "userVoucherID") val userVoucherID: String,
    @Json(name = "userID") val userID: String,
    @Json(name = "voucherID") val voucherID: String,
    @Json(name = "isUsed") val isUsed: Boolean,
    @Json(name = "savedAt") val savedAt: String?,
    @Json(name = "userName") val userName: String?,
    @Json(name = "voucherCode") val voucherCode: String,
    @Json(name = "voucherPercent") val voucherPercent: Int,
    @Json(name = "voucherTableType") val voucherTableType: String, // "Hotel" | "Tour"
    @Json(name = "voucherStartDate") val voucherStartDate: String?, // ISO
    @Json(name = "voucherEndDate") val voucherEndDate: String?,     // ISO
    @Json(name = "voucherDescription") val voucherDescription: String?,
    @Json(name = "voucherIsExpired") val voucherIsExpired: Boolean
)

// (Optional) server có thể trả body nào cũng được; nếu rỗng ta dùng Unit