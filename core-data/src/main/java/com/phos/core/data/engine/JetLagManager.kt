package com.phos.core.data.engine

import com.phos.core.data.model.TitrationStep
import com.phos.core.data.model.TravelProposal
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class JetLagManager {

    /**
     * Calculates a titration schedule to transition from a current T-Wake to a target T-Wake.
     * Strategy: Shift by max 1.5 hours per day to allow for circadian adjustment.
     */
    fun calculateTitrationSchedule(
        currentWakeMillis: Long,
        targetWakeMillis: Long,
        startDate: ZonedDateTime,
        maxShiftPerDayHours: Double = 1.5
    ): List<TitrationStep> {
        val diff = targetWakeMillis - currentWakeMillis
        if (Math.abs(diff) < 300000) return emptyList() // Ignore less than 5 min diff

        val maxShiftMillis = (maxShiftPerDayHours * 3600000L).toLong()
        val numDays = Math.ceil(Math.abs(diff).toDouble() / maxShiftMillis).toInt()
        
        val steps = mutableListOf<TitrationStep>()
        val shiftPerDay = diff / numDays

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        for (i in 1..numDays) {
            val stepDate = startDate.plusDays(i.toLong() - 1)
            steps.add(TitrationStep(
                dayNumber = i,
                date = stepDate.format(formatter),
                targetWakeTime = currentWakeMillis + (shiftPerDay * i)
            ))
        }

        return steps
    }

    /**
     * Proposes a titration schedule starting multiple days BEFORE the travel date.
     */
    fun proposeAdvanceTitration(
        destination: String,
        targetZoneId: String,
        travelDate: Instant,
        currentWakeTimeLocal: LocalTime = LocalTime.of(8, 0)
    ): TravelProposal {
        val targetZone = ZoneId.of(targetZoneId)
        val currentZone = ZoneId.systemDefault()
        
        // Calculate what 8:00 AM at destination is in current local time
        val travelZoned = travelDate.atZone(targetZone)
        val targetWakeAtDestination = travelZoned.with(currentWakeTimeLocal)
        val targetWakeInCurrentZone = targetWakeAtDestination.withZoneSameInstant(currentZone)
        
        val currentWakeToday = ZonedDateTime.now(currentZone).with(currentWakeTimeLocal)
        
        val steps = calculateTitrationSchedule(
            currentWakeToday.toInstant().toEpochMilli(),
            targetWakeInCurrentZone.toInstant().toEpochMilli(),
            ZonedDateTime.now(currentZone).plusDays(1)
        )

        return TravelProposal(
            destination = destination,
            targetZoneId = targetZoneId,
            travelDate = travelDate,
            titrationSteps = steps,
            explanation = "Proposing a ${steps.size}-day shift to align with $destination time."
        )
    }

    /**
     * Helper to calculate target wake time in a new time zone.
     */
    fun getTargetWakeInTimeZone(
        originalWakeTime: Instant,
        targetZone: ZoneId
    ): Instant {
        val originalZoned = originalWakeTime.atZone(ZoneId.systemDefault())
        val targetZoned = originalZoned.withZoneSameLocal(targetZone)
        return targetZoned.toInstant()
    }
}
