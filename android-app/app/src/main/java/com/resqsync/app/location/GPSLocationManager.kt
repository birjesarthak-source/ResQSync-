package com.resqsync.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.resqsync.app.models.EmergencyLocation
import com.resqsync.app.models.LocationSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class GPSLocationManager(private val context: Context) {

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun isLocationPermissionGranted(): Boolean {
        val finePerm = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePerm = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return finePerm || coarsePerm
    }

    fun isGPSEnabled(): Boolean {
        return try {
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getStructuredOfflineLocation(): EmergencyLocation {
        if (!isLocationPermissionGranted() || locationManager == null) {
            Log.w("ResQSyncGPS", "Location permissions missing or LocationManager unavailable")
            return EmergencyLocation(source = LocationSource.UNAVAILABLE)
        }

        val now = System.currentTimeMillis()

        // 1. First attempt: Fresh or Cached GPS location
        try {
            val gpsLast = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (gpsLast != null) {
                val ageMs = now - gpsLast.time
                if (ageMs < 60_000) {
                    Log.d("ResQSyncGPS", "Fresh GPS location acquired: ${gpsLast.latitude}, ${gpsLast.longitude}")
                    return toEmergencyLocation(gpsLast, LocationSource.GPS)
                } else if (ageMs < 1_800_000) {
                    Log.d("ResQSyncGPS", "Cached GPS location acquired (age ${ageMs / 1000}s): ${gpsLast.latitude}")
                    return toEmergencyLocation(gpsLast, LocationSource.CACHED_GPS)
                }
            }
        } catch (e: Exception) {
            Log.w("ResQSyncGPS", "Failed to query GPS_PROVIDER last known location", e)
        }

        // 2. Second attempt: Fresh single update from GPS
        val freshLocation = requestFreshLocationUpdate()
        if (freshLocation != null) {
            return toEmergencyLocation(freshLocation, LocationSource.GPS)
        }

        // 3. Fallback attempt: Network / Passive cached location
        try {
            val netLast = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (netLast != null) {
                Log.d("ResQSyncGPS", "Fallback to cached network/passive location")
                return toEmergencyLocation(netLast, LocationSource.CACHED_GPS)
            }
        } catch (e: Exception) {
            Log.w("ResQSyncGPS", "Failed to query NETWORK/PASSIVE last known location", e)
        }

        return EmergencyLocation(source = LocationSource.UNAVAILABLE)
    }

    @SuppressLint("MissingPermission")
    suspend fun getOfflineLocation(): Location? {
        val emergencyLoc = getStructuredOfflineLocation()
        if (emergencyLoc.latitude != null && emergencyLoc.longitude != null) {
            val loc = Location("ResQSync")
            loc.latitude = emergencyLoc.latitude
            loc.longitude = emergencyLoc.longitude
            loc.accuracy = emergencyLoc.accuracyMeters ?: 0f
            loc.time = emergencyLoc.timestamp ?: System.currentTimeMillis()
            return loc
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocationUpdate(): Location? {
        if (locationManager == null) return null
        return withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try {
                            locationManager.removeUpdates(this)
                        } catch (e: Exception) {
                            // ignore
                        }
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                try {
                    val provider = when {
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                        else -> LocationManager.PASSIVE_PROVIDER
                    }

                    locationManager.requestLocationUpdates(
                        provider,
                        0L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )

                    continuation.invokeOnCancellation {
                        try {
                            locationManager.removeUpdates(listener)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ResQSyncGPS", "Error requesting location updates", e)
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    private fun toEmergencyLocation(location: Location, source: LocationSource): EmergencyLocation {
        return EmergencyLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
            altitudeMeters = if (location.hasAltitude()) location.altitude else null,
            timestamp = location.time,
            source = source
        )
    }
}
