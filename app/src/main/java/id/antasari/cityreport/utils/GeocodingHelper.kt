package id.antasari.cityreport.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Helper for reverse geocoding (converting coordinates to address)
 */
class GeocodingHelper(private val context: Context) {
    
    private val geocoder = Geocoder(context, Locale("id", "ID"))
    
    /**
     * Get address name from coordinates
     * @param lat Latitude
     * @param lng Longitude
     * @return Address string (e.g., "Jl. Sudirman, Jakarta")  or null if failed
     */
    suspend fun getAddressFromLocation(lat: Double, lng: Double): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ API
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        continuation.resume(formatAddress(address))
                    }
                } else {
                    // Legacy API
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val address = addresses?.firstOrNull()
                    continuation.resume(formatAddress(address))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to coordinates
                continuation.resume("${"%.4f".format(lat)}, ${"%.4f".format(lng)}")
            }
        }
    }
    
    private fun formatAddress(address: Address?): String? {
        if (address == null) return null
        
        return buildString {
            // Try to get street name + city
            address.thoroughfare?.let { append("$it, ") }
            address.subLocality?.let { append("$it, ") }
            address.locality?.let { append(it) }
            
            // If nothing found, use full address line
            if (isEmpty()) {
                address.getAddressLine(0)?.let { append(it) }
            }
            
            // If still empty, fallback
            if (isEmpty()) {
                append("${address.latitude}, ${address.longitude}")
            }
        }
    }
}
