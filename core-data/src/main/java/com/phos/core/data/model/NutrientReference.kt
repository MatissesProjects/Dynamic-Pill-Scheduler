package com.phos.core.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local database of common foods and their nutrient profiles.
 */
@Entity(tableName = "nutrient_references")
data class NutrientReference(
    @PrimaryKey val foodId: String,
    val name: String,
    @Embedded(prefix = "ref_")
    val nutrients: NutrientFacts,
    val bestSources: String? = null // e.g., "Found in: Local grocery, Quick Meals"
)

/**
 * Links medications to nutrients they commonly deplete.
 */
@Entity(tableName = "medication_depletions")
data class MedicationInducedDepletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationNamePattern: String, // e.g., "statin", "metformin"
    val depletedNutrient: String, // e.g., "CoQ10", "Vitamin B12"
    val advice: String,
    val foodSuggestions: List<String> = emptyList()
)
