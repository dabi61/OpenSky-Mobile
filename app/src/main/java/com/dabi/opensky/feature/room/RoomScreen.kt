package com.dabi.opensky.feature.room

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dabi.opensky.R
import com.dabi.opensky.core.model.room.Room
import com.dabi.opensky.core.navigation.currentComposeNavigator
import com.dabi.opensky.feature.room.component.HighQualityImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    hotelId: String,
    onBack: () -> Unit,
    onOpenRoom: (Room) -> Unit,
    modifier: Modifier = Modifier,
    vm: RoomViewModel = hiltViewModel()
) {
    LaunchedEffect(hotelId) { vm.load(hotelId) }

    val ui by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách phòng") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                windowInsets = WindowInsets(0)
            )
        }
    ) { pv ->
        when (ui) {
            UiResult.Loading -> Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiResult.Error -> Column(Modifier.fillMaxSize().padding(pv).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(8.dp))
                Text("Không thể tải danh sách phòng")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { vm.load(hotelId) }) { Text("Thử lại") }
            }
            is UiResult.Success -> {
                val s = (ui as UiResult.Success<RoomViewModel.RoomsUiState>).data
                LazyColumn(
                    modifier = modifier.padding(pv),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(s.items, key = { it.roomID }) { room ->
                        RoomCard(room = room, onClick = { onOpenRoom(room) })
                    }
                    item {
                        if (s.isLoadingMore) {
                            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else if (s.hasNext) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                OutlinedButton(
                                    onClick = { vm.loadMore(hotelId) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text("Tải thêm") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomCard(room: Room, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
// Image
            val first = room.images.firstOrNull()
            if (first != null) {
                HighQualityImage(
                    url = first,
                    contentDescription = room.roomName,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.padding(12.dp)) {
                Text(room.roomName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Menu, null, modifier = Modifier.size(18.dp))
                    Text(
                        text = " ${room.maxPeople ?: 2} khách • ${room.roomType ?: "Tiêu chuẩn"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = room.price.toVnd() + " / đêm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.blue)
                )
            }
        }
    }
}