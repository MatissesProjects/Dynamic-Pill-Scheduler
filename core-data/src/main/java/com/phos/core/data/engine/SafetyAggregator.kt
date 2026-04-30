package com.phos.core.data.engine

import com.phos.core.data.model.*

class SafetyAggregator {

    /**
     * Synthesizes a unified SafetyStatus from multiple physiological and adherence inputs.
     * 
     * @param adherenceScore 0.0 (none) to 1.0 (perfect) for the last 24 hours.
     * @param hfInsight The latest insight from the Heart Failure Decompensation engine.
     * @param betaBlockerInsights List of latest insights from the Beta-Blocker Safety engine.
     * @param activeCollisions Number of currently unresolved medication collisions.
     */
    fun calculateStatus(
        adherenceScore: Double,
        hfInsight: HFDecompensationInsight?,
        betaBlockerInsights: List<BetaBlockerInsight>,
        activeCollisions: Int
    ): SafetyStatus {
        // Red Condition: Critical physiological risk OR severe non-adherence OR multiple collisions.
        val isRed = (adherenceScore < 0.7) ||
                (hfInsight?.riskLevel == HFRiskLevel.CRITICAL) ||
                (betaBlockerInsights.any { it.isCritical }) ||
                (activeCollisions > 1)

        if (isRed) return SafetyStatus.RED

        // Yellow Condition: Elevated risk OR minor non-adherence OR single collision.
        val isYellow = (adherenceScore < 0.95) ||
                (hfInsight?.riskLevel == HFRiskLevel.ELEVATED) ||
                (hfInsight?.riskLevel == HFRiskLevel.WATCH) ||
                (betaBlockerInsights.isNotEmpty()) ||
                (activeCollisions > 0)

        if (isYellow) return SafetyStatus.YELLOW

        // Green Condition: No risks, high adherence, no collisions.
        return SafetyStatus.GREEN
    }
}
