package id.antasari.cityreport.domain.model

/**
 * Report data class matching Appwrite 'reports' collection
 */
data class Report(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val priority: String,
    val severity: Int = 3, // 1-5 scale, default medium
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val address: String = "",
    val photoId: String? = null,
    val completionPhotoId: String? = null,
    val votes: Int = 0,
    val userId: String,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Report status enumeration
 */
enum class ReportStatus(val value: String, val displayName: String) {
    NEW("Baru", "Baru"),
    IN_PROGRESS("Diproses", "Diproses"),
    COMPLETED("Selesai", "Selesai"),
    REJECTED("Ditolak", "Ditolak");
    
    companion object {
        fun fromString(value: String): ReportStatus {
            return values().find { it.value == value } ?: NEW
        }
    }
}

/**
 * Report priority enumeration
 */
enum class ReportPriority(val value: String, val displayName: String) {
    LOW("Rendah", "Rendah"),
    MEDIUM("Sedang", "Sedang"),
    HIGH("Tinggi", "Tinggi");
    
    companion object {
        fun fromString(value: String): ReportPriority {
            return values().find { it.value == value } ?: MEDIUM
        }
    }
}

