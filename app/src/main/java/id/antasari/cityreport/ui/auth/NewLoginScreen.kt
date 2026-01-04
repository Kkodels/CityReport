package id.antasari.cityreport.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.data.model.UserRole
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.ui.components.PrimaryButton
import id.antasari.cityreport.ui.components.RoundedTextField
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NewLoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onRegisterClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    
    var selectedTab by remember { mutableStateOf(0) } // 0 = Masuk, 1 = Daftar
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
                        .background(AdaptiveColors.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(Spacing.ExtraLarge))
        
        // Icon Container
        Surface(
            modifier = Modifier
                .size(100.dp)
                .shadow(Elevation.Medium, RoundedCornerShape(CornerRadius.ExtraLarge)),
            shape = RoundedCornerShape(CornerRadius.ExtraLarge),
            color = BackgroundWhite
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_dialog_map),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(Modifier.height(Spacing.Large))
        
        // Greeting
        Text(
            text = "Selamat Datang",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(Spacing.Small))
        
        Text(
            text = "Laporkan masalah publik di sekitarmu\ndengan mudah dan cepat.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(Modifier.height(Spacing.ExtraLarge))
        
        // Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(SurfaceGray, RoundedCornerShape(CornerRadius.Medium)),
            horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
        ) {
            // Masuk Tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { selectedTab = 0 },
                shape = RoundedCornerShape(CornerRadius.Medium),
                color = if (selectedTab == 0) BackgroundWhite else Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Masuk",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedTab == 0) Primary else TextSecondary
                    )
                }
            }
            
            // Daftar Tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { selectedTab = 1 },
                shape = RoundedCornerShape(CornerRadius.Medium),
                color = if (selectedTab == 1) BackgroundWhite else Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Daftar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedTab == 1) Primary else TextSecondary
                    )
                }
            }
        }
        
        Spacer(Modifier.height(Spacing.Large))
        
        // Form Fields
        if ( selectedTab == 1) {
            RoundedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Nama Lengkap",
                leadingIcon = Icons.Default.Person,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(Spacing.Medium))
        }
        
        RoundedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "contoh@email.com",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        
        Spacer(Modifier.height(Spacing.Medium))
        
        RoundedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Kata Sandi",
            leadingIcon = Icons.Default.Lock,
            isPassword = !passwordVisible,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = TextSecondary
                    )
                }
            },
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
        
        if (selectedTab == 0) {
            Spacer(Modifier.height(Spacing.Small))
            Text(
                text = "Lupa Kata Sandi?",
                style = MaterialTheme.typography.bodyMedium,
                color = Primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { /* TODO */ }
            )
        }
        
        Spacer(Modifier.height(Spacing.Large))
        
        // Submit Button
        PrimaryButton(
            text = if (selectedTab == 0) "Masuk" else "Daftar",
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    if (selectedTab == 1 && name.isBlank()) {
                        Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        return@PrimaryButton
                    }
                    
                    isLoading = true
                    scope.launch {
                        try {
                            if (selectedTab == 0) {
                                // Login
                                val loginResult = authRepository.login(email, password)
                                if (loginResult.isSuccess) {
                                    val userWithRole = authRepository.getCurrentUserWithRole()
                                    if (userWithRole.isSuccess) {
                                        val profile = userWithRole.getOrNull()
                                        onLoginSuccess(profile?.role ?: UserRole.USER)
                                    } else {
                                        Toast.makeText(context, "Gagal mendapatkan profil", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val errorMsg = loginResult.exceptionOrNull()?.message ?: "Login gagal"
                                    val userMsg = when {
                                        errorMsg.contains("rate limit", ignoreCase = true) -> 
                                            "Server sibuk. Tunggu 1 menit lalu coba lagi."
                                        errorMsg.contains("Invalid credentials", ignoreCase = true) -> 
                                            "Email atau password salah"
                                        errorMsg.contains("network", ignoreCase = true) -> 
                                            "Koneksi internet bermasalah"
                                        else -> errorMsg
                                    }
                                    Toast.makeText(context, userMsg, Toast.LENGTH_LONG).show()
                                }
                            } else {
                                // Register
                                val registerResult = authRepository.register(name, email, password)
                                if (registerResult.isSuccess) {
                                    Toast.makeText(context, "Registrasi berhasil! Silakan login.", Toast.LENGTH_SHORT).show()
                                    selectedTab = 0
                                    name = ""
                                } else {
                                    val errorMsg = registerResult.exceptionOrNull()?.message ?: "Registrasi gagal"
                                    val userMsg = when {
                                        errorMsg.contains("rate limit", ignoreCase = true) -> 
                                            "Server sibuk. Tunggu 1 menit lalu coba lagi."
                                        errorMsg.contains("already exists", ignoreCase = true) -> 
                                            "Email sudah terdaftar"
                                        else -> errorMsg
                                    }
                                    Toast.makeText(context, userMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            val userMsg = when {
                                e.message?.contains("rate limit", ignoreCase = true) == true -> 
                                    "⏱️ Server sibuk. Silakan tunggu 1 menit lalu coba lagi."
                                else -> e.message ?: "Terjadi kesalahan"
                            }
                            Toast.makeText(context, userMsg, Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Mohon isi semua field", Toast.LENGTH_SHORT).show()
                }
            },
            isLoading = isLoading
        )
        
        Spacer(Modifier.height(Spacing.Large))
        
        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = BorderPrimary)
            Text(
                text = "ATAU MASUK DENGAN",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
            Divider(modifier = Modifier.weight(1f), color = BorderPrimary)
        }
        
        Spacer(Modifier.height(Spacing.Large))
        
        // Google Sign In Button
        OutlinedButton(
            onClick = { /* TODO: Google Sign In */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(CornerRadius.Medium),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = BackgroundWhite
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("G", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Google", style = MaterialTheme.typography.labelLarge)
            }
        }
        
        Spacer(Modifier.height(Spacing.ExtraLarge))
        
        // Development: Force Logout Button
        TextButton(
            onClick = {
                scope.launch {
                    try {
                        authRepository.logout()
                        Toast.makeText(context, "✅ Logout berhasil! Session dihapus.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Session sudah habis", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔓 Force Logout (Dev)",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}
