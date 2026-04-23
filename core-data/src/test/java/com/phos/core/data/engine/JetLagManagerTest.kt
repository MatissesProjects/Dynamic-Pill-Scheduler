package com.phos.core.data.engine

import com.phos.core.data.model.TitrationStep
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class JetLagManagerTest {

    private val jetLagManager = JetLagManager()

    @Test
    fun `test calculateTitrationSchedule forward shift`() {
        val now = ZonedDateTime.now()
        val currentWake = now.with(LocalTime.of(8, 0)).toInstant().toEpochMilli()
        val targetWake = now.with(LocalTime.of(12, 0)).toInstant().toEpochMilli() // 4h forward
        
        val schedule = jetLagManager.calculateTitrationSchedule(
            currentWake, targetWake, now, maxShiftPerDayHours = 1.5
        )

        // 4h / 1.5h = 2.66 -> 3 days
        assertEquals(3, schedule.size)
        assertTrue(schedule[0].targetWakeTime > currentWake)
        assertTrue(schedule[schedule.size - 1].targetWakeTime == targetWake)
    }

    @Test
    fun `test proposeAdvanceTitration calculates steps`() {
        val travelDate = Instant.now().plus(7, ChronoUnit.DAYS)
        val proposal = jetLagManager.proposeAdvanceTitration(
            destination = "London",
            targetZoneId = "Europe/London",
            travelDate = travelDate
        )

        assertNotNull(proposal)
        assertEquals("London", proposal.destination)
        assertTrue(proposal.titrationSteps.isNotEmpty())
    }
}
