package com.resqsync.app.networking.connection

interface ConnectionLifecycleCallback {
    fun onDeviceConnected(deviceId: String)

    fun onDeviceDisconnected(deviceId: String)

    fun onConnectionFailed(deviceId: String, error: String)
}