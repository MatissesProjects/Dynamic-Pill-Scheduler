package com.phos.core.data.engine

import com.phos.core.data.model.AppetiteLog
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import java.time.Instant
import kotlin.math.abs

data class OptimalEatingWindow(
    val startTime: Long, // Epoch millis
    val endTime: Long,
    val reason: String,
    val score: Int // 1-10 (10 = Best window)
)

class MealScheduler {

    /**
     * Identifies windows of time where no medications are scheduled, 
     * specifically looking for gaps of at least 60 minutes.
     */
    fun findOptimalEatingWindows(
        medications: List<MedicationRecord>,
        anchor: TemporalAnchor,
        appetiteHistory: List<AppetiteLog>
    ): List<OptimalEatingWindow> {
        val scheduledTimes = medications.map { anchor.wakeTime + it.frequencyOffset }.sorted()
        val windows = mutableListOf<OptimalEatingWindow>()
        
        // Strategy: Look at gaps between medications
        for (i in 0 until scheduledTimes.size - 1) {
            val gapStart = scheduledTimes[i] + (30 * 60 * 1000L) // 30 mins after dose
            val gapEnd = scheduledTimes[i+1] - (30 * 60 * 1000L) // 30 mins before next
            
            val gapDurationMins = (gapEnd - gapStart) / (60 * 1000)
            if (gapDurationMins >= 60) {
                // If user has high difficulty eating recently, we prioritize the longest gaps
                val recentDifficulty = appetiteHistory.firstOrNull()?.difficultyLevel ?: 1
                val score = if (gapDurationMins > 120) 10 else 7
                
                windows.add(OptimalEatingWindow(
                    startTime = gapStart,
                    endTime = gapEnd,
                    reason = "Uninterrupted gap for digestion and hunger recovery.",
                    score = if (recentDifficulty > 7) score + 2 else score
                ))
            }
        }
        
        // Also check window after the last medication of the day
        scheduledTimes.lastOrNull()?.let { lastMedTime ->
            windows.add(OptimalEatingWindow(
                startTime = lastMedTime + (60 * 60 * 1000L),
                endTime = lastMedTime + (180 * 60 * 1000L),
                reason = "Evening window for relaxed digestion.",
                score = 8
            ))
        }

        return windows.sortedByDescending { it.score }
    }
}
