package id.antasari.cityreport.ui.report

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.PrimaryButton
import id.antasari.cityreport.ui.components.SecondaryButton
import id.antasari.cityreport.ui.components.StatusBadge
import id.antasari.cityreport.ui.components.VotingSection
import id.antasari.cityreport.ui.components.CommentsSection
import id.antasari.cityreport.ui.components.QRCodeDialog
import id.antasari.cityreport.ui.components.BeforeAfterSlider
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewReportDetailScreen(
    reportId: String,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    val storageRepository = remember { id.antasari.cityreport.data.repository.StorageRepository() }
    val interactionRepository = remember { id.antasari.cityreport.data.repository.InteractionRepository() }
    val authRepository = remember { id.antasari.cityreport.data.repository.AuthRepository() }
    val votingRepository = remember { id.antasari.cityreport.data.repository.VotingRepository() }
    
    var report by remember { mutableStateOf<Report?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var supportCount by remember { mutableStateOf(0) }
    var hasSupported by remember { mutableStateOf(false) }
    var commentCount by remember { mutableStateOf(0) }
    var currentUserId by remember { mutableStateOf("") }
    var showQRDialog by remember { mutableStateOf(false) }
    var isVoting by remember { mutableStateOf(false) }
    
    // Load report and interaction data
    LaunchedEffect(reportId) {
        scope.launch {
            // Get current user
            val userResult = authRepository.getCurrentUserWithRole()
            currentUserId = userResult.getOrNull()?.userId ?: ""
            
            // Load report
            val result = reportsRepository.getReportById(reportId)
            if (result.isSuccess) {
                report = result.getOrNull()
            }
            
            // Load vote count and user vote status
            supportCount = interactionRepository.getVoteCount(reportId).getOrDefault(0)
            hasSupported = interactionRepository.hasUserVoted(reportId, currentUserId).getOrDefault(false)
            
            // Load comment count
            commentCount = interactionRepository.getCommentCount(reportId).getOrDefault(0)
            
            isLoading = false
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }
    
    if (report == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Report not found", color = TextSecondary)
        }
        return
    }
    
    val currentReport = report!!
    val context = LocalContext.current
    
    // Share function
    val shareReport = {
        val shareText = """
📍 CityReport - Laporan Warga

📋 ${currentReport.title}
📂 Kategori: ${currentReport.category}
📍 Lokasi: ${currentReport.locationName}
📝 ${currentReport.description.take(200)}${if (currentReport.description.length > 200) "..." else ""}

Status: ${currentReport.status}

#CityReport #LaporanWarga
        """.trimIndent()
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Bagikan laporan ke..."))
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdaptiveColors.background)
    ) {
        // Header with image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                // Cover image from report
                if (!currentReport.photoId.isNullOrEmpty()) {
                    val photoUrl = storageRepository.getPhotoUrl(currentReport.photoId)
                    android.util.Log.d("ReportDetail", "Photo ID: ${currentReport.photoId}")
                    android.util.Log.d("ReportDetail", "Photo URL: $photoUrl")
                    
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(photoUrl)
                            .crossfade(true)
                            .listener(
                                onError = { _, result ->
                                    android.util.Log.e("ReportDetail", "Photo error: ${result.throwable.message}")
                                },
                                onSuccess = { _, _ ->
                                    android.util.Log.d("ReportDetail", "Photo loaded!")
                                }
                            )
                            .build(),
                        contentDescription = "Foto laporan",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    android.util.Log.d("ReportDetail", "No photo - showing placeholder")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(Spacing.Medium),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .shadow(Elevation.Medium, RoundedCornerShape(CornerRadius.Small))
                            .background(AdaptiveColors.surface, RoundedCornerShape(CornerRadius.Small))
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AdaptiveColors.textPrimary)
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showQRDialog = true },
                            modifier = Modifier
                                .shadow(Elevation.Medium, RoundedCornerShape(CornerRadius.Small))
                                .background(AdaptiveColors.surface, RoundedCornerShape(CornerRadius.Small))
                        ) {
                            Icon(Icons.Default.QrCode2, "QR Code", tint = AdaptiveColors.textPrimary)
                        }
                        
                        IconButton(
                            onClick = shareReport,
                            modifier = Modifier
                                .shadow(Elevation.Medium, RoundedCornerShape(CornerRadius.Small))
                                .background(AdaptiveColors.surface, RoundedCornerShape(CornerRadius.Small))
                        ) {
                            Icon(Icons.Default.Share, "Share", tint = AdaptiveColors.textPrimary)
                        }
                    }
                }
                
                // Image counter - hidden since we only have one photo
                // Uncomment when multiple images supported
            }
        }
        
        // Content
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium)
                    .padding(top = Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                // Header Card - Status, Title, Reporter
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AdaptiveColors.card,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        // Status, Urgency & ID Row
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = currentReport.status)
                                
                                // Urgency badge based on severity
                                val urgencyLabel = when {
                                    currentReport.severity >= 4 -> "Mendesak"
                                    currentReport.severity >= 3 -> "Sedang"
                                    currentReport.severity >= 2 -> "Rendah"
                                    else -> null
                                }
                                if (urgencyLabel != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            currentReport.severity >= 4 -> androidx.compose.ui.graphics.Color(0xFFFFE5E5)
                                            currentReport.severity >= 3 -> androidx.compose.ui.graphics.Color(0xFFFFF4E5)
                                            else -> androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                                        }
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                when {
                                                    currentReport.severity >= 4 -> "🔴"
                                                    currentReport.severity >= 3 -> "🟡"
                                                    else -> "🟢"
                                                },
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            Text(
                                                urgencyLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = when {
                                                    currentReport.severity >= 4 -> androidx.compose.ui.graphics.Color(0xFFFF5722)
                                                    currentReport.severity >= 3 -> androidx.compose.ui.graphics.Color(0xFFFFA726)
                                                    else -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                                },
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "#${currentReport.id.takeLast(4).uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextTertiary
                            )
                        }
                        
                        // Title
                        Text(
                            text = currentReport.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AdaptiveColors.textPrimary
                        )
                        
                        // Reporter & time
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AdaptiveColors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Reporter",
                                style = MaterialTheme.typography.bodySmall,
                                color = AdaptiveColors.textSecondary
                            )
                            Text("•", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = formatDate(currentReport.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = AdaptiveColors.textSecondary
                            )
                        }
                    }
                }
                
                // Action Buttons Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AdaptiveColors.card,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.Medium),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        // Support button
                        Button(
                            onClick = {
                                if (!isVoting && currentUserId.isNotEmpty()) {
                                    isVoting = true
                                    scope.launch {
                                        if (hasSupported) {
                                            interactionRepository.removeVote(reportId, currentUserId)
                                            hasSupported = false
                                            supportCount = (supportCount - 1).coerceAtLeast(0)
                                        } else {
                                            interactionRepository.addVote(reportId, currentUserId)
                                            hasSupported = true
                                            supportCount++
                                        }
                                        isVoting = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasSupported) 
                                    androidx.compose.ui.graphics.Color(0xFF4CAF50) 
                                else Primary
                            ),
                            enabled = !isVoting,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isVoting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (hasSupported) 
                                        Icons.Default.Favorite 
                                    else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (supportCount > 0) "Support $supportCount" else "Support",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                        
                        // Comment button
                        OutlinedButton(
                            onClick = { 
                                val encodedTitle = java.net.URLEncoder.encode(currentReport.title, "UTF-8")
                                onNavigate("${id.antasari.cityreport.ui.navigation.Routes.COMMENTS_BASE}/$reportId/$encodedTitle")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (commentCount > 0) "Comment $commentCount" else "Comment",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // Description Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AdaptiveColors.card,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        Text(
                            text = "Deskripsi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AdaptiveColors.textPrimary
                        )
                        Text(
                            text = currentReport.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.textSecondary,
                            lineHeight = 22.sp
                        )
                    }
                }
                
                // Location Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AdaptiveColors.card,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        Text(
                            text = "Lokasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AdaptiveColors.textPrimary
                        )
                        
                        // Map
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    org.osmdroid.views.MapView(ctx).apply {
                                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                        setMultiTouchControls(true)
                                        controller.setZoom(16.0)
                                        controller.setCenter(org.osmdroid.util.GeoPoint(
                                            currentReport.latitude,
                                            currentReport.longitude
                                        ))
                                        
                                        val marker = org.osmdroid.views.overlay.Marker(this)
                                        marker.position = org.osmdroid.util.GeoPoint(
                                            currentReport.latitude,
                                            currentReport.longitude
                                        )
                                        marker.setAnchor(
                                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, 
                                            org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                                        )
                                        marker.title = currentReport.title
                                        overlays.add(marker)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Address
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = currentReport.locationName.ifEmpty { "Lokasi Laporan" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = AdaptiveColors.textPrimary
                                )
                                Text(
                                    text = "${"%.6f".format(currentReport.latitude)}, ${"%.6f".format(currentReport.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }
                
                // Status Timeline Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AdaptiveColors.card,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                    ) {
                        Text(
                            text = "Riwayat Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AdaptiveColors.textPrimary
                        )
                        StatusTimeline(currentReport.status)
                    }
                }
            }
        }
        
        // Before/After Photo Comparison (if completion photo exists)
        item {
            if (currentReport.completionPhotoId != null && currentReport.photoId != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(CornerRadius.Medium),
                    shadowElevation = Elevation.Low,
                    color = AdaptiveColors.card
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Perbandingan Sebelum & Sesudah",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AdaptiveColors.textPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        val beforeUrl = "https://sgp.cloud.appwrite.io/v1/storage/buckets/693fc034000a342e577c/files/${currentReport.photoId}/view?project=693ac9810019b47f348e"
                        val afterUrl = "https://sgp.cloud.appwrite.io/v1/storage/buckets/693fc034000a342e577c/files/${currentReport.completionPhotoId}/view?project=693ac9810019b47f348e"
                        
                        BeforeAfterSlider(
                            beforePhotoUrl = beforeUrl,
                            afterPhotoUrl = afterUrl
                        )
                        
                        Text(
                            "💡 Berikut adalah kondisi sebelum dan sesudah perbaikan",
                            style = MaterialTheme.typography.bodySmall,
                            color = AdaptiveColors.textTertiary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(Modifier.height(80.dp))
        }
    }
    
    // QR Code Dialog Overlay
    if (showQRDialog) {
        QRCodeDialog(
            reportId = reportId,
            reportTitle = currentReport.title,
            onDismiss = { showQRDialog = false }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StatusTimeline(currentStatus: String) {
    val statuses = listOf(
        "Laporan Diterima" to true,
        "Verifikasi Petugas" to (currentStatus != "Menunggu"),
        "Sedang Dikerjakan" to (currentStatus == "Diproses" || currentStatus == "Selesai"),
        "Selesai" to (currentStatus == "Selesai")
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
        statuses.forEach { (status, isCompleted) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = if (isCompleted) Primary else Gray300
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Column {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isCompleted) TextPrimary else TextSecondary
                    )
                    if (isCompleted) {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(createdAt: String): String {
    return try {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(createdAt)?.time 
            ?: return createdAt
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")).format(Date(timestamp))
    } catch (e: Exception) {
        createdAt
    }
}
