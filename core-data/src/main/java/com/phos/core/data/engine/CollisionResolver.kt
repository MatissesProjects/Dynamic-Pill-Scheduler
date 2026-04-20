package com.phos.core.data.engine

import com.phos.core.data.model.MedicationRecord
import kotlin.math.abs

data class Collision(
    val medicationA: MedicationRecord,
    val medicationB: MedicationRecord,
    val reason: String
)

class CollisionResolver {
    
    // Example rules: medicationId to set of conflicting medicationIds
    private val conflictRules = mapOf(
        "fiber_supplements" to setOf("bp_medication", "heart_medication")
    )
    
    private val MIN_GAP_MILLIS = 2 * 60 * 60 * 1000 // 2 hours

    /**
     * Checks for collisions between a list of medications based on their scheduled offsets.
     */
    fun findCollisions(medications: List<MedicationRecord>): List<Collision> {
        val collisions = mutableListOf<Collision>()
        for (i in medications.indices) {
            for (j in i + 1 until medications.size) {
                val medA = medications[i]
                val medB = medications[j]
                
                if (areConflicting(medA, medB)) {
                    val gap = abs(medA.frequencyOffset - medB.frequencyOffset)
                    if (gap < MIN_GAP_MILLIS) {
                        collisions.add(Collision(medA, medB, "Sponge Effect: Meds taken too close together (< 2 hours)"))
                    }
                }
            }
        }
        return collisions
    }

    private fun areConflicting(medA: MedicationRecord, medB: MedicationRecord): Boolean {
        return conflictRules[medA.medicationId]?.contains(medB.medicationId) == true ||
               conflictRules[medB.medicationId]?.contains(medA.medicationId) == true
    }
    
    /**
     * Suggests a new offset for medication B to resolve the collision.
     */
    fun suggestResolution(collision: Collision): Long {
        // Simple strategy: push medB further out
        return collision.medicationA.frequencyOffset + MIN_GAP_MILLIS
    }
}
