// feature/billing/PaymentSheetContent.kt
package com.dabi.opensky.feature.billing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.payment.CreateQrResponse

@Composable
fun PaymentSheetContent(
    state: PaymentUiState,
    onConfirm: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Thanh toán QR", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        when (val qr = state.createQr) {
            is Resource.Loading -> {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(24.dp))
                Text("Đang tạo mã QR…")
            }
            is Resource.Error -> {
                Text(qr.cause.message ?: "Không tạo được QR", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onClose) { Text("Đóng") }
            }
            is Resource.Success -> {
                QrBlock(qr.data)
                Spacer(Modifier.height(16.dp))
                when (val s = state.scan) {
                    is Resource.Loading -> {
                        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Đang xác nhận…")
                        }
                    }
                    is Resource.Success -> {
                        Text(s.data.message, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Đóng") }
                    }
                    is Resource.Error -> {
                        Text(s.cause.message ?: "Xác nhận thất bại", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text("Xác nhận thanh toán") }
                    }
                    null -> {
                        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text("Xác nhận thanh toán") }
                    }
                }
            }
            null -> {}
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun QrBlock(payload: CreateQrResponse) {
    val bmp = remember(payload.paymentUrl) { generateQrBitmap(payload.paymentUrl, size = 720) }
    Image(
        bitmap = bmp.asImageBitmap(),
        contentDescription = "QR thanh toán",
        modifier = Modifier.size(220.dp)
    )
    Spacer(Modifier.height(8.dp))
    Text("Số tiền: ${payload.amount.toVnd()}", fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(4.dp))
    Text(payload.orderDescription, style = MaterialTheme.typography.bodySmall)
    // Bạn có thể thêm countdown theo expiresAt nếu muốn
}

private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')
