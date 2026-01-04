package id.antasari.cityreport.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.NewReportCard
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    val authRepository = remember { AuthRepository() }
    
    var allReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("Semua") }
    var selectedUrgencyFilter by remember { mutableStateOf(0) } // 0=All, 2=Medium, 3=High
    var isLoading by remember { mutableStateOf(true) }
    var userId by remember { mutableStateOf<String?>(null) }
    
    // Load reports
    LaunchedEffect(Unit) {
        scope.launch {
            // Get current user
            val userResult = authRepository.getCurrentUserWithRole()
            userId = userResult.getOrNull()?.userId
            
            // Get reports
            val result = userId?.let {
                reportsRepository.getReportsForUser(it)
            } ?: reportsRepository.getAllReports()
            
            if (result.isSuccess) {
                allReports = result.getOrNull() ?: emptyList()
            }
            isLoading = false
        }
    }
    
    // Filter reports based on selected tab AND urgency
    val filteredReports = remember(selectedFilter, selectedUrgencyFilter, allReports) {
        var reports = when (selectedFilter) {
            "Menunggu" -> allReports.filter { it.status == "Baru" || it.status == "Menunggu" }
            "Diproses" -> allReports.filter { it.status == "Diproses" }
            "Selesai" -> allReports.filter { it.status == "Selesai" }
            else -> allReports // Semua
        }
        
        // Apply urgency filter
        if (selectedUrgencyFilter > 0) {
            reports = reports.filter { it.severity >= selectedUrgencyFilter }
        }
        
        reports
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Riwayat Laporan",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* Navigate to search */ }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdaptiveColors.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                            .background(AdaptiveColors.background)
                .padding(paddingValues)
        ) {
            // Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AdaptiveColors.surface)
                    .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                // Status filters
                items(listOf("Semua", "Menunggu", "Diproses", "Selesai")) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { 
                            Text(
                                filter,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = White,
                            containerColor = SurfaceGray,
                            labelColor = TextSecondary
                        )
                    )
                }
                
                // Urgency filters
                item {
                    FilterChip(
                        selected = selectedUrgencyFilter == 3,
                        onClick = { selectedUrgencyFilter = if (selectedUrgencyFilter == 3) 0 else 3 },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔴")
                                Text(
                                    "Mendesak",
                                    fontWeight = if (selectedUrgencyFilter == 3) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = androidx.compose.ui.graphics.Color(0xFFFF5722),
                            selectedLabelColor = White,
                            containerColor = SurfaceGray,
                            labelColor = TextSecondary
                        )
                    )
                }
                
                item {
                    FilterChip(
                        selected = selectedUrgencyFilter == 2,
                        onClick = { selectedUrgencyFilter = if (selectedUrgencyFilter == 2) 0 else 2 },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🟡")
                                Text(
                                    "Prioritas",
                                    fontWeight = if (selectedUrgencyFilter == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = androidx.compose.ui.graphics.Color(0xFFFFA726),
                            selectedLabelColor = White,
                            containerColor = SurfaceGray,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            
            Divider(color = Gray200)
            
            Spacer(Modifier.height(Spacing.Small))
            
            // Reports List
            if (isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (filteredReports.isEmpty()) {
                // Empty state
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(Spacing.ExtraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "📋",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(Spacing.Medium))
                    Text(
                        "Belum ada laporan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spacing.Small))
                    Text(
                        if (selectedFilter == "Semua") "Buat laporan pertama Anda" 
                        else "Tidak ada laporan dengan status \"$selectedFilter\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(Spacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    items(filteredReports) { report ->
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
                            onClick = { onNavigateToDetail(report.id) }
                        )
                    }
                    
                    item {
                        Spacer(Modifier.height(80.dp)) // Bottom nav padding
                    }
                }
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
        diff < 3600_000 -> "${diff / 60_000}m yang lalu"
        diff < 86400_000 -> "${diff / 3600_000}h yang lalu"
        diff < 604800_000 -> "${diff / 86400_000}d yang lalu"
        else -> {
            val sdf = SimpleDateFormat("dd MMM • HH:mm", Locale("id"))
            sdf.format(Date(timestamp))
        }
    }
}
