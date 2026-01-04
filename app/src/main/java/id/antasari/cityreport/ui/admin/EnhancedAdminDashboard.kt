package id.antasari.cityreport.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.NewReportCard
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAdminDashboard(
    onNavigateToDetail: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    
    var allReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            val result = reportsRepository.getAllReports()
            if (result.isSuccess) {
                allReports = result.getOrNull() ?: emptyList()
            }
            isLoading = false
        }
    }
    
    // Calculate statistics
    val stats = remember(allReports) {
        mapOf(
            "total" to allReports.size,
            "menunggu" to allReports.count { it.status == "Baru" || it.status == "Menunggu" },
            "diproses" to allReports.count { it.status == "Diproses" },
            "selesai" to allReports.count { it.status == "Selesai" },
            "urgensi_tinggi" to allReports.count { it.severity == 3 },
            "urgensi_sedang" to allReports.count { it.severity == 2 },
            "urgensi_rendah" to allReports.count { it.severity == 1 }
        )
    }
    
    val recentReports = allReports.take(5)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
                .padding(paddingValues)
                .padding(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            // Statistics Cards
            item {
                Text(
                    "Statistik Laporan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Status Statistics Row
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    StatCard(
                        title = "Total",
                        value = stats["total"] ?: 0,
                        icon = Icons.Default.Description,
                        color = Primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Menunggu",
                        value = stats["menunggu"] ?: 0,
                        icon = Icons.Default.HourglassEmpty,
                        color = androidx.compose.ui.graphics.Color(0xFFFFA726),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    StatCard(
                        title = "Diproses",
                        value = stats["diproses"] ?: 0,
                        icon = Icons.Default.Engineering,
                        color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Selesai",
                        value = stats["selesai"] ?: 0,
                        icon = Icons.Default.CheckCircle,
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Urgency Statistics
            item {
                Spacer(Modifier.height(Spacing.Small))
                Text(
                    "Tingkat Urgensi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    UrgencyStatCard(
                        title = "Mendesak",
                        value = stats["urgensi_tinggi"] ?: 0,
                        emoji = "🔴",
                        color = androidx.compose.ui.graphics.Color(0xFFFF5722),
                        modifier = Modifier.weight(1f)
                    )
                    UrgencyStatCard(
                        title = "Sedang",
                        value = stats["urgensi_sedang"] ?: 0,
                        emoji = "🟡",
                        color = androidx.compose.ui.graphics.Color(0xFFFFA726),
                        modifier = Modifier.weight(1f)
                    )
                    UrgencyStatCard(
                        title = "Rendah",
                        value = stats["urgensi_rendah"] ?: 0,
                        emoji = "🟢",
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Recent Activity
            item {
                Spacer(Modifier.height(Spacing.Medium))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Aktivitas Terbaru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigate("admin/reports") }) {
                        Text("Lihat Semua")
                    }
                }
            }
            
            // Recent Reports
            if (isLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(Spacing.ExtraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            } else {
                items(recentReports) { report ->
                    NewReportCard(
                        title = report.title,
                        status = report.status,
                        category = report.category,
                        location = report.locationName,
                        timeAgo = getTimeAgo(report.createdAt),
                        imageUrl = report.photoId,
                        severity = report.severity,
                        onClick = { onNavigateToDetail(report.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = BackgroundWhite,
        shadowElevation = Elevation.Low
    ) {
        Column(
            Modifier.padding(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun UrgencyStatCard(
    title: String,
    value: Int,
    emoji: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = BackgroundWhite,
        shadowElevation = Elevation.Low
    ) {
        Column(
            Modifier.padding(Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
        ) {
            Text(
                emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
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
        diff < 3600_000 -> "${diff / 60_000}m yang lalu"
        diff < 86400_000 -> "${diff / 3600_000}h yang lalu"
        diff < 604800_000 -> "${diff / 86400_000}d yang lalu"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale("id"))
            sdf.format(Date(timestamp))
        }
    }
}
