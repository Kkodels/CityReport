package id.antasari.cityreport.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import id.antasari.cityreport.ui.theme.*
import java.io.File

/**
 * Komponen untuk pilih foto dari galeri ATAU ambil dari kamera
 * 
 * @param selectedUri URI foto yang sudah dipilih (null jika belum ada)
 * @param onPhotoSelected Callback ketika foto dipilih
 * @param onPhotoRemoved Callback ketika foto dihapus
 * @param isUploading Status loading saat upload
 * @param modifier Modifier
 */
@Composable
fun PhotoPicker(
    selectedUri: Uri?,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoRemoved: () -> Unit,
    isUploading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    
    // URI untuk menyimpan foto dari kamera
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Launcher untuk galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPhotoSelected(it) }
    }
    
    // Launcher untuk kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            onPhotoSelected(cameraImageUri!!)
        }
    }
    
    // Launcher untuk camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, buka kamera
            cameraImageUri = createImageUri(context)
            cameraImageUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (selectedUri == null) {
            // Empty state - kotak untuk pilih foto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundSecondary, RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp,
                        color = BorderPrimary,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = !isUploading) {
                        showDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Pilih foto",
                        modifier = Modifier.size(48.dp),
                        tint = TextSecondary
                    )
                    Text(
                        text = "Tap untuk upload foto",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Kamera atau Galeri",
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                    Text(
                        text = "Maksimal 5 MB",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }
        } else {
            // Preview foto yang dipilih
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Preview foto",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Loading overlay
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = White,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Mengupload foto...",
                                color = White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                // Tombol hapus foto (kanan atas)
                if (!isUploading) {
                    IconButton(
                        onClick = onPhotoRemoved,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .background(White.copy(alpha = 0.9f), RoundedCornerShape(18.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus foto",
                            tint = Error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Dialog pilihan: Kamera atau Galeri
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            BlueCard(
                padding = BlueCardPadding.Large,
                elevation = 8
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Pilih Sumber Foto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    // Opsi Kamera
                    PhotoSourceOption(
                        icon = Icons.Default.CameraAlt,
                        title = "Kamera",
                        description = "Ambil foto langsung",
                        onClick = {
                            showDialog = false
                            // Request camera permission
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    )
                    
                    HorizontalDivider(color = BorderSecondary)
                    
                    // Opsi Galeri
                    PhotoSourceOption(
                        icon = Icons.Default.Image,
                        title = "Galeri",
                        description = "Pilih dari galeri",
                        onClick = {
                            showDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                    
                    // Tombol Batal
                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal", color = TextSecondary)
                    }
                }
            }
        }
    }
}

/**
 * Option item untuk sumber foto
 */
@Composable
private fun PhotoSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * Buat URI untuk menyimpan foto dari kamera
 */
private fun createImageUri(context: Context): Uri? {
    val imageFile = File(
        context.cacheDir,
        "camera_photo_${System.currentTimeMillis()}.jpg"
    )
    
    return try {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        null
    }
}
