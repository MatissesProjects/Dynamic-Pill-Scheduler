package com.phos.core.data.engine

import com.phos.core.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class SafetyAggregatorTest {

    private val aggregator = SafetyAggregator()

    @Test
    fun `perfect conditions return GREEN`() {
        val status = aggregator.calculateStatus(
            adherenceScore = 1.0,
            hfInsight = HFDecompensationInsight("2026-04-30", HFRiskLevel.STABLE, 0.1, "Stable", "Justification"),
            betaBlockerInsights = emptyList(),
            activeCollisions = 0
        )
        assertEquals(SafetyStatus.GREEN, status)
    }

    @Test
    fun `minor non-adherence returns YELLOW`() {
        val status = aggregator.calculateStatus(
            adherenceScore = 0.9, // < 0.95
            hfInsight = null,
            betaBlockerInsights = emptyList(),
            activeCollisions = 0
        )
        assertEquals(SafetyStatus.YELLOW, status)
    }

    @Test
    fun `critical HF risk returns RED`() {
        val status = aggregator.calculateStatus(
            adherenceScore = 1.0,
            hfInsight = HFDecompensationInsight("2026-04-30", HFRiskLevel.CRITICAL, 0.8, "Critical", "Justification"),
            betaBlockerInsights = emptyList(),
            activeCollisions = 0
        )
        assertEquals(SafetyStatus.RED, status)
    }

    @Test
    fun `any beta blocker insight returns YELLOW`() {
        val status = aggregator.calculateStatus(
            adherenceScore = 1.0,
            hfInsight = null,
            betaBlockerInsights = listOf(BetaBlockerInsight(BetaBlockerInsightType.FATIGUE_SLUMP, "Slump", "Description", false)),
            activeCollisions = 0
        )
        assertEquals(SafetyStatus.YELLOW, status)
    }

    @Test
    fun `critical beta blocker insight returns RED`() {
        val status = aggregator.calculateStatus(
            adherenceScore = 1.0,
            hfInsight = null,
            betaBlockerInsights = listOf(BetaBlockerInsight(BetaBlockerInsightType.BRADYCARDIA, "Brady", "Description", true)),
            activeCollisions = 0
        )
        assertEquals(SafetyStatus.RED, status)
    }

    @Test
    fun `multiple collisions return RED`() {
        val status = aggregator.calculateStatus(
            adherenceScore = 1.0,
            hfInsight = null,
            betaBlockerInsights = emptyList(),
            activeCollisions = 2
        )
        assertEquals(SafetyStatus.RED, status)
    }
}
