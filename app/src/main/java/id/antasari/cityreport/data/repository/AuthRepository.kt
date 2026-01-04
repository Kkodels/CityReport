package id.antasari.cityreport.data.repository

import id.antasari.cityreport.data.model.Profile
import id.antasari.cityreport.data.model.UserRole
import id.antasari.cityreport.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for authentication operations using Appwrite
 */
class AuthRepository {
    
    private val account = AppwriteClient.account
    private val databases = AppwriteClient.databases
    
    /**
     * Register a new user and create their profile
     * @param name User's full name
     * @param email User's email
     * @param password User's password
     * @return Result with Profile or error message
     */
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Profile> = withContext(Dispatchers.IO) {
        try {
            // Create Appwrite account
            val user = account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
            
            // Create profile document with USER role
            val profileData = mapOf(
                "userId" to user.id,
                "name" to name,
                "email" to email,
                "role" to UserRole.USER.value
            )
            
            val profileDoc = databases.createDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_PROFILES,
                documentId = ID.unique(),
                data = profileData
            )
            
            // Return the created profile
            val profile = Profile(
                userId = user.id,
                name = name,
                email = email,
                role = UserRole.USER
            )
            
            Result.success(profile)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Registrasi gagal: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Login user with email and password
     * @param email User's email
     * @param password User's password
     * @return Result with success or error message
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Create email session
            account.createEmailPasswordSession(
                email = email,
                password = password
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Login gagal: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            account.deleteSession("current")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current logged in user's profile with role
     * @return Result with Profile or error
     */
    suspend fun getCurrentUserWithRole(): Result<Profile> = withContext(Dispatchers.IO) {
        try {
            // Get current account
            val user = account.get()
            
            // Query profile by userId
            val profiles = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_PROFILES,
                queries = listOf(
                    Query.equal("userId", user.id)
                )
            )
            
            if (profiles.documents.isEmpty()) {
                return@withContext Result.failure(Exception("Profile tidak ditemukan"))
            }
            
            val profileDoc = profiles.documents[0]
            val profile = Profile(
                userId = profileDoc.data["userId"] as String,
                name = profileDoc.data["name"] as String,
                email = profileDoc.data["email"] as String,
                role = UserRole.fromString(profileDoc.data["role"] as String),
                profilePhotoId = profileDoc.data["profilePhotoId"] as? String
            )
            
            Result.success(profile)
        } catch (e: AppwriteException) {
            Result.failure(Exception("Gagal mendapatkan profil: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan: ${e.message}"))
        }
    }
    
    /**
     * Check if user is currently logged in
     */
    suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            account.get()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateProfilePhoto(userId: String, photoId: String): Result<Unit> = 
        withContext(Dispatchers.IO) {
        try {
            val profiles = databases.listDocuments(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_PROFILES,
                queries = listOf(Query.equal("userId", userId))
            )
            
            if (profiles.documents.isEmpty()) {
                return@withContext Result.failure(Exception("Profile not found"))
            }
            
            databases.updateDocument(
                databaseId = AppwriteClient.DATABASE_ID,
                collectionId = AppwriteClient.COLLECTION_PROFILES,
                documentId = profiles.documents[0].id,
                data = mapOf("profilePhotoId" to photoId)
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal update foto: ${e.message}"))
        }
    }
}
