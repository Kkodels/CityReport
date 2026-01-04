package id.antasari.cityreport.ui.report

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.data.repository.StorageRepository
import id.antasari.cityreport.ui.components.CategorySelectionCard
import id.antasari.cityreport.ui.components.PrimaryButton
import id.antasari.cityreport.ui.components.RoundedTextField
import id.antasari.cityreport.ui.components.SecondaryButton
import id.antasari.cityreport.ui.theme.*
import id.antasari.cityreport.utils.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportFormScreen(
    onNavigateBack: () -> Unit,
    onReportSubmitted: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    val storageRepository = remember { StorageRepository() }
    val authRepository = remember { AuthRepository() }
    val locationHelper = remember { LocationHelper(context) }
    val geocodingHelper = remember { id.antasari.cityreport.utils.GeocodingHelper(context) }
    
    var currentStep by remember { mutableStateOf(1) }
    
    // Step 1: Category
    var selectedCategory by remember { mutableStateOf("") }
    
    // Step 2: Location & Details
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(-6.2088) }
    var longitude by remember { mutableStateOf(106.8456) }
    var locationName by remember { mutableStateOf("Memuat lokasi...") }
    
    // Step 3: Photo
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Step 4: Priority (optional)
    var severity by remember { mutableStateOf(3) } // 1-5 scale
    
    var isLoading by remember { mutableStateOf(false) }
    
    // Gallery picker
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> 
        uri?.let { photoUri = it }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = tempPhotoUri
        }
    }
    
    // Create temp file for camera
    fun createTempImageFile(): Uri? {
        return try {
            val file = java.io.File(
                context.cacheDir,
                "JPEG_${System.currentTimeMillis()}.jpg"
            )
            file.createNewFile()
            android.util.Log.d("Camera", "Created temp file: ${file.absolutePath}")
            
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            android.util.Log.e("Camera", "Failed to create temp file: ${e.message}", e)
            null
        }
    }
    
    // Location Permission state
    var hasLocationPermission by remember { mutableStateOf(locationHelper.hasLocationPermission()) }
    
    // Camera permission state
    var hasCameraPermission by remember { 
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var pendingCameraLaunch by remember { mutableStateOf(false) }
    
    // Location Permission request launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.getOrDefault(
            android.Manifest.permission.ACCESS_FINE_LOCATION, false
        ) || permissions.getOrDefault(
            android.Manifest.permission.ACCESS_COARSE_LOCATION, false
        )
        
        android.util.Log.d("GPS", "Permission result: $hasLocationPermission")
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        android.util.Log.d("Camera", "Camera permission: $granted")
        
        // Launch camera if permission granted and was pending
        if (granted && pendingCameraLaunch) {
            pendingCameraLaunch = false
            val uri = createTempImageFile()
            if (uri != null) {
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }
    
    // Request location permission on launch if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // Get current GPS location when permission granted
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            scope.launch {
                try {
                    android.util.Log.d("GPS", "Fetching location...")
                    val loc = locationHelper.getCurrentLocation()
                    android.util.Log.d("GPS", "Location: $loc")
                    
                    if (loc != null) {
                        latitude = loc.first
                        longitude = loc.second
                        android.util.Log.d("GPS", "Coordinates: ${loc.first}, ${loc.second}")
                        
                        // Reverse geocode to get address name
                        val address = geocodingHelper.getAddressFromLocation(loc.first, loc.second)
                        android.util.Log.d("GPS", "Address: $address")
                        locationName = address ?: "${"%.4f".format(loc.first)}, ${"%.4f".format(loc.second)}"
                    } else {
                        android.util.Log.e("GPS", "Location is null - check GPS enabled")
                        locationName = "Aktifkan GPS di settings"
                    }
                } catch (e: Exception) {
                    android.util.Log.e("GPS", "Error: ${e.message}", e)
                    locationName = "Error lokasi"
                }
            }
        } else {
            locationName = "Izin lokasi diperlukan"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Laporan Baru", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Tutup")
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
            // Progress Indicator
            LinearProgressIndicator(
                progress = { currentStep / 4f },
                modifier = Modifier.fillMaxWidth(),
                color = Primary,
                trackColor = Gray300
            )
            
            // Step indicator text
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.Medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (currentStep == 1) "Langkah 1: Pilih Kategori"
                    else if (currentStep == 2) "Langkah 2: Detail Laporan"
                    else if (currentStep == 3) "Langkah 3: Tambah Foto"
                    else "Langkah 4: Review",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$currentStep dari 4",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary
                )
            }
            
            // Content
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (currentStep) {
                    1 -> CategoryStep(selectedCategory) { selectedCategory = it }
                    2 -> DetailsStep(
                        title = title,
                        desc = description,
                        loc = locationName,
                        currentLat = latitude,
                        currentLng = longitude,
                        onTitleChange = { title = it },
                        onDescChange = { description = it },
                        onLocationChange = { lat, lng ->
                            latitude = lat
                            longitude = lng
                            // Update location name via reverse geocoding
                            scope.launch {
                                locationName = "Memuat alamat..."
                                val address = geocodingHelper.getAddressFromLocation(lat, lng)
                                locationName = address ?: "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                            }
                        }
                    )
                    3 -> PhotoStep(
                        photoUri = photoUri,
                        onTakePhoto = {
                            if (hasCameraPermission) {
                                // Permission granted, launch camera
                                val uri = createTempImageFile()
                                if (uri != null) {
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    android.util.Log.e("Camera", "Failed to create temp file")
                                }
                            } else {
                                // Request permission first
                                pendingCameraLaunch = true
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        onSelectFromGallery = {
                            photoLauncher.launch("image/*")
                        }
                    )
                    4 -> ReviewStep(selectedCategory, title, description, locationName, photoUri, severity, { severity = it })
                }
            }
            
            // Bottom Navigation Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = Elevation.Medium,
                color = AdaptiveColors.card
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.Medium),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    if (currentStep > 1) {
                        SecondaryButton(
                            text = "Kembali",
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    PrimaryButton(
                        text = if (currentStep == 4) "Kirim Laporan" else "Lanjut",
                        onClick = {
                            when (currentStep) {
                                1 -> {
                                    if (selectedCategory.isBlank()) {
                                        Toast.makeText(context, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
                                    } else {
                                        currentStep++
                                    }
                                }
                                2 -> {
                                    if (title.isBlank() || description.isBlank()) {
                                        Toast.makeText(context, "Isi judul dan deskripsi", Toast.LENGTH_SHORT).show()
                                    } else {
                                        currentStep++
                                    }
                                }
                                3 -> currentStep++ // Photo optional
                                4 -> {
                                    // Submit
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            // Get current user
                                            val userResult = authRepository.getCurrentUserWithRole()
                                            val userId = userResult.getOrNull()?.userId ?: ""
                                            
                                            // Upload photo with compression if exists
                                            var photoId: String? = null
                                            if (photoUri != null) {
                                                android.util.Log.d("ReportSubmit", "Uploading photo: $photoUri")
                                                val uploadResult = storageRepository.uploadPhotoCompressed(context, photoUri!!)
                                                if (uploadResult.isSuccess) {
                                                    photoId = uploadResult.getOrNull()
                                                    android.util.Log.d("ReportSubmit", "Photo uploaded! ID: $photoId")
                                                } else {
                                                    android.util.Log.e("ReportSubmit", "Photo upload failed: ${uploadResult.exceptionOrNull()?.message}")
                                                }
                                            } else {
                                                android.util.Log.d("ReportSubmit", "No photo to upload")
                                            }
                                            
                                            android.util.Log.d("ReportSubmit", "Creating report with photoId: $photoId")
                                            
                                            // Create report
                                            val result = reportsRepository.createReport(
                                                title = title,
                                                description = description,
                                                category = selectedCategory,
                                                severity = severity,
                                                latitude = latitude,
                                                longitude = longitude,
                                                locationName = locationName,
                                                photoId = photoId,
                                                userId = userId
                                            )
                                            
                                            if (result.isSuccess) {
                                                val report = result.getOrNull()
                                                android.util.Log.d("ReportSubmit", "Report created! ID: ${report?.id}, photoId: ${report?.photoId}")
                                                Toast.makeText(context, "✅ Laporan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                                onReportSubmitted(report?.id ?: "")
                                            } else {
                                                android.util.Log.e("ReportSubmit", "Report creation failed: ${result.exceptionOrNull()?.message}")
                                                Toast.makeText(context, "❌ Gagal membuat laporan", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("ReportSubmit", "Error: ${e.message}", e)
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        isLoading = isLoading && currentStep == 4,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryStep(selected: String, onSelect: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.Medium)
    ) {
        Text(
            "Apa yang ingin Anda laporkan?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        
        Spacer(Modifier.height(Spacing.Small))
        
        Text(
            "Pilih kategori yang paling sesuai dengan masalah di sekitar Anda.",
            style = MaterialTheme.typography.bodyMedium,
            color = AdaptiveColors.textSecondary
        )
        
        Spacer(Modifier.height(Spacing.Large))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
            modifier = Modifier.heightIn(min = 400.dp, max = 600.dp)
        ) {
            items(listOf(
                Pair("Jalan Rusak", "Infrastruktur"),
                Pair("Sampah", "Kebersihan"),
                Pair("Banjir", "Bencana"),
                Pair("Lampu Jalan", "Penerangan"),
                Pair("Fasilitas", "Taman & Kota"),
                Pair("Lainnya", "Kategori Umum")
            )) { (cat, subtitle) ->
                CategorySelectionCard(
                    title = cat,
                    subtitle = subtitle,
                    icon = id.antasari.cityreport.utils.CategoryIcons.getIconForCategory(cat),
                    iconColor = id.antasari.cityreport.utils.CategoryIcons.getColorForCategory(cat),
                    backgroundColor = id.antasari.cityreport.utils.CategoryIcons.getBackgroundColorForCategory(cat),
                    isSelected = selected == cat,
                    onClick = { onSelect(cat) }
                )
            }
        }
    }
}

@Composable
private fun DetailsStep(
    title: String,
    desc: String,
    loc: String,
    currentLat: Double,
    currentLng: Double,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onLocationChange: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<org.osmdroid.views.MapView?>(null) }
    var marker by remember { mutableStateOf<org.osmdroid.views.overlay.Marker?>(null) }
    var isManualLocation by remember { mutableStateOf(false) }
    
    // Configure OSMDroid
    DisposableEffect(Unit) {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }
    
    // Update marker when coordinates change
    LaunchedEffect(currentLat, currentLng, mapView) {
        mapView?.let { map ->
            // Remove old marker
            marker?.let { map.overlays.remove(it) }
            
            // Add new marker at current location
            val newMarker = org.osmdroid.views.overlay.Marker(map).apply {
                position = org.osmdroid.util.GeoPoint(currentLat, currentLng)
                setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                setTitle(if (isManualLocation) "Lokasi Dipilih Manual" else "Lokasi dari GPS")
                icon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
            }
            map.overlays.add(newMarker)
            marker = newMarker
            
            // Center map on location
            map.controller.animateTo(org.osmdroid.util.GeoPoint(currentLat, currentLng))
            map.invalidate()
        }
    }
    
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        Text(
            "Detail Laporan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Jelaskan masalah yang Anda temukan dengan detail.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(Modifier.height(Spacing.Small))
        
        RoundedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = "Judul laporan...",
            modifier = Modifier.fillMaxWidth()
        )
        
        RoundedTextField(
            value = desc,
            onValueChange = onDescChange,
            placeholder = "Deskripsi lengkap masalah...",
            modifier = Modifier.fillMaxWidth().height(120.dp),
            singleLine = false
        )
        
        // Location Section
        Text(
            "Lokasi Laporan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Icon(
                if (isManualLocation) Icons.Default.TouchApp else Icons.Default.MyLocation,
                null,
                tint = if (isManualLocation) Primary else StatusSelesai,
                modifier = Modifier.size(16.dp)
            )
            Text(
                if (isManualLocation) "📍 Tap peta untuk pilih lokasi manual" else "🟢 Menggunakan lokasi GPS Anda",
                style = MaterialTheme.typography.bodySmall,
                color = if (isManualLocation) Primary else StatusSelesai,
                fontWeight = FontWeight.Medium
            )
        }
        
        // OSMDroid Map
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.Medium),
            shadowElevation = Elevation.Low
        ) {
            AndroidView(
                factory = { ctx ->
                    org.osmdroid.views.MapView(ctx).apply {
                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        
                        // Set initial position with current GPS
                        controller.setZoom(15.0)
                        controller.setCenter(org.osmdroid.util.GeoPoint(currentLat, currentLng))
                        
                        // Add initial marker
                        val initialMarker = org.osmdroid.views.overlay.Marker(this).apply {
                            position = org.osmdroid.util.GeoPoint(currentLat, currentLng)
                            setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                            setTitle("Lokasi dari GPS")
                        }
                        overlays.add(initialMarker)
                        marker = initialMarker
                        
                        // Map tap listener for manual location selection
                        val mapEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
                            override fun singleTapConfirmedHelper(geoPoint: org.osmdroid.util.GeoPoint?): Boolean {
                                geoPoint?.let {
                                    isManualLocation = true
                                    onLocationChange(it.latitude, it.longitude)
                                }
                                return true
                            }
                            
                            override fun longPressHelper(p: org.osmdroid.util.GeoPoint?): Boolean {
                                return false
                            }
                        }
                        val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
                        overlays.add(0, mapEventsOverlay)
                        
                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Coordinates Display with GPS/Manual indicator
        Surface(
            Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.Medium),
            color = SurfaceGray
        ) {
            Column(Modifier.padding(Spacing.Medium)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Primary)
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    loc.ifBlank { "Menggunakan GPS..." },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                // Location type indicator
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = if (isManualLocation) Primary else StatusSelesai,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                            Text(
                                "Lat: ${"%.6f".format(currentLat)}, Lng: ${"%.6f".format(currentLng)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                    
                    // Reset to GPS button
                    if (isManualLocation) {
                        TextButton(onClick = {
                            isManualLocation = false
                            // Re-get GPS location would go here
                            // For now just toggle indicator
                        }) {
                            Icon(Icons.Default.MyLocation, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("GPS", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        IconButton(onClick = {
                            // Re-center map to current GPS location
                            mapView?.controller?.animateTo(org.osmdroid.util.GeoPoint(currentLat, currentLng))
                        }) {
                            Icon(Icons.Default.MyLocation, "GPS Location", tint = Primary)
                        }
                    }
                }
                
                // Help text
                if (!isManualLocation) {
                    Spacer(Modifier.height(Spacing.ExtraSmall))
                    Text(
                        "💡 Tap peta untuk pilih lokasi secara manual",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoStep(
    photoUri: Uri?,
    onTakePhoto: () -> Unit,
    onSelectFromGallery: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        Text(
            "Tambahkan Foto",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Foto akan membantu mempercepat penanganan laporan (opsional).",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(Modifier.height(Spacing.Medium))
        
        if (photoUri != null) {
            // Show selected image
            AsyncImage(
                model = photoUri,
                contentDescription = "Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        SurfaceGray,
                        androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.Medium)
                    ),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            Spacer(Modifier.height(Spacing.Medium))
            
            // Options to change
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                OutlinedButton(
                    onClick = onTakePhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(Modifier.width(Spacing.Small))
                    Text("Ambil Ulang")
                }
                
                OutlinedButton(
                    onClick = onSelectFromGallery,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(Modifier.width(Spacing.Small))
                    Text("Pilih Lain")
                }
            }
        } else {
            // Upload options
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                // Camera button
                Button(
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(48.dp))
                        Text(
                            "Ambil Foto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Gunakan kamera",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Gallery button
                OutlinedButton(
                    onClick = onSelectFromGallery,
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        Icon(Icons.Default.Image, null, Modifier.size(48.dp), tint = Primary)
                        Text(
                            "Pilih dari Galeri",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            "Pilih foto yang ada",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Text(
                    "💡 Tip: Pastikan foto jelas dan fokus pada masalah",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = Spacing.Small)
                )
            }
        }
    }
}

@Composable
private fun ReviewStep(
    category: String, title: String, desc: String, location: String, photo: Uri?,
    severity: Int, onSeverityChange: (Int) -> Unit
) {
    
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        Text(
            "Review Laporan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Pastikan semua informasi sudah benar sebelum mengirim.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(Modifier.height(Spacing.Medium))
        
        // Summary card
        Surface(
            Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.Medium),
            color = AdaptiveColors.card,
            shadowElevation = Elevation.Low
        ) {
            Column(Modifier.padding(Spacing.Medium), verticalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
                ReviewItem("Kategori", category)
                HorizontalDivider()
                ReviewItem("Judul", title)
                HorizontalDivider()
                ReviewItem("Deskripsi", desc)
                HorizontalDivider()
                ReviewItem("Lokasi", location)
                
                // Photo preview
                if (photo != null) {
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                        Text(
                            "Foto",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.Small)
                        ) {
                            coil.compose.AsyncImage(
                                model = photo,
                                contentDescription = "Preview foto laporan",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(Spacing.Large))
        
        // Urgency Level Selector
        Text(
            "Tingkat Urgensi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            "Seberapa mendesak masalah ini?",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        
        Spacer(Modifier.height(Spacing.Small))
        
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            UrgencyButton(
                label = "Rendah",
                level = 1,
                isSelected = severity == 1,
                color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                onClick = { onSeverityChange(1) },
                modifier = Modifier.weight(1f)
            )
            UrgencyButton(
                label = "Sedang",
                level = 3,
                isSelected = severity == 3,
                color = androidx.compose.ui.graphics.Color(0xFFFFA726),
                onClick = { onSeverityChange(3) },
                modifier = Modifier.weight(1f)
            )
            UrgencyButton(
                label = "Tinggi",
                level = 5,
                isSelected = severity >= 4,
                color = androidx.compose.ui.graphics.Color(0xFFFF5722),
                onClick = { onSeverityChange(5) },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Help text
        Text(
            when {
                severity <= 1 -> "🟢 Masalah ringan, tidak mendesak"
                severity <= 3 -> "🟡 Perlu perhatian dalam beberapa hari"
                else -> "🔴 Sangat mendesak, perlu penanganan segera"
            },
            style = MaterialTheme.typography.bodySmall,
            color = when {
                severity <= 1 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                severity <= 3 -> androidx.compose.ui.graphics.Color(0xFFFFA726)
                else -> androidx.compose.ui.graphics.Color(0xFFFF5722)
            },
            modifier = Modifier.padding(top = Spacing.ExtraSmall)
        )
    }
}

@Composable
private fun UrgencyButton(
    label: String,
    level: Int,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else SurfaceGray,
            contentColor = if (isSelected) White else TextSecondary
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.Small)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ReviewItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
