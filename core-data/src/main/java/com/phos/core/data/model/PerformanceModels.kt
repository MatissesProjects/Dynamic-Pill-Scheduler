package com.phos.core.data.model

import java.time.Instant

data class PulsePowerMetric(
    val timestamp: Instant,
    val powerWatts: Double,
    val heartRateBpm: Double,
    val cadenceRpm: Double? = null
) {
    val wattsPerBpm: Double get() = if (heartRateBpm > 0) powerWatts / heartRateBpm else 0.0
}

data class EfficiencyInsight(
    val date: String,
    val avgWattsPerBpm: Double,
    val efficiencyTrend: Double, // % change vs baseline
    val summary: String,
    val recommendation: String? = null
)
