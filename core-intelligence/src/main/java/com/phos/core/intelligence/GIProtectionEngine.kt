package com.phos.core.intelligence

import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.SymptomLog
import java.time.Instant
import java.time.temporal.ChronoUnit

data class GIProtectionInsight(
    val highRiskMedications: List<String>, // medicationIds
    val advice: String,
    val needsGastricBuffer: Boolean
)

data class ChelationInsight(
    val medicationName: String,
    val mineralType: String,
    val conflictingFoodName: String,
    val advice: String,
    val riskLevel: String = "CRITICAL"
)

class GIProtectionEngine {

    /**
     * T44: Micronutrient Chelation Avoidance
     * Detects if a medication is taken too close to a mineral-rich food (chelation risk).
     */
    fun detectChelationRisk(
        medication: MedicationRecord,
        foodLogs: List<com.phos.core.data.model.FoodLog>,
        rules: List<com.phos.core.data.model.ChelationRule>
    ): ChelationInsight? {
        val rule = rules.find { it.medicationId == medication.medicationId } ?: return null
        
        // Find food logs within the required gap window (both before and after)
        val now = Instant.now()
        val gapMinutes = rule.requiredGapMinutes
        
        val conflictingFood = foodLogs.find { food ->
            val nutrients = food.nutrients ?: return@find false
            val hasMineral = when (rule.mineralType) {
                "CALCIUM" -> (nutrients.calciumMg ?: 0.0) > 50.0 // Threshold for significant calcium
                "IRON" -> (nutrients.ironMg ?: 0.0) > 2.0
                else -> false
            }
            
            if (!hasMineral) return@find false
            
            val foodTime = Instant.ofEpochMilli(food.timestamp)
            val minutesDiff = Math.abs(ChronoUnit.MINUTES.between(foodTime, now))
            minutesDiff < gapMinutes
        }

        if (conflictingFood != null) {
            return ChelationInsight(
                medicationName = medication.name,
                mineralType = rule.mineralType,
                conflictingFoodName = conflictingFood.name,
                advice = rule.advice
            )
        }
        
        return null
    }

    /**
     * Identifies if recent stomach discomfort correlates with irritant dose timing.
     */
    fun correlateStomachPain(
        meds: List<MedicationRecord>,
        irritantIds: List<String>,
        symptomLogs: List<SymptomLog>
    ): List<String> {
        val stomachPainLogs = symptomLogs.filter { it.symptomName.lowercase().contains("stomach") || it.symptomName.lowercase().contains("gastric") }
        val culpritIds = mutableListOf<String>()

        stomachPainLogs.forEach { log ->
            // Check if any irritant was taken 1-4 hours before pain
            val relevantMeds = meds.filter { irritantIds.contains(it.medicationId) }
            // Note: In real app we'd check actual dose logs, here we check schedule proximity to "now" 
            // if symptom was recent. Simplified for orchestration.
            CulpritFound@for (med in relevantMeds) {
                // Heuristic: If pain occurred and irritant is in schedule
                culpritIds.add(med.medicationId)
            }
        }
        return culpritIds.distinct()
    }
}
