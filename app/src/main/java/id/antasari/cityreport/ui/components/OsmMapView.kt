package id.antasari.cityreport.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Composable wrapper for osmdroid MapView
 * 
 * @param initialLat Initial latitude for map center
 * @param initialLng Initial longitude for map center
 * @param onLocationChanged Callback when location changes (marker moved)
 * @param isInteractive Whether the map is interactive (draggable) or read-only
 */
@Composable
fun OsmMapView(
    initialLat: Double?,
    initialLng: Double?,
    onLocationChanged: ((Double, Double) -> Unit)? = null,
    isInteractive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    
    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(isInteractive)
                    
                    // Set initial position
                    val lat = initialLat ?: -6.2088 // Default to Jakarta
                    val lng = initialLng ?: 106.8456
                    val startPoint = GeoPoint(lat, lng)
                    controller.setZoom(15.0)
                    controller.setCenter(startPoint)
                    
                    // Add marker
                    if (isInteractive) {
                        val marker = Marker(this).apply {
                            position = startPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Lokasi Laporan"
                            isDraggable = true
                            
                            // Update location when marker is dragged
                            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                override fun onMarkerDrag(marker: Marker) {}
                                
                                override fun onMarkerDragEnd(marker: Marker) {
                                    onLocationChanged?.invoke(
                                        marker.position.latitude,
                                        marker.position.longitude
                                    )
                                }
                                
                                override fun onMarkerDragStart(marker: Marker) {}
                            })
                        }
                        overlays.add(marker)
                        currentMarker = marker
                    } else {
                        // Static marker for detail view
                        val marker = Marker(this).apply {
                            position = startPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Lokasi Laporan"
                        }
                        overlays.add(marker)
                    }
                }
            },
            update = { mapView ->
                // Update map when location changes externally
                if (initialLat != null && initialLng != null) {
                    val newPoint = GeoPoint(initialLat, initialLng)
                    currentMarker?.position = newPoint
                    if (!isInteractive) {
                        // For read-only maps, center on the location
                        mapView.controller.setCenter(newPoint)
                    }
                    mapView.invalidate()
                }
            }
        )
        
        // Info card for interactive mode
        if (isInteractive) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Geser peta atau seret marker untuk menentukan lokasi",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
