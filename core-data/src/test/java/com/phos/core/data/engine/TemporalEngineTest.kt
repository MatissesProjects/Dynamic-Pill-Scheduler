package com.phos.core.data.engine

import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class TemporalEngineTest {

    private val engine = TemporalEngine()

    @Test
    fun testCalculateScheduledTime() {
        val anchor = TemporalAnchor("2026-04-20", 1713596400000L, "Manual") // 7:00 AM
        val medication = MedicationRecord(
            medicationId = "test",
            name = "Test Med",
            dosage = "10mg",
            frequencyOffset = 2 * 60 * 60 * 1000L, // 2 hours
            validFrom = 0L
        )

        val scheduledTime = engine.calculateScheduledTime(medication, anchor)
        assertEquals(1713603600000L, scheduledTime) // 9:00 AM
    }

    @Test
    fun testFuzzyWindow() {
        val scheduledTime = 1000L
        assertTrue(engine.isWithinFuzzyWindow(scheduledTime, 1000L))
        assertTrue(engine.isWithinFuzzyWindow(scheduledTime, 1500L, 1000L))
        assertTrue(engine.isWithinFuzzyWindow(scheduledTime, 500L, 1000L)) // Lower bound
        assertTrue(engine.isWithinFuzzyWindow(scheduledTime, 2000L, 1000L)) // Upper bound
        assertFalse(engine.isWithinFuzzyWindow(scheduledTime, 2001L, 1000L)) // Just outside
        assertFalse(engine.isWithinFuzzyWindow(scheduledTime, -1L, 1000L)) // Just outside
    }

    @Test
    fun testCalculateScheduledTime_lateWake() {
        val anchor = TemporalAnchor("2026-04-20", 1713603600000L, "HealthConnect") // 9:00 AM (2h late)
        val medication = MedicationRecord(
            medicationId = "test",
            name = "Test Med",
            dosage = "10mg",
            frequencyOffset = 1 * 60 * 60 * 1000L, // T + 1h
            validFrom = 0L
        )

        val scheduledTime = engine.calculateScheduledTime(medication, anchor)
        assertEquals(1713607200000L, scheduledTime) // 10:00 AM
    }
}
