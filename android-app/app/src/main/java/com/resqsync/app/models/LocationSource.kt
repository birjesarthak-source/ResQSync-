package com.resqsync.app.models

import kotlinx.serialization.Serializable

@Serializable
enum class LocationSource {
    GPS,
    CACHED_GPS,
    MANUAL_MAP,
    MANUAL_ADDRESS,
    NEARBY_DEVICE,
    UNAVAILABLE
}
