package com.resqsync.app.networking.discovery

import android.Manifest
import android.os.Build

object NearbyPermissionManager {

    fun getRequiredPermissions(): Array<String> {

        val basePermissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO
        )

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                basePermissions.addAll(
                    listOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.NEARBY_WIFI_DEVICES,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                basePermissions.addAll(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    )
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                basePermissions.addAll(
                    listOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }

            else -> {
                basePermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }

        return basePermissions.toTypedArray()
    }
}
