package com.phos.core.data.engine

import com.phos.core.data.model.TemporalAnchor
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class BetaBlockerSafetyEngineTest {

    private val engine = BetaBlockerSafetyEngine()

    @Test
    fun `test detectBradycardia triggers on low HR`() {
        val anchor = TemporalAnchor("2026-04-27", Instant.now().toEpochMilli(), "Manual")
        val hrSamples = listOf(60L, 55L, 48L, 52L)
        
        val result = engine.detectBradycardia(anchor, hrSamples)
        
        assertNotNull(result)
        assertEquals(BetaBlockerInsightType.BRADYCARDIA, result?.type)
        assertTrue(result?.description?.contains("48 BPM") == true)
    }

    @Test
    fun `test detectBradycardia returns null on normal HR`() {
        val anchor = TemporalAnchor("2026-04-27", Instant.now().toEpochMilli(), "Manual")
        val hrSamples = listOf(60L, 55L, 52L, 58L)
        
        val result = engine.detectBradycardia(anchor, hrSamples)
        
        assertNull(result)
    }

    @Test
    fun `test detectFatigueSlump triggers when HR is 15 percent below average`() {
        val dailyAvgHr = 70.0
        val slumpHrSamples = listOf(58L, 59L, 57L) // Average is 58.0, which is < 59.5 (70 * 0.85)
        
        val result = engine.detectFatigueSlump(Instant.now().toEpochMilli(), dailyAvgHr, slumpHrSamples)
        
        assertNotNull(result)
        assertEquals(BetaBlockerInsightType.FATIGUE_SLUMP, result?.type)
    }

    @Test
    fun `test detectFatigueSlump does not trigger on normal slump HR`() {
        val dailyAvgHr = 70.0
        val slumpHrSamples = listOf(65L, 68L, 66L) // Average is 66.3, which is > 59.5
        
        val result = engine.detectFatigueSlump(Instant.now().toEpochMilli(), dailyAvgHr, slumpHrSamples)
        
        assertNull(result)
    }

    @Test
    fun `test suggestOxygenationBout returns reminder for slump`() {
        val slumpInsight = BetaBlockerInsight(
            type = BetaBlockerInsightType.FATIGUE_SLUMP,
            title = "Slump",
            description = "Desc"
        )
        
        val result = engine.suggestOxygenationBout(slumpInsight)
        
        assertNotNull(result)
        assertEquals(BetaBlockerInsightType.OXYGENATION_REMINDER, result?.type)
        assertTrue(result?.description?.contains("5-minute light activity") == true)
    }
}
