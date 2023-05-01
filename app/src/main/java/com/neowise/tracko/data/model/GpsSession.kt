package com.neowise.tracko.data.model

data class GpsSession(
        var name: String = "",
        val description: String = "",
        val recordedAt: String = "",
        val duration: Int = 0,
        val speed: Int = 0,
        val distance: Int = 0,
        val climb: Int = 0,
        val descent: Int = 0,
        val appUserId: String = "",
        val id: String = ""
)