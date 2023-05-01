package com.neowise.tracko.constant

class Action {
    companion object {

        private const val PREFIX = "com.taltech.runnermap."

        const val NOTIFICATION_WP = PREFIX + "wp"
        const val NOTIFICATION_CP = PREFIX + "cp"
        const val NOTIFICATION_START_STOP = PREFIX +  "start-stop"

        const val LOCATION_UPDATE = PREFIX + "location_update"

        const val MAP_UPDATE = PREFIX + "map_update"

        const val LOCATION_UPDATE_STOP = PREFIX + "location_stop"

        const val LOCATION_UPDATE_LAT = PREFIX + "location_update.lat"
        const val LOCATION_UPDATE_LON = PREFIX + "location_update.lon"

        const val LOCATION_UPDATE_OVERALL = PREFIX + "location_update.overall"
        const val LOCATION_UPDATE_CP = PREFIX + "location_update.cp"
        const val LOCATION_UPDATE_WP = PREFIX + "location_update.wp"

    }
}