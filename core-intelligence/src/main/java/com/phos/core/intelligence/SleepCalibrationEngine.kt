package com.phos.core.intelligence

import com.phos.core.data.model.SleepSubjectiveLog
import java.time.Duration
import java.time.Instant

data class SleepCalibrationInsight(
    val title: String,
    val description: String,
    val recommendedShiftMinutes: Int = 0,
    val needsScheduleAudit: Boolean = false
)

class SleepCalibrationEngine {

    /**
     * Calibrates objective data from Fitbit/Health Connect with subjective user feeling.
     */
    fun calibrate(
        objectiveDurationMillis: Long,
        subjectiveLog: SleepSubjectiveLog
    ): SleepCalibrationInsight? {
        val objectiveHours = objectiveDurationMillis / 3600000.0
        val isPoorSubjective = subjectiveLog.reportedQuality <= 4 || subjectiveLog.restfulnessRating <= 4
        
        // Scenario 1: "Ghost Rest" - Fitbit thinks you slept, but you feel exhausted.
        if (objectiveHours >= 7.0 && isPoorSubjective) {
            return SleepCalibrationInsight(
                title = "Sleep Discrepancy Detected",
                description = "Fitbit reported a full 7+ hours of sleep, but you reported feeling poor/unrested. This might indicate poor sleep quality from late-night medication side effects or digestion issues.",
                recommendedShiftMinutes = -60, // Suggest shifting things earlier
                needsScheduleAudit = true
            )
        }

        // Scenario 2: "Verified Exhaustion" - Both data and user agree it was bad.
        if (objectiveHours < 5.0 && isPoorSubjective) {
            return SleepCalibrationInsight(
                title = "Restorative Recovery Needed",
                description = "Both your Fitbit data and your morning check-in confirm a short, poor night. We recommend reserving an extra-long 'Sacred Eating Window' today to help with metabolism.",
                needsScheduleAudit = true
            )
        }

        return null
    }
}
