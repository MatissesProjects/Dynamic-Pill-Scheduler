package com.phos.core.intelligence

import com.phos.core.data.model.*
import java.time.Instant

data class EnvironmentalInsight(
    val title: String,
    val description: String,
    val riskLevel: String, // "LOW", "MODERATE", "HIGH"
    val timestamp: Long = System.currentTimeMillis()
)

class EnvironmentalCorrelationEngine {

    /**
     * T45: Environmental Respiratory Strain
     * Correlates physiological strain (SpO2 drops, RR increases) with external air quality.
     */
    fun correlateRespiratoryStrain(
        respiratoryMetrics: List<NocturnalRespiratoryMetric>,
        environmentalLogs: List<EnvironmentalLog>
    ): EnvironmentalInsight? {
        if (respiratoryMetrics.isEmpty() || environmentalLogs.isEmpty()) return null

        val avgSpO2 = respiratoryMetrics.map { it.oxygenSaturation }.average()
        val latestEnv = environmentalLogs.maxByOrNull { it.timestamp } ?: return null

        val isAqiPoor = latestEnv.aqi > 100 // Threshold for "Unhealthy for Sensitive Groups"
        val isPm25High = latestEnv.pm25 > 35.0 // µg/m³ threshold
        val isOzoneHigh = latestEnv.ozone > 70.0 // ppb threshold

        if (avgSpO2 < 94.0 && (isAqiPoor || isPm25High || isOzoneHigh)) {
            return EnvironmentalInsight(
                title = "Environmental Respiratory Strain",
                description = "Detected SpO2 drop (Avg: ${"%.1f".format(avgSpO2)}%) correlates with poor external air quality (AQI: ${latestEnv.aqi}). This may be causing nocturnal respiratory strain. We suggest closing windows and running a HEPA filter.",
                riskLevel = "HIGH"
            )
        } else if (isAqiPoor) {
            return EnvironmentalInsight(
                title = "Air Quality Warning",
                description = "External air quality is poor (AQI: ${latestEnv.aqi}). Consider limiting outdoor activity and ensuring indoor air is filtered before sleep.",
                riskLevel = "MODERATE"
            )
        }

        return null
    }
}
