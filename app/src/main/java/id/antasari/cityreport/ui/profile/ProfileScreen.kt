package id.antasari.cityreport.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.ui.components.PremiumCard
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigateToReportList: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    val reportsRepository = remember { ReportsRepository() }
    
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var totalReports by remember { mutableStateOf(0) }
    
    // Load user data
    LaunchedEffect(Unit) {
        scope.launch {
            val result = authRepository.getCurrentUserWithRole()
            if (result.isSuccess) {
                val profile = result.getOrNull()
                userName = profile?.name ?: "User"
                userEmail = profile?.email ?: ""
                
                // Get total reports
                profile?.userId?.let { userId ->
                    val reportsResult = reportsRepository.getReportsForUser(userId)
                    if (reportsResult.isSuccess) {
                        totalReports = reportsResult.getOrNull()?.size ?: 0
                    }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Primary, PrimaryDark) // Updated to blue theme
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Profil",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Profile Card
                PremiumCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Primary
                            )
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                        }
                    }
                    
                    HorizontalDivider()
                    
                    OutlinedButton(
                        onClick = { /* TODO: Edit profile */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profil")
                    }
                }
            }
        }
        
        // Menu Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.Description,
                title = "Total Laporan",
                subtitle = "$totalReports laporan",
                onClick = onNavigateToReportList
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "Pengaturan",
                subtitle = null,
                onClick = { /* TODO */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "Bantuan & FAQ",
                subtitle = null,
                onClick = { /* TODO */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Keluar",
                subtitle = null,
                onClick = onLogout,
                textColor = Error
            )
        }
        
        Spacer(Modifier.weight(1f))
        
        // Version Footer
        Text(
            text = "CityReport v1.6.7\n© 2025 All rights reserved",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    textColor: Color = Gray800
) {
    PremiumCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (textColor == Error) Error.copy(alpha = 0.1f)
                        else Primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Gray400
            )
        }
    }
}
