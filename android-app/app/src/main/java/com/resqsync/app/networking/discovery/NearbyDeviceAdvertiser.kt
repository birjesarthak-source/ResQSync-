
package com.resqsync.app.networking.discovery

import android.content.Context
import android.util.Log

import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.Strategy

class NearbyDeviceAdvertiser(
    context: Context,
    private val connectionLifecycleCallback: ConnectionLifecycleCallback,
    private val onStatusChanged: (String) -> Unit = {}
) {

    private val connectionsClient =
        Nearby.getConnectionsClient(context.applicationContext)

    private val serviceId = "com.resqsync.app"

    private val strategy = Strategy.P2P_CLUSTER


    // Start advertising this device
    fun startAdvertising(deviceName: String) {

        connectionsClient.startAdvertising(
            deviceName,
            serviceId,
            connectionLifecycleCallback,

            AdvertisingOptions.Builder()
                .setStrategy(strategy)
                .build()
        )
            .addOnSuccessListener {

                Log.d(
                    "ResQSyncNetwork",
                    "Advertising started successfully"
                )

                onStatusChanged(
                    "Your device is visible to nearby ResQSync devices."
                )
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "ResQSyncNetwork",
                    "Advertising failed",
                    exception
                )

                onStatusChanged(
                    "Advertising failed: ${exception.localizedMessage}"
                )
            }
    }


    // Stop advertising this device
    fun stopAdvertising() {

        connectionsClient.stopAdvertising()

        Log.d(
            "ResQSyncNetwork",
            "Advertising stopped"
        )

        onStatusChanged("Advertising stopped.")
    }
}
