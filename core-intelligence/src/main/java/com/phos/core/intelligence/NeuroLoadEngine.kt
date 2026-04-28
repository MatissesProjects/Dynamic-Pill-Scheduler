package com.phos.core.intelligence

import android.util.Log
import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.MedicationRecord
import java.time.Instant

/**
 * Data structure representing a segment of speech with its associated timing.
 */
data class SpeechSegment(
    val text: String,
    val startTimeMilli: Long,
    val endTimeMilli: Long
) {
    val durationMilli: Long = endTimeMilli - startTimeMilli
}

/**
 * Result of the neuro-cognitive load analysis.
 */
data class CognitiveMetrics(
    val fluidityScore: Float, // 0.0 to 1.0 (1.0 is perfectly fluid)
    val averageInterWordLatencyMilli: Long,
    val fillerCount: Int,
    val circumlocutionDetected: Boolean,
    val brainFogIndex: Float // 0.0 to 1.0
)

/**
 * High-level insight correlating cognitive load with medications.
 */
data class NeuroCognitiveInsight(
    val brainFogIndex: Float,
    val fluidityScore: Float,
    val correlatedMedication: MedicationRecord?,
    val isSignificant: Boolean,
    val advice: String?
)

/**
 * Engine responsible for analyzing speech patterns as a proxy for cognitive load.
 */
class NeuroLoadEngine(
    private val nanoEngine: GeminiNanoEngine
) {
    private val TAG = "NeuroLoadEngine"
    private val FILLER_WORDS = listOf("um", "uh", "ah", "like", "err")

    /**
     * Analyzes a list of speech segments to produce cognitive metrics.
     */
    suspend fun analyzeSpeech(segments: List<SpeechSegment>, fullText: String): CognitiveMetrics {
        val interWordLatencies = mutableListOf<Long>()
        var totalFillerCount = 0

        // 1. Basic Heuristic Analysis
        for (i in 0 until segments.size - 1) {
            val latency = segments[i + 1].startTimeMilli - segments[i].endTimeMilli
            if (latency > 0) {
                interWordLatencies.add(latency)
            }
            
            val lowerText = segments[i].text.lowercase().trim()
            if (FILLER_WORDS.contains(lowerText)) {
                totalFillerCount++
            }
        }

        val avgLatency = if (interWordLatencies.isNotEmpty()) interWordLatencies.average().toLong() else 0L
        
        // 2. Nano-Powered Semantic Audit
        val nanoAudit = nanoEngine.auditSemanticFluidity(fullText, avgLatency, totalFillerCount)
        
        // 3. Synthesis
        val fluidity = calculateFluidity(avgLatency, totalFillerCount)
        
        return CognitiveMetrics(
            fluidityScore = fluidity,
            averageInterWordLatencyMilli = avgLatency,
            fillerCount = totalFillerCount,
            circumlocutionDetected = nanoAudit?.contains("circumlocution: true", ignoreCase = true) == true,
            brainFogIndex = nanoAudit?.extractBrainFogIndex() ?: (1.0f - fluidity)
        )
    }

    /**
     * Correlates cognitive metrics with recent medication doses to find potential peaks.
     */
    fun correlateWithMeds(
        metrics: CognitiveMetrics,
        medications: List<MedicationRecord>,
        recentDoses: List<DoseLog>
    ): NeuroCognitiveInsight {
        val now = System.currentTimeMillis()
        
        // Find meds taken in the last 4 hours (potential peak window)
        val candidateDoses = recentDoses.filter { 
            it.status == "TAKEN" && it.actualTime != null && (now - it.actualTime!!) < 4 * 3600000L
        }

        val correlatedMed = candidateDoses.mapNotNull { dose ->
            medications.find { it.medicationId == dose.medicationId }
        }.firstOrNull()

        val isSignificant = metrics.brainFogIndex > 0.4f || metrics.fluidityScore < 0.7f

        val advice = if (isSignificant && correlatedMed != null) {
            "Detected a drop in speech fluidity coinciding with the peak concentration of ${correlatedMed.name}. Consider discussing a dose adjustment or timing shift with your doctor if this brain fog persists."
        } else if (isSignificant) {
            "Speech fluidity is lower than baseline. Monitor for fatigue or environmental stressors."
        } else null

        return NeuroCognitiveInsight(
            brainFogIndex = metrics.brainFogIndex,
            fluidityScore = metrics.fluidityScore,
            correlatedMedication = correlatedMed,
            isSignificant = isSignificant,
            advice = advice
        )
    }

    private fun calculateFluidity(avgLatency: Long, fillerCount: Int): Float {
        // Simple heuristic: Fluidity drops as latency and fillers increase
        val latencyPenalty = (avgLatency.toFloat() / 2000f).coerceIn(0f, 0.5f)
        val fillerPenalty = (fillerCount.toFloat() / 10f).coerceIn(0f, 0.5f)
        return (1.0f - latencyPenalty - fillerPenalty).coerceIn(0f, 1f)
    }

    private fun String.extractBrainFogIndex(): Float {
        val match = Regex("brain_fog_index:\\s*([0-9.]+)").find(this)
        return match?.groupValues?.get(1)?.toFloatOrNull() ?: 0.5f
    }
}
