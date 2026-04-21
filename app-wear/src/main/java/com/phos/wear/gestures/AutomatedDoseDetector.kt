package com.phos.wear.gestures

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Advanced sensor fusion for hand-to-mouth gesture detection.
 * Extends the basic GestureManager for Track 10.
 */
class AutomatedDoseDetector(
    private val context: Context,
    private val onDoseDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Heuristic thresholds for "Hand-to-Mouth" motion
    private val ELEVATION_THRESHOLD = 7.0f // Significant Z-axis change
    private val ROTATION_THRESHOLD = 2.0f // Pitch rotation
    private val TIME_WINDOW_MS = 2000L
    
    private var lastElevationTime: Long = 0
    private var isElevating = false

    fun start() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val z = event.values[2]
                if (z > ELEVATION_THRESHOLD) {
                    isElevating = true
                    lastElevationTime = System.currentTimeMillis()
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val pitch = event.values[0]
                val currentTime = System.currentTimeMillis()
                
                // If we detected elevation recently and now see rotation (drinking/pill motion)
                if (isElevating && abs(pitch) > ROTATION_THRESHOLD && (currentTime - lastElevationTime) < TIME_WINDOW_MS) {
                    onDoseDetected()
                    isElevating = false // Reset
                }
            }
        }
        
        // Timeout elevation
        if (isElevating && (System.currentTimeMillis() - lastElevationTime) > TIME_WINDOW_MS) {
            isElevating = false
        }
    }

    private fun abs(value: Float) = if (value < 0) -value else value

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
