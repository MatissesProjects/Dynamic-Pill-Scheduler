package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

class HFDecompensationEngine {

    /**
     * M1: Sensor-fusion aggregator for fluid accumulation proxy.
     * Combines RR increase, SpO2 decrease, and RHR/HRV trends.
     */
    fun calculateFluidProxy(
        current: HFTrendData,
        baseline: HFTrendData
    ): HFDecompensationInsight {
        // High Risk Signals:
        // 1. Respiratory Rate Increase (>15% from baseline)
        // 2. Oxygen Saturation Decrease (<94% or >3% drop)
        // 3. Resting HR Increase (>10% from baseline)
        // 4. HRV Decrease (>20% from baseline)
        
        val rrIncrease = (current.avgRespiratoryRate / baseline.avgRespiratoryRate).coerceIn(0.5, 2.0)
        val spo2Drop = max(0.0, baseline.avgOxygenSaturation - current.avgOxygenSaturation)
        val rhrIncrease = (current.avgRestingHeartRate / baseline.avgRestingHeartRate).coerceIn(0.5, 2.0)
        val hrvDrop = (baseline.avgHrv / current.avgHrv).coerceIn(0.5, 2.0)
        
        // Weighted Fluid Proxy Score
        val score = ((max(0.0, rrIncrease - 1.0) * 0.4) + 
                     (spo2Drop / 10.0 * 0.3) + 
                     (max(0.0, rhrIncrease - 1.0) * 0.2) + 
                     (max(0.0, hrvDrop - 1.0) * 0.1)).coerceIn(0.0, 1.0)
                     
        val riskLevel = when {
            score > 0.7 || current.avgOxygenSaturation < 90 -> HFRiskLevel.CRITICAL
            score > 0.4 || current.avgOxygenSaturation < 94 -> HFRiskLevel.ELEVATED
            score > 0.2 -> HFRiskLevel.WATCH
            else -> HFRiskLevel.STABLE
        }
        
        val today = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(Instant.now())
        
        val summary = when(riskLevel) {
            HFRiskLevel.CRITICAL -> "Critical Decompensation Warning: Immediate clinical consultation recommended."
            HFRiskLevel.ELEVATED -> "Elevated Fluid Risk: Significant multi-metric deviation detected."
            HFRiskLevel.WATCH -> "Fluid Watch: Minor physiological shifts observed. Monitor weight closely."
            HFRiskLevel.STABLE -> "Stable Cardiac Status: No significant fluid accumulation signals."
        }
        
        val justification = "Based on: RR at ${"%.1f".format(current.avgRespiratoryRate)} (vs ${"%.1f".format(baseline.avgRespiratoryRate)}), SpO2 at ${"%.1f".format(current.avgOxygenSaturation)}%, and RHR at ${"%.0f".format(current.avgRestingHeartRate)} BPM."

        return HFDecompensationInsight(
            date = today,
            riskLevel = riskLevel,
            fluidProxyScore = score,
            summary = summary,
            justification = justification,
            suggestedDiureticAdjustment = if (riskLevel == HFRiskLevel.ELEVATED || riskLevel == HFRiskLevel.CRITICAL) 
                "Tighten diuretic safe-gaps by 2 hours and trigger manual weight check." else null
        )
    }

    /**
     * M3: Safety Tightening logic for diuretics.
     * Returns the number of milliseconds to subtract from current safe-gaps.
     */
    fun getSafetyTighteningMillis(risk: HFRiskLevel): Long {
        return when(risk) {
            HFRiskLevel.CRITICAL -> 7200000L // 2 hours
            HFRiskLevel.ELEVATED -> 3600000L // 1 hour
            else -> 0L
        }
    }
}
