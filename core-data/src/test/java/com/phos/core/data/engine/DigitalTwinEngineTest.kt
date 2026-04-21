package com.phos.core.data.engine

import com.phos.core.data.dao.BiometricDao
import com.phos.core.data.model.BiometricBaseline
import com.phos.core.data.model.BiometricLog
import com.phos.core.data.model.BiometricType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit

class DigitalTwinEngineTest {

    private lateinit var biometricDao: BiometricDao
    private lateinit var engine: DigitalTwinEngine

    @Before
    fun setup() {
        biometricDao = mock()
        engine = DigitalTwinEngine(biometricDao)
    }

    @Test
    fun `test updateBaseline creates new baseline if none exists`() = runBlocking {
        val medId = "statin_001"
        val doseTime = Instant.now()
        
        val hrLogs = listOf(
            BiometricLog(type = BiometricType.HEART_RATE, value = 70.0, timestamp = doseTime.plus(30, ChronoUnit.MINUTES)),
            BiometricLog(type = BiometricType.HEART_RATE, value = 74.0, timestamp = doseTime.plus(60, ChronoUnit.MINUTES))
        )
        val hrvLogs = listOf(
            BiometricLog(type = BiometricType.HRV, value = 40.0, timestamp = doseTime.plus(30, ChronoUnit.MINUTES))
        )

        whenever(biometricDao.getLogsSince(eq(BiometricType.HEART_RATE), any())).thenReturn(hrLogs)
        whenever(biometricDao.getLogsSince(eq(BiometricType.HRV), any())).thenReturn(hrvLogs)
        whenever(biometricDao.getBaselineForMedication(medId)).thenReturn(null)

        engine.updateBaseline(medId, doseTime)

        verify(biometricDao).updateBaseline(argThat {
            medicationId == medId && averageHrPostDose == 72.0 && averageHrvPostDose == 40.0 && sampleSize == 1
        })
    }

    @Test
    fun `test updateBaseline updates existing baseline`() = runBlocking {
        val medId = "statin_001"
        val doseTime = Instant.now()
        
        val existing = BiometricBaseline(medId, averageHrPostDose = 70.0, averageHrvPostDose = 40.0, sampleSize = 1)
        
        val hrLogs = listOf(BiometricLog(type = BiometricType.HEART_RATE, value = 80.0, timestamp = doseTime.plus(30, ChronoUnit.MINUTES)))
        val hrvLogs = listOf(BiometricLog(type = BiometricType.HRV, value = 50.0, timestamp = doseTime.plus(30, ChronoUnit.MINUTES)))

        whenever(biometricDao.getLogsSince(eq(BiometricType.HEART_RATE), any())).thenReturn(hrLogs)
        whenever(biometricDao.getLogsSince(eq(BiometricType.HRV), any())).thenReturn(hrvLogs)
        whenever(biometricDao.getBaselineForMedication(medId)).thenReturn(existing)

        engine.updateBaseline(medId, doseTime)

        // (70*1 + 80)/2 = 75
        // (40*1 + 50)/2 = 45
        verify(biometricDao).updateBaseline(argThat {
            medicationId == medId && averageHrPostDose == 75.0 && averageHrvPostDose == 45.0 && sampleSize == 2
        })
    }

    @Test
    fun `test isAnomaly detected correctly`() {
        assertTrue(engine.isAnomaly(100.0, 80.0, 0.20)) // 25% diff > 20%
        assertFalse(engine.isAnomaly(85.0, 80.0, 0.10)) // 6.25% diff < 10%
    }
}
