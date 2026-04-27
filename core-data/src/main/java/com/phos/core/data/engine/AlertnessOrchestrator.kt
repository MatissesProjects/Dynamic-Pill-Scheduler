package com.phos.core.data.engine

import androidx.health.connect.client.records.SleepSessionRecord
import com.phos.core.data.model.SleepSubjectiveLog
import com.phos.core.data.model.MetabolicLoadLog
import java.time.Duration
import java.time.Instant

data class AlertnessIntervention(
    val title: String,
    val description: String,
    val suggestedActivity: String,
    val timeToDipMinutes: Long,
    val isHighRisk: Boolean
)

class AlertnessOrchestrator {

    /**
     * Constructs a prompt for Gemini Nano to predict nap vulnerability.
     */
    fun buildPredictionPrompt(
        tWakeEpoch: Long,
        recentSleep: List<SleepSessionRecord>,
        subjectiveSleep: List<SleepSubjectiveLog>,
        recentExercise: List<MetabolicLoadLog>
    ): String {
        val now = Instant.now()
        val hoursSinceWake = java.time.Duration.between(Instant.ofEpochMilli(tWakeEpoch), now).toHours()
        
        val sleepSummary = recentSleep.take(3).joinToString("; ") { 
            "${it.startTime} to ${it.endTime}"
        }
        val subjectiveSummary = subjectiveSleep.firstOrNull()?.let { 
            "Quality: ${it.reportedQuality}/10, Mood: ${it.morningMood}"
        } ?: "No subjective data"

        val exerciseSummary = recentExercise.joinToString("; ") {
            "Intensity: ${it.trimpScore}, Time: ${it.timestamp}"
        }

        return """
            You are a predictive chronobiology engine. 
            Current context:
            - Hours since wake: $hoursSinceWake
            - Recent objective sleep: $sleepSummary
            - Subjective morning feel: $subjectiveSummary
            - Recent metabolic load: $exerciseSummary
            
            Predict if a daytime nap is likely in the next 2 hours due to sleep pressure or circadian dips.
            Return JSON: {isVulnerable: Boolean, confidence: Float, predictedMinutesUntilDip: Int, reason: String}
        """.trimIndent()
    }
}
