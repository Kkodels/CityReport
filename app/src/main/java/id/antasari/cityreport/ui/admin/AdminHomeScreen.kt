package id.antasari.cityreport.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.data.repository.StorageRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.ProfilePhotoCircle
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onNavigateToReportDetail: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    val authRepository = remember { AuthRepository() }
    
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var adminName by remember { mutableStateOf("Admin CityReport") }
    var avgResponseTime by remember { mutableStateOf(0.0) }
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }
    var categoryDistribution by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    
    val storageRepository = remember { StorageRepository() }
    
    // Load data
    LaunchedEffect(Unit) {
        scope.launch {
            // Get admin name
            val userResult = authRepository.getCurrentUserWithRole()
            if (userResult.isSuccess) {
                adminName = userResult.getOrNull()?.name ?: "Admin"
                val photoId = userResult.getOrNull()?.profilePhotoId
                
                // Load admin photo
                if (!photoId.isNullOrEmpty()) {
                    profilePhotoUrl = storageRepository.getProfilePhotoUrl(photoId)
                }
            }
            
            // Get all reports
            val result = reportsRepository.getAllReports()
            if (result.isSuccess) {
                reports = result.getOrNull() ?: emptyList()
            }
            
            // Get analytics data
            val responseTimeResult = reportsRepository.getAverageResponseTime()
            if (responseTimeResult.isSuccess) {
                avgResponseTime = responseTimeResult.getOrNull() ?: 0.0
            }
            
            val categoryResult = reportsRepository.getCategoryDistribution()
            if (categoryResult.isSuccess) {
                categoryDistribution = categoryResult.getOrNull() ?: emptyMap()
            }
            
            isLoading = false
        }
    }
    
    // Calculate statistics
    val totalReports = reports.size
    val thisMonthReports = reports.count { 
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.createdAt.take(10))
            calendar.time = date ?: Date()
            calendar.get(Calendar.MONTH) == currentMonth
        } catch (e: Exception) { false }
    }
    val completedReports = reports.count { it.status == "Selesai" }
    val pendingReports = reports.count { it.status == "Baru" || it.status == "Diproses" }
    
    // Category breakdown
    val categoryStats = reports.groupBy { it.category }
        .mapValues { (_, list) -> list.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)
    
    // High priority reports
    val highPriorityReports = reports
        .filter { it.severity >= 4 }
        .sortedByDescending { it.createdAt }
        .take(5)
    
    Scaffold(
        containerColor = AdaptiveColors.background,
        bottomBar = {
            AdminBottomNav(
                currentRoute = id.antasari.cityreport.ui.navigation.Routes.HOME_ADMIN,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Header
                item {
                    AdminHeader(
                        adminName = adminName,
                        profilePhotoUrl = profilePhotoUrl
                    )
                }
                
                // Stats Cards
                item {
                    AdminStatsSection(
                        totalReports = totalReports,
                        thisMonthReports = thisMonthReports,
                        completedReports = completedReports,
                        pendingReports = pendingReports,
                        onNavigate = onNavigate
                    )
                }
                
                // Trend Chart Placeholder
                item {
                    TrendChartCard()
                }
                
                // Category Breakdown
                item {
                    CategoryBreakdownCard(
                        categoryStats = categoryStats,
                        totalReports = totalReports
                    )
                }
                
                // Response Time Analytics
                item {
                    ResponseTimeCard(avgResponseTime = avgResponseTime)
                }
                
                // High Priority Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Prioritas Tinggi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Lihat Semua",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onNavigate("admin/reports/priority") }
                        )
                    }
                }
                
                // High Priority Reports
                items(highPriorityReports) { report ->
                    PriorityReportCard(
                        report = report,
                        onClick = { onNavigateToReportDetail(report.id) }
                    )
                }
                
                // Empty state for priority
                if (highPriorityReports.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tidak ada laporan prioritas tinggi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHeader(
    adminName: String,
    profilePhotoUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar - Use ProfilePhotoCircle
            ProfilePhotoCircle(
                photoUrl = profilePhotoUrl,
                userName = adminName,
                size = 48.dp,
                onClick = null
            )
            
            Column {
                Text(
                    "Selamat Datang,",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    adminName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

    }
}

@Composable
private fun AdminStatsSection(
    totalReports: Int,
    thisMonthReports: Int,
    completedReports: Int,
    pendingReports: Int,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Total & This Month
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Laporan",
                value = totalReports.toString(),
                trend = "+2.5%",
                trendPositive = true,
                icon = Icons.Default.Description,
                iconColor = Primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("admin/reports") }
            )
            StatCard(
                title = "Laporan Bulan Ini",
                value = thisMonthReports.toString(),
                trend = "+12%",
                trendPositive = true,
                icon = Icons.Default.CalendarToday,
                iconColor = Color(0xFFFFA726),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("admin/reports") }
            )
        }
        
        // Row 2: Completed & Pending
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Selesai",
                value = completedReports.toString(),
                trend = null,
                trendPositive = true,
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF4CAF50),
                showProgress = true,
                progress = if (totalReports > 0) completedReports.toFloat() / totalReports else 0f,
                progressColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pending",
                value = pendingReports.toString(),
                trend = null,
                trendPositive = false,
                icon = Icons.Default.Pending,
                iconColor = Color(0xFFFF9800),
                showProgress = true,
                progress = if (totalReports > 0) pendingReports.toFloat() / totalReports else 0f,
                progressColor = Color(0xFFFF9800),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("admin/reports") }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    trend: String?,
    trendPositive: Boolean,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    progress: Float = 0f,
    progressColor: Color = Primary,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (trend != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (trendPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = trend,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trendPositive) Color(0xFF4CAF50) else Color(0xFFE53935),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            if (showProgress) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun TrendChartCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Tren Laporan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Statistik volume laporan mingguan",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Simple bar chart representation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                val values = listOf(0.4f, 0.6f, 0.9f, 0.5f, 0.7f, 0.3f, 0.5f)
                
                days.forEachIndexed { index, day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height((80 * values[index]).dp)
                                .background(
                                    if (index == 2) Primary else Primary.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Text(
                            day,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (index == 2) TextPrimary else TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    categoryStats: List<Pair<String, Int>>,
    totalReports: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Kategori Terbanyak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val colors = listOf(
                Color(0xFF2196F3),
                Color(0xFFFF9800),
                Color(0xFF4CAF50)
            )
            
            categoryStats.forEachIndexed { index, (category, count) ->
                val percentage = if (totalReports > 0) (count * 100) / totalReports else 0
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = id.antasari.cityreport.utils.CategoryIcons.getIconForCategory(category),
                                contentDescription = null,
                                tint = id.antasari.cityreport.utils.CategoryIcons.getColorForCategory(category),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        Text(
                            "$percentage%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = id.antasari.cityreport.utils.CategoryIcons.getColorForCategory(category),
                        trackColor = id.antasari.cityreport.utils.CategoryIcons.getColorForCategory(category).copy(alpha = 0.2f)
                    )
                }
            }
            
            if (categoryStats.isEmpty()) {
                Text(
                    "Belum ada data kategori",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ResponseTimeCard(avgResponseTime: Double) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Waktu Respons Rata-rata",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = String.format("%.1f", avgResponseTime),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        "hari",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                // Status indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        avgResponseTime <= 3.0 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        avgResponseTime <= 7.0 -> Color(0xFFFFA726).copy(alpha = 0.1f)
                        else -> Color(0xFFE53935).copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = when {
                            avgResponseTime <= 3.0 -> "Sangat Baik"
                            avgResponseTime <= 7.0 -> "Baik"
                            else -> "Perlu Ditingkatkan"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            avgResponseTime <= 3.0 -> Color(0xFF4CAF50)
                            avgResponseTime <= 7.0 -> Color(0xFFFFA726)
                            else -> Color(0xFFE53935)
                        }
                    )
                }
            }
            
            Text(
                "Waktu rata-rata dari laporan dibuat hingga diselesaikan",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun PriorityReportCard(
    report: Report,
    onClick: () -> Unit
) {
    val photoUrl = if (!report.photoId.isNullOrEmpty()) {
        "https://sgp.cloud.appwrite.io/v1/storage/buckets/693fc034000a342e577c/files/${report.photoId}/view?project=693ac9810019b47f348e"
    } else null
    
    val priorityColor = when {
        report.severity >= 5 -> Color(0xFFE53935)
        report.severity >= 4 -> Color(0xFFFF5722)
        else -> Color(0xFFFFA726)
    }
    
    val priorityLabel = when {
        report.severity >= 5 -> "DARURAT"
        report.severity >= 4 -> "TINGGI"
        else -> "SEDANG"
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(priorityColor.copy(alpha = 0.1f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(priorityColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = priorityColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        report.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = priorityColor
                    ) {
                        Text(
                            priorityLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    report.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        getTimeAgo(report.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text("•", color = TextTertiary)
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        report.locationName.take(15) + if (report.locationName.length > 15) "..." else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
                
                // Created date (absolute)
                Text(
                    text = "📅 ${formatAdminDate(report.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
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
        diff < 3600_000 -> "${diff / 60_000} mnt lalu"
        diff < 86400_000 -> "${diff / 3600_000} jam lalu"
        else -> "${diff / 86400_000} hari lalu"
    }
}

private fun formatAdminDate(dateString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(dateString)
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        date?.let { outputFormat.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}

