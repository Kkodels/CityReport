package id.antasari.cityreport.ui.components

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import id.antasari.cityreport.ui.theme.*
import id.antasari.cityreport.utils.QRCodeGenerator
import java.io.File
import java.io.FileOutputStream

/**
 * Dialog to display and share QR code
 */
@Composable
fun QRCodeDialog(
    reportId: String,
    reportTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrContent = QRCodeGenerator.generateReportShareContent(reportId)
    val qrBitmap = remember { QRCodeGenerator.generateQRCode(qrContent, 512) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(CornerRadius.Large),
            color = AdaptiveColors.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "QR Code Laporan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AdaptiveColors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = AdaptiveColors.textSecondary
                        )
                    }
                }
                
                // Report title
                Text(
                    reportTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AdaptiveColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                
                // QR Code
                if (qrBitmap != null) {
                    Surface(
                        modifier = Modifier
                            .size(250.dp)
                            .padding(Spacing.Medium),
                        shape = RoundedCornerShape(CornerRadius.Medium),
                        color = White,
                        shadowElevation = Elevation.Medium
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(SurfaceGray, RoundedCornerShape(CornerRadius.Medium)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Gagal generate QR",
                            color = AdaptiveColors.textSecondary
                        )
                    }
                }
                
                // Description
                Text(
                    "Scan QR code ini untuk melihat detail laporan",
                    style = MaterialTheme.typography.bodySmall,
                    color = AdaptiveColors.textTertiary,
                    textAlign = TextAlign.Center
                )
                
                Divider(color = Gray200)
                
                // Share button
                PrimaryButton(
                    text = "Share QR Code",
                    onClick = {
                        qrBitmap?.let { bitmap ->
                            shareQRCode(context, bitmap, reportTitle)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Share QR code bitmap using Android share intent
 */
private fun shareQRCode(
    context: android.content.Context,
    bitmap: Bitmap,
    reportTitle: String
) {
    try {
        // Save bitmap to cache
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "qr_code_${System.currentTimeMillis()}.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        
        // Get URI
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        // Share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, "QR Code untuk laporan: $reportTitle")
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
