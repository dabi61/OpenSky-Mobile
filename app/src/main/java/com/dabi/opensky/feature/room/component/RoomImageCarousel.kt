package com.dabi.opensky.feature.room.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.dabi.opensky.R

@Composable
fun RoomImageCarousel(
    images: List<String>,
    modifier: Modifier,
    indicatorAlignment: Alignment = Alignment.BottomCenter
) {
    val safe = if (images.isEmpty()) listOf("") else images
    val pagerState = rememberPagerState(pageCount = { safe.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            flingBehavior = PagerDefaults.flingBehavior(pagerState)
        ) { page ->
            val url = safe[page]
            if (url.isNotBlank()) {
                HighQualityImage(
                    url = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.2f)
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(indicatorAlignment)
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(20))
                .background(Color.Black.copy(alpha = 0.28f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DotsIndicator(totalDots = safe.size, selectedIndex = pagerState.currentPage)
        }
    }
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = colorResource(
        R.color.blue
    ),
    inactiveColor: Color = Color.White.copy(alpha = 0.6f)
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(totalDots) { index ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (selected) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (selected) activeColor else inactiveColor)
            )
        }
    }
}

@Composable
fun HighQualityImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val context = LocalContext.current
    val req = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .allowHardware(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    AsyncImage(
        model = req,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

