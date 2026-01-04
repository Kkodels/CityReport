package id.antasari.cityreport.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun CityReportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val lightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    
    secondary = PrimaryLight,
    onSecondary = White,
    secondaryContainer = PrimaryLight,
    onSecondaryContainer = PrimaryDark,
    
    tertiary = AccentGreen,
    onTertiary = White,
    
    error = Error,
    onError = White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    
    surface = BackgroundWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,
    
    outline = BorderPrimary,
    outlineVariant = BorderSecondary,
    
    scrim = Black.copy(alpha = 0.3f)
)

private val darkScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Gray900,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Gray100,
    
    secondary = PrimaryLight,
    onSecondary = Gray900,
    
    tertiary = AccentGreen,
    onTertiary = Gray900,
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    
    background = Gray900,
    onBackground = Gray100,
    
    surface = Gray800,
    onSurface = Gray100,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray300,
    
    outline = Gray600,
    outlineVariant = Gray700
)
