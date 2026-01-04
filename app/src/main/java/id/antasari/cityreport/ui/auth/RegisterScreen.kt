package id.antasari.cityreport.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.ui.components.BlueButton
import id.antasari.cityreport.ui.components.BlueButtonSize
import id.antasari.cityreport.ui.components.BlueTextField
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
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
            
            // Logo
            Text(
                text = "🏙️",
                fontSize = 64.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "CityReport",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            
            Text(
                text = "Buat akun untuk mulai melaporkan",
                fontSize = 16.sp,
                color = White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Register Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Daftar",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = "Isi data diri Anda",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )
                    
                    // Name Field
                    BlueTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = "Nama Lengkap",
                        placeholder = "John Doe",
                        leadingIcon = Icons.Default.Person,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Email Field
                    BlueTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = "Email",
                        placeholder = "nama@email.com",
                        leadingIcon = Icons.Default.Email,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Password Field
                    BlueTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = "Password",
                        placeholder = "Minimal 6 karakter",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Confirm Password Field
                    BlueTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; error = null },
                        label = "Konfirmasi Password",
                        placeholder = "Ketik ulang password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        enabled = !isLoading,
                        isError = error != null,
                        errorMessage = error,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Register Button
                    BlueButton(
                        text = if (isLoading) "Mendaftar..." else "Daftar",
                        onClick = {
                            when {
                                name.isBlank() -> error = "Nama harus diisi"
                                email.isBlank() -> error = "Email harus diisi"
                                password.isBlank() -> error = "Password harus diisi"
                                password.length < 6 -> error = "Password minimal 6 karakter"
                                password != confirmPassword -> error = "Password tidak cocok"
                                else -> {
                                    isLoading = true
                                    error = null
                                    scope.launch {
                                        val result = authRepository.register(name, email, password)
                                        
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                                            onRegisterSuccess()
                                        } else {
                                            isLoading = false
                                            error = result.exceptionOrNull()?.message ?: "Registrasi gagal"
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
                    
                    // Login Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sudah punya akun?",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = onBackToLogin) {
                            Text(
                                text = "Masuk",
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
