package com.phos.core.intelligence

import androidx.health.connect.client.records.SleepSessionRecord
import com.phos.core.data.model.Chronotype
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId

class ChronotypeClassifierTest {

    private val classifier = ChronotypeClassifier()
    private val zoneId = ZoneId.systemDefault()

    @Test
    fun `test classify as LARK for early mid-sleep`() {
        // Sleep 10 PM to 6 AM -> Mid sleep at 2 AM
        // We simulate 7 days (including Sat/Sun)
        val records = (1..7).map { day ->
            val start = ZonedDateTime.of(2026, 4, day, 22, 0, 0, 0, zoneId).toInstant()
            val end = ZonedDateTime.of(2026, 4, day + 1, 6, 0, 0, 0, zoneId).toInstant()
            mock<SleepSessionRecord>().apply {
                whenever(startTime).thenReturn(start)
                whenever(endTime).thenReturn(end)
            }
        }
        
        val result = classifier.classify(records)
        assertEquals(Chronotype.LARK, result.type)
    }

    @Test
    fun `test classify as OWL for late mid-sleep`() {
        // Sleep 2 AM to 10 AM -> Mid sleep at 6 AM
        val records = (1..7).map { day ->
            val start = ZonedDateTime.of(2026, 4, day, 2, 0, 0, 0, zoneId).toInstant()
            val end = ZonedDateTime.of(2026, 4, day, 10, 0, 0, 0, zoneId).toInstant()
            mock<SleepSessionRecord>().apply {
                whenever(startTime).thenReturn(start)
                whenever(endTime).thenReturn(end)
            }
        }
        
        val result = classifier.classify(records)
        assertEquals(Chronotype.OWL, result.type)
    }

    @Test
    fun `test classify with sleep debt correction`() {
        // Workdays (1-5): 11 PM to 6 AM (7h duration, mid-sleep 2:30 AM)
        // Free days (6-7): 1 AM to 11 AM (10h duration, mid-sleep 6:00 AM)
        
        // SDw = 7h, SDf = 10h. 
        // MSF = 6:00 AM (21,600,000ms)
        // MSFsc = 6:00 - (10 - 7)/2 = 6:00 - 1.5h = 4:30 AM
        // 4:30 AM is between 3 AM and 5 AM -> NEUTRAL
        
        val workdays = (1..5).map { day ->
            val start = ZonedDateTime.of(2026, 4, day, 23, 0, 0, 0, zoneId).toInstant()
            val end = ZonedDateTime.of(2026, 4, day + 1, 6, 0, 0, 0, zoneId).toInstant()
            mock<SleepSessionRecord>().apply {
                whenever(startTime).thenReturn(start)
                whenever(endTime).thenReturn(end)
            }
        }
        val freedays = (6..7).map { day ->
            val start = ZonedDateTime.of(2026, 4, day, 1, 0, 0, 0, zoneId).toInstant()
            val end = ZonedDateTime.of(2026, 4, day, 11, 0, 0, 0, zoneId).toInstant()
            mock<SleepSessionRecord>().apply {
                whenever(startTime).thenReturn(start)
                whenever(endTime).thenReturn(end)
            }
        }
        
        val result = classifier.classify(workdays + freedays)
        assertEquals(Chronotype.NEUTRAL, result.type)
    }
}
