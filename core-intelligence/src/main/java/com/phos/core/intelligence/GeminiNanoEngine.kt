package com.phos.core.intelligence

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.genai.prompt.*

/**
 * Manages the on-device Gemini Nano model on Pixel 9 Pro via ML Kit bridge.
 * Performs multimodal inference for pill recognition and meal analysis.
 */
class GeminiNanoEngine(
    private val context: Context,
    private var aiClient: PhosAiClient? = null
) {

    /**
     * Initializes the Gemini Nano connection via ML Kit.
     */
    suspend fun initialize() {
        if (aiClient != null) return
        
        try {
            val modelConfig = ModelConfig.Builder().apply {
                preference = ModelPreference.FULL
            }.build()
                
            val generationConfig = GenerationConfig.Builder().apply {
                this.modelConfig = modelConfig
            }.build()

            val model = Generation.getClient(generationConfig)
            aiClient = object : PhosAiClient {
                override suspend fun generateResponse(prompt: String): String? {
                    val request = GenerateContentRequest.Builder(TextPart(prompt)).build()
                    val response = model.generateContent(request)
                    return response.candidates.firstOrNull()?.text
                }

                override suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String? {
                    val request = GenerateContentRequest.Builder(ImagePart(bitmap), TextPart(prompt)).build()
                    val response = model.generateContent(request)
                    return response.candidates.firstOrNull()?.text
                }
            }
            android.util.Log.i("GeminiNanoEngine", "ML Kit Gemini Nano bridge initialized.")
        } catch (e: Exception) {
            android.util.Log.e("GeminiNanoEngine", "Bridge init failed", e)
        }
    }

    suspend fun generateResponse(prompt: String): String? {
        return aiClient?.generateResponse(prompt)
    }

    suspend fun analyzePillImage(bitmap: Bitmap): String? {
        val prompt = "Analyze this pill or medication bottle. Return JSON: {detectedName, detectedDosage, detectedColor, detectedShape, frequencyDosesPerDay}"
        return aiClient?.analyzeImage(bitmap, prompt)
    }

    suspend fun analyzeMealImage(bitmap: Bitmap): String? {
        val prompt = "Analyze this meal image for health tracking. Return JSON: {detectedName, category, calories, proteinG, calciumMg, ingredients: []}"
        return aiClient?.analyzeImage(bitmap, prompt)
    }

    suspend fun parseNutritionText(ocrText: String): String? {
        val prompt = "Convert this OCR text from a nutrition label to structured JSON: {calories, proteinG, calciumMg, ingredients: []}. Text: $ocrText"
        return generateResponse(prompt)
    }

    suspend fun extractHealthEntities(voiceText: String): String? {
        val prompt = "Extract health entities (meds, symptoms, foods) to JSON from: $voiceText"
        return generateResponse(prompt)
    }

    /**
     * Extracts a sentiment score (-1.0 to 1.0) and primary emotion from a health log.
     */
    suspend fun calculateSentiment(text: String): String? {
        val prompt = """
            Analyze the emotional sentiment of this health journal entry.
            Return JSON: {score: Float, primaryEmotion: String, intensity: 1-10}
            Score: -1.0 (extremely stressed/depressed) to 1.0 (extremely calm/happy).
            Text: "$text"
        """.trimIndent()
        return generateResponse(prompt)
    }

    /**
     * Identifies which medications in a list are known gastric irritants.
     */
    suspend fun detectGI_Irritants(medicationNames: List<String>): String? {
        val prompt = """
            Analyze this list of medications and identify which ones are known to cause gastric irritation or stomach discomfort.
            List: ${medicationNames.joinToString(", ")}
            Return JSON: {irritants: ["MedName1", "MedName2"], advice: "General stomach protection advice"}
        """.trimIndent()
        return generateResponse(prompt)
    }

    /**
     * M2: Dream vividness synthesis.
     * Extracts intensity and vividness scores from a dream description.
     */
    suspend fun synthesizeDreamIntensity(dreamText: String): String? {
        val prompt = """
            Analyze this dream journal entry for intensity and vividness.
            Intensity is the emotional or physical impact (1-10).
            Vividness is the level of detail and clarity (1-10).
            Return JSON: {intensity: Int, vividness: Int}
            Text: "$dreamText"
        """.trimIndent()
        return generateResponse(prompt)
    }
}
