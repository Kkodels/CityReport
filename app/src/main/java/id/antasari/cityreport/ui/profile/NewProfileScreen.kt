package id.antasari.cityreport.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.data.repository.StorageRepository
import id.antasari.cityreport.ui.components.ProfilePhotoCircle
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NewProfileScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    val reportsRepository = remember { ReportsRepository() }
    
    var userName by remember { mutableStateOf("User") }
    var userEmail by remember { mutableStateOf("email@example.com") }
    var totalReports by remember { mutableStateOf(0) }
    var reportsInProgress by remember { mutableStateOf(0) }
    var reportsNew by remember { mutableStateOf(0) }
    var reportsCompleted by remember { mutableStateOf(0) }
    var userStats by remember { mutableStateOf<id.antasari.cityreport.domain.model.UserStats?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val storageRepository = remember { StorageRepository() }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isUploadingPhoto = true
                android.util.Log.d("ProfilePhoto", "Starting upload for URI: $uri")
                
                val uploadResult = storageRepository.uploadProfilePhoto(context, uri)
                
                if (uploadResult.isSuccess) {
                    val photoId = uploadResult.getOrNull()
                    android.util.Log.d("ProfilePhoto", "Upload success! PhotoID: $photoId")
                    
                    // Save to database
                    if (photoId != null) {
                        val userResult = authRepository.getCurrentUserWithRole()
                        val userId = userResult.getOrNull()?.userId
                        
                        android.util.Log.d("ProfilePhoto", "UserId: $userId")
                        
                        if (userId != null) {
                            android.util.Log.d("ProfilePhoto", "Attempting to save photoId to database...")
                            val saveResult = authRepository.updateProfilePhoto(userId, photoId)
                            
                            if (saveResult.isSuccess) {
                                android.util.Log.d("ProfilePhoto", "✅ SaveResult to database!")
                            } else {
                                android.util.Log.e("ProfilePhoto", "❌ Save FAILED: ${saveResult.exceptionOrNull()?.message}")
                            }
                        } else {
                            android.util.Log.e("ProfilePhoto", "❌ UserId is NULL!")
                        }
                    }
                    
                    // Generate URL and set to state
                    val photoUrl = storageRepository.getProfilePhotoUrl(photoId)
                    android.util.Log.d("ProfilePhoto", "Generated URL: $photoUrl")
                    
                    profilePhotoUrl = photoUrl
                } else {
                    android.util.Log.e("ProfilePhoto", "Upload failed: ${uploadResult.exceptionOrNull()?.message}")
                }
                
                isUploadingPhoto = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        scope.launch {
            val userResult = authRepository.getCurrentUserWithRole()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                userName = user?.name ?: "User"
                userEmail = user?.email ?: "email@example.com"
                
                // Load existing profile photo
                val photoId = user?.profilePhotoId
                if (!photoId.isNullOrEmpty()) {
                    profilePhotoUrl = storageRepository.getProfilePhotoUrl(photoId)
                }
                
                val userId = user?.userId
                if (userId != null) {
                    val reports = reportsRepository.getReportsForUser(userId)
                    if (reports.isSuccess) {
                        val reportList = reports.getOrNull() ?: emptyList()
                        totalReports = reportList.size
                        reportsNew = reportList.count { it.status == "Baru" }
                        reportsInProgress = reportList.count { it.status == "Diproses" }
                        reportsCompleted = reportList.count { it.status == "Selesai" }
                        
                        // Get user stats
                        val statsResult = reportsRepository.getUserStats(userId)
                        if (statsResult.isSuccess) {
                            userStats = statsResult.getOrNull()
                        }
                    }
                }
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdaptiveColors.background)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                Text(
                    text = "Profil Saya",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                
                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    ProfilePhotoCircle(
                        photoUrl = profilePhotoUrl,
                        userName = userName,
                        size = 100.dp,
                        onClick = null
                    )
                    
                    // Upload indicator
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center),
                            color = Primary
                        )
                    }
                    
                    // Edit button
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clickable { 
                                if (!isUploadingPhoto) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                        shape = CircleShape,
                        color = Primary,
                        shadowElevation = Elevation.Medium
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Name & Email
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AdaptiveColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // User Stats Card
                userStats?.let { stats ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.Medium),
                        shape = RoundedCornerShape(16.dp),
                        color = AdaptiveColors.card,
                        shadowElevation = Elevation.Low
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.Medium)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${stats.totalReports}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("Total", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${stats.resolvedPercentage}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
                                    Text("Selesai", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${stats.averageVotes}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("Avg Votes", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Stats Cards - 2x2 Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    StatCard(
                        icon = Icons.Default.Description,
                        iconBackground = CategoryJalanRusakBg,
                        value = totalReports.toString(),
                        label = "TOTAL",
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        icon = Icons.Default.NewReleases,
                        iconBackground = Primary.copy(alpha = 0.1f),
                        value = reportsNew.toString(),
                        label = "BARU",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    StatCard(
                        icon = Icons.Default.HourglassBottom,
                        iconBackground = CategorySampahBg,
                        value = reportsInProgress.toString(),
                        label = "DIPROSES",
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        icon = Icons.Default.CheckCircle,
                        iconBackground = Success.copy(alpha = 0.1f),
                        value = reportsCompleted.toString(),
                        label = "SELESAI",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        
        // Menu Items
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                MenuItemCard(
                    icon = Icons.Default.Bookmark,
                    title = "Bookmark Area",
                    subtitle = "Lokasi tersimpan",
                    onClick = { /* Navigate */ }
                )
                
                MenuItemCard(
                    icon = Icons.Default.NightsStay,
                    title = "Mode Gelap",
                    subtitle = "Tampilan gelap untuk malam",
                    trailing = {
                        val context = LocalContext.current
                        val isDarkMode = ThemeManager.isDarkMode
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    ThemeManager.saveDarkMode(context, enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Primary
                            )
                        )
                    }
                )
                
                MenuItemCard(
                    icon = Icons.Default.HelpOutline,
                    title = "Bantuan & FAQ",
                    subtitle = "Pusat bantuan",
                    onClick = { /* Navigate */ }
                )
                
                MenuItemCard(
                    icon = Icons.Default.Settings,
                    title = "Pengaturan",
                    subtitle = "Akun & aplikasi",
                    onClick = { /* Navigate */ }
                )
            }
            Spacer(Modifier.height(Spacing.Large))
        }
        
        // Logout Button
        item {
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Large)
                    .height(52.dp),
                shape = RoundedCornerShape(CornerRadius.Medium),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Error)
            ) {
                Text(
                    text = "Keluar",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(Modifier.height(Spacing.Medium))
            
            // Version
            Text(
                text = "CityReport v1.6.7",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
    
    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(Icons.Default.Logout, null, tint = Error)
            },
            title = {
                Text("Keluar dari akun?")
            },
            text = {
                Text("Anda akan keluar dari aplikasi dan perlu login kembali.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            authRepository.logout()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: androidx.compose.ui.graphics.Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(Elevation.Low, RoundedCornerShape(CornerRadius.Medium)),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = AdaptiveColors.card
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBackground, RoundedCornerShape(CornerRadius.Small)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = AdaptiveColors.textPrimary
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = AdaptiveColors.textSecondary
            )
        }
    }
}

@Composable
private fun MenuItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(Elevation.Low, RoundedCornerShape(CornerRadius.Medium))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = AdaptiveColors.card
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(CornerRadius.Small)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AdaptiveColors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AdaptiveColors.textSecondary
                )
            }
            
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextTertiary
                )
            }
        }
    }
}
