
package com.resqsync.app.models

data class NearbyPeer(
    val endpointId: String,
    val deviceName: String,
    val isConnected: Boolean = false
)
