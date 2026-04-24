package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Tracks a user-defined health goal.
 * e.g., targetSymptom = "stomach pain", targetTimeOffset = 14400000L (4 hours after wake)
 */
@Entity(tableName = "health_goals")
data class HealthGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String, // e.g., "Prevent stomach pain at 4 AM"
    val targetSymptom: String, // e.g., "stomach pain", "gas"
    val targetTimeOffset: Long?, // Time relative to T-Wake if specific, or null
    val targetTimeOfDay: String?, // e.g. "04:00" or null
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now()
)
