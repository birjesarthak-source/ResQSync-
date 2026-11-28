package com.resqsync.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.LocationSource
import com.resqsync.app.ui.theme.ResQSyncEmergencyRed
import com.resqsync.app.ui.theme.ResQSyncPrimaryBlue
import kotlin.math.round

@Composable
fun OfflineMapPicker(
    initialLat: Double? = null,
    initialLong: Double? = null,
    onLocationSelected: (EmergencyLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var centerLatitude by remember { mutableStateOf(initialLat?.toString().orEmpty()) }
    var centerLongitude by remember { mutableStateOf(initialLong?.toString().orEmpty()) }
    var selectedLatitude by remember { mutableStateOf(initialLat) }
    var selectedLongitude by remember { mutableStateOf(initialLong) }
    var spanDegrees by remember { mutableFloatStateOf(0.02f) }

    val parsedCenterLatitude = centerLatitude.toDoubleOrNull()
        ?.takeIf { it in -90.0..90.0 }
    val parsedCenterLongitude = centerLongitude.toDoubleOrNull()
        ?.takeIf { it in -180.0..180.0 }
    val hasValidCenter = parsedCenterLatitude != null && parsedCenterLongitude != null
    val hasSelectedLocation = selectedLatitude != null && selectedLongitude != null

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Offline location picker",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }

            Text(
                text = "This works without map tiles. Set a known coordinate center, then tap the grid to choose an approximate location.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = centerLatitude,
                    onValueChange = { value ->
                        centerLatitude = value.filterCoordinateInput()
                        selectedLatitude = centerLatitude.toDoubleOrNull()
                            ?.takeIf { it in -90.0..90.0 }
                        selectedLongitude = centerLongitude.toDoubleOrNull()
                            ?.takeIf { it in -180.0..180.0 }
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Center latitude") },
                    placeholder = { Text("e.g. 18.5204") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = centerLatitude.isNotBlank() && parsedCenterLatitude == null
                )
                OutlinedTextField(
                    value = centerLongitude,
                    onValueChange = { value ->
                        centerLongitude = value.filterCoordinateInput()
                        selectedLatitude = centerLatitude.toDoubleOrNull()
                            ?.takeIf { it in -90.0..90.0 }
                        selectedLongitude = centerLongitude.toDoubleOrNull()
                            ?.takeIf { it in -180.0..180.0 }
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Center longitude") },
                    placeholder = { Text("e.g. 73.8567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = centerLongitude.isNotBlank() && parsedCenterLongitude == null
                )
            }

            if (hasValidCenter) {
                LaunchedEffect(parsedCenterLatitude, parsedCenterLongitude) {
                    if (selectedLatitude == null || selectedLongitude == null) {
                        selectedLatitude = parsedCenterLatitude
                        selectedLongitude = parsedCenterLongitude
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Approximate area",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                selectedLatitude?.let { centerLatitude = it.toString() }
                                selectedLongitude?.let { centerLongitude = it.toString() }
                                spanDegrees = (spanDegrees * 0.5f).coerceAtLeast(0.0005f)
                            }
                        ) {
                            Text("Zoom +")
                        }
                        TextButton(
                            onClick = {
                                selectedLatitude?.let { centerLatitude = it.toString() }
                                selectedLongitude?.let { centerLongitude = it.toString() }
                                spanDegrees = (spanDegrees * 2f).coerceAtMost(10f)
                            }
                        ) {
                            Text("Zoom −")
                        }
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 260.dp, max = 420.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
                ) {
                    val pinFractionX = if (hasSelectedLocation) {
                        ((selectedLongitude!! - (parsedCenterLongitude!! - spanDegrees / 2.0)) /
                            spanDegrees).toFloat().coerceIn(0f, 1f)
                    } else {
                        0.5f
                    }
                    val pinFractionY = if (hasSelectedLocation) {
                        (1.0 - (selectedLatitude!! - (parsedCenterLatitude!! - spanDegrees / 2.0)) /
                            spanDegrees).toFloat().coerceIn(0f, 1f)
                    } else {
                        0.5f
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(parsedCenterLatitude, parsedCenterLongitude, spanDegrees) {
                                detectTapGestures { tap ->
                                    val fractionX = (tap.x / size.width).coerceIn(0f, 1f)
                                    val fractionY = (tap.y / size.height).coerceIn(0f, 1f)
                                    selectedLongitude = (
                                        parsedCenterLongitude!! - spanDegrees / 2.0 +
                                            fractionX * spanDegrees
                                        ).coerceIn(-180.0, 180.0)
                                    selectedLatitude = (
                                        parsedCenterLatitude!! + spanDegrees / 2.0 -
                                            fractionY * spanDegrees
                                        ).coerceIn(-90.0, 90.0)
                                }
                            }
                    ) {
                        val gridLines = 8
                        for (index in 0..gridLines) {
                            val x = size.width * index / gridLines
                            val y = size.height * index / gridLines
                            drawLine(
                                color = Color(0xFFD8E1EC),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color(0xFFD8E1EC),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            color = ResQSyncPrimaryBlue.copy(alpha = 0.65f),
                            radius = 7.dp.toPx(),
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        if (hasSelectedLocation) {
                            val pin = Offset(size.width * pinFractionX, size.height * pinFractionY)
                            drawCircle(
                                color = ResQSyncEmergencyRed.copy(alpha = 0.2f),
                                radius = 18.dp.toPx(),
                                center = pin
                            )
                            drawCircle(
                                color = ResQSyncEmergencyRed,
                                radius = 8.dp.toPx(),
                                center = pin
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = pin
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Tap to place pin",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (hasSelectedLocation) {
                    Text(
                        text = "Selected: ${selectedLatitude!!.formatCoordinate()}, ${selectedLongitude!!.formatCoordinate()}",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ResQSyncPrimaryBlue,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Enter valid center coordinates to enable the picker. No location will be guessed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (centerLatitude.isNotBlank() || centerLongitude.isNotBlank()) {
                        ResQSyncEmergencyRed
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Button(
                onClick = {
                    onLocationSelected(
                        EmergencyLocation(
                            latitude = selectedLatitude,
                            longitude = selectedLongitude,
                            source = LocationSource.MANUAL_MAP,
                            landmark = "Offline coordinate selection",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                },
                enabled = hasValidCenter && hasSelectedLocation,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ResQSyncPrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Use selected coordinates",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun String.filterCoordinateInput(): String =
    filterIndexed { index, character ->
        character.isDigit() ||
            (character == '-' && index == 0) ||
            (character == '.' && !take(index).contains('.'))
    }

private fun Double.formatCoordinate(): String =
    (round(this * 1_000_000.0).toLong() / 1_000_000.0).toString()
