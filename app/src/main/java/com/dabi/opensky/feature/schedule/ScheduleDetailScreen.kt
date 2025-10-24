package com.dabi.opensky.feature.tour

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.schedule.ScheduleDetailResponse

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    scheduleId: String,
    onBack: () -> Unit,
    vm: ScheduleDetailViewModel = hiltViewModel()
) {
    val st by vm.detail.collectAsState()
    val bookingUi by vm.booking.collectAsState()

    // ✅ Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(scheduleId) { vm.load(scheduleId) }

    // ✅ Show snackbar khi thành công
    LaunchedEffect(bookingUi.success) {
        bookingUi.success?.let { ok ->
            snackbarHostState.showSnackbar(ok.message)
            vm.clearBookingMessage()
        }
    }
    // ✅ Show snackbar khi lỗi
    LaunchedEffect(bookingUi.error) {
        bookingUi.error?.let { e ->
            snackbarHostState.showSnackbar(e.message ?: "Tạo booking thất bại")
            vm.clearBookingMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết lịch") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // ✅ gắn host
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val res = st) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is Resource.Error -> Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(res.cause.message ?: "Lỗi tải chi tiết")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.load(scheduleId) }) { Text("Thử lại") }
                }
                is Resource.Success<ScheduleDetailResponse> -> ScheduleDetailContent(
                    d = res.data,
                    isBooking = bookingUi.isBooking,
                    onBook = { guests, notes -> vm.createBooking(scheduleId, guests, notes) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ScheduleDetailContent(
    d: ScheduleDetailResponse,
    isBooking: Boolean,
    onBook: (guests: Int, notes: String?) -> Unit
) {
    val start = parseIsoToZdt(d.startTime)
    val end = parseIsoToZdt(d.endTime)

    var guests by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }

    val max = d.tour?.maxPeople ?: 0
// Backend trả numberPeople = SỐ CHỖ CÒN TRỐNG
    val remain = d.numberPeople.coerceIn(0, max)
    val booked = (max - remain).coerceAtLeast(0)

    if (guests > remain) guests = remain.coerceAtLeast(0)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val img = d.tour?.imageUrl
        if (!img.isNullOrBlank()) {
            AsyncImage(
                model = img,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentScale = ContentScale.Crop
            )
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(d.tourName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${fmtDate(start)} ${fmtTime(start)} → ${fmtDate(end)} ${fmtTime(end)}  •  ${durationLabel(d.startTime, d.endTime)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = { if (max > 0) booked.toFloat() / max else 0f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Đã đăng ký: $booked/$max • Còn trống: $remain",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            d.user?.let { u ->
                Text("Người đặt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(u.fullName ?: u.email.orEmpty())
                u.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                    Text("SĐT: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            d.tour?.let { t ->
                Spacer(Modifier.height(8.dp))
                Text("Thông tin tour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                t.description?.takeIf { it.isNotBlank() }?.let { Text(it, color = MaterialTheme.colorScheme.onSurface) }
                Text("Giá: ${t.price.toVnd()}", fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(12.dp))
            Text("Đặt lịch này", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = guests.toString(),
                    onValueChange = { v -> guests = v.toIntOrNull()?.coerceIn(0, remain.coerceAtMost(99)) ?: 0 },
                    label = { Text("Số khách") },
                    singleLine = true,
                    modifier = Modifier.width(140.dp),
                    supportingText = { if (remain <= 0) Text("Hết chỗ") }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú (tuỳ chọn)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { /* share/copy */ }, modifier = Modifier.weight(1f)) {
                    Text("Chia sẻ")
                }
                Button(
                    onClick = { onBook(guests, notes.ifBlank { null }) },
                    enabled = !isBooking && guests > 0 && remain > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isBooking) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Đặt ngay")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')
