package com.dabi.opensky.feature.profile

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dabi.opensky.core.designsystem.component.CustomBottomNavigation
import com.dabi.opensky.core.designsystem.component.FabGroup
import com.dabi.opensky.core.designsystem.component.getRenderEffect
import com.dabi.opensky.feature.session.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    // Mock data (giữ nguyên)
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

    // ====== FAB menu state & animations (đồng bộ với HomeScreen) ======
    val isMenuExtended = remember { mutableStateOf(false) }

    val fabAnimationProgress by animateFloatAsState(
        targetValue = if (isMenuExtended.value) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "fabProgress"
    )

    val renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getRenderEffect().asComposeRenderEffect()
    } else null

    // Back để đóng menu
    BackHandler(enabled = isMenuExtended.value) {
        isMenuExtended.value = false
    }
    // ===============================================================

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 24.dp) // chừa chỗ cho bottom nav
        ) {
            // ===== CONTENT =====
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
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
                            Box(contentAlignment = Alignment.Center) {
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
                        onClick = { /* TODO */ }
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Refresh,
                        title = "Lịch sử đặt phòng",
                        subtitle = "Xem các đặt phòng đã thực hiện",
                        onClick = { /* TODO */ }
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.ShoppingCart,
                        title = "Phương thức thanh toán",
                        subtitle = "Quản lý thẻ và ví điện tử",
                        onClick = { /* TODO */ }
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Thông báo",
                        subtitle = "Cài đặt thông báo và email",
                        onClick = { /* TODO */ }
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
                        onClick = { /* TODO */ }
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "Về OpenSky",
                        subtitle = "Thông tin ứng dụng",
                        onClick = { /* TODO */ }
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Đăng xuất",
                        subtitle = "Thoát khỏi tài khoản",
                        onClick = { sessionViewModel.logout() },
                        isDestructive = true
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            // ===================

            // ===== Bottom Navigation =====
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                CustomBottomNavigation()
            }

            // ===== Overlay: tap bất kỳ đâu để đóng FAB menu =====
            if (isMenuExtended.value) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { isMenuExtended.value = false })
                        }
                )
            }

            // ===== FAB groups (giống HomeScreen) =====
            FabGroup(
                renderEffect = renderEffect,
                animationProgress = fabAnimationProgress
            )

            FabGroup(
                renderEffect = null,
                animationProgress = fabAnimationProgress,
                toggleAnimation = { isMenuExtended.value = !isMenuExtended.value }
            )
        }
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
