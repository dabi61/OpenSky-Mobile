package com.dabi.opensky.feature.ownerhotel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHotelEditScreen(
    hotelId: String,                         // ✅ chỉ truyền hotelId
    vm: OwnerHotelEditViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Lần đầu: fetch dữ liệu cũ từ repo theo hotelId
    LaunchedEffect(hotelId) { vm.loadByHotelId(hotelId) }

    // Chọn nhiều ảnh mới
    val addImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> vm.addNewImages(ctx.contentResolver, ctx.cacheDir ?: File(ctx.filesDir, "cache"), uris) }

    // Snackbar khi có message
    LaunchedEffect(state.lastMessage) {
        state.lastMessage?.let {
            scope.launch { snackbar.showSnackbar(it) }
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa khách sạn") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.Close, null) } },
                actions = {
                    TextButton(
                        onClick = { vm.save() },
                        enabled = !state.isLoading && state.hotelId.isNotBlank()
                    ) { Text("Lưu") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (state.isLoading && state.hotelId.isBlank()) {
            // Đang tải lần đầu (chưa có dữ liệu)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ====== Thông tin cơ bản ======
            OutlinedTextField(
                value = state.hotelName,
                onValueChange = { vm.onFieldChange(name = it) },
                label = { Text("Tên khách sạn") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = { vm.onFieldChange(description = it) },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.address,
                onValueChange = { vm.onFieldChange(address = it) },
                label = { Text("Địa chỉ") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.province,
                onValueChange = { vm.onFieldChange(province = it) },
                label = { Text("Tỉnh/Thành") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.latitude,
                    onValueChange = { vm.onFieldChange(latitude = it) },
                    label = { Text("Vĩ độ") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.longitude,
                    onValueChange = { vm.onFieldChange(longitude = it) },
                    label = { Text("Kinh độ") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // ====== Ảnh hiện có ======
            Text("Ảnh hiện có", style = MaterialTheme.typography.titleSmall)
            when {
                state.existingImages.isEmpty() && !state.isLoading -> {
                    AssistChip(onClick = {}, label = { Text("Không có ảnh cũ") }, enabled = false)
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 220.dp)
                    ) {
                        items(state.existingImages) { img ->
                            val id = (img.imageId ?: -1L).toInt()
                            ExistingImageItem(
                                url = img.imageUrl.orEmpty(),
                                markedDelete = id in state.deleteImageIds,
                                onToggle = { if (id >= 0) vm.toggleDeleteImage(id) }
                            )
                        }
                    }
                }
            }

            // ====== Ảnh mới ======
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Ảnh mới", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { addImagesLauncher.launch("image/*") }, enabled = !state.isLoading) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm ảnh")
                }
            }
            if (state.newImageUris.isEmpty()) {
                AssistChip(onClick = {}, label = { Text("Chưa chọn ảnh mới") }, enabled = false)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 220.dp)
                ) {
                    itemsIndexed(state.newImageUris) { index, uri ->
                        NewImageItem(uri = uri, onRemove = { vm.removeNewImageAt(index) })
                    }
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ExistingImageItem(
    url: String,
    markedDelete: Boolean,
    onToggle: () -> Unit
) {
    val ctx = LocalContext.current
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(Color(0xFFF7F7F7), shape = MaterialTheme.shapes.medium)
            .clickable { onToggle() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(ctx).data(url).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Surface(
            color = if (markedDelete) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        ) {
            Icon(
                imageVector = if (markedDelete) Icons.Filled.Close else Icons.Filled.Check,
                contentDescription = null,
                tint = if (markedDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

@Composable
private fun NewImageItem(
    uri: android.net.Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(Color(0xFFF7F7F7), shape = MaterialTheme.shapes.medium)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .clickable { onRemove() }
        ) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(6.dp))
        }
    }
}
