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

class PostureIntelligence {

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
