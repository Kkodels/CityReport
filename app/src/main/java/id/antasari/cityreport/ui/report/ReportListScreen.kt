package id.antasari.cityreport.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.PremiumCard
import id.antasari.cityreport.ui.components.StatusBadge
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    val reportsRepository = remember { ReportsRepository() }
    
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var filteredReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Filter state
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    // Load reports
    LaunchedEffect(Unit) {
        scope.launch {
            val userResult = authRepository.getCurrentUserWithRole()
            
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                val reportsResult = reportsRepository.getReportsForUser(user.userId)
                
                if (reportsResult.isSuccess) {
                    reports = reportsResult.getOrNull() ?: emptyList()
                    filteredReports = reports
                } else {
                    errorMessage = reportsResult.exceptionOrNull()?.message
                }
            } else {
                errorMessage = "Gagal mendapatkan data pengguna"
            }
            
            isLoading = false
        }
    }
    
    // Apply filter
    LaunchedEffect(reports, selectedStatus) {
        filteredReports = if (selectedStatus == null) {
            reports
        } else {
            reports.filter { it.status == selectedStatus }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Gradient Header with Filter Chips
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Add padding for status bar
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Laporan Saya",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null },
                        label = { Text("Semua") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Primary,
                            containerColor = Color.White.copy(alpha = 0.2f),
                            labelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedStatus == "Menunggu",
                        onClick = { selectedStatus = if (selectedStatus == "Menunggu") null else "Menunggu" },
                        label = { Text("Menunggu") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Primary,
                            containerColor = Color.White.copy(alpha = 0.2f),
                            labelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedStatus == "Diproses",
                        onClick = { selectedStatus = if (selectedStatus == "Diproses") null else "Diproses" },
                        label = { Text("Diproses") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Primary,
                            containerColor = Color.White.copy(alpha = 0.2f),
                            labelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedStatus == "Selesai",
                        onClick = { selectedStatus = if (selectedStatus == "Selesai") null else "Selesai" },
                        label = { Text("Selesai") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Primary,
                            containerColor = Color.White.copy(alpha = 0.2f),
                            labelColor = Color.White
                        )
                    )
                }
            }
        }
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Space for bottom nav
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Terjadi kesalahan",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                filteredReports.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedStatus != null) {
                                "Tidak ada laporan dengan status $selectedStatus"
                            } else {
                                "Belum ada laporan"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Gray600
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredReports) { report ->
                            ReportListItem(
                                report = report,
                                onClick = { onOpenDetail(report.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportListItem(
    report: Report,
    onClick: () -> Unit
) {
    PremiumCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = 2
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = report.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📅 ${report.createdAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            StatusBadge(
                status = report.status,
                animated = true
            )
        }
    }
}
