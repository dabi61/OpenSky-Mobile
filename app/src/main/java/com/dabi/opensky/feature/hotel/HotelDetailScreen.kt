package com.dabi.opensky.feature.hotel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.hotel.Hotel
import com.dabi.opensky.core.model.User
import com.dabi.opensky.R
import com.dabi.opensky.core.navigation.currentComposeNavigator

/* ---------- Demo UI models cho phần đánh giá ---------- */
data class HotelReview(
    val userName: String,
    val avatar: String?,
    val rating: Int,
    val comment: String,
    val photos: List<String>
)

/* ---------- ENTRY SCREEN ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    hotelId: String,
    onBackClick: () -> Unit,
    onSeeRooms: (Hotel) -> Unit = {},
    onSeeAllReviews: (Hotel) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HotelDetailViewModel = hiltViewModel()
) {
    val hotelState by viewModel.hotelDetail.collectAsStateWithLifecycle()
    val navigator = currentComposeNavigator

    LaunchedEffect(hotelId) { viewModel.loadHotelDetail(hotelId) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết khách sạn") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(0)
            )
        },
        bottomBar = {
            if (hotelState is Resource.Success) {
                val hotel = (hotelState as Resource.Success<Hotel>).data
                HotelPriceBar(
                    price = hotel.minPrice ?: hotel.maxPrice ?: 0.0,
                    onSeeRooms = { onSeeRooms(hotel) }
                )
            }
        }
    ) { paddingValues ->
        when (val state = hotelState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is Resource.Error -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Warning, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Không thể tải thông tin khách sạn",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        state.cause.message ?: "Lỗi không xác định",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Button(
                        onClick = { viewModel.loadHotelDetail(hotelId) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) { Text("Thử lại") }
                }
            }

            is Resource.Success -> {
                HotelDetailContent(
                    hotel = state.data,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    onSeeAllReviews = { onSeeAllReviews(state.data) }
                )
            }
        }
    }
}

/* ---------- MAIN CONTENT ---------- */
@Composable
private fun HotelDetailContent(
    hotel: Hotel,
    onSeeAllReviews: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Demo reviews (map từ API thực của bạn)
    val demoReviews = remember(hotel.hotelID) {
        listOf(
            HotelReview(
                userName = "Hoàng Quang",
                avatar = hotel.user?.avatarURL,
                rating = 5,
                comment = "Team hướng dẫn nhiệt tình, độc lạ; HDV dễ thương, chăm lo ❤",
                photos = hotel.images.take(4).ifEmpty { listOfNotNull(hotel.displayImage).take(4) }
            ),
            HotelReview(
                userName = "Hoàng Quang",
                avatar = hotel.user?.avatarURL,
                rating = 5,
                comment = "Team hướng dẫn siêu nhiệt tình, độc-lạ; HDV dễ thương, chu đáo.",
                photos = hotel.images.takeLast(4).ifEmpty { listOfNotNull(hotel.displayImage).take(4) }
            )
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp), // chừa chỗ cho bottom bar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /* Header image */
        item {
            val images = buildList {
                val main = hotel.displayImage
                if (!main.isNullOrBlank()) add(main)
                addAll(hotel.images.filter { it.isNotBlank() && it != main })
            }.distinct()

            HeaderImageCarousel(images = images)
        }

        /* Title + stars + address */
        item {
            HotelMainInfo(hotel)
        }

        /* About (Về chúng tôi) */
        hotel.description?.let { desc ->
            item {
                HotelAboutSection(description = desc)
            }
        }

        /* Owner */
        hotel.user?.let { owner ->
            item { OwnerInfoCard(owner = owner) }
        }

        /* Reviews block */
        item {
            ReviewsHeader(onSeeAll = onSeeAllReviews)
        }
        items(demoReviews) { review ->
            ReviewCard(review = review)
        }

        /* Location (text) */
        if (hotel.latitude != null && hotel.longitude != null) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Vị trí",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tọa độ: ${hotel.latitude}, ${hotel.longitude}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // TODO: nhúng MapView nếu cần
                    }
                }
            }
        }

        /* Room info */
        if (hotel.totalRooms != null || hotel.availableRooms != null || (hotel.minPrice != null && hotel.maxPrice != null)) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Thông tin phòng",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        hotel.totalRooms?.let {
                            Text("Tổng số phòng: $it", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        hotel.availableRooms?.let {
                            Text(
                                "Phòng trống: $it",
                                color = if (it > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                        if (hotel.minPrice != null && hotel.maxPrice != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Giá: ${hotel.minPrice.toVnd()} - ${hotel.maxPrice.toVnd()} / đêm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}


@Composable
private fun HotelMainInfo(hotel: Hotel) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = hotel.hotelName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(maxOf(hotel.star, 0)) {
                Icon(
                    Icons.Default.Star, null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(18.dp)
                )
            }
            if (hotel.star > 0) {
                Text(
                    text = "  ${hotel.star} / 5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn, null,
                tint = Color.Black, modifier = Modifier.size(18.dp)
            )
            Text(
                text = buildString {
                    append(hotel.address)
                    if (!hotel.province.isNullOrBlank()) append(", ${hotel.province}")
                    append(", Việt Nam")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.padding(start = 6.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HotelAboutSection(description: String) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Về chúng tôi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun HeaderImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier,
    indicatorAlignment: Alignment = Alignment.BottomCenter
) {
    val safeImages = if (images.isEmpty()) listOf("") else images
    val pagerState = rememberPagerState(pageCount = { safeImages.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp)
//            .clip(RoundedCornerShape(16.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
        ) { page ->
            val url = safeImages[page]
            if (url.isNotBlank()) {
                AsyncImage(
                    model = url,
                    contentDescription = "Hotel image ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // fallback rỗng
                Box(
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }

        // shadow gradient nhẹ dưới đáy để nổi chỉ số/indicator
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.20f)
                    )
                )
        )

        // indicator + counter
        Row(
            modifier = Modifier
                .align(indicatorAlignment)
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.25f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DotsIndicator(
                totalDots = safeImages.size,
                selectedIndex = pagerState.currentPage
            )
//            Spacer(Modifier.width(10.dp))
//            Text(
//                text = "${pagerState.currentPage + 1}/${safeImages.size}",
//                style = MaterialTheme.typography.labelMedium,
//                color = Color.White
//            )
        }
    }
}

@Composable
private fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = colorResource(R.color.blue),
    inactiveColor: Color = Color.White.copy(alpha = 0.6f)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalDots) { index ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (isSelected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor)
            )
        }
    }
}

@Composable
private fun ReviewsHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Đánh giá của khách hàng",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onSeeAll) {
            Text("Xem thêm", color = Color.Black)
        }
    }
}

@Composable
private fun ReviewCard(review: HotelReview) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (review.avatar != null) {
                    AsyncImage(
                        model = review.avatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.size(36.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, null) }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(review.userName, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(review.rating) {
                            Icon(
                                Icons.Default.Star, null,
                                tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp)
                            )
                        }
                        if (review.rating < 5) repeat(5 - review.rating) {
                            Icon(
                                Icons.Default.Star, null,
                                tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp)
                            )
                        }
                        Text("  ${review.rating} / 5", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (review.photos.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(review.photos) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(width = 84.dp, height = 64.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerInfoCard(
    owner: User,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Thông tin chủ khách sạn",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                owner.avatarURL?.let { avatarUrl ->
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Owner Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(owner.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(owner.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    owner.phoneNumber?.let {
                        Text("SĐT: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "Vai trò: ${owner.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/* ---------- Bottom price bar ---------- */
@Composable
private fun HotelPriceBar(
    price: Double,
    onSeeRooms: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Giá chỉ từ", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                Text(
                    text = price.toVnd(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Button(
                onClick = onSeeRooms,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors().copy(containerColor = colorResource(R.color.blue)),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
            ) { Text("Xem phòng") }
        }
    }
}

/* ---------- Utils ---------- */
private fun Double.toVnd(): String {
    return "%,d VND".format(kotlin.math.round(this).toLong()).replace(',', '.')
}
