package com.phos.core.data.engine

import com.phos.core.data.model.EfficiencyInsight
import com.phos.core.data.model.PulsePowerMetric
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PulsePowerEngine {

    /**
     * T40: Cardiac Output Efficiency (Pulse-Power Twin)
     * Calculates efficiency as Watts per BPM.
     */
    fun calculateEfficiency(
        recentMetrics: List<PulsePowerMetric>,
        baselineEfficiency: Double
    ): EfficiencyInsight {
        if (recentMetrics.isEmpty()) {
            return EfficiencyInsight(
                date = getTodayDate(),
                avgWattsPerBpm = 0.0,
                efficiencyTrend = 0.0,
                summary = "Insufficient data for efficiency calculation."
            )
        }

        val avgEfficiency = recentMetrics.map { it.wattsPerBpm }.average()
        val trend = if (baselineEfficiency > 0) {
            ((avgEfficiency - baselineEfficiency) / baselineEfficiency) * 100
        } else {
            0.0
        }

        val summary = when {
            trend > 5.0 -> "Increased Cardiac Efficiency: Higher power output per heartbeat detected."
            trend < -5.0 -> "Decreased Cardiac Efficiency: Myocardial strain or fatigue possible."
            else -> "Stable Cardiac Efficiency: Performance aligned with baseline."
        }

        val recommendation = if (trend < -10.0) {
            "Efficiency drop detected. Consider increasing e-bike assist level to reduce myocardial load."
        } else null

        return EfficiencyInsight(
            date = getTodayDate(),
            avgWattsPerBpm = avgEfficiency,
            efficiencyTrend = trend,
            summary = summary,
            recommendation = recommendation
        )
    }

    private fun getTodayDate(): String {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(Instant.now())
    }
}
