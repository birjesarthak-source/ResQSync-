package com.resqsync.app

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.lifecycle.lifecycleScope

import com.resqsync.app.ai.HybridAIEngine
import com.resqsync.app.common.ResQSyncConstants
import com.resqsync.app.database.DatabaseManager
import com.resqsync.app.database.repository.ReportRepository
import com.resqsync.app.location.GPSLocationManager

import com.resqsync.app.models.DeliveryStatus
import com.resqsync.app.models.DeviceRole
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.EmergencyReport
import com.resqsync.app.models.IncidentType
import com.resqsync.app.models.LocationSource
import com.resqsync.app.models.NetworkMessage
import com.resqsync.app.models.NearbyPeer
import com.resqsync.app.models.RescueUpdate
import com.resqsync.app.models.UrgencyLevel
import com.resqsync.app.models.VerificationStatus

import com.resqsync.app.networking.connection.NearbyConnectionManager
import com.resqsync.app.networking.discovery.NearbyDeviceAdvertiser
import com.resqsync.app.networking.discovery.NearbyDeviceScanner
import com.resqsync.app.networking.discovery.NearbyPermissionManager
import com.resqsync.app.networking.routing.MessageRouter
import com.resqsync.app.networking.transport.PacketType
import com.resqsync.app.sync.SupabaseSyncManager

import com.resqsync.app.ui.navigation.AppNavigation
import com.resqsync.app.ui.screens.WelcomeScreen
import com.resqsync.app.ui.theme.ResQSyncTheme

import kotlinx.coroutines.launch
import java.util.UUID


private data class PendingConnection(
    val endpointId: String,
    val deviceName: String,
    val authenticationDigits: String
)


class MainActivity : ComponentActivity() {

    // DATABASE & MANAGERS
    private lateinit var reportRepository: ReportRepository
    private lateinit var aiEngine: HybridAIEngine
    private lateinit var syncManager: SupabaseSyncManager
    private lateinit var gpsLocationManager: GPSLocationManager

    // NETWORK COMPONENTS
    private lateinit var scanner: NearbyDeviceScanner
    private lateinit var advertiser: NearbyDeviceAdvertiser
    private lateinit var connectionManager: NearbyConnectionManager

    private val messageRouter = MessageRouter()
    private val nearbyPeers = mutableStateListOf<NearbyPeer>()
    private val rescueUpdates = mutableStateListOf<RescueUpdate>()
    private val attemptedTransmissions = mutableSetOf<String>()
    private val messageSourceEndpoints = mutableMapOf<String, String>()

    // UI STATES
    private var statusMessage by mutableStateOf("Ready to start nearby disaster mesh networking.")
    private var showWelcome by mutableStateOf(true)
    private var pendingConnection by mutableStateOf<PendingConnection?>(null)
    private var pendingLocationRequestEndpoint by mutableStateOf<String?>(null)
    private var receivedNearbyLocation by mutableStateOf<EmergencyLocation?>(null)
    private var nearbyStarted by mutableStateOf(false)
    private var startAfterPermission = false
    private var deviceRole by mutableStateOf(DeviceRole.PUBLIC_USER)


    // PERMISSION HANDLER
    private val nearbyPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (areNearbyPermissionsGranted()) {
                statusMessage = "Nearby permissions granted!"
                Log.d("ResQSync", "Nearby permissions granted")
                if (startAfterPermission) {
                    startAfterPermission = false
                    ensureRadiosReadyAndStart()
                }
            } else {
                startAfterPermission = false
                statusMessage = "Nearby permissions are required."
                Log.e("ResQSync", "Nearby permissions denied: $permissions")
            }
        }

    // BLUETOOTH ENABLE REQUEST
    private val bluetoothEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && isBluetoothEnabled()) {
                statusMessage = "Bluetooth enabled. Checking Wi-Fi..."
                ensureWifiReadyAndStart()
            } else {
                statusMessage = "Please enable Bluetooth to use Nearby networking."
            }
        }

    // WI-FI SETTINGS REQUEST
    private val wifiSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isWifiEnabled()) {
                statusMessage = "Wi-Fi enabled. Starting Nearby..."
                startNearbyServices()
            } else {
                statusMessage = "Please enable Wi-Fi and press Start Nearby again."
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reportRepository = DatabaseManager(applicationContext).reportRepository
        aiEngine = HybridAIEngine()
        syncManager = SupabaseSyncManager(applicationContext, reportRepository)
        gpsLocationManager = GPSLocationManager(applicationContext)

        showWelcome = !getSharedPreferences("resqsync_preferences", MODE_PRIVATE)
            .getBoolean("welcome_completed", false)

        initializeNearbyComponents()

        // Periodically trigger cloud sync when network is present
        lifecycleScope.launch {
            if (syncManager.isInternetAvailable()) {
                syncManager.syncPendingReports()
            }
        }

        setContent {
            ResQSyncTheme {
                val reports by reportRepository.observeReports().collectAsState(initial = emptyList())

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (showWelcome) {
                        WelcomeScreen(
                            onGetStarted = {
                                getSharedPreferences("resqsync_preferences", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("welcome_completed", true)
                                    .apply()
                                showWelcome = false
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppNavigation(
                                statusMessage = statusMessage,
                                nearbyPeers = nearbyPeers.toList(),
                                reports = reports,
                                rescueUpdates = rescueUpdates.toList(),
                                deviceId = getResQSyncDeviceId(),
                                deviceRole = deviceRole,
                                onStartNearby = { startNearby() },
                                onStopNearby = { stopNearby() },
                                onConnectDevice = { endpointId -> connectToDevice(endpointId) },
                                onDisconnectDevice = { endpointId -> disconnectDevice(endpointId) },
                                onRoleChanged = { newRole -> deviceRole = newRole },
                                onManualSyncRequested = {
                                    lifecycleScope.launch {
                                        val result = syncManager.syncPendingReports()
                                        statusMessage = result.message
                                    }
                                },
                                onClearDataRequested = {
                                    lifecycleScope.launch {
                                        reportRepository.cleanupExpiredRecords()
                                        statusMessage = "Cleaned up expired records and mesh cache."
                                    }
                                },
                                onRequestNearbyLocation = { endpointId ->
                                    connectionManager.sendLocationRequest(endpointId, getResQSyncDeviceId())
                                },
                                receivedNearbyLocation = receivedNearbyLocation,
                                onSubmitReport = { incidentType, description, peopleAffected, urgency, emergencyLocation ->
                                    saveEmergencyReport(incidentType, description, peopleAffected, urgency, emergencyLocation)
                                }
                            )

                            // Connection request dialog
                            pendingConnection?.let { request ->
                                AlertDialog(
                                    onDismissRequest = { rejectConnection(request) },
                                    title = { Text("Connection Request") },
                                    text = {
                                        Text(
                                            "Device: ${request.deviceName}\n\n" +
                                                    "Authentication code:\n" +
                                                    "${request.authenticationDigits}\n\n" +
                                                    "Compare the code on both phones before accepting."
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { acceptConnection(request) }) {
                                            Text("Accept")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { rejectConnection(request) }) {
                                            Text("Reject")
                                        }
                                    }
                                )
                            }

                            // Explicit Consent Dialog for Nearby Location Sharing
                            pendingLocationRequestEndpoint?.let { endpointId ->
                                AlertDialog(
                                    onDismissRequest = { pendingLocationRequestEndpoint = null },
                                    title = { Text("Location Sharing Consent") },
                                    text = {
                                        Text(
                                            "A nearby connected peer is requesting your current GPS coordinates to attach as a reference to an emergency report.\n\n" +
                                                    "Do you grant permission to share your location?"
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                val requester = endpointId
                                                pendingLocationRequestEndpoint = null
                                                lifecycleScope.launch {
                                                    val loc = gpsLocationManager.getStructuredOfflineLocation()
                                                    connectionManager.sendLocationResponse(requester, loc, getResQSyncDeviceId())
                                                    statusMessage = "Shared location reference with peer."
                                                }
                                            }
                                        ) {
                                            Text("Share Location")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { pendingLocationRequestEndpoint = null }
                                        ) {
                                            Text("Decline")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    private fun getResQSyncDeviceId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
    }


    private fun saveEmergencyReport(
        incidentType: IncidentType,
        description: String,
        peopleAffected: Int?,
        urgency: UrgencyLevel,
        emergencyLocation: EmergencyLocation = EmergencyLocation()
    ) {
        val currentTime = System.currentTimeMillis()
        val deviceId = getResQSyncDeviceId()

        val rawReport = EmergencyReport(
            reportId = UUID.randomUUID().toString(),
            reporterDeviceId = deviceId,
            incidentType = incidentType,
            description = description,
            latitude = emergencyLocation.latitude,
            longitude = emergencyLocation.longitude,
            location = emergencyLocation,
            urgency = urgency,
            peopleAffected = peopleAffected,
            verificationStatus = VerificationStatus.UNVERIFIED,
            createdAt = currentTime,
            expiresAt = currentTime + ResQSyncConstants.MESSAGE_EXPIRY_HOURS * 60L * 60L * 1000L
        )

        lifecycleScope.launch {
            try {
                // AI analysis (on-device or rules engine fallback)
                val aiResult = aiEngine.analyzeReport(rawReport, syncManager.isInternetAvailable())
                val report = rawReport.copy(
                    incidentType = aiResult.detectedIncidentType,
                    urgency = aiResult.detectedUrgency,
                    peopleAffected = aiResult.peopleAffected
                )

                val networkMessage = NetworkMessage(
                    messageId = UUID.randomUUID().toString(),
                    reportId = report.reportId,
                    senderDeviceId = deviceId,
                    destinationId = null,
                    createdAt = currentTime,
                    hopCount = 0,
                    maxHops = ResQSyncConstants.MAX_HOPS,
                    ttlExpiresAt = report.expiresAt,
                    visitedDeviceIds = listOf(deviceId),
                    payload = report
                )

                reportRepository.saveReport(report)
                reportRepository.saveNetworkMessage(networkMessage, DeliveryStatus.QUEUED)
                messageRouter.markAsProcessed(networkMessage.messageId)

                val connectedPeers = nearbyPeers.filter { it.isConnected }
                connectedPeers.forEach { peer ->
                    sendMessageToPeer(peer.endpointId, networkMessage)
                }

                statusMessage = if (connectedPeers.isEmpty()) {
                    "Emergency Report saved locally (${report.location.source.name}). Waiting for mesh peers."
                } else {
                    "Report saved & transfer initiated to ${connectedPeers.size} connected peer(s)."
                }
            } catch (exception: Exception) {
                statusMessage = "Failed to save emergency report."
                Log.e("ResQSync", "Report saving failed", exception)
            }
        }
    }


    private fun sendMessageToPeer(endpointId: String, message: NetworkMessage) {
        val transmissionKey = "${message.messageId}@$endpointId"
        if (transmissionKey in attemptedTransmissions) return

        attemptedTransmissions.add(transmissionKey)
        connectionManager.sendMessage(endpointId, message)
        lifecycleScope.launch {
            reportRepository.updateMessageStatus(message.messageId, DeliveryStatus.SENT_TO_PEER)
        }
    }


    private fun handleReceivedMessage(sourceEndpointId: String, message: NetworkMessage) {
        lifecycleScope.launch {
            try {
                val deviceId = getResQSyncDeviceId()

                if (!messageRouter.shouldForward(message, deviceId)) {
                    Log.d("ResQSyncNetwork", "Duplicate/expired message ignored.")
                    return@launch
                }

                // AI Processing on received report
                val aiResult = aiEngine.analyzeReport(message.payload, syncManager.isInternetAvailable())
                val processedReport = message.payload.copy(
                    incidentType = aiResult.detectedIncidentType,
                    urgency = aiResult.detectedUrgency
                )

                reportRepository.saveReport(processedReport)
                reportRepository.saveNetworkMessage(message, DeliveryStatus.RECEIVED)
                reportRepository.updateReportAISummary(processedReport.reportId, aiResult.summary)

                messageSourceEndpoints[message.messageId] = sourceEndpointId
                messageRouter.markAsProcessed(message.messageId)

                // Send Delivery Acknowledgement back to sender endpoint!
                connectionManager.sendDeliveryAck(sourceEndpointId, message.messageId, deviceId)

                val forwardingPeers = nearbyPeers.filter {
                    it.isConnected && it.endpointId != sourceEndpointId
                }

                if (forwardingPeers.isNotEmpty()) {
                    val forwardedMessage = messageRouter.createForwardedMessage(message, deviceId)
                    reportRepository.saveNetworkMessage(forwardedMessage, DeliveryStatus.FORWARDED)

                    forwardingPeers.forEach { peer ->
                        sendMessageToPeer(peer.endpointId, forwardedMessage)
                    }
                    statusMessage = "Emergency report received and forwarded."
                } else {
                    statusMessage = "Emergency report received and saved locally."
                }
            } catch (exception: Exception) {
                Log.e("ResQSyncNetwork", "Incoming message processing failed", exception)
            }
        }
    }


    private fun sendPendingMessagesToPeer(endpointId: String) {
        lifecycleScope.launch {
            try {
                val currentDeviceId = getResQSyncDeviceId()
                val pendingMessages = reportRepository.getPendingMessages()

                pendingMessages.forEach { message ->
                    if (messageSourceEndpoints[message.messageId] == endpointId) return@forEach
                    if (System.currentTimeMillis() >= message.ttlExpiresAt || message.hopCount >= message.maxHops) return@forEach

                    val outgoingMessage = when {
                        message.senderDeviceId == currentDeviceId -> message
                        currentDeviceId in message.visitedDeviceIds -> message
                        else -> {
                            if (!messageRouter.shouldForward(message, currentDeviceId)) return@forEach
                            val forwarded = messageRouter.createForwardedMessage(message, currentDeviceId)
                            messageRouter.markAsProcessed(message.messageId)
                            reportRepository.saveNetworkMessage(forwarded, DeliveryStatus.FORWARDED)
                            forwarded
                        }
                    }
                    sendMessageToPeer(endpointId, outgoingMessage)
                }
            } catch (exception: Exception) {
                Log.e("ResQSyncNetwork", "Pending message forwarding failed", exception)
            }
        }
    }


    private fun initializeNearbyComponents() {
        connectionManager = NearbyConnectionManager(
            context = this,
            onConnectionRequest = { endpointId, deviceName, authDigits ->
                showConnectionRequest(endpointId, deviceName, authDigits)
            }
        )

        connectionManager.onConnectionStateChanged = { endpointId, connected, message ->
            runOnUiThread {
                val index = nearbyPeers.indexOfFirst { it.endpointId == endpointId }
                if (index >= 0) {
                    nearbyPeers[index] = nearbyPeers[index].copy(isConnected = connected)
                } else if (connected) {
                    nearbyPeers.add(
                        NearbyPeer(endpointId = endpointId, deviceName = "ResQSync Peer", isConnected = true)
                    )
                }

                statusMessage = message
                if (connected) {
                    sendPendingMessagesToPeer(endpointId)
                }
            }
        }

        connectionManager.onMessageReceived = { endpointId, message ->
            handleReceivedMessage(endpointId, message)
        }

        connectionManager.onPacketReceived = { endpointId, packet ->
            when (packet.packetType) {
                PacketType.DELIVERY_ACK -> {
                    if (packet.acknowledgedMessageId != null) {
                        lifecycleScope.launch {
                            reportRepository.updateMessageStatus(packet.acknowledgedMessageId, DeliveryStatus.DELIVERED)
                            statusMessage = "Report delivery confirmed (ACK received)."
                        }
                    }
                }
                PacketType.LOCATION_REQUEST -> {
                    runOnUiThread {
                        pendingLocationRequestEndpoint = endpointId
                    }
                }
                PacketType.LOCATION_RESPONSE -> {
                    if (packet.requestedLocation != null) {
                        runOnUiThread {
                            receivedNearbyLocation = packet.requestedLocation.copy(
                                source = LocationSource.NEARBY_DEVICE,
                                sourceDeviceId = packet.senderDeviceId
                            )
                            statusMessage = "Received location reference from peer ${packet.senderDeviceId.take(8)}."
                        }
                    }
                }
                else -> {}
            }
        }

        advertiser = NearbyDeviceAdvertiser(
            context = this,
            connectionLifecycleCallback = connectionManager.connectionLifecycleCallback,
            onStatusChanged = { msg -> runOnUiThread { statusMessage = msg } }
        )

        scanner = NearbyDeviceScanner(
            context = this,
            onDeviceFound = { endpointId, deviceName ->
                runOnUiThread {
                    val index = nearbyPeers.indexOfFirst { it.endpointId == endpointId }
                    if (index >= 0) {
                        nearbyPeers[index] = nearbyPeers[index].copy(deviceName = deviceName)
                    } else {
                        nearbyPeers.add(NearbyPeer(endpointId = endpointId, deviceName = deviceName, isConnected = false))
                    }
                    statusMessage = "Discovered device: $deviceName"
                }
            },
            onDeviceLost = { endpointId ->
                runOnUiThread {
                    val index = nearbyPeers.indexOfFirst { it.endpointId == endpointId }
                    if (index >= 0 && !nearbyPeers[index].isConnected) {
                        nearbyPeers.removeAt(index)
                    }
                }
            },
            onStatusChanged = { msg -> runOnUiThread { statusMessage = msg } }
        )
    }


    private fun connectToDevice(endpointId: String) {
        if (!nearbyStarted) {
            statusMessage = "Start Nearby before requesting a connection."
            return
        }
        val deviceName = Build.MODEL.takeIf { it.isNotBlank() } ?: "ResQSync Device"
        statusMessage = "Requesting connection..."
        connectionManager.startConnection(deviceName, endpointId)
    }


    private fun disconnectDevice(endpointId: String) {
        connectionManager.disconnectDevice(endpointId)
        statusMessage = "Disconnect requested."
    }


    private fun showConnectionRequest(endpointId: String, deviceName: String, authDigits: String) {
        runOnUiThread {
            pendingConnection = PendingConnection(endpointId, deviceName, authDigits)
            statusMessage = "Connection request from $deviceName"
        }
    }

    private fun acceptConnection(request: PendingConnection) {
        connectionManager.acceptConnection(request.endpointId)
        pendingConnection = null
        statusMessage = "Accepted. Connecting..."
    }

    private fun rejectConnection(request: PendingConnection) {
        connectionManager.rejectConnection(request.endpointId)
        pendingConnection = null
        statusMessage = "Rejected connection from ${request.deviceName}"
    }


    private fun startNearby() {
        if (nearbyStarted) {
            statusMessage = "Nearby networking is already active."
            return
        }
        if (!areNearbyPermissionsGranted()) {
            startAfterPermission = true
            requestNearbyPermissions()
            return
        }
        ensureRadiosReadyAndStart()
    }


    @SuppressLint("MissingPermission")
    private fun ensureRadiosReadyAndStart() {
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val adapter = bluetoothManager?.adapter
        if (adapter == null || !adapter.isEnabled) {
            statusMessage = "Please enable Bluetooth to continue."
            try {
                bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } catch (e: Exception) {
                statusMessage = "Enable Bluetooth manually in settings."
            }
            return
        }
        ensureWifiReadyAndStart()
    }

    @SuppressLint("MissingPermission")
    private fun isBluetoothEnabled(): Boolean {
        return try {
            getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true
        } catch (e: SecurityException) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun isWifiEnabled(): Boolean {
        return try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled
        } catch (e: SecurityException) {
            false
        }
    }

    private fun ensureWifiReadyAndStart() {
        if (isWifiEnabled()) {
            startNearbyServices()
            return
        }
        statusMessage = "Please enable Wi-Fi."
        val wifiIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        try {
            wifiSettingsLauncher.launch(wifiIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun areNearbyPermissionsGranted(): Boolean {
        return NearbyPermissionManager.getRequiredPermissions().all { permission ->
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestNearbyPermissions() {
        val missing = NearbyPermissionManager.getRequiredPermissions().filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            nearbyPermissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startNearbyServices() {
        if (nearbyStarted) return
        val deviceName = Build.MODEL.takeIf { it.isNotBlank() } ?: "ResQSync Device"

        attemptedTransmissions.clear()
        messageSourceEndpoints.clear()
        nearbyPeers.clear()

        scanner.startDiscovery()
        advertiser.startAdvertising(deviceName)
        nearbyStarted = true
        statusMessage = "Nearby mesh discovery & advertising active."
    }

    private fun stopNearby() {
        if (nearbyStarted) {
            scanner.stopDiscovery()
            advertiser.stopAdvertising()
            connectionManager.disconnectAll()
            nearbyStarted = false
        }
        nearbyPeers.clear()
        attemptedTransmissions.clear()
        statusMessage = "Nearby networking stopped."
    }

    override fun onDestroy() {
        if (nearbyStarted) {
            scanner.stopDiscovery()
            advertiser.stopAdvertising()
            connectionManager.disconnectAll()
        }
        super.onDestroy()
    }
}
