package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

class CardioMismatchEngine {

    /**
     * M1: Muscle-Heart Mismatch detector.
     * Detects "High Output, Low Load" zones where Step Rate > 120 but HR < (RHR + 20).
     */
    fun detectMismatch(
        timestamp: Long,
        stepRate: Double,
        heartRate: Double,
        rhr: Double
    ): CardioMismatchInsight {
        // Normal response: HR should rise significantly with high step rate.
        // Heuristic: If stepping at >120 spm, HR should be at least RHR + 20.
        // If HR is below this, the "pump" isn't matching the "legs".
        
        val expectedHrFloor = rhr + 20.0
        val isBlunted = stepRate > 110.0 && heartRate < expectedHrFloor
        
        val intensity = if (stepRate > 0) {
            ((stepRate / 120.0) - (max(0.0, heartRate - rhr) / 40.0)).coerceIn(0.0, 1.0)
        } else 0.0

        return CardioMismatchInsight(
            timestamp = timestamp,
            stepRate = stepRate,
            heartRate = heartRate,
            rhr = rhr,
            mismatchIntensity = intensity,
            isSignificant = isBlunted && intensity > 0.5
        )
    }

    /**
     * M2: Daily Readiness scaling.
     */
    fun calculateReadiness(
        hrv: Double, // Current HRV
        avgHrv: Double, // Historical Avg
        rhr: Double, // Current RHR
        avgRhr: Double, // Historical Avg
        sleepQuality: Int // 1-10
    ): DailyReadiness {
        val hrvFactor = (hrv / avgHrv).coerceIn(0.5, 1.5)
        val rhrFactor = (avgRhr / rhr).coerceIn(0.5, 1.5) // Higher RHR is worse
        val sleepFactor = (sleepQuality / 10.0)
        
        val score = ((hrvFactor * 40.0) + (rhrFactor * 30.0) + (sleepFactor * 30.0)).toInt().coerceIn(0, 100)
        
        val recommendation = when {
            score < 40 -> "Critical Recovery: Your physiology shows signs of strain. Prefer light stretching or total rest."
            score < 70 -> "Moderate Readiness: Balanced state. Good for maintenance activity, avoid heavy peaks."
            else -> "Optimal Readiness: Your body is well-recovered and ready for higher intensity today."
        }
        
        val today = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(Instant.now())
        
        return DailyReadiness(
            date = today,
            score = score,
            hrvFactor = hrvFactor,
            rhrFactor = rhrFactor,
            sleepFactor = sleepFactor,
            recommendation = recommendation
        )
    }

    fun getScaling(score: Int): ActivityScaling {
        return when {
            score < 40 -> ActivityScaling.RECOVERY
            score < 70 -> ActivityScaling.MODERATE
            else -> ActivityScaling.OPTIMAL
        }
    }
}
