package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class REMSafetyEngine {

    /**
     * M1: REM fragmentation detection.
     * Identifies "Awake" spikes that occur during or immediately adjacent to REM blocks.
     */
    fun calculateFragmentationIndex(
        samples: List<SleepStageSample>
    ): REMFragmentationInsight {
        val sortedSamples = samples.sortedBy { it.startTime }
        var totalRemDuration = Duration.ZERO
        var awakeSpikesInRem = 0
        
        for (i in sortedSamples.indices) {
            val current = sortedSamples[i]
            if (current.stage == SleepStage.REM) {
                totalRemDuration = totalRemDuration.plus(Duration.between(current.startTime, current.endTime))
            } else if (current.stage == SleepStage.AWAKE) {
                // Check if this AWAKE spike is adjacent to REM
                val prev = if (i > 0) sortedSamples[i - 1] else null
                val next = if (i < sortedSamples.size - 1) sortedSamples[i + 1] else null
                
                if (prev?.stage == SleepStage.REM || next?.stage == SleepStage.REM) {
                    awakeSpikesInRem++
                }
            }
        }
        
        val remMinutes = totalRemDuration.toMinutes().toInt()
        // Heuristic: More than 1 spike per 20 mins of REM is excessive
        val fragmentationIndex = if (remMinutes > 0) {
            (awakeSpikesInRem.toDouble() / (remMinutes.toDouble() / 20.0)).coerceIn(0.0, 1.0)
        } else 0.0
        
        val today = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(Instant.now())
        
        return REMFragmentationInsight(
            date = today,
            fragmentationIndex = fragmentationIndex,
            awakeSpikeCount = awakeSpikesInRem,
            totalRemMinutes = remMinutes,
            isExcessive = fragmentationIndex > 0.7
        )
    }

    /**
     * M3: Sleep Restoration Audit builder.
     */
    fun buildRestorationAudit(
        insight: REMFragmentationInsight,
        dreamIntensity: Int? = null
    ): SleepRestorationAudit {
        val stabilityScore = Math.round((1.0 - insight.fragmentationIndex) * 100).toInt()
        
        val message = when {
            insight.isExcessive && (dreamIntensity ?: 0) > 7 -> 
                "High REM-Rebound: Fragmented REM (${insight.awakeSpikeCount} spikes) correlates with your vivid dreams. This is a known beta-blocker side effect."
            insight.isExcessive -> 
                "Fragmented REM: Your REM blocks were interrupted multiple times. You may feel less mentally restored today."
            else -> "Stable REM: Your REM architecture was preserved, supporting healthy cognitive restoration."
        }
        
        return SleepRestorationAudit(
            date = insight.date,
            remStabilityScore = stabilityScore,
            dreamIntensity = dreamIntensity,
            restorationMessage = message
        )
    }
}
