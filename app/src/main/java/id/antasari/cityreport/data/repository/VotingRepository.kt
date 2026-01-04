package id.antasari.cityreport.data.repository

import id.antasari.cityreport.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing user votes on reports
 */
class VotingRepository {
    
    private val databases = AppwriteClient.databases
    
    companion object {
        private const val COLLECTION_USER_VOTES = "userVotes" // Manual setup required
    }
    
    /**
     * Vote on a report
     * Creates vote record and increments report vote count
     */
    suspend fun voteReport(
        reportId: String,
        userId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Check if user already voted
            val existingVotes = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = COLLECTION_USER_VOTES,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.equal("reportId", reportId)
                )
            )
            
            if (existingVotes.documents.isNotEmpty()) {
                return@withContext Result.failure(Exception("Anda sudah vote laporan ini"))
            }
            
            // Create vote record
            databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = COLLECTION_USER_VOTES,
                documentId = ID.unique(),
                data = mapOf(
                    "userId" to userId,
                    "reportId" to reportId,
                    "createdAt" to java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        java.util.Locale.US
                    ).format(java.util.Date())
                )
            )
            
            // Increment report votes
            val reportsRepo = ReportsRepository()
            val report = reportsRepo.getReportById(reportId).getOrNull()
            if (report != null) {
                databases.updateDocument(
                    databaseId = AppwriteClient.DATABASE_ID,
                    collectionId = AppwriteClient.COLLECTION_REPORTS,
                    documentId = reportId,
                    data = mapOf(
                        "votes" to (report.votes + 1)
                    )
                )
            }
            
            Result.success(true)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal vote: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Remove vote from a report
     */
    suspend fun unvoteReport(
        reportId: String,
        userId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Find vote record
            val votes = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = COLLECTION_USER_VOTES,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.equal("reportId", reportId)
                )
            )
            
            if (votes.documents.isEmpty()) {
                return@withContext Result.failure(Exception("Vote tidak ditemukan"))
            }
            
            // Delete vote record
            databases.deleteDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = COLLECTION_USER_VOTES,
                documentId = votes.documents[0].id
            )
            
            // Decrement report votes
            val reportsRepo = ReportsRepository()
            val report = reportsRepo.getReportById(reportId).getOrNull()
            if (report != null && report.votes > 0) {
                databases.updateDocument(
                    databaseId = AppwriteClient.DATABASE_ID,
                    collectionId = AppwriteClient.COLLECTION_REPORTS,
                    documentId = reportId,
                    data = mapOf(
                        "votes" to (report.votes - 1)
                    )
                )
            }
            
            Result.success(true)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal unvote: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Check if user has voted on a report
     */
    suspend fun hasUserVoted(
        reportId: String,
        userId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val votes = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = COLLECTION_USER_VOTES,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.equal("reportId", reportId)
                )
            )
            
            Result.success(votes.documents.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
