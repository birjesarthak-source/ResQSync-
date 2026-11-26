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
import com.resqsync.app.models.RescueUpdate
import com.resqsync.app.ui.theme.ResQSyncPrimaryBlue
import com.resqsync.app.ui.theme.ResQSyncSuccessGreen
import com.resqsync.app.ui.theme.ResQSyncTextDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UpdatesScreen(
    updates: List<RescueUpdate> = emptyList()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Rescue & System Updates",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ResQSyncTextDark
        )

        Text(
            text = "Official announcements and rescue team broadcast updates relayed across the mesh.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (updates.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No Rescue Broadcasts Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ResQSyncTextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "When rescue teams broadcast safety instructions or status updates, they will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(updates) { update ->
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
                                    text = "Rescue Team Update",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ResQSyncPrimaryBlue
                                )
                                Surface(
                                    color = ResQSyncSuccessGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = update.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ResQSyncSuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = update.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ResQSyncTextDark
                            )

                            Text(
                                text = formatTimestamp(update.createdAt),
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

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
