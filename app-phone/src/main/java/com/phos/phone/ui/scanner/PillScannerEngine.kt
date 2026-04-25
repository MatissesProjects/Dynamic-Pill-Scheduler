package com.phos.phone.ui.scanner

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class PillScanResult(
    val detectedName: String? = null,
    val detectedDosage: String? = null,
    val detectedColor: String? = null,
    val detectedShape: String? = null,
    val frequencyDosesPerDay: Int = 1,
    val confidence: Float
)

class PillScannerEngine {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // User configuration (could be moved to a settings DAO later)
    private val userName = "Devin Finkel"

    /**
     * Extracts medication info from a bottle label using OCR or AI vision.
     */
    suspend fun recognizeBottleText(bitmap: Bitmap, aiVisionAnalyzer: (suspend (Bitmap) -> PillScanResult?)? = null): PillScanResult {
        // 1. Try real Vision AI (Gemini Nano)
        val aiResult = aiVisionAnalyzer?.invoke(bitmap)
        if (aiResult != null && aiResult.confidence > 0.5f) {
            return aiResult
        }

        // 2. Fallback to heuristic OCR extraction
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            val allLines = result.textBlocks.flatMap { it.lines }.map { it.text }
            
            var dosage: String? = null
            var name: String? = null
            var frequency = 1

            val dosageRegex = Regex("(\\d+\\s?mg)", RegexOption.IGNORE_CASE)
            val frequencyRegex = Regex("(\\d)\\s?(times|x)\\s?daily", RegexOption.IGNORE_CASE)
            
            val noise = listOf(
                userName, "TABLET", "CAPSULE", "GENERIC", "MOUTH", 
                "EVERY DAY", "DAILY", "TAKE", "PRINIVIL", "QTY", "REFILL"
            ).map { it.lowercase() }

            for (line in allLines) {
                // 1. Detect Frequency (e.g., "3x daily")
                frequencyRegex.find(line)?.let { match ->
                    frequency = match.groupValues[1].toIntOrNull() ?: 1
                }
                
                // 2. Detect Dosage (e.g., "20 MG")
                if (dosage == null) {
                    dosage = dosageRegex.find(line)?.value
                }
                
                // 3. Detect Name (Look for line containing dosage, but not noise)
                if (name == null && dosageRegex.containsMatchIn(line)) {
                    val words = line.split(" ")
                        .map { it.replace(Regex("[^A-Za-z]"), "").trim() }
                        .filter { it.length > 3 && !noise.any { n -> it.lowercase().contains(n) } }
                    
                    name = words.firstOrNull()
                }
            }

            // Fallback for name if first method failed
            if (name == null) {
                name = allLines.flatMap { it.split(" ") }
                    .map { it.replace(Regex("[^A-Za-z]"), "").trim() }
                    .find { it.length > 5 && !noise.any { n -> it.lowercase().contains(n) } }
            }

            PillScanResult(
                detectedName = name?.uppercase(),
                detectedDosage = dosage?.uppercase(),
                frequencyDosesPerDay = frequency,
                confidence = if (name != null) 0.9f else 0.4f
            )
        } catch (e: Exception) {
            PillScanResult(confidence = 0f)
        }
    }

    /**
     * Heuristic-based analysis of a single pill with optional AI vision processing.
     */
    suspend fun analyzePill(bitmap: Bitmap, aiVisionAnalyzer: (suspend (Bitmap) -> PillScanResult?)? = null): PillScanResult {
        // 1. Try real Vision AI (Gemini Nano)
        val aiResult = aiVisionAnalyzer?.invoke(bitmap)
        if (aiResult != null && aiResult.confidence > 0.5f) {
            return aiResult
        }

        // 2. Fallback to heuristic classification
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        
        // Average color in a 10x10 area for better robustness
        var totalRed = 0L
        var totalGreen = 0L
        var totalBlue = 0L
        val area = 10
        val halfArea = area / 2
        
        for (x in (centerX - halfArea) until (centerX + halfArea)) {
            for (y in (centerY - halfArea) until (centerY + halfArea)) {
                if (x in 0 until bitmap.width && y in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    totalRed += Color.red(pixel)
                    totalGreen += Color.green(pixel)
                    totalBlue += Color.blue(pixel)
                }
            }
        }
        
        val count = (area * area).toDouble()
        val r = (totalRed / count).toInt()
        val g = (totalGreen / count).toInt()
        val b = (totalBlue / count).toInt()
        
        val colorName = when {
            r > 200 && g > 200 && b > 200 -> "White"
            r > 150 && g < 100 && b < 100 -> "Red"
            b > 150 && r < 100 && g < 100 -> "Blue"
            r > 200 && g > 180 && b < 100 -> "Yellow"
            g > 150 && r < 120 && b < 120 -> "Green"
            r > 180 && g > 120 && b < 80 -> "Orange"
            else -> "Unknown"
        }

        val isRound = bitmap.width.toFloat() / bitmap.height.toFloat() in 0.9..1.1
        val shapeName = if (isRound) "Round" else "Oval"

        return PillScanResult(
            detectedColor = colorName,
            detectedShape = shapeName,
            confidence = 0.8f
        )
    }
}
