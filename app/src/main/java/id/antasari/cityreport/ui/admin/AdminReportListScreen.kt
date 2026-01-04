package id.antasari.cityreport.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.navigation.Routes
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var filteredReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Load reports
    LaunchedEffect(Unit) {
        scope.launch {
            val result = reportsRepository.getAllReports()
            if (result.isSuccess) {
                reports = result.getOrNull() ?: emptyList()
                filteredReports = reports
            }
            isLoading = false
        }
    }
    
    // Apply filters
    LaunchedEffect(reports, selectedFilter, searchQuery) {
        filteredReports = reports.filter { report ->
            val matchesSearch = searchQuery.isEmpty() || 
                report.title.contains(searchQuery, ignoreCase = true) ||
                report.locationName.contains(searchQuery, ignoreCase = true)
            
            val matchesFilter = when (selectedFilter) {
                "Baru" -> report.status == "Baru"
                "Diproses" -> report.status == "Diproses"
                "Selesai" -> report.status == "Selesai"
                "Prioritas" -> report.severity >= 4
                else -> true
            }
            
            matchesSearch && matchesFilter
        }
    }
    
    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundWhite)
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Kelola Laporan",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.FilterList, "Filter")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundWhite
                    )
                )
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Cari laporan...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Gray200,
                        focusedBorderColor = Primary
                    )
                )
                
                // Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Semua", "Baru", "Diproses", "Selesai", "Prioritas")
                    items(filters) { filter ->
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
                                selectedLabelColor = White
                            )
                        )
                    }
                }
                
                Divider(color = Gray200)
            }
        },
        bottomBar = {
            AdminBottomNav(
                currentRoute = Routes.ADMIN_REPORT_LIST,
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
        } else if (filteredReports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Tidak ada laporan",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stats summary
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${filteredReports.size} laporan ditemukan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                
                items(filteredReports) { report ->
                    AdminReportCard(
                        report = report,
                        onClick = { onNavigateToDetail(report.id) }
                    )
                }
                
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminReportCard(
    report: Report,
    onClick: () -> Unit
) {
    val photoUrl = if (!report.photoId.isNullOrEmpty()) {
        "https://sgp.cloud.appwrite.io/v1/storage/buckets/693fc034000a342e577c/files/${report.photoId}/view?project=693ac9810019b47f348e"
    } else null
    
    val statusColor = when (report.status) {
        "Baru" -> Color(0xFF2196F3)
        "Diproses" -> Color(0xFFFFA726)
        "Selesai" -> Color(0xFF4CAF50)
        else -> TextSecondary
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
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
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SurfaceGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = TextTertiary,
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
                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            report.status.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Priority badge
                    if (report.severity >= 4) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF5722)
                        ) {
                            Text(
                                "PRIORITAS",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Text(
                    report.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    report.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        report.locationName.take(20) + if (report.locationName.length > 20) "..." else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text("•", color = TextTertiary)
                    Text(
                        getTimeAgo(report.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
            
            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun AdminBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundWhite,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AdminNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = currentRoute == Routes.HOME_ADMIN,
                onClick = { onNavigate(Routes.HOME_ADMIN) }
            )
            AdminNavItem(
                icon = Icons.Default.Description,
                label = "Laporan",
                selected = currentRoute == Routes.ADMIN_REPORT_LIST,
                onClick = { onNavigate(Routes.ADMIN_REPORT_LIST) }
            )
            AdminNavItem(
                icon = Icons.Default.Person,
                label = "Profil",
                selected = currentRoute == Routes.ADMIN_PROFILE,
                onClick = { onNavigate(Routes.ADMIN_PROFILE) }
            )
        }
    }
}

@Composable
private fun AdminNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) Primary else TextTertiary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Primary else TextTertiary,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
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
        diff < 3600_000 -> "${diff / 60_000} mnt"
        diff < 86400_000 -> "${diff / 3600_000} jam"
        else -> "${diff / 86400_000} hari"
    }
}
