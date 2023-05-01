package com.neowise.tracko.constant

class Urls {
    companion object {

        private const val BASE_URL = "{base_url}"

        const val LOGIN_URL = BASE_URL + "account/login"
        const val REGISTER_URL = BASE_URL + "account/register"
        const val SESSIONS_URL = BASE_URL + "GpsSessions"
        const val LOCATIONS_URL = BASE_URL + "GpsLocations"
    }
}