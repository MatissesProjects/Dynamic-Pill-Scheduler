package com.phos.core.intelligence

data class ExtractedEntities(
    val medications: List<VoiceMedication> = emptyList(),
    val symptoms: List<VoiceSymptom> = emptyList(),
    val foods: List<VoiceFood> = emptyList()
)

data class VoiceMedication(val name: String, val dosage: String? = null)
data class VoiceSymptom(val name: String, val severity: Int? = null)
data class VoiceFood(val name: String)

interface VoiceEntityParser {
    suspend fun parse(text: String): ExtractedEntities
}

class GeminiVoiceParser : VoiceEntityParser {
    override suspend fun parse(text: String): ExtractedEntities {
        // In a real implementation, this would call Gemini Nano (AICore)
        // For now, we simulate the extraction logic with simple heuristics or a mock call.
        
        val medications = mutableListOf<VoiceMedication>()
        val symptoms = mutableListOf<VoiceSymptom>()
        val foods = mutableListOf<VoiceFood>()

        val lowerText = text.lowercase()
        
        // Simple heuristic simulation
        if (lowerText.contains("took") || lowerText.contains("med") || lowerText.contains("pill")) {
            // Mock extraction
            if (lowerText.contains("lisinopril")) medications.add(VoiceMedication("Lisinopril"))
            if (lowerText.contains("ibuprofen")) medications.add(VoiceMedication("Ibuprofen"))
        }

        if (lowerText.contains("feeling") || lowerText.contains("pain") || lowerText.contains("dizzy")) {
            if (lowerText.contains("headache")) symptoms.add(VoiceSymptom("Headache", 3))
            if (lowerText.contains("dizzy")) symptoms.add(VoiceSymptom("Dizziness", 2))
            if (lowerText.contains("pain")) symptoms.add(VoiceSymptom("Pain", 5))
        }

        if (lowerText.contains("ate") || lowerText.contains("drank") || lowerText.contains("bowl") || lowerText.contains("juice")) {
            if (lowerText.contains("grapefruit")) foods.add(VoiceFood("Grapefruit"))
            if (lowerText.contains("milk") || lowerText.contains("dairy")) foods.add(VoiceFood("Dairy"))
        }

        return ExtractedEntities(medications, symptoms, foods)
    }
}
