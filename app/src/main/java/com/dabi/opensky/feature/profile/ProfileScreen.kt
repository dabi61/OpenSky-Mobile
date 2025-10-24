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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.size.Scale
import com.dabi.opensky.core.designsystem.component.CustomBottomNavigation
import com.dabi.opensky.core.designsystem.component.FabGroup
import com.dabi.opensky.core.designsystem.component.getRenderEffect
import com.dabi.opensky.feature.session.SessionViewModel
import com.dabi.opensky.R
import com.dabi.opensky.core.navigation.currentComposeNavigator

private enum class UserRole {
    Customer, Hotel, TourGuide, Admin, Supervisor, Unknown;

    companion object {
        fun from(raw: String?): UserRole = when (raw?.trim()?.lowercase()) {
            "customer" -> Customer
            "hotel" -> Hotel
            "tourguide", "tour_guide", "guide" -> TourGuide
            "admin" -> Admin
            "supervisor" -> Supervisor
            else -> Unknown
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onEditDetail: () -> Unit = {},
    onBookingDetail: () -> Unit = {},
    onLogout: () -> Unit = {},
    // NEW: các dest theo role (tùy chọn)
    onOpenMyBills: () -> Unit = {},
    onOpenMyVouchers: () -> Unit = {},
    onOpenMyHotel: () -> Unit = {},
    onOpenRefundRequests: () -> Unit = {},
    onOpenHotelStats: () -> Unit = {},
    onOpenTourSchedule: () -> Unit = {},
    onOpenAdminManagement: () -> Unit = {},
    onOpenAdminStats: () -> Unit = {}
) {
    val user by profileViewModel.user.collectAsStateWithLifecycle()
    val role = remember(user?.role) { UserRole.from(user?.role) }


    // Mock data (giữ nguyên)
//    val user = remember {
//        user(
//            name = "Nguyễn Văn A",
//            email = "user@opensky.com",
//            phone = "+84 123 456 789",
//            memberSince = "Tháng 9, 2024",
//            totalBookings = 5,
//            favoriteHotels = 12
//        )
//    }

    // ===== FAB giữ nguyên của bạn =====
    val isMenuExtended = remember { mutableStateOf(false) }
    val fabAnimationProgress by animateFloatAsState(
        targetValue = if (isMenuExtended.value) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "fabProgress"
    )
    val renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getRenderEffect().asComposeRenderEffect()
    } else null

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ===== Header =====
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar (FIXED null/blank)
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = Color(0xFFe2e8f0)
                        ) {
                            val avatarUrl = user?.avatarURL
                            if (avatarUrl.isNullOrBlank()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp),
                                        tint = Color(0xFF64748b)
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Text(
                            text = user?.fullName.orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = user?.email.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Role chip + member since
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        when (role) {
                                            UserRole.Customer -> "Khách hàng"
                                            UserRole.Hotel -> "Chủ khách sạn"
                                            UserRole.TourGuide -> "Hướng dẫn viên"
                                            UserRole.Admin -> "Quản trị viên"
                                            UserRole.Supervisor -> "Supervisor"
                                            UserRole.Unknown -> user?.role ?: "Vai trò"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        when (role) {
                                            UserRole.Customer -> Icons.Default.AccountCircle
                                            UserRole.Hotel -> Icons.Default.Hotel
                                            UserRole.TourGuide -> Icons.Default.DirectionsWalk
                                            UserRole.Admin, UserRole.Supervisor -> Icons.Default.AdminPanelSettings
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorResource(R.color.blue).copy(alpha = 0.12f)
                                )
                            )

                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Thành viên từ ${user?.createdAt.orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // ===== Menu theo ROLE =====
                val sections = remember(role) {
                    buildSectionsForRole(
                        role = role,
                        // routes/callbacks
                        onEditDetail = onEditDetail,
                        onBookingDetail = onBookingDetail,
                        onLogout = { sessionViewModel.logout(); onLogout() },
                        onOpenMyBills = onOpenMyBills,
                        onOpenMyVouchers = onOpenMyVouchers,
                        onOpenMyHotel = onOpenMyHotel,
                        onOpenRefundRequests = onOpenRefundRequests,
                        onOpenHotelStats = onOpenHotelStats,
                        onOpenTourSchedule = onOpenTourSchedule,
                        onOpenAdminManagement = onOpenAdminManagement,
                        onOpenAdminStats = onOpenAdminStats
                    )
                }

                sections.forEach { (sectionTitle, items) ->
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                        )
                        items.forEach { item ->
                            ProfileMenuItem(
                                icon = item.icon,
                                title = item.title,
                                subtitle = item.subtitle,
                                onClick = item.onClick,
                                isDestructive = item.isDestructive
                            )
                        }
                    }
                }

                Spacer(Modifier.height(100.dp))
            }

            // Bottom nav & FAB (giữ nguyên)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                CustomBottomNavigation()
            }
            if (isMenuExtended.value) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { isMenuExtended.value = false })
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

//            Icon(
//                Icons.Default.Create,
//                contentDescription = null,
//                modifier = Modifier.size(20.dp),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
        }
    }
}

private data class user(
    val name: String,
    val email: String,
    val phone: String,
    val memberSince: String,
    val totalBookings: Int,
    val favoriteHotels: Int
)

// ProfileScreen.kt (dưới ProfileScreen)
private data class Menu(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

/** Trả về list (SectionTitle -> các Menu) theo role */
private fun buildSectionsForRole(
    role: UserRole,
    onEditDetail: () -> Unit,
    onBookingDetail: () -> Unit,
    onLogout: () -> Unit,
    onOpenMyBills: () -> Unit,
    onOpenMyVouchers: () -> Unit,
    onOpenMyHotel: () -> Unit,
    onOpenRefundRequests: () -> Unit,
    onOpenHotelStats: () -> Unit,
    onOpenTourSchedule: () -> Unit,
    onOpenAdminManagement: () -> Unit,
    onOpenAdminStats: () -> Unit
): List<Pair<String, List<Menu>>> {

    val accountCommon = listOf(
        Menu(Icons.Default.Person, "Thông tin cá nhân", "Cập nhật thông tin của bạn", onEditDetail),
    )

    val supportAndAbout = listOf(
        Menu(Icons.Default.Notifications, "Hỗ trợ", "Liên hệ với chúng tôi", onClick = {}),
        Menu(Icons.Default.Info, "Về OpenSky", "Thông tin ứng dụng", onClick = {}) ,
        Menu(Icons.Default.ExitToApp, "Đăng xuất", "Thoát khỏi tài khoản", onLogout, isDestructive = true)
    )

    return when (role) {
        UserRole.Customer -> listOf(
            "Tài khoản" to (
                    accountCommon + listOf(
                        Menu(Icons.Default.History, "Lịch sử", "Xem các đặt phòng đã thực hiện", onBookingDetail),
                        Menu(Icons.Default.ReceiptLong, "Hóa đơn của tôi", "Thanh toán & hóa đơn", onOpenMyBills),
                        Menu(Icons.Default.CardGiftcard, "Voucher của tôi", "Mã giảm giá đã lưu", onOpenMyVouchers),
                        Menu(Icons.Default.Restore, "Yêu cầu hoàn tiền", "Yêu cầu hoàn tiền", onOpenRefundRequests),

                    )
                    ),
            "Khác" to supportAndAbout
        )

        UserRole.Hotel -> listOf(
            "Tài khoản" to (
                    accountCommon + listOf(
                        Menu(Icons.Default.History, "Lịch sử đặt phòng", "Xem đặt phòng tại khách sạn", onBookingDetail),
                    )
                    ),
            "Quản lý" to listOf(
                Menu(Icons.Default.Hotel, "Khách sạn của tôi", "Quản lý thông tin & phòng", onOpenMyHotel),
                Menu(Icons.Default.Restore, "Yêu cầu hoàn tiền", "Xử lý yêu cầu hoàn tiền", onOpenRefundRequests),
                Menu(Icons.Default.BarChart, "Thống kê", "Doanh thu, lượt đặt", onOpenHotelStats),
            ),
            "Khác" to supportAndAbout
        )

        UserRole.TourGuide -> listOf(
            "Tài khoản" to accountCommon,
            "Công việc" to listOf(
                Menu(Icons.Default.Map, "Lịch trình đặt tour", "Các tour sắp tới", onOpenTourSchedule),
            ),
            "Khác" to supportAndAbout
        )

        UserRole.Admin, UserRole.Supervisor -> listOf(
            "Tài khoản" to accountCommon,
            "Quản trị" to listOf(
                Menu(Icons.Default.SettingsApplications, "Quản lý", "Người dùng, nội dung, phê duyệt", onOpenAdminManagement),
                Menu(Icons.Default.Insights, "Thống kê", "Báo cáo hệ thống", onOpenAdminStats),
            ),
            "Khác" to supportAndAbout
        )

        UserRole.Unknown -> listOf(
            "Tài khoản" to accountCommon,
            "Khác" to supportAndAbout
        )
    }
}