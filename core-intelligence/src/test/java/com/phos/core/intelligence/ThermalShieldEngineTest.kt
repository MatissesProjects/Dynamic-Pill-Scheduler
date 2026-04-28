package com.phos.core.intelligence

import com.phos.core.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ThermalShieldEngineTest {

    private lateinit var engine: ThermalShieldEngine

    @Before
    fun setup() {
        engine = ThermalShieldEngine()
    }

    @Test
    fun `test classifyMedication identifies sweat inhibitors`() {
        assertEquals(ThermalEffectType.SWEAT_INHIBITOR, engine.classifyMedication("Oxybutynin 5mg"))
        assertEquals(ThermalEffectType.SWEAT_INHIBITOR, engine.classifyMedication("Benztropine"))
    }

    @Test
    fun `test classifyMedication identifies flushing agents`() {
        assertEquals(ThermalEffectType.FLUSHING_AGENT, engine.classifyMedication("Niacin ER"))
        assertEquals(ThermalEffectType.FLUSHING_AGENT, engine.classifyMedication("Amlodipine"))
    }

    @Test
    fun `test analyzeThermalRisk detects SI risk with rising temp`() {
        val now = Instant.now()
        val logs = listOf(
            BiometricLog(type = BiometricType.SKIN_TEMPERATURE, value = 33.0, timestamp = now.minusSeconds(7200)), // Baseline
            BiometricLog(type = BiometricType.SKIN_TEMPERATURE, value = 34.5, timestamp = now) // Elevated
        )
        val meds = listOf(MedicationRecord(id = 1, medicationId = "si1", name = "Oxybutynin", dosage = "5mg", frequencyOffset = 0, validFrom = 0))
        val doses = listOf(DoseLog(medicationId = "si1", scheduledTime = now.toEpochMilli() - 3600000, actualTime = now.toEpochMilli() - 3600000, status = "TAKEN"))

        val insight = engine.analyzeThermalRisk(logs, meds, doses)
        
        assertNotNull(insight)
        assertEquals(ThermalEffectType.SWEAT_INHIBITOR, insight?.effectType)
        assertEquals(ThermalRiskLevel.ELEVATED, insight?.riskLevel)
        assertTrue(insight?.advice?.contains("inhibits sweating") == true)
    }

    @Test
    fun `test analyzeThermalRisk detects flushing`() {
        val now = Instant.now()
        val logs = listOf(
            BiometricLog(type = BiometricType.SKIN_TEMPERATURE, value = 33.0, timestamp = now.minusSeconds(7200)),
            BiometricLog(type = BiometricType.SKIN_TEMPERATURE, value = 34.0, timestamp = now)
        )
        val meds = listOf(MedicationRecord(id = 1, medicationId = "fl1", name = "Niacin", dosage = "500mg", frequencyOffset = 0, validFrom = 0))
        val doses = listOf(DoseLog(medicationId = "fl1", scheduledTime = now.toEpochMilli() - 1800000, actualTime = now.toEpochMilli() - 1800000, status = "TAKEN"))

        val insight = engine.analyzeThermalRisk(logs, meds, doses)
        
        assertNotNull(insight)
        assertEquals(ThermalEffectType.FLUSHING_AGENT, insight?.effectType)
        assertEquals(ThermalRiskLevel.ELEVATED, insight?.riskLevel)
        assertTrue(insight?.advice?.contains("Normal flushing") == true)
    }
}
