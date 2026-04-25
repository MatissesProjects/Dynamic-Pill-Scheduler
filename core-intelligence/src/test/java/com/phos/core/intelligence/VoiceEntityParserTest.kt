package com.phos.core.intelligence

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class VoiceEntityParserTest {

    private lateinit var nanoEngine: GeminiNanoEngine
    private lateinit var parser: GeminiVoiceParser

    @Before
    fun setup() {
        nanoEngine = mock()
        parser = GeminiVoiceParser(nanoEngine)
    }

    @Test
    fun `test parse with successful JSON from Nano`() = runBlocking {
        val text = "Took lisinopril and had a headache"
        val jsonResponse = """{"medications": ["lisinopril"], "symptoms": ["headache"]}"""
        
        whenever(nanoEngine.extractHealthEntities(text)).thenReturn(jsonResponse)
        
        val result = parser.parse(text)
        
        assertEquals(1, result.medications.size)
        assertEquals("Lisinopril", result.medications[0].name)
        assertEquals(1, result.symptoms.size)
        assertEquals("Headache", result.symptoms[0].name)
    }

    @Test
    fun `test parse with fallback heuristic when Nano fails`() = runBlocking {
        val text = "Took lisinopril"
        whenever(nanoEngine.extractHealthEntities(text)).thenReturn(null)
        
        val result = parser.parse(text)
        
        assertEquals(1, result.medications.size)
        assertEquals("Lisinopril", result.medications[0].name)
    }

    @Test
    fun `test parse with fallback heuristic when Nano returns garbage`() = runBlocking {
        val text = "I ate grapefruit"
        whenever(nanoEngine.extractHealthEntities(text)).thenReturn("Random string")
        
        val result = parser.parse(text)
        
        assertEquals(1, result.foods.size)
        assertEquals("Grapefruit", result.foods[0].name)
    }
}
