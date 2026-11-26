package com.resqsync.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resqsync.app.models.IncidentType
import com.resqsync.app.ui.theme.ResQSyncEmergencyRed
import com.resqsync.app.ui.theme.ResQSyncPrimaryBlue
import com.resqsync.app.ui.theme.ResQSyncSuccessGreen
import com.resqsync.app.ui.theme.ResQSyncTextDark

data class QuickCategoryItem(
    val label: String,
    val icon: String,
    val incidentType: IncidentType
)

val QUICK_CATEGORIES = listOf(
    QuickCategoryItem("Medical", "🩺", IncidentType.MEDICAL_EMERGENCY),
    QuickCategoryItem("Fire", "🔥", IncidentType.FIRE),
    QuickCategoryItem("Accident", "🚗", IncidentType.OTHER),
    QuickCategoryItem("Disaster", "🌊", IncidentType.FLOOD),
    QuickCategoryItem("Trapped", "🏗️", IncidentType.BUILDING_COLLAPSE),
    QuickCategoryItem("Other SOS", "⚠️", IncidentType.OTHER)
)

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
    var selectedCategory by remember { mutableStateOf(QUICK_CATEGORIES[0]) }

    // Pulsing Radar Ring Animations
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    // SOS Circular Button Pulsing Rings
    val sosPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sosScale"
    )
    val sosPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sosAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
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
                        fontWeight = FontWeight.ExtraBold,
                        color = ResQSyncTextDark
                    )
                    Text(
                        text = " AI",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = ResQSyncEmergencyRed
                    )
                }

                Text(
                    text = "Decentralized Disaster Mesh Platform",
                    style = MaterialTheme.typography.bodySmall,
                    color = ResQSyncPrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Mesh Status Badge with Animated Radar Ping
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (connectedCount > 0) ResQSyncSuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(12.dp)
                    ) {
                        // Radar Outer Ring
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .graphicsLayer {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                    alpha = pulseAlpha
                                }
                                .clip(CircleShape)
                                .background(if (connectedCount > 0) ResQSyncSuccessGreen else ResQSyncPrimaryBlue)
                        )
                        // Inner Dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (connectedCount > 0) ResQSyncSuccessGreen else Color.Gray)
                        )
                    }

                    Text(
                        text = if (connectedCount > 0) "$connectedCount Connected" else "Mesh Standby",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (connectedCount > 0) ResQSyncSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Category Selector Card
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
                    text = "Select Emergency Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                QUICK_CATEGORIES.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text("${cat.icon} ${cat.label}", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Prominent Circular SOS Emergency Button Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "EMERGENCY SOS DISPATCH",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ResQSyncTextDark,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "Category: ${selectedCategory.icon} ${selectedCategory.label}. Tap the circular SOS button below to open report form & broadcast distress alert.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                // Large Circular SOS Button with Pulsing Radar Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(170.dp)
                        .padding(8.dp)
                ) {
                    // Pulsing Outer Radar Ring
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .graphicsLayer {
                                scaleX = sosPulseScale
                                scaleY = sosPulseScale
                                alpha = sosPulseAlpha
                            }
                            .clip(CircleShape)
                            .background(ResQSyncEmergencyRed.copy(alpha = 0.3f))
                    )

                    // Core Circular Interactive SOS Button
                    Surface(
                        onClick = onReportEmergency,
                        modifier = Modifier.size(130.dp),
                        shape = CircleShape,
                        color = ResQSyncEmergencyRed,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🆘",
                                fontSize = 26.sp
                            )
                            Text(
                                text = "SOS",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "TAP TO REPORT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFEBEE)
                            )
                        }
                    }
                }
            }
        }

        // Network Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Mesh Network Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartNearby,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ResQSyncPrimaryBlue)
                    ) {
                        Text("Start Mesh", color = Color.White, fontWeight = FontWeight.Bold)
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
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${discoveredDeviceNames.size}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ResQSyncPrimaryBlue
                    )
                    Text(
                        text = "Discovered Peers",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // Mission statement card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Store-Carry-Forward Mesh",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResQSyncTextDark
                )

                Text(
                    "ResQSync keeps disaster reports safe on your device and automatically routes them across intermediate smartphones until they reach rescue personnel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        statusMessage = "Disaster Mesh Network Active",
        discoveredDeviceNames = listOf("Peer 1", "Peer 2"),
        connectedCount = 2,
        totalReportsCount = 3,
        onStartNearby = {},
        onStopNearby = {},
        onReportEmergency = {}
    )
}
