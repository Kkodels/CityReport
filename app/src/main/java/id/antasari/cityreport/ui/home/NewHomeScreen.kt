package id.antasari.cityreport.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.data.repository.StorageRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.*
import id.antasari.cityreport.ui.navigation.Routes
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewHomeScreen(
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    val reportsRepository = remember { ReportsRepository() }
    
    var userName by remember { mutableStateOf("User") }
    var allReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var myReportsCount by remember { mutableStateOf(0) }
    var popularReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }
    
    val storageRepository = remember { StorageRepository() }
    
    // Filter & Sort states
    var selectedFilter by remember { mutableStateOf("Semua") }
    var sortNewest by remember { mutableStateOf(true) }
    
    // Load data
    LaunchedEffect(Unit) {
        scope.launch {
            // Get user name
            val userResult = authRepository.getCurrentUserWithRole()
            if (userResult.isSuccess) {
                userName = userResult.getOrNull()?.name ?: "User"
                val userId = userResult.getOrNull()?.userId
                val photoId = userResult.getOrNull()?.profilePhotoId
                
                // Load profile photo
                if (!photoId.isNullOrEmpty()) {
                    profilePhotoUrl = storageRepository.getProfilePhotoUrl(photoId)
                }
                
                // Get my reports count
                if (userId != null) {
                    val myReports = reportsRepository.getReportsForUser(userId)
                    if (myReports.isSuccess) {
                        myReportsCount = myReports.getOrNull()?.size ?: 0
                    }
                }
            }
            
            // Get all reports
            val reportsResult = reportsRepository.getAllReports()
            if (reportsResult.isSuccess) {
                allReports = reportsResult.getOrNull() ?: emptyList()
            }
            
            // Get popular reports
            val popularResult = reportsRepository.getPopularReportsThisWeek()
            if (popularResult.isSuccess) {
                popularReports = popularResult.getOrNull() ?: emptyList()
            }
            
            isLoading = false
        }
    }
    
    // Apply filter and sort
    val filteredReports = allReports
        .filter { report ->
            when (selectedFilter) {
                "Baru" -> report.status == "Baru"
                "Diproses" -> report.status == "Diproses"
                "Selesai" -> report.status == "Selesai"
                "Mendesak" -> report.severity >= 4
                else -> true
            }
        }
        .let { list ->
            if (sortNewest) list.sortedByDescending { it.createdAt }
            else list.sortedBy { it.createdAt }
        }
        .take(10)
    
    val recentReports = filteredReports
    val greeting = getGreeting()
    
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
                    .padding(Spacing.Large)
            ) {
                // Top row: Greeting + Profile
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.textSecondary
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Profile Photo
                    Box {
                        ProfilePhotoCircle(
                            photoUrl = profilePhotoUrl,
                            userName = userName,
                            size = 48.dp,
                            onClick = { onNavigate("profile") }
                        )
                        
                        // Red notification dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .size(10.dp)
                                .background(AccentRed, CircleShape)
                        )
                    }
                }
                
                Spacer(Modifier.height(Spacing.Large))
                
                // Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("search") },
                    shape = RoundedCornerShape(CornerRadius.Medium),
                    color = BackgroundWhite,
                    shadowElevation = Elevation.Low
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.Medium),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(IconSize.Medium)
                        )
                        Text(
                            text = "Cari laporan atau lokasi...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextHint
                        )
                    }
                }
            }
        }
        
        // Create Report Card
        item {
            GradientActionCard(
                title = "Buat Laporan",
                subtitle = "Laporkan masalah di sekitarmu",
                icon = Icons.Default.CameraAlt,
                onClick = { onNavigate("report/create") },
                modifier = Modifier.padding(horizontal = Spacing.Large)
            )
            Spacer(Modifier.height(Spacing.Large))
        }
        
        // Feature Cards Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Large),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                FeatureCard(
                    title = "Laporan Saya",
                    subtitle = "$myReportsCount Laporan",
                    icon = Icons.Default.Folder,
                    iconBackground = CategoryJalanRusakBg,
                    onClick = { onNavigate(Routes.REPORT_LIST) },
                    modifier = Modifier.weight(1f)
                )
                
                FeatureCard(
                    title = "Di Sekitar",
                    subtitle = "Lihat peta",
                    icon = Icons.Default.Map,
                    iconBackground = CategorySampahBg,
                    onClick = { onNavigate(Routes.NEARBY_REPORTS) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(Spacing.Large))
        }
        
        // Popular Reports Section (Trending)
        if (popularReports.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Trending Minggu Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
            
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.Medium),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularReports.size) { index ->
                        val report = popularReports[index]
                        PopularReportCard(
                            title = report.title,
                            votes = report.votes,
                            category = report.category,
                            onClick = { onNavigate("report/${report.id}") }
                        )
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(Spacing.Large))
            }
        }
        
        //  Recent Reports Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Laporan Terbaru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sort toggle
                    Surface(
                        modifier = Modifier.clickable { sortNewest = !sortNewest },
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceGray
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (sortNewest) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                if (sortNewest) "Terbaru" else "Terlama",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigate("reports/all") }
                    )
                }
            }
            Spacer(Modifier.height(Spacing.Small))
            
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Large)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua", "Baru", "Diproses", "Selesai", "Mendesak").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { 
                            Text(
                                filter,
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = White
                        )
                    )
                }
            }
            Spacer(Modifier.height(Spacing.Medium))
        }
        
        // Reports List
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.ExtraLarge),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        } else if (recentReports.isEmpty()) {
            item {
                EmptyStateView(
                    message = "Belum ada laporan",
                    modifier = Modifier.padding(Spacing.ExtraLarge)
                )
            }
        } else {
            items(recentReports) { report ->
                NewReportCard(
                    title = report.title,
                    status = report.status,
                    category = report.category,
                    location = report.locationName,
                    timeAgo = getTimeAgo(report.createdAt),
                    createdAt = report.createdAt,
                    imageUrl = report.photoId,
                    severity = report.severity,
                    votes = report.votes,
                    onClick = { onNavigate("report/${report.id}") },
                    modifier = Modifier.padding(horizontal = Spacing.Large, vertical = Spacing.ExtraSmall)
                )
            }
            
            item {
                Spacer(Modifier.height(100.dp)) // Bottom nav padding
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        Icon(
            imageVector = Icons.Rounded.Inbox,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Selamat Pagi"
        in 12..14 -> "Selamat Siang"
        in 15..18 -> "Selamat Sore"
        else -> "Selamat Malam"
    }
}

private fun getTimeAgo(createdAt: String): String {
    val timestamp = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(createdAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Baru saja"
        diff < 3600_000 -> "${diff / 60_000}m yang lalu"
        diff < 86400_000 -> "${diff / 3600_000}h yang lalu"
        diff < 604800_000 -> "${diff / 86400_000}d yang lalu"
        else -> SimpleDateFormat("dd MMM yyyy", Locale("id")).format(Date(timestamp))
    }
}

@Composable
private fun PopularReportCard(
    title: String,
    votes: Int,
    category: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = AdaptiveColors.card,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Trending badge
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🔥",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            "$votes",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    category,
                    style = MaterialTheme.typography.labelSmall,
                    color = AdaptiveColors.textTertiary
                )
            }
            
            // Title
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AdaptiveColors.textPrimary,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
