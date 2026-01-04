package id.antasari.cityreport.ui.nearby

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import id.antasari.cityreport.data.repository.ReportsRepository
import id.antasari.cityreport.domain.model.Report
import id.antasari.cityreport.ui.theme.*
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun NewNearbyReportsScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reportsRepository = remember { ReportsRepository() }
    
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("Semua") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load reports
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val result = reportsRepository.getAllReports()
                reports = result.getOrNull() ?: emptyList()
                isLoading = false
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading reports", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        }
    }
    
    // Configure OSMDroid
    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // OSMDroid Map
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    
                    // Set initial position (Jakarta)
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(-6.2088, 106.8456))
                    
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Top Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(Spacing.Medium)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                items(listOf("Semua", "Terbaru", "Terdekat")) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }
        
        // My Location Button
        FloatingActionButton(
            onClick = {
                mapView?.controller?.animateTo(GeoPoint(-6.2088, 106.8456))
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = Spacing.Medium, bottom = 100.dp),
            containerColor = Color.White,
            contentColor = Primary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.MyLocation, "My Location")
        }
        
        // Create Report FAB
        FloatingActionButton(
            onClick = onNavigateToCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Spacing.Medium, bottom = 100.dp),
            containerColor = Primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                Icon(Icons.Default.Add, "Lapor")
                Text("Lapor", fontWeight = FontWeight.Bold)
            }
        }
        
        // Loading
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
    
    // Add markers when reports loaded
    LaunchedEffect(reports, mapView) {
        mapView?.let { map ->
            map.overlays.clear()
            
            reports.forEach { report ->
                val marker = Marker(map).apply {
                    position = GeoPoint(report.latitude, report.longitude)
                    title = report.title
                    snippet = report.category
                    
                    setOnMarkerClickListener { clickedMarker, _ ->
                        Toast.makeText(
                            context,
                            clickedMarker.title,
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                }
                map.overlays.add(marker)
            }
            map.invalidate()
        }
    }
}
