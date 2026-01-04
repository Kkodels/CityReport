package id.antasari.cityreport.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import coil.compose.AsyncImage
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.OsmMapView
import id.antasari.cityreport.ui.theme.*
import id.antasari.cityreport.utils.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportDetailScreen(
    reportId: String,
    onBack: () -> Unit,
    onReportDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    
    var report by remember { mutableStateOf<Report?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Edit states
    var selectedStatus by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var isUploadingCompletionPhoto by remember { mutableStateOf(false) }
    var completionPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val storageRepository = remember { id.antasari.cityreport.data.repository.StorageRepository() }
    
    // Photo picker for completion photo
    val completionPhotoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            completionPhotoUri = it
            // Upload immediately
            scope.launch {
                isUploadingCompletionPhoto = true
                try {
                    val uploadResult = storageRepository.uploadPhoto(context, it)
                    if (uploadResult.isSuccess) {
                        val photoId = uploadResult.getOrNull()
                        if (photoId != null) {
                            val updateResult = reportsRepository.updateCompletionPhoto(reportId, photoId)
                            if (updateResult.isSuccess) {
                                report = updateResult.getOrNull()
                                showSuccessMessage = true
                            }
                        }
                    }
                } finally {
                    isUploadingCompletionPhoto = false
                }
            }
        }
    }

    // Load report
    LaunchedEffect(reportId) {
        scope.launch {
            val result = reportsRepository.getReportById(reportId)
            
            if (result.isSuccess) {
                report = result.getOrNull()
                selectedStatus = report?.status ?: "Baru"
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            
            isLoading = false
        }
    }

    Scaffold(
        containerColor = AdaptiveColors.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Detail Laporan",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdaptiveColors.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Gagal memuat laporan",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                report != null -> {
                    val currentReport = report!!
                    val hasChanges = selectedStatus != currentReport.status
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Photo Section
                        PhotoSection(report = currentReport)
                        
                        // Content
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header Card - Status & ID
                            HeaderCard(
                                report = currentReport,
                                selectedStatus = selectedStatus
                            )
                            
                            // Admin Actions Card
                            AdminActionsCard(
                                selectedStatus = selectedStatus,
                                isSaving = isSaving,
                                hasChanges = hasChanges,
                                onStatusChange = { selectedStatus = it },
                                onSave = {
                                    isSaving = true
                                    scope.launch {
                                        val result = reportsRepository.updateReportStatus(
                                            reportId = reportId,
                                            status = selectedStatus,
                                            priority = currentReport.priority
                                        )
                                        
                                        isSaving = false
                                        if (result.isSuccess) {
                                            showSuccessMessage = true
                                            val updated = reportsRepository.getReportById(reportId)
                                            if (updated.isSuccess) {
                                                report = updated.getOrNull()
                                            }
                                        }
                                    }
                                },
                                onDelete = { showDeleteDialog = true }
                            )
                            
                            // Description Card
                            DescriptionCard(description = currentReport.description)
                            
                            // Completion Photo Upload (Admin only, when status is Selesai)
                            if (currentReport.status == "Selesai") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = BackgroundWhite,
                                    shadowElevation = 2.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            "Foto Hasil Perbaikan",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        
                                        if (currentReport.completionPhotoId.isNullOrEmpty() || currentReport.completionPhotoId == "") {
                                            // Upload button
                                            Button(
                                                onClick = {
                                                    completionPhotoPicker.launch(
                                                        androidx.activity.result.PickVisualMediaRequest(
                                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = !isUploadingCompletionPhoto,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Primary
                                                )
                                            ) {
                                                if (isUploadingCompletionPhoto) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(18.dp),
                                                        color = Color.White,
                                                        strokeWidth = 2.dp
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Mengunggah...")
                                                } else {
                                                    Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Upload Foto Hasil")
                                                }
                                            }
                                            
                                            Text(
                                                "Upload foto setelah perbaikan selesai untuk perbandingan",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextTertiary
                                            )
                                        } else {
                                            // Show uploaded completion photo
                                            val completionPhotoUrl = storageRepository.getPhotoUrl(currentReport.completionPhotoId!!)
                                            
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            ) {
                                                coil.compose.AsyncImage(
                                                    model = completionPhotoUrl,
                                                    contentDescription = "Foto hasil perbaikan",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            }
                                            
                                            Button(
                                                onClick = {
                                                    completionPhotoPicker.launch(
                                                        androidx.activity.result.PickVisualMediaRequest(
                                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.outlinedButtonColors(),
                                                border = BorderStroke(1.dp, Primary)
                                            ) {
                                                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Ganti Foto")
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Location Card
                            LocationCard(report = currentReport)
                            
                            // Reporter Info Card
                            ReporterInfoCard(report = currentReport)
                            
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = { 
                    Text(
                        "Hapus Laporan?",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = { 
                    Text("Laporan yang dihapus tidak dapat dikembalikan. Tindakan ini bersifat permanen.") 
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            scope.launch {
                                val result = reportsRepository.deleteReport(reportId)
                                if (result.isSuccess) {
                                    onReportDeleted()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
        
        // Success Snackbar
        if (showSuccessMessage) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showSuccessMessage = false
            }
        }
    }
}

@Composable
private fun PhotoSection(report: Report) {
    val photoUrl = if (!report.photoId.isNullOrEmpty()) {
        "https://sgp.cloud.appwrite.io/v1/storage/buckets/693fc034000a342e577c/files/${report.photoId}/view?project=693ac9810019b47f348e"
    } else null
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = report.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceGray),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Tidak ada foto",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(
    report: Report,
    selectedStatus: String
) {
    val statusColor = when (selectedStatus) {
        "Baru" -> Color(0xFF2196F3)
        "Diproses" -> Color(0xFFFFA726)
        "Selesai" -> Color(0xFF4CAF50)
        else -> TextSecondary
    }
    
    val severityColor = when {
        report.severity >= 4 -> Color(0xFFE53935)
        report.severity >= 3 -> Color(0xFFFFA726)
        else -> Color(0xFF4CAF50)
    }
    
    val severityLabel = when {
        report.severity >= 4 -> "Mendesak"
        report.severity >= 3 -> "Sedang"
        else -> "Rendah"
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = selectedStatus.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    
                    // Severity Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = severityColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = severityLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = severityColor
                        )
                    }
                }
                
                // Report ID
                Text(
                    text = "#${report.id.take(6).uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary
                )
            }
            
            // Title
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            // Category + Date
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = report.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatDate(report.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminActionsCard(
    selectedStatus: String,
    isSaving: Boolean,
    hasChanges: Boolean,
    onStatusChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Primary.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Tindakan Admin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            // Status selector chips
            Text(
                "Ubah Status",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Constants.STATUSES) { status ->
                    val isSelected = selectedStatus == status
                    val chipColor = when (status) {
                        "Baru" -> Color(0xFF2196F3)
                        "Diproses" -> Color(0xFFFFA726)
                        "Selesai" -> Color(0xFF4CAF50)
                        else -> Primary
                    }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusChange(status) },
                        label = { Text(status) },
                        enabled = !isSaving,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor,
                            selectedLabelColor = White
                        )
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Save button
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = hasChanges && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (isSaving) "Menyimpan..." else "Simpan")
                }
                
                // Delete button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53935)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Hapus")
                }
            }
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Deskripsi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = description.ifEmpty { "Tidak ada deskripsi" },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
            )
        }
    }
}

@Composable
private fun LocationCard(report: Report) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Lokasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            // Map Preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                OsmMapView(
                    initialLat = report.latitude,
                    initialLng = report.longitude,
                    isInteractive = false
                )
            }
            
            // Address
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = report.locationName.ifBlank { report.address },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Koordinat: ${String.format("%.6f", report.latitude)}, ${String.format("%.6f", report.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReporterInfoCard(report: Report) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Informasi Pelapor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "User ${report.userId.take(8)}...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "ID: ${report.userId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            
            Divider(color = Gray200)
            
            // Stats Row - only show severity since votes/comments aren't in model
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Kategori",
                    value = report.category.take(10)
                )
                StatItem(
                    label = "Prioritas",
                    value = report.priority
                )
                StatItem(
                    label = "Severity",
                    value = report.severity.toString()
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

private fun formatDate(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val date = inputFormat.parse(createdAt)
        date?.let { outputFormat.format(it) } ?: createdAt.take(10)
    } catch (e: Exception) {
        createdAt.take(10)
    }
}
