// core/model/billing/BillsResponse.kt
package com.dabi.opensky.core.model.billing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BillsResponse(
    @Json(name = "bills") val bills: List<BillItem>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNextPage") val hasNextPage: Boolean,
    @Json(name = "hasPreviousPage") val hasPreviousPage: Boolean
)

@JsonClass(generateAdapter = true)
data class BillItem(
    @Json(name = "billID") val billID: String,
    @Json(name = "userID") val userID: String,
    @Json(name = "targetID") val targetID: String,
    @Json(name = "userName") val userName: String,
    @Json(name = "bookingID") val bookingID: String?,
    @Json(name = "startTime") val startTime: String, // ISO-8601
    @Json(name = "endTime") val endTime: String,     // ISO-8601
    @Json(name = "deposit") val deposit: Long,
    @Json(name = "refundPrice") val refundPrice: Long?,
    @Json(name = "totalPrice") val totalPrice: Long,
    @Json(name = "originalTotalPrice") val originalTotalPrice: Long,
    @Json(name = "discountAmount") val discountAmount: Long,
    @Json(name = "discountPercent") val discountPercent: Int,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String,
    @Json(name = "userVoucherID") val userVoucherID: String?,
    @Json(name = "voucherInfo") val voucherInfo: String?,
    @Json(name = "user") val user: BillUser,
    @Json(name = "billDetails") val billDetails: List<BillDetail>
)

@JsonClass(generateAdapter = true)
data class BillUser(
    @Json(name = "userID") val userID: String,
    @Json(name = "email") val email: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "phoneNumber") val phoneNumber: String?,
    @Json(name = "citizenId") val citizenId: String?
)

@JsonClass(generateAdapter = true)
data class BillDetail(
    @Json(name = "billDetailID") val billDetailID: String,
    @Json(name = "billID") val billID: String,
    @Json(name = "itemType") val itemType: String,   // Hotel/Tour...
    @Json(name = "itemID") val itemID: String,
    @Json(name = "itemName") val itemName: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unitPrice") val unitPrice: Long,
    @Json(name = "totalPrice") val totalPrice: Long,
    @Json(name = "notes") val notes: String?,
    @Json(name = "createdAt") val createdAt: String
)
