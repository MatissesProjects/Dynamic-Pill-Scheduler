package com.phos.core.intelligence

import com.phos.core.data.model.SleepSubjectiveLog
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class SleepCalibrationEngineTest {

    private val engine = SleepCalibrationEngine()

    @Test
    fun `test calibrate detects discrepancy`() {
        val subjective = SleepSubjectiveLog(
            reportedQuality = 2, // Very bad
            restfulnessRating = 3,
            morningMood = "Exhausted",
            date = "2026-04-25"
        )
        
        // Fitbit says 8 hours (Good), but user feels bad.
        val insight = engine.calibrate(8 * 3600000L, subjective)
        
        assertNotNull(insight)
        assertEquals("Sleep Discrepancy Detected", insight?.title)
        assertTrue(insight?.needsScheduleAudit == true)
    }

    @Test
    fun `test calibrate handles verified exhaustion`() {
        val subjective = SleepSubjectiveLog(
            reportedQuality = 3,
            restfulnessRating = 3,
            morningMood = "Tired",
            date = "2026-04-25"
        )
        
        // Both say bad (only 4 hours)
        val insight = engine.calibrate(4 * 3600000L, subjective)
        
        assertNotNull(insight)
        assertEquals("Restorative Recovery Needed", insight?.title)
    }

    @Test
    fun `test calibrate returns null for good sleep`() {
        val subjective = SleepSubjectiveLog(
            reportedQuality = 9,
            restfulnessRating = 9,
            morningMood = "Alert",
            date = "2026-04-25"
        )
        
        val insight = engine.calibrate(8 * 3600000L, subjective)
        
        assertNull(insight)
    }
}
