// feature/roomdetail/RoomDetailScreen.kt
package com.dabi.opensky.feature.roomdetail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.availability.AvailabilityResponse
import com.dabi.opensky.core.model.room.RoomDetailResponse
import com.dabi.opensky.feature.room.RoomDetailViewModel // <- đổi import nếu VM ở package khác
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomDetailViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    val snack = remember { SnackbarHostState() }

    // State picker ngày
    var showPickIn by remember { mutableStateOf(false) }
    var showPickOut by remember { mutableStateOf(false) }

    LaunchedEffect(roomId) { viewModel.load(roomId) }

    // Snackbar cho availability
    LaunchedEffect(ui.availability) {
        when (val a = ui.availability) {
            is Resource.Error -> snack.showSnackbar(a.cause.message ?: "Không kiểm tra được tình trạng")
            is Resource.Success -> if (!a.data.isAvailable) {
                snack.showSnackbar(a.data.message ?: "Phòng không khả dụng trong khoảng đã chọn")
            }
            else -> Unit
        }
    }
    // Snackbar cho booking
    LaunchedEffect(ui.booking) {
        when (val b = ui.booking) {
            is Resource.Error -> snack.showSnackbar(b.cause.message ?: "Đặt phòng thất bại")
            is Resource.Success -> snack.showSnackbar("Đặt phòng thành công!")
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết phòng") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val d = ui.detail) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is Resource.Error -> ErrorState(
                    message = d.cause.message ?: "Không thể tải dữ liệu",
                    onRetry = { viewModel.load(roomId) }
                )
                is Resource.Success -> {
                    val detail = d.data
                    RoomDetailContent(
                        detail = detail,
                        checkIn = ui.checkInIso,
                        checkOut = ui.checkOutIso,
                        onPickIn = { showPickIn = true },
                        onPickOut = { showPickOut = true },

                        // NEW: truyền xuống UI
                        availability = ui.availability,
                        onCheckAvailability = { viewModel.checkAvailability() },

                        // NEW: chỉ đặt khi available
                        onBook = { viewModel.bookIfAvailable() },
                        booking = ui.booking
                    )
                }
            }
        }

        // Date pickers
        if (showPickIn) {
            DatePickerDialog(
                onDismissRequest = { showPickIn = false },
                confirmButton = {
                    TextButton(onClick = { showPickIn = false }) { Text("Đóng") }
                }
            ) {
                val state = rememberDatePickerState()
                DatePicker(state = state)
                LaunchedEffect(state.selectedDateMillis) {
                    state.selectedDateMillis?.let { millis ->
                        viewModel.setCheckIn(millis.toUtcIsoStartOfDay()) // gửi UTC 'Z'
                    }
                }
            }
        }
        if (showPickOut) {
            DatePickerDialog(
                onDismissRequest = { showPickOut = false },
                confirmButton = {
                    TextButton(onClick = { showPickOut = false }) { Text("Đóng") }
                }
            ) {
                val state = rememberDatePickerState()
                DatePicker(state = state)
                LaunchedEffect(state.selectedDateMillis) {
                    state.selectedDateMillis?.let { millis ->
                        viewModel.setCheckOut(millis.toUtcIsoStartOfDay()) // gửi UTC 'Z'
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RoomDetailContent(
    detail: RoomDetailResponse,
    checkIn: String?,
    checkOut: String?,
    onPickIn: () -> Unit,
    onPickOut: () -> Unit,

    // NEW
    availability: Resource<*>?,
    onCheckAvailability: () -> Unit,

    onBook: () -> Unit,
    booking: Resource<*>?
) {
    val images = detail.images.map { it.imageUrl }.filter { it.isNotBlank() }
    val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })

    Column(Modifier.fillMaxSize()) {
        // Header carousel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
            ) { page ->
                val url = images.getOrNull(page)
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1f to Color.Black.copy(0.18f)
                        )
                    )
            )
            // Dots
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(images.size.coerceAtLeast(1)) { idx ->
                    val sel = idx == pagerState.currentPage
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (sel) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (sel) MaterialTheme.colorScheme.primary
                                else Color.White.copy(0.6f)
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Info
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                detail.roomName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(detail.roomType) })
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("Tối đa ${detail.maxPeople} người") })
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text(detail.status) })
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = detail.price.toVnd(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(detail.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(16.dp))

        // Booking panel
        Card(
            modifier = Modifier.padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Đặt phòng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onPickIn, modifier = Modifier.weight(1f)) {
                        Text(checkIn?.toShortDateOrSelf() ?: "Chọn ngày nhận phòng")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onPickOut, modifier = Modifier.weight(1f)) {
                        Text(checkOut?.toShortDateOrSelf() ?: "Chọn ngày trả phòng")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // --- Nút kiểm tra tình trạng ---
                val enableCheck = !checkIn.isNullOrBlank() && !checkOut.isNullOrBlank()
                when (availability) {
                    is Resource.Loading -> {
                        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text("Đang kiểm tra...")
                        }
                    }
                    is Resource.Success -> {
                        // Nếu available true -> show nhãn & bật Đặt phòng
                        val ok = (availability as Resource.Success<*>).data.let {
                            @Suppress("UNCHECKED_CAST")
                            (it as? AvailabilityResponse)?.isAvailable == true
                        }
                        if (ok) {
                            Text(
                                "Phòng khả dụng cho khoảng đã chọn.",
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            // Hiển thị tạm tính nếu có
                            val av = (availability as Resource.Success<*>).data as? AvailabilityResponse
                            av?.numberOfNights?.let { Text("Số đêm: $it") }
                            av?.totalPrice?.let { Text("Tạm tính: ${it.toVnd()}", fontWeight = FontWeight.SemiBold) }

                            Spacer(Modifier.height(12.dp))
                            // --- Nút Đặt phòng ---
                            when (booking) {
                                is Resource.Loading -> {
                                    Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                                        CircularProgressIndicator(Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp)); Text("Đang đặt...")
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = onBook,
                                        enabled = true,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Đặt phòng") }
                                }
                            }
                        } else {
                            // Không khả dụng: đã có snackbar, cho phép bấm kiểm tra lại
                            OutlinedButton(
                                onClick = onCheckAvailability,
                                enabled = enableCheck,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Kiểm tra lại tình trạng") }
                        }
                    }
                    else -> {
                        Button(
                            onClick = onCheckAvailability,
                            enabled = enableCheck,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Kiểm tra tình trạng") }
                    }
                }

                // Trạng thái booking lỗi/thành công cũng đã Snackbar ở trên;
                // có thể hiển thị phụ ở đây nếu muốn.
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Có lỗi xảy ra", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Thử lại") }
    }
}

private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')

@RequiresApi(Build.VERSION_CODES.O)
private fun String.toShortDateOrSelf(): String = try {
    val odt = java.time.OffsetDateTime.parse(this)
    odt.toLocalDate().toString()
} catch (_: Exception) { this }

@RequiresApi(Build.VERSION_CODES.O)
private fun Long.toUtcIsoString(): String {
    val zone = ZoneId.systemDefault()
    val localDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
    val startOfDayLocal = localDate.atStartOfDay(zone)
    return startOfDayLocal.toInstant().toString() // ISO_INSTANT, có 'Z'
}
@RequiresApi(Build.VERSION_CODES.O)
private fun Long.toUtcIsoStartOfDay(): String {
    // selectedDateMillis của Material DatePicker vốn dĩ là 00:00 UTC của ngày đã chọn.
    // Vì vậy: convert trực tiếp về LocalDate theo UTC, rồi gắn 00:00 UTC.
    val dateUtc = java.time.Instant.ofEpochMilli(this)
        .atZone(java.time.ZoneOffset.UTC)
        .toLocalDate()
    return dateUtc
        .atStartOfDay(java.time.ZoneOffset.UTC)
        .toInstant()
        .toString() // dạng ISO_INSTANT, có 'Z' và là 00:00Z
}