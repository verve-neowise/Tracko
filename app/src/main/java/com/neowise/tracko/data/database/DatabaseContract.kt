package com.neowise.tracko.data.database

import android.provider.BaseColumns


object DatabaseContract {

     const val CREATE_SESSION_ENTRIES =
            "CREATE TABLE ${SessionEntry.TABLE} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                    "${SessionEntry.COLUMN_NAME} TEXT," +
                    "${SessionEntry.COLUMN_DESCRIPTION} TEXT," +
                    "${SessionEntry.COLUMN_RECORDED_AT} TEXT," +
                    "${SessionEntry.COLUMN_DURATION} INTEGER," +
                    "${SessionEntry.COLUMN_SPEED} INTEGER," +
                    "${SessionEntry.COLUMN_DISTANCE} INTEGER," +
                    "${SessionEntry.COLUMN_CLIMB} INTEGER," +
                    "${SessionEntry.COLUMN_DESCENT} INTEGER," +
                    "${SessionEntry.COLUMN_APP_USER_ID} TEXT," +
                    "${SessionEntry.COLUMN_SESSION_ID} TEXT)"

    const val DELETE_SESSION_ENTRIES = "DROP TABLE IF EXISTS ${SessionEntry.TABLE}"

    const val CREATE_LOCATION_ENTRIES =
            "CREATE TABLE ${LocationEntry.TABLE} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                    "${LocationEntry.COLUMN_RECORDED_AT} TEXT," +
                    "${LocationEntry.COLUMN_LAT} REAL," +
                    "${LocationEntry.COLUMN_LON} REAL," +
                    "${LocationEntry.COLUMN_ACCURACY} REAL," +
                    "${LocationEntry.COLUMN_ALTITUDE} REAL," +
                    "${LocationEntry.COLUMN_VERTICAL_ACCURACY} REAL," +
                    "${LocationEntry.COLUMN_SESSION_ID} TEXT," +
                    "${LocationEntry.COLUMN_LOCATION_TYPE} TEXT)"

    const val DELETE_LOCATION_ENTRIES = "DROP TABLE IF EXISTS ${LocationEntry.TABLE}"

    object SessionEntry : BaseColumns {
        const val TABLE = "Session"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_RECORDED_AT = "recorded_at"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_SPEED = "speed"
        const val COLUMN_DISTANCE = "distance"
        const val COLUMN_CLIMB = "climb"
        const val COLUMN_DESCENT = "descent"
        const val COLUMN_APP_USER_ID = "app_user_id"
        const val COLUMN_SESSION_ID = "session_id"
    }

    object LocationEntry : BaseColumns {
        const val TABLE = "Location"
        const val COLUMN_RECORDED_AT = "recorded_at"
        const val COLUMN_LAT = "latitude"
        const val COLUMN_LON = "longitude"
        const val COLUMN_ACCURACY = "accuracy"
        const val COLUMN_ALTITUDE = "altitude"
        const val COLUMN_VERTICAL_ACCURACY = "vertical_accuracy"
        const val COLUMN_SESSION_ID = "session_id"
        const val COLUMN_LOCATION_TYPE = "type"
    }
}