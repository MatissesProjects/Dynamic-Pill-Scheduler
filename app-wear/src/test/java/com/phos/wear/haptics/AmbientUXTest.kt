package com.phos.wear.haptics

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class AmbientUXTest {

    private lateinit var context: Context
    private lateinit var vibrator: Vibrator
    private lateinit var hapticVocabulary: HapticVocabulary

    @Before
    fun setup() {
        context = mock()
        vibrator = mock()
        
        // Mocking VibratorManager for SDK >= S
        val vibratorManager: VibratorManager = mock()
        whenever(context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)).thenReturn(vibratorManager)
        whenever(vibratorManager.defaultVibrator).thenReturn(vibrator)
        
        // Mocking direct Vibrator for SDK < S
        whenever(context.getSystemService(Context.VIBRATOR_SERVICE)).thenReturn(vibrator)

        hapticVocabulary = HapticVocabulary(context)
    }

    @Test
    fun `test playDoseDue triggers vibration`() {
        hapticVocabulary.playDoseDue()
        verify(vibrator).vibrate(anyOrNull<VibrationEffect>())
    }

    @Test
    fun `test playCollisionWarning triggers vibration`() {
        hapticVocabulary.playCollisionWarning()
        verify(vibrator).vibrate(anyOrNull<VibrationEffect>())
    }

    @Test
    fun `test playSyncSuccess triggers vibration`() {
        hapticVocabulary.playSyncSuccess()
        verify(vibrator).vibrate(anyOrNull<VibrationEffect>())
    }
}
