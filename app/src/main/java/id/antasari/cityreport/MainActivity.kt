package id.antasari.cityreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import id.antasari.cityreport.data.remote.AppwriteClient
import id.antasari.cityreport.ui.navigation.AppNavHost
import id.antasari.cityreport.ui.theme.CityReportTheme
import id.antasari.cityreport.ui.theme.ThemeManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Appwrite client with context
        AppwriteClient.initialize(applicationContext)
        
        // Load saved dark mode preference
        runBlocking {
            val savedDarkMode = ThemeManager.getDarkModeFlow(applicationContext).first()
            ThemeManager.updateDarkMode(savedDarkMode)
        }
        
        // Auto-close expired reports (runs once per day)
        lifecycleScope.launch {
            val prefs = getSharedPreferences("cityreport_prefs", MODE_PRIVATE)
            val lastAutoClose = prefs.getLong("last_auto_close", 0L)
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            
            // Run if never run before, or more than 24h ago
            if (now - lastAutoClose > oneDayMs) {
                val repo = id.antasari.cityreport.data.repository.ReportsRepository()
                val result = repo.closeExpiredReports()
                if (result.isSuccess) {
                    val closedCount = result.getOrNull() ?: 0
                    android.util.Log.d("MainActivity", "Auto-closed $closedCount expired reports")
                    // Save last run time
                    prefs.edit().putLong("last_auto_close", now).apply()
                }
            }
        }
        
        setContent {
            val isDarkMode = ThemeManager.isDarkMode
            
            CityReportTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}
