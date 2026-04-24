package com.phos.core.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks recent food intake to check against medication schedules.
 */
@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: String,   // Unique identifier for the food type (e.g., "grapefruit", "dairy")
    val name: String,     // Display name (e.g., "Grapefruit Juice")
    val timestamp: Long,  // When the food was consumed
    
    @Embedded(prefix = "nutrients_")
    val nutrients: NutrientFacts? = null
)
