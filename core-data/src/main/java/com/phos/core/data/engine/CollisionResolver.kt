package com.phos.core.data.engine

import com.phos.core.data.model.*
import kotlin.math.abs

data class Collision(
    val medicationA: MedicationRecord,
    val otherId: String, // Can be medicationId or foodId
    val otherName: String,
    val reason: String,
    val severity: InteractionSeverity = InteractionSeverity.WARNING
)

class CollisionResolver(
    private val interactionRules: List<InteractionRule> = emptyList(),
    private val absorptionRules: List<AbsorptionRule> = emptyList(),
    private val sideEffectRules: List<SideEffectRule> = emptyList()
) {

    /**
     * Finds spacing suggestions for medications with specific absorption requirements.
     */
    fun findAbsorptionSpacingSuggestions(medications: List<MedicationRecord>): List<String> {
        val suggestions = mutableListOf<String>()
        for (med in medications) {
            val rule = absorptionRules.find { med.medicationId.equals(it.medicationId, ignoreCase = true) }
            if (rule != null) {
                val conflicts = medications.filter { 
                    it.id != med.id && 
                    abs(it.frequencyOffset - med.frequencyOffset) < (rule.requiredGapMinutes * 60 * 1000L)
                }
                if (conflicts.isNotEmpty()) {
                    suggestions.add(rule.reason)
                }
            }
        }
        return suggestions
    }

    /**
     * Gets side effect warnings for current medications.
     */
    fun getSideEffectAlerts(medications: List<MedicationRecord>): List<SideEffectRule> {
        return medications.flatMap { med ->
            sideEffectRules.filter { med.medicationId.equals(it.medicationId, ignoreCase = true) }
        }
    }

    fun findMedicationCollisions(medications: List<MedicationRecord>): List<Collision> {
        val collisions = mutableListOf<Collision>()
        for (i in medications.indices) {
            for (j in i + 1 until medications.size) {
                val medA = medications[i]
                val medB = medications[j]
                
                val rule = findRule(medA.medicationId, medB.medicationId)
                if (rule != null) {
                    val gap = abs(medA.frequencyOffset - medB.frequencyOffset)
                    if (gap < rule.gapMillis) {
                        collisions.add(Collision(
                            medA, 
                            medB.medicationId, 
                            medB.name,
                            rule.reason,
                            rule.severity
                        ))
                    }
                }
            }
        }
        return collisions
    }

    fun findFoodCollisions(
        medications: List<MedicationRecord>, 
        foodLogs: List<FoodLog>,
        twakeMillis: Long
    ): List<Collision> {
        val collisions = mutableListOf<Collision>()
        for (med in medications) {
            val medAbsoluteTime = twakeMillis + med.frequencyOffset
            
            for (food in foodLogs) {
                val rule = findRule(med.medicationId, food.foodId)
                if (rule != null) {
                    val gap = abs(medAbsoluteTime - food.timestamp)
                    if (gap < rule.gapMillis) {
                        collisions.add(Collision(
                            med,
                            food.foodId,
                            food.name,
                            rule.reason,
                            rule.severity
                        ))
                    }
                }
            }
        }
        return collisions
    }

    private fun findRule(idA: String, idB: String): InteractionRule? {
        return interactionRules.find { 
            (idA.lowercase().contains(it.sourceId) && idB.lowercase().contains(it.targetId)) || 
            (idB.lowercase().contains(it.sourceId) && idA.lowercase().contains(it.targetId)) 
        }
    }
    
    fun suggestResolution(medication: MedicationRecord, collisionSourceTime: Long, requiredGap: Long): Long {
        return collisionSourceTime + requiredGap
    }
}
