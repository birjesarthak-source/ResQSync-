package com.resqsync.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resqsync.app.ui.theme.ResQSyncEmergencyRed
import com.resqsync.app.ui.theme.ResQSyncPrimaryBlue
import com.resqsync.app.ui.theme.ResQSyncTextDark

enum class ThemeMode {
    LIGHT,
    DARK_HIGH_CONTRAST,
    SYSTEM
}

@Composable
fun SettingsScreen(
    deviceId: String,
    onManualSyncRequested: () -> Unit,
    onClearDataRequested: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf(ThemeMode.LIGHT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Preferences",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ResQSyncTextDark
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Device Identification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncPrimaryBlue
                )

                Text(
                    text = "Unique Device ID: $deviceId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Display Theme & Battery Saver",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                Text(
                    text = "High-contrast dark theme reduces OLED screen battery drain during power outages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedTheme == mode,
                            onClick = { selectedTheme = mode },
                            label = { Text(mode.name.replace('_', ' ')) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cloud Synchronization & Maintenance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                Button(
                    onClick = onManualSyncRequested,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ResQSyncPrimaryBlue)
                ) {
                    Text("Trigger Cloud Sync", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onClearDataRequested,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ResQSyncEmergencyRed)
                ) {
                    Text("Clear Local Reports & Mesh Cache")
                }
            }
        }
    }
}
