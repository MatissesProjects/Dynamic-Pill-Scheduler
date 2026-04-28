package com.phos.core.intelligence

import com.phos.core.data.model.FoodLog
import java.time.Instant
import java.time.temporal.ChronoUnit

data class PosturalRecommendation(
    val title: String,
    val recommendation: String,
    val durationMinutes: Int,
    val remainingMinutes: Int
)

data class OrthostaticInsight(
    val title: String,
    val riskLevel: String, // "LOW", "MODERATE", "HIGH"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

class PostureIntelligence {

    /**
     * T43: Orthostatic Posture & Biomechanics
     * Detects rapid vertical transitions using barometric pressure changes.
     * Strategy: A pressure drop of ~0.12 hPa corresponds to ~1 meter rise.
     */
    fun detectOrthostaticTransition(
        pressureSamples: List<Double>,
        isOnBetaBlocker: Boolean
    ): OrthostaticInsight? {
        if (pressureSamples.size < 2) return null
        
        val startPressure = pressureSamples.first()
        val endPressure = pressureSamples.last()
        val deltaP = startPressure - endPressure // Positive if pressure dropped (rising)
        
        val estimatedRiseMeters = deltaP / 0.12
        
        // If rise is > 0.5m in a short window, it's a stand-up event
        if (estimatedRiseMeters > 0.5) {
            return OrthostaticInsight(
                title = "Rapid Stand-up Detected",
                riskLevel = if (isOnBetaBlocker) "HIGH" else "MODERATE",
                description = if (isOnBetaBlocker) {
                    "Your Beta-Blocker medication may delay heart rate compensation. Pause and steady yourself to prevent dizziness."
                } else {
                    "Quick vertical movement detected. Watch for lightheadedness."
                }
            )
        }
        
        return null
    }

    /**
     * T43: Biomechanics Desk Feedback
     * Evaluates desk posture slump angle (from webcam/MediaPipe feedback).
     */
    fun evaluateDeskPosture(slumpAngle: Double): PosturalRecommendation? {
        if (slumpAngle > 30.0) { // Degrees of forward lean
            return PosturalRecommendation(
                title = "Posture Check",
                recommendation = "Slumped posture detected. Sit upright to reduce cardiovascular strain and optimize thoracic expansion.",
                durationMinutes = 5,
                remainingMinutes = 5
            )
        }
        return null
    }

    /**
     * Checks if a user has recently eaten and provides "Stay Upright" guidance.
     * Strategy: Post-prandial GERD prevention usually recommends 30-60 mins upright.
     */
    fun checkPostPrandialPosture(recentFoodLogs: List<FoodLog>): PosturalRecommendation? {
        val latestMeal = recentFoodLogs.maxByOrNull { it.timestamp } ?: return null
        
        val now = Instant.now()
        val mealTime = Instant.ofEpochMilli(latestMeal.timestamp)
        val minutesSinceMeal = ChronoUnit.MINUTES.between(mealTime, now).toInt()
        
        val requiredUprightMinutes = 45 // Standard recommendation
        
        if (minutesSinceMeal < requiredUprightMinutes) {
            return PosturalRecommendation(
                title = "Digestion Guidance",
                recommendation = "Stay upright for another ${requiredUprightMinutes - minutesSinceMeal} minutes to optimize digestion of '${latestMeal.name}' and avoid acid reflux.",
                durationMinutes = requiredUprightMinutes,
                remainingMinutes = requiredUprightMinutes - minutesSinceMeal
            )
        }
        
        return null
    }
}
