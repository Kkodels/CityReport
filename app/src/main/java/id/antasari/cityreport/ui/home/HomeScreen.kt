package id.antasari.cityreport.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import id.antasari.cityreport.data.repository.AuthRepository
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.data.repository.StorageRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.BlueCard
import id.antasari.cityreport.ui.components.BlueCardPadding
import id.antasari.cityreport.ui.components.StatusPill
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    val reportsRepository = remember { ReportsRepository() }
    
    var userName by remember { mutableStateOf("User") }
    var allReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load data
    LaunchedEffect(Unit) {
        scope.launch {
            // Get user name
            val userResult = authRepository.getCurrentUserWithRole()
            if (userResult.isSuccess) {
                userName = userResult.getOrNull()?.name ?: "User"
            }
            
            // Get all reports
            val reportsResult = reportsRepository.getAllReports()
            if (reportsResult.isSuccess) {
                allReports = reportsResult.getOrNull() ?: emptyList()
            }
            isLoading = false
        }
    }
    
    val recentReports = allReports.take(5)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundTertiary)
    ) {
        // Header with Gradient
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
            ) {
                Column {
                    // Top row: Greeting + Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Selamat Datang,",
                                fontSize = 14.sp,
                                color = White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = userName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { /* TODO: Notifications */ },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = { onNavigate("profile") },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    // Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Cari laporan...",
                                color = TextTertiary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Feature Cards (elevated above content)
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height((-24).dp)) // Overlap with header
                
                // Create Report Card
                FeatureCard(
                    icon = Icons.Default.Add,
                    title = "Buat Laporan",
                    description = "Laporkan masalah publik di sekitar Anda",
                    color = Primary,
                    onClick = { onNavigate("report_form") }
                )
                
                // My Reports Card
                FeatureCard(
                    icon = Icons.Default.List,
                    title = "Laporan Saya",
                    description = "Lihat status laporan yang Anda buat",
                    color = PrimaryLight,
                    onClick = { onNavigate("report_list") }
                )
                
                // Nearby Card
                FeatureCard(
                    icon = Icons.Default.LocationOn,
                    title = "Di Sekitar",
                    description = "Lihat laporan di sekitar lokasi Anda",
                    color = Success,
                    onClick = { onNavigate("nearby_reports") }
                )
            }
        }
        
        // Recent Reports Section
        item {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Laporan Terbaru",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = { onNavigate("report_list") }) {
                    Text(
                        text = "Lihat Semua",
                        color = Primary,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        
        // Recent Reports List
        items(recentReports) { report ->
            ReportCardItem(
                report = report,
                onClick = { onNavigate("report_detail/${report.id}") },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
        
        // Empty state
        if (!isLoading && recentReports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📭", fontSize = 56.sp)
                        Text(
                            "Belum ada laporan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            "Buat laporan pertama Anda",
                            fontSize = 14.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
        
        // Bottom padding for nav bar
        item {
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BlueCard(
        onClick = onClick,
        padding = BlueCardPadding.Medium,
        elevation = 4,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ReportCardItem(
    report: Report,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val storageRepository = remember { StorageRepository() }
    
    BlueCard(
        onClick = onClick,
        padding = BlueCardPadding.Small,
        elevation = 2,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto Laporan - Tampilkan foto asli atau placeholder
            if (report.photoId != null) {
                AsyncImage(
                    model = storageRepository.getPhotoUrl(report.photoId),
                    contentDescription = report.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder jika tidak ada foto
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Gray200, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = report.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Text(
                    text = report.category,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                
                Spacer(Modifier.height(8.dp))
                
                StatusPill(status = report.status, animated = false)
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = formatDate(report.createdAt),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = report.locationName.take(20),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
