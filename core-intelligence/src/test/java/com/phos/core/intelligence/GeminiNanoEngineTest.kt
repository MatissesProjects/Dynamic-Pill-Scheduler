package com.phos.core.intelligence

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class GeminiNanoEngineTest {

    private lateinit var context: Context
    private lateinit var aiClient: PhosAiClient
    private lateinit var engine: GeminiNanoEngine

    @Before
    fun setup() {
        context = mock()
        aiClient = mock()
        engine = GeminiNanoEngine(context, aiClient)
    }

    @Test
    fun `test generateResponse uses aiClient`() = runTest {
        val prompt = "Hello"
        val expected = "Response"
        whenever(aiClient.generateResponse(prompt)).thenReturn(expected)
        
        val result = engine.generateResponse(prompt)
        assertEquals(expected, result)
        verify(aiClient).generateResponse(prompt)
    }

    @Test
    fun `test analyzePillImage uses aiClient`() = runTest {
        val bitmap: Bitmap = mock()
        val expected = "Pill Info"
        whenever(aiClient.analyzeImage(eq(bitmap), any())).thenReturn(expected)
        
        val result = engine.analyzePillImage(bitmap)
        assertEquals(expected, result)
        verify(aiClient).analyzeImage(eq(bitmap), any())
    }

    @Test
    fun `test analyzeMealImage uses aiClient`() = runTest {
        val bitmap: Bitmap = mock()
        val expected = "Meal Info"
        whenever(aiClient.analyzeImage(eq(bitmap), any())).thenReturn(expected)
        
        val result = engine.analyzeMealImage(bitmap)
        assertEquals(expected, result)
        verify(aiClient).analyzeImage(eq(bitmap), any())
    }

    @Test
    fun `test parseNutritionText uses generateResponse`() = runTest {
        val ocrText = "Calories 100"
        val expected = "JSON"
        whenever(aiClient.generateResponse(any())).thenReturn(expected)
        
        val result = engine.parseNutritionText(ocrText)
        assertEquals(expected, result)
    }

    @Test
    fun `test extractHealthEntities uses generateResponse`() = runTest {
        val voiceText = "Took meds"
        val expected = "JSON"
        whenever(aiClient.generateResponse(any())).thenReturn(expected)
        
        val result = engine.extractHealthEntities(voiceText)
        assertEquals(expected, result)
    }
}
