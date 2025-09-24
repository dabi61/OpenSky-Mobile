package com.dabi.opensky.feature.home

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dabi.opensky.core.model.Hotel
import com.dabi.opensky.core.navigation.currentComposeNavigator
import com.dabi.opensky.feature.hotel.HotelAction
import com.dabi.opensky.feature.hotel.HotelViewModel
import com.dabi.opensky.feature.session.SessionViewModel
import com.ronalksp.bottomnavigationuidesignliquid.ui.screens.Circle
import com.ronalksp.bottomnavigationuidesignliquid.ui.screens.CustomBottomNavigation
import com.ronalksp.bottomnavigationuidesignliquid.ui.screens.FabGroup
import com.ronalksp.bottomnavigationuidesignliquid.ui.screens.getRenderEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onHotelClick: (String) -> Unit = {},
    hotelViewModel: HotelViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel()
) {

    val uiState by hotelViewModel.state.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf(uiState.searchQuery) }

    // ====== FAB Menu state & animations (copy từ MainScreen) ======
    val isMenuExtended = remember { mutableStateOf(false) }

    val fabAnimationProgress by animateFloatAsState(
        targetValue = if (isMenuExtended.value) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "fabProgress"
    )

    val clickAnimationProgress by animateFloatAsState(
        targetValue = if (isMenuExtended.value) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "clickProgress"
    )

    val renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getRenderEffect().asComposeRenderEffect()
    } else {
        null
    }
    // ===============================================================

    Scaffold { paddingValues ->
        // Dùng Box để chồng nội dung + bottom nav + FAB group + ripple
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 24.dp)
        ) {
            // ======= CONTENT (bị blur khi mở menu) =======
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header + Search
                item {
                    Column {
                        Text(
                            text = "Chào mừng đến với OpenSky",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = search,
                            onValueChange = {
                                search = it
                                hotelViewModel.onSearchTextChanged(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Tìm khách sạn, thành phố, địa danh...") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { hotelViewModel.dispatch(HotelAction.Refresh) },
                                label = { Text("Làm mới") }
                            )
                            TextButton(onClick = { sessionViewModel.logout() }) {
                                Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Featured Hotels
                if (uiState.featuredHotels.isNotEmpty()) {
                    item {
                        Text(
                            text = "Khách sạn nổi bật",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(uiState.featuredHotels) { hotel ->
                                FeaturedHotelCard(
                                    hotel = hotel,
                                    onClick = { onHotelClick(hotel.hotelID) }
                                )
                            }
                        }
                    }
                }

                // All Hotels
                item {
                    Text(
                        text = "Tất cả khách sạn",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (uiState.isLoading && uiState.hotels.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }

                items(uiState.hotels) { hotel ->
                    HotelCard(hotel = hotel, onClick = { onHotelClick(hotel.hotelID) })
                }

                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                    }
                }

                uiState.errorMessage?.let { error ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { hotelViewModel.clearError() }) { Text("Đóng") }
                            }
                        }
                    }
                }
            }
            // ============================================

            // ======= BOTTOM NAV (giữ API của bạn) =======
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                CustomBottomNavigation()
            }

            // ======= OVERLAYS (Circle + FAB Groups) =======
            // Vòng tròn nền mờ (màu theo theme)
//            Circle(
//                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
//                animationProgress = 0.5f
//            )
            if (isMenuExtended.value) {
                Box(
                    Modifier
                        .fillMaxSize()
                        // .background(Color.Black.copy(alpha = 0.10f)) // nếu muốn mờ nhẹ
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                isMenuExtended.value = false
                            })
                        }
                )
            }

            // Nhóm FAB phía dưới: lớp có blur nhẹ (nếu bạn muốn)
            FabGroup(
                renderEffect = renderEffect,
                animationProgress = fabAnimationProgress
            )

            // Lớp trên: nhận click toggle mở/đóng
            FabGroup(
                renderEffect = null,
                animationProgress = fabAnimationProgress,
                toggleAnimation = { isMenuExtended.value = !isMenuExtended.value }
            )

            // Ripple trắng khi click (400ms)
//            Circle(
//                color = Color.White,
//                animationProgress = clickAnimationProgress
//            )
            // =============================================
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedHotelCard(
    hotel: Hotel,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(280.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = hotel.displayImage,
                contentDescription = hotel.hotelName,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = hotel.hotelName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = hotel.province,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(hotel.star) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(
                        text = "từ ${String.format("%.0f", hotel.displayMinPrice)}đ",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HotelCard(
    hotel: Hotel,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = hotel.displayImage,
                contentDescription = hotel.hotelName,
                modifier = Modifier.size(80.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp)) {
                Text(
                    text = hotel.hotelName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${hotel.address}, ${hotel.province}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(hotel.star) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = " • ${hotel.displayAvailableRooms}/${hotel.displayTotalRooms} phòng",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${String.format("%.0f", hotel.displayMinPrice)}đ - ${String.format("%.0f", hotel.displayMaxPrice)}đ",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
