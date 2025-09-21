package com.dabi.opensky.feature.search

import com.dabi.opensky.feature.hotel.HotelViewModel
import com.dabi.opensky.feature.hotel.HotelAction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dabi.opensky.core.model.Hotel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onHotelClick: (String) -> Unit = {},
//    hotelViewModel: HotelViewModel = hiltViewModel()
) {

}
//    val uiState by hotelViewModel.uiState.collectAsStateWithLifecycle()
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    var searchQuery by remember { mutableStateOf("") }
//    var showFilters by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = modifier.fillMaxSize()
//    ) {
//        // Search Header
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            shadowElevation = 4.dp
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Text(
//                    text = "Tìm kiếm khách sạn",
//                    style = MaterialTheme.typography.headlineMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.padding(bottom = 16.dp)
//                )
//
//                // Search Bar
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    placeholder = { Text("Nhập tên khách sạn, địa điểm...") },
//                    leadingIcon = {
//                        Icon(Icons.Default.Search, contentDescription = "Search")
//                    },
//                    trailingIcon = {
//                        Row {
//                            if (searchQuery.isNotEmpty()) {
//                                IconButton(
//                                    onClick = {
//                                        searchQuery = ""
//                                        hotelViewModel.clearSearch()
//                                    }
//                                ) {
//                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
//                                }
//                            }
//                            IconButton(
//                                onClick = { showFilters = !showFilters }
//                            ) {
//                                Icon(
//                                    Icons.Default.List,
//                                    contentDescription = "Filters",
//                                    tint = if (showFilters) MaterialTheme.colorScheme.primary
//                                          else MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }
//                        }
//                    },
//                    keyboardOptions = KeyboardOptions(
//                        imeAction = ImeAction.Search
//                    ),
//                    keyboardActions = KeyboardActions(
//                        onSearch = {
//                            keyboardController?.hide()
//                            hotelViewModel.searchHotels(searchQuery)
//                        }
//                    ),
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                // Filters Section
//                if (showFilters) {
//                    FilterSection(
//                        onApplyFilters = { province, stars, minPrice, maxPrice ->
//                            hotelViewModel.advancedSearch(
//                                query = searchQuery.takeIf { it.isNotEmpty() },
//                                province = province,
//                                stars = stars,
//                                minPrice = minPrice,
//                                maxPrice = maxPrice
//                            )
//                        },
//                        modifier = Modifier.padding(top = 16.dp)
//                    )
//                }
//            }
//        }
//
//        // Search Results
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            if (uiState.isLoading && uiState.hotels.isEmpty()) {
//                item {
//                    Box(
//                        modifier = Modifier.fillMaxWidth().height(200.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//            } else if (uiState.hotels.isEmpty() && searchQuery.isNotEmpty()) {
//                item {
//                    EmptySearchResults(
//                        query = searchQuery,
//                        onClearSearch = {
//                            searchQuery = ""
//                            hotelViewModel.clearSearch()
//                        }
//                    )
//                }
//            } else {
//                // Results header
//                if (uiState.hotels.isNotEmpty()) {
//                    item {
//                        Text(
//                            text = "Tìm thấy ${uiState.totalCount} khách sạn",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//
//                items(uiState.hotels) { hotel ->
//                    SearchHotelCard(
//                        hotel = hotel,
//                        onClick = { onHotelClick(hotel.hotelID) }
//                    )
//                }
//
//                if (uiState.isLoadingMore) {
//                    item {
//                        Box(
//                            modifier = Modifier.fillMaxWidth().padding(16.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
//                        }
//                    }
//                }
//            }
//
//            uiState.errorMessage?.let { error ->
//                item {
//                    Card(
//                        colors = CardDefaults.cardColors(
//                            containerColor = MaterialTheme.colorScheme.errorContainer
//                        ),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text(
//                            text = error,
//                            color = MaterialTheme.colorScheme.onErrorContainer,
//                            modifier = Modifier.padding(16.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun FilterSection(
//    onApplyFilters: (String?, Int?, Double?, Double?) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var selectedProvince by remember { mutableStateOf("") }
//    var selectedStars by remember { mutableIntStateOf(0) }
//    var minPrice by remember { mutableStateOf("") }
//    var maxPrice by remember { mutableStateOf("") }
//
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text(
//                text = "Bộ lọc",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            // Province filter
//            OutlinedTextField(
//                value = selectedProvince,
//                onValueChange = { selectedProvince = it },
//                label = { Text("Tỉnh/Thành phố") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            // Star rating filter
//            Column {
//                Text(
//                    text = "Hạng sao",
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Medium
//                )
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(top = 8.dp)
//                ) {
//                    (0..5).forEach { stars ->
//                        FilterChip(
//                            onClick = { selectedStars = if (selectedStars == stars) 0 else stars },
//                            label = {
//                                Text(if (stars == 0) "Tất cả" else "$stars sao")
//                            },
//                            selected = selectedStars == stars
//                        )
//                    }
//                }
//            }
//
//            // Price range filter
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                OutlinedTextField(
//                    value = minPrice,
//                    onValueChange = { minPrice = it },
//                    label = { Text("Giá từ") },
//                    modifier = Modifier.weight(1f)
//                )
//                OutlinedTextField(
//                    value = maxPrice,
//                    onValueChange = { maxPrice = it },
//                    label = { Text("Giá đến") },
//                    modifier = Modifier.weight(1f)
//                )
//            }
//
//            // Apply filters button
//            Button(
//                onClick = {
//                    onApplyFilters(
//                        selectedProvince.takeIf { it.isNotEmpty() },
//                        selectedStars.takeIf { it > 0 },
//                        minPrice.toDoubleOrNull(),
//                        maxPrice.toDoubleOrNull()
//                    )
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Áp dụng bộ lọc")
//            }
//        }
//    }
//}
//
//@Composable
//private fun EmptySearchResults(
//    query: String,
//    onClearSearch: () -> Unit
//) {
//    Column(
//        modifier = Modifier.fillMaxWidth().padding(32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Icon(
//            Icons.Default.Search,
//            contentDescription = null,
//            modifier = Modifier.size(64.dp),
//            tint = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        Text(
//            text = "Không tìm thấy khách sạn",
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Medium,
//            modifier = Modifier.padding(top = 16.dp)
//        )
//
//        Text(
//            text = "Không có kết quả cho \"$query\"",
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            modifier = Modifier.padding(top = 8.dp)
//        )
//
//        TextButton(
//            onClick = onClearSearch,
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            Text("Xóa tìm kiếm")
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun SearchHotelCard(
//    hotel: Hotel,
//    onClick: () -> Unit
//) {
//    Card(
//        onClick = onClick,
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp)
//        ) {
//            AsyncImage(
//                model = hotel.images.firstOrNull(),
//                contentDescription = hotel.hotelName,
//                modifier = Modifier
//                    .size(100.dp),
//                contentScale = ContentScale.Crop
//            )
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 12.dp)
//            ) {
//                Text(
//                    text = hotel.hotelName,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                ) {
//                    Icon(
//                        Icons.Default.LocationOn,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = "${hotel.address}, ${hotel.province}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.padding(start = 4.dp),
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(vertical = 2.dp)
//                ) {
//                    repeat(hotel.star) {
//                        Icon(
//                            Icons.Default.Star,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp),
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "${hotel.availableRooms} phòng trống",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = if (hotel.availableRooms > 0)
//                            MaterialTheme.colorScheme.primary
//                        else MaterialTheme.colorScheme.error
//                    )
//                }
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = hotel.description.take(50) + if (hotel.description.length > 50) "..." else "",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis,
//                        modifier = Modifier.weight(1f)
//                    )
//
//                    Column(
//                        horizontalAlignment = Alignment.End
//                    ) {
//                        Text(
//                            text = "${String.format("%.0f", hotel.minPrice)}đ",
//                            style = MaterialTheme.typography.titleSmall,
//                            color = MaterialTheme.colorScheme.primary,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Text(
//                            text = "/ đêm",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
