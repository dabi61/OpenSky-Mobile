// feature/hotel/MyHotelsScreen.kt
package com.dabi.opensky.feature.hotel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.dabi.opensky.core.model.hotel.Hotel
import com.dabi.opensky.core.model.room.Room
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHotelsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    vm: MyHotelsViewModel = hiltViewModel(),
    onOpenRoomDetail: (String) -> Unit = {} // optional
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingRoom: Room? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khách sạn của tôi") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = vm::retryHotels) { Icon(Icons.Default.Refresh, contentDescription = "Tải lại") } }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoaderBox(padding)
            state.errorMessage != null -> ErrorBox(padding, state.errorMessage ?: "Lỗi", onRetry = vm::retryHotels)
            state.hotels.isEmpty() -> EmptyBox(padding)
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.hotels) { hotel ->
                        MyHotelCard(
                            hotel = hotel,
                            selected = state.selectedHotelId == hotel.hotelID,
                            onSelect = {
                                if (state.selectedHotelId != hotel.hotelID) vm.selectHotel(hotel.hotelID)
                            },
                            roomsArea = {
                                // Filter bar
                                StatusChips(
                                    selected = state.statusFilter ?: "All",
                                    onSelected = { sel -> vm.setRoomStatusFilter(sel.takeIf { it != "All" }) }
                                )
                                // Toolbar add room
                                Row(
                                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Phòng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    TextButton(onClick = { showCreateDialog = true }) {
                                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Thêm phòng")
                                    }
                                }
                                when {
                                    state.roomsLoading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                    state.roomsError != null -> Text(state.roomsError!!, color = MaterialTheme.colorScheme.error)
                                    state.rooms.isEmpty() -> Text("Chưa có phòng nào.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    else -> {
                                        state.rooms.forEach { r ->
                                            RoomRow(
                                                room = r,
                                                onOpen = { onOpenRoomDetail(hotel.hotelID) },
                                                onEdit = { editingRoom = r },
                                                onDelete = { vm.deleteRoom(r.id) },
                                                onChangeStatus = { newStatus -> vm.updateRoomStatus(r.id, newStatus) }
                                            )
                                            Divider()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- Dialogs ---
        if (showCreateDialog && state.selectedHotelId != null) {
            RoomEditorDialog(
                title = "Thêm phòng",
                initial = null,
                onDismiss = { showCreateDialog = false },
                onSubmit = { name, type, address, price, maxPeople, files ->
                    vm.createRoom(
                        hotelId = state.selectedHotelId!!,
                        roomName = name,
                        roomType = type,
                        address = address,
                        price = price,
                        maxPeople = maxPeople,
                        files = files
                    )
                    showCreateDialog = false
                }
            )
        }

        editingRoom?.let { room ->
            RoomEditorDialog(
                title = "Sửa phòng",
                initial = room,
                onDismiss = { editingRoom = null },
                onSubmit = { name, type, address, price, maxPeople, files ->
                    // Ở đây đơn giản: chưa làm existing/delete ids UI → gửi files mới + giữ nguyên các ảnh cũ (existingImageIdsCsv = null)
                    vm.updateRoom(
                        roomId = room.id,
                        roomName = name.takeIf { it.isNotBlank() },
                        roomType = type.takeIf { it.isNotBlank() },
                        address = address.takeIf { it.isNotBlank() },
                        price = price,
                        maxPeople = maxPeople,
                        existingImageIdsCsv = null,
                        deleteImageIdsCsv = null,
                        files = files
                    )
                    editingRoom = null
                }
            )
        }
    }
}

// ---------- Small parts ----------

@Composable private fun LoaderBox(padding: PaddingValues) =
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

@Composable private fun ErrorBox(padding: PaddingValues, msg: String, onRetry: () -> Unit) =
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(msg, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp)); Button(onClick = onRetry) { Text("Thử lại") }
        }
    }

@Composable private fun EmptyBox(padding: PaddingValues) =
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bạn chưa có khách sạn nào.", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Hãy tạo mới khách sạn để bắt đầu kinh doanh trên OpenSky.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyHotelCard(
    hotel: Hotel,
    selected: Boolean,
    onSelect: () -> Unit,
    roomsArea: @Composable ColumnScope.() -> Unit
) {
    Card(onClick = onSelect) {
        Column {
            AsyncImage(
                model = hotel.displayImage,
                contentDescription = hotel.hotelName,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(12.dp)) {
                Text(hotel.hotelName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(hotel.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(hotel.province) }, enabled = false)
                    Spacer(Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text("Sao: ${hotel.star}") }, enabled = false)
                    Spacer(Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text(hotel.status ?: "Active") }, enabled = false,
                        leadingIcon = { Surface(color = Color.Green, shape = CircleShape, modifier = Modifier.size(8.dp)) {} })
                }

                if (selected) {
                    Spacer(Modifier.height(12.dp))
                    roomsArea()
                } else {
                    Spacer(Modifier.height(6.dp))
                    AssistChip(onClick = onSelect, label = { Text("Quản lý phòng") })
                }
            }
        }
    }
}

@Composable
private fun StatusChips(selected: String, onSelected: (String) -> Unit) {
    val all = listOf("All", "Available", "Occupied", "Maintenance", "Removed")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        all.forEach { s ->
            FilterChip(
                selected = selected == s,
                onClick = { onSelected(s) },
                label = { Text(s) }
            )
        }
    }
}

@Composable
private fun RoomRow(
    room: Room,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangeStatus: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(room.name ?: "Phòng", fontWeight = FontWeight.SemiBold)
                Text(room.type ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!room.status.isNullOrBlank()) AssistChip(onClick = {}, enabled = false, label = { Text(room.status!!) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpen) { Icon(Icons.Default.Info, null) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
                MenuStatus(onChangeStatus)
//                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun MenuStatus(onSelect: (String) -> Unit) {
    var show by remember { mutableStateOf(false) }
    IconButton(onClick = { show = true }) { Icon(Icons.Default.Sync, contentDescription = "Đổi trạng thái") }
    DropdownMenu(expanded = show, onDismissRequest = { show = false }) {
        listOf("Available", "Occupied", "Maintenance", "Removed").forEach { s ->
            DropdownMenuItem(text = { Text(s) }, onClick = { show = false; onSelect(s) })
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomEditorDialog(
    title: String,
    initial: Room? = null,
    onDismiss: () -> Unit,
    onSubmit: (name: String, type: String, address: String, price: Double, maxPeople: Int, files: List<File>) -> Unit
) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var type by remember { mutableStateOf(initial?.type.orEmpty()) }
    var address by remember { mutableStateOf(initial?.address.orEmpty()) }
    var priceTxt by remember { mutableStateOf(initial?.price?.toString().orEmpty()) }
    var maxPeopleTxt by remember { mutableStateOf(initial?.capacity?.toString().orEmpty()) }
    var files by remember { mutableStateOf(emptyList<File>()) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val dir = ctx.cacheDir
        val fs = uris.mapNotNull { u ->
            try {
                val f = File(dir, "room_${System.currentTimeMillis()}.jpg")
                ctx.contentResolver.openInputStream(u)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } }
                f
            } catch (_: Throwable) { null }
        }
        files = files + fs
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Tên phòng") }, singleLine = true)
                OutlinedTextField(type, { type = it }, label = { Text("Loại phòng") }, singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("Địa chỉ") })
                OutlinedTextField(priceTxt, { priceTxt = it }, label = { Text("Giá") }, singleLine = true)
                OutlinedTextField(maxPeopleTxt, { maxPeopleTxt = it }, label = { Text("Số người tối đa") }, singleLine = true)
                TextButton(onClick = { picker.launch("image/*") }) { Icon(Icons.Default.AddPhotoAlternate, null); Spacer(Modifier.width(6.dp)); Text("Chọn ảnh") }
                if (files.isNotEmpty()) Text("Đã chọn ${files.size} ảnh")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = priceTxt.toDoubleOrNull() ?: 0.0
                val maxPeople = maxPeopleTxt.toIntOrNull() ?: 1
                onSubmit(name.trim(), type.trim(), address.trim(), price, maxPeople, files)
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } }
    )
}
