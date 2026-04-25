package com.phos.core.data.engine

import com.phos.core.data.model.AppetiteLog
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import com.phos.core.data.proto.MealPreferences
import java.time.Instant
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
        appetiteHistory: List<AppetiteLog>,
        mealPreferences: MealPreferences? = null
    ): List<OptimalEatingWindow> {
        val scheduledMeds = medications.map { 
            it to (anchor.wakeTime + it.frequencyOffset) 
        }.sortedBy { it.second }
        
        val windows = mutableListOf<OptimalEatingWindow>()
        val recentDifficulty = appetiteHistory.maxByOrNull { it.timestamp }?.difficultyLevel ?: 1
        val isHighDifficulty = recentDifficulty > 7

        // Helper to check overlap with preferences
        fun calculatePreferenceBoost(start: Long, end: Long): Int {
            if (mealPreferences == null) return 0
            var boost = 0
            val tWake = anchor.wakeTime
            
            // Check Breakfast
            if (mealPreferences.hasBreakfastStartOffset() && mealPreferences.hasBreakfastEndOffset()) {
                val bStart = tWake + mealPreferences.breakfastStartOffset
                val bEnd = tWake + mealPreferences.breakfastEndOffset
                if (start <= bEnd && end >= bStart) boost = max(boost, 2)
            }
            // Check Lunch
            if (mealPreferences.hasLunchStartOffset() && mealPreferences.hasLunchEndOffset()) {
                val lStart = tWake + mealPreferences.lunchStartOffset
                val lEnd = tWake + mealPreferences.lunchEndOffset
                if (start <= lEnd && end >= lStart) boost = max(boost, 2)
            }
            // Check Dinner
            if (mealPreferences.hasDinnerStartOffset() && mealPreferences.hasDinnerEndOffset()) {
                val dStart = tWake + mealPreferences.dinnerStartOffset
                val dEnd = tWake + mealPreferences.dinnerEndOffset
                if (start <= dEnd && end >= dStart) boost = max(boost, 2)
            }
            return boost
        }

        // 1. Check all medications for food requirements and gaps
        for (i in scheduledMeds.indices) {
            val (currentMed, currentTime) = scheduledMeds[i]
            
            if (currentMed.foodRequirement == "WITH_FOOD") {
                val wStart = currentTime - (15 * 60 * 1000L)
                val wEnd = currentTime + (45 * 60 * 1000L)
                windows.add(OptimalEatingWindow(
                    startTime = wStart,
                    endTime = wEnd,
                    reason = "Optimal window to take ${currentMed.name} with food.",
                    score = min(10, 10 + calculatePreferenceBoost(wStart, wEnd))
                ))
            }

            if (i < scheduledMeds.size - 1) {
                val nextMed = scheduledMeds[i+1].first
                val nextTime = scheduledMeds[i+1].second
                
                var gapStart = currentTime + (30 * 60 * 1000L)
                var gapEnd = nextTime - (30 * 60 * 1000L)
                
                if (nextMed.foodRequirement == "EMPTY_STOMACH") {
                    gapEnd = nextTime - (60 * 60 * 1000L)
                }

                val gapDurationMins = (gapEnd - gapStart) / (60 * 1000)
                if (gapDurationMins >= 60) {
                    var score = if (gapDurationMins > 120) 10 else 7
                    if (isHighDifficulty) score += 2
                    score += calculatePreferenceBoost(gapStart, gapEnd)
                    
                    windows.add(OptimalEatingWindow(
                        startTime = gapStart,
                        endTime = gapEnd,
                        reason = if (isHighDifficulty) "Sacred Eating Window: Reserved for digestion recovery." else "Uninterrupted gap for comfortable eating.",
                        score = min(10, score),
                        isSacred = isHighDifficulty
                    ))
                }
            }
        }
        
        // 2. Window after the last medication
        scheduledMeds.lastOrNull()?.let { (lastMed, lastMedTime) ->
            val wStart = lastMedTime + (60 * 60 * 1000L)
            val wEnd = lastMedTime + (240 * 60 * 1000L)
            windows.add(OptimalEatingWindow(
                startTime = wStart,
                endTime = wEnd,
                reason = "Evening nutrition window.",
                score = min(10, 8 + calculatePreferenceBoost(wStart, wEnd))
            ))
        }

        // 3. Early morning window (before first med if possible)
        scheduledMeds.firstOrNull()?.let { (firstMed, firstMedTime) ->
            if (firstMedTime - anchor.wakeTime > 60 * 60 * 1000L) {
                val wStart = anchor.wakeTime
                val wEnd = firstMedTime - (30 * 60 * 1000L)
                windows.add(OptimalEatingWindow(
                    startTime = wStart,
                    endTime = wEnd,
                    reason = "Pre-medication breakfast window.",
                    score = min(10, 9 + calculatePreferenceBoost(wStart, wEnd))
                ))
            }
        }

        return windows.distinctBy { it.startTime }.sortedByDescending { it.score }
    }
}
