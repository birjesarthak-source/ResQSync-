package com.resqsync.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import com.resqsync.app.models.DeviceRole
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.IncidentType
import com.resqsync.app.models.NearbyPeer
import com.resqsync.app.models.RescueUpdate
import com.resqsync.app.models.UrgencyLevel

import com.resqsync.app.ui.screens.HomeScreen
import com.resqsync.app.ui.screens.NetworkScreen
import com.resqsync.app.ui.screens.ReportScreen
import com.resqsync.app.ui.screens.ReportsScreen
import com.resqsync.app.ui.screens.SettingsScreen
import com.resqsync.app.ui.screens.UpdatesScreen

private enum class AppTab(
    val title: String,
    val symbol: String
) {
    HOME("Home", "⌂"),
    REPORTS("Reports", "▤"),
    NETWORK("Network", "⌁"),
    UPDATES("Updates", "♧"),
    SETTINGS("Settings", "⚙")
}

@Composable
fun AppNavigation(
    statusMessage: String,
    nearbyPeers: List<NearbyPeer>,
    reports: List<EmergencyReport>,
    rescueUpdates: List<RescueUpdate> = emptyList(),
    deviceId: String = "unknown-device",
    deviceRole: DeviceRole = DeviceRole.PUBLIC_USER,

    onStartNearby: () -> Unit,
    onStopNearby: () -> Unit,

    onConnectDevice: (String) -> Unit,
    onDisconnectDevice: (String) -> Unit,

    onRoleChanged: (DeviceRole) -> Unit = {},
    onManualSyncRequested: () -> Unit = {},
    onClearDataRequested: () -> Unit = {},

    onRequestNearbyLocation: ((endpointId: String) -> Unit)? = null,
    receivedNearbyLocation: EmergencyLocation? = null,

    onSubmitReport: (
        IncidentType,
        String,
        Int?,
        UrgencyLevel,
        EmergencyLocation
    ) -> Unit
) {

    var selectedTab by remember {
        mutableStateOf(AppTab.HOME)
    }

    var showReportForm by remember {
        mutableStateOf(false)
    }

    Scaffold(

        bottomBar = {

            if (!showReportForm) {

                NavigationBar {

                    AppTab.entries.forEach { tab ->

                        NavigationBarItem(

                            selected = selectedTab == tab,

                            onClick = {
                                selectedTab = tab
                            },

                            icon = {
                                Text(tab.symbol)
                            },

                            label = {
                                Text(tab.title)
                            }
                        )
                    }
                }
            }
        }

    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (showReportForm) {

                ReportScreen(

                    onSubmit = {
                            incidentType,
                            description,
                            peopleAffected,
                            urgency,
                            emergencyLocation ->

                        onSubmitReport(
                            incidentType,
                            description,
                            peopleAffected,
                            urgency,
                            emergencyLocation
                        )

                        showReportForm = false
                        selectedTab = AppTab.HOME
                    },

                    onCancel = {
                        showReportForm = false
                    },

                    nearbyPeers = nearbyPeers,
                    onRequestNearbyLocation = onRequestNearbyLocation,
                    receivedNearbyLocation = receivedNearbyLocation
                )

            } else {

                when (selectedTab) {

                    // HOME
                    AppTab.HOME -> {

                        HomeScreen(

                            statusMessage = statusMessage,

                            discoveredDeviceNames =
                                nearbyPeers.map { it.deviceName },

                            connectedCount = nearbyPeers.count { it.isConnected },

                            totalReportsCount = reports.size,

                            onStartNearby = onStartNearby,

                            onStopNearby = onStopNearby,

                            onReportEmergency = {
                                showReportForm = true
                            }
                        )
                    }

                    // REPORTS
                    AppTab.REPORTS -> {

                        ReportsScreen(
                            reports = reports
                        )
                    }

                    // NETWORK
                    AppTab.NETWORK -> {

                        NetworkScreen(

                            statusMessage = statusMessage,

                            nearbyPeers = nearbyPeers,

                            onStartNearby = onStartNearby,

                            onStopNearby = onStopNearby,

                            onConnectDevice = onConnectDevice,

                            onDisconnectDevice = onDisconnectDevice
                        )
                    }

                    // UPDATES
                    AppTab.UPDATES -> {

                        UpdatesScreen(
                            updates = rescueUpdates
                        )
                    }

                    // SETTINGS
                    AppTab.SETTINGS -> {

                        SettingsScreen(
                            deviceId = deviceId,
                            deviceRole = deviceRole,
                            onRoleChanged = onRoleChanged,
                            onManualSyncRequested = onManualSyncRequested,
                            onClearDataRequested = onClearDataRequested
                        )
                    }
                }
            }
        }
    }
}
