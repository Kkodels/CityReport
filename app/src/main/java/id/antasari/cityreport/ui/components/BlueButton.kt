package id.antasari.cityreport.ui.components

import androidx.compose.foundation.layout.*  
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.ui.theme.*

/**
 * Blue-themed button matching Figma design
 * Rounded pill shape (28dp), blue primary color
 */
@Composable
fun BlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BlueButtonVariant = BlueButtonVariant.Primary,
    size: BlueButtonSize = BlueButtonSize.Medium,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val colors = when (variant) {
        BlueButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = White,
            disabledContainerColor = Gray300,
            disabledContentColor = Gray500
        )
        BlueButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = PrimaryLight,
            contentColor = White,
            disabledContainerColor = Gray300,
            disabledContentColor = Gray500
        )
        BlueButtonVariant.Outline -> ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Gray500
        )
        BlueButtonVariant.Ghost -> ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = Primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Gray500
        )
    }
    
    val shape = RoundedCornerShape(28.dp) // Pill shape
    
    val height = when (size) {
        BlueButtonSize.Small -> 40.dp
        BlueButtonSize.Medium -> 52.dp
        BlueButtonSize.Large -> 60.dp
    }
    
    val horizontalPadding = when (size) {
        BlueButtonSize.Small -> 16.dp
        BlueButtonSize.Medium -> 24.dp
        BlueButtonSize.Large -> 32.dp
    }
    
    when (variant) {
        BlueButtonVariant.Outline -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                    .height(height),
                enabled = enabled,
                colors = colors,
                shape = shape,
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(if (enabled) Primary else Gray300)
                )
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        BlueButtonVariant.Ghost -> {
            TextButton(
                onClick = onClick,
                modifier = modifier
                    .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                    .height(height),
                enabled = enabled,
                colors = colors,
                shape = shape,
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 12.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        else -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                    .height(height),
                enabled = enabled,
                colors = colors,
                shape = shape,
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

enum class BlueButtonVariant {
    Primary,    // Solid blue background
    Secondary,  // Light blue background
    Outline,    // Transparent with blue border
    Ghost       // Transparent, text only
}

enum class BlueButtonSize {
    Small,      // 40dp height
    Medium,     // 52dp height
    Large       // 60dp height
}
