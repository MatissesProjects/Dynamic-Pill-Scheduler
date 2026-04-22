package com.phos.core.data.engine

import com.phos.core.data.model.FoodLog
import com.phos.core.data.model.InteractionRule
import com.phos.core.data.model.InteractionSeverity
import com.phos.core.data.model.MedicationRecord
import kotlin.math.abs

data class Collision(
    val medicationA: MedicationRecord,
    val otherId: String, // Can be medicationId or foodId
    val otherName: String,
    val reason: String,
    val severity: InteractionSeverity = InteractionSeverity.WARNING
)

class CollisionResolver(
    private val rules: List<InteractionRule> = emptyList()
) {
    
    private val DEFAULT_GAP_MILLIS = 2 * 60 * 60 * 1000L // 2 hours default

    /**
     * Checks for collisions between medications.
     */
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

    /**
     * Checks for collisions between medications and recently consumed food.
     * @param twakeMillis The absolute timestamp of today's T-Wake.
     */
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
        return rules.find { 
            (it.sourceId == idA && it.targetId == idB) || 
            (it.sourceId == idB && it.targetId == idA) 
        }
    }
    
    /**
     * Suggests a new absolute timestamp or offset for medication to resolve the collision.
     */
    fun suggestResolution(medication: MedicationRecord, collisionSourceTime: Long, requiredGap: Long): Long {
        return collisionSourceTime + requiredGap
    }
}
