package com.phos.core.data.engine

import com.phos.core.data.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class REMSafetyEngineTest {

    private val engine = REMSafetyEngine()

    @Test
    fun `test calculateFragmentationIndex detects spikes`() {
        val now = Instant.now()
        val samples = listOf(
            SleepStageSample(now.minus(60, ChronoUnit.MINUTES), now.minus(50, ChronoUnit.MINUTES), SleepStage.LIGHT),
            SleepStageSample(now.minus(50, ChronoUnit.MINUTES), now.minus(45, ChronoUnit.MINUTES), SleepStage.AWAKE), // Spike
            SleepStageSample(now.minus(45, ChronoUnit.MINUTES), now.minus(30, ChronoUnit.MINUTES), SleepStage.REM),
            SleepStageSample(now.minus(30, ChronoUnit.MINUTES), now.minus(25, ChronoUnit.MINUTES), SleepStage.AWAKE), // Spike
            SleepStageSample(now.minus(25, ChronoUnit.MINUTES), now.minus(10, ChronoUnit.MINUTES), SleepStage.REM)
        )
        
        val result = engine.calculateFragmentationIndex(samples)
        
        assertEquals(2, result.awakeSpikeCount)
        assertEquals(30, result.totalRemMinutes)
        assertTrue(result.fragmentationIndex > 0.0)
    }

    @Test
    fun `test buildRestorationAudit includes dream correlation`() {
        val insight = REMFragmentationInsight(
            date = "2026-04-27",
            fragmentationIndex = 0.8,
            awakeSpikeCount = 4,
            totalRemMinutes = 40,
            isExcessive = true
        )
        
        val result = engine.buildRestorationAudit(insight, dreamIntensity = 9)
        
        assertEquals(20, result.remStabilityScore)
        assertTrue(result.restorationMessage.contains("High REM-Rebound"))
        assertTrue(result.restorationMessage.contains("vivid dreams"))
    }
}
