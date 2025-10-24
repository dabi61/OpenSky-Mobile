package com.dabi.opensky.feature.tour

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.schedule.ScheduleItem
import com.dabi.opensky.core.model.tour.TourDetailResponse
import com.dabi.opensky.feature.booking.StatusChip
import com.dabi.opensky.feature.schedule.TourSchedulesViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourDetailScreen(
    tourId: String,
    onBack: () -> Unit,
    onBookTour: (TourDetailResponse) -> Unit = {},
    // ✅ NEW: callback mở ScheduleDetail
    onOpenScheduleDetail: (String) -> Unit = {},
    vm: TourDetailViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(tourId) { vm.load(tourId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết tour") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, null
                        )
                    }
                }
            )
        },
    ) { padding ->
        Box(Modifier
            .padding(padding)
            .fillMaxSize()) {
            when (val st = ui.detail) {
                is Resource.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is Resource.Error -> Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        st.cause.message ?: "Lỗi"
                    ); Spacer(Modifier.height(8.dp)); Button(onClick = { vm.load(tourId) }) { Text("Thử lại") }
                }

                is Resource.Success -> TourDetailContent(st.data, onScheduleClick = onOpenScheduleDetail)
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TourDetailContent(
    d: TourDetailResponse,
    schedulesVm: TourSchedulesViewModel = hiltViewModel(),
    onScheduleClick: (String) -> Unit = {}
) {
    LaunchedEffect(d.tourID) {
        schedulesVm.init(d.tourID, page = 1, size = 10)
    }
    val schState by schedulesVm.ui.collectAsState()
    val listState = rememberLazyListState()

    // 1 LazyColumn cho TOÀN MÀN HÌNH (ảnh + info + mô tả + lịch)
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            top = 12.dp,
            bottom = 96.dp,               // chừa chỗ cho BottomAppBar
            start = 16.dp, end = 16.dp
        )
    ) {
        // --- Ảnh (LazyRow) ---
        item(key = "images") {
            val images = d.images.mapNotNull { it.imageUrl }.ifEmpty {
                listOfNotNull(d.images.firstOrNull()?.imageUrl)
            }
            if (images.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(images) { url ->
                        Card(shape = RoundedCornerShape(16.dp)) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillParentMaxHeight()
                                    .aspectRatio(16 / 9f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        // --- Tiêu đề / địa chỉ / meta ---
        item(key = "meta") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(d.tourName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(d.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AssistChip(onClick = {}, label = { Text(d.province) }, enabled = false)
                Text("Tối đa: ${d.maxPeople} người")
            }
        }

        // --- Mô tả ---
        if (!d.description.isNullOrBlank()) {
            item(key = "desc_title") {
                Divider(); Text("Mô tả", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item(key = "desc_body") { Text(d.description!!) }
        }

        // --- Lịch khởi hành (tiêu đề) ---
        item(key = "schedule_title") {
            Spacer(Modifier.height(4.dp))
            Text("Lịch khởi hành", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        // --- Lịch: trạng thái loading/error rỗng ---
        when {
            schState.isLoading && schState.schedules.isEmpty() -> {
                item(key = "schedule_loading_first") {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            schState.error != null && schState.schedules.isEmpty() -> {
                item(key = "schedule_error_first") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(schState.error?.message ?: "Lỗi tải lịch")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { schedulesVm.retry() }) { Text("Thử lại") }
                    }
                }
            }
            else -> {
                // --- Các item lịch ---
                items(
                    count = schState.schedules.size,
                    key = { idx -> schState.schedules[idx].scheduleID }
                ) { idx ->
                    val item = schState.schedules[idx]
                    ScheduleCard(item = item, onClick = { onScheduleClick(item.scheduleID) })

                    // Trigger load-more khi tới cuối & còn trang
                    if (idx == schState.schedules.lastIndex && schState.page < schState.totalPages) {
                        // Dùng LaunchedEffect gắn key = page để không spam
                        LaunchedEffect(schState.page) { schedulesVm.loadNextPage() }
                        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                // --- Đã hết trang ---
                if (schState.page >= schState.totalPages && schState.schedules.isNotEmpty()) {
                    item(key = "schedule_end") {
                        Text(
                            "Đã hiển thị ${schState.schedules.size} lịch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleCard(
    item: ScheduleItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val start = parseIsoToZdt(item.startTime)
    val end = parseIsoToZdt(item.endTime)

    val max = item.tour?.maxPeople ?: 0
    // 🔧 API trả available seats -> remain
    val remain = item.numberPeople.coerceIn(0, max)
    val booked = (max - remain).coerceAtLeast(0)
    val occupancy = if (max > 0) booked.toFloat() / max else 0f

    ElevatedCard(
        onClick = { onClick?.invoke() },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.tourName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusChip(item.status)
            }

            Text(
                "${fmtDate(start)} ${fmtTime(start)} → ${fmtDate(end)} ${fmtTime(end)}  •  ${durationLabel(item.startTime, item.endTime)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Occupancy bar
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(occupancy)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Đã đăng ký: $booked/$max • Còn trống: $remain",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Người liên quan (nếu muốn giữ)
            item.user?.fullName?.let { name ->
                AssistChip(onClick = {}, enabled = false, label = { Text("Người đặt: $name") })
            }
        }
    }
}


private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')
