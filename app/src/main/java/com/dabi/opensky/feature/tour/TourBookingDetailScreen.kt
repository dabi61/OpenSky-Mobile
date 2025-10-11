// feature/tourdetail/TourBookingDetailScreen.kt
package com.dabi.opensky.feature.tour

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.booking.TourBookingDetail
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourBookingDetailScreen(
    bookingId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TourBookingDetailViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    LaunchedEffect(bookingId) { viewModel.load(bookingId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết tour") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val state = ui.data) {
                is Resource.Loading -> CenterLoading()
                is Resource.Error -> CenterError(state.cause.message ?: "Không thể tải", onRetry = { viewModel.load(bookingId) })
                is Resource.Success -> Content(detail = state.data)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun Content(detail: TourBookingDetail) {
    val local = ZoneId.systemDefault()
    val sd = runCatching { Instant.parse(detail.startDate).atZone(local).toLocalDate() }.getOrNull()?.toString() ?: detail.startDate
    val ed = runCatching { Instant.parse(detail.endDate).atZone(local).toLocalDate() }.getOrNull()?.toString() ?: detail.endDate

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(detail.tourName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AssistChip(onClick = {}, label = { Text("Trạng thái: ${detail.status}") })
            detail.paymentStatus?.takeIf { it.isNotBlank() }?.let {
                AssistChip(onClick = {}, label = { Text("Thanh toán: $it") })
            }
        }

        Text("Bắt đầu: $sd")
        Text("Kết thúc: $ed")
        detail.numberOfGuests?.let { Text("Số khách: $it") }
        Spacer(Modifier.height(8.dp))

        // Thông tin tour
        ElevatedCard {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Thông tin tour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Tên: ${detail.tourInfo.tourName}")
                detail.tourInfo.location?.let { Text("Địa điểm: $it") }
                detail.tourInfo.duration?.let { Text("Quy mô: $it") }
                detail.tourInfo.price?.let { Text("Giá tham khảo: ${it.toVnd()}") }
                detail.tourInfo.description?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        detail.notes?.takeIf { it.isNotBlank() }?.let {
            OutlinedCard {
                Column(Modifier.padding(12.dp)) {
                    Text("Ghi chú", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(it)
                }
            }
        }
    }
}

@Composable private fun CenterLoading() =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

@Composable private fun CenterError(message: String, onRetry: () -> Unit) =
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Không thể tải", style = MaterialTheme.typography.titleMedium)
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp)); Button(onClick = onRetry) { Text("Thử lại") }
    }

private fun Long.toVnd(): String = "%,d VND".format(this).replace(',', '.')
