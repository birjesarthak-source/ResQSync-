package com.resqsync.app.ui.screens

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resqsync.app.audio.AudioRecorderHelper
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.LocationSource
import com.resqsync.app.models.UrgencyLevel
import com.resqsync.app.models.VerificationStatus
import com.resqsync.app.ui.components.PhotoThumbnail
import com.resqsync.app.ui.theme.ResQSyncEmergencyRed
import com.resqsync.app.ui.theme.ResQSyncPrimaryBlue
import com.resqsync.app.ui.theme.ResQSyncSuccessGreen
import com.resqsync.app.ui.theme.ResQSyncTextDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    reports: List<EmergencyReport>
) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    var playingReportId by remember { mutableStateOf<String?>(null) }
    val audioPlayer = remember(context) { AudioRecorderHelper(context) }

    DisposableEffect(audioPlayer) {
        onDispose {
            audioPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Emergency reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ResQSyncTextDark
            )
            Text(
                text = "Review saved reports and their location details.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                color = ResQSyncPrimaryBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "${reports.size} ${if (reports.size == 1) "report" else "reports"}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = ResQSyncPrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (reports.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No reports yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ResQSyncTextDark
                    )
                    Text(
                        text = "Submitted or nearby-received emergency reports will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = reports,
                    key = { it.reportId }
                ) { report ->
                    ReportCard(
                        report = report,
                        isInspection = isInspection,
                        isPlaying = playingReportId == report.reportId,
                        audioPlayer = audioPlayer,
                        onPlaybackStarted = { playingReportId = report.reportId },
                        onPlaybackStopped = { playingReportId = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: EmergencyReport,
    isInspection: Boolean,
    isPlaying: Boolean,
    audioPlayer: AudioRecorderHelper,
    onPlaybackStarted: () -> Unit,
    onPlaybackStopped: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = report.incidentType.name.replace('_', ' ').lowercase()
                    .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ResQSyncTextDark
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportTag(
                    text = report.urgency.name.lowercase()
                        .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                    color = urgencyColor(report.urgency)
                )
                ReportTag(
                    text = verificationLabel(report.verificationStatus),
                    color = verificationColor(report.verificationStatus)
                )
            }

            if (report.description.isNotBlank()) {
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (report.localPhotoPaths.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Photo evidence · ${report.localPhotoPaths.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        report.localPhotoPaths.forEach { path ->
                            PhotoThumbnail(
                                path = path,
                                modifier = Modifier.size(76.dp)
                            )
                        }
                    }
                    Text(
                        text = "Stored on this device",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!report.voiceBase64.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ResQSyncPrimaryBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Voice memo attached",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = ResQSyncPrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isInspection) {
                            TextButton(
                                onClick = {
                                    if (isPlaying) {
                                        audioPlayer.stopPlayback()
                                        onPlaybackStopped()
                                    } else {
                                        try {
                                            val bytes = Base64.decode(report.voiceBase64, Base64.DEFAULT)
                                            onPlaybackStarted()
                                            audioPlayer.startPlayback(bytes) {
                                                onPlaybackStopped()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ResQSync", "Failed to play voice memo", e)
                                            onPlaybackStopped()
                                        }
                                    }
                                }
                            ) {
                                Text(if (isPlaying) "Stop" else "Play")
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetadata(
                    label = "People affected",
                    value = report.peopleAffected?.toString() ?: "Unknown",
                    modifier = Modifier.weight(1f)
                )
                ReportMetadata(
                    label = "Reported",
                    value = formatReportDate(report.createdAt),
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Location · ${locationSourceLabel(report.location.source)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ResQSyncPrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    val latitude = report.location.latitude ?: report.latitude
                    val longitude = report.location.longitude ?: report.longitude
                    if (latitude != null && longitude != null) {
                        Text(
                            text = "$latitude, $longitude",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else if (report.location.source == LocationSource.UNAVAILABLE) {
                        Text(
                            text = "Coordinates unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    report.location.address?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    report.location.landmark?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "Landmark: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    report.location.sourceDeviceId?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "Nearby reference: $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    report.location.accuracyMeters?.let {
                        Text(
                            text = "Accuracy ±${it.toInt()} m",
                            style = MaterialTheme.typography.labelSmall,
                            color = ResQSyncSuccessGreen
                        )
                    }
                }
            }

            Text(
                text = "ID  ${report.reportId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReportTag(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun ReportMetadata(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = ResQSyncTextDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun urgencyColor(urgency: UrgencyLevel) = when (urgency) {
    UrgencyLevel.LOW -> ResQSyncSuccessGreen
    UrgencyLevel.MEDIUM -> androidx.compose.ui.graphics.Color(0xFFD97706)
    UrgencyLevel.HIGH -> androidx.compose.ui.graphics.Color(0xFFEA580C)
    UrgencyLevel.CRITICAL -> ResQSyncEmergencyRed
    UrgencyLevel.UNKNOWN -> androidx.compose.ui.graphics.Color(0xFF64748B)
}

private fun verificationColor(status: VerificationStatus) = when (status) {
    VerificationStatus.UNVERIFIED -> androidx.compose.ui.graphics.Color(0xFFD97706)
    VerificationStatus.CORROBORATED -> ResQSyncSuccessGreen
    VerificationStatus.HUMAN_REVIEWED -> ResQSyncPrimaryBlue
    VerificationStatus.DISPUTED -> ResQSyncEmergencyRed
}

private fun verificationLabel(status: VerificationStatus) = when (status) {
    VerificationStatus.UNVERIFIED -> "Unverified"
    VerificationStatus.CORROBORATED -> "Corroborated"
    VerificationStatus.HUMAN_REVIEWED -> "Reviewed"
    VerificationStatus.DISPUTED -> "Disputed"
}

private fun locationSourceLabel(source: LocationSource) = when (source) {
    LocationSource.GPS -> "GPS"
    LocationSource.CACHED_GPS -> "Cached GPS"
    LocationSource.MANUAL_MAP -> "Map"
    LocationSource.MANUAL_ADDRESS -> "Address"
    LocationSource.NEARBY_DEVICE -> "Nearby device"
    LocationSource.UNAVAILABLE -> "Unavailable"
}

private fun formatReportDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
