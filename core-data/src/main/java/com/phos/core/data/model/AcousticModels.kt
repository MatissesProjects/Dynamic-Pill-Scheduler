package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "acoustic_logs")
data class AcousticLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val decibels: Double,
    val durationMillis: Long,
    val environmentTag: String? = null
)

data class AcousticInsight(
    val title: String,
    val description: String,
    val riskLevel: AcousticRiskLevel,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AcousticRiskLevel {
    LOW,        // < 70 dB
    MODERATE,   // 70-85 dB
    HIGH,       // 85-100 dB
    CRITICAL    // > 100 dB
}
