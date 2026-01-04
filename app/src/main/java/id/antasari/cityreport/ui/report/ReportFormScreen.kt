package id.antasari.cityreport.ui.report

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.data.repository.StorageRepository
import id.antasari.cityreport.ui.components.OsmMapView
import id.antasari.cityreport.ui.components.PremiumButton
import id.antasari.cityreport.ui.components.PremiumTextField
import id.antasari.cityreport.ui.components.OutlinedPremiumButton
import id.antasari.cityreport.ui.components.SeverityRating
import id.antasari.cityreport.ui.components.PhotoPicker
import id.antasari.cityreport.ui.theme.Primary
import id.antasari.cityreport.ui.theme.Success
import id.antasari.cityreport.ui.theme.Error
import id.antasari.cityreport.utils.Constants
import id.antasari.cityreport.utils.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    onBack: () -> Unit,
    onReportSubmitted: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Repositories
    val authRepository = remember { AuthRepository() }
    val reportsRepository = remember { ReportsRepository() }
    val storageRepository = remember { StorageRepository() }
    val locationHelper = remember { LocationHelper(context) }
    
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Constants.CATEGORIES[0]) }
    var selectedPriority by remember { mutableStateOf(Constants.PRIORITIES[1]) } // Default: Sedang
    var selectedSeverity by remember { mutableStateOf(3) } // 1-5 scale, default medium
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationName by remember { mutableStateOf("") }
    
    // UI state
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    
    // Photo upload state
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedPhotoId by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    
    // Location permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            // Get location after permission granted
            scope.launch {
                val location = locationHelper.getCurrentLocation()
                if (location != null) {
                    latitude = location.first
                    longitude = location.second
                    locationName = "Lat: ${location.first}, Lng: ${location.second}"
                } else {
                    Toast.makeText(context, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Laporan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            PremiumTextField(
                value = title,
                onValueChange = { title = it; errorMessage = null },
                label = "Judul Laporan",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = errorMessage != null && title.isBlank(),
                errorMessage = if (errorMessage != null && title.isBlank()) "Judul harus diisi" else null
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Description
            PremiumTextField(
                value = description,
                onValueChange = { description = it; errorMessage = null },
                label = "Deskripsi",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = errorMessage != null && description.isBlank(),
                errorMessage = if (errorMessage != null && description.isBlank()) "Deskripsi harus diisi" else null,
                singleLine = false,
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded && !isLoading }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    enabled = !isLoading
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    Constants.CATEGORIES.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Priority Dropdown
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = !priorityExpanded && !isLoading }
            ) {
                OutlinedTextField(
                    value = selectedPriority,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Prioritas") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    enabled = !isLoading
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    Constants.PRIORITIES.forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority) },
                            onClick = {
                                selectedPriority = priority
                                priorityExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Severity Rating
            SeverityRating(
                selectedLevel = selectedSeverity,
                onLevelSelected = { selectedSeverity = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Location Name
            PremiumTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = "Nama Lokasi",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Use My Location Button
            OutlinedPremiumButton(
                onClick = {
                    if (locationHelper.hasLocationPermission()) {
                        scope.launch {
                            val location = locationHelper.getCurrentLocation()
                            if (location != null) {
                                latitude = location.first
                                longitude = location.second
                                if (locationName.isBlank()) {
                                    locationName = "Lat: ${location.first}, Lng: ${location.second}"
                                }
                            } else {
                                Toast.makeText(context, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        permissionLauncher.launch(LocationHelper.LOCATION_PERMISSIONS)
                    }
                },
                text = "Gunakan Lokasi Saya",
                icon = Icons.Default.MyLocation,
                enabled = !isLoading,
                color = Primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Map
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                OsmMapView(
                    initialLat = latitude,
                    initialLng = longitude,
                    onLocationChanged = { lat, lng ->
                        latitude = lat
                        longitude = lng
                    },
                    isInteractive = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Photo Upload
            Text(
                text = "Foto Laporan (Opsional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            PhotoPicker(
                selectedUri = selectedPhotoUri,
                onPhotoSelected = { uri ->
                    // Validasi ukuran file
                    if (!storageRepository.validateFileSize(context, uri)) {
                        Toast.makeText(context, "Ukuran file maksimal 5 MB!", Toast.LENGTH_SHORT).show()
                        return@PhotoPicker
                    }
                    
                    selectedPhotoUri = uri
                    uploadError = null
                    
                    // Upload foto ke Appwrite
                    scope.launch {
                        isUploadingPhoto = true
                        val result = storageRepository.uploadPhoto(context, uri)
                        
                        if (result.isSuccess) {
                            uploadedPhotoId = result.getOrNull()
                            Toast.makeText(context, "Foto berhasil diupload!", Toast.LENGTH_SHORT).show()
                        } else {
                            uploadError = result.exceptionOrNull()?.message ?: "Upload gagal"
                            Toast.makeText(context, uploadError, Toast.LENGTH_SHORT).show()
                            selectedPhotoUri = null
                        }
                        
                        isUploadingPhoto = false
                    }
                },
                onPhotoRemoved = {
                    // Hapus foto dari storage jika sudah diupload
                    uploadedPhotoId?.let { fileId ->
                        scope.launch {
                            storageRepository.deletePhoto(fileId)
                        }
                    }
                    selectedPhotoUri = null
                    uploadedPhotoId = null
                    uploadError = null
                },
                isUploading = isUploadingPhoto,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Upload error message
            if (uploadError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uploadError!!,
                    color = Error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            PremiumButton(
                onClick = {
                    // Validation
                    when {
                        title.isBlank() -> errorMessage = "Judul tidak boleh kosong"
                        description.isBlank() -> errorMessage = "Deskripsi tidak boleh kosong"
                        latitude == null || longitude == null -> errorMessage = "Tentukan lokasi laporan"
                        isUploadingPhoto -> errorMessage = "Tunggu upload foto selesai"
                        else -> {
                            isLoading = true
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                
                                val result = authRepository.getCurrentUserWithRole()
                                
                                if (result.isSuccess) {
                                    val profile = result.getOrNull()!!
                                    val reportResult = reportsRepository.createReport(
                                        title = title,
                                        description = description,
                                        category = selectedCategory,
                                        latitude = latitude ?: 0.0,
                                        longitude = longitude ?: 0.0,
                                        locationName = locationName,
                                        userId = profile.userId,
                                        priority = selectedPriority,
                                        severity = selectedSeverity,
                                        photoId = uploadedPhotoId
                                    )
                                    
                                    if (reportResult.isSuccess) {
                                        val reportId = reportResult.getOrNull()!!.id
                                        Toast.makeText(context, "Laporan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                        onReportSubmitted(reportId)
                                    } else {
                                        errorMessage = reportResult.exceptionOrNull()?.message ?: "Gagal membuat laporan"
                                    }
                                } else {
                                    errorMessage = "Gagal mendapatkan data user"
                                }
                                
                                isLoading = false
                            }
                        }
                    }
                },
                text = "Kirim Laporan",
                icon = Icons.AutoMirrored.Filled.Send,
                loading = isLoading,
                enabled = !isLoading,
                color = Success
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

