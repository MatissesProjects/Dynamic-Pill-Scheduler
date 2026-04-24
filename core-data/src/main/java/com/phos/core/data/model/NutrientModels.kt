package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents macronutrients and specific micronutrients extracted from a label.
 */
data class NutrientFacts(
    val calories: Int = 0,
    val totalFatG: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val totalCarbsG: Double = 0.0,
    val proteinG: Double = 0.0,
    val calciumMg: Double = 0.0,
    val ironMg: Double = 0.0,
    val ingredients: List<String> = emptyList()
)

/**
 * User's personal allergen profile to check against ingredient lists.
 */
@Entity(tableName = "allergen_profiles")
data class AllergenProfile(
    @PrimaryKey val allergenId: String, // e.g., "dairy", "gluten", "soy"
    val displayName: String,
    val severity: String = "MODERATE" // "MILD", "MODERATE", "SEVERE"
)
