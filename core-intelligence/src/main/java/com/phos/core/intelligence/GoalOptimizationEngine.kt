package com.phos.core.intelligence

import com.phos.core.data.model.HealthGoal
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.proto.MealPreferences
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class OptimizationSuggestion(
    val goalId: Long,
    val description: String,
    val suggestedMedicationShifts: Map<String, Long> = emptyList<Pair<String, Long>>().toMap(),
    val suggestedMealShifts: String? = null
)

class GoalOptimizationEngine {

    /**
     * Evaluates active health goals against the current schedule and proposes optimizations.
     */
    fun evaluateGoals(
        goals: List<HealthGoal>,
        medications: List<MedicationRecord>,
        mealPreferences: MealPreferences,
        tWakeEpoch: Long,
        nocturiaCount: Int = 0
    ): List<OptimizationSuggestion> {
        val suggestions = mutableListOf<OptimizationSuggestion>()
        
        // 1. Automatic Nocturia Check (Independent of explicit goals if count is high)
        if (nocturiaCount > 1) {
            val fluidMeds = medications.filter { med ->
                val name = med.name.lowercase()
                name.contains("furosemide") || name.contains("hydrochlorothiazide") || 
                name.contains("spironolactone") || name.contains("diuretic") ||
                name.contains("lithium") || name.contains("dapagliflozin")
            }
            
            val lateFluidMeds = fluidMeds.filter { it.frequencyOffset > 8 * 3600000L }
            if (lateFluidMeds.isNotEmpty()) {
                val shiftMap = mutableMapOf<String, Long>()
                lateFluidMeds.forEach { med ->
                    // Shift to 4 hours after wake
                    shiftMap[med.medicationId] = 4 * 3600000L
                }
                suggestions.add(OptimizationSuggestion(
                    goalId = -1, // System-generated goal
                    description = "Frequent nighttime bathroom breaks detected ($nocturiaCount last night). Suggesting earlier timing for ${lateFluidMeds.joinToString { it.name }} to minimize sleep disruption.",
                    suggestedMedicationShifts = shiftMap
                ))
            }
        }

        for (goal in goals) {
            val lowerSymptom = goal.targetSymptom.lowercase()
            
            // Example Rule: "stomach hurts at 4am" -> Probably late night NSAID or empty stomach issue.
            // Check if there's a late night medication (e.g. 14 hours after wake).
            if (lowerSymptom.contains("stomach") || lowerSymptom.contains("gas") || lowerSymptom.contains("nausea")) {
                val lateMeds = medications.filter { it.frequencyOffset > 12 * 3600000L }
                
                if (lateMeds.isNotEmpty()) {
                    val shiftMap = mutableMapOf<String, Long>()
                    lateMeds.forEach { med ->
                        // Suggest shifting it earlier by 2 hours (2 * 3600000L)
                        shiftMap[med.medicationId] = med.frequencyOffset - (2 * 3600000L)
                    }
                    suggestions.add(
                        OptimizationSuggestion(
                            goalId = goal.id,
                            description = "To help with ${goal.targetSymptom}, try taking evening medications earlier to prevent late-night stomach irritation.",
                            suggestedMedicationShifts = shiftMap,
                            suggestedMealShifts = "Consider moving dinner 1 hour earlier."
                        )
                    )
                } else if (mealPreferences.hasDinnerStartOffset() && mealPreferences.dinnerStartOffset > 14 * 3600000L) {
                    suggestions.add(
                        OptimizationSuggestion(
                            goalId = goal.id,
                            description = "Your late dinner might be causing ${goal.targetSymptom}. Consider shifting it earlier.",
                            suggestedMealShifts = "Try setting dinner between ${formatOffset(12 * 3600000L)} and ${formatOffset(13 * 3600000L)} after waking."
                        )
                    )
                } else {
                     suggestions.add(
                        OptimizationSuggestion(
                            goalId = goal.id,
                            description = "We are monitoring your schedule to optimize for ${goal.targetSymptom}."
                        )
                    )
                }
            }
        }
        
        return suggestions
    }
    
    private fun formatOffset(offsetMillis: Long): String {
        val hours = offsetMillis / 3600000L
        return "+${hours}h"
    }
}
