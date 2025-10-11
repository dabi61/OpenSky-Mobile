// feature/billing/MyBillsScreen.kt
package com.dabi.opensky.feature.billing

import android.R
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dabi.opensky.core.model.billing.BillDetail
import com.dabi.opensky.core.model.billing.BillItem
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBillsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyBillsViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()

    LaunchedEffect(Unit) { viewModel.start(pageSize = 10) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoá đơn của tôi") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            painter = painterResource(R.drawable.stat_notify_sync),
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()

        // auto-load next page
        LaunchedEffect(listState, ui.items.size, ui.endReached, ui.isLoading) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collectLatest { last ->
                    val total = ui.items.size
                    if (last != null && last >= total - 3 && !ui.endReached && !ui.isLoading) {
                        viewModel.loadNextPage()
                    }
                }
        }

        val payment = ui.payment
        if (payment.visible) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closePayment() },
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                PaymentSheetContent(
                    state = payment,
                    onConfirm = { viewModel.confirmPayment() },
                    onClose = { viewModel.closePayment() }
                )
            }
        }

        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                ui.isLoading && ui.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                ui.error != null && ui.items.isEmpty() -> {
                    ErrorState(message = ui.error ?: "Có lỗi xảy ra", onRetry = { viewModel.refresh() })
                }
                ui.isEmpty -> {
                    EmptyState(onRefresh = { viewModel.refresh() })
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(ui.items, key = { _, b -> b.billID }) { _, bill ->
                            BillCard(bill = bill, onPay = { viewModel.openPayment(it) })
                        }
                        item {
                            if (ui.isLoading) {
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) { CircularProgressIndicator() }
                            } else if (ui.endReached && ui.items.isNotEmpty()) {
                                Text(
                                    "Đã tải hết ${ui.items.size} hoá đơn",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BillCard(bill: BillItem,  onPay: (BillItem) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(text = bill.status)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Mã: ${bill.billID.take(8)}…",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, null)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Khách: ${bill.userName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row {
                Text("Từ: ${bill.startTime.toLocalDateStr()}  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("đến: ${bill.endTime.toLocalDateStr()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                bill.totalPrice.toVnd(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Details
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                bill.billDetails.forEach { d -> BillDetailRow(detail = d) }
                Spacer(Modifier.height(8.dp))
                if (!bill.notesSummary().isNullOrBlank()) {
                    Text(bill.notesSummary()!!, style = MaterialTheme.typography.bodySmall)
                }
            }
            // Footer actions
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!bill.status.equals("Paid", ignoreCase = true)) {
                    Button(onClick = { onPay(bill) }) { Text("Thanh toán") }
                } else {
                    AssistChip(onClick = {}, label = { Text("Đã thanh toán") })
                }
            }
        }
    }
}

@Composable
private fun StatusDot(text: String) {
    val color = when (text.lowercase()) {
        "pending" -> Color(0xFFFFA000)
        "paid", "completed", "success" -> Color(0xFF2E7D32)
        "canceled", "cancelled", "failed" -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.outline
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text)
    }
}

@Composable
private fun BillDetailRow(detail: BillDetail) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("${detail.itemType} • ${detail.itemName}", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Row {
            Text("SL: ${detail.quantity}  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Đơn giá: ${detail.unitPrice.toVnd()}  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Thành tiền: ${detail.totalPrice.toVnd()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!detail.notes.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(detail.notes!!, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EmptyState(onRefresh: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chưa có hoá đơn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("Khi bạn đặt phòng hoặc tour, hoá đơn sẽ xuất hiện ở đây.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRefresh) { Text("Làm mới") }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Không thể tải hoá đơn", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Thử lại") }
    }
}

/* Utils */
private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')
@RequiresApi(Build.VERSION_CODES.O)
private fun String.toLocalDateStr(): String = try {
    val dt = Instant.parse(this).atZone(ZoneId.systemDefault()).toLocalDate()
    dt.toString()
} catch (_: Exception) { this }

// gom notes mẫu: lấy note đầu tiên trong billDetails
private fun BillItem.notesSummary(): String? = billDetails.firstOrNull()?.notes

fun generateQrBitmap(content: String, size: Int = 720): Bitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.RGB_565)
    for (x in 0 until matrix.width) {
        for (y in 0 until matrix.height) {
            bmp.setPixel(x, y, if (matrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
        }
    }
    return bmp
}