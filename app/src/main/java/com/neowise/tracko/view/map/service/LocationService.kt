package com.neowise.tracko.view.map.service

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.neowise.tracko.R
import com.neowise.tracko.constant.Action
import com.neowise.tracko.constant.Const
import com.neowise.tracko.data.Database
import com.neowise.tracko.data.Preferences
import com.neowise.tracko.data.RemoteApi
import com.neowise.tracko.view.map.Distance
import com.neowise.tracko.data.model.GpsLocation
import com.neowise.tracko.data.model.GpsSession
import com.neowise.tracko.data.model.SessionParams
import com.neowise.tracko.util.Utilities
import com.neowise.tracko.view.GlobalOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class LocationService : Service() {

    companion object {
        private const val UPDATE_INTERVAL = 2000L
        private const val FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2
    }

    private lateinit var remoteApi: RemoteApi
    private lateinit var database: Database

    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private val overallDistance = Distance()
    private val waypointDistance = Distance()
    private val checkpointDistance = Distance()

    private var currentLocation: Location? = null

    private var startLocation: Location? = null
    private var checkpointLocation: Location? = null
    private var waypointLocation: Location? = null

    private var token : String? = null
    private var currentSession : GpsSession? = null

    private var isWaypointSetted = false

    override fun onCreate() {
        super.onCreate()

        GlobalOptions.isLocationServiceAlive = true

        remoteApi = RemoteApi.getInstance(applicationContext)
        database = Database.getInstance(applicationContext)

        initializeBroadcastReceiver()
        initializeLocationUpdates()

        authorization()
    }

    // ------------------------- Initializations ---------------------------- //

    private fun initializeBroadcastReceiver() {
        broadcastReceiver = LocationBroadcastReceiver()

        val filter = IntentFilter()
        filter.apply {
            addAction(Action.NOTIFICATION_CP)
            addAction(Action.NOTIFICATION_WP)
            addAction(Action.NOTIFICATION_START_STOP)
        }

        registerReceiver(broadcastReceiver, filter)
    }

    private fun initializeLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                updateLocation(result.lastLocation!!)
            }
        }

        if (checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
            checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    updateLocation(task.result!!)
                }
            }

            locationRequest = LocationRequest()
            locationRequest.interval = UPDATE_INTERVAL
            locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.maxWaitTime = UPDATE_INTERVAL

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun updateLocation(location: Location) {
//        if (location.accuracy > 100) {
//            return
//        }
        if (currentLocation == null) {
            startLocation = location
            checkpointLocation = location
            waypointLocation = location
        } else {
            overallDistance.update(location, currentLocation!!, startLocation!!)
            checkpointDistance.update(location, currentLocation!!, checkpointLocation!!)
            waypointDistance.update(location, currentLocation!!, waypointLocation!!)
        }

        currentLocation = location

        addLocation(location)

        if (isWaypointSetted) {
            GlobalOptions.updateWaypoint(currentLocation!!, waypointLocation!!)
        }

        sendUpdateLocationBroadcast(location)
        sendLocation(location, Const.LOCATION_TYPE_LOC)

        showNotification()
    }

    // ------------------------- Map Updates ---------------------------- //

    private fun setCheckpoint() {
        // set checkpoint location
        checkpointLocation = currentLocation
        // clear checkpoint data in info table
        checkpointDistance.clear()

        // send cp location to api and db
        sendLocation(checkpointLocation!!, Const.LOCATION_TYPE_CP)
        // add checkpoint to map
        GlobalOptions.addCheckPoint(currentLocation!!.latitude, currentLocation!!.longitude)
        // call map visual update
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Action.MAP_UPDATE))
    }

    private fun setWaypoint(lat: Double, lon: Double) {
        // enable waypoint visible
        isWaypointSetted = true

        // set waypoint location
        waypointLocation = Location(currentLocation!!)
        waypointLocation!!.latitude = lat
        waypointLocation!!.longitude = lon

        // send cp location to api and db
        sendLocation(waypointLocation!!, Const.LOCATION_TYPE_WP)

        // change waypoint location on map
        GlobalOptions.updateWaypoint(currentLocation!!, waypointLocation!!)
        // call map visual update
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Action.MAP_UPDATE))
    }

    private fun removeWaypoint() {
        isWaypointSetted = false

        waypointLocation = currentLocation
        waypointDistance.clear()
        GlobalOptions.clearWaypoint()
    }

    private fun addLocation(location: Location) {
        GlobalOptions.addTrackingPoint(location)
    }

    // ------------------------- Broadcast ---------------------------- //

    private fun sendUpdateLocationBroadcast(location: Location) {
        val intent = Intent(Action.LOCATION_UPDATE)

        intent.putExtra(Action.LOCATION_UPDATE_LAT, location.latitude)
        intent.putExtra(Action.LOCATION_UPDATE_LON, location.longitude)

        intent.putExtra(Action.LOCATION_UPDATE_CP, checkpointDistance.json())
        intent.putExtra(Action.LOCATION_UPDATE_WP, waypointDistance.json())
        intent.putExtra(Action.LOCATION_UPDATE_OVERALL, overallDistance.json())

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendServiceStopBroadcast() {
        val intent = Intent(Action.NOTIFICATION_START_STOP)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun showNotification() {

        val notify = RemoteViews(packageName, R.layout.layout_notification)
        val notifyExpanded = RemoteViews(packageName, R.layout.layout_notification_big)

        val pendingIntentStartStop = PendingIntent.getBroadcast(this, 0, Intent(Action.NOTIFICATION_START_STOP), 0)
        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, Intent(Action.NOTIFICATION_CP), 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, Intent(Action.NOTIFICATION_WP), 0)

        notifyExpanded.setOnClickPendingIntent(R.id.notify_stop_btn, pendingIntentStartStop)
        notifyExpanded.setOnClickPendingIntent(R.id.notify_checkpoint_btn, pendingIntentCp)
        notifyExpanded.setOnClickPendingIntent(R.id.waypoint_btn, pendingIntentWp)

        notify.setTextViewText(R.id.start_distance_txt, "Distance: " + Utilities.formatDistance(overallDistance.total.toDouble()))
        notify.setTextViewText(R.id.start_duration_txt, "Time: " + Utilities.formatTime(overallDistance.time))
        notify.setTextViewText(R.id.start_speed_txt, "Speed: " + Utilities.getPace(overallDistance.time, overallDistance.total))

        notifyExpanded.setTextViewText(R.id.start_distance_txt, Utilities.formatDistance(overallDistance.total.toDouble()))
        notifyExpanded.setTextViewText(R.id.start_duration_txt, Utilities.formatTime(overallDistance.time))
        notifyExpanded.setTextViewText(R.id.start_speed_txt, Utilities.getPace(overallDistance.time, overallDistance.total))

        notifyExpanded.setTextViewText(R.id.cp_total_txt, Utilities.formatDistance(checkpointDistance.total.toDouble()))
        notifyExpanded.setTextViewText(R.id.cp_direct_txt, Utilities.formatDistance(checkpointDistance.direct.toDouble()))
        notifyExpanded.setTextViewText(R.id.cp_speed_txt, Utilities.getPace(checkpointDistance.time, checkpointDistance.total))

        notifyExpanded.setTextViewText(R.id.wp_total_txt, Utilities.formatDistance(waypointDistance.total.toDouble()))
        notifyExpanded.setTextViewText(R.id.wp_direct_txt, Utilities.formatDistance(waypointDistance.direct.toDouble()))
        notifyExpanded.setTextViewText(R.id.wp_speed_txt, Utilities.getPace(waypointDistance.time, waypointDistance.total))

        val notification = NotificationCompat.Builder(applicationContext, Const.NOTIFICATION_CHANNEL)
            .setCustomContentView(notify)
            .setCustomBigContentView(notifyExpanded)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_runner_20dp)
            .setOngoing(true)
            .build()

        startForeground(Const.NOTIFICATION_ID, notification)
    }

    private fun showServiceStopConfirmation() {
        val dialog = AlertDialog.Builder(this)
                .setTitle("Stop session")
                .setMessage("Do you want to end session?")
                .setPositiveButton("Stop") { _, _ ->
                    sendServiceStopBroadcast()
                }
                .setNegativeButton("Cancel", null)
                .create()
        dialog.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        dialog.show()
    }

    // ------------------------- API Requests ---------------------------- //

    private fun authorization() {

        val login = Preferences.getInstance(this).getLogin()

        GlobalScope.launch(Dispatchers.IO) {
            token = remoteApi.login(login).token
            startSession()
        }
    }

    private fun startSession() {
        if (token == null) return

        GlobalScope.launch(Dispatchers.IO) {

            val params = SessionParams(
                name = Date().toString(),
                description = Date().toString(),
                minSpeed = 6*60,
                maxSpeed = 18*60
            )

            currentSession = remoteApi.startSession(token!!, params)
            database.saveSession(currentSession!!)
        }
    }

    private fun updateSession() {

        currentSession?.let {session ->
            GlobalScope.launch(Dispatchers.IO) {
                val sessionId = session.id
                val duration = overallDistance.time.toInt()
                val speed = (overallDistance.time / 60.0 / overallDistance.total).toInt()
                val distance = overallDistance.total.toInt()

                database.updateSession(sessionId, duration, speed, distance)
            }
        }
    }

    private fun sendLocation(location: Location, locationTypeId: String) {

        if (token == null && currentSession == null) return

        val recordedAt = Date().toString()
        val sessionId = currentSession!!.id
        val gpsLocation = GpsLocation(location, recordedAt, sessionId, locationTypeId)

        GlobalScope.launch(Dispatchers.IO) {

            if (locationTypeId != Const.LOCATION_TYPE_WP) {
                database.saveLocation(currentSession!!.id, gpsLocation)
            }

            remoteApi.sendLocation(token!!, gpsLocation)
        }
    }

    // ------------------------- Override Functions ---------------------------- //

    override fun onDestroy() {
        super.onDestroy()

        if(currentSession != null) {
            updateSession()
        }

        GlobalOptions.isLocationServiceAlive = false

        fusedLocationClient.removeLocationUpdates(locationCallback)
        NotificationManagerCompat.from(this).cancelAll()
        unregisterReceiver(broadcastReceiver)

        val intent = Intent(Action.LOCATION_UPDATE_STOP)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentLocation = null
        startLocation = null
        waypointLocation = null
        checkpointLocation = null

        overallDistance.direct = 0f
        overallDistance.total = 0f
        checkpointDistance.direct = 0f
        checkpointDistance.total = 0f
        waypointDistance.direct = 0f
        waypointDistance.total = 0f

        showNotification()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    // ------------------------- Broadcast Receiver ---------------------------- //

    inner class LocationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {
                Action.NOTIFICATION_WP -> {

                    if (currentLocation == null) return

                    // get waypoint location
                    val lat = intent.getDoubleExtra(Action.LOCATION_UPDATE_LAT, currentLocation!!.latitude)
                    val lon = intent.getDoubleExtra(Action.LOCATION_UPDATE_LON, currentLocation!!.longitude)
                    // set waypoint to map
                    setWaypoint(lat, lon)
                    // update info in notification
                    showNotification()
                }
                Action.NOTIFICATION_CP -> {

                    if (currentLocation == null) return
                    // remove waypoint from map
                    removeWaypoint()
                    // add checkpoint to map
                    setCheckpoint()
                    // update info in notification
                    showNotification()
                }
                Action.NOTIFICATION_START_STOP -> {
                    sendServiceStopBroadcast()
                }
            }
        }
    }
}