package id.antasari.cityreport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.cityreport.ui.theme.*

/**
 * Status pill component matching Figma design
 * Rounded 12dp, colored background based on status
 */
@Composable
fun StatusPill(
    status: String,
    modifier: Modifier = Modifier,
    animated: Boolean = false
) {
    val (backgroundColor, textColor, borderColor) = when (status.lowercase()) {
        "baru", "menunggu", "pending" -> Triple(StatusPending, StatusPendingText, StatusPendingBorder)
        "diproses", "in-progress", "in_progress" -> Triple(StatusInProgress, StatusInProgressText, StatusInProgressBorder)
        "selesai", "completed", "done" -> Triple(StatusCompleted, StatusCompletedText, StatusCompletedBorder)
        else -> Triple(Gray100, Gray600, Gray300)
    }
    
    val label = when (status.lowercase()) {
        "baru", "pending" -> "Menunggu"
        "diproses", "in-progress", "in_progress" -> "Diproses"
        "selesai", "completed", "done" -> "Selesai"
        else -> status
    }
    
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}

/**
 * Priority pill component
 */
@Composable
fun PriorityPill(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (priority.lowercase()) {
        "rendah", "low" -> Pair(Color(0xFFD1FAE5), Color(0xFF047857))
        "sedang", "medium" -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
        "tinggi", "high" -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
        else -> Pair(Gray100, Gray600)
    }
    
    val label = when (priority.lowercase()) {
        "rendah", "low" -> "Rendah"
        "sedang", "medium" -> "Sedang"
        "tinggi", "high" -> "Tinggi"
        else -> priority
    }
    
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}
