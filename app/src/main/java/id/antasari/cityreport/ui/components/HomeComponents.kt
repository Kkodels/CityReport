package id.antasari.cityreport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import id.antasari.cityreport.ui.theme.*

/**
 * Main action card with gradient background
 */
@Composable
fun GradientActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .shadow(Elevation.Medium, RoundedCornerShape(CornerRadius.Large))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadius.Large),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(Spacing.Large)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(IconSize.Large)
                    )
                    Spacer(Modifier.height(Spacing.Small))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.9f)
                    )
                }
                
                // Decorative megaphone icon
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = White.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

/**
 * Feature card for grid (2x1)
 */
@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(Elevation.Low, RoundedCornerShape(CornerRadius.Medium))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = AdaptiveColors.card
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = iconBackground,
                        shape = RoundedCornerShape(CornerRadius.Small)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AdaptiveColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AdaptiveColors.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Report card for list
 */
@Composable
fun NewReportCard(
    title: String,
    status: String,
    category: String,
    location: String,
    timeAgo: String,
    createdAt: String = "",
    imageUrl: String?,
    severity: Int = 3,
    votes: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert photoId to full URL
    val photoUrl = if (!imageUrl.isNullOrEmpty()) {
        "https://sgp.cloud.appwrite.io/v1/storage/buckets/693fc034000a342e577c/files/$imageUrl/view?project=693ac9810019b47f348e"
    } else null
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = AdaptiveColors.card,
        shadowElevation = Elevation.Low
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image thumbnail - square
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(SurfaceGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Status + Urgency badges on single row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge compact
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceGray
                    ) {
                        Text(
                            text = status.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AdaptiveColors.textSecondary
                        )
                    }
                    
                    // Urgency badge
                    if (severity >= 3) {
                        val isHigh = severity >= 4
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isHigh) 
                                androidx.compose.ui.graphics.Color(0xFFFFE5E5) 
                            else 
                                androidx.compose.ui.graphics.Color(0xFFFFF4E5)
                        ) {
                            Text(
                                text = if (isHigh) "🔴 Mendesak" else "🟡 Sedang",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isHigh)
                                    androidx.compose.ui.graphics.Color(0xFFFF5722)
                                else
                                    androidx.compose.ui.graphics.Color(0xFFFFA726),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Title - single line
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = AdaptiveColors.textPrimary
                )
                
                // Category with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = id.antasari.cityreport.utils.CategoryIcons.getIconForCategory(category),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = id.antasari.cityreport.utils.CategoryIcons.getColorForCategory(category)
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = AdaptiveColors.textSecondary,
                        maxLines = 1
                    )
                }
                
                // Location + time on same row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AdaptiveColors.textTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = location.take(20) + if (location.length > 20) "..." else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = AdaptiveColors.textTertiary,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
                
                // Created date (absolute) - ONLY THIS NOW
                Text(
                    text = if (createdAt.isNotEmpty()) formatReportDate(createdAt) else timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = AdaptiveColors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
                
                
                // Vote count
                if (votes > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "$votes dukungan",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatReportDate(dateString: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(dateString)
        val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID"))
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
