package id.antasari.cityreport.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Utility object for QR code generation
 */
object QRCodeGenerator {
    
    /**
     * Generate QR code bitmap from content string
     * @param content The text content to encode in QR code
     * @param size The size of the QR code bitmap (width = height)
     * @return Bitmap of the QR code
     */
    fun generateQRCode(content: String, size: Int = 512): Bitmap? {
        try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.MARGIN] = 1
            
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    )
                }
            }
            
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Generate shareable report URL/ID
     * @param reportId The report document ID
     * @return Shareable URL or ID string
     */
    fun generateReportShareContent(reportId: String): String {
        // For now, return simple ID. Can be enhanced to full URL with deep linking
        return "CITYREPORT:$reportId"
    }
}
