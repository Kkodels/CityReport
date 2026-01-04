package id.antasari.cityreport.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import id.antasari.cityreport.ui.theme.*

/**
 * Interactive Before/After photo comparison slider
 */
@Composable
fun BeforeAfterSlider(
    beforePhotoUrl: String,
    afterPhotoUrl: String,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(0.5f) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(CornerRadius.Medium))
    ) {
        // After photo (background)
        AsyncImage(
            model = afterPhotoUrl,
            contentDescription = "Foto setelah perbaikan",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Before photo (with clip)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(sliderPosition)
        ) {
            AsyncImage(
                model = beforePhotoUrl,
                contentDescription = "Foto sebelum perbaikan",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Slider handle
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .zIndex(1f)
        ) {
            // Vertical line
            Surface(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .offset(x = (sliderPosition * 1000).dp) // Approximation
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newPosition = sliderPosition + (dragAmount.x / size.width)
                            sliderPosition = newPosition.coerceIn(0f, 1f)
                        }
                    },
                color = White,
                shadowElevation = Elevation.Medium
            ) {}
            
            // Handle button
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (sliderPosition * 1000).dp - 24.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newPosition = sliderPosition + (dragAmount.x / size.width)
                            sliderPosition = newPosition.coerceIn(0f, 1f)
                        }
                    },
                shape = CircleShape,
                color = Primary,
                shadowElevation = Elevation.High
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Drag to compare",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(CornerRadius.Small),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    "Sebelum",
                    modifier = Modifier.padding(horizontal = Spacing.Small, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = White
                )
            }
            
            Surface(
                shape = RoundedCornerShape(CornerRadius.Small),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    "Sesudah",
                    modifier = Modifier.padding(horizontal = Spacing.Small, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = White
                )
            }
        }
    }
}
