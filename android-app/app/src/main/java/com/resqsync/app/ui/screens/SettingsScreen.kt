package com.resqsync.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ResQSyncTextDark
        )
        Text(
            text = "Manage this device and your app preferences.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingsSection(
            title = "This device",
            description = "Device ID"
        ) {
            Text(
                text = deviceId,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = ResQSyncTextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        SettingsSection(
            title = "Appearance",
            description = "High-contrast dark theme can help conserve battery on OLED screens."
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    val label = when (mode) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK_HIGH_CONTRAST -> "Dark"
                        ThemeMode.SYSTEM -> "System"
                    }
                    FilterChip(
                        selected = selectedTheme == mode,
                        onClick = { selectedTheme = mode },
                        label = {
                            Text(
                                text = label,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        SettingsSection(
            title = "Data & sync",
            description = "Send pending reports or remove expired records from this device."
        ) {
            Button(
                onClick = onManualSyncRequested,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResQSyncPrimaryBlue)
            ) {
                Text("Sync pending reports", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onClearDataRequested,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ResQSyncEmergencyRed)
            ) {
                Text("Remove expired reports", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ResQSyncTextDark
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}
