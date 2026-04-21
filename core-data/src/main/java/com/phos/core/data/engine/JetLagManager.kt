package com.phos.core.data.engine

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class TitrationStep(
    val dayNumber: Int,
    val wakeTimeShiftMillis: Long
)

class JetLagManager {

    /**
     * Calculates a titration schedule to transition from a current T-Wake to a target T-Wake.
     * Strategy: Shift by max 2 hours per day to allow for circadian adjustment.
     */
    fun calculateTitrationSchedule(
        currentWakeMillis: Long,
        targetWakeMillis: Long,
        maxShiftPerDayHours: Int = 2
    ): List<TitrationStep> {
        val diff = targetWakeMillis - currentWakeMillis
        if (diff == 0L) return emptyList()

        val maxShiftMillis = Duration.ofHours(maxShiftPerDayHours.toLong()).toMillis()
        val numDays = Math.ceil(Math.abs(diff).toDouble() / maxShiftMillis).toInt()
        
        val steps = mutableListOf<TitrationStep>()
        val shiftPerDay = diff / numDays

        for (i in 1..numDays) {
            steps.add(TitrationStep(
                dayNumber = i,
                wakeTimeShiftMillis = currentWakeMillis + (shiftPerDay * i)
            ))
        }

        return steps
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
