package id.antasari.cityreport.data.repository

import id.antasari.cityreport.data.remote.AppwriteClient
import id.antasari.cityreport.domain.model.Report
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for report CRUD operations using Appwrite
 */
class ReportsRepository {
    
    private val databases = AppwriteClient.databases
    
    /**
     * Create a new report
     */
    suspend fun createReport(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double,
        locationName: String,
        userId: String,
        priority: String = "Sedang",
        severity: Int = 3,
        photoId: String? = null
    ): Result<Report> = withContext(Dispatchers.IO) {
        try {
            val reportData = mutableMapOf<String, Any>(
                "title" to title,
                "description" to description,
                "category" to category,
                "status" to "Baru",
                "priority" to priority,
                "severity" to severity,
                "latitude" to latitude,
                "longitude" to longitude,
                "locationName" to locationName,
                "address" to locationName, // For backward compatibility
                "userId" to userId
            )
            
            if (photoId != null) {
                reportData["photoId"] = photoId
            }
            
            // Add completionPhotoId as empty string (will be updated later by admin)
            reportData["completionPhotoId"] = ""
            
            // Add votes with default 0
            reportData["votes"] = 0
            
            val doc = databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                documentId = ID.unique(),
                data = reportData
            )
            
            val report = documentToReport(doc)
            Result.success(report)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal membuat laporan: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Get reports for a specific user
     */
    suspend fun getReportsForUser(userId: String): Result<List<Report>> = 
        withContext(Dispatchers.IO) {
        try {
            val docs = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.orderDesc("\$createdAt"),
                    Query.limit(100)
                )
            )
            
            val reports = docs.documents.map { documentToReport(it) }
            Result.success(reports)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal mengambil laporan: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Get all reports (for admin)
     */
    suspend fun getAllReports(): Result<List<Report>> = withContext(Dispatchers.IO) {
        try {
            val docs = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                queries = listOf(
                    Query.orderDesc("\$createdAt"),
                    Query.limit(100)
                )
            )
            
            val reports = docs.documents.map { documentToReport(it) }
            Result.success(reports)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal mengambil laporan: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Get a single report by ID
     */
    suspend fun getReportById(reportId: String): Result<Report> = withContext(Dispatchers.IO) {
        try {
            val doc = databases.getDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                documentId = reportId
            )
            
            val report = documentToReport(doc)
            Result.success(report)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal mengambil laporan: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Update report status and priority (admin only)
     */
    suspend fun updateReportStatus(
        reportId: String,
        status: String,
        priority: String? = null
    ): Result<Report> = withContext(Dispatchers.IO) {
        try {
            val updateData = mutableMapOf<String, Any>("status" to status)
            if (priority != null) {
                updateData["priority"] = priority
            }
            
            val doc = databases.updateDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                documentId = reportId,
                data = updateData
            )
            
            val report = documentToReport(doc)
            Result.success(report)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal mengupdate laporan: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Delete a report
     */
    suspend fun deleteReport(reportId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                documentId = reportId
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal menghapus laporan: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Helper function to convert Appwrite document to Report
     */
    private fun documentToReport(doc: io.appwrite.models.Document<Map<String, Any>>): Report {
        return Report(
            id = doc.id,
            title = doc.data["title"] as? String ?: "",
            description = doc.data["description"] as? String ?: "",
            category = doc.data["category"] as? String ?: "",
            status = doc.data["status"] as? String ?: "Baru",
            priority = doc.data["priority"] as? String ?: "Sedang",
            severity = (doc.data["severity"] as? Number)?.toInt() ?: 3,
            latitude = (doc.data["latitude"] as? Number)?.toDouble() ?: 0.0,
            longitude = (doc.data["longitude"] as? Number)?.toDouble() ?: 0.0,
            locationName = doc.data["locationName"] as? String ?: "",
            address = doc.data["address"] as? String ?: "",
            photoId = doc.data["photoId"] as? String,
            completionPhotoId = doc.data.getOrDefault("completionPhotoId", null) as? String,
            votes = (doc.data.getOrDefault("votes", 0) as? Number)?.toInt() ?: 0,
            userId = doc.data["userId"] as? String ?: "",
            createdAt = doc.data["createdAt"] as? String ?: "",
            updatedAt = doc.data["updatedAt"] as? String ?: ""
        )
    }
    
    /**
     * Update completion photo for a report (admin only)
     */
    suspend fun updateCompletionPhoto(
        reportId: String,
        photoId: String
    ): Result<Report> = withContext(Dispatchers.IO) {
        try {
            val updatedDoc = databases.updateDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                documentId = reportId,
                data = mapOf(
                    "completionPhotoId" to photoId
                )
            )
            
            Result.success(documentToReport(updatedDoc))
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal update foto: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Get monthly report statistics for trend chart
     * Returns map of "Month" to count
     */
    suspend fun getMonthlyStats(): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val reports = getAllReports().getOrNull() ?: emptyList()
            val monthCounts = mutableMapOf<String, Int>()
            
            reports.forEach { report ->
                val month = report.createdAt.take(7) // "2024-12"
                monthCounts[month] = (monthCounts[month] ?: 0) + 1
            }
            
            Result.success(monthCounts.toSortedMap())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get category distribution for pie/bar chart
     */
    suspend fun getCategoryDistribution(): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val reports = getAllReports().getOrNull() ?: emptyList()
            val categoryCounts = mutableMapOf<String, Int>()
            
            reports.forEach { report ->
                categoryCounts[report.category] = (categoryCounts[report.category] ?: 0) + 1
            }
            
            Result.success(categoryCounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get average response time in days
     */
    suspend fun getAverageResponseTime(): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val reports = getAllReports().getOrNull() ?: emptyList()
            val completedReports = reports.filter { it.status == "Selesai" }
            
            if (completedReports.isEmpty()) {
                return@withContext Result.success(0.0)
            }
            
            val responseTimes = completedReports.mapNotNull { report ->
                try {
                    val created = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        .parse(report.createdAt)?.time ?: return@mapNotNull null
                    val updated = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        .parse(report.updatedAt)?.time ?: return@mapNotNull null
                    
                    val diffMs = updated - created
                    diffMs / (1000.0 * 60 * 60 * 24) // Convert to days
                } catch (e: Exception) {
                    null
                }
            }
            
            val avgDays = if (responseTimes.isNotEmpty()) {
                responseTimes.average()
            } else {
                0.0
            }
            
            Result.success(avgDays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get popular reports from last 7 days (most voted)
     */
    suspend fun getPopularReportsThisWeek(): Result<List<Report>> = withContext(Dispatchers.IO) {
        try {
            val sevenDaysAgo = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, -7)
            }.time
            val sevenDaysAgoString = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(sevenDaysAgo)
            
            val docs = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                queries = listOf(
                    Query.greaterThan("createdAt", sevenDaysAgoString),
                    Query.greaterThan("votes", 0),
                    Query.orderDesc("votes"),
                    Query.limit(5)
                )
            )
            
            val reports = docs.documents.map { documentToReport(it) }
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user statistics
     */
    suspend fun getUserStats(userId: String): Result<id.antasari.cityreport.domain.model.UserStats> = withContext(Dispatchers.IO) {
        try {
            // Get all user reports
            val allReportsResult = getReportsForUser(userId)
            if (allReportsResult.isFailure) {
                return@withContext Result.failure(allReportsResult.exceptionOrNull()!!)
            }
            
            val reports = allReportsResult.getOrNull() ?: emptyList()
            val totalReports = reports.size
            val resolvedCount = reports.count { it.status == "Selesai" }
            val totalVotes = reports.sumOf { it.votes }
            val lastReportDate = reports.maxByOrNull { it.createdAt }?.createdAt
            
            val stats = id.antasari.cityreport.domain.model.UserStats(
                totalReports = totalReports,
                resolvedCount = resolvedCount,
                totalVotes = totalVotes,
                lastReportDate = lastReportDate
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Auto-close expired reports (older than 90 days, not resolved)
     */
    suspend fun closeExpiredReports(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val ninetyDaysAgo = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, -90)
            }.time
            val ninetyDaysAgoString = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(ninetyDaysAgo)
            
            // Find expired reports
            val docs = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_REPORTS,
                queries = listOf(
                    Query.lessThan("createdAt", ninetyDaysAgoString),
                    Query.notEqual("status", "Selesai"),
                    Query.notEqual("status", "Kadaluarsa"),
                    Query.limit(100)  // Process in batches
                )
            )
            
            var closedCount = 0
            for (doc in docs.documents) {
                try {
                    databases.updateDocument(
                        databaseId = AppwriteClient.DATABASE_ID,
                        collectionId = AppwriteClient.COLLECTION_REPORTS,
                        documentId = doc.id,
                        data = mapOf("status" to "Kadaluarsa")
                    )
                    closedCount++
                } catch (e: Exception) {
                    // Continue with next document
                    android.util.Log.e("ReportsRepository", "Failed to close expired report ${doc.id}: ${e.message}")
                }
            }
            
            Result.success(closedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
