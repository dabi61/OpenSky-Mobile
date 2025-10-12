package com.dabi.opensky.feature.home

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dabi.opensky.core.model.hotel.Hotel
import com.dabi.opensky.feature.hotel.HotelViewModel
import com.dabi.opensky.feature.session.SessionViewModel
import com.dabi.opensky.core.designsystem.component.CustomBottomNavigation
import com.dabi.opensky.core.designsystem.component.FabGroup
import com.dabi.opensky.core.designsystem.component.getRenderEffect
import com.dabi.opensky.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onHotelClick: (String) -> Unit = {},
    hotelViewModel: HotelViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel
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

    val renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getRenderEffect().asComposeRenderEffect()
    } else {
        null
    }

//    val topColor = Color(0xFF1DA1F2) // màu bạn đang dùng cho header
//    val systemUi = rememberSystemUiController()

    // set màu thanh status bar theo màn hình Home
//    DisposableEffect(topColor) {
//        systemUi.setStatusBarColor(topColor, darkIcons = false) // icon trắng
//        onDispose {
//            // tùy ý: khôi phục khi rời màn
//            systemUi.setStatusBarColor(Color.Transparent, darkIcons = true)
//        }
//    }
    // ============ HomeScreen: CONTENT mới theo ảnh ============
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 24.dp)
        ) {
            // CONTENT
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Top blue header + search
                item {
                    TopSearchBar(
                        value = search,
                        onChange = {
                            search = it
                            hotelViewModel.onSearchTextChanged(it)
                        },
                        topColor = colorResource(R.color.blue),   // 💙 màu top bar bạn muốn
                        radius = 28.dp,                 // 🔵 bo tròn hơn
                        placeholderColor = Color.Gray,
                        iconTint = Color.White,
                        fieldBg = Color.White
                    )
                }

                // Categories
                item {
                    CategoryRow(
                        modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
                    )
                }

                // Ưu đãi
                item {
                    Text(
                        text = "Ưu đãi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                item {
                    PromoCard(
                        code = "SUNWORLDDANANG",
                        title = "Tặng vé tham quan Sun World",
                        subtitle = "Cho khách hàng đi tour Đà Nẵng",
                        onSave = { /* TODO: handle save */ },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Khuyến mãi cho bạn
                item {
                    Text(
                        text = "Khuyến mãi cho bạn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                // Grid 2 cột từ danh sách hotels
                if (uiState.isLoading && uiState.hotels.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                } else {
                    items(uiState.hotels.chunked(2)) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.getOrNull(0)?.let { h ->
                                HotelPromoCard(
                                    hotel = h,
                                    onClick = { onHotelClick(h.hotelID) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            row.getOrNull(1)?.let { h ->
                                HotelPromoCard(
                                    hotel = h,
                                    onClick = { onHotelClick(h.hotelID) },
                                    modifier = Modifier.weight(1f)
                                )
                            } ?: Spacer(Modifier.weight(1f))
                        }
                    }
                }

                // error + load more (giữ nguyên logic)
                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                    }
                }
                uiState.errorMessage?.let { error ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { hotelViewModel.clearError() }) { Text("Đóng") }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) } // chừa bottom nav
            }

            // ===== Bottom nav & FAB (giữ nguyên của bạn) =====
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                CustomBottomNavigation()
            }

            if (isMenuExtended.value) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                isMenuExtended.value = false
                            })
                        }
                )
            }

            FabGroup(renderEffect = renderEffect, animationProgress = fabAnimationProgress)
            FabGroup(
                renderEffect = null,
                animationProgress = fabAnimationProgress,
                toggleAnimation = { isMenuExtended.value = !isMenuExtended.value }
            )
        }
    }
}
//    Các composable phụ(đặt dưới file)
    @Composable
    private fun TopSearchBar(
    value: String,
    onChange: (String) -> Unit,
    // 🔧 tham số tuỳ biến
    topColor: Color = colorResource(id = R.color.blue),      // màu nền thanh trên
    radius: Dp = 24.dp,                                      // độ bo ô search
    placeholderColor: Color = Color(0xFF8AAFD6),
    iconTint: Color = Color.White,
    fieldBg: Color = Color.White
    ) {
    val shape: Shape = RoundedCornerShape(radius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(topColor)
            .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            placeholder = { Text("Nhập nội dung tìm kiếm…", color = placeholderColor) },
            singleLine = true,
            shape = shape,                       // ✅ bo tròn theo tham số
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                disabledContainerColor = fieldBg,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = topColor
            ),
            trailingIcon = {
                // nút icon trong suốt/bo tròn nhẹ cho hợp ảnh mẫu
                Surface(
                    color = topColor.copy(alpha = 0.25f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun CategoryRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CategoryItem(
            title = "Khách sạn",
            bg = Color(0xFFFFF4E6),
            drawableRes = R.drawable.img_1,
            modifier = Modifier.weight(1f)   // ✅ weight ở RowScope
        )
        CategoryItem(
            title = "Tour",
            bg = Color(0xFFEFF7FF),
            drawableRes = R.drawable.img_2,
            modifier = Modifier.weight(1f)
        )
        CategoryItem(
            title = "Hoạt động",
            bg = Color(0xFFFFF0F3),
            drawableRes = R.drawable.img_3,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CategoryItem(
    title: String,
    bg: Color,
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier   // ✅ dùng modifier truyền vào
            .height(164.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(bg, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = drawableRes,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF5C6A79)
            )
        }
    }
}

    @Composable
    private fun PromoCard(
        code: String,
        title: String,
        subtitle: String,
        onSave: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFEAF4FF),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = colorResource(R.color.blue)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1f).wrapContentHeight(),
                            singleLine = true
                        )
                        Button(
                            onClick = onSave,
                            modifier = Modifier.height(40.dp)
                        ) { Text("Lưu") }
                    }
                }
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