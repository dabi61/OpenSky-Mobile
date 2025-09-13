package com.dabi.opensky.feature.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dabi.opensky.core.model.Hotel

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    onHotelClick: (String) -> Unit = {}
) {
    // TODO: Implement favorites logic with local storage or user preferences
    var favoriteHotels by remember { mutableStateOf(emptyList<Hotel>()) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Text(
                text = "Khách sạn yêu thích",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (favoriteHotels.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = "Chưa có khách sạn yêu thích",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    Text(
                        text = "Thêm khách sạn vào danh sách yêu thích để xem ở đây",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Button(
                        onClick = { /* Navigate to search */ },
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text("Khám phá khách sạn")
                    }
                }
            }
        } else {
            // Favorites list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteHotels) { hotel ->
                    FavoriteHotelCard(
                        hotel = hotel,
                        onClick = { onHotelClick(hotel.hotelID) },
                        onRemoveFavorite = {
                            // TODO: Implement remove from favorites
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteHotelCard(
    hotel: Hotel,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = hotel.images.firstOrNull(),
                    contentDescription = hotel.hotelName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                
                // Favorite button overlay
                IconButton(
                    onClick = onRemoveFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Remove from favorites",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = hotel.hotelName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${hotel.address}, ${hotel.province}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp),
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
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = " • ${hotel.availableRooms} phòng trống",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "từ ${String.format("%.0f", hotel.minPrice)}đ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/ đêm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (hotel.description.isNotEmpty()) {
                    Text(
                        text = hotel.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
