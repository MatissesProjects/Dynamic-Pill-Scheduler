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

/**
 * Specifically for rules about when to take a med relative to others for optimal gut health/absorption.
 */
@Entity(tableName = "absorption_rules")
data class AbsorptionRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: String,
    val requiredGapMinutes: Int,
    val reason: String,
    val mustBeBefore: Boolean = true // True if this med must be taken BEFORE others
)

/**
 * For general side effects to watch out for when taking specific meds.
 */
@Entity(tableName = "side_effect_rules")
data class SideEffectRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: String,
    val sideEffect: String,
    val advice: String
)
