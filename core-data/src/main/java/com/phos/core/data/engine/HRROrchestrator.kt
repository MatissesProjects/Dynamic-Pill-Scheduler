package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HRROrchestrator {

    /**
     * M1: HRR monitor logic.
     * Calculates deltas between peak HR and post-workout milestones.
     */
    fun calculateHRR(
        endTime: Instant,
        hrSamples: List<BiometricLog>, // 2 min window post-workout
        peakHr: Double,
        medicationVersion: Long
    ): HRRRecord {
        val hrAtOneMin = hrSamples.filter { 
            val diff = java.time.Duration.between(endTime, it.timestamp).toSeconds()
            diff in 55..65
        }.map { it.value }.average().takeIf { !it.isNaN() }

        val hrAtTwoMin = hrSamples.filter { 
            val diff = java.time.Duration.between(endTime, it.timestamp).toSeconds()
            diff in 115..125
        }.map { it.value }.average().takeIf { !it.isNaN() }

        val hrrOne = hrAtOneMin?.let { peakHr - it }
        val hrrTwo = hrAtTwoMin?.let { peakHr - it }

        val today = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(Instant.now())

        return HRRRecord(
            date = today,
            workoutEndTime = endTime,
            peakHr = peakHr,
            hrAtOneMin = hrAtOneMin,
            hrAtTwoMin = hrAtTwoMin,
            hrrOneMin = hrrOne,
            hrrTwoMin = hrrTwo,
            medicationVersion = medicationVersion
        )
    }

    /**
     * M2: Medication-Induced Recovery Audit.
     */
    fun buildHRRAudit(
        currentRecord: HRRRecord,
        historicalRecords: List<HRRRecord>
    ): HRRAudit? {
        val hrrOne = currentRecord.hrrOneMin ?: return null
        
        val validHistory = historicalRecords.mapNotNull { it.hrrOneMin }
        if (validHistory.isEmpty()) return null
        
        val avgHrr = validHistory.average()
        val trendDelta = ((hrrOne - avgHrr) / avgHrr) // Negative means slowing down
        
        val isStrained = trendDelta < -0.15 // >15% slowdown
        
        val advice = when {
            isStrained -> "Autonomic Strain Detected: Your 1-min heart rate recovery has slowed by ${"%.1f".format(-trendDelta * 100)}% compared to your 7-day average. This may indicate poor adaptation to your current medication dose."
            trendDelta < -0.05 -> "Recovery Lag: Your recovery is slightly slower today. Consider extra hydration and a lighter activity level."
            else -> "Resilient ANS: Your heart rate recovery is stable, indicating healthy autonomic adaptation."
        }

        return HRRAudit(
            date = currentRecord.date,
            sevenDayAvgOneMin = avgHrr,
            currentOneMin = hrrOne,
            trendDelta = trendDelta,
            isStrained = isStrained,
            advice = advice
        )
    }
}
