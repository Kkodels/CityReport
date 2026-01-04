package id.antasari.cityreport.data.remote

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage

/**
 * Singleton object for Appwrite client configuration
 * 
 * IMPORTANT: Call initialize() with context before using
 */
object AppwriteClient {
    // Configuration from Appwrite Dashboard
    private const val ENDPOINT = "https://sgp.cloud.appwrite.io/v1"  // Singapore region
    private const val PROJECT_ID = "693ac9810019b47f348e"  // Your actual Project ID
    
    // Database configuration (use actual Database ID from Appwrite dashboard)
    const val DATABASE_ID = "693aca2d001fe1c4a236"  // Your actual Database ID
    const val COLLECTION_PROFILES = "profiles"
    const val COLLECTION_REPORTS = "reports"
    
    // Storage configuration
    const val BUCKET_PHOTOS = "693fc034000a342e577c"  // Photos bucket ID
    
    private lateinit var _client: Client
    
    fun initialize(context: Context) {
        if (!::_client.isInitialized) {
            _client = Client(context)
                .setEndpoint(ENDPOINT)
                .setProject(PROJECT_ID)
        }
    }
    
    val client: Client
        get() = _client
    
    val account: Account by lazy {
        Account(client)
    }
    
    val databases: Databases by lazy {
        Databases(client)
    }
    
    val storage: Storage by lazy {
        Storage(client)
    }
}
