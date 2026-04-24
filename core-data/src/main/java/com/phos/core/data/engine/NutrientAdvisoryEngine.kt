package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Instant
import kotlin.math.abs

data class NutrientAdvisory(
    val isGoodIdea: Boolean,
    val summary: String,
    val warnings: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val bestTimeToEatMillis: Long? = null
)

class NutrientAdvisoryEngine(
    private val collisionResolver: CollisionResolver
) {

    /**
     * Checks if current medications are known to deplete specific nutrients.
     */
    fun findDepletionWarnings(
        scheduledMeds: List<MedicationRecord>,
        depletionRules: List<MedicationInducedDepletion>
    ): List<String> {
        val warnings = mutableListOf<String>()
        scheduledMeds.forEach { med ->
            depletionRules.forEach { rule ->
                if (med.name.contains(rule.medicationNamePattern, ignoreCase = true)) {
                    warnings.add("DEPLETION: ${med.name} may deplete ${rule.depletedNutrient}. ${rule.advice}")
                }
            }
        }
        return warnings
    }

    /**
     * Evaluates if eating a specific food (with extracted nutrients) is a good idea right now.
     */
    fun evaluateFood(
        foodName: String,
        nutrients: NutrientFacts,
        userAllergens: List<AllergenProfile>,
        scheduledMeds: List<MedicationRecord>,
        twakeMillis: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): NutrientAdvisory {
        val warnings = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        // 1. Allergen Check
        userAllergens.forEach { allergen ->
            val matchFound = nutrients.ingredients.any { it.contains(allergen.displayName, ignoreCase = true) || it.contains(allergen.allergenId, ignoreCase = true) } ||
                             foodName.contains(allergen.displayName, ignoreCase = true) ||
                             foodName.contains(allergen.allergenId, ignoreCase = true)
            
            if (matchFound) {
                warnings.add("ALLERGEN DETECTED: Contains ${allergen.displayName} (${allergen.severity} risk).")
            }
        }

        // 2. Medication Interference (e.g. Calcium)
        if (nutrients.calciumMg > 100) {
            val interferingMeds = scheduledMeds.filter { med ->
                val name = med.name.lowercase()
                name.contains("cipro") || name.contains("levo") || name.contains("tetra")
            }

            interferingMeds.forEach { med ->
                val medTime = twakeMillis + med.frequencyOffset
                val gap = abs(currentTimeMillis - medTime)
                if (gap < 2 * 3600000L) { // Less than 2 hours
                    warnings.add("INTERFERENCE: High Calcium in this food can block absorption of ${med.name}. Suggest 2h gap.")
                    suggestions.add("Best to wait until at least 2 hours after your ${med.name} dose.")
                }
            }
        }

        // 3. Overall Summary
        val isGoodIdea = warnings.isEmpty()
        val summary = if (isGoodIdea) {
            "Looks good! This food aligns with your current medication schedule."
        } else {
            "Caution recommended. Found ${warnings.size} potential issues."
        }

        return NutrientAdvisory(
            isGoodIdea = isGoodIdea,
            summary = summary,
            warnings = warnings,
            suggestions = suggestions,
            bestTimeToEatMillis = if (!isGoodIdea) currentTimeMillis + 3 * 3600000L else currentTimeMillis
        )
    }
}
