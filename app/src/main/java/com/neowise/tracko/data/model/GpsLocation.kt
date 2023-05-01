package com.neowise.tracko.data.model

import android.location.Location

data class GpsLocation(
        val recordedAt: String,
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val altitude: Double,
        val verticalAccuracy: Double? = null,
        val sessionId: String,
        val locationTypeId: String
) {
        constructor(location: Location, recordedAt: String, sessionId: String, locationTypeId: String)
                : this(
                recordedAt = recordedAt,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                altitude = location.altitude,
                verticalAccuracy = null,
                sessionId = sessionId,
                locationTypeId = locationTypeId
        )
}