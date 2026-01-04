package id.antasari.cityreport.data.repository

import id.antasari.cityreport.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository untuk mengelola votes dan comments di Appwrite
 */
class InteractionRepository {
    
    private val databases = AppwriteClient.databases
    private val databaseId = AppwriteClient.DATABASE_ID
    
    companion object {
        const val COLLECTION_VOTES = "votes"
        const val COLLECTION_COMMENTS = "comments"
    }
    
    // ============ VOTES ============
    
    /**
     * Get vote count for a report
     */
    suspend fun getVoteCount(reportId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = COLLECTION_VOTES,
                queries = listOf(
                    Query.equal("reportId", reportId)
                )
            )
            Result.success(response.total.toInt())
        } catch (e: Exception) {
            // Collection might not exist, return 0
            Result.success(0)
        }
    }
    
    /**
     * Check if user has voted
     */
    suspend fun hasUserVoted(reportId: String, userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = COLLECTION_VOTES,
                queries = listOf(
                    Query.equal("reportId", reportId),
                    Query.equal("userId", userId)
                )
            )
            Result.success(response.total > 0)
        } catch (e: Exception) {
            Result.success(false)
        }
    }
    
    /**
     * Add vote
     */
    suspend fun addVote(reportId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.createDocument(
                databaseId = databaseId,
                collectionId = COLLECTION_VOTES,
                documentId = ID.unique(),
                data = mapOf(
                    "reportId" to reportId,
                    "userId" to userId,
                    "createdAt" to java.time.Instant.now().toString()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove vote
     */
    suspend fun removeVote(reportId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = COLLECTION_VOTES,
                queries = listOf(
                    Query.equal("reportId", reportId),
                    Query.equal("userId", userId)
                )
            )
            if (response.documents.isNotEmpty()) {
                databases.deleteDocument(
                    databaseId = databaseId,
                    collectionId = COLLECTION_VOTES,
                    documentId = response.documents[0].id
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ COMMENTS ============
    
    data class Comment(
        val id: String,
        val reportId: String,
        val userId: String,
        val userName: String,
        val content: String,
        val createdAt: String
    )
    
    /**
     * Get comments for a report
     */
    suspend fun getComments(reportId: String): Result<List<Comment>> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = COLLECTION_COMMENTS,
                queries = listOf(
                    Query.equal("reportId", reportId),
                    Query.orderDesc("createdAt"),
                    Query.limit(50)
                )
            )
            
            val comments = response.documents.map { doc ->
                Comment(
                    id = doc.id,
                    reportId = doc.data["reportId"] as? String ?: "",
                    userId = doc.data["userId"] as? String ?: "",
                    userName = doc.data["userName"] as? String ?: "Anonymous",
                    content = doc.data["content"] as? String ?: "",
                    createdAt = doc.data["createdAt"] as? String ?: ""
                )
            }
            Result.success(comments)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }
    
    /**
     * Get comment count
     */
    suspend fun getCommentCount(reportId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = COLLECTION_COMMENTS,
                queries = listOf(
                    Query.equal("reportId", reportId)
                )
            )
            Result.success(response.total.toInt())
        } catch (e: Exception) {
            Result.success(0)
        }
    }
    
    /**
     * Add comment
     */
    suspend fun addComment(
        reportId: String, 
        userId: String, 
        userName: String,
        content: String
    ): Result<Comment> = withContext(Dispatchers.IO) {
        try {
            val createdAt = java.time.Instant.now().toString()
            val doc = databases.createDocument(
                databaseId = databaseId,
                collectionId = COLLECTION_COMMENTS,
                documentId = ID.unique(),
                data = mapOf(
                    "reportId" to reportId,
                    "userId" to userId,
                    "userName" to userName,
                    "content" to content,
                    "createdAt" to createdAt
                )
            )
            Result.success(Comment(
                id = doc.id,
                reportId = reportId,
                userId = userId,
                userName = userName,
                content = content,
                createdAt = createdAt
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
