package com.phos.core.data.engine

import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.Metadata
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class EbikeEffortNormalizerTest {

    private val normalizer = EbikeEffortNormalizer()

    @Test
    fun `test ebike detection heuristic`() {
        val session: ExerciseSessionRecord = mock()
        val metadata: Metadata = mock()
        whenever(session.metadata).thenReturn(metadata)
        whenever(metadata.id).thenReturn("test_id")
        whenever(session.exerciseType).thenReturn(8) // Cycling literal
        
        val power = listOf(mock<PowerRecord.Sample>())
        val hr = listOf(mock<HeartRateRecord.Sample>().apply { whenever(beatsPerMinute).thenReturn(110L) })
        val cadence = listOf(mock<CyclingPedalingCadenceRecord.Sample>())
        
        val result = normalizer.normalizeEffort(session, power, hr, cadence)
        
        assertEquals(CyclingType.EBIKE, result.type)
        val rawLoad = (110.0 / 190.0) * 100.0
        assertEquals(rawLoad * 0.7, result.normalizedCardioLoad, 0.1)
    }

    @Test
    fun `test traditional bike detection`() {
        val session: ExerciseSessionRecord = mock()
        val metadata: Metadata = mock()
        whenever(session.metadata).thenReturn(metadata)
        whenever(metadata.id).thenReturn("test_id")
        whenever(session.exerciseType).thenReturn(8)
        
        val power = emptyList<PowerRecord.Sample>()
        val hr = listOf(mock<HeartRateRecord.Sample>().apply { whenever(beatsPerMinute).thenReturn(155L) })
        val cadence = listOf(mock<CyclingPedalingCadenceRecord.Sample>())
        
        val result = normalizer.normalizeEffort(session, power, hr, cadence)
        
        assertEquals(CyclingType.TRADITIONAL, result.type)
        val rawLoad = (155.0 / 190.0) * 100.0
        assertEquals(rawLoad, result.normalizedCardioLoad, 0.1)
        assertTrue(result.wasHighIntensity)
    }

    @Test
    fun `test non-cycling session`() {
        val session: ExerciseSessionRecord = mock()
        whenever(session.exerciseType).thenReturn(1) // Not cycling
        
        val result = normalizer.normalizeEffort(session, emptyList(), emptyList(), emptyList())
        
        assertEquals(CyclingType.UNKNOWN, result.type)
        assertEquals(0.0, result.normalizedCardioLoad, 0.1)
    }
}
