package com.resqsync.app.ui.screens

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resqsync.app.audio.AudioRecorderHelper
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.LocationSource
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
            .padding(18.dp)
    ) {

        Text(
            text = "Emergency Reports Log",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ResQSyncTextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Stored local and mesh-relayed emergency reports with real-time delivery status.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Total Reports: ${reports.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = ResQSyncTextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (reports.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "No Emergency Reports Stored",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ResQSyncTextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your submitted or mesh-received emergency reports will appear here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                items(
                    items = reports,
                    key = { it.reportId }
                ) { report ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = report.incidentType.name.replace('_', ' '),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ResQSyncTextDark
                                )

                                Surface(
                                    color = when (report.location.source) {
                                        LocationSource.GPS -> ResQSyncSuccessGreen.copy(alpha = 0.15f)
                                        LocationSource.CACHED_GPS -> ResQSyncPrimaryBlue.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Source: ${report.location.source.name}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (report.location.source) {
                                            LocationSource.GPS -> ResQSyncSuccessGreen
                                            LocationSource.CACHED_GPS -> ResQSyncPrimaryBlue
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = report.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Voice SOS Audio Memo Player Badge
                            if (!report.voiceBase64.isNullOrBlank()) {
                                Surface(
                                    color = ResQSyncPrimaryBlue.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "🎤 Voice SOS Audio Attached",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = ResQSyncPrimaryBlue
                                            )
                                            Text(
                                                text = "10s recorded voice memo",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (playingReportId == report.reportId) {
                                                    audioPlayer.stopPlayback()
                                                    playingReportId = null
                                                } else {
                                                    try {
                                                        val bytes = Base64.decode(report.voiceBase64, Base64.DEFAULT)
                                                        playingReportId = report.reportId
                                                        audioPlayer.startPlayback(bytes) {
                                                            if (playingReportId == report.reportId) {
                                                                playingReportId = null
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("ResQSync", "Failed to play voice memo", e)
                                                        playingReportId = null
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ResQSyncPrimaryBlue)
                                        ) {
                                            Text(
                                                text = if (playingReportId == report.reportId) "⏹ Stop" else "▶ Play Memo",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Urgency: ${report.urgency.name}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = ResQSyncEmergencyRed
                                )

                                Text(
                                    text = "Victims: ${report.peopleAffected ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Delivery Status Banner
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Delivery & Sync Status:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ResQSyncTextDark
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "💾 Saved Locally",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ResQSyncSuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "📡 Mesh Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ResQSyncPrimaryBlue,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "☁️ Synced to Server",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ResQSyncPrimaryBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Detailed Location Display
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    val lat = report.location.latitude ?: report.latitude
                                    val long = report.location.longitude ?: report.longitude

                                    if (lat != null && long != null) {
                                        Text(
                                            text = "📍 Coordinates: $lat, $long",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ResQSyncTextDark
                                        )
                                    } else {
                                        Text(
                                            text = "📍 Coordinates: Not available (Text location mode)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    report.location.accuracyMeters?.let { acc ->
                                        Text(
                                            text = "Accuracy: ±${acc.toInt()} meters",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ResQSyncSuccessGreen
                                        )
                                    }

                                    if (!report.location.address.isNullOrBlank()) {
                                        Text(
                                            text = "Address: ${report.location.address}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ResQSyncTextDark
                                        )
                                    }

                                    if (!report.location.landmark.isNullOrBlank()) {
                                        Text(
                                            text = "Landmark: ${report.location.landmark}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ResQSyncPrimaryBlue
                                        )
                                    }

                                    if (!report.location.sourceDeviceId.isNullOrBlank()) {
                                        Text(
                                            text = "Nearby Reference Peer: ${report.location.sourceDeviceId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Report ID: ${report.reportId.take(12)}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = formatReportDate(report.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatReportDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
