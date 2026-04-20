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
        assertFalse(engine.isWithinFuzzyWindow(scheduledTime, 2500L, 1000L))
    }
}
