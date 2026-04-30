package com.phos.core.data.engine

import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class HormonalSyncEngineTest {

    @Test
    fun testCARPeakCalculation() {
        val engine = HormonalSyncEngine()
        val baseTime = Instant.parse("2024-01-01T07:00:00Z").toEpochMilli() // 7:00 AM
        val anchor = TemporalAnchor(
            date = "2024-01-01",
            wakeTime = baseTime,
            source = "Manual"
        )

        val (peakStart, peakEnd) = engine.calculateCARPeakWindow(anchor)

        // CAR peak should be 30-45 mins after T-Wake
        val expectedStart = baseTime + (30 * 60 * 1000L)
        val expectedEnd = baseTime + (45 * 60 * 1000L)

        assertEquals(expectedStart, peakStart)
        assertEquals(expectedEnd, peakEnd)
    }
}
