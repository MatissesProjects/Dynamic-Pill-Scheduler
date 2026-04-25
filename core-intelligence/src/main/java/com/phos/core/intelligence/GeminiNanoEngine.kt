package com.phos.core.intelligence

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.genai.prompt.*

/**
 * Manages the on-device Gemini Nano model on Pixel 9 Pro via ML Kit bridge.
 * Performs real multimodal inference for meal analysis and health entity extraction.
 */
class GeminiNanoEngine(private val context: Context) {

    private var generativeModel: GenerativeModel? = null

    /**
     * Initializes the Gemini Nano connection via ML Kit.
     */
    suspend fun initialize() {
        try {
            val modelConfig = ModelConfig.Builder().apply {
                preference = ModelPreference.FULL // Use the best available Gemma 4 model
            }.build()
                
            val generationConfig = GenerationConfig.Builder().apply {
                this.modelConfig = modelConfig
            }.build()

            generativeModel = Generation.getClient(generationConfig)
            android.util.Log.i("GeminiNanoEngine", "ML Kit Gemini Nano bridge initialized with FULL preference.")
        } catch (e: Exception) {
            android.util.Log.e("GeminiNanoEngine", "Bridge init failed", e)
        }
    }

    /**
     * REAL multimodal inference for pill/bottle analysis using ML Kit bridge.
     */
    suspend fun analyzePillImage(inputImage: Bitmap): String? {
        val model = generativeModel ?: return null

        val prompt = """
            Analyze this pill or medication bottle photo. 
            Identify the medication details and return strict JSON format:
            {
                "detectedName": "string",
                "detectedDosage": "string",
                "detectedColor": "string",
                "detectedShape": "string",
                "frequencyDosesPerDay": 1
            }
            Return ONLY the JSON.
        """.trimIndent()

        return try {
            val request = GenerateContentRequest.Builder(
                ImagePart(inputImage),
                TextPart(prompt)
            ).build()

            val response = model.generateContent(request)
            response.candidates.firstOrNull()?.text
        } catch (e: Exception) {
            android.util.Log.e("GeminiNanoEngine", "Vision failed", e)
            null
        }
    }

    /**
     * REAL multimodal inference for meal analysis using ML Kit bridge.
     * Identifies ingredients and estimates nutrients from aprepared meal photo.
     */
    suspend fun analyzeMealImage(inputImage: Bitmap): String? {
        val model = generativeModel ?: return null

        val prompt = """
            Analyze this prepared meal photo. 
            Identify the dish and list all visible ingredients.
            Estimate nutritional values and return strict JSON format:
            {
                "detectedName": "string",
                "category": "string",
                "nutrients": {
                    "calories": number,
                    "proteinG": number,
                    "calciumMg": number,
                    "ingredients": ["string"]
                },
                "explanation": "brief health insight"
            }
            Return ONLY the JSON.
        """.trimIndent()

        return try {
            val request = GenerateContentRequest.Builder(
                ImagePart(inputImage),
                TextPart(prompt)
            ).build()

            val response = model.generateContent(request)
            response.candidates.firstOrNull()?.text
        } catch (e: Exception) {
            android.util.Log.e("GeminiNanoEngine", "Vision failed", e)
            null
        }
    }

    suspend fun generateResponse(prompt: String): String? {
        val model = generativeModel ?: return null
        return try {
            val request = GenerateContentRequest.Builder(TextPart(prompt)).build()
            val response = model.generateContent(request)
            response.candidates.firstOrNull()?.text
        } catch (e: Exception) {
            null
        }
    }

    suspend fun parseNutritionText(ocrText: String): String? {
        val prompt = "Extract structured nutrition JSON from this label text: $ocrText"
        return generateResponse(prompt)
    }

    suspend fun extractHealthEntities(voiceText: String): String? {
        val prompt = "Extract health entities (meds, symptoms, foods) to JSON from: $voiceText"
        return generateResponse(prompt)
    }
}
