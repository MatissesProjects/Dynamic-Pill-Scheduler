package com.phos.core.data.engine

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.metadata.Metadata
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant

class MetabolicEngineTest {

    private val engine = MetabolicEngine()

    @Test
    fun `test high intensity TRIMP calculation`() {
        val session: ExerciseSessionRecord = mock()
        val metadata: Metadata = mock()
        whenever(session.metadata).thenReturn(metadata)
        whenever(metadata.id).thenReturn("test_id")
        whenever(session.startTime).thenReturn(Instant.now().minusSeconds(3600)) // 60 mins
        whenever(session.endTime).thenReturn(Instant.now())
        
        val hrSamples = listOf(
            mock<HeartRateRecord.Sample>().apply { whenever(beatsPerMinute).thenReturn(175L) }
        )
        
        val result = engine.calculateMetabolicLoad(session, hrSamples)
        
        assertEquals(300.0, result.trimpScore, 0.1)
        assertTrue(result.isHyperMetabolic)
    }

    @Test
    fun `test moderate intensity TRIMP calculation`() {
        val session: ExerciseSessionRecord = mock()
        val metadata: Metadata = mock()
        whenever(session.metadata).thenReturn(metadata)
        whenever(metadata.id).thenReturn("test_id")
        whenever(session.startTime).thenReturn(Instant.now().minusSeconds(1800)) // 30 mins
        whenever(session.endTime).thenReturn(Instant.now())
        
        val hrSamples = listOf(
            mock<HeartRateRecord.Sample>().apply { whenever(beatsPerMinute).thenReturn(135L) }
        )
        
        val result = engine.calculateMetabolicLoad(session, hrSamples)
        
        assertEquals(90.0, result.trimpScore, 0.1)
        assertFalse(result.isHyperMetabolic)
    }

    @Test
    fun `test calculation with no samples`() {
        val session: ExerciseSessionRecord = mock()
        val metadata: Metadata = mock()
        whenever(session.metadata).thenReturn(metadata)
        whenever(metadata.id).thenReturn("test_id")
        whenever(session.startTime).thenReturn(Instant.now().minusSeconds(600))
        whenever(session.endTime).thenReturn(Instant.now())
        
        val result = engine.calculateMetabolicLoad(session, emptyList())
        
        assertEquals(0.0, result.trimpScore, 0.1)
        assertEquals(0.0, result.avgHeartRate, 0.1)
    }
}
