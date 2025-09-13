package com.dabi.opensky.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dabi.opensky.core.navigation.OpenSkyScreen

data class BottomNavItem(
    val route: OpenSkyScreen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = OpenSkyScreen.Home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "Trang chủ"
    ),
    BottomNavItem(
        route = OpenSkyScreen.Search,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        label = "Tìm kiếm"
    ),
    BottomNavItem(
        route = OpenSkyScreen.Favorites,
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
        label = "Yêu thích"
    ),
    BottomNavItem(
        route = OpenSkyScreen.Profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        label = "Hồ sơ"
    ),
    BottomNavItem(
        route = OpenSkyScreen.Settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "Cài đặt"
    )
)

@Composable
fun OpenSkyBottomNavigation(
    currentRoute: OpenSkyScreen?,
    onNavigate: (OpenSkyScreen) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
