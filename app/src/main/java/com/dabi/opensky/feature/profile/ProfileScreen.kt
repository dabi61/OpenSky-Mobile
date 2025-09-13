package com.dabi.opensky.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dabi.opensky.feature.session.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    // TODO: Get user data from repository/session
    val mockUser = remember {
        MockUser(
            name = "Nguyễn Văn A",
            email = "user@opensky.com",
            phone = "+84 123 456 789",
            memberSince = "Tháng 9, 2024",
            totalBookings = 5,
            favoriteHotels = 12
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with user info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Text(
                    text = mockUser.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Text(
                    text = mockUser.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Text(
                    text = "Thành viên từ ${mockUser.memberSince}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Đặt phòng",
                value = mockUser.totalBookings.toString(),
                icon = Icons.Default.Home,
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                title = "Yêu thích",
                value = mockUser.favoriteHotels.toString(),
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f)
            )
        }

        // Menu Items
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Tài khoản",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "Thông tin cá nhân",
                subtitle = "Cập nhật thông tin của bạn",
                onClick = { /* TODO: Navigate to edit profile */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Refresh,
                title = "Lịch sử đặt phòng",
                subtitle = "Xem các đặt phòng đã thực hiện",
                onClick = { /* TODO: Navigate to booking history */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.ShoppingCart,
                title = "Phương thức thanh toán",
                subtitle = "Quản lý thẻ và ví điện tử",
                onClick = { /* TODO: Navigate to payment methods */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                title = "Thông báo",
                subtitle = "Cài đặt thông báo và email",
                onClick = { /* TODO: Navigate to notifications */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(
                text = "Khác",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                title = "Hỗ trợ",
                subtitle = "Liên hệ với chúng tôi",
                onClick = { /* TODO: Navigate to support */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "Về OpenSky",
                subtitle = "Thông tin ứng dụng",
                onClick = { /* TODO: Navigate to about */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Đăng xuất",
                subtitle = "Thoát khỏi tài khoản",
                onClick = { sessionViewModel.logout() },
                isDestructive = true
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else MaterialTheme.colorScheme.onSurface
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDestructive) 
                        MaterialTheme.colorScheme.error 
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDestructive) 
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.Create,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class MockUser(
    val name: String,
    val email: String,
    val phone: String,
    val memberSince: String,
    val totalBookings: Int,
    val favoriteHotels: Int
)
