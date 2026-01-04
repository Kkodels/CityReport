package id.antasari.cityreport.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.data.model.UserRole
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.delay

/**
 * DEV ONLY: Bypass login for testing UI
 * Remove this before production!
 */
@Composable
fun DevBypassLoginScreen(
    onLoginSuccess: (UserRole) -> Unit
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        // Auto login after 1 second
        delay(1000)
        onLoginSuccess(UserRole.USER)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            CircularProgressIndicator(color = Primary)
            
            Spacer(Modifier.height(Spacing.Medium))
            
            Text(
                text = "🚀 DEV MODE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            
            Text(
                text = "Auto login...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(Modifier.height(Spacing.Large))
            
            Surface(
                color = StatusDaruratBg,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.Small)
            ) {
                Text(
                    text = "⚠️ Bypassing Appwrite\nRemove before production!",
                    modifier = Modifier.padding(Spacing.Medium),
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusDaruratText
                )
            }
        }
    }
}
