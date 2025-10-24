// feature/voucher/MyVouchersScreen.kt
package com.dabi.opensky.feature.voucher

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dabi.opensky.core.model.voucher.UserVoucherItem
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Màu chủ đạo xanh biển của app (có thể map với theme của bạn)
private val OceanBlue = Color(0xFF1077E6)
private val OceanBlueDark = Color(0xFF0B58AA)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVouchersScreen(
    onBack: () -> Unit,
    onUseVoucher: (UserVoucherItem) -> Unit = {}, // hook khi bấm "Dùng ngay"
    viewModel: MyVouchersViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    LaunchedEffect(Unit) { viewModel.start(10) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voucher của tôi") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    TextButton(onClick = { viewModel.refresh() }) { Text("Làm mới") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                ui.isLoading && ui.items.isEmpty() -> CenterLoading()
                ui.error != null && ui.items.isEmpty() -> CenterError(ui.error!!, onRetry = viewModel::refresh)
                ui.isEmpty -> CenterEmpty()
                else -> {
                    val listState = rememberLazyListState()
                    LaunchedEffect(listState, ui.items.size, ui.endReached, ui.isLoading) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .collectLatest { last ->
                                val total = ui.items.size
                                if (last != null && last >= total - 3 && !ui.endReached && !ui.isLoading) {
                                    viewModel.loadNextPage()
                                }
                            }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(ui.items, key = { _, it -> it.userVoucherID }) { _, voucher ->
                            VoucherTicketCard(
                                item = voucher,
                                onUse = { onUseVoucher(voucher) }
                            )
                        }
                        item {
                            if (ui.isLoading) Row(
                                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) { CircularProgressIndicator() }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun VoucherTicketCard(
    item: UserVoucherItem,
    onUse: () -> Unit
) {
    val expired = item.voucherIsExpired
    val used = item.isUsed
    val disabled = expired || used

    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // Header gradient
        Box(
            Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(
                    Brush.horizontalGradient(listOf(OceanBlue, OceanBlueDark))
                ),
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // % lớn ở trái
                Column(Modifier.weight(1f)) {
                    Text("${item.voucherPercent}%", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    Text(item.voucherTableType, color = Color.White.copy(alpha = .9f))
                }
                // Code ở phải
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(item.voucherCode, color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f))
                )
            }
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Mô tả
            if (!item.voucherDescription.isNullOrBlank()) {
                Text(item.voucherDescription!!, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
            }

            // Thời gian hiệu lực
            Row {
                Text(
                    "Hiệu lực: ${item.voucherStartDate.toLocalDateStr()}  •  Hết hạn: ${item.voucherEndDate.toLocalDateStr()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(10.dp))

            // Trạng thái & hành động
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusText = when {
                    used -> "Đã dùng"
                    expired -> "Hết hạn"
                    else -> "Sẵn sàng"
                }
                val statusColor = when {
                    used -> Color(0xFF4F4848)
                    expired -> Color(0xFFC62828)
                    else -> Color(0xFF2E7D32)
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(statusText, color = Color.Black) },
                    leadingIcon = {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                    }
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onUse,
                    enabled = !disabled
                ) { Text("Dùng ngay") }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable private fun CenterLoading() =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

@Composable private fun CenterEmpty() =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chưa có voucher nào") }

@Composable private fun CenterError(message: String, onRetry: () -> Unit) =
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Không thể tải voucher", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Thử lại") }
    }

// -------- Utils
@RequiresApi(Build.VERSION_CODES.O)
private fun String?.toLocalDateStr(): String {
    if (this.isNullOrBlank()) return "—"
    return try {
        val zone = ZoneId.systemDefault()
        val d = Instant.parse(this).atZone(zone).toLocalDate()
        d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: Exception) {
        this
    }
}
