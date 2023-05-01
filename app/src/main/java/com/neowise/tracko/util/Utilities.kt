package com.neowise.tracko.util

import java.util.concurrent.TimeUnit

object Utilities {

    fun formatTime(millis: Long): String {
        return String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millis)
                ),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millis)
                )
        )
    }

    fun formatDistance(distance: Double): String = "%.1f".format(distance) + "m"

    fun getPace(millis: Long, distance: Float): String {

        val speed = millis / 60.0 / distance
        if (speed > 99) return "--:--"
        val minutes = (speed ).toInt();
        val seconds = ((speed - minutes) * 60).toInt()

        return minutes.toString() + ":" + (if (seconds < 10)  "0" else "") +seconds.toString();
    }
}