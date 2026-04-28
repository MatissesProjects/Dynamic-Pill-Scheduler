package com.phos.core.data.engine

import com.phos.core.data.model.PulsePowerMetric
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class PulsePowerEngineTest {

    private val engine = PulsePowerEngine()

    @Test
    fun `calculateEfficiency returns stable for baseline-matching data`() {
        val metrics = listOf(
            PulsePowerMetric(Instant.now(), 150.0, 150.0), // 1.0 Watts/BPM
            PulsePowerMetric(Instant.now(), 160.0, 160.0)  // 1.0 Watts/BPM
        )
        val result = engine.calculateEfficiency(metrics, 1.0)
        
        assertEquals(1.0, result.avgWattsPerBpm, 0.01)
        assertEquals(0.0, result.efficiencyTrend, 0.01)
        assertTrue(result.summary.contains("Stable"))
    }

    @Test
    fun `calculateEfficiency detects efficiency drop`() {
        val metrics = listOf(
            PulsePowerMetric(Instant.now(), 120.0, 150.0), // 0.8 Watts/BPM
            PulsePowerMetric(Instant.now(), 120.0, 150.0)  // 0.8 Watts/BPM
        )
        val result = engine.calculateEfficiency(metrics, 1.0)
        
        assertEquals(0.8, result.avgWattsPerBpm, 0.01)
        assertEquals(-20.0, result.efficiencyTrend, 0.01)
        assertTrue(result.summary.contains("Decreased"))
        assertNotNull(result.recommendation)
    }

    @Test
    fun `calculateEfficiency detects efficiency gain`() {
        val metrics = listOf(
            PulsePowerMetric(Instant.now(), 180.0, 150.0), // 1.2 Watts/BPM
            PulsePowerMetric(Instant.now(), 180.0, 150.0)  // 1.2 Watts/BPM
        )
        val result = engine.calculateEfficiency(metrics, 1.0)
        
        assertEquals(1.2, result.avgWattsPerBpm, 0.01)
        assertEquals(20.0, result.efficiencyTrend, 0.01)
        assertTrue(result.summary.contains("Increased"))
    }
}
