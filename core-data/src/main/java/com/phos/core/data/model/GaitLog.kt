package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Tracks neuromotor gait metrics from Wear OS 5 / Health Connect.
 * Used to detect medication side effects like ataxia or dizziness.
 */
@Entity(tableName = "gait_logs")
data class GaitLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val strideLengthMeters: Double,
    val cadenceSpm: Double,
    val groundContactTimeMs: Double? = null,
    val verticalOscillationCm: Double? = null,
    val verticalRatio: Double? = null,
    val timestamp: Instant = Instant.now(),
    val source: String? = "HealthConnect"
)
