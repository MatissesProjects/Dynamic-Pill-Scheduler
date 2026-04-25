package com.phos.core.data.engine

import org.junit.Assert.*
import org.junit.Test

class PhysiologicalSuppressorTest {

    private val suppressor = PhysiologicalSuppressor()

    @Test
    fun `test basic HR suppression for non-critical meds`() {
        val result = suppressor.shouldSuppressNotification(110, "Vitamin D")
        assertTrue(result.isSuppressed)
        assertEquals(15, result.suggestedSnoozeMinutes)
        assertTrue(result.reason?.contains("High Heart Rate") == true)
    }

    @Test
    fun `test normal HR does not suppress`() {
        val result = suppressor.shouldSuppressNotification(75, "Vitamin D")
        assertFalse(result.isSuppressed)
    }

    @Test
    fun `test critical medication bypasses normal suppression`() {
        val result = suppressor.shouldSuppressNotification(110, "Heart Med", isCriticalMed = true)
        assertFalse(result.isSuppressed)
        assertTrue(result.isCritical)
    }

    @Test
    fun `test critical medication suppressed at extreme HR`() {
        val result = suppressor.shouldSuppressNotification(150, "Heart Med", isCriticalMed = true)
        // Note: The logic in class says "if (isCriticalMed && currentHeartRate < 140) return false"
        // Otherwise it proceeds to the > 100 check.
        assertTrue(result.isSuppressed)
    }

    @Test
    fun `test fuzzy window optimization for stress`() {
        // High HR (95) and Low HRV (20.0) -> Stressed
        val result = suppressor.getFuzzyWindowOptimization(95, 20.0, "Supplements")
        assertTrue(result.isSuppressed)
        assertEquals(30, result.suggestedSnoozeMinutes)
        assertTrue(result.reason?.contains("Elevated physiological stress") == true)
    }

    @Test
    fun `test fuzzy window not optimized when not stressed`() {
        val result = suppressor.getFuzzyWindowOptimization(70, 60.0, "Supplements")
        assertFalse(result.isSuppressed)
    }

    @Test
    fun `test beta blocker specific suppression`() {
        val result = suppressor.shouldSuppressWithContext(95, "beta_blocker_abc", "Metoprolol")
        assertTrue(result.isSuppressed)
        assertTrue(result.reason?.contains("Beta Blocker") == true)
        assertEquals(20, result.suggestedSnoozeMinutes)
    }
}
