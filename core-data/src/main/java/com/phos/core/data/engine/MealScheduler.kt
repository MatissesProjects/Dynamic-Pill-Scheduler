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
    val score: Int, // 1-10 (10 = Best window)
    val isSacred: Boolean = false // If true, this is a reserved window for high-difficulty days
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
        val scheduledMeds = medications.map { 
            it to (anchor.wakeTime + it.frequencyOffset) 
        }.sortedBy { it.second }
        
        val windows = mutableListOf<OptimalEatingWindow>()
        val recentDifficulty = appetiteHistory.maxByOrNull { it.timestamp }?.difficultyLevel ?: 1
        val isHighDifficulty = recentDifficulty > 7

        // 1. Check gaps between medications
        for (i in 0 until scheduledMeds.size - 1) {
            val currentMed = scheduledMeds[i].first
            val currentTime = scheduledMeds[i].second
            val nextMed = scheduledMeds[i+1].first
            val nextTime = scheduledMeds[i+1].second
            
            // Adjust gap start/end based on food requirements
            var gapStart = currentTime + (30 * 60 * 1000L) // Default 30 mins recovery
            var gapEnd = nextTime - (30 * 60 * 1000L) // Default 30 mins buffer
            
            if (currentMed.foodRequirement == "WITH_FOOD") {
                // This is actually a great time to eat!
                windows.add(OptimalEatingWindow(
                    startTime = currentTime - (15 * 60 * 1000L),
                    endTime = currentTime + (45 * 60 * 1000L),
                    reason = "Optimal window to take ${currentMed.name} with food.",
                    score = 10
                ))
            }
            
            if (nextMed.foodRequirement == "EMPTY_STOMACH") {
                gapEnd = nextTime - (60 * 60 * 1000L) // Need 1h empty before
            }

            val gapDurationMins = (gapEnd - gapStart) / (60 * 1000)
            if (gapDurationMins >= 60) {
                val score = if (gapDurationMins > 120) 10 else 7
                
                windows.add(OptimalEatingWindow(
                    startTime = gapStart,
                    endTime = gapEnd,
                    reason = if (isHighDifficulty) "Sacred Eating Window: Reserved for digestion recovery." else "Uninterrupted gap for comfortable eating.",
                    score = if (isHighDifficulty) score + 2 else score,
                    isSacred = isHighDifficulty
                ))
            }
        }
        
        // 2. Window after the last medication
        scheduledMeds.lastOrNull()?.let { (lastMed, lastMedTime) ->
            windows.add(OptimalEatingWindow(
                startTime = lastMedTime + (60 * 60 * 1000L),
                endTime = lastMedTime + (240 * 60 * 1000L),
                reason = "Evening nutrition window.",
                score = 8
            ))
        }

        // 3. Early morning window (before first med if possible)
        scheduledMeds.firstOrNull()?.let { (firstMed, firstMedTime) ->
            if (firstMedTime - anchor.wakeTime > 60 * 60 * 1000L) {
                windows.add(OptimalEatingWindow(
                    startTime = anchor.wakeTime,
                    endTime = firstMedTime - (30 * 60 * 1000L),
                    reason = "Pre-medication breakfast window.",
                    score = 9
                ))
            }
        }

        return windows.distinctBy { it.startTime }.sortedByDescending { it.score }
    }
}
