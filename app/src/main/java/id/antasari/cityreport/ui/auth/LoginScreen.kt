package id.antasari.cityreport.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.cityreport.data.model.UserRole
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.ui.components.BlueButton
import id.antasari.cityreport.ui.components.BlueButtonSize
import id.antasari.cityreport.ui.components.BlueButtonVariant
import id.antasari.cityreport.ui.components.BlueTextField
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            
            // Logo/Icon placeholder
            Text(
                text = "🏙️",
                fontSize = 64.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Title
            Text(
                text = "CityReport",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            
            Text(
                text = "Laporkan masalah kota Anda",
                fontSize = 16.sp,
                color = White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Login Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Title
                    Text(
                        text = "Masuk",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = "Selamat datang kembali!",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )
                    
                    // Email Field
                    BlueTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = "Email",
                        placeholder = "nama@email.com",
                        leadingIcon = Icons.Default.Email,
                        enabled = !isLoading,
                        isError = error != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Password Field
                    BlueTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = "Password",
                        placeholder = "Masukkan password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        enabled = !isLoading,
                        isError = error != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Login Button
                    BlueButton(
                        text = if (isLoading) "Memproses..." else "Masuk",
                        onClick = {
                            when {
                                email.isBlank() -> error = "Email harus diisi"
                                password.isBlank() -> error = "Password harus diisi"
                                else -> {
                                    isLoading = true
                                    error = null
                                    scope.launch {
                                        // Logout first to clear any existing session
                                        authRepository.logout()
                                        
                                        // Login
                                        val loginResult = authRepository.login(email, password)
                                        
                                        if (loginResult.isSuccess) {
                                            // Get user profile with role
                                            val profileResult = authRepository.getCurrentUserWithRole()
                                            
                                            if (profileResult.isSuccess) {
                                                val profile = profileResult.getOrNull()
                                                if (profile != null) {
                                                    Toast.makeText(context, "Login berhasil!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess(profile.role)
                                                } else {
                                                    isLoading = false
                                                    error = "Gagal mendapatkan profil pengguna"
                                                }
                                            } else {
                                                isLoading = false
                                                error = profileResult.exceptionOrNull()?.message ?: "Gagal mendapatkan profil"
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            isLoading = false
                                            error = loginResult.exceptionOrNull()?.message ?: "Login gagal"
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        fullWidth = true,
                        size = BlueButtonSize.Large
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Register Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Belum punya akun?",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = onRegisterClick) {
                            Text(
                                text = "Daftar",
                                color = Primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
