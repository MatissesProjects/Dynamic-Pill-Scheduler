package com.phos.core.data.engine

import com.phos.core.data.model.AppetiteLog
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import java.time.Instant
import kotlin.math.max

data class HormonalSyncAlert(
    val type: String, // "SYNC_ALERT", "DAWN_PHENOMENON", "ABSORPTION_WARNING"
    val medicationId: String,
    val message: String,
    val severity: Int // 1-10
)

data class HormonalHarmonyReport(
    val alignmentScore: Int, // 0-100
    val alerts: List<HormonalSyncAlert>
)

class HormonalSyncEngine(
    private val mealScheduler: MealScheduler = MealScheduler()
) {

    /**
     * Models the Cortisol Awakening Response (CAR) peak.
     * 30-45 minutes after T-Wake.
     */
    fun calculateCARPeakWindow(anchor: TemporalAnchor): Pair<Long, Long> {
        val peakStart = anchor.wakeTime + (30 * 60 * 1000L)
        val peakEnd = anchor.wakeTime + (45 * 60 * 1000L)
        return Pair(peakStart, peakEnd)
    }

    /**
     * Evaluates alignment for an entire regimen and returns a harmony score & alerts.
     */
    fun evaluateHormonalHarmony(
        medications: List<MedicationRecord>,
        anchor: TemporalAnchor,
        appetiteHistory: List<AppetiteLog> = emptyList()
    ): HormonalHarmonyReport {
        val alerts = mutableListOf<HormonalSyncAlert>()
        var totalScore = 100

        val (carStart, carEnd) = calculateCARPeakWindow(anchor)
        val dayEnd = anchor.wakeTime + (16 * 60 * 60 * 1000L)

        // Evaluate actual meal logs (AppetiteLogs) against thyroid meds
        val recentMeals = appetiteHistory.filter { it.timestamp.toEpochMilli() >= anchor.wakeTime }

        for (med in medications) {
            val medTime = anchor.wakeTime + med.frequencyOffset
            
            // 1. Cortisol replacement
            if (med.category?.contains("Cortisol", ignoreCase = true) == true || med.name.contains("Hydrocortisone", ignoreCase = true)) {
                if (medTime < carStart || medTime > carEnd + (60 * 60 * 1000L)) {
                    if (medTime >= dayEnd - (4 * 60 * 60 * 1000L)) {
                        alerts.add(HormonalSyncAlert("SYNC_ALERT", med.medicationId, "Late-night cortisol replacement may cause insomnia.", 8))
                        totalScore -= 20
                    } else if (med.frequencyOffset < 4 * 60 * 60 * 1000L) {
                         alerts.add(HormonalSyncAlert("SYNC_ALERT", med.medicationId, "Morning cortisol should ideally align with the CAR peak (30-45 mins post-wake).", 5))
                         totalScore -= 10
                    }
                }
            }

            // 2. Thyroid fasting rule
            if (med.category?.contains("Thyroid", ignoreCase = true) == true || med.name.contains("Levothyroxine", ignoreCase = true)) {
                val requiredFastingEnd = medTime + (30 * 60 * 1000L)
                
                // Check if any actual meal was logged within 30 mins after taking the dose
                val overlappingMeal = recentMeals.find { 
                    it.timestamp.toEpochMilli() in medTime..requiredFastingEnd 
                }
                
                if (overlappingMeal != null) {
                    alerts.add(HormonalSyncAlert("ABSORPTION_WARNING", med.medicationId, "Thyroid medication taken too close to a meal. Absorption may be impaired.", 9))
                    totalScore -= 30
                } else {
                    // Check against scheduled eating windows
                    val eatingWindows = mealScheduler.findOptimalEatingWindows(medications, anchor, appetiteHistory)
                    val overlappingWindow = eatingWindows.find { it.startTime < requiredFastingEnd && it.endTime > medTime }
                    if (overlappingWindow != null) {
                        alerts.add(HormonalSyncAlert("ABSORPTION_WARNING", med.medicationId, "Thyroid medication overlaps with an eating window. Absorption may be impaired.", 7))
                        totalScore -= 20
                    }
                }
            }

            // 3. Insulin / Dawn Phenomenon
            if (med.category?.contains("Insulin", ignoreCase = true) == true) {
                // Delayed morning insulin
                if (medTime > anchor.wakeTime + (90 * 60 * 1000L) && med.frequencyOffset < 4 * 60 * 60 * 1000L) {
                    alerts.add(HormonalSyncAlert("DAWN_PHENOMENON", med.medicationId, "Delayed morning insulin increases risk of Dawn Phenomenon hyperglycemia.", 7))
                    totalScore -= 15
                }
            }
        }

        return HormonalHarmonyReport(
            alignmentScore = max(0, totalScore),
            alerts = alerts
        )
    }
}
