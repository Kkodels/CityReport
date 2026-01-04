package id.antasari.cityreport.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.cityreport.R
import id.antasari.cityreport.data.model.UserRole
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.ui.navigation.Routes
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    
    // Trigger animation on start
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Auto-navigate after checking session
    LaunchedEffect(Unit) {
        scope.launch {
            delay(1500) // Show splash for 1.5s
            
            // Check if user already logged in
            val isLoggedIn = authRepository.isLoggedIn()
            
            if (isLoggedIn) {
                // Get user role to navigate to correct screen
                val userResult = authRepository.getCurrentUserWithRole()
                
                if (userResult.isSuccess) {
                    val role = userResult.getOrNull()?.role
                    android.util.Log.d("SplashScreen", "✅ Session found! Role: $role")
                    
                    // Auto-navigate based on role
                    val destination = when (role) {
                        UserRole.ADMIN -> Routes.HOME_ADMIN
                        UserRole.USER -> Routes.HOME_USER
                        else -> Routes.LOGIN
                    }
                    android.util.Log.d("SplashScreen", "🚀 Auto-navigating to: $destination")
                    onNavigate(destination)
                } else {
                    // Session exists but profile fetch failed
                    android.util.Log.d("SplashScreen", "⚠️ Session invalid, show login")
                    onNavigate(Routes.LOGIN)
                }
            } else {
                // No session, show login
                android.util.Log.d("SplashScreen", "ℹ️ No session, show login")
                onNavigate(Routes.LOGIN)
            }
        }
    }
    
    // Fade in animation
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.Large)
        ) {
            // App Icon Container
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(
                        elevation = Elevation.Medium,
                        shape = RoundedCornerShape(CornerRadius.ExtraLarge)
                    ),
                shape = RoundedCornerShape(CornerRadius.ExtraLarge),
                color = BackgroundWhite
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(Spacing.Large)
                ) {
                    // Building icon placeholder
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_dialog_map),
                        contentDescription = "App Icon",
                        tint = Primary,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    // Red notification dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .size(12.dp)
                            .background(AccentRed, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
            
            Spacer(Modifier.height(Spacing.Medium))
            
            // Logo Text
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("City")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Report")
                    }
                },
                style = MaterialTheme.typography.displayLarge
            )
            
            // Tagline
            Text(
                text = "Lapor lebih mudah, kota lebih baik",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            
            Spacer(Modifier.height(Spacing.Large))
            
            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Primary,
                strokeWidth = 3.dp
            )
        }
        
        // Footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Spacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Powered by Pemerintah Kegelapan",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
            Spacer(Modifier.height(Spacing.ExtraSmall))
            Text(
                text = "v1.6.7",
                style = MaterialTheme.typography.bodySmall,
                color = Primary
            )
        }
    }
}
