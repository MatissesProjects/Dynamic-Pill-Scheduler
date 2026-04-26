package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Tracks calculated training impulse (TRIMP) and metabolic strain.
 * Used to adjust medication absorption windows and hydration needs.
 */
@Entity(tableName = "metabolic_load_logs")
data class MetabolicLoadLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseSessionId: String?,
    val trimpScore: Double, // Training Impulse based on HR intensity * duration
    val avgHeartRate: Double,
    val durationMinutes: Long,
    val timestamp: Instant = Instant.now(),
    val isHyperMetabolic: Boolean = false
)
