package id.antasari.cityreport.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utility object for category icon and color mapping
 */
object CategoryIcons {
    
    /**
     * Get icon for a specific category
     */
    fun getIconForCategory(category: String): ImageVector {
        return when (category) {
            "Jalan Rusak" -> Icons.Default.Build
            "Sampah" -> Icons.Default.Delete
            "Banjir" -> Icons.Default.WaterDrop
            "Lampu Jalan" -> Icons.Default.Lightbulb
            "Fasilitas" -> Icons.Default.Park
            "Lainnya" -> Icons.Default.MoreHoriz
            else -> Icons.Default.Description
        }
    }
    
    /**
     * Get color for a specific category
     */
    fun getColorForCategory(category: String): Color {
        return when (category) {
            "Jalan Rusak" -> Color(0xFF2196F3)      // Blue
            "Sampah" -> Color(0xFFFF9800)           // Orange
            "Banjir" -> Color(0xFF00BCD4)           // Cyan
            "Lampu Jalan" -> Color(0xFFFFC107)      // Yellow
            "Fasilitas" -> Color(0xFF4CAF50)        // Green
            "Lainnya" -> Color(0xFF9C27B0)          // Purple
            else -> Color(0xFF757575)                // Gray
        }
    }
    
    /**
     * Get background color for category badge
     */
    fun getBackgroundColorForCategory(category: String): Color {
        return when (category) {
            "Jalan Rusak" -> Color(0xFFE3F2FD)      // Light Blue
            "Sampah" -> Color(0xFFFFE0B2)           // Light Orange
            "Banjir" -> Color(0xFFE0F7FA)           // Light Cyan
            "Lampu Jalan" -> Color(0xFFFFF9C4)      // Light Yellow
            "Fasilitas" -> Color(0xFFE8F5E9)        // Light Green
            "Lainnya" -> Color(0xFFF3E5F5)          // Light Purple
            else -> Color(0xFFF5F5F5)                // Light Gray
        }
    }
}
