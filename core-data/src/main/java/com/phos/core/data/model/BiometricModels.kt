package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Raw biometric data from Stream A (Passive).
 */
@Entity(tableName = "biometric_logs")
data class BiometricLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: BiometricType,
    val value: Double,
    val timestamp: Instant = Instant.now()
)

enum class BiometricType {
    HEART_RATE,
    HRV,
    SPO2,
    COGNITIVE_FLUIDITY,
    BRAIN_FOG_INDEX,
    SKIN_TEMPERATURE
}

/**
 * Modeled baseline data for a specific medication.
 * Used for Digital Twin anomaly detection.
 */
@Entity(tableName = "biometric_baselines")
data class BiometricBaseline(
    @PrimaryKey val medicationId: String,
    val averageHrPostDose: Double,
    val averageHrvPostDose: Double,
    val sampleSize: Int, // Number of doses this baseline is built on
    val lastUpdated: Instant = Instant.now()
)
