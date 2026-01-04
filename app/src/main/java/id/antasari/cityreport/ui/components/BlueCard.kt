package id.antasari.cityreport.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.ui.theme.*

/**
 * Blue-themed card matching Figma design
 * White background, subtle shadow, rounded 16dp
 */
@Composable
fun BlueCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: BlueCardPadding = BlueCardPadding.Medium,
    elevation: Int = 2,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    
    val contentPadding = when (padding) {
        BlueCardPadding.None -> PaddingValues(0.dp)
        BlueCardPadding.Small -> PaddingValues(12.dp)
        BlueCardPadding.Medium -> PaddingValues(16.dp)
        BlueCardPadding.Large -> PaddingValues(20.dp)
    }
    
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

enum class BlueCardPadding {
    None,    // 0dp
    Small,   // 12dp
    Medium,  // 16dp
    Large    // 20dp
}
