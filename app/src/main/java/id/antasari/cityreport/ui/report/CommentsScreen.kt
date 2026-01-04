package id.antasari.cityreport.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.InteractionRepository
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    reportId: String,
    reportTitle: String = "Laporan",
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val interactionRepository = remember { InteractionRepository() }
    val authRepository = remember { AuthRepository() }
    
    var comments by remember { mutableStateOf<List<InteractionRepository.Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var newComment by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("User") }
    
    // Load comments and user
    LaunchedEffect(reportId) {
        scope.launch {
            // Get current user
            val userResult = authRepository.getCurrentUserWithRole()
            userResult.getOrNull()?.let {
                currentUserId = it.userId
                currentUserName = it.name.ifEmpty { "User" }
            }
            
            // Load comments
            val result = interactionRepository.getComments(reportId)
            if (result.isSuccess) {
                comments = result.getOrNull() ?: emptyList()
            }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Komentar", fontWeight = FontWeight.Bold)
                        Text(
                            text = reportTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        bottomBar = {
            // Comment input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BackgroundWhite,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(Spacing.Medium)
                        .imePadding(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tulis komentar...") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderPrimary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    
                    FloatingActionButton(
                        onClick = {
                            if (newComment.isNotBlank() && !isSending) {
                                isSending = true
                                scope.launch {
                                    val result = interactionRepository.addComment(
                                        reportId = reportId,
                                        userId = currentUserId,
                                        userName = currentUserName,
                                        content = newComment
                                    )
                                    if (result.isSuccess) {
                                        result.getOrNull()?.let {
                                            comments = listOf(it) + comments
                                        }
                                        newComment = ""
                                    }
                                    isSending = false
                                }
                            }
                        },
                        containerColor = Primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, "Kirim", tint = White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Belum ada komentar",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Text(
                        "Jadilah yang pertama berkomentar!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                                .background(AdaptiveColors.background),
                contentPadding = PaddingValues(Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                item {
                    Text(
                        text = "${comments.size} Komentar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = Spacing.Small)
                    )
                }
                
                items(comments, key = { it.id }) { comment ->
                    CommentCard(comment = comment)
                }
                
                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun CommentCard(comment: InteractionRepository.Comment) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundWhite,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = comment.userName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.userName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    Text(
                        text = formatTimeAgo(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

private fun formatTimeAgo(isoDate: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoDate.take(19)) ?: return "Baru saja"
        val now = Date()
        val diff = now.time - date.time
        
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)
        
        when {
            minutes < 1 -> "Baru saja"
            minutes < 60 -> "$minutes menit"
            hours < 24 -> "$hours jam"
            days < 7 -> "$days hari"
            else -> "${days / 7} minggu"
        }
    } catch (e: Exception) {
        "Baru saja"
    }
}
