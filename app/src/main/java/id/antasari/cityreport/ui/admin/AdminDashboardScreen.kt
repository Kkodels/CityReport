package id.antasari.cityreport.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.*
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Admin Dashboard dengan statistics dan charts
 */
@Composable
fun AdminDashboardScreen() {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load all reports
    LaunchedEffect(Unit) {
        scope.launch {
            val result = reportsRepository.getAllReports()
            if (result.isSuccess) {
                reports = result.getOrNull() ?: emptyList()
            }
            isLoading = false
        }
    }
    
    // Calculate statistics
    val totalReports = reports.size
    val pendingCount = reports.count { it.status == "Pending" }
    val inProgressCount = reports.count { it.status == "In Progress" }
    val completedCount = reports.count { it.status == "Completed" }
    
    // Category breakdown
    val categoryData = reports.groupBy { it.category }
        .map { (category, items) -> 
            CategoryStat(category, items.size) 
        }
        .sortedByDescending { it.count }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Dashboard Admin",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ringkasan laporan dan statistik",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        
        // Statistics Cards
        item {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StatCard(
                            title = "Total",
                            value = totalReports.toString(),
                            icon = Icons.Default.Assessment,
                            color = Primary
                        )
                    }
                    item {
                        StatCard(
                            title = "Pending",
                            value = pendingCount.toString(),
                            icon = Icons.Default.HourglassEmpty,
                            color = StatusPendingText
                        )
                    }
                    item {
                        StatCard(
                            title = "Proses",
                            value = inProgressCount.toString(),
                            icon = Icons.Default.Refresh,
                            color = StatusInProgressText
                        )
                    }
                    item {
                        StatCard(
                            title = "Selesai",
                            value = completedCount.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = StatusCompletedText
                        )
                    }
                }
            }
        }
        
        // Category Chart Section
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Laporan per Kategori",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        // Category bars
        items(categoryData) { stat ->
            CategoryBar(
                category = stat.category,
                count = stat.count,
                maxCount = categoryData.firstOrNull()?.count ?: 1
            )
        }
        
        // Status Breakdown
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Status Laporan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))
        }
        
        item {
            BlueCard(elevation = 2) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusRow("Pending", pendingCount, totalReports, StatusPendingText)
                    StatusRow("In Progress", inProgressCount, totalReports, StatusInProgressText)
                    StatusRow("Completed", completedCount, totalReports, StatusCompletedText)
                }
            }
        }
        
        // Empty space at bottom
        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    BlueCard(
        elevation = 3,
        padding = BlueCardPadding.Medium,
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun CategoryBar(
    category: String,
    count: Int,
    maxCount: Int
) {
    val percentage = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "$count laporan",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            // Background
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Gray200
            ) {}
            
            // Foreground
            Surface(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight(),
                color = Primary
            ) {}
        }
    }
}

@Composable
private fun StatusRow(
    status: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat() * 100).toInt() else 0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = color
                ) {}
            }
            Text(
                text = status,
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "($percentage%)",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

data class CategoryStat(
    val category: String,
    val count: Int
)
