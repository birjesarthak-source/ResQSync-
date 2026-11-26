package com.resqsync.app.ui.screens

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.resqsync.app.audio.AudioRecorderHelper
import com.resqsync.app.location.GPSLocationManager
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.IncidentType
import com.resqsync.app.models.LocationSource
import com.resqsync.app.models.NearbyPeer
import com.resqsync.app.models.UrgencyLevel
import com.resqsync.app.ui.components.OfflineMapPicker
import com.resqsync.app.ui.theme.ResQSyncEmergencyRed
import com.resqsync.app.ui.theme.ResQSyncPrimaryBlue
import com.resqsync.app.ui.theme.ResQSyncSuccessGreen
import com.resqsync.app.ui.theme.ResQSyncTextDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

enum class LocationMethodTab {
    AUTOMATIC_GPS,
    OFFLINE_MAP,
    MANUAL_ADDRESS,
    NEARBY_ASSISTANCE
}

private data class IncidentTypeInfo(
    val type: IncidentType,
    val title: String,
    val icon: String,
    val activeColor: Color,
    val activeBg: Color
)

private val INCIDENT_TYPES_INFO = listOf(
    IncidentTypeInfo(IncidentType.FLOOD, "Flood", "🌊", Color(0xFF2563EB), Color(0xFFEFF6FF)),
    IncidentTypeInfo(IncidentType.FIRE, "Fire", "🔥", Color(0xFFDC2626), Color(0xFFFEF2F2)),
    IncidentTypeInfo(IncidentType.EARTHQUAKE, "Earthquake", "🌋", Color(0xFFEA580C), Color(0xFFFFF7ED)),
    IncidentTypeInfo(IncidentType.LANDSLIDE, "Landslide", "⛰️", Color(0xFFD97706), Color(0xFFFEF3C7)),
    IncidentTypeInfo(IncidentType.BUILDING_COLLAPSE, "Building Collapse", "🏗️", Color(0xFF9333EA), Color(0xFFFAF5FF)),
    IncidentTypeInfo(IncidentType.MEDICAL_EMERGENCY, "Medical Emergency", "🩺", Color(0xFFE11D48), Color(0xFFFFF1F2)),
    IncidentTypeInfo(IncidentType.OTHER, "Other SOS", "⚠️", Color(0xFF475569), Color(0xFFF8FAFC))
)

@Composable
fun ReportScreen(
    onSubmit: (
        IncidentType,
        String,
        Int?,
        UrgencyLevel,
        EmergencyLocation,
        String?
    ) -> Unit,
    onCancel: () -> Unit,
    nearbyPeers: List<NearbyPeer> = emptyList(),
    onRequestNearbyLocation: ((endpointId: String) -> Unit)? = null,
    receivedNearbyLocation: EmergencyLocation? = null
) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val coroutineScope = rememberCoroutineScope()

    val gpsManager = remember(isInspection) {
        if (!isInspection) GPSLocationManager(context) else null
    }

    val audioRecorder = remember(isInspection) {
        if (!isInspection) AudioRecorderHelper(context) else null
    }

    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingSecondsLeft by remember { mutableStateOf(10) }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var recordedAudioFile by remember { mutableStateOf<File?>(null) }
    var cachedVoiceBase64 by remember { mutableStateOf<String?>(null) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micGranted = permissions[android.Manifest.permission.RECORD_AUDIO] == true
        if (micGranted) {
            recordedAudioFile = audioRecorder?.startRecording(10000L)
            isRecordingAudio = (recordedAudioFile != null && recordedAudioFile?.exists() == true)
        }
    }

    // Automatic 10-second countdown timer for audio recording
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingSecondsLeft = 10
            while (isRecordingAudio && recordingSecondsLeft > 0) {
                delay(1000L)
                recordingSecondsLeft -= 1
            }
            if (isRecordingAudio && recordingSecondsLeft == 0) {
                recordedAudioFile = audioRecorder?.stopRecording()
                isRecordingAudio = false

                // Allow the OS 300ms to flush the MP4/3GP bytes to disk completely
                delay(300L)
                cachedVoiceBase64 = audioRecorder?.getAudioBase64(recordedAudioFile)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioRecorder?.release()
        }
    }

    var selectedIncident by remember { mutableStateOf(IncidentType.FIRE) }
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

    // Automatically trigger GPS acquisition on launch
    LaunchedEffect(Unit) {
        if (!isInspection && activeLocation.source == LocationSource.UNAVAILABLE) {
            isFetchingLocation = true
            locationStatusText = "Acquiring automatic offline GPS fix..."
            val loc = gpsManager?.getStructuredOfflineLocation() ?: EmergencyLocation()
            isFetchingLocation = false
            activeLocation = loc
            locationStatusText = when (loc.source) {
                LocationSource.GPS -> "Fresh GPS Fix Acquired (${loc.latitude}, ${loc.longitude})"
                LocationSource.CACHED_GPS -> "Using Cached GPS Position (${loc.latitude}, ${loc.longitude})"
                else -> "GPS Fix unavailable indoors. Use Offline Map or Address below."
            }
        }
    }

    LaunchedEffect(receivedNearbyLocation) {
        if (receivedNearbyLocation != null) {
            activeLocation = receivedNearbyLocation
            locationStatusText = "Nearby device reference received (${receivedNearbyLocation.sourceDeviceId?.take(8)})"
        }
    }

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
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header Title
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Report Emergency (SOS)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = ResQSyncTextDark
            )
            Text(
                text = "Provide details & location for immediate offline mesh dispatch.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )
        }

        // Section 1: Incident Type Category Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Incident Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                // 2-column Grid for Incident Categories
                INCIDENT_TYPES_INFO.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { info ->
                            val isSelected = selectedIncident == info.type
                            val borderColor by animateColorAsState(
                                if (isSelected) info.activeColor else Color(0xFFE2E8F0),
                                label = "border"
                            )
                            val bgColor by animateColorAsState(
                                if (isSelected) info.activeBg else Color.White,
                                label = "bg"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedIncident = info.type }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = info.icon, fontSize = 20.sp)
                                    Text(
                                        text = info.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) info.activeColor else ResQSyncTextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Fill odd row space if necessary
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Section 2: Voice SOS Audio Recording Card with Countdown Timer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isRecordingAudio) Color(0xFFFEF2F2) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎤 Voice SOS Audio Memo (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecordingAudio) ResQSyncEmergencyRed else ResQSyncTextDark
                    )

                    if (isRecordingAudio) {
                        Surface(
                            color = ResQSyncEmergencyRed,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🔴 00:0${recordingSecondsLeft}s",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = if (isRecordingAudio) {
                        "Recording voice message... Will automatically stop in ${recordingSecondsLeft}s or tap Stop below."
                    } else if (recordedAudioFile != null && recordedAudioFile?.exists() == true) {
                        "✅ Voice SOS Audio Memo captured (${recordedAudioFile?.length() ?: 0} bytes). Tap Play or Re-record."
                    } else {
                        "Record up to 10 seconds of voice message to attach to your emergency packet."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )

                if (isRecordingAudio) {
                    LinearProgressIndicator(
                        progress = { (10 - recordingSecondsLeft) / 10f },
                        modifier = Modifier.fillMaxWidth(),
                        color = ResQSyncEmergencyRed,
                        trackColor = Color(0xFFFEE2E2)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val hasMicPermission = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!hasMicPermission) {
                                audioPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.RECORD_AUDIO,
                                        android.Manifest.permission.POST_NOTIFICATIONS
                                    )
                                )
                            } else {
                                if (isRecordingAudio) {
                                    coroutineScope.launch {
                                        recordedAudioFile = audioRecorder?.stopRecording()
                                        isRecordingAudio = false
                                        // Allow the OS 300ms to flush the bytes to disk completely
                                        delay(300L)
                                        cachedVoiceBase64 = audioRecorder?.getAudioBase64(recordedAudioFile)
                                    }
                                } else {
                                    recordedAudioFile = audioRecorder?.startRecording(10000L)
                                    isRecordingAudio = (recordedAudioFile != null && recordedAudioFile?.exists() == true)
                                    cachedVoiceBase64 = null
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecordingAudio) ResQSyncEmergencyRed else ResQSyncPrimaryBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRecordingAudio) "⏹ Stop Recording" else "🎙️ Record 10s Memo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (recordedAudioFile != null && recordedAudioFile?.exists() == true && !isRecordingAudio) {
                        Button(
                            onClick = {
                                if (isPlayingAudio) {
                                    audioRecorder?.stopPlayback()
                                    isPlayingAudio = false
                                } else {
                                    isPlayingAudio = true
                                    audioRecorder?.startPlayback(recordedAudioFile) {
                                        isPlayingAudio = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ResQSyncSuccessGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isPlayingAudio) "⏹ Stop" else "▶️ Play Voice SOS",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                audioRecorder?.stopPlayback()
                                recordedAudioFile?.delete()
                                recordedAudioFile = null
                                cachedVoiceBase64 = null
                                isPlayingAudio = false
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🗑 Clear", color = ResQSyncEmergencyRed)
                        }
                    }
                }
            }
        }

        // Section 3: Emergency Description Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Emergency Description (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Optional description of victims or assistance needed...",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = ResQSyncPrimaryBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }

        // Section 4: Urgency Level & People Affected
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Urgency Level & People Affected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                // Urgency Selector Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val urgencies = listOf(
                        UrgencyLevel.LOW to Color(0xFF16A34A),
                        UrgencyLevel.MEDIUM to Color(0xFFD97706),
                        UrgencyLevel.HIGH to Color(0xFFEA580C),
                        UrgencyLevel.CRITICAL to Color(0xFFDC2626)
                    )

                    urgencies.forEach { (urgency, color) ->
                        val isSelected = selectedUrgency == urgency
                        val chipBg = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF8FAFC)
                        val chipBorder = if (isSelected) color else Color(0xFFCBD5E1)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(chipBg)
                                .border(width = if (isSelected) 2.dp else 1.dp, color = chipBorder, shape = RoundedCornerShape(10.dp))
                                .clickable { selectedUrgency = urgency }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = urgency.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) color else Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // People Affected Input
                OutlinedTextField(
                    value = peopleAffected,
                    onValueChange = {
                        if (it.all { character -> character.isDigit() }) {
                            peopleAffected = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Victims / People Affected (Optional)", fontSize = 12.sp) },
                    placeholder = { Text("e.g. 3", color = Color(0xFF94A3B8)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = ResQSyncPrimaryBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )
            }
        }

        // Section 5: Offline Location Sharing
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Offline Location Sharing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Source: ${activeLocation.source.name}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ResQSyncPrimaryBlue
                            )
                            if (activeLocation.accuracyMeters != null) {
                                Surface(
                                    color = ResQSyncSuccessGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Accuracy: ±${activeLocation.accuracyMeters?.toInt()}m",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ResQSyncSuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = locationStatusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )

                        if (activeLocation.latitude != null && activeLocation.longitude != null) {
                            Text(
                                text = "📍 ${activeLocation.latitude}, ${activeLocation.longitude}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ResQSyncTextDark
                            )
                        }
                    }
                }

                // Location Mode Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedMethodTab.ordinal,
                    edgePadding = 0.dp,
                    containerColor = Color(0xFFF8FAFC)
                ) {
                    Tab(
                        selected = selectedMethodTab == LocationMethodTab.AUTOMATIC_GPS,
                        onClick = { selectedMethodTab = LocationMethodTab.AUTOMATIC_GPS },
                        text = { Text("🛰️ GPS", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedMethodTab == LocationMethodTab.OFFLINE_MAP,
                        onClick = { selectedMethodTab = LocationMethodTab.OFFLINE_MAP },
                        text = { Text("🗺️ Map", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedMethodTab == LocationMethodTab.MANUAL_ADDRESS,
                        onClick = { selectedMethodTab = LocationMethodTab.MANUAL_ADDRESS },
                        text = { Text("📝 Address", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedMethodTab == LocationMethodTab.NEARBY_ASSISTANCE,
                        onClick = { selectedMethodTab = LocationMethodTab.NEARBY_ASSISTANCE },
                        text = { Text("📡 Nearby", fontSize = 12.sp) }
                    )
                }

                when (selectedMethodTab) {
                    LocationMethodTab.AUTOMATIC_GPS -> {
                        Button(
                            onClick = {
                                isFetchingLocation = true
                                locationStatusText = "Acquiring GPS fix from satellites..."
                                coroutineScope.launch {
                                    val loc = gpsManager?.getStructuredOfflineLocation() ?: EmergencyLocation()
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
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ResQSyncPrimaryBlue)
                        ) {
                            if (isFetchingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fixing Satellites...", color = Color.White)
                            } else {
                                Text("Refresh GPS Position", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LocationMethodTab.OFFLINE_MAP -> {
                        Button(
                            onClick = { showMapPickerModal = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ResQSyncPrimaryBlue)
                        ) {
                            Text("Open Offline Interactive Canvas Map", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    LocationMethodTab.MANUAL_ADDRESS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cityText,
                                    onValueChange = { cityText = it },
                                    label = { Text("City / Village", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = areaText,
                                    onValueChange = { areaText = it },
                                    label = { Text("Area / Locality", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = streetText,
                                    onValueChange = { streetText = it },
                                    label = { Text("Street / Road", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = landmarkText,
                                    onValueChange = { landmarkText = it },
                                    label = { Text("Landmark / Shelter", fontSize = 11.sp) },
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
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ResQSyncPrimaryBlue)
                            ) {
                                Text("Attach Address & Landmark", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LocationMethodTab.NEARBY_ASSISTANCE -> {
                        if (nearbyPeers.isEmpty()) {
                            Text(
                                text = "No connected nearby devices. Start Nearby on another phone to request GPS assistance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
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

        // Section 6: Prominent SOS Submit & Cancel Buttons
        Button(
            onClick = {
                val finalDesc = if (description.isBlank()) {
                    "${selectedIncident.name.replace('_', ' ')} emergency alert dispatched from field device."
                } else {
                    description.trim()
                }
                val voiceBase64Payload = cachedVoiceBase64 ?: audioRecorder?.getAudioBase64(recordedAudioFile)
                onSubmit(
                    selectedIncident,
                    finalDesc,
                    peopleAffected.toIntOrNull(),
                    selectedUrgency,
                    activeLocation,
                    voiceBase64Payload
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResQSyncEmergencyRed),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("🆘 SUBMIT EMERGENCY SOS ALERT NOW", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancel & Return", color = Color(0xFF64748B))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ReportScreen(
        onSubmit = { _, _, _, _, _, _ -> },
        onCancel = {}
    )
}
