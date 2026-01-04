package id.antasari.cityreport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.antasari.cityreport.ui.theme.Primary
import id.antasari.cityreport.ui.theme.White

@Composable
fun ProfilePhotoCircle(
    photoUrl: String?,
    userName: String,
    size: Dp = 48.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            // Display actual photo
            android.util.Log.d("ProfilePhotoCircle", "Loading photo URL: $photoUrl")
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onError = { error ->
                    android.util.Log.e("ProfilePhotoCircle", "Image load error: ${error.result.throwable.message}")
                },
                onSuccess = {
                    android.util.Log.d("ProfilePhotoCircle", "Image loaded successfully")
                }
            )
        } else {
            // Default avatar with initials
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (userName.isNotEmpty()) {
                    Text(
                        text = userName.take(1).uppercase(),
                        color = White,
                        fontSize = (size.value * 0.4).sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
        }
    }
}
