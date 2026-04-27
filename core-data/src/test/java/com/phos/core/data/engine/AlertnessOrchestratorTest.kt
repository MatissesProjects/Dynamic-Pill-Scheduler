package com.phos.core.data.engine

import androidx.health.connect.client.records.SleepSessionRecord
import com.phos.core.data.model.SleepSubjectiveLog
import com.phos.core.data.model.MetabolicLoadLog
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import java.time.Instant

class AlertnessOrchestratorTest {

    private val orchestrator = AlertnessOrchestrator()

    @Test
    fun `test buildPredictionPrompt contains necessary data`() {
        val now = Instant.now()
        val tWake = now.minusSeconds(28800).toEpochMilli() // 8 hours ago
        
        val recentSleep = listOf(mock<SleepSessionRecord>())
        val subjective = listOf(SleepSubjectiveLog(reportedQuality = 3, restfulnessRating = 2, morningMood = "Exhausted", date = "2026-04-25"))
        val exercise = emptyList<MetabolicLoadLog>()
        
        val prompt = orchestrator.buildPredictionPrompt(tWake, recentSleep, subjective, exercise)
        
        assertTrue(prompt.contains("Hours since wake: 8"))
        assertTrue(prompt.contains("Quality: 3/10"))
        assertTrue(prompt.contains("Predict if a daytime nap is likely"))
    }
}
