package com.phos.core.intelligence

import com.phos.core.data.dao.BiometricDao
import com.phos.core.data.engine.DigitalTwinEngine
import com.phos.core.data.model.BiometricLog
import com.phos.core.data.model.BiometricType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit

class DigitalTwinIntelligenceTest {

    private lateinit var biometricDao: BiometricDao
    private lateinit var digitalTwinEngine: DigitalTwinEngine
    private lateinit var intelligence: DigitalTwinIntelligence

    @Before
    fun setup() {
        biometricDao = mock()
        digitalTwinEngine = mock()
        intelligence = DigitalTwinIntelligence(biometricDao, digitalTwinEngine)
    }

    @Test
    fun `test checkPostDoseResponse returns alert on anomaly`() = runBlocking {
        val medId = "med_1"
        val doseTime = Instant.now()
        
        whenever(digitalTwinEngine.getExpectedResponse(medId)).thenReturn(Pair(70.0, 40.0))
        
        val logs = listOf(
            BiometricLog(type = BiometricType.HEART_RATE, value = 95.0, timestamp = doseTime.plus(30, ChronoUnit.MINUTES))
        )
        whenever(biometricDao.getLogsSince(eq(BiometricType.HEART_RATE), any())).thenReturn(logs)
        whenever(digitalTwinEngine.isAnomaly(any(), any(), any())).thenReturn(true)

        val result = intelligence.checkPostDoseResponse(medId, "Metoprolol", doseTime)

        assertNotNull(result)
        assertTrue(result?.contains("Digital Twin Alert") == true)
        assertTrue(result?.contains("Metoprolol") == true)
    }

    @Test
    fun `test checkPostDoseResponse returns null when no anomaly`() = runBlocking {
        val medId = "med_1"
        val doseTime = Instant.now()
        
        whenever(digitalTwinEngine.getExpectedResponse(medId)).thenReturn(Pair(70.0, 40.0))
        whenever(biometricDao.getLogsSince(any(), any())).thenReturn(emptyList())

        val result = intelligence.checkPostDoseResponse(medId, "Metoprolol", doseTime)

        assertNull(result)
    }
}
