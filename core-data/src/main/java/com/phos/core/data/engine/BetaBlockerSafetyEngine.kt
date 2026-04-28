package com.phos.core.data.engine

import com.phos.core.data.model.TemporalAnchor
import java.time.Instant
import java.time.temporal.ChronoUnit

enum class BetaBlockerInsightType {
    BRADYCARDIA,
    FATIGUE_SLUMP,
    OXYGENATION_REMINDER
}

data class BetaBlockerInsight(
    val type: BetaBlockerInsightType,
    val title: String,
    val description: String,
    val isCritical: Boolean = false
)

class BetaBlockerSafetyEngine {

    /**
     * M1: Idle Speed RHR monitor for morning bradycardia detection.
     * Checks if any heart rate samples during the first 30 mins of wake are below 50 BPM.
     */
    fun detectBradycardia(
        anchor: TemporalAnchor,
        hrSamples: List<Long> // Beats per minute
    ): BetaBlockerInsight? {
        val lowHr = hrSamples.filter { it < 50 }
        if (lowHr.isNotEmpty()) {
            return BetaBlockerInsight(
                type = BetaBlockerInsightType.BRADYCARDIA,
                title = "Idle Speed Alert (Bradycardia)",
                description = "Your resting heart rate dropped below 50 BPM (${lowHr.minOrNull()} BPM) during your first 30 minutes awake. This may indicate excessive beta-blocker effect.",
                isCritical = true
            )
        }
        return null
    }

    /**
     * M2: 6-hour Slump automatic HR correlation logic.
     * If HR is >15% lower than the daily average during the 6-hour post-dose window.
     */
    fun detectFatigueSlump(
        doseTime: Long,
        dailyAvgHr: Double,
        slumpHrSamples: List<Long>
    ): BetaBlockerInsight? {
        if (slumpHrSamples.isEmpty() || dailyAvgHr <= 0) return null
        
        val avgSlumpHr = slumpHrSamples.average()
        val threshold = dailyAvgHr * 0.85 // 15% lower
        
        if (avgSlumpHr < threshold) {
            return BetaBlockerInsight(
                type = BetaBlockerInsightType.FATIGUE_SLUMP,
                title = "6-Hour Metabolic Slump",
                description = "Your heart rate is currently ${"%.1f".format(avgSlumpHr)} BPM, which is >15% below your daily average. This aligns with your beta-blocker's peak effect.",
                isCritical = false
            )
        }
        return null
    }

    /**
     * M3: Oxygenation Bout light-activity reminders.
     */
    fun suggestOxygenationBout(
        insight: BetaBlockerInsight?
    ): BetaBlockerInsight? {
        if (insight?.type == BetaBlockerInsightType.FATIGUE_SLUMP) {
            return BetaBlockerInsight(
                type = BetaBlockerInsightType.OXYGENATION_REMINDER,
                title = "Oxygenation Bout Recommended",
                description = "A 5-minute light activity (e.g., a brisk walk) is recommended to counter the current metabolic slump and improve circulation.",
                isCritical = false
            )
        }
        return null
    }
}
