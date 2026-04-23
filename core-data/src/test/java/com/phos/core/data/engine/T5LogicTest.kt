package com.phos.core.data.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

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
        val targetWake = 5000000L // Larger diff for meaningful test
        val startDate = ZonedDateTime.now()
        
        // Max shift 2.0 hours per day (7,200,000 ms)
        val steps = jetLagManager.calculateTitrationSchedule(currentWake, targetWake, startDate, 2.0)
        
        assertEquals(1, steps.size)
        assertEquals(targetWake, steps[0].targetWakeTime)
    }
    
    @Test
    fun `test jet lag multi-day titration`() {
        val currentWake = 0L
        val targetWake = Duration.ofHours(6).toMillis() // 6 hours diff
        val startDate = ZonedDateTime.now()
        
        // Max shift 2 hours per day -> should take 3 days
        val steps = jetLagManager.calculateTitrationSchedule(currentWake, targetWake, startDate, 2.0)
        
        assertEquals(3, steps.size)
        assertEquals(Duration.ofHours(2).toMillis(), steps[0].targetWakeTime)
        assertEquals(Duration.ofHours(4).toMillis(), steps[1].targetWakeTime)
        assertEquals(Duration.ofHours(6).toMillis(), steps[2].targetWakeTime)
    }
}
