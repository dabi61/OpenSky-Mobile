// feature/tour/ToursScreen.kt
package com.dabi.opensky.feature.tour

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import com.dabi.opensky.core.model.tour.Tour

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToursScreen(
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ToursViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    LaunchedEffect(Unit) { viewModel.start(size = 10) }

    val listState = rememberLazyListState()

    // Auto load next page khi gần cuối list
    LaunchedEffect(listState, ui.items.size, ui.endReached, ui.isLoading) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { last ->
                val total = ui.items.size
                if (last != null && last >= total - 3 && !ui.endReached && !ui.isLoading) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khám phá Tour") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { TextButton(onClick = { viewModel.refresh() }) { Text("Làm mới") } }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                ui.isLoading && ui.items.isEmpty() -> CenterLoading()
                ui.error != null && ui.items.isEmpty() -> CenterError(ui.error ?: "Lỗi", onRetry = viewModel::refresh)
                ui.isEmpty -> CenterEmpty(onRefresh = viewModel::refresh)
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(ui.items, key = { _, t -> t.id }) { _, tour ->
                            TourCardModern(tour = tour, onClick = { onOpenDetail(tour.id) })
                        }
                        item {
                            if (ui.isLoading && ui.items.isNotEmpty()) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator(Modifier.padding(vertical = 16.dp))
                                }
                            } else if (ui.endReached && ui.items.isNotEmpty()) {
                                Text(
                                    "Đã hiển thị hết ${ui.items.size} tour",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
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
private fun TourCardModern(tour: Tour, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.height(220.dp)) {
            AsyncImage(
                model = tour.imageUrl,
                contentDescription = tour.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay gradient
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xAA000000))
                        )
                    )
            )
            // Text content
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    tour.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(tour.address, style = MaterialTheme.typography.bodySmall, color = Color(0xFFEDEDED))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    AssistChip(onClick = {}, label = { Text(tour.price.toVnd()) }, enabled = false)
//                    Spacer(Modifier.width(8.dp))
//                    AssistChip(onClick = {}, label = { Text("Tối đa ${tour.maxPeople} người") }, enabled = false)
//                    Spacer(Modifier.width(8.dp))
                    val statusColor =
                        if (tour.status.equals("Active", true)) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(tour.status, color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable private fun CenterLoading() =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

@Composable private fun CenterEmpty(onRefresh: () -> Unit) =
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chưa có tour"); Spacer(Modifier.height(8.dp)); Button(onClick = onRefresh) { Text("Làm mới") }
    }

@Composable private fun CenterError(msg: String, onRetry: () -> Unit) =
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Không thể tải: $msg"); Spacer(Modifier.height(8.dp)); Button(onClick = onRetry) { Text("Thử lại") }
    }

private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')
