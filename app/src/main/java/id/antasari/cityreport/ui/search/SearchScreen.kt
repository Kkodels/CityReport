package id.antasari.cityreport.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.components.NewReportCard
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    var searchQuery by remember { mutableStateOf("") }
    var allReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Load all reports on init
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val result = reportsRepository.getAllReports()
            if (result.isSuccess) {
                allReports = result.getOrNull() ?: emptyList()
            }
            isLoading = false
        }
        
        // Auto-focus search field
        focusRequester.requestFocus()
    }
    
    // Filter reports based on search query
    val filteredReports = remember(searchQuery, allReports) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allReports.filter { report ->
                report.title.contains(searchQuery, ignoreCase = true) ||
                report.category.contains(searchQuery, ignoreCase = true) ||
                report.locationName.contains(searchQuery, ignoreCase = true) ||
                report.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            isSearchActive = it.isNotBlank()
                        },
                        placeholder = { 
                            Text(
                                "Cari laporan atau lokasi...",
                                color = TextHint
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = TextTertiary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    isSearchActive = false
                                }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = TextTertiary)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BackgroundWhite,
                            unfocusedContainerColor = BackgroundWhite,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                            .background(AdaptiveColors.background)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (!isSearchActive || searchQuery.isBlank()) {
                // Show search suggestions/empty state
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(Spacing.Large),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Search,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = TextTertiary
                    )
                    Spacer(Modifier.height(Spacing.Medium))
                    Text(
                        "Cari Laporan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spacing.Small))
                    Text(
                        "Masukkan kata kunci untuk mencari laporan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else if (filteredReports.isEmpty()) {
                // No results
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(Spacing.Large),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "❌",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(Spacing.Medium))
                    Text(
                        "Tidak ada hasil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spacing.Small))
                    Text(
                        "Coba kata kunci lain",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                // Show results
                Column(Modifier.fillMaxSize()) {
                    Text(
                        "${filteredReports.size} hasil ditemukan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(Spacing.Large)
                    )
                    
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = Spacing.Large,
                            vertical = Spacing.Small
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        items(filteredReports) { report ->
                            NewReportCard(
                                title = report.title,
                                status = report.status,
                                category = report.category,
                                location = report.locationName,
                                timeAgo = getTimeAgo(report.createdAt),
                                createdAt = report.createdAt,
                                imageUrl = report.photoId,
                                severity = report.severity,
                                votes = report.votes,
                                onClick = { onNavigateToDetail(report.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getTimeAgo(createdAt: String): String {
    val timestamp = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(createdAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Baru saja"
        diff < 3600_000 -> "${diff / 60_000}m yang lalu"
        diff < 86400_000 -> "${diff / 3600_000}h yang lalu"
        diff < 604800_000 -> "${diff / 86400_000}d yang lalu"
        else -> SimpleDateFormat("dd MMM yyyy", Locale("id")).format(Date(timestamp))
    }
}
