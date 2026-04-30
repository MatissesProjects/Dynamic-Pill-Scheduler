package com.phos.core.intelligence

import com.phos.core.data.model.*
import java.time.Instant
import java.time.LocalDate
import kotlin.math.max

/**
 * T50: Biological Velocity Engine.
 * Calculates Pace of Aging and Biological Age using HRV and RHR trends.
 */
class BioVelocityEngine(
    private val geminiNano: GeminiNanoEngine
) {

    /**
     * M2: Pace of Aging algorithm.
     * Heuristic-based modeling of biological age vs. chronological age.
     */
    fun calculateBioVelocity(
        birthYear: Int,
        currentHrv: Double,
        currentRhr: Double,
        baseline: BioBaseline,
        adherenceRate: Double // 0.0 to 1.0
    ): BioVelocityLog {
        val chronologicalAge = (LocalDate.now().year - birthYear).toDouble()
        
        // Pace of Aging Heuristic:
        // 1.0 is the baseline (aging at chronological speed).
        // Higher HRV is better (lower pace), Higher RHR is worse (higher pace).
        val hrvFactor = if (currentHrv > 0) (baseline.baselineHrv / currentHrv).coerceIn(0.5, 2.0) else 1.0
        val rhrFactor = if (baseline.baselineRhr > 0) (currentRhr / baseline.baselineRhr).coerceIn(0.5, 2.0) else 1.0
        
        // Base pace is average of biometric factors
        var paceOfAging = (hrvFactor * 0.5 + rhrFactor * 0.5).coerceIn(0.7, 1.5)
        
        // M3: Adherence correlation
        // High adherence (>90%) provides a "longevity bonus" to the pace of aging.
        val adherenceBonus = if (adherenceRate > 0.9) (adherenceRate - 0.9) * 0.5 else 0.0
        paceOfAging -= adherenceBonus
        
        // Clamp pace to realistic bounds
        paceOfAging = paceOfAging.coerceIn(0.6, 2.0)
        
        // Biological Age Calculation:
        // BioAge = ChronoAge + (Pace - 1.0) * Sensitivity
        // Sensitivity of 10.0 means a 0.1 deviation in pace equals a 1-year age delta.
        val biologicalAge = chronologicalAge + (paceOfAging - 1.0) * 10.0 
        
        return BioVelocityLog(
            biologicalAge = max(chronologicalAge - 10.0, biologicalAge), // Limit to -10 years for safety
            chronologicalAge = chronologicalAge,
            paceOfAging = paceOfAging,
            hrvDelta = currentHrv - baseline.baselineHrv,
            rhrDelta = currentRhr - baseline.baselineRhr,
            sleepDelta = 0.0, // Future: Integration with sleep consistency
            adherenceImpact = adherenceBonus * 10.0
        )
    }

    /**
     * M3: Gemini Nano insight generation for bio-velocity trends.
     */
    suspend fun generateVelocityInsight(log: BioVelocityLog): String? {
        val prompt = """
            Generate a short, professional biological health insight based on these metrics:
            - Chronological Age: ${log.chronologicalAge.toInt()}
            - Biological Age: ${"%.1f".format(log.biologicalAge)}
            - Pace of Aging: ${"%.2f".format(log.paceOfAging)} (Target is < 1.0)
            - Adherence Impact: ${"%.2f".format(log.adherenceImpact)} years improved due to medication consistency.
            
            Focus on how their adherence is successfully slowing their biological clock. Keep it under 50 words.
        """.trimIndent()
        return geminiNano.generateResponse(prompt)
    }
}
