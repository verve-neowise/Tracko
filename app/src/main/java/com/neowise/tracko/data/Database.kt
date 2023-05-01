package com.neowise.tracko.data

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import androidx.core.database.getDoubleOrNull
import com.neowise.tracko.data.database.DatabaseContract.LocationEntry
import com.neowise.tracko.data.database.DatabaseContract.SessionEntry
import com.neowise.tracko.data.database.DatabaseHelper
import com.neowise.tracko.data.model.GpsLocation
import com.neowise.tracko.data.model.GpsSession

class Database private constructor(context: Context) {

    companion object {
        private var instance: Database? = null

        fun getInstance(context: Context): Database {
            if (instance == null) {
                instance = Database(context)
            }
            return instance!!
        }
    }

    private val databaseHelper = DatabaseHelper(context)

    fun getSessions(sessionId: String? = null) : List<GpsSession> {

        val db = databaseHelper.readableDatabase

        val projection = arrayOf(
                BaseColumns._ID,
                SessionEntry.COLUMN_NAME,
                SessionEntry.COLUMN_DESCRIPTION,
                SessionEntry.COLUMN_RECORDED_AT,
                SessionEntry.COLUMN_DURATION,
                SessionEntry.COLUMN_SPEED,
                SessionEntry.COLUMN_DISTANCE,
                SessionEntry.COLUMN_CLIMB,
                SessionEntry.COLUMN_DESCENT,
                SessionEntry.COLUMN_APP_USER_ID,
                SessionEntry.COLUMN_SESSION_ID
        )

        var selection: String? = null
        var selectionArgs: Array<String>? = null

        if (sessionId != null) {
            selection = "${SessionEntry.COLUMN_SESSION_ID} LIKE ?"
            selectionArgs = arrayOf(sessionId)
        }

        val sortOrder = "${BaseColumns._ID} DESC"

        val cursor = db.query(SessionEntry.TABLE, projection, selection, selectionArgs, null, null, sortOrder)

        val items = ArrayList<GpsSession>()

        with(cursor) {
            while (moveToNext()) {
                getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val name = getString(getColumnIndexOrThrow(SessionEntry.COLUMN_NAME))
                val description = getString(getColumnIndexOrThrow(SessionEntry.COLUMN_NAME))
                val recordedAt = getString(getColumnIndexOrThrow(SessionEntry.COLUMN_NAME))
                val duration = getInt(getColumnIndexOrThrow(SessionEntry.COLUMN_DURATION))
                val speed = getInt(getColumnIndexOrThrow(SessionEntry.COLUMN_SPEED))
                val distance = getInt(getColumnIndexOrThrow(SessionEntry.COLUMN_DISTANCE))
                val climb = getInt(getColumnIndexOrThrow(SessionEntry.COLUMN_CLIMB))
                val descent = getInt(getColumnIndexOrThrow(SessionEntry.COLUMN_DESCENT))
                val appUserId = getString(getColumnIndexOrThrow(SessionEntry.COLUMN_APP_USER_ID))
                val id = getString(getColumnIndexOrThrow(SessionEntry.COLUMN_SESSION_ID))

                items += GpsSession(
                    name,
                    description,
                    recordedAt,
                    duration,
                    speed,
                    distance,
                    climb,
                    descent,
                    appUserId,
                    id
                )
            }
        }

        return items
    }

    fun saveSession(gpsSession: GpsSession) {
        val db = databaseHelper.writableDatabase

        val values = ContentValues().apply {
            put(SessionEntry.COLUMN_NAME, gpsSession.name)
            put(SessionEntry.COLUMN_DESCRIPTION, gpsSession.description)
            put(SessionEntry.COLUMN_RECORDED_AT, gpsSession.recordedAt)
            put(SessionEntry.COLUMN_DURATION, gpsSession.duration)
            put(SessionEntry.COLUMN_SPEED, gpsSession.speed)
            put(SessionEntry.COLUMN_DISTANCE, gpsSession.distance)
            put(SessionEntry.COLUMN_CLIMB, gpsSession.climb)
            put(SessionEntry.COLUMN_DISTANCE, gpsSession.distance)
            put(SessionEntry.COLUMN_APP_USER_ID, gpsSession.appUserId)
            put(SessionEntry.COLUMN_SESSION_ID, gpsSession.id)
        }

        db.insert(SessionEntry.TABLE, null, values)
    }

    fun renameSession(sessionId: String, name: String) {
        val db = databaseHelper.writableDatabase

        val values = ContentValues().apply {
            put(SessionEntry.COLUMN_NAME, name)
        }

        val selection = "${SessionEntry.COLUMN_SESSION_ID} LIKE ?"
        val selectionArgs = arrayOf(sessionId)
        db.update(SessionEntry.TABLE, values, selection, selectionArgs)
    }

    fun removeSession(sessionId: String) {
        val db = databaseHelper.writableDatabase

        val selection = "${SessionEntry.COLUMN_SESSION_ID} LIKE ?"
        val selectionArgs = arrayOf(sessionId)
        db.delete(SessionEntry.TABLE, selection, selectionArgs)
    }

    fun updateSession(sessionId: String, duration: Int, speed: Int, distance: Int) {

        val db = databaseHelper.writableDatabase

        val values = ContentValues().apply {
            put(SessionEntry.COLUMN_DURATION, duration)
            put(SessionEntry.COLUMN_SPEED, speed)
            put(SessionEntry.COLUMN_DISTANCE, distance)
        }

        val selection = "${SessionEntry.COLUMN_SESSION_ID} LIKE ?"
        val selectionArgs = arrayOf(sessionId)
        db.update(SessionEntry.TABLE, values, selection, selectionArgs)
    }

    fun getLocations(sessionId: String) : List<GpsLocation> {

        val db = databaseHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            LocationEntry.COLUMN_RECORDED_AT,
            LocationEntry.COLUMN_LAT,
            LocationEntry.COLUMN_LON,
            LocationEntry.COLUMN_ACCURACY,
            LocationEntry.COLUMN_ALTITUDE,
            LocationEntry.COLUMN_VERTICAL_ACCURACY,
            LocationEntry.COLUMN_SESSION_ID,
            LocationEntry.COLUMN_LOCATION_TYPE
        )
        
        val selection = "${LocationEntry.COLUMN_SESSION_ID} LIKE ?"
        val selectionArgs = arrayOf(sessionId)
        val cursor = db.query(LocationEntry.TABLE, projection, selection, selectionArgs, null, null, null)

        val items = ArrayList<GpsLocation>()

        with(cursor) {
            while (moveToNext()) {
                getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val recordedAt = getString(getColumnIndexOrThrow(LocationEntry.COLUMN_RECORDED_AT))
                val latitude = getDouble(getColumnIndexOrThrow(LocationEntry.COLUMN_LAT))
                val longitude = getDouble(getColumnIndexOrThrow(LocationEntry.COLUMN_LON))
                val accuracy = getFloat(getColumnIndexOrThrow(LocationEntry.COLUMN_ACCURACY))
                val altitude = getDouble(getColumnIndexOrThrow(LocationEntry.COLUMN_ALTITUDE))
                val verticalAccuracy = getDoubleOrNull(getColumnIndexOrThrow(LocationEntry.COLUMN_VERTICAL_ACCURACY))
                val gpsSessionId = getString(getColumnIndexOrThrow(LocationEntry.COLUMN_SESSION_ID))
                val gpsLocationTypeId = getString(getColumnIndexOrThrow(LocationEntry.COLUMN_LOCATION_TYPE))

                items += GpsLocation(
                    recordedAt,
                    latitude,
                    longitude,
                    accuracy,
                    altitude,
                    verticalAccuracy,
                    gpsSessionId,
                    gpsLocationTypeId
                )
            }
        }

        return items
    }

    fun saveLocation(sessionId: String, location: GpsLocation) {
        val db = databaseHelper.writableDatabase

        val values = ContentValues().apply {
            put(LocationEntry.COLUMN_RECORDED_AT, location.recordedAt)
            put(LocationEntry.COLUMN_LAT, location.latitude)
            put(LocationEntry.COLUMN_LON, location.longitude)
            put(LocationEntry.COLUMN_ACCURACY, location.accuracy)
            put(LocationEntry.COLUMN_ALTITUDE, location.altitude)
            put(LocationEntry.COLUMN_VERTICAL_ACCURACY, location.verticalAccuracy)
            put(LocationEntry.COLUMN_SESSION_ID, sessionId)
            put(LocationEntry.COLUMN_LOCATION_TYPE, location.locationTypeId)
        }

        db.insert(LocationEntry.TABLE, null, values)
    }

    fun removeLocations(sessionId: String) {
        val db = databaseHelper.writableDatabase

        val selection = "${LocationEntry.COLUMN_SESSION_ID} LIKE ?"
        val selectionArgs = arrayOf(sessionId)
        db.delete(LocationEntry.TABLE, selection, selectionArgs)
    }
}