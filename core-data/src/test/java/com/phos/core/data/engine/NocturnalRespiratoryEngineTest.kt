package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class NocturnalRespiratoryEngineTest {

    private val engine = NocturnalRespiratoryEngine()

    @Test
    fun `analyzeCongestion detects orthopnea when flat RR is significantly higher`() {
        val metrics = listOf(
            NocturnalRespiratoryMetric(Instant.now(), 20.0, SleepPosition.FLAT, 95.0),
            NocturnalRespiratoryMetric(Instant.now(), 16.0, SleepPosition.PROPPED_UP, 96.0)
        )
        val result = engine.analyzeCongestion(metrics)
        
        assertTrue(result.orthopneaDetected)
        assertEquals(4.0, result.rrDeltaFlatVsPropped, 0.01)
        assertNotNull(result.recommendation)
    }

    @Test
    fun `analyzeCongestion returns stable for similar RR across positions`() {
        val metrics = listOf(
            NocturnalRespiratoryMetric(Instant.now(), 16.5, SleepPosition.FLAT, 98.0),
            NocturnalRespiratoryMetric(Instant.now(), 16.0, SleepPosition.PROPPED_UP, 98.0)
        )
        val result = engine.analyzeCongestion(metrics)
        
        assertFalse(result.orthopneaDetected)
        assertEquals(0.5, result.rrDeltaFlatVsPropped, 0.01)
        assertNull(result.recommendation)
    }

    @Test
    fun `analyzeCongestion handles missing data gracefully`() {
        val metrics = listOf(
            NocturnalRespiratoryMetric(Instant.now(), 16.0, SleepPosition.FLAT, 98.0)
        )
        val result = engine.analyzeCongestion(metrics)
        
        assertFalse(result.orthopneaDetected)
        assertTrue(result.summary.contains("Insufficient"))
    }
}
