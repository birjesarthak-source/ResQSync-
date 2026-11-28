package com.resqsync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.RescueUpdate
import com.resqsync.app.ui.theme.ResQSyncEmergencyRed
import com.resqsync.app.ui.theme.ResQSyncPrimaryBlue
import com.resqsync.app.ui.theme.ResQSyncSuccessGreen
import com.resqsync.app.ui.theme.ResQSyncTextDark
import com.resqsync.app.ui.theme.ResQSyncTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    statusMessage: String,
    discoveredDeviceNames: List<String>,
    connectedCount: Int = 0,
    totalReportsCount: Int = 0,
    recentReports: List<EmergencyReport> = emptyList(),
    rescueUpdates: List<RescueUpdate> = emptyList(),
    onStartNearby: () -> Unit,
    onStopNearby: () -> Unit,
    onReportEmergency: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ResQSync",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )
                Text(
                    text = "Emergency communication",
                    style = MaterialTheme.typography.bodySmall,
                    color = ResQSyncTextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = if (connectedCount > 0)
                    ResQSyncSuccessGreen.copy(alpha = 0.12f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (connectedCount > 0) "$connectedCount connected" else "Offline mesh",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (connectedCount > 0) ResQSyncSuccessGreen else ResQSyncTextMuted
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Emergency assistance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                Button(
                    onClick = onReportEmergency,
                    modifier = Modifier
                        .size(168.dp),
                    shape = RoundedCornerShape(84.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ResQSyncEmergencyRed)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SOS",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Send emergency",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "Works without internet",
                    style = MaterialTheme.typography.bodySmall,
                    color = ResQSyncTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusRow(
                    icon = { Icon(Icons.Default.Devices, contentDescription = null, tint = ResQSyncPrimaryBlue) },
                    title = "Nearby devices",
                    value = if (discoveredDeviceNames.isEmpty())
                        "No devices found"
                    else
                        "${discoveredDeviceNames.size} found • $connectedCount connected"
                )
                StatusRow(
                    icon = { Icon(Icons.Default.CloudDone, contentDescription = null, tint = ResQSyncSuccessGreen) },
                    title = "Cloud synchronization",
                    value = if (statusMessage.isBlank()) "Available when internet is restored" else statusMessage
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onStartNearby,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start network")
            }
            androidx.compose.material3.OutlinedButton(
                onClick = onStopNearby,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Stop")
            }
        }

        SectionCard(
            title = "Recent reports",
            icon = Icons.Default.LocationOn
        ) {
            if (recentReports.isEmpty()) {
                Text(
                    text = "No emergency reports submitted yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ResQSyncTextMuted
                )
            } else {
                recentReports.take(3).forEach { report ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = report.incidentType.name.replace('_', ' '),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ResQSyncTextDark
                        )
                        Text(
                            text = "${dateFormat.format(Date(report.createdAt))} • ${report.urgency.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ResQSyncTextMuted
                        )
                    }
                }
                Text(
                    text = "$totalReportsCount total reports stored",
                    style = MaterialTheme.typography.labelMedium,
                    color = ResQSyncPrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        SectionCard(
            title = "Rescue announcements",
            icon = Icons.Default.Campaign
        ) {
            if (rescueUpdates.isEmpty()) {
                Text(
                    text = "No rescue announcements available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ResQSyncTextMuted
                )
            } else {
                rescueUpdates.take(2).forEach { update ->
                    Text(
                        text = update.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ResQSyncTextDark,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun StatusRow(
    icon: @Composable () -> Unit,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ResQSyncTextDark
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = ResQSyncTextMuted
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = ResQSyncPrimaryBlue)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )
            }
            content()
        }
    }
}
