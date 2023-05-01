package com.neowise.tracko.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.neowise.tracko.data.database.DatabaseContract.CREATE_LOCATION_ENTRIES
import com.neowise.tracko.data.database.DatabaseContract.CREATE_SESSION_ENTRIES

class DatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_SESSION_ENTRIES)
        db.execSQL(CREATE_LOCATION_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DatabaseContract.DELETE_SESSION_ENTRIES)
        db.execSQL(DatabaseContract.DELETE_LOCATION_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DatabaseContract.DELETE_SESSION_ENTRIES)
        db.execSQL(DatabaseContract.DELETE_LOCATION_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "RunnerMap.db"
    }
}