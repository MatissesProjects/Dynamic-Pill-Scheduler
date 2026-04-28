package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NocturnalRespiratoryEngine {

    /**
     * T41: Nightly Respiration & Congestion Proxy
     * Detects orthopnea (difficulty breathing when flat) as a proxy for fluid.
     */
    fun analyzeCongestion(
        metrics: List<NocturnalRespiratoryMetric>
    ): CongestionInsight {
        val flatMetrics = metrics.filter { it.sleepPosition == SleepPosition.FLAT }
        val proppedMetrics = metrics.filter { it.sleepPosition == SleepPosition.PROPPED_UP }

        if (flatMetrics.isEmpty() || proppedMetrics.isEmpty()) {
            return CongestionInsight(
                date = getTodayDate(),
                orthopneaDetected = false,
                rrDeltaFlatVsPropped = 0.0,
                summary = "Insufficient position-diverse data for orthopnea detection."
            )
        }

        val avgRRFlat = flatMetrics.map { it.respiratoryRate }.average()
        val avgRRPropped = proppedMetrics.map { it.respiratoryRate }.average()
        
        // Orthopnea is characterized by higher RR when flat vs propped up.
        val rrDelta = avgRRFlat - avgRRPropped
        val orthopneaDetected = rrDelta > 3.0 // Threshold: 3 breaths per minute difference

        val summary = if (orthopneaDetected) {
            "Nocturnal Respiratory Strain: Elevated breathing rate detected while sleeping flat."
        } else {
            "Stable Nocturnal Respiration: No significant position-based respiratory variance."
        }

        val recommendation = if (orthopneaDetected) {
            "Potential sub-clinical congestion. Suggest propped-up sleep and clinical consultation if persistent."
        } else null

        return CongestionInsight(
            date = getTodayDate(),
            orthopneaDetected = orthopneaDetected,
            rrDeltaFlatVsPropped = rrDelta,
            summary = summary,
            recommendation = recommendation
        )
    }

    private fun getTodayDate(): String {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(Instant.now())
    }
}
