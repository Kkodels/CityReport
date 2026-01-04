package id.antasari.cityreport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.ui.theme.*

/**
 * Category selection card for report form
 */
@Composable
fun CategorySelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Primary else Color.Transparent
    val elevation = if (isSelected) Elevation.Medium else Elevation.Low
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(elevation, RoundedCornerShape(CornerRadius.Medium))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(CornerRadius.Medium)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = AdaptiveColors.card
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            // Selected checkmark
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = White,
                            modifier = Modifier
                                .padding(2.dp)
                                .size(16.dp)
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(24.dp))
            }
            
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(CornerRadius.Medium)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(Modifier.height(Spacing.ExtraSmall))
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = AdaptiveColors.textPrimary,
                maxLines = 2
            )
            
            // Subtitle
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AdaptiveColors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
