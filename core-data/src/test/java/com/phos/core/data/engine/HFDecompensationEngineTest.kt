package com.phos.core.data.engine

import com.phos.core.data.model.HFRiskLevel
import com.phos.core.data.model.HFTrendData
import org.junit.Assert.*
import org.junit.Test

class HFDecompensationEngineTest {

    private val engine = HFDecompensationEngine()

    private val baseline = HFTrendData(
        avgRespiratoryRate = 16.0,
        avgOxygenSaturation = 98.0,
        avgRestingHeartRate = 65.0,
        avgHrv = 45.0
    )

    @Test
    fun `calculateFluidProxy returns STABLE for baseline metrics`() {
        val current = baseline.copy()
        val result = engine.calculateFluidProxy(current, baseline)
        
        assertEquals(HFRiskLevel.STABLE, result.riskLevel)
        assertEquals(0.0, result.fluidProxyScore, 0.01)
        assertNull(result.suggestedDiureticAdjustment)
    }

    @Test
    fun `calculateFluidProxy returns CRITICAL for high respiratory rate and low SpO2`() {
        val current = HFTrendData(
            avgRespiratoryRate = 22.0, // >30% increase
            avgOxygenSaturation = 89.0, // <90%
            avgRestingHeartRate = 85.0,
            avgHrv = 30.0
        )
        val result = engine.calculateFluidProxy(current, baseline)
        
        assertEquals(HFRiskLevel.CRITICAL, result.riskLevel)
        assertTrue(result.fluidProxyScore > 0.5)
        assertNotNull(result.suggestedDiureticAdjustment)
    }

    @Test
    fun `calculateFluidProxy returns ELEVATED for significant deviations`() {
        val current = HFTrendData(
            avgRespiratoryRate = 19.0, // ~18% increase
            avgOxygenSaturation = 93.0, // <94%
            avgRestingHeartRate = 75.0,
            avgHrv = 35.0
        )
        val result = engine.calculateFluidProxy(current, baseline)
        
        assertEquals(HFRiskLevel.ELEVATED, result.riskLevel)
        assertTrue(result.fluidProxyScore > 0.25)
        assertNotNull(result.suggestedDiureticAdjustment)
    }

    @Test
    fun `getSafetyTighteningMillis returns correct values`() {
        assertEquals(7200000L, engine.getSafetyTighteningMillis(HFRiskLevel.CRITICAL))
        assertEquals(3600000L, engine.getSafetyTighteningMillis(HFRiskLevel.ELEVATED))
        assertEquals(0L, engine.getSafetyTighteningMillis(HFRiskLevel.WATCH))
        assertEquals(0L, engine.getSafetyTighteningMillis(HFRiskLevel.STABLE))
    }
}
