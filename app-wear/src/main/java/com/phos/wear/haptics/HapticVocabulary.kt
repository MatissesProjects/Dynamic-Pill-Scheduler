package com.phos.wear.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * PHOS Haptic Vocabulary for Pixel Watch 3.
 * Defines unique vibration patterns for context-aware feedback.
 */
class HapticVocabulary(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * "Dose Due" Pattern: A distinct double-pulse.
     */
    fun playDoseDue() {
        val timings = longArrayOf(0, 150, 100, 150) // Wait, Pulse, Gap, Pulse
        val amplitudes = intArrayOf(0, 255, 0, 255)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }

    /**
     * "Collision Warning" Pattern: A long, low-frequency rumble.
     */
    fun playCollisionWarning() {
        val timings = longArrayOf(0, 800) // 800ms rumble
        val amplitudes = intArrayOf(0, 100) // Lower intensity for rumble feel
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }

    /**
     * "Sync Successful" Pattern: A light, confirming tick.
     */
    fun playSyncSuccess() {
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        } else {
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    }

    /**
     * "Thermal Alert" Pattern: A pulsating, "heat-like" rhythm.
     * Three pulses with increasing duration and intensity.
     */
    fun playThermalAlert() {
        val timings = longArrayOf(0, 200, 100, 400, 100, 600)
        val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }
}
