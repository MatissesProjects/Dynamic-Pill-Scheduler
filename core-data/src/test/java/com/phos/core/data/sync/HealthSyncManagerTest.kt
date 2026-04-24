package com.phos.core.data.sync

import androidx.health.connect.client.records.SleepSessionRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthSyncManagerTest {

    @Test
    fun `test healSleepSessions merges close sessions`() {
        val manager = HealthSyncManager(mock())
        
        val now = Instant.now()
        
        // Session 1: 10 PM - 2 AM
        val s1Start = now.minus(10, ChronoUnit.HOURS)
        val s1End = now.minus(6, ChronoUnit.HOURS)
        val mockS1 = mock<SleepSessionRecord>()
        whenever(mockS1.startTime).thenReturn(s1Start)
        whenever(mockS1.endTime).thenReturn(s1End)
        
        // Session 2: 2:10 AM - 6 AM (10 min gap)
        val s2Start = s1End.plus(10, ChronoUnit.MINUTES)
        val s2End = now.minus(2, ChronoUnit.HOURS)
        val mockS2 = mock<SleepSessionRecord>()
        whenever(mockS2.startTime).thenReturn(s2Start)
        whenever(mockS2.endTime).thenReturn(s2End)
        
        val result = manager.healSleepSessions(listOf(mockS1, mockS2))
        
        assertEquals(1, result.size)
        assertEquals(s1Start, result[0].first)
        assertEquals(s2End, result[0].second)
    }

    @Test
    fun `test healSleepSessions does not merge far sessions`() {
        val manager = HealthSyncManager(mock())
        
        val now = Instant.now()
        
        val s1End = now.minus(6, ChronoUnit.HOURS)
        val mockS1 = mock<SleepSessionRecord>()
        whenever(mockS1.startTime).thenReturn(now.minus(10, ChronoUnit.HOURS))
        whenever(mockS1.endTime).thenReturn(s1End)
        
        val s2Start = s1End.plus(45, ChronoUnit.MINUTES) // 45 min gap > 30 min
        val mockS2 = mock<SleepSessionRecord>()
        whenever(mockS2.startTime).thenReturn(s2Start)
        whenever(mockS2.endTime).thenReturn(now.minus(2, ChronoUnit.HOURS))
        
        val result = manager.healSleepSessions(listOf(mockS1, mockS2))
        
        assertEquals(2, result.size)
    }
}
