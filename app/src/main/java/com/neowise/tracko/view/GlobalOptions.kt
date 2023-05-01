package com.neowise.tracko.view

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.neowise.tracko.util.MarkerFactory

class GlobalOptions {

    companion object {

        // Service
        var isLocationServiceAlive = false

        var isTrackUserLocation = false

        var isDirectCompass = false
        var isDirectNorthUp = false
        var isDirectDefault = true

        // Map
        var trackingPolyline = PolylineOptions()
        var waypointPolyline = PolylineOptions()

        var waypointMarker = MarkerFactory.waypointMarker()
                                        .title("Waypoint")
                                        .visible(false)

        val checkpointMarkers = ArrayList<MarkerOptions>()

        fun addTrackingPoint(location: Location) {
            trackingPolyline.add(LatLng(location.latitude, location.longitude))
        }

        fun addCheckPoint(lat: Double, lon: Double) {
            val marker = MarkerFactory.checkpointMarker()
                    .title("Checkpoint #${checkpointMarkers.size + 1}")
                    .position(LatLng(lat, lon))
            checkpointMarkers.add(marker)
        }

        fun updateWaypoint(current: Location, waypoint: Location) {
            clearWaypoint()
            val currLatLng = LatLng(current.latitude, current.longitude)
            val wpLatLng = LatLng(waypoint.latitude, waypoint.longitude)

            waypointPolyline.add(currLatLng, wpLatLng)

            waypointMarker
                .position(wpLatLng)
                .visible(true)
        }

        private fun clearTracking() {
            trackingPolyline = PolylineOptions()
        }

        fun clearWaypoint() {
            waypointPolyline = PolylineOptions()
            waypointMarker.visible(false)
        }

        fun clearAll() {
            clearWaypoint()
            clearTracking()
            checkpointMarkers.clear()
        }
    }
}