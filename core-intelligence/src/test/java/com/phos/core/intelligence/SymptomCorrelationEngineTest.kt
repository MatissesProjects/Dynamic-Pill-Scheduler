package com.phos.core.intelligence

import com.phos.core.data.dao.IntelligenceDao
import com.phos.core.data.model.EnvironmentalLog
import com.phos.core.data.model.SymptomLog
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.Instant

class SymptomCorrelationEngineTest {

    @Mock
    private lateinit var intelligenceDao: IntelligenceDao

    private lateinit var engine: SymptomCorrelationEngine

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        engine = SymptomCorrelationEngine(intelligenceDao)
    }

    @Test
    fun `analyzeCorrelations returns insight when data is present`() = runBlocking {
        // Arrange
        val symptoms = listOf(
            SymptomLog(symptomName = "Stomach Pain", severity = 7, timestamp = Instant.now())
        )
        val environmental = listOf(
            EnvironmentalLog(barometricPressure = 1005.0, timestamp = Instant.now(), aqi = 50, pm25 = 10.0, ozone = 30.0, humidity = 50.0, temperatureCelsius = 20.0)
        )
        `when`(intelligenceDao.getRecentSymptoms(50)).thenReturn(symptoms)
        `when`(intelligenceDao.getRecentEnvironmental(50)).thenReturn(environmental)

        // Act
        val result = engine.analyzeCorrelations()

        // Assert
        assertTrue(result.contains("AI Insight"))
    }

    @Test
    fun `analyzeCorrelations returns error message when data is missing`() = runBlocking {
        // Arrange
        `when`(intelligenceDao.getRecentSymptoms(50)).thenReturn(emptyList())
        `when`(intelligenceDao.getRecentEnvironmental(50)).thenReturn(emptyList())

        // Act
        val result = engine.analyzeCorrelations()

        // Assert
        assertTrue(result.contains("Insufficient data"))
    }
}
