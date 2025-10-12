package com.dabi.opensky.core.model.payment

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateQrRequest(
    @Json(name = "billId") val billId: String
)

@JsonClass(generateAdapter = true)
data class CreateQrResponse(
    @Json(name = "qrCode") val qrCode: String,
    @Json(name = "paymentUrl") val paymentUrl: String,
    @Json(name = "billId") val billId: String,
    @Json(name = "amount") val amount: Long,
    @Json(name = "orderDescription") val orderDescription: String,
    @Json(name = "expiresAt") val expiresAt: String
)

@JsonClass(generateAdapter = true)
data class ScanQrRequest(
    @Json(name = "qrCode") val qrCode: String
)

@JsonClass(generateAdapter = true)
data class ScanQrResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "paidAt") val paidAt: String?
)