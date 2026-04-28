package com.phos.core.data.engine

import org.junit.Assert.*
import org.junit.Test

class CardioMismatchEngineTest {

    private val engine = CardioMismatchEngine()

    @Test
    fun `test detectMismatch triggers on blunted HR with high step rate`() {
        val rhr = 60.0
        val stepRate = 130.0 // Fast walking/running
        val heartRate = 70.0 // Blunted response (should be at least 80)
        
        val result = engine.detectMismatch(System.currentTimeMillis(), stepRate, heartRate, rhr)
        
        assertTrue(result.isSignificant)
        assertTrue(result.mismatchIntensity > 0.5)
    }

    @Test
    fun `test detectMismatch does not trigger on normal HR response`() {
        val rhr = 60.0
        val stepRate = 130.0
        val heartRate = 100.0 // Normal rise
        
        val result = engine.detectMismatch(System.currentTimeMillis(), stepRate, heartRate, rhr)
        
        assertFalse(result.isSignificant)
    }

    @Test
    fun `test calculateReadiness yields correct score levels`() {
        // Perfect recovery
        val optimal = engine.calculateReadiness(50.0, 50.0, 60.0, 60.0, 10)
        assertTrue(optimal.score >= 90)
        
        // Strained recovery
        val strained = engine.calculateReadiness(30.0, 50.0, 75.0, 60.0, 4)
        assertEquals(60, strained.score)
    }
}
