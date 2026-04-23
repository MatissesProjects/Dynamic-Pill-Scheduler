package com.phos.core.data.engine

import androidx.health.connect.client.records.SleepSessionRecord
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import com.phos.core.data.sync.HealthSyncManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit

class NapManagerTest {

    private lateinit var healthSyncManager: HealthSyncManager
    private lateinit var napManager: NapManager

    @Before
    fun setup() {
        healthSyncManager = mock()
        napManager = NapManager(healthSyncManager)
    }

    @Test
    fun `test checkNapOverlaps detects overlap and suggests shift`() = runBlocking {
        val now = Instant.now()
        val napStart = now.minus(30, ChronoUnit.MINUTES)
        val napEnd = now.plus(30, ChronoUnit.MINUTES)
        
        val mockNap = mock<SleepSessionRecord>()
        whenever(mockNap.startTime).thenReturn(napStart)
        whenever(mockNap.endTime).thenReturn(napEnd)
        
        whenever(healthSyncManager.fetchLatestNap()).thenReturn(mockNap)
        
        val anchor = TemporalAnchor(
            date = "2026-04-22", 
            wakeTime = now.minus(4, ChronoUnit.HOURS).toEpochMilli(),
            source = "Manual"
        )
        val medication = MedicationRecord(
            medicationId = "med_1",
            name = "Naptime Pill",
            dosage = "5mg",
            frequencyOffset = 4 * 3600000L, // Scheduled exactly at 'now'
            validFrom = 0L
        )

        val result = napManager.checkNapOverlaps(listOf(medication), anchor)

        assertEquals(1, result.size)
        assertEquals("med_1", result[0].medicationId)
        // napEnd is 30 mins after now. scheduled is now. delayNeeded = 30 mins. shift = 30 + 15 = 45 mins.
        assertEquals(45 * 60000L, result[0].suggestedShiftMillis)
    }

    @Test
    fun `test checkNapOverlaps returns empty when no nap`() = runBlocking {
        whenever(healthSyncManager.fetchLatestNap()).thenReturn(null)
        val result = napManager.checkNapOverlaps(emptyList(), mock())
        assertTrue(result.isEmpty())
    }
}
