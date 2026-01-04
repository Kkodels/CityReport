package id.antasari.cityreport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.ui.theme.*

@Composable
fun VotingSection(
    voteCount: Int,
    hasVoted: Boolean,
    onVote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = BackgroundWhite,
        shadowElevation = Elevation.Low
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vote info
            Column {
                Text(
                    "Dukung Laporan Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$voteCount orang mendukung",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            // Vote button
            Button(
                onClick = onVote,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasVoted) androidx.compose.ui.graphics.Color(0xFF4CAF50) else Primary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(CornerRadius.Small)
            ) {
                Icon(
                    if (hasVoted) Icons.Default.CheckCircle else Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(if (hasVoted) "Didukung" else "Dukung")
            }
        }
    }
}

@Composable
fun CommentsSection(
    userName: String,
    modifier: Modifier = Modifier,
    onAddComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        Text(
            "Komentar & Update",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Add comment input
        Surface(
            shape = RoundedCornerShape(CornerRadius.Medium),
            color = BackgroundWhite,
            shadowElevation = Elevation.Low
        ) {
            Column(
                Modifier.padding(Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Tambahkan komentar...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(CornerRadius.Small)
                )
                
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Kirim")
                    }
                }
            }
        }
        
        // Comments list placeholder
        Text(
            "Belum ada komentar. Jadilah yang pertama!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary,
            modifier = Modifier.padding(vertical = Spacing.Medium)
        )
    }
}

@Composable
fun CommentItem(
    name: String,
    message: String,
    timeAgo: String,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Small),
        color = if (isAdmin) androidx.compose.ui.graphics.Color(0xFFF3F4F6) else BackgroundWhite,
        shadowElevation = if (isAdmin) 0.dp else Elevation.Low
    ) {
        Row(
            Modifier.padding(Spacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            // Avatar
            Box(
                Modifier
                    .size(40.dp)
                    .background(
                        if (isAdmin) Primary else SurfaceGray,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isAdmin) androidx.compose.ui.graphics.Color.White else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Comment content
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isAdmin) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Primary
                            ) {
                                Text(
                                    "Admin",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
