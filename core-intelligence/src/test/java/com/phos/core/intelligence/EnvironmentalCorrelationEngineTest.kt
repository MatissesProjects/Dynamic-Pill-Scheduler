package com.phos.core.intelligence

import com.phos.core.data.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class EnvironmentalCorrelationEngineTest {

    private val engine = EnvironmentalCorrelationEngine()

    @Test
    fun `correlateRespiratoryStrain identifies strain with poor AQI`() {
        val respiratoryMetrics = listOf(
            NocturnalRespiratoryMetric(Instant.now(), 18.0, SleepPosition.FLAT, 92.0)
        )
        val environmentalLogs = listOf(
            EnvironmentalLog(timestamp = Instant.now(), aqi = 150, pm25 = 55.0, ozone = 80.0, barometricPressure = 1013.25, humidity = 45.0, temperatureCelsius = 22.0)
        )
        
        val insight = engine.correlateRespiratoryStrain(respiratoryMetrics, environmentalLogs)
        
        assertNotNull(insight)
        assertEquals("HIGH", insight?.riskLevel)
        assertTrue(insight?.description!!.contains("HEPA filter"))
    }

    @Test
    fun `correlateRespiratoryStrain ignores stable SpO2 despite poor AQI`() {
        val respiratoryMetrics = listOf(
            NocturnalRespiratoryMetric(Instant.now(), 16.0, SleepPosition.FLAT, 98.0)
        )
        val environmentalLogs = listOf(
            EnvironmentalLog(timestamp = Instant.now(), aqi = 150, pm25 = 55.0, ozone = 80.0, barometricPressure = 1013.25, humidity = 45.0, temperatureCelsius = 22.0)
        )
        
        val insight = engine.correlateRespiratoryStrain(respiratoryMetrics, environmentalLogs)
        
        assertNotNull(insight)
        assertEquals("MODERATE", insight?.riskLevel)
        assertTrue(insight?.title!!.contains("Air Quality Warning"))
    }
}
