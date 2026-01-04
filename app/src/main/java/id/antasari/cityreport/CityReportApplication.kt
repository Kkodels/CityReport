package id.antasari.cityreport

import android.app.Application
import android.util.Log
import id.antasari.cityreport.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Custom Application class with global crash handler
 * Automatically logs out user when app crashes
 */
class CityReportApplication : Application() {
    
    // Lazy initialization to avoid early instantiation before AppwriteClient is ready
    private val authRepository by lazy { AuthRepository() }
    private val TAG = "CityReportApp"
    
    override fun onCreate() {
        super.onCreate()
        
        // Set global exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        Log.d(TAG, "CityReportApplication initialized with crash handler")
    }
    
    private fun handleCrash(throwable: Throwable) {
        Log.e(TAG, "App crashed, logging out user", throwable)
        
        // Logout user in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                authRepository.logout()
                Log.d(TAG, "User logged out successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to logout user", e)
            }
        }
    }
}
