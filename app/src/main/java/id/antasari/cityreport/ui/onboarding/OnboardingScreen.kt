package id.antasari.cityreport.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var page by remember { mutableStateOf(0) }

    val titles = listOf(
        "Lapor Masalah Publik",
        "Ambil Lokasi Otomatis",
        "Lihat Status Penanganan"
    )

    val subtitles = listOf(
        "Laporkan masalah di sekitar Anda dengan mudah dan cepat. Bantu tingkatkan kualitas lingkungan kota.",
        "Sistem akan mendeteksi lokasi Anda secara otomatis untuk mempermudah proses pelaporan.",
        "Pantau progress penanganan laporan Anda secara real-time hingga selesai ditangani."
    )

    val images = listOf(
        Icons.Default.Description,
        Icons.Default.LocationOn,
        Icons.Default.Check
    )

    // BACKGROUND GRADIENT
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF3E88FF),
                        Color(0xFF4AB0FF)
                    )
                )
            )
            .padding(24.dp)
    ) {

        // SKIP BUTTON
        Text(
            text = "Lewati",
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp)
        )

        // PAGE CONTENT
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {

            // ICON CIRCLE
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = images[page],
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = titles[page],
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitles[page],
                fontSize = 15.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 30.dp),
                lineHeight = 21.sp
            )
        }

        // PAGE INDICATORS
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(if (it == page) 18.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (it == page) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                )
            }
        }

        // BUTTON NEXT / START
        Button(
            onClick = {
                if (page < 2) page++
                else onFinish()
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .fillMaxWidth()
                .height(52.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.White
            )
        ) {
            Text(
                text = if (page == 2) "Mulai" else "Lanjut",
                fontSize = 16.sp,
                color = Color(0xFF1E6FFF)
            )
        }
    }
}
