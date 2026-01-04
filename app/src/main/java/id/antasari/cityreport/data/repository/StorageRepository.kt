package id.antasari.cityreport.data.repository

import android.content.Context
import android.net.Uri
import id.antasari.cityreport.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.models.InputFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository untuk mengelola upload/download foto ke Appwrite Storage
 */
class StorageRepository {
    
    private val storage = AppwriteClient.storage
    
    /**
     * Upload foto ke Appwrite Storage
     * @param context Application context
     * @param uri URI foto yang dipilih dari galeri
     * @return Result dengan file ID jika sukses
     */
    suspend fun uploadPhoto(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Buat temporary file dari URI
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Tidak bisa membuka file"))
            
            // Generate unique file ID
            val fileId = ID.unique()
            
            // Dapatkan nama file dengan extension yang benar
            val originalName = getFileName(context, uri)
            val extension = when (context.contentResolver.getType(uri)) {
                "image/jpeg", "image/jpg" -> ".jpg"
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                else -> ".jpg"
            }
            val fileName = originalName ?: "photo_${System.currentTimeMillis()}$extension"
            
            // Buat temporary file dengan extension yang benar
            val tempFile = File(context.cacheDir, fileName)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            // Upload ke Appwrite Storage (MIME type auto-detected dari extension)
            val uploadedFile = storage.createFile(
                bucketId = AppwriteClient.BUCKET_PHOTOS,
                fileId = fileId,
                file = InputFile.fromFile(tempFile)
            )
            
            // Hapus temporary file
            tempFile.delete()
            
            Result.success(uploadedFile.id)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal upload foto: ${e.message}"))
        }
    }
    
    /**
     * Upload foto dengan kompresi otomatis
     * @param context Application context
     * @param uri URI foto yang dipilih
     * @param maxWidth Lebar maksimum (default 1024px)
     * @param maxHeight Tinggi maksimum (default 1024px)
     * @param quality Kualitas JPEG 0-100 (default 80)
     * @return Result dengan file ID jika sukses
     */
    suspend fun uploadPhotoCompressed(
        context: Context, 
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("StorageRepo", "Starting upload for: $uri")
            
            // Try compression first
            var fileToUpload: File? = null
            
            try {
                fileToUpload = id.antasari.cityreport.utils.ImageCompressor.compressToFile(
                    context, uri, maxWidth, maxHeight, quality
                )
                if (fileToUpload != null) {
                    android.util.Log.d("StorageRepo", "Compressed: ${fileToUpload.length() / 1024}KB")
                }
            } catch (compressError: Exception) {
                android.util.Log.w("StorageRepo", "Compression failed, using original: ${compressError.message}")
            }
            
            // Fallback to original file if compression failed
            if (fileToUpload == null || !fileToUpload.exists()) {
                android.util.Log.d("StorageRepo", "Using original file")
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(Exception("Cannot open file"))
                
                fileToUpload = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                fileToUpload.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                android.util.Log.d("StorageRepo", "Original size: ${fileToUpload.length() / 1024}KB")
            }
            
            // Generate unique file ID
            val fileId = ID.unique()
            android.util.Log.d("StorageRepo", "Uploading to Appwrite with ID: $fileId")
            
            // Upload ke Appwrite Storage
            val uploadedFile = storage.createFile(
                bucketId = AppwriteClient.BUCKET_PHOTOS,
                fileId = fileId,
                file = InputFile.fromFile(fileToUpload)
            )
            
            // Hapus temporary file
            fileToUpload.delete()
            
            android.util.Log.d("StorageRepo", "✅ Upload success! File ID: ${uploadedFile.id}")
            Result.success(uploadedFile.id)
        } catch (e: Exception) {
            android.util.Log.e("StorageRepo", "❌ Upload failed: ${e.message}", e)
            Result.failure(Exception("Gagal upload foto: ${e.message}"))
        }
    }
    
    /**
     * Hapus foto dari Appwrite Storage
     * @param fileId ID file yang akan dihapus
     * @return Result sukses atau error
     */
    suspend fun deletePhoto(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            storage.deleteFile(
                bucketId = AppwriteClient.BUCKET_PHOTOS,
                fileId = fileId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal hapus foto: ${e.message}"))
        }
    }
    
    /**
     * Dapatkan URL untuk menampilkan foto
     * @param fileId ID file yang akan ditampilkan
     * @return URL string untuk load gambar
     */
    fun getPhotoUrl(fileId: String): String {
        // Endpoint Appwrite
        val endpoint = "https://sgp.cloud.appwrite.io/v1"
        val projectId = AppwriteClient.client.config["project"]
        return "$endpoint/storage/buckets/${AppwriteClient.BUCKET_PHOTOS}/files/$fileId/view?project=$projectId"
    }
    
    /**
     * Get file name from URI
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        }
    }
    
    /**
     * Validasi ukuran file (max 5MB)
     * @param context Application context
     * @param uri URI file yang akan divalidasi
     * @return true jika ukuran valid, false jika terlalu besar
     */
    fun validateFileSize(context: Context, uri: Uri): Boolean {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileSize = inputStream?.available() ?: 0
        inputStream?.close()
        
        val maxSize = 5 * 1024 * 1024 // 5 MB
        return fileSize <= maxSize
    }
    
    suspend fun uploadProfilePhoto(context: Context, uri: Uri): Result<String> = 
        uploadPhotoCompressed(context, uri, 512, 512, 85)
    
    fun getProfilePhotoUrl(photoId: String?): String? = photoId?.let {
        "https://sgp.cloud.appwrite.io/v1/storage/buckets/${AppwriteClient.BUCKET_PHOTOS}/files/$it/view?project=693ac9810019b47f348e"
    }
}
