package com.phos.core.intelligence

import com.phos.core.data.model.BiometricLog
import com.phos.core.data.model.BiometricType
import com.phos.core.data.model.SentimentLog
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class StressSynthesisEngineTest {

    private val engine = StressSynthesisEngine()

    @Test
    fun `test high burnout risk detection`() {
        val sentiment = listOf(
            SentimentLog(text = "I am so overwhelmed", score = -0.8f, primaryEmotion = "Stress", intensity = 9)
        )
        val biometrics = listOf(
            BiometricLog(type = BiometricType.HRV, value = 25.0, timestamp = Instant.now()) // Low HRV
        )
        
        val result = engine.detectBurnoutRisk(sentiment, biometrics)
        
        assertTrue(result.isCritical)
        assertTrue(result.recommendation?.contains("High Burnout Risk") == true)
        assertTrue(result.score > 0.75f)
    }

    @Test
    fun `test moderate stress detection`() {
        val sentiment = listOf(
            SentimentLog(text = "Had a long day", score = -0.3f, primaryEmotion = "Tired", intensity = 6)
        )
        val biometrics = listOf(
            BiometricLog(type = BiometricType.HRV, value = 55.0, timestamp = Instant.now()) // Normal HRV
        )
        
        val result = engine.detectBurnoutRisk(sentiment, biometrics)
        
        assertFalse(result.isCritical)
        // Score should be moderate
        assertTrue(result.score in 0.3f..0.6f)
    }

    @Test
    fun `test healthy state detection`() {
        val sentiment = listOf(
            SentimentLog(text = "Feeling great today", score = 0.8f, primaryEmotion = "Happy", intensity = 8)
        )
        val biometrics = listOf(
            BiometricLog(type = BiometricType.HRV, value = 75.0, timestamp = Instant.now()) // High HRV
        )
        
        val result = engine.detectBurnoutRisk(sentiment, biometrics)
        
        assertFalse(result.isCritical)
        assertNull(result.recommendation)
        assertTrue(result.score < 0.3f)
    }

    @Test
    fun `test fallback when no HRV data`() {
        val sentiment = listOf(
            SentimentLog(text = "Terrible morning", score = -0.9f, primaryEmotion = "Angry", intensity = 9)
        )
        
        val result = engine.detectBurnoutRisk(sentiment, emptyList())
        
        assertEquals(0.6f, result.score, 0.01f)
        assertTrue(result.recommendation?.contains("Emotional strain") == true)
    }
}
