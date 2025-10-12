// feature/checkin/BookingDetailScreen.kt
package com.dabi.opensky.feature.booking

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // phản hồi check-in
    LaunchedEffect(ui.checkInAction) {
        val action = ui.checkInAction ?: return@LaunchedEffect
        when (action) {
            is Resource.Success -> snack.showSnackbar(action.data.message)
            is Resource.Error -> snack.showSnackbar(action.cause.message ?: "Check-in thất bại")
            else -> Unit
        }
    }
    // phản hồi check-out
    LaunchedEffect(ui.checkOutAction) {
        val action = ui.checkOutAction ?: return@LaunchedEffect
        when (action) {
            is Resource.Success -> snack.showSnackbar(action.data.message)
            is Resource.Error -> snack.showSnackbar(action.cause.message ?: "Check-out thất bại")
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đặt phòng") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->

        val detail = ui.data

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
                // --- CHỈ VÀO ĐÂY KHI detail != null ---
                val zone = ZoneId.systemDefault()
                val ci = runCatching { Instant.parse(detail.checkInDate).atZone(zone).toLocalDate() }
                    .getOrNull()?.toString() ?: detail.checkInDate
                val co = runCatching { Instant.parse(detail.checkOutDate).atZone(zone).toLocalDate() }
                    .getOrNull()?.toString() ?: detail.checkOutDate

                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(detail.hotelName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(detail.hotelAddress, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Nhận phòng: $ci  •  Trả phòng: $co")
                        Text("Đêm: ${detail.numberOfNights}")
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AssistChip(onClick = {}, label = { Text("Trạng thái: ${detail.status}") })
                            detail.paymentStatus?.takeIf { it.isNotBlank() }?.let {
                                AssistChip(onClick = {}, label = { Text("Thanh toán: $it") })
                            }
                        }
                        detail.notes?.takeIf { it.isNotBlank() }?.let {
                            Spacer(Modifier.height(6.dp))
                            Text("Ghi chú: $it")
                        }
                    }

                    item {
                        Card {
                            Column(Modifier.padding(12.dp)) {
                                Text("Chi tiết phòng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                detail.roomDetails.forEach { RoomRow(it) }
                                Spacer(Modifier.height(6.dp))
                                Text("Tổng tiền: ${detail.totalPrice.toVnd()}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    item {
                        val enableCheckIn = ui.canCheckIn()
                        val enableCheckOut = ui.canCheckOut()

                        Column {
                            Button(
                                onClick = { viewModel.doCheckIn() },
                                enabled = enableCheckIn && ui.checkInAction !is Resource.Loading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (ui.checkInAction is Resource.Loading) {
                                    CircularProgressIndicator(Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                }
                                Text("Check-in")
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.doCheckOut() },
                                enabled = enableCheckOut && ui.checkOutAction !is Resource.Loading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (ui.checkOutAction is Resource.Loading) {
                                    CircularProgressIndicator(Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                }
                                Text("Check-out")
                            }
                            if (!enableCheckIn) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Không thể check-in lúc này. Hãy đảm bảo đã đến ngày nhận phòng và hóa đơn đã thanh toán.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                // Trạng thái hiếm: không loading, không error nhưng data vẫn null → coi như loading
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
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
