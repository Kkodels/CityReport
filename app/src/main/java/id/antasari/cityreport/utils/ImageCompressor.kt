package id.antasari.cityreport.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Image compression utility to reduce photo file size
 */
object ImageCompressor {
    
    /**
     * Compress image from Uri
     * @param context Application context
     * @param imageUri Source image URI
     * @param maxWidth Maximum width (default 1024px)
     * @param maxHeight Maximum height (default 1024px)
     * @param quality JPEG quality 0-100 (default 80)
     * @return Compressed image as ByteArray, or null if failed
     */
    fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): ByteArray? {
        return try {
            // Open input stream
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return null
            
            // Decode with inSampleSize for memory efficiency
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            
            // Decode bitmap
            val newInputStream = context.contentResolver.openInputStream(imageUri)
            var bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
            newInputStream?.close()
            
            if (bitmap == null) return null
            
            // Handle rotation from EXIF
            bitmap = rotateImageIfRequired(context, bitmap, imageUri)
            
            // Scale to max dimensions
            bitmap = scaleBitmap(bitmap, maxWidth, maxHeight)
            
            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            
            val result = outputStream.toByteArray()
            android.util.Log.d("ImageCompressor", 
                "Compressed: ${result.size / 1024}KB, ${bitmap.width}x${bitmap.height}")
            
            bitmap.recycle()
            result
            
        } catch (e: Exception) {
            android.util.Log.e("ImageCompressor", "Compression failed: ${e.message}", e)
            null
        }
    }
    
    /**
     * Compress and save to file
     */
    fun compressToFile(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): File? {
        val compressedBytes = compressImage(context, imageUri, maxWidth, maxHeight, quality)
            ?: return null
        
        return try {
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { fos ->
                fos.write(compressedBytes)
            }
            android.util.Log.d("ImageCompressor", "Saved to: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            android.util.Log.e("ImageCompressor", "Save failed: ${e.message}", e)
            null
        }
    }
    
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight 
                && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
    
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val ratio = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, uri: Uri): Bitmap {
        // Try to get rotation from EXIF - simplified for API compatibility
        return try {
            // Get file path from URI for EXIF reading
            val inputStream = context.contentResolver.openInputStream(uri) 
                ?: return bitmap
            
            // Create temp file for EXIF reading
            val tempFile = File(context.cacheDir, "temp_exif_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            val exif = ExifInterface(tempFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            tempFile.delete()
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            android.util.Log.w("ImageCompressor", "EXIF rotation skipped: ${e.message}")
            bitmap
        }
    }
}
