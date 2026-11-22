package com.resqsync.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.resqsync.app.location.GPSLocationManager
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.IncidentType
import com.resqsync.app.models.LocationSource
import com.resqsync.app.models.NearbyPeer
import com.resqsync.app.models.UrgencyLevel
import com.resqsync.app.ui.components.OfflineMapPicker
import com.resqsync.app.ui.theme.ResQSyncCyan
import com.resqsync.app.ui.theme.ResQSyncSuccessGreen
import kotlinx.coroutines.launch

enum class LocationMethodTab {
    AUTOMATIC_GPS,
    OFFLINE_MAP,
    MANUAL_ADDRESS,
    NEARBY_ASSISTANCE
}

@Composable
fun ReportScreen(
    onSubmit: (
        IncidentType,
        String,
        Int?,
        UrgencyLevel,
        EmergencyLocation
    ) -> Unit,
    onCancel: () -> Unit,
    nearbyPeers: List<NearbyPeer> = emptyList(),
    onRequestNearbyLocation: ((endpointId: String) -> Unit)? = null,
    receivedNearbyLocation: EmergencyLocation? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gpsManager = remember { GPSLocationManager(context) }

    var selectedIncident by remember { mutableStateOf(IncidentType.FLOOD) }
    var description by remember { mutableStateOf("") }
    var peopleAffected by remember { mutableStateOf("") }
    var selectedUrgency by remember { mutableStateOf(UrgencyLevel.HIGH) }

    // Active location state
    var activeLocation by remember { mutableStateOf(EmergencyLocation()) }
    var selectedMethodTab by remember { mutableStateOf(LocationMethodTab.AUTOMATIC_GPS) }
    var showMapPickerModal by remember { mutableStateOf(false) }

    // Manual Address form states
    var cityText by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf("") }
    var streetText by remember { mutableStateOf("") }
    var landmarkText by remember { mutableStateOf("") }

    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationStatusText by remember {
        mutableStateOf("Acquire satellite GPS fix or choose offline fallback below.")
    }

    // React to nearby location received over mesh
    LaunchedEffect(receivedNearbyLocation) {
        if (receivedNearbyLocation != null) {
            activeLocation = receivedNearbyLocation
            locationStatusText = "Nearby device reference received (${receivedNearbyLocation.sourceDeviceId?.take(8)})"
        }
    }

    val incidentTypes = IncidentType.entries.toList()
    val urgencyLevels = listOf(
        UrgencyLevel.LOW,
        UrgencyLevel.MEDIUM,
        UrgencyLevel.HIGH,
        UrgencyLevel.CRITICAL
    )

    if (showMapPickerModal) {
        Dialog(onDismissRequest = { showMapPickerModal = false }) {
            OfflineMapPicker(
                initialLat = activeLocation.latitude,
                initialLong = activeLocation.longitude,
                onLocationSelected = { loc ->
                    activeLocation = loc
                    locationStatusText = "Map coordinates confirmed: ${loc.latitude}, ${loc.longitude}"
                    showMapPickerModal = false
                },
                onDismiss = { showMapPickerModal = false }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        Text(
            text = "Report Emergency (SOS)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Provide details and specify your location using one of four offline acquisition methods.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Incident selection
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Incident Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                incidentTypes.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { incident ->
                            FilterChip(
                                selected = selectedIncident == incident,
                                onClick = { selectedIncident = incident },
                                label = { Text(incident.name.replace('_', ' ')) }
                            )
                        }
                    }
                }
            }
        }

        // Description
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Emergency Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Describe what happened, trapped victims, and urgent assistance needed...")
                    },
                    minLines = 4,
                    maxLines = 6
                )
            }
        }

        // People affected
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "People Affected & Urgency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = peopleAffected,
                    onValueChange = {
                        if (it.all { character -> character.isDigit() }) {
                            peopleAffected = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Approximate count of victims/affected") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                urgencyLevels.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { urgency ->
                            FilterChip(
                                selected = selectedUrgency == urgency,
                                onClick = { selectedUrgency = urgency },
                                label = { Text(urgency.name) }
                            )
                        }
                    }
                }
            }
        }

        // Comprehensive Offline Location System Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Offline Location Sharing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Current Active Location Status Badge
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Source: ${activeLocation.source.name}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ResQSyncCyan
                            )
                            if (activeLocation.accuracyMeters != null) {
                                Text(
                                    text = "Accuracy: ±${activeLocation.accuracyMeters?.toInt()}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ResQSyncSuccessGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = locationStatusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (activeLocation.latitude != null && activeLocation.longitude != null) {
                            Text(
                                text = "Coordinates: ${activeLocation.latitude}, ${activeLocation.longitude}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Location Acquisition Method Selector Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedMethodTab.ordinal,
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedMethodTab == LocationMethodTab.AUTOMATIC_GPS,
                        onClick = { selectedMethodTab = LocationMethodTab.AUTOMATIC_GPS },
                        text = { Text("🛰️ GPS") }
                    )
                    Tab(
                        selected = selectedMethodTab == LocationMethodTab.OFFLINE_MAP,
                        onClick = { selectedMethodTab = LocationMethodTab.OFFLINE_MAP },
                        text = { Text("🗺️ Map") }
                    )
                    Tab(
                        selected = selectedMethodTab == LocationMethodTab.MANUAL_ADDRESS,
                        onClick = { selectedMethodTab = LocationMethodTab.MANUAL_ADDRESS },
                        text = { Text("📝 Address") }
                    )
                    Tab(
                        selected = selectedMethodTab == LocationMethodTab.NEARBY_ASSISTANCE,
                        onClick = { selectedMethodTab = LocationMethodTab.NEARBY_ASSISTANCE },
                        text = { Text("📡 Nearby") }
                    )
                }

                // TAB CONTENT
                when (selectedMethodTab) {
                    LocationMethodTab.AUTOMATIC_GPS -> {
                        Button(
                            onClick = {
                                isFetchingLocation = true
                                locationStatusText = "Acquiring GPS fix from satellites..."
                                coroutineScope.launch {
                                    val loc = gpsManager.getStructuredOfflineLocation()
                                    isFetchingLocation = false
                                    activeLocation = loc
                                    locationStatusText = when (loc.source) {
                                        LocationSource.GPS -> "Fresh GPS Fix Acquired (${loc.latitude}, ${loc.longitude})"
                                        LocationSource.CACHED_GPS -> "Using Cached GPS Position (${loc.latitude}, ${loc.longitude})"
                                        else -> "GPS Fix unavailable indoors. Use Offline Map or Address below."
                                    }
                                }
                            },
                            enabled = !isFetchingLocation,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isFetchingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fixing Satellites...")
                            } else {
                                Text("Acquire GPS Fix Offline")
                            }
                        }
                    }

                    LocationMethodTab.OFFLINE_MAP -> {
                        Button(
                            onClick = { showMapPickerModal = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ResQSyncCyan)
                        ) {
                            Text("Open Offline Interactive Canvas Map", color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold)
                        }
                    }

                    LocationMethodTab.MANUAL_ADDRESS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cityText,
                                    onValueChange = { cityText = it },
                                    label = { Text("City / Village") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = areaText,
                                    onValueChange = { areaText = it },
                                    label = { Text("Area / Locality") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = streetText,
                                    onValueChange = { streetText = it },
                                    label = { Text("Street / Road") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = landmarkText,
                                    onValueChange = { landmarkText = it },
                                    label = { Text("Landmark / Shelter") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Button(
                                onClick = {
                                    val formattedAddr = listOf(streetText, areaText, cityText).filter { it.isNotBlank() }.joinToString(", ")
                                    activeLocation = EmergencyLocation(
                                        source = LocationSource.MANUAL_ADDRESS,
                                        address = formattedAddr.ifBlank { "Unspecified Area" },
                                        landmark = landmarkText.ifBlank { null },
                                        timestamp = System.currentTimeMillis()
                                    )
                                    locationStatusText = "Manual address set: $formattedAddr ($landmarkText)"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Attach Manual Address & Landmark")
                            }
                        }
                    }

                    LocationMethodTab.NEARBY_ASSISTANCE -> {
                        if (nearbyPeers.isEmpty()) {
                            Text(
                                text = "No connected nearby devices. Start Nearby on another phone to request GPS assistance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Select connected peer to request location reference:", style = MaterialTheme.typography.labelSmall)
                                nearbyPeers.filter { it.isConnected }.forEach { peer ->
                                    OutlinedButton(
                                        onClick = {
                                            onRequestNearbyLocation?.invoke(peer.endpointId)
                                            locationStatusText = "Location request sent to ${peer.deviceName}. Waiting for approval..."
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Request Location from ${peer.deviceName}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                onSubmit(
                    selectedIncident,
                    description.trim(),
                    peopleAffected.toIntOrNull(),
                    selectedUrgency,
                    activeLocation
                )
            },
            enabled = description.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Submit Emergency Report (SOS)")
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
