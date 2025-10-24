// feature/checkin/BookingDetailScreen.kt
package com.dabi.opensky.feature.booking

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.booking.RoomDetailLine
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onBack: () -> Unit,
    viewModel: BookingDetailViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(bookingId) { viewModel.load(bookingId) }

    // feedback check-in/out
    LaunchedEffect(ui.checkInAction) {
        when (val a = ui.checkInAction) {
            is Resource.Success -> snack.showSnackbar(a.data.message)
            is Resource.Error -> snack.showSnackbar(a.cause.message ?: "Check-in thất bại")
            else -> Unit
        }
    }
    LaunchedEffect(ui.checkOutAction) {
        when (val a = ui.checkOutAction) {
            is Resource.Success -> snack.showSnackbar(a.data.message)
            is Resource.Error -> snack.showSnackbar(a.cause.message ?: "Check-out thất bại")
            else -> Unit
        }
    }

    val detail = ui.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đặt phòng") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snack) },
//        bottomBar = {
//            if (detail != null) {
//                val enableCheckIn = ui.canCheckIn()               // theo logic mới
//                val showCheckOut = ui.showCheckOutButton()       // chỉ khi đã check-in
//
//                BottomAppBar {
//                    if (showCheckOut) {
//                        OutlinedButton(
//                            onClick = { viewModel.doCheckOut() },
//                            enabled = ui.checkOutAction !is Resource.Loading,
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            if (ui.checkOutAction is Resource.Loading) {
//                                CircularProgressIndicator(Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
//                            }
//                            Text("Check-out")
//                        }
//                        Spacer(Modifier.width(12.dp))
//                    }
//                    Button(
//                        onClick = { viewModel.doCheckIn() },
//                        enabled = enableCheckIn && ui.checkInAction !is Resource.Loading,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        if (ui.checkInAction is Resource.Loading) {
//                            CircularProgressIndicator(Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
//                        }
//                        Text("Check-in")
//                    }
//                }
//            }
//        }
    ) { padding ->
        when {
            ui.isLoading && detail == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ui.error != null && detail == null -> {
                Column(
                    Modifier.padding(padding).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Không thể tải", style = MaterialTheme.typography.titleMedium)
                    Text(ui.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.load(bookingId) }) { Text("Thử lại") }
                }
            }
            detail != null -> {
                val zone = ZoneId.systemDefault()
                val ci = runCatching { Instant.parse(detail.checkInDate).atZone(zone).toLocalDate() }
                    .getOrNull()?.toString() ?: detail.checkInDate
                val co = runCatching { Instant.parse(detail.checkOutDate).atZone(zone).toLocalDate() }
                    .getOrNull()?.toString() ?: detail.checkOutDate

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    item {
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(detail.hotelName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(detail.hotelAddress, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatusChip(detail.status)
                                    detail.paymentStatus?.takeIf { it.isNotBlank() }?.let { PayChip(it) }
                                }
                            }
                        }
                    }
                    // Info
                    item {
                        Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Thông tin đặt phòng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(10.dp))
                                InfoRow("Nhận phòng", ci)
                                Spacer(Modifier.height(6.dp))
                                InfoRow("Trả phòng", co)
                                Spacer(Modifier.height(6.dp))
                                InfoRow("Số đêm", detail.numberOfNights.toString())
                                detail.notes?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(10.dp)); Text("Ghi chú: $it")
                                }
                            }
                        }
                    }
                    // Rooms
                    item {
                        Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Chi tiết phòng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                detail.roomDetails.forEach { RoomRow(it) }
                                Spacer(Modifier.height(10.dp))
                                InfoRow("Tổng tiền", detail.totalPrice.toVnd())
                            }
                        }
                    }
                    // Timeline
//                    item {
//                        Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(0.dp)) {
//                            Column(Modifier.padding(16.dp)) {
//                                Text("Lịch trình", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//                                Spacer(Modifier.height(8.dp))
//                                TimelineRow("Đặt phòng", "Hoàn tất đơn", active = true)
//                                TimelineRow("Check-in", "Tại quầy lễ tân / quét QR", active = ui.canCheckIn())
//                                TimelineRow("Check-out", "Thanh lý / trả phòng", active = ui.showCheckOutButton())
//                            }
//                        }
//                    }
                }
            }
            else -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/* ==== Helpers UI (giữ nguyên như bạn đang có) ==== */
@Composable private fun TimelineRow(title: String, subtitle: String, active: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val dot = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        Box(Modifier.size(12.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun RoomRow(line: RoomDetailLine) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("${line.roomName} • ${line.roomType}", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text("SL: ${line.quantity}  •  Đơn giá: ${line.unitPrice.toVnd()}  •  Thành tiền: ${line.totalPrice.toVnd()}",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!line.notes.isNullOrBlank()) Text(line.notes!!, style = MaterialTheme.typography.bodySmall)
    }
}

private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')
