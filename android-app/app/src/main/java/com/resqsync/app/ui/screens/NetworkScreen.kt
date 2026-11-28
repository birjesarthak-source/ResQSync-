
package com.resqsync.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.resqsync.app.models.NearbyPeer
import com.resqsync.app.ui.theme.ResQSyncTextDark
import androidx.compose.ui.graphics.Color

@Composable
fun NetworkScreen(
    statusMessage: String,
    nearbyPeers: List<NearbyPeer>,
    onStartNearby: () -> Unit,
    onStopNearby: () -> Unit,
    onConnectDevice: (String) -> Unit,
    onDisconnectDevice: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Nearby network",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Connect with nearby ResQSync devices and exchange emergency messages.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Network status
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    "Network Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(statusMessage)

                Text(
                    "Devices discovered: ${nearbyPeers.size}"
                )

                Text(
                    "Connected devices: ${
                        nearbyPeers.count { it.isConnected }
                    }"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Button(
                        onClick = onStartNearby,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start Nearby")
                    }

                    OutlinedButton(
                        onClick = onStopNearby,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop")
                    }
                }
            }
        }

        // Device list
        Text(
            text = "Nearby Devices",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (nearbyPeers.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "No Nearby Devices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Start Nearby on this and another ResQSync device.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = nearbyPeers,
                    key = { it.endpointId }
                ) { peer ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                text = peer.deviceName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Endpoint: ${
                                    peer.endpointId.take(8)
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (peer.isConnected) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Connected",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        onDisconnectDevice(peer.endpointId)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Disconnect")
                                }

                            } else {

                                Text(
                                    "Available for connection",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        onConnectDevice(peer.endpointId)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Request Connection")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mesh information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Text(
                    "Mesh Communication",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Connected devices can exchange emergency messages. " +
                            "ResQSync will use multi-hop forwarding to relay " +
                            "messages through available devices."
                )
            }
        }
    }
}
