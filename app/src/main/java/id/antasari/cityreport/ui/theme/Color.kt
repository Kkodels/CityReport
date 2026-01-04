package id.antasari.cityreport.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors
val Primary = Color(0xFF2196F3)        // Main blue
val PrimaryLight = Color(0xFF64B5F6)   // Light blue
val PrimaryDark = Color(0xFF1976D2)    // Dark blue
val PrimaryVariant = Color(0xFF1E88E5)

// Gradient Colors
val GradientStart = Color(0xFF2196F3)
val GradientEnd = Color(0xFF1976D2)

// Status Colors
val StatusInProgress = Color(0xFFFFC107)  // Yellow
val StatusDiproses = Color(0xFF2196F3)    // Blue
val StatusSelesai = Color(0xFF4CAF50)     // Green
val StatusMenunggu = Color(0xFFFF9800)    // Orange
val StatusDarurat = Color(0xFFFF5252)     // Red
val StatusTinggi = Color(0xFFFF9800)      // Orange
val StatusPending = StatusMenunggu

// Status Background (lighter versions)
val StatusInProgressBg = Color(0xFFFFF3CD)
val StatusDiprosesBg = Color(0xFFE3F2FD)
val StatusSelesaiBg = Color(0xFFE8F5E9)
val StatusMenungguBg = Color(0xFFFFE0B2)
val StatusDaruratBg = Color(0xFFFFEBEE)

// Status Text (for badges)
val StatusInProgressText = Color(0xFF856404)
val StatusDiprosesText = Color(0xFF0D47A1)
val StatusSelesaiText = Color(0xFF2E7D32)
val StatusMenungguText = Color(0xFFE65100)
val StatusDaruratText = Color(0xFFC62828)

// Category Colors
val CategoryJalanRusak = Color(0xFF2196F3)    // Blue
val CategorySampah = Color(0xFFFF9800)        // Orange
val CategoryBanjir = Color(0xFF00BCD4)        // Cyan
val CategoryLampuJalan = Color(0xFFFFC107)    // Yellow
val CategoryFasilitas = Color(0xFF4CAF50)     // Green
val CategoryLainnya = Color(0xFF9C27B0)       // Purple

// Category Backgrounds (light versions)
val CategoryJalanRusakBg = Color(0xFFE3F2FD)
val CategorySampahBg = Color(0xFFFFE0B2)
val CategoryBanjirBg = Color(0xFFE0F7FA)
val CategoryLampuJalanBg = Color(0xFFFFF9C4)
val CategoryFasilitasBg = Color(0xFFE8F5E9)
val CategoryLainnyaBg = Color(0xFFF3E5F5)

// Background Colors
val BackgroundPrimary = Color(0xFFF5F7FA)  // Main app background
val BackgroundWhite = Color(0xFFFFFFFF)
val BackgroundCard = Color(0xFFFFFFFF)
val SurfaceGray = Color(0xFFF8F9FA)
val SurfaceLight = Color(0xFFFAFAFA)

// Text Colors
val TextPrimary = Color(0xFF212121)      // Almost black
val TextSecondary = Color(0xFF757575)    // Medium gray
val TextTertiary = Color(0xFF9E9E9E)     // Light gray
val TextHint = Color(0xFFBDBDBD)         // Very light gray

// Accent Colors
val AccentRed = Color(0xFFFF5252)
val AccentBlue = Color(0xFF2196F3)
val AccentGreen = Color(0xFF4CAF50)
val AccentOrange = Color(0xFFFF9800)
val AccentYellow = Color(0xFFFFC107)

// UI Element Colors
val BorderPrimary = Color(0xFFE0E0E0)
val BorderSecondary = Color(0xFFEEEEEE)
val Divider = Color(0xFFE0E0E0)

// Grays
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val IconColor = Color(0xFF757575)

// Backwards compatibility aliases for old components
val BackgroundSecondary = SurfaceGray
val BackgroundTertiary = BackgroundPrimary
val StatusPendingText = StatusMenungguText
val StatusPendingBorder = StatusMenunggu.copy(alpha = 0.3f)
val StatusInProgressBorder = StatusInProgress.copy(alpha = 0.3f)
val StatusCompleted = StatusSelesai
val StatusCompletedText = StatusSelesaiText
val StatusCompletedBorder = StatusSelesai.copy(alpha = 0.3f)
val StatusRejected = Color(0xFFE53935)
val StatusNew = Primary
val PriorityHigh = Color(0xFFFF5252)
val PriorityMedium = Color(0xFFFFC107)
val PriorityLow = Color(0xFF4CAF50)

val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)

// Special Colors
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Transparent = Color(0x00000000)

// Success, Warning, Error, Info
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFFF5252)
val Info = Color(0xFF2196F3)

// Shadow colors
val ShadowLight = Color(0x1A000000)  // 10% black
val ShadowMedium = Color(0x33000000) // 20% black
val ShadowDark = Color(0x4D000000)   // 30% black

// Dark Mode Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkCard = Color(0xFF2C2C2C)
val DarkTextPrimary = Color(0xFFE1E1E1)
val DarkTextSecondary = Color(0xFFB0B0B0)
val DarkTextTertiary = Color(0xFF808080)

/**
 * Adaptive colors - returns appropriate color based on dark mode state
 */
object AdaptiveColors {
    val background: Color
        get() = if (ThemeManager.isDarkMode) DarkBackground else BackgroundPrimary
    
    val surface: Color
        get() = if (ThemeManager.isDarkMode) DarkSurface else BackgroundWhite
    
    val card: Color
        get() = if (ThemeManager.isDarkMode) DarkCard else BackgroundCard
    
    val textPrimary: Color
        get() = if (ThemeManager.isDarkMode) DarkTextPrimary else TextPrimary
    
    val textSecondary: Color
        get() = if (ThemeManager.isDarkMode) DarkTextSecondary else TextSecondary
    
    val textTertiary: Color
        get() = if (ThemeManager.isDarkMode) DarkTextTertiary else TextTertiary
    
    val divider: Color
        get() = if (ThemeManager.isDarkMode) Color(0xFF404040) else Divider
}
