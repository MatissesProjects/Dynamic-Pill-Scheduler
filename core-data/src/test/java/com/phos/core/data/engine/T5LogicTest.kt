package com.phos.core.data.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class T5LogicTest {

    private val suppressor = PhysiologicalSuppressor()
    private val jetLagManager = JetLagManager()

    @Test
    fun `test physiological suppression with high HR`() {
        val result = suppressor.shouldSuppressNotification(120, "Metoprolol")
        assertTrue(result.isSuppressed)
        assertTrue(result.reason?.contains("High Heart Rate") == true)
        assertEquals(15, result.suggestedSnoozeMinutes)
    }

    @Test
    fun `test physiological suppression with normal HR`() {
        val result = suppressor.shouldSuppressNotification(70, "Metoprolol")
        assertTrue(!result.isSuppressed)
    }

    @Test
    fun `test beta blocker specific suppression`() {
        val result = suppressor.shouldSuppressWithContext(95, "beta_blocker_1", "Metoprolol")
        assertTrue(result.isSuppressed)
        assertTrue(result.reason?.contains("Beta Blocker") == true)
        assertEquals(20, result.suggestedSnoozeMinutes)
    }

    @Test
    fun `test jet lag titration schedule generation`() {
        val currentWake = 1000L
        val targetWake = 5000L // 4000ms diff
        // Max shift 2000ms per day (simulated)
        val steps = jetLagManager.calculateTitrationSchedule(currentWake, targetWake, 2)
        
        // Duration of 2 hours is 7,200,000 ms. 
        // 4000 ms diff / 7.2m ms = 1 step.
        // Wait, my titration schedule logic uses L diff.
        
        assertEquals(1, steps.size)
        assertEquals(5000L, steps[0].wakeTimeShiftMillis)
    }
    
    @Test
    fun `test jet lag multi-day titration`() {
        val currentWake = 0L
        val targetWake = Duration.ofHours(6).toMillis() // 6 hours diff
        // Max shift 2 hours per day -> should take 3 days
        val steps = jetLagManager.calculateTitrationSchedule(currentWake, targetWake, 2)
        
        assertEquals(3, steps.size)
        assertEquals(Duration.ofHours(2).toMillis(), steps[0].wakeTimeShiftMillis)
        assertEquals(Duration.ofHours(4).toMillis(), steps[1].wakeTimeShiftMillis)
        assertEquals(Duration.ofHours(6).toMillis(), steps[2].wakeTimeShiftMillis)
    }
}
