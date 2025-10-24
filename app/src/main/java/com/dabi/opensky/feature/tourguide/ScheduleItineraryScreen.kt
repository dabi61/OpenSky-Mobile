package com.dabi.opensky.feature.tourguide

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.tourguide.TourItinerary
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleItineraryScreen(
    scheduleId: String,
    tourId: String,
    title: String,
    onBack: () -> Unit,
    vm: ScheduleItineraryViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(scheduleId, tourId) { vm.init(scheduleId, tourId) }

    LaunchedEffect(state.actMessage) {
        state.actMessage?.let { snackbarHostState.showSnackbar(it); vm.clearToast() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it.message ?: "Có lỗi xảy ra"); vm.clearToast() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hành trình • $title") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { TextButton(onClick = { vm.refresh() }) { Text("Làm mới") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {

            when (val listRes = state.list) {
                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Error -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(listRes.cause.message ?: "Không thể tải")
                        Spacer(Modifier.height(8.dp))
//                        Button(onClick = { vm.load(tourId) }) { Text("Thử lại") }
                    }
                }

                is Resource.Success -> {
                    val items = listRes.data
                        .sortedWith(compareBy<TourItinerary> { it.dayNumber }.thenBy { it.createdAt })

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items, key = { it.itineraryID }) { base ->
                            val rt = state.rt[base.itineraryID]
                            ItineraryCard(
                                base = base,
                                startTime = rt?.startTime,
                                endTime = rt?.endTime,
                                isActing = state.isActing,
                                onStart = { vm.start(base.itineraryID, nowIso()) },
                                onEnd   = {
                                    val scheduleItID = rt?.scheduleItID
                                    if (scheduleItID.isNullOrBlank()) {
                                        vm.notify("Bạn cần bấm ‘Bắt đầu’ trước khi ‘Rời đi’.")
                                    } else {
                                        vm.endByScheduleItID(scheduleItID)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItineraryCard(
    base: TourItinerary,
    startTime: String?,
    endTime: String?,
    isActing: Boolean,
    onStart: () -> Unit,
    onEnd: () -> Unit
) {
    val status = when {
        startTime == null -> "Chưa bắt đầu"
        endTime == null   -> "Đang tham quan"
        else              -> "Đã rời đi"
    }
    var confirmEnd by remember { mutableStateOf(false) }

    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ngày ${base.dayNumber} • ${base.location}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = {}, enabled = false, label = { Text(status) })
            }
            base.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Bắt đầu: ${startTime ?: "--"}")
            Text("Kết thúc: ${endTime ?: "--"}")

            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (startTime == null) {
                    Button(onClick = onStart, enabled = !isActing, modifier = Modifier.weight(1f)) { Text("Bắt đầu") }
                } else if (endTime == null) {
                    OutlinedButton(onClick = { /* trạng thái */ }, enabled = false, modifier = Modifier.weight(1f)) { Text("Đã đến nơi") }
                    Button(onClick = { confirmEnd = true }, enabled = !isActing, modifier = Modifier.weight(1f)) { Text("Rời đi") }
                } else {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("Hoàn tất điểm dừng")
                    }
                }
            }
        }
    }

    if (confirmEnd) {
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            confirmButton = { TextButton(onClick = { confirmEnd = false; onEnd() }) { Text("Xác nhận") } },
            dismissButton = { TextButton(onClick = { confirmEnd = false }) { Text("Huỷ") } },
            title = { Text("Kết thúc điểm dừng") },
            text = { Text("Bạn chắc chắn muốn cập nhật thời gian rời đi cho điểm này?") }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun nowIso(): String =
    ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
