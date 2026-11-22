package com.resqsync.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.LocationSource
import com.resqsync.app.ui.theme.ResQSyncCyan
import com.resqsync.app.ui.theme.ResQSyncSuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    reports: List<EmergencyReport>
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        Text(
            text = "Emergency Reports Log",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Stored local and mesh-relayed emergency reports with location verification.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Total Reports: ${reports.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
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
                        color = Color.White
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
                        shape = RoundedCornerShape(14.dp)
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
                                    color = Color.White
                                )

                                Surface(
                                    color = when (report.location.source) {
                                        LocationSource.GPS -> ResQSyncSuccessGreen.copy(alpha = 0.2f)
                                        LocationSource.CACHED_GPS -> ResQSyncCyan.copy(alpha = 0.2f)
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
                                            LocationSource.CACHED_GPS -> ResQSyncCyan
                                            else -> Color.LightGray
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

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Urgency: ${report.urgency.name}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = ResQSyncCyan
                                )

                                Text(
                                    text = "Victims: ${report.peopleAffected ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
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
                                            color = Color.White
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
                                            color = Color.White
                                        )
                                    }

                                    if (!report.location.landmark.isNullOrBlank()) {
                                        Text(
                                            text = "Landmark: ${report.location.landmark}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ResQSyncCyan
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

                            Text(
                                text = "Created: ${formatReportDate(report.createdAt)}",
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

private fun formatReportDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
