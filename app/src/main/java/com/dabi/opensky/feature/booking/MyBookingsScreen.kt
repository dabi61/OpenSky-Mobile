// feature/checkin/MyBookingsScreen.kt
package com.dabi.opensky.feature.booking

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                ui.isLoading && ui.hotelItems.isEmpty() && ui.tourItems.isEmpty() ->
                    CenterLoading()

                ui.error != null && ui.hotelItems.isEmpty() && ui.tourItems.isEmpty() ->
                    CenterError(ui.error ?: "Lỗi", onRetry = viewModel::refresh)

                ui.isEmpty -> CenterEmpty()

                else -> {
                    Column {
                        FilterTabs(
                            selected = ui.tab,
                            hotelCount = ui.hotelItems.size,
                            tourCount  = ui.tourItems.size,
                            onSelect = viewModel::switchTab
                        )

                        if (ui.tab == BookingTab.HOTEL) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(ui.hotelItems, key = { _, it -> it.bookingID }) { _, item ->
                                    BookingCard(item, onClick = { onOpenBookingDetail(item.bookingID) })
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
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(ui.tourItems, key = { it.bookingID }) { item ->
                                    TourBookingCard(item, onOpen = { onOpenTourBookingDetail(item.bookingID) })
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
            TextButton(onClick = onClick) { Text("Xem chi tiết / Check-in") }
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
