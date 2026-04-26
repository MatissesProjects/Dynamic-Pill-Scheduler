package com.phos.core.intelligence

import com.phos.core.data.model.Chronotype
import com.phos.core.data.model.HealthGoal
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.MetabolicLoadLog
import com.phos.core.data.proto.MealPreferences
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class OptimizationSuggestion(
    val goalId: Long,
    val title: String = "Health Optimization",
    val id: String = "gen_${System.currentTimeMillis()}",
    val description: String,
    val suggestedMedicationShifts: Map<String, Long> = emptyMap(),
    val suggestedMealShifts: String? = null
)

class GoalOptimizationEngine {

    /**
     * Evaluates active health goals and metabolic state against the current schedule.
     */
    fun evaluateGoals(
        goals: List<HealthGoal>,
        medications: List<MedicationRecord>,
        mealPreferences: MealPreferences,
        tWakeEpoch: Long,
        nocturiaCount: Int = 0,
        chronotype: Chronotype = Chronotype.NEUTRAL,
        metabolicLogs: List<MetabolicLoadLog> = emptyList()
    ): List<OptimizationSuggestion> {
        val suggestions = mutableListOf<OptimizationSuggestion>()
        
        // 1. Automatic Nocturia Check
        if (nocturiaCount > 1) {
            val fluidMeds = medications.filter { med ->
                val name = med.name.lowercase()
                name.contains("furosemide") || name.contains("hydrochlorothiazide") || 
                name.contains("spironolactone") || name.contains("diuretic") ||
                name.contains("lithium") || name.contains("dapagliflozin")
            }
            
            val lateFluidMeds = fluidMeds.filter { it.frequencyOffset > 8 * 3600000L }
            if (lateFluidMeds.isNotEmpty()) {
                suggestions.add(OptimizationSuggestion(
                    goalId = -1,
                    id = "nocturia_reduction",
                    title = "Circadian Safety (Nocturia)",
                    description = "Frequent nighttime bathroom breaks detected. Suggesting earlier timing for diuretics to minimize sleep disruption.",
                    suggestedMedicationShifts = lateFluidMeds.associate { it.medicationId to 4 * 3600000L }
                ))
            }
        }

        // 2. Chronotype-Aware Alignment
        val antihypertensives = medications.filter { 
            val cat = it.category?.lowercase() ?: ""
            cat == "heart" || it.name.lowercase().contains("lisinopril") || 
            it.name.lowercase().contains("metoprolol") || it.name.lowercase().contains("amlodipine")
        }
        
        if (antihypertensives.isNotEmpty()) {
            when (chronotype) {
                Chronotype.OWL -> {
                    val misplaced = antihypertensives.filter { it.frequencyOffset < 10 * 3600000L }
                    if (misplaced.isNotEmpty()) {
                        suggestions.add(OptimizationSuggestion(
                            goalId = -2,
                            id = "owl_bp_alignment",
                            title = "Biological Evening Alignment",
                            description = "As a Night Owl, research suggests taking blood pressure medication in the evening significantly reduces your cardiovascular risk.",
                            suggestedMedicationShifts = misplaced.associate { it.medicationId to 13 * 3600000L }
                        ))
                    }
                }
                Chronotype.LARK -> {
                    val misplaced = antihypertensives.filter { it.frequencyOffset > 4 * 3600000L }
                    if (misplaced.isNotEmpty()) {
                        suggestions.add(OptimizationSuggestion(
                            goalId = -2,
                            id = "lark_bp_alignment",
                            title = "Biological Morning Alignment",
                            description = "As a Morning Lark, your body responds best to blood pressure medication taken early in the day.",
                            suggestedMedicationShifts = misplaced.associate { it.medicationId to 1 * 3600000L }
                        ))
                    }
                }
                else -> {}
            }
        }

        // 3. Statin Circadian Sync
        val shortStatins = medications.filter { 
            val name = it.name.lowercase()
            name.contains("simvastatin") || name.contains("lovastatin") || name.contains("fluvastatin")
        }
        val misplacedStatins = shortStatins.filter { it.frequencyOffset < 12 * 3600000L }
        if (misplacedStatins.isNotEmpty()) {
            suggestions.add(OptimizationSuggestion(
                goalId = -3,
                id = "statin_sync",
                title = "Cholesterol Synthesis Sync",
                description = "Short-acting statins are most effective when taken at bedtime to align with peak overnight cholesterol production.",
                suggestedMedicationShifts = misplacedStatins.associate { it.medicationId to 14 * 3600000L }
            ))
        }

        // 4. Metabolic Digital Twin Logic
        val recentMetabolic = metabolicLogs.filter { it.timestamp.isAfter(Instant.now().minusSeconds(7200)) } // Last 2 hours
        val isHyperMetabolic = recentMetabolic.any { it.isHyperMetabolic }
        
        if (isHyperMetabolic) {
            val highStrain = recentMetabolic.maxByOrNull { it.trimpScore }
            suggestions.add(OptimizationSuggestion(
                goalId = -4,
                id = "metabolic_spike",
                title = "Metabolic Stress Compensation",
                description = "High cardiovascular strain detected (TRIMP: ${"%.0f".format(highStrain?.trimpScore)}). To avoid absorption spikes and ensure kidney safety, we recommend increasing hydration by 500ml and delaying current doses by 30 minutes.",
                suggestedMedicationShifts = medications.associate { it.medicationId to it.frequencyOffset + 1800000L } // +30 mins
            ))
        }

        // 5. Goal-Based Optimization
        for (goal in goals) {
            val lowerSymptom = goal.targetSymptom.lowercase()
            if (lowerSymptom.contains("stomach") || lowerSymptom.contains("gas") || lowerSymptom.contains("nausea")) {
                val lateMeds = medications.filter { it.frequencyOffset > 12 * 3600000L }
                if (lateMeds.isNotEmpty()) {
                    suggestions.add(OptimizationSuggestion(
                        goalId = goal.id,
                        id = "goal_stomach_${goal.id}",
                        title = "Symptom Targeted: ${goal.targetSymptom}",
                        description = "To help with ${goal.targetSymptom}, try taking evening medications earlier to prevent late-night stomach irritation.",
                        suggestedMedicationShifts = lateMeds.associate { it.medicationId to it.frequencyOffset - (2 * 3600000L) },
                        suggestedMealShifts = "Consider moving dinner 1 hour earlier."
                    ))
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
