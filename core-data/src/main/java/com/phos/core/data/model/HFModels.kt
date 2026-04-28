package com.phos.core.data.model

import java.time.Instant

enum class HFRiskLevel {
    STABLE,
    WATCH,
    ELEVATED,
    CRITICAL
}

data class HFTrendData(
    val avgRespiratoryRate: Double,
    val avgOxygenSaturation: Double,
    val avgRestingHeartRate: Double,
    val avgHrv: Double,
    val trendPeriodDays: Int = 7
)

data class HFDecompensationInsight(
    val date: String,
    val riskLevel: HFRiskLevel,
    val fluidProxyScore: Double, // 0.0 to 1.0
    val summary: String,
    val justification: String,
    val suggestedDiureticAdjustment: String? = null,
    val timestamp: Instant = Instant.now()
)

data class NocturnalRespiratoryMetric(
    val timestamp: Instant,
    val respiratoryRate: Double,
    val sleepPosition: SleepPosition, // FLAT, PROPPED_UP
    val oxygenSaturation: Double
)

enum class SleepPosition {
    FLAT,
    PROPPED_UP
}

data class CongestionInsight(
    val date: String,
    val orthopneaDetected: Boolean,
    val rrDeltaFlatVsPropped: Double,
    val summary: String,
    val recommendation: String? = null
)
