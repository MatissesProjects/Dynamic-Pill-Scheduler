package com.phos.core.intelligence

import com.phos.core.data.dao.BiometricDao
import com.phos.core.data.engine.DigitalTwinEngine
import com.phos.core.data.model.BiometricType
import java.time.Instant
import java.time.temporal.ChronoUnit

class DigitalTwinIntelligence(
    private val biometricDao: BiometricDao,
    private val digitalTwinEngine: DigitalTwinEngine
) {
    /**
     * Analyzes recent biometric data post-dose and returns an AI insight if an anomaly is detected.
     */
    suspend fun checkPostDoseResponse(medicationId: String, medicationName: String, doseTimestamp: Instant): String? {
        val expected = digitalTwinEngine.getExpectedResponse(medicationId) ?: return null
        
        // Fetch last 2 hours of HR data
        val recentHrLogs = biometricDao.getLogsSince(BiometricType.HEART_RATE, doseTimestamp)
            .filter { it.timestamp.isBefore(doseTimestamp.plus(2, ChronoUnit.HOURS)) }
        
        if (recentHrLogs.isEmpty()) return null
        
        val actualHr = recentHrLogs.map { it.value }.average()
        
        if (digitalTwinEngine.isAnomaly(actualHr, expected.first)) {
            return buildAnomalyPrompt(medicationName, expected.first, actualHr)
        }
        
        return null
    }

    private fun buildAnomalyPrompt(medicationName: String, expected: Double, actual: Double): String {
        return """
            Digital Twin Alert: Unusual response to $medicationName detected.
            
            Model Expected Heart Rate: ${String.format("%.1f", expected)} BPM
            Actual Observed Heart Rate: ${String.format("%.1f", actual)} BPM
            
            AI Insight: The physiological response to $medicationName is deviating from your baseline. This might indicate low absorption or a change in metabolic response. Monitor for recurring patterns.
        """.trimIndent()
    }
}
