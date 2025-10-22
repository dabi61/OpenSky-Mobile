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

// (Optional) server có thể trả body nào cũng được; nếu rỗng ta dùng Unit