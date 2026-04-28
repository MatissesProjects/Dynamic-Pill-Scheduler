package com.phos.core.data.engine

import com.phos.core.data.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class HRROrchestratorTest {

    private val orchestrator = HRROrchestrator()

    @Test
    fun `test calculateHRR handles missing samples gracefully`() {
        val endTime = Instant.now()
        val hrSamples = emptyList<BiometricLog>()
        
        val record = orchestrator.calculateHRR(endTime, hrSamples, 150.0, 1L)
        
        assertNull(record.hrAtOneMin)
        assertNull(record.hrrOneMin)
    }

    @Test
    fun `test calculateHRR calculates correct deltas`() {
        val endTime = Instant.now()
        val hrSamples = listOf(
            BiometricLog(type = BiometricType.HEART_RATE, value = 110.0, timestamp = endTime.plusSeconds(60)),
            BiometricLog(type = BiometricType.HEART_RATE, value = 90.0, timestamp = endTime.plusSeconds(120))
        )
        
        val record = orchestrator.calculateHRR(endTime, hrSamples, 150.0, 1L)
        
        assertEquals(40.0, record.hrrOneMin!!, 0.1)
        assertEquals(60.0, record.hrrTwoMin!!, 0.1)
    }

    @Test
    fun `test buildHRRAudit detects strain on 15 percent slowdown`() {
        val current = HRRRecord(
            date = "2026-04-27", workoutEndTime = Instant.now(), peakHr = 150.0,
            hrAtOneMin = 120.0, hrAtTwoMin = 100.0, hrrOneMin = 30.0, hrrTwoMin = 50.0, medicationVersion = 1L
        )
        
        val history = listOf(
            current.copy(hrrOneMin = 40.0),
            current.copy(hrrOneMin = 42.0),
            current.copy(hrrOneMin = 38.0)
        ) // Avg is 40.0. Current 30.0 is -25% (Strained)
        
        val audit = orchestrator.buildHRRAudit(current, history)
        
        assertNotNull(audit)
        assertTrue(audit!!.isStrained)
        assertTrue(audit.advice.contains("Autonomic Strain Detected"))
    }
}
