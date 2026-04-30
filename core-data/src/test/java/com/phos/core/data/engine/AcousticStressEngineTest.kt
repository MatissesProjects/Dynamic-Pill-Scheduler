package com.phos.core.data.engine

import com.phos.core.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class AcousticStressEngineTest {

    private val engine = AcousticStressEngine()

    @Test
    fun `low noise returns null insight`() {
        val insight = engine.analyzeAcousticStress(
            latestDb = 60.0,
            activeMedications = emptyList(),
            biometricLogs = emptyList()
        )
        assertNull(insight)
    }

    @Test
    fun `critical noise returns CRITICAL insight`() {
        val insight = engine.analyzeAcousticStress(
            latestDb = 105.0,
            activeMedications = emptyList(),
            biometricLogs = emptyList()
        )
        assertNotNull(insight)
        assertEquals(AcousticRiskLevel.CRITICAL, insight?.riskLevel)
    }

    @Test
    fun `high noise with hypertension meds returns insight`() {
        val meds = listOf(MedicationRecord(medicationId = "lis_1", name = "Lisinopril", dosage = "10mg", frequencyOffset = 0, validFrom = 0))
        val insight = engine.analyzeAcousticStress(
            latestDb = 90.0,
            activeMedications = meds,
            biometricLogs = emptyList()
        )
        assertNotNull(insight)
        assertEquals(AcousticRiskLevel.HIGH, insight?.riskLevel)
        assert(insight?.title?.contains("Acoustic Shielding") == true)
    }

    @Test
    fun `moderate noise with elevated heart rate returns insight`() {
        val biometrics = List(5) { BiometricLog(type = BiometricType.HEART_RATE, value = 95.0, timestamp = Instant.now()) }
        val insight = engine.analyzeAcousticStress(
            latestDb = 75.0,
            activeMedications = emptyList(),
            biometricLogs = biometrics
        )
        assertNotNull(insight)
        assertEquals(AcousticRiskLevel.MODERATE, insight?.riskLevel)
        assert(insight?.title?.contains("Acoustic Tension") == true)
    }
}
