package com.phos.core.intelligence

import com.phos.core.data.model.BioBaseline
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class BioVelocityEngineTest {

    private val nanoEngine: GeminiNanoEngine = mock()
    private val engine = BioVelocityEngine(nanoEngine)

    @Test
    fun `calculateBioVelocity - neutral state`() {
        val baseline = BioBaseline(
            baselineHrv = 50.0,
            baselineRhr = 60.0,
            baselineSleepConsistency = 0.8
        )
        
        val log = engine.calculateBioVelocity(
            birthYear = 1990, // 36 in 2026
            currentHrv = 50.0,
            currentRhr = 60.0,
            baseline = baseline,
            adherenceRate = 0.8 // Neutral
        )
        
        assertEquals(36.0, log.chronologicalAge, 0.1)
        assertEquals(36.0, log.biologicalAge, 0.1)
        assertEquals(1.0, log.paceOfAging, 0.1)
    }

    @Test
    fun `calculateBioVelocity - improved state with high adherence`() {
        val baseline = BioBaseline(
            baselineHrv = 50.0,
            baselineRhr = 60.0,
            baselineSleepConsistency = 0.8
        )
        
        val log = engine.calculateBioVelocity(
            birthYear = 1990,
            currentHrv = 60.0, // Improved HRV
            currentRhr = 55.0, // Improved RHR
            baseline = baseline,
            adherenceRate = 1.0 // Perfect adherence
        )
        
        assertTrue("Pace should be less than 1.0, was ${log.paceOfAging}", log.paceOfAging < 1.0)
        assertTrue("Bio age should be less than chrono age", log.biologicalAge < log.chronologicalAge)
        assertTrue("Adherence impact should be positive", log.adherenceImpact > 0)
    }

    @Test
    fun `calculateBioVelocity - declined state`() {
        val baseline = BioBaseline(
            baselineHrv = 50.0,
            baselineRhr = 60.0,
            baselineSleepConsistency = 0.8
        )
        
        val log = engine.calculateBioVelocity(
            birthYear = 1990,
            currentHrv = 40.0, // Declined HRV
            currentRhr = 70.0, // Declined RHR
            baseline = baseline,
            adherenceRate = 0.5 // Poor adherence
        )
        
        assertTrue("Pace should be greater than 1.0, was ${log.paceOfAging}", log.paceOfAging > 1.0)
        assertTrue("Bio age should be greater than chrono age", log.biologicalAge > log.chronologicalAge)
    }
}
