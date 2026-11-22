
package com.resqsync.app.networking.discovery

import android.content.Context
import android.util.Log

import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Strategy

class NearbyDeviceScanner(
    context: Context,

    private val onDeviceFound: (
        endpointId: String,
        deviceName: String
    ) -> Unit = { _, _ -> },

    private val onDeviceLost: (
        endpointId: String
    ) -> Unit = {},

    private val onStatusChanged: (
        message: String
    ) -> Unit = {}
) {

    private val connectionsClient =
        Nearby.getConnectionsClient(context.applicationContext)

    private val serviceId = "com.resqsync.app"

    private val strategy = Strategy.P2P_CLUSTER

    private var isDiscovering = false


    // Start discovering nearby ResQSync devices
    fun startDiscovery() {

        if (isDiscovering) {
            return
        }

        connectionsClient.startDiscovery(
            serviceId,

            object : EndpointDiscoveryCallback() {

                override fun onEndpointFound(
                    endpointId: String,
                    info: DiscoveredEndpointInfo
                ) {

                    Log.d(
                        "ResQSyncNetwork",
                        "Device discovered: ${info.endpointName}"
                    )

                    onDeviceFound(
                        endpointId,
                        info.endpointName
                    )
                }

                override fun onEndpointLost(
                    endpointId: String
                ) {

                    Log.d(
                        "ResQSyncNetwork",
                        "Device lost: $endpointId"
                    )

                    onDeviceLost(endpointId)
                }
            },

            DiscoveryOptions.Builder()
                .setStrategy(strategy)
                .build()
        )
            .addOnSuccessListener {

                isDiscovering = true

                Log.d(
                    "ResQSyncNetwork",
                    "Nearby discovery started"
                )

                onStatusChanged(
                    "Searching for nearby ResQSync devices..."
                )
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "ResQSyncNetwork",
                    "Discovery failed",
                    exception
                )

                onStatusChanged(
                    "Discovery failed: ${exception.localizedMessage}"
                )
            }
    }


    // Stop device discovery
    fun stopDiscovery() {

        connectionsClient.stopDiscovery()

        isDiscovering = false

        Log.d(
            "ResQSyncNetwork",
            "Nearby discovery stopped"
        )

        onStatusChanged("Device discovery stopped.")
    }
}
