package com.neowise.tracko.view.map

import android.location.Location
import com.google.gson.Gson

data class Distance(var direct: Float = 0f, var total: Float = 0f, var time: Long = 0L) {

    fun update(new: Location, current: Location, target: Location) {
        direct = new.distanceTo(target)
        total += new.distanceTo(current)
        time += (new.time - current.time)
    }

    fun clear() {
        direct = 0f
        total = 0f
        time = 0L
    }

    fun json(): String? {
        return Gson().toJson(this)
    }
}