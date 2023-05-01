package com.neowise.tracko.view.sessions.viewer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.neowise.tracko.R
import com.neowise.tracko.constant.Const
import com.neowise.tracko.constant.Const.Companion.LOCATION_PERMISSIONS_REQUEST_CODE
import com.neowise.tracko.data.Database
import com.neowise.tracko.data.Preferences
import com.neowise.tracko.data.model.GpsLocation
import com.neowise.tracko.data.model.GpsSession
import com.neowise.tracko.util.MarkerFactory
import com.neowise.tracko.util.Utilities
import kotlinx.android.synthetic.main.activity_view_session.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException

class ViewSessionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var session: GpsSession
    private lateinit var locations: List<GpsLocation>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_session)

        val sessionId = intent.getStringExtra("session_id")!!
        session = Database.getInstance(applicationContext).getSessions(sessionId)[0]

        session_name.text = session.name
        distance_txt.text = Utilities.formatDistance(session.distance.toDouble())
        time_txt.text = Utilities.formatTime(session.duration.toLong())
        speed_txt.text = Utilities.getPace(session.duration.toLong(), session.distance.toFloat())

        export_gpx_btn.isEnabled = false

        export_gpx_btn.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                exportToGPX()
            }
            else {
                requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), LOCATION_PERMISSIONS_REQUEST_CODE)
            }
        }

        back_btn.setOnClickListener {
            finish()
        }

        initializeMap()
    }

    private fun exportToGPX() {

        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val fileName = "Session ${session.recordedAt}.gpx"

        val userEmail = Preferences.getInstance(this).getLogin().email
        val gpxContent = GPXConverter(
            userEmail,
            session,
            locations
        ).convert()

        try {
            FileWriter(File(dir, fileName)).use {
                    fileWriter -> fileWriter.append(gpxContent)
            }
            Toast.makeText(this, "GPX file saved in Documents folder", Toast.LENGTH_LONG).show()
        }
        catch (e: IOException) {
            Log.d("EXPORT_GPX", "$e")
            Toast.makeText(this, "Error on export to GPX", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        loadLocations()
    }

    private fun loadLocations() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                locations = Database.getInstance(applicationContext).getLocations(session.id)
                launch(Dispatchers.Main) {
                    buildTracking()
                }
            }
            catch(e: Exception) {
                Log.d("VIEW_SESSION", "Error load data -> $e")
            }
        }
    }

    private fun buildTracking() {

        val polylineOptions = PolylineOptions()
            .color(ContextCompat.getColor(this, R.color.trackingColor))

        val checkPointMarks = ArrayList<MarkerOptions>()

        for (location in locations) {
            val position = LatLng(location.latitude, location.longitude)
            Log.d("SESSION_VIEW", "location = ${location.locationTypeId}")

            if (location.locationTypeId == Const.LOCATION_TYPE_LOC) {
                polylineOptions.add(position)
                Log.d("SESSION_VIEW", "add locate")
            }

            if (location.locationTypeId == Const.LOCATION_TYPE_CP) {
                val marker = MarkerFactory.checkpointMarker()
                                .title("Checkpoint #${checkPointMarks.size + 1}")
                                .position(position)

                checkPointMarks.add(marker)
                Log.d("SESSION_VIEW", "add checkpoint")
            }
        }

        googleMap.addPolyline(polylineOptions)

        for(mark in checkPointMarks) {
            googleMap.addMarker(mark)
        }

        if (locations.isNotEmpty()) {

            val position = LatLng(locations[0].latitude, locations[0].longitude)

            val cameraPosition = CameraPosition.Builder().target(position).zoom(17f).build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            Log.d("SESSION_VIEW", "add start position")
        }

        export_gpx_btn.isEnabled = true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.isNotEmpty()) {
            if (requestCode == Const.STORAGE_PERMISSIONS_REQUEST_CODE) {
                if (permissions[0] == WRITE_EXTERNAL_STORAGE) {
                    exportToGPX()
                }
            }
        }
    }
}