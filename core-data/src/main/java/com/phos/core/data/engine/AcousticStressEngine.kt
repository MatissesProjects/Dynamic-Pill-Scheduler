package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Instant

class AcousticStressEngine {

    /**
     * Analyzes current ambient noise levels against medication profile and physiological risks.
     */
    fun analyzeAcousticStress(
        latestDb: Double,
        activeMedications: List<MedicationRecord>,
        biometricLogs: List<BiometricLog>
    ): AcousticInsight? {
        val riskLevel = when {
            latestDb > 100 -> AcousticRiskLevel.CRITICAL
            latestDb > 85 -> AcousticRiskLevel.HIGH
            latestDb > 70 -> AcousticRiskLevel.MODERATE
            else -> AcousticRiskLevel.LOW
        }

        if (riskLevel == AcousticRiskLevel.LOW) return null

        // Check if user is on anti-hypertensive or stress-sensitive meds
        val hasHypeMed = activeMedications.any { 
            it.name.contains("Lisinopril", ignoreCase = true) || 
            it.name.contains("Metoprolol", ignoreCase = true) ||
            it.name.contains("Amlodipine", ignoreCase = true)
        }

        // Check for physiological stress (elevated HR compared to baseline)
        val recentHr = biometricLogs.filter { it.type == BiometricType.HEART_RATE }.take(5).map { it.value }.average()
        val isPhysicallyStressed = recentHr > 90.0

        return when {
            riskLevel == AcousticRiskLevel.CRITICAL -> AcousticInsight(
                title = "Critical Acoustic Stress",
                description = "Ambient noise levels exceed 100dB. This poses immediate cardiovascular risk and may significantly blunt the effect of your medications. Please relocate to a quieter environment immediately.",
                riskLevel = riskLevel
            )
            riskLevel == AcousticRiskLevel.HIGH && hasHypeMed -> AcousticInsight(
                title = "Acoustic Shielding Required",
                description = "High noise levels detected (>85dB). Studies show this can elevate BP by 5-10mmHg, counteracting your ${activeMedications.firstOrNull { it.name.contains("Lisinopril") || it.name.contains("Metoprolol") }?.name ?: "medications"}. Seek quiet or use noise cancellation.",
                riskLevel = riskLevel
            )
            riskLevel == AcousticRiskLevel.MODERATE && isPhysicallyStressed -> AcousticInsight(
                title = "Acoustic Tension Detected",
                description = "Moderate noise pollution combined with elevated heart rate detected. Consider a 5-minute silence break to restore autonomic balance.",
                riskLevel = riskLevel
            )
            else -> null
        }
    }
}
