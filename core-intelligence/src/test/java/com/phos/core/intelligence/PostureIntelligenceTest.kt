package com.phos.core.intelligence

import org.junit.Assert.*
import org.junit.Test

class PostureIntelligenceTest {

    private val engine = PostureIntelligence()

    @Test
    fun `detectOrthostaticTransition identifies stand-up event`() {
        val pressureSamples = listOf(1013.25, 1013.10) // 0.15 hPa drop
        val insight = engine.detectOrthostaticTransition(pressureSamples, isOnBetaBlocker = true)
        
        assertNotNull(insight)
        assertEquals("HIGH", insight?.riskLevel)
        assertTrue(insight?.title!!.contains("Rapid Stand-up"))
    }

    @Test
    fun `detectOrthostaticTransition ignores minor pressure changes`() {
        val pressureSamples = listOf(1013.25, 1013.23) // 0.02 hPa drop
        val insight = engine.detectOrthostaticTransition(pressureSamples, isOnBetaBlocker = true)
        
        assertNull(insight)
    }

    @Test
    fun `evaluateDeskPosture identifies slump`() {
        val recommendation = engine.evaluateDeskPosture(35.0)
        assertNotNull(recommendation)
        assertTrue(recommendation?.title!!.contains("Posture Check"))
    }
}
