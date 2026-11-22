package com.resqsync.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.LocationSource
import com.resqsync.app.ui.theme.ResQSyncCyan
import com.resqsync.app.ui.theme.ResQSyncEmergencyRed
import kotlin.math.roundToInt

data class OfflineLandmarkPreset(
    val name: String,
    val area: String,
    val latitude: Double,
    val longitude: Double
)

val DEFAULT_OFFLINE_LANDMARKS = listOf(
    OfflineLandmarkPreset("Central Hospital & Emergency Zone", "District Center", 18.5204, 73.8567),
    OfflineLandmarkPreset("High School Relief Shelter", "North Sector", 18.5312, 73.8445),
    OfflineLandmarkPreset("Market Square & Bus Terminal", "East Sector", 18.5109, 73.8621),
    OfflineLandmarkPreset("River Bridge Crossing", "South Sector", 18.4980, 73.8500),
    OfflineLandmarkPreset("Industrial Zone Warehouse", "West Sector", 18.5250, 73.8310)
)

@Composable
fun OfflineMapPicker(
    initialLat: Double? = null,
    initialLong: Double? = null,
    onLocationSelected: (EmergencyLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val baseLat = 18.5204
    val baseLong = 73.8567

    var pinOffset by remember {
        mutableStateOf(Offset(250f, 250f))
    }

    var selectedLat by remember {
        mutableStateOf(initialLat ?: baseLat)
    }

    var selectedLong by remember {
        mutableStateOf(initialLong ?: baseLong)
    }

    var selectedLandmarkName by remember {
        mutableStateOf("Interactive Grid Map Target")
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Offline Interactive Map",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = onDismiss) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                text = "Tap on the grid map or select a preset landmark below to set location without internet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Canvas Grid Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFF04101D), RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                pinOffset = tapOffset
                                // Convert canvas offset (0..width, 0..height) to Lat/Long delta
                                val latDelta = (250f - tapOffset.y) * 0.0001
                                val longDelta = (tapOffset.x - 250f) * 0.0001
                                selectedLat = ((baseLat + latDelta) * 10000.0).roundToInt() / 10000.0
                                selectedLong = ((baseLong + longDelta) * 10000.0).roundToInt() / 10000.0
                                selectedLandmarkName = "Grid Selection (${selectedLat}, ${selectedLong})"
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw grid lines
                    val step = 40f
                    var x = 0f
                    while (x < width) {
                        drawLine(
                            color = Color(0xFF102D46),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1f
                        )
                        x += step
                    }

                    var y = 0f
                    while (y < height) {
                        drawLine(
                            color = Color(0xFF102D46),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }

                    // Draw simulated river path
                    val riverPath = Path().apply {
                        moveTo(0f, height * 0.3f)
                        cubicTo(width * 0.3f, height * 0.2f, width * 0.7f, height * 0.6f, width, height * 0.5f)
                    }
                    drawPath(riverPath, Color(0xFF00C5D2).copy(alpha = 0.3f), style = Stroke(width = 12f))

                    // Draw target pin
                    drawCircle(ResQSyncEmergencyRed, radius = 10f, center = pinOffset)
                    drawCircle(Color.White, radius = 4f, center = pinOffset)
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Lat: $selectedLat | Long: $selectedLong",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ResQSyncCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Offline Preset Landmarks Selector
            Text(
                text = "Preset Local Landmarks (Offline Sector Cache)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DEFAULT_OFFLINE_LANDMARKS.take(3).forEach { landmark ->
                    FilterChip(
                        selected = selectedLat == landmark.latitude && selectedLong == landmark.longitude,
                        onClick = {
                            selectedLat = landmark.latitude
                            selectedLong = landmark.longitude
                            selectedLandmarkName = landmark.name
                        },
                        label = {
                            Text("${landmark.name} (${landmark.area})", fontSize = 12.sp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    onLocationSelected(
                        EmergencyLocation(
                            latitude = selectedLat,
                            longitude = selectedLong,
                            source = LocationSource.MANUAL_MAP,
                            landmark = selectedLandmarkName,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ResQSyncCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Confirm Selected Location",
                    color = MaterialTheme.colorScheme.background,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
