package id.antasari.cityreport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.cityreport.ui.navigation.Routes
import id.antasari.cityreport.ui.theme.*

/**
 * Simple Bottom Navigation sesuai design
 */
@Composable
fun BlueBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AdaptiveColors.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            BottomNavItem(
                icon = if (currentRoute == Routes.HOME_USER) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = currentRoute == Routes.HOME_USER,
                onClick = { onNavigate(Routes.HOME_USER) }
            )
            
            // Laporan
            BottomNavItem(
                icon = if (currentRoute == Routes.REPORT_LIST) Icons.Filled.Description else Icons.Outlined.Description,
                label = "Laporan",
                isSelected = currentRoute == Routes.REPORT_LIST,
                onClick = { onNavigate(Routes.REPORT_LIST) }
            )
            
            // FAB - Create Report (tengah, elevated)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Primary, CircleShape)
                    .clickable { onNavigate(Routes.REPORT_FORM) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Buat Laporan",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Nearby
            BottomNavItem(
                icon = if (currentRoute == Routes.NEARBY_REPORTS) Icons.Filled.LocationOn else Icons.Outlined.LocationOn,
                label = "Nearby",
                isSelected = currentRoute == Routes.NEARBY_REPORTS,
                onClick = { onNavigate(Routes.NEARBY_REPORTS) }
            )
            
            // Profile
            BottomNavItem(
                icon = if (currentRoute == Routes.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                label = "Profile",
                isSelected = currentRoute == Routes.PROFILE,
                onClick = { onNavigate(Routes.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Primary else AdaptiveColors.textSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) Primary else AdaptiveColors.textSecondary
        )
    }
}
