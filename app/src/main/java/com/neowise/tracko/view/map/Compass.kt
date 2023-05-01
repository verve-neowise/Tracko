package com.neowise.tracko.view.map

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import android.view.View
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.TextView
import kotlin.math.roundToLong

class Compass(private val compassView: View, private val orientText: TextView? = null) : SensorEventListener {

    private var listener: DirectionChangeListener? = null
    private var currentDegree = 0.0f
    private var lastUpdate = System.currentTimeMillis()

    override fun onSensorChanged(event: SensorEvent) {

        val actualTime = System.currentTimeMillis()

        if (actualTime - lastUpdate < 2000) {
            Log.d("UPDATE_SENSOR", "update")
            return
        }

        val degree = event.values[0].roundToLong().toFloat()

        val rotateAnimation = RotateAnimation(currentDegree, -degree, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)

        rotateAnimation.duration = 200
        rotateAnimation.fillAfter = true

        orientText?.text = "$degree"
        compassView.startAnimation(rotateAnimation)

        currentDegree = -degree
        this.listener?.onDirectionChange(degree)
    }

    fun setOnDirectionChange(listener: DirectionChangeListener) {
        this.listener = listener
    }

    fun removeDirectionChangeListener() {
        this.listener = null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    interface DirectionChangeListener {
        fun onDirectionChange(degree: Float)
    }
 }