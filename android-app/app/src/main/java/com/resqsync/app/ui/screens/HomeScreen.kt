package com.resqsync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resqsync.app.ui.theme.ResQSyncCyan
import com.resqsync.app.ui.theme.ResQSyncEmergencyRed
import com.resqsync.app.ui.theme.ResQSyncSuccessGreen

@Composable
fun HomeScreen(
    statusMessage: String,
    discoveredDeviceNames: List<String>,
    connectedCount: Int = 0,
    totalReportsCount: Int = 0,
    onStartNearby: () -> Unit,
    onStopNearby: () -> Unit,
    onReportEmergency: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ResQSync",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = " AI",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = ResQSyncEmergencyRed
                    )
                }

                Text(
                    text = "Decentralized Disaster Mesh Network",
                    style = MaterialTheme.typography.bodySmall,
                    color = ResQSyncCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Mesh Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (connectedCount > 0) ResQSyncSuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (connectedCount > 0) ResQSyncSuccessGreen else Color.Gray)
                    )
                    Text(
                        text = if (connectedCount > 0) "$connectedCount Connected" else "Standby",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (connectedCount > 0) ResQSyncSuccessGreen else Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // SOS Big Emergency Button Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = ResQSyncEmergencyRed
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "EMERGENCY SOS REPORT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )

                Text(
                    "Tap to send an offline emergency report. Nearby devices will relay your distress signal to rescue teams.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFEBEE),
                    fontSize = 13.sp
                )

                Button(
                    onClick = onReportEmergency,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ResQSyncEmergencyRed
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "broadcast emergency report (sos)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Network Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Mesh Network Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartNearby,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ResQSyncCyan)
                    ) {
                        Text("Start Nearby", color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onStopNearby,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop Network")
                    }
                }
            }
        }

        // Overview Statistics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${discoveredDeviceNames.size}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ResQSyncCyan
                    )
                    Text(
                        text = "Discovered Devices",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$totalReportsCount",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ResQSyncSuccessGreen
                    )
                    Text(
                        text = "Saved Reports",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mission statement
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Store-Carry-Forward Mesh",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    "ResQSync keeps disaster reports safe on your device and automatically routes them across intermediate smartphones until they reach rescue personnel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
