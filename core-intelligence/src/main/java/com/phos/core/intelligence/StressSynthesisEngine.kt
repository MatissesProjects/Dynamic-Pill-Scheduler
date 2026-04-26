package com.phos.core.intelligence

import com.phos.core.data.model.SentimentLog
import com.phos.core.data.model.BiometricLog
import com.phos.core.data.model.BiometricType
import java.time.Instant
import java.time.temporal.ChronoUnit

data class BurnoutRisk(
    val score: Float, // 0.0 to 1.0 (1.0 is high risk)
    val isCritical: Boolean,
    val recommendation: String?
)

class StressSynthesisEngine {

    /**
     * Correlates recent sentiment with physiological HRV trends.
     * High Stress + Low HRV = Burnout Risk.
     */
    fun detectBurnoutRisk(
        recentSentiment: List<SentimentLog>,
        recentBiometrics: List<BiometricLog>
    ): BurnoutRisk {
        if (recentSentiment.isEmpty()) return BurnoutRisk(0f, false, null)

        val avgSentiment = recentSentiment.map { it.score }.average().toFloat()
        
        // Filter for HRV (SDNN or RMSSD)
        val hrvLogs = recentBiometrics.filter { it.type == BiometricType.HRV }
        if (hrvLogs.isEmpty()) {
            // Sentiment only fallback
            return if (avgSentiment < -0.5) {
                BurnoutRisk(0.6f, false, "Emotional strain detected. Consider scheduling a short break.")
            } else BurnoutRisk(0f, false, null)
        }

        val avgHrv = hrvLogs.map { it.value }.average()
        
        // Heuristic: If sentiment is < -0.4 AND HRV is < 40ms (typical low for many)
        // Note: Real baseline comparison would be better.
        val sentimentFactor = (1.0 - (avgSentiment + 1.0) / 2.0).coerceIn(0.0, 1.0)
        val hrvFactor = if (avgHrv < 40) 0.8 else 0.2
        
        val riskScore = (sentimentFactor * 0.6 + hrvFactor * 0.4).toFloat()
        
        val recommendation = when {
            riskScore > 0.75 -> "⚠️ High Burnout Risk: Physiological and emotional strain are critically high. We've reserved a 4-hour 'Rest Window' in your timeline. Delaying non-essential tasks is recommended."
            riskScore > 0.5 -> "Moderate Stress Detected: Your biological recovery is slowing. Consider a 1-hour digital detox this evening."
            else -> null
        }

        return BurnoutRisk(
            score = riskScore,
            isCritical = riskScore > 0.75,
            recommendation = recommendation
        )
    }
}
