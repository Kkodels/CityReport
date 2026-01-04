package id.antasari.cityreport.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.data.repository.StorageRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.*
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Enhanced ReportDetailScreen dengan photo viewer dan better layout
 */
@Composable
fun ReportDetailScreen(
    reportId: String,
    onBack: () -> Unit,
    onOpenMap: (Double, Double) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    val storageRepository = remember { StorageRepository() }
    
    var report by remember { mutableStateOf<Report?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPhotoFullScreen by remember { mutableStateOf(false) }

    // Load report details
    LaunchedEffect(reportId) {
        scope.launch {
            val result = reportsRepository.getReportById(reportId)
            
            if (result.isSuccess) {
                report = result.getOrNull()
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        text = "Gagal memuat laporan",
                        style = MaterialTheme.typography.titleMedium,
                        color = Error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            report != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // PHOTO HEADER (if exists)
                    if (report!!.photoId != null) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = storageRepository.getPhotoUrl(report!!.photoId!!),
                                    contentDescription = report!!.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                        .clickable { showPhotoFullScreen = true },
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Back button overlay
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .padding(8.dp)
                                        .size(40.dp)
                                        .background(White.copy(alpha = 0.9f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = TextPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        // No photo - regular top bar
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Primary,
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = onBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = White
                                        )
                                    }
                                    Text(
                                        text = "Detail Laporan",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = White
                                    )
                                }
                            }
                        }
                    }
                    
                    // CONTENT
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Title
                            Text(
                                text = report!!.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            
                            // Status & Priority Pills
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusPill(status = report!!.status)
                                PriorityPill(priority = report!!.priority)
                            }
                            
                            HorizontalDivider()
                            
                            // Description Section
                            SectionHeader("Deskripsi")
                            Text(
                                text = report!!.description,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                            
                            HorizontalDivider()
                            
                            // Info Section
                            SectionHeader("Informasi")
                            InfoRow(
                                icon = Icons.Default.Category,
                                label = "Kategori",
                                value = report!!.category
                            )
                            InfoRow(
                                icon = Icons.Default.DateRange,
                                label = "Dilaporkan",
                                value = formatDate(report!!.createdAt)
                            )
                            InfoRow(
                                icon = Icons.Default.Star,
                                label = "Tingkat Keparahan",
                                value = "${report!!.severity}/5"
                            )
                            
                            HorizontalDivider()
                            
                            // Location Section
                            SectionHeader("Lokasi")
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                label = report!!.locationName,
                                value = report!!.address.ifEmpty { "Tidak ada alamat" }
                            )
                            
                            // Map Preview
                            Spacer(Modifier.height(8.dp))
                            BlueCard(
                                onClick = {
                                    onOpenMap(report!!.latitude, report!!.longitude)
                                },
                                elevation = 2,
                                padding = BlueCardPadding.None
                            ) {
                                Box(modifier = Modifier.height(200.dp)) {
                                    OsmMapView(
                                        initialLat = report!!.latitude,
                                        initialLng = report!!.longitude,
                                        isInteractive = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Full-screen photo viewer
        if (showPhotoFullScreen && report?.photoId != null) {
            Dialog(onDismissRequest = { showPhotoFullScreen = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { showPhotoFullScreen = false }
                ) {
                    AsyncImage(
                        model = storageRepository.getPhotoUrl(report!!.photoId!!),
                        contentDescription = report!!.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Close button
                    IconButton(
                        onClick = { showPhotoFullScreen = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(40.dp)
                            .background(White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

private fun formatDate(date: String): String {
    // Simplified date formatting
    return date.take(10)
}
