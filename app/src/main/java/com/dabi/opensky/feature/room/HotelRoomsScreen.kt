// feature/rooms/HotelRoomsScreen.kt
package com.dabi.opensky.feature.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import coil.compose.AsyncImage
import com.dabi.opensky.core.model.room.Room
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelRoomsScreen(
    hotelId: String,
    onBack: () -> Unit,
    onRoomClick: (Room) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HotelRoomsViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()

    LaunchedEffect(hotelId) { viewModel.start(hotelId, pageSize = 10) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách phòng") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()

        // auto load next when reach near end
        LaunchedEffect(listState, ui.items.size, ui.endReached, ui.isLoading) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collectLatest { lastIdx ->
                    val total = ui.items.size
                    if (lastIdx != null && lastIdx >= total - 3 && !ui.endReached && !ui.isLoading) {
                        viewModel.loadNextPage()
                    }
                }
        }

        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                ui.isLoading && ui.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ui.error != null && ui.items.isEmpty() -> {
                    ErrorState(message = ui.error ?: "Đã xảy ra lỗi", onRetry = { viewModel.retry() })
                }
                ui.isEmpty -> {
                    EmptyRoomsState(onRetry = { viewModel.retry() })
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(ui.items, key = { _, r -> r.id }) { _, room ->
                            RoomCard(room = room, onClick = {
                                onRoomClick(room)
                            })
                        }
                        item {
                            if (ui.isLoading) {
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) { CircularProgressIndicator() }
                            } else if (ui.endReached && ui.items.isNotEmpty()) {
                                Text(
                                    "Đã tải hết ${ui.items.size} phòng",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
private fun EmptyRoomsState(onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hiện chưa có phòng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("Vui lòng quay lại sau hoặc thử làm mới.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Làm mới") }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Không thể tải phòng", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Thử lại") }
    }
}

@Composable
private fun RoomCard(room: Room, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp)) {
            AsyncImage(
                model = room.imageUrl,
                contentDescription = room.name,
                modifier = Modifier
                    .size(width = 110.dp, height = 80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(room.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(status = room.status)
                    Spacer(Modifier.width(8.dp))
                    CapacityChip(capacity = room.capacity)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = room.price.toVnd(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable private fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "available" -> Color(0xFF2E7D32)
        "unavailable", "booked", "occupied" -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.outline
    }
    AssistChip(onClick = {}, label = { Text(status) },
        leadingIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(color)) })
}

@Composable private fun CapacityChip(capacity: Int) {
    AssistChip(onClick = {}, label = { Text("Tối đa $capacity người") })
}

private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')
