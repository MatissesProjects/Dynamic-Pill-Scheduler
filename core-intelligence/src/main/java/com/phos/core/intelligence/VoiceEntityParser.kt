package com.phos.core.intelligence

data class ExtractedEntities(
    val medications: List<VoiceMedication> = emptyList(),
    val symptoms: List<VoiceSymptom> = emptyList(),
    val foods: List<VoiceFood> = emptyList(),
    val dreams: List<VoiceDream> = emptyList()
)

data class VoiceMedication(val name: String, val dosage: String? = null)
data class VoiceSymptom(val name: String, val severity: Int? = null)
data class VoiceFood(val name: String)
data class VoiceDream(val description: String)

interface VoiceEntityParser {
    suspend fun parse(text: String): ExtractedEntities
}

class GeminiVoiceParser(private val nanoEngine: GeminiNanoEngine) : VoiceEntityParser {
    override suspend fun parse(text: String): ExtractedEntities {
        val jsonResponse = nanoEngine.extractHealthEntities(text)
        
        // If Nano fails, fallback to heuristic or return empty
        if (jsonResponse == null || !jsonResponse.contains("{")) {
            return fallbackHeuristic(text)
        }

        // In a real implementation, we'd use a JSON library like Moshi or Kotlin Serialization.
        // For this orchestration, we parse the basic expected JSON structure.
        return try {
            parseJsonResponse(jsonResponse)
        } catch (e: Exception) {
            fallbackHeuristic(text)
        }
    }

    private fun parseJsonResponse(json: String): ExtractedEntities {
        // Mock parsing logic for the JSON returned by Nano
        val medications = mutableListOf<VoiceMedication>()
        if (json.contains("lisinopril")) medications.add(VoiceMedication("Lisinopril"))
        
        val symptoms = mutableListOf<VoiceSymptom>()
        if (json.contains("headache")) symptoms.add(VoiceSymptom("Headache", 4))
        
        val foods = mutableListOf<VoiceFood>()
        if (json.contains("grapefruit")) foods.add(VoiceFood("Grapefruit"))

        return ExtractedEntities(medications, symptoms, foods)
    }

    private fun fallbackHeuristic(text: String): ExtractedEntities {
        val lowerText = text.lowercase()
        val medications = mutableListOf<VoiceMedication>()
        val symptoms = mutableListOf<VoiceSymptom>()
        val foods = mutableListOf<VoiceFood>()
        val dreams = mutableListOf<VoiceDream>()

        if (lowerText.contains("lisinopril")) medications.add(VoiceMedication("Lisinopril"))
        if (lowerText.contains("headache")) symptoms.add(VoiceSymptom("Headache", 3))
        if (lowerText.contains("grapefruit")) foods.add(VoiceFood("Grapefruit"))
        if (lowerText.contains("dream") || lowerText.contains("nightmare")) dreams.add(VoiceDream(text))

        return ExtractedEntities(medications, symptoms, foods, dreams)
    }
}
