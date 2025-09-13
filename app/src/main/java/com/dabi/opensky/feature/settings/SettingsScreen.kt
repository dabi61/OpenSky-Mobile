package com.dabi.opensky.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Text(
                text = "Cài đặt",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // App Preferences
            SettingsSection(title = "Giao diện") {
                SettingsSwitchItem(
                    icon = Icons.Default.Star,
                    title = "Chế độ tối",
                    subtitle = "Sử dụng giao diện tối",
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )
            }

            // Notifications
            SettingsSection(title = "Thông báo") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Thông báo",
                    subtitle = "Nhận thông báo từ ứng dụng",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                
                SettingsSwitchItem(
                    icon = Icons.Default.Email,
                    title = "Email thông báo",
                    subtitle = "Nhận thông báo qua email",
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it },
                    enabled = notificationsEnabled
                )
                
                SettingsSwitchItem(
                    icon = Icons.Default.Phone,
                    title = "Thông báo đẩy",
                    subtitle = "Nhận thông báo đẩy trên điện thoại",
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it },
                    enabled = notificationsEnabled
                )
            }

            // Privacy & Security
            SettingsSection(title = "Quyền riêng tư & Bảo mật") {
                SettingsSwitchItem(
                    icon = Icons.Default.LocationOn,
                    title = "Vị trí",
                    subtitle = "Cho phép truy cập vị trí để tìm khách sạn gần bạn",
                    checked = locationEnabled,
                    onCheckedChange = { locationEnabled = it }
                )
                
                SettingsClickItem(
                    icon = Icons.Default.Settings,
                    title = "Bảo mật",
                    subtitle = "Đổi mật khẩu, xác thực 2 lớp",
                    onClick = { /* TODO: Navigate to security settings */ }
                )
                
                SettingsClickItem(
                    icon = Icons.Default.ThumbUp,
                    title = "Quyền riêng tư",
                    subtitle = "Quản lý dữ liệu cá nhân",
                    onClick = { /* TODO: Navigate to privacy settings */ }
                )
            }

            // App Info
            SettingsSection(title = "Về ứng dụng") {
                SettingsClickItem(
                    icon = Icons.Default.Info,
                    title = "Phiên bản",
                    subtitle = "OpenSky v1.0.0",
                    onClick = { /* TODO: Show version info */ }
                )
                
                SettingsClickItem(
                    icon = Icons.Default.AccountBox,
                    title = "Điều khoản sử dụng",
                    subtitle = "Đọc điều khoản và điều kiện",
                    onClick = { /* TODO: Show terms */ }
                )
                
                SettingsClickItem(
                    icon = Icons.Default.Person,
                    title = "Chính sách bảo mật",
                    subtitle = "Tìm hiểu cách chúng tôi bảo vệ dữ liệu của bạn",
                    onClick = { /* TODO: Show privacy policy */ }
                )
                
                SettingsClickItem(
                    icon = Icons.Default.ThumbUp,
                    title = "Hỗ trợ",
                    subtitle = "Liên hệ với chúng tôi",
                    onClick = { /* TODO: Open support */ }
                )
                
                SettingsClickItem(
                    icon = Icons.Default.Star,
                    title = "Đánh giá ứng dụng",
                    subtitle = "Để lại đánh giá trên cửa hàng ứng dụng",
                    onClick = { /* TODO: Open app store */ }
                )
            }

            // Danger Zone
            SettingsSection(title = "Tài khoản") {
                SettingsClickItem(
                    icon = Icons.Default.Delete,
                    title = "Xóa tài khoản",
                    subtitle = "Xóa vĩnh viễn tài khoản và tất cả dữ liệu",
                    onClick = { /* TODO: Show delete account dialog */ },
                    isDestructive = true
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        content()
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) 
                MaterialTheme.colorScheme.surface 
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
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
                tint = if (enabled) 
                    MaterialTheme.colorScheme.onSurface 
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) 
                        MaterialTheme.colorScheme.onSurface 
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsClickItem(
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
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
