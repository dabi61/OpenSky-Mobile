// feature/checkin/MyBookingsScreen.kt
package com.dabi.opensky.feature.booking

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dabi.opensky.core.model.booking.HotelBookingSummary
import com.dabi.opensky.core.model.booking.TourBookingSummary
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    onOpenBookingDetail: (bookingId: String) -> Unit,
    onOpenTourBookingDetail: (bookingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyBookingsViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()

    LaunchedEffect(Unit) { viewModel.start(limit = 10) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt phòng của tôi") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { TextButton(onClick = { viewModel.refresh() }) { Text("Làm mới") } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                ui.isLoading && ui.hotelItems.isEmpty() && ui.tourItems.isEmpty() -> CenterLoading()
                ui.error != null && ui.hotelItems.isEmpty() && ui.tourItems.isEmpty() -> CenterError(ui.error ?: "Lỗi", onRetry = viewModel::refresh)
                ui.isEmpty -> CenterEmpty()
                else -> {
                    Column {
                        // Segmented tabs (gọn, hiện đại)
                        BookingSegmentedTabs(
                            selected = ui.tab,
                            hotelCount = ui.hotelItems.size,
                            tourCount = ui.tourItems.size,
                            onSelect = viewModel::switchTab
                        )

                        if (ui.tab == BookingTab.HOTEL) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(ui.hotelItems, key = { it.bookingID }) { item ->
                                    HotelBookingTicket(
                                        booking = item,
                                        onOpen = { onOpenBookingDetail(item.bookingID) }
                                    )
                                }
                                item {
                                    if (ui.isLoading) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                        CircularProgressIndicator(Modifier.padding(16.dp))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(ui.tourItems, key = { it.bookingID }) { item ->
                                    TourBookingTicket(
                                        booking = item,
                                        onOpen = { onOpenTourBookingDetail(item.bookingID) }
                                    )
                                }
                                item {
                                    if (ui.isLoading) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                        CircularProgressIndicator(Modifier.padding(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingSegmentedTabs(
    selected: BookingTab,
    hotelCount: Int,
    tourCount: Int,
    onSelect: (BookingTab) -> Unit
) {
    val index = if (selected == BookingTab.HOTEL) 0 else 1
    val titles = listOf("Khách sạn ($hotelCount)", "Tour ($tourCount)")
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        titles.forEachIndexed { i, title ->
            SegmentedButton(
                selected = i == index,
                onClick = { onSelect(if (i == 0) BookingTab.HOTEL else BookingTab.TOUR) },
                shape = when (i) {
                    0 -> SegmentedButtonDefaults.itemShape(index = 0, count = titles.size)
                    else -> SegmentedButtonDefaults.itemShape(index = 1, count = titles.size)
                }
            ) { Text(title) }
        }
    }
}


@Composable private fun FilterTabs(selected: BookingTab, hotelCount: Int, tourCount: Int, onSelect: (BookingTab) -> Unit) {
    TabRow(selectedTabIndex = if (selected == BookingTab.HOTEL) 0 else 1) {
        Tab(selected = selected == BookingTab.HOTEL, onClick = { onSelect(BookingTab.HOTEL) }, text = { Text("Khách sạn ($hotelCount)") })
        Tab(selected = selected == BookingTab.TOUR,  onClick = { onSelect(BookingTab.TOUR)  }, text = { Text("Tour ($tourCount)") })
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TourBookingCard(item: TourBookingSummary, onOpen: () -> Unit) {
    Card(onClick = onOpen) {
        Column(Modifier.padding(12.dp)) {
            Text(item.tourName.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            val local = ZoneId.systemDefault()
            val sd = runCatching { Instant.parse(item.startDate).atZone(local).toLocalDate() }.getOrNull()?.toString() ?: item.startDate
            val ed = runCatching { Instant.parse(item.endDate).atZone(local).toLocalDate() }.getOrNull()?.toString() ?: item.endDate
            Text("Bắt đầu: $sd  •  Kết thúc: $ed", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(onClick = {}, label = { Text("Trạng thái: ${item.status}") })
                if (!item.paymentStatus.isNullOrBlank()) {
                    AssistChip(onClick = {}, label = { Text("Thanh toán: ${item.paymentStatus}") })
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HotelBookingTicket(
    booking: HotelBookingSummary,
    onOpen: () -> Unit
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Header ảnh (hero)
            Box {
                // có thể thay ảnh khách sạn thật nếu bạn có URL trong booking
                // đây dùng màu nền + gradient nhẹ
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
//                        .background(
//                            Brush.verticalGradient(
//                                listOf(
//                                    OpenSkyBlue.copy(alpha = .80f),
//                                    OpenSkyBlue.copy(alpha = .50f)
//                                )
//                            )
//                        )
                )
                // Tiêu đề + chip trạng thái
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        booking.hotelName.ifBlank { "Khách sạn" },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(booking.status)
                        booking.paymentStatus?.takeIf { it.isNotBlank() }?.let { PayChip(it) }
                    }
                }
            }

            PerforationDivider()

            // Nội dung vé
            Column(Modifier.padding(14.dp)) {
                val zone = ZoneId.systemDefault()
                val ci = runCatching { Instant.parse(booking.checkInDate).atZone(zone).toLocalDate().toString() }
                    .getOrElse { booking.checkInDate }
                val co = runCatching { Instant.parse(booking.checkOutDate).atZone(zone).toLocalDate().toString() }
                    .getOrElse { booking.checkOutDate }

                InfoRow(label = "Nhận phòng", value = ci)
                Spacer(Modifier.height(6.dp))
                InfoRow(label = "Trả phòng", value = co)
                Spacer(Modifier.height(10.dp))

                TextButton(onClick = onOpen, modifier = Modifier.align(Alignment.End)) {
                    Text("Xem chi tiết")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TourBookingTicket(
    booking: TourBookingSummary,
    onOpen: () -> Unit
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
//                        .background(
//                            Brush.verticalGradient(
//                                listOf(
//                                    OpenSkyBlue.copy(alpha = .90f),
//                                    OpenSkyBlue.copy(alpha = .60f)
//                                )
//                            )
//                        )
                )
                Column(Modifier.padding(14.dp)) {
                    Text(
                        booking.tourName.ifBlank { "Tour" },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(booking.status)
                        booking.paymentStatus?.takeIf { it.isNotBlank() }?.let { PayChip(it) }
                    }
                }
            }

            PerforationDivider()

            Column(Modifier.padding(14.dp)) {
                val zone = ZoneId.systemDefault()
                val sd = runCatching { Instant.parse(booking.startDate).atZone(zone).toLocalDate().toString() }
                    .getOrElse { booking.startDate }
                val ed = runCatching { Instant.parse(booking.endDate).atZone(zone).toLocalDate().toString() }
                    .getOrElse { booking.endDate }

                InfoRow(label = "Bắt đầu", value = sd)
                Spacer(Modifier.height(6.dp))
                InfoRow(label = "Kết thúc", value = ed)

                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onOpen, modifier = Modifier.align(Alignment.End)) { Text("Xem chi tiết") }
            }
        }
    }
}

@Composable
fun StatusChip(text: String) {
    val base = when (text.lowercase()) {
        "pending" -> Color(0xFFFFA000)
        "paid", "completed", "success" -> Color(0xFF2E7D32)
        "canceled", "cancelled", "failed" -> Color(0xFFC62828)
        else -> Color.Black                             // <— dùng brand
    }
    AssistChip(
        onClick = {},
        label = { Text(text) },
        enabled = false,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White.copy(alpha = 0.12f),
            labelColor = Color.Black
        )
    )
}

@Composable
fun PayChip(status: String) {
    AssistChip(
        onClick = {},
        label = { Text("Thanh toán: $status") },
        enabled = false
    )
}

@Composable private fun PerforationDivider() {
    val dot = "•"
    val line = remember { (1..40).joinToString("") { dot } }
    Text(
        line,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable private fun BookingCard(item: HotelBookingSummary, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Column(Modifier.padding(12.dp)) {
            Text(item.hotelName.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            val local = ZoneId.systemDefault()
            val ci = runCatching { Instant.parse(item.checkInDate).atZone(local).toLocalDate() }.getOrNull()?.toString() ?: item.checkInDate
            val co = runCatching { Instant.parse(item.checkOutDate).atZone(local).toLocalDate() }.getOrNull()?.toString() ?: item.checkOutDate
            Text("Nhận phòng: $ci  •  Trả phòng: $co", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(onClick = {}, label = { Text("Trạng thái: ${item.status}") })
                if (!item.paymentStatus.isNullOrBlank()) {
                    AssistChip(onClick = {}, label = { Text("Thanh toán: ${item.paymentStatus}") })
                }
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onClick) { Text("Xem chi tiết") }
        }
    }
}

@Composable private fun CenterLoading() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
@Composable private fun CenterEmpty() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chưa có booking") }
@Composable private fun CenterError(message: String, onRetry: () -> Unit) =
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Không thể tải", style = MaterialTheme.typography.titleMedium)
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp)); Button(onClick = onRetry) { Text("Thử lại") }
    }

//val OpenSkyBlue = Color(0xFF0EA5E9) // ocean blue