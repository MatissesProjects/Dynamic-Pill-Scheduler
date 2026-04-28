package com.phos.core.intelligence

import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.MedicationRecord
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant

class NeuroLoadEngineTest {

    private lateinit var nanoEngine: GeminiNanoEngine
    private lateinit var engine: NeuroLoadEngine

    @Before
    fun setup() {
        nanoEngine = mock()
        engine = NeuroLoadEngine(nanoEngine)
    }

    @Test
    fun `test calculateFluidity with high latency and fillers`() = runTest {
        val segments = listOf(
            SpeechSegment("I", 1000, 1100),
            SpeechSegment("um", 2000, 2100),
            SpeechSegment("took", 4000, 4100),
            SpeechSegment("ah", 5000, 5100),
            SpeechSegment("meds", 7000, 7100)
        )
        
        whenever(nanoEngine.auditSemanticFluidity(any(), any(), any())).thenReturn("circumlocution: false\nbrain_fog_index: 0.6")
        
        val metrics = engine.analyzeSpeech(segments, "I um took ah meds")
        
        assertTrue(metrics.fluidityScore < 0.7f)
        assertEquals(2, metrics.fillerCount)
        assertEquals(0.6f, metrics.brainFogIndex)
    }

    @Test
    fun `test correlateWithMeds identifies peak window`() {
        val now = System.currentTimeMillis()
        val med = MedicationRecord(id = 1, medicationId = "med1", name = "Lisinopril", dosage = "10mg", frequencyOffset = 0, validFrom = 0)
        val dose = DoseLog(medicationId = "med1", scheduledTime = now, actualTime = now - 3600000L, status = "TAKEN") // 1 hour ago
        
        val metrics = CognitiveMetrics(
            fluidityScore = 0.5f,
            averageInterWordLatencyMilli = 1500,
            fillerCount = 3,
            circumlocutionDetected = true,
            brainFogIndex = 0.7f
        )
        
        val insight = engine.correlateWithMeds(metrics, listOf(med), listOf(dose))
        
        assertTrue(insight.isSignificant)
        assertEquals("med1", insight.correlatedMedication?.medicationId)
        assertTrue(insight.advice?.contains("Lisinopril") == true)
    }
}
