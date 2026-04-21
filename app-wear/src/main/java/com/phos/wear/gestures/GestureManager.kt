package com.phos.wear.gestures

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

enum class GestureType {
    WRIST_FLICK, // Log dose
    WRIST_SHAKE  // Snooze alert
}

class GestureManager(
    private val context: Context,
    private val onGestureDetected: (GestureType) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val FLICK_THRESHOLD = 15f
    private val SHAKE_THRESHOLD = 12f
    private val SHAKE_WINDOW_MS = 500L
    
    private var lastShakeTime: Long = 0
    private var shakeCount = 0

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Detect Wrist Flick (Spike in Z-axis when moving hand to mouth or away)
        if (abs(z) > FLICK_THRESHOLD) {
            onGestureDetected(GestureType.WRIST_FLICK)
        }

        // Detect Wrist Shake (Rapid oscillation in X-axis)
        if (abs(x) > SHAKE_THRESHOLD) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTime < SHAKE_WINDOW_MS) {
                shakeCount++
                if (shakeCount > 5) {
                    onGestureDetected(GestureType.WRIST_SHAKE)
                    shakeCount = 0
                }
            } else {
                shakeCount = 1
            }
            lastShakeTime = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
