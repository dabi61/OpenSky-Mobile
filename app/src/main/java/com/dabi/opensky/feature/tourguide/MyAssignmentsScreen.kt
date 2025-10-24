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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.tourguide.AssignedSchedule

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAssignmentsScreen(
    onBack: () -> Unit,
    onOpenScheduleItinerary: (scheduleId: String, tourId: String, title: String) -> Unit,
    vm: MyAssignmentsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load(page = 1, size = 10) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch được giao") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = { TextButton(onClick = { vm.load() }) { Text("Làm mới") } }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {

            when (val st = ui) {
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
                        Text(st.cause.message ?: "Không thể tải danh sách")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.load() }) { Text("Thử lại") }
                    }
                }

                is Resource.Success -> {
                    val items = st.data.schedules
                    if (items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Chưa có lịch được giao")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(items, key = { it.scheduleID }) { sch ->
                                AssignmentCard(
                                    s = sch,
                                    onOpen = {
                                        onOpenScheduleItinerary(
                                            sch.scheduleID,
                                            sch.tourID,
                                            sch.tourName
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun AssignmentCard(
    s: AssignedSchedule,
    onOpen: () -> Unit
) {
    // 🔧 Chuẩn hoá số khách
    val max = s.tour?.maxPeople ?: 0
    val remain = s.numberPeople.coerceIn(0, max)       // API trả còn trống
    val booked = (max - remain).coerceAtLeast(0)
    val occupancy = if (max > 0) booked.toFloat() / max else 0f

    ElevatedCard(onClick = onOpen) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(s.tourName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = {}, enabled = false, label = { Text(s.status) })
            }

            s.tour?.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // ✅ Hiển thị đúng số khách
            Text("Đã đăng ký: $booked/$max • Còn trống: $remain", color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Progress đơn giản
            LinearProgressIndicator(
                progress = { occupancy },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Bắt đầu: ${s.startTime}")
            Text("Kết thúc: ${s.endTime}")

            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text("Xem hành trình") }
        }
    }
}
