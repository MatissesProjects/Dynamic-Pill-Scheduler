package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines a relationship between medications or foods that requires a safety gap.
 */
@Entity(tableName = "interaction_rules")
data class InteractionRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceId: String, // Medication or Food ID (e.g., "grapefruit_juice", "dairy")
    val targetId: String, // Conflicting Medication or Food ID
    val gapMillis: Long,  // Required time gap between the two (in milliseconds)
    val reason: String,   // Explanation (e.g., "Grapefruit inhibits metabolism of Statins")
    val severity: InteractionSeverity = InteractionSeverity.WARNING
)

enum class InteractionSeverity {
    INFO,
    WARNING,
    CRITICAL
}
