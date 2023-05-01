package com.neowise.tracko.view.map

import android.Manifest.permission
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Sensor.TYPE_ORIENTATION
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.neowise.tracko.R
import com.neowise.tracko.constant.Action
import com.neowise.tracko.constant.Const
import com.neowise.tracko.constant.Const.Companion.NOTIFICATION_CHANNEL
import com.neowise.tracko.util.Utilities
import com.neowise.tracko.view.GlobalOptions
import com.neowise.tracko.view.sessions.SessionsActivity
import com.neowise.tracko.view.map.service.LocationService
import kotlinx.android.synthetic.main.activity_main.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, Compass.DirectionChangeListener {

    private var trackPolyline: Polyline? = null
    private var wpPolyline: Polyline? = null
    private var wpMarker: Marker? = null
    private var cpMarkers = mutableListOf<Marker>()

    private var permissionGranted: Boolean = false

    private lateinit var googleMap: GoogleMap

    private val broadcastReceiver = LocationBroadcastReceiver()
    private val broadcastFilter = IntentFilter()

    private lateinit var sensorManager: SensorManager
    private lateinit var compass: Compass

    private var currentPosition: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start_btn.setOnClickListener {
            if (GlobalOptions.isLocationServiceAlive) {
                showStopServiceConfirmDialog()
            }
            else {
                GlobalOptions.clearAll()
                googleMap.clear()
                startService()
            }
        }

        checkpoint_btn.setOnClickListener {
            sendBroadcast(Intent(Action.NOTIFICATION_CP))
        }

        waypoint_btn.setOnClickListener {
            sendBroadcast(Intent(Action.NOTIFICATION_WP))
        }

        last_sessions_btn.setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }

        updateButtonsState(GlobalOptions.isLocationServiceAlive)
        updateStartButtonState()
        initializeNotificationChannel()
        initializeBroadcast()
        initializeCompass()
        initializeMap()
    }

    private fun showStopServiceConfirmDialog() {
        AlertDialog.Builder(this)
                .setTitle("Stop session")
                .setMessage("Do you want to end session?")
                .setPositiveButton("Stop") { _, _ ->
                    stopService()
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    private fun updateStartButtonState() {
        if (GlobalOptions.isLocationServiceAlive) {
            setButtonState(R.string.stop, R.drawable.ic_stop)
        }
        else {
            setButtonState(R.string.start, R.drawable.ic_run)
        }
    }

    private fun updateButtonsState(isEnable: Boolean) {
        checkpoint_btn.isEnabled = isEnable
        waypoint_btn.isEnabled = isEnable
    }

    private fun setButtonState(textId: Int, icon: Int) {
        start_btn.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, icon), null, null, null)
        start_btn.text = getString(textId)
    }

    private fun initializeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL, "Default channel", IMPORTANCE_LOW)
            channel.description = "Default channel"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeBroadcast() {
        broadcastFilter.addAction(Action.LOCATION_UPDATE)
        broadcastFilter.addAction(Action.NOTIFICATION_START_STOP)
        broadcastFilter.addAction(Action.MAP_UPDATE)
    }

    // ------------------------- Initializes ---------------------------- //

    private fun initializeCompass() {
        compass = Compass(compass_img)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initializeMapControlButtons() {

        // Add listener for checking updates
        rot_group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (checkedId == R.id.rot_north_up) {

                GlobalOptions.isDirectNorthUp = isChecked

                // Disable rotation
                googleMap.uiSettings.isRotateGesturesEnabled = !isChecked

                if (isChecked) {
                    changeMapDirection(0f)
                    // Disable compass rotation
                    compass.removeDirectionChangeListener()
                }
            }
            if (checkedId == R.id.rot_compass) {
                GlobalOptions.isDirectCompass = isChecked
                // Disable rotation
                googleMap.uiSettings.isRotateGesturesEnabled = !isChecked
                if (isChecked) {
                    // enable compass rotation
                    compass.setOnDirectionChange(this)
                }
            }
            if (checkedId == R.id.rot_default) {

                if (isChecked) {
                    GlobalOptions.isDirectCompass = false
                    GlobalOptions.isDirectNorthUp = false
                    GlobalOptions.isDirectDefault = true
                    // enable rotation
                    googleMap.uiSettings.isRotateGesturesEnabled = true
                    // disable compass rotation
                    compass.removeDirectionChangeListener()
                }
            }
        }

        pos_group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (checkedId == R.id.pos_location) {
                GlobalOptions.isTrackUserLocation = isChecked
            }
        }
    }

    // ------------------------- Service ---------------------------- //

    private fun startService() {
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(Intent(this, LocationService::class.java))
        } else {
            startService(Intent(this, LocationService::class.java))
        }
        setButtonState(R.string.stop, R.drawable.ic_stop)
        updateButtonsState(true)
    }

    private fun stopService() {
        // stopping the service
        stopService(Intent(this, LocationService::class.java))
        setButtonState(R.string.start, R.drawable.ic_run)
        updateButtonsState(false)
    }

    // ------------------------- Map Options  ---------------------------- //

    private fun changeMapDirection(degree: Float) {
        googleMap.cameraPosition

        Toast.makeText(this, "direction changed", Toast.LENGTH_SHORT).show()

        val cameraPosition = CameraPosition.Builder()
                .target(googleMap.cameraPosition.target)
                .tilt(googleMap.cameraPosition.tilt)
                .zoom(googleMap.cameraPosition.zoom)
                .bearing(degree)
                .build()

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.setOnMapLongClickListener(this)

        // hide compass
        this.googleMap.uiSettings.isCompassEnabled = false

        if (checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ||
            checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
            this.googleMap.isMyLocationEnabled = true
        }
        else {
            requestLocationPermissions()
        }

        initializeMapControlButtons()

        // First initialize toggle states
        if (GlobalOptions.isDirectDefault) {
            rot_group.check(R.id.rot_default)
        }
        if (GlobalOptions.isDirectNorthUp) {
            rot_group.check(R.id.rot_north_up)
        }

        if (GlobalOptions.isDirectCompass) {
            rot_group.check(R.id.rot_compass)
        }

        if (GlobalOptions.isTrackUserLocation) {
            pos_group.check(R.id.pos_location)
        }
    }

    override fun onMapLongClick(location: LatLng) {

        val intent = Intent(Action.NOTIFICATION_WP)
        intent.putExtra(Action.LOCATION_UPDATE_LAT, location.latitude)
        intent.putExtra(Action.LOCATION_UPDATE_LON, location.longitude)

        sendBroadcast(intent)
    }

    override fun onDirectionChange(degree: Float) {
        changeMapDirection(degree)
    }

    private fun updateMap(lat: Double, lon: Double) {
        updateMapVisual()
        currentPosition = LatLng(lat, lon)
        Log.d("MAP", "update")

        if (GlobalOptions.isTrackUserLocation) {
            Log.d("MAP", "Tracking user location")
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentPosition!!))
        }
    }

    private fun updateMapVisual() {
        // clear map old data
        trackPolyline?.remove()
        wpPolyline?.remove()
        wpMarker?.remove()
        cpMarkers.forEach { it.remove() }
        cpMarkers.clear()

        // add map new data
        val trackColor = ContextCompat.getColor(this, R.color.trackingColor)
        val waypointColor = ContextCompat.getColor(this, R.color.waypointColor)

        trackPolyline = googleMap.addPolyline(GlobalOptions.trackingPolyline.color(trackColor))
        wpPolyline = googleMap.addPolyline(GlobalOptions.waypointPolyline.color(waypointColor))

        for (checkpoint in GlobalOptions.checkpointMarkers) {
            cpMarkers.add(googleMap.addMarker(checkpoint)!!)
        }
        wpMarker = googleMap.addMarker(GlobalOptions.waypointMarker)
    }

    private fun updateInformation(overall: Distance, checkpoint: Distance, waypoint: Distance) {

        start_distance_txt.text = Utilities.formatDistance(overall.total.toDouble())
        start_duration_txt.text = Utilities.formatTime(overall.time)
        start_speed_txt.text = Utilities.getPace(overall.time, overall.total)

        cp_total_txt.text = Utilities.formatDistance(checkpoint.total.toDouble())
        cp_direct_txt.text = Utilities.formatDistance(checkpoint.direct.toDouble())
        cp_speed_txt.text = Utilities.getPace(checkpoint.time, checkpoint.total)

        wp_total_txt.text = Utilities.formatDistance(waypoint.total.toDouble())
        wp_direct_txt.text = Utilities.formatDistance(waypoint.direct.toDouble())
        wp_speed_txt.text = Utilities.getPace(waypoint.time, waypoint.total)
    }

    // ------------------------- Permissions ---------------------------- //

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_FINE_LOCATION), Const.LOCATION_PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Const.LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (permissions[0] == permission.ACCESS_FINE_LOCATION) {
                permissionGranted = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(compass, sensorManager.getDefaultSensor(TYPE_ORIENTATION), SENSOR_DELAY_GAME)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, broadcastFilter)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(compass)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    // ------------------------- Broadcast Receiver ---------------------------- //

    inner class LocationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Action.LOCATION_UPDATE -> {

                    val lat = intent.getDoubleExtra(Action.LOCATION_UPDATE_LAT, 0.0)
                    val lon = intent.getDoubleExtra(Action.LOCATION_UPDATE_LON, 0.0)

                    val overallDist = Gson().fromJson(intent.getStringExtra(Action.LOCATION_UPDATE_OVERALL), Distance::class.java)
                    val checkpointDist = Gson().fromJson(intent.getStringExtra(Action.LOCATION_UPDATE_CP), Distance::class.java)
                    val waypointDist = Gson().fromJson(intent.getStringExtra(Action.LOCATION_UPDATE_WP), Distance::class.java)
                    
                    updateInformation(overallDist, checkpointDist, waypointDist)
                    updateMap(lat, lon)
                }

                Action.NOTIFICATION_START_STOP -> {
                    showStopServiceConfirmDialog()
                }
                Action.MAP_UPDATE -> {
                    updateMapVisual()
                }
            }
        }
    }
}

