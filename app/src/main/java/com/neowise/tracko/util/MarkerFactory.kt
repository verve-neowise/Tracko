package com.neowise.tracko.util

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

object MarkerFactory {

    fun checkpointMarker() : MarkerOptions {
        return MarkerOptions()
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .position(LatLng(0.0, 0.0))
    }

    fun waypointMarker() : MarkerOptions {
        return MarkerOptions()
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            .position(LatLng(0.0, 0.0))
    }
}