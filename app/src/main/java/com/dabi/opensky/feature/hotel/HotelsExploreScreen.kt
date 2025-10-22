package com.dabi.opensky.feature.hotel

import android.util.Range
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dabi.opensky.R
import com.dabi.opensky.core.model.hotel.Hotel
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelsExploreScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onHotelClick: (String) -> Unit,
    hotelViewModel: HotelViewModel = hiltViewModel(),
) {
    val state by hotelViewModel.state.collectAsStateWithLifecycle()
    var showFilter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hotelViewModel.dispatch(HotelAction.LoadProvinces)
        hotelViewModel.dispatch(HotelAction.LoadAll)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khám phá khách sạn") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilter = true }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Bộ lọc")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading && state.hotels.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.errorMessage != null -> {
                    ErrorCard(
                        message = state.errorMessage!!,
                        onRetry = { hotelViewModel.dispatch(HotelAction.LoadAll) }
                    )
                }
                state.hotels.isEmpty() -> { // ⬅️ EMPTY STATE
                    EmptyState(
                        title = "Không tìm thấy kết quả",
                        subtitle = "Thử thay đổi bộ lọc hoặc đặt lại tất cả để xem nhiều khách sạn hơn.",
                        primary = "Đặt lại bộ lọc",
                        onPrimary = { hotelViewModel.dispatch(HotelAction.LoadAll) }
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.hotels) { h ->
                            HotelPromoCard(
                                hotel = h,
                                onClick = { onHotelClick(h.hotelID) }
                            )
                        }
                    }
                }
            }

            if (showFilter) {
                FilterBottomSheet(
                    provinces = state.provinces,
                    selectedProvince = state.selectedProvince,
                    selectedStars = state.selectedStars,
                    onDismiss = { showFilter = false },
                    onReloadProvinces = { hotelViewModel.dispatch(HotelAction.LoadProvinces) },
                    onClearAll = {
                        showFilter = false
                        hotelViewModel.dispatch(
                            HotelAction.AdvancedSearch(keyword = null, province = null, stars = null)
                        )
                    },
                    onApply = { province, stars ->
                        showFilter = false
                        hotelViewModel.dispatch(
                            HotelAction.AdvancedSearch(keyword = null, province = province, stars = stars)
                        )
                    }
                )
            }
        }
    }
}
// UI helpers
@Composable
private fun EmptyState(
    title: String,
    subtitle: String,
    primary: String,
    onPrimary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onPrimary) { Text(primary) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    provinces: List<String>,
    selectedProvince: String?,
    selectedStars: Int?,
    onDismiss: () -> Unit,
    onReloadProvinces: () -> Unit,
    onClearAll: () -> Unit,
    onApply: (province: String?, stars: Int?) -> Unit
) {
    var province by remember { mutableStateOf(selectedProvince) }
    var stars by remember { mutableStateOf(selectedStars) }
    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text("Bộ lọc", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            // --- Province ---
            Text("Tỉnh/Thành", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))

            if (provinces.isEmpty()) {
                // Loading/empty/error cho provinces
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Đang tải danh sách tỉnh…", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onReloadProvinces) { Text("Tải lại") }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = province.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Chọn tỉnh/thành") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true) // ✅ không warning
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tất cả") },
                            onClick = { province = null; expanded = false }
                        )
                        provinces.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = { province = p; expanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Stars ---
            Text("Số sao", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = stars == null,
                    onClick = { stars = null },
                    label = { Text("Tất cả") }
                )
                (1..5).forEach { s ->
                    FilterChip(
                        selected = stars == s,
                        onClick = { stars = s },
                        label = { Text("$s★") }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onClearAll,
                    modifier = Modifier.weight(1f)
                ) { Text("Xoá lọc") }
                Button(
                    onClick = { onApply(province, stars) },
                    modifier = Modifier.weight(1f)
                ) { Text("Áp dụng") }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}


/* ---------------------- UI pieces ---------------------- */

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) { Text("Thử lại") }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HotelPromoCard(
    hotel: Hotel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = hotel.displayImage,
                    contentDescription = hotel.hotelName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                // badge địa điểm
                Surface(
                    color = colorResource(R.color.blue),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = hotel.province,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Column(Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(hotel.star.coerceIn(0, 5)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = hotel.hotelName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "${"%,.0f".format(hotel.displayMinPrice)} VND",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE53935), // đỏ nhẹ như ảnh
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}