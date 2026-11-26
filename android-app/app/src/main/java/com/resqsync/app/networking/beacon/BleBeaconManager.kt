package com.resqsync.app.networking.beacon

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.nio.charset.StandardCharsets
import java.util.UUID

data class SafetyBeaconInfo(
    val beaconId: String,
    val shelterName: String,
    val rssi: Int,
    val timestamp: Long
)

class BleBeaconManager(private val context: Context) {

    companion object {
        val BEACON_SERVICE_UUID: UUID = UUID.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bleAdvertiser: BluetoothLeAdvertiser? = null
    private var bleScanner: BluetoothLeScanner? = null

    var isBeaconBroadcasting = false
        private set

    @SuppressLint("MissingPermission")
    fun startShelterBeacon(shelterName: String = "ResQSync Relief Point"): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false

        bleAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (bleAdvertiser == null) return false

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()

        val dataNameBytes = shelterName.take(15).toByteArray(StandardCharsets.UTF_8)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(BEACON_SERVICE_UUID))
            .addServiceData(ParcelUuid(BEACON_SERVICE_UUID), dataNameBytes)
            .build()

        return try {
            bleAdvertiser?.startAdvertising(settings, data, advertiseCallback)
            isBeaconBroadcasting = true
            Log.d("ResQSyncBeacon", "Started BLE Safety Beacon: $shelterName")
            true
        } catch (e: Exception) {
            Log.e("ResQSyncBeacon", "Failed to start BLE Safety Beacon", e)
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopShelterBeacon() {
        try {
            if (isBeaconBroadcasting) {
                bleAdvertiser?.stopAdvertising(advertiseCallback)
                isBeaconBroadcasting = false
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d("ResQSyncBeacon", "BLE Beacon advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("ResQSyncBeacon", "BLE Beacon advertising failed: $errorCode")
            isBeaconBroadcasting = false
        }
    }
}
