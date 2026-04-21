package com.phos.core.data.engine

import com.phos.core.data.dao.BiometricDao
import com.phos.core.data.model.BiometricBaseline
import com.phos.core.data.model.BiometricLog
import com.phos.core.data.model.BiometricType
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class DigitalTwinEngine @Inject constructor(
    private val biometricDao: BiometricDao
) {
    /**
     * Updates the biometric baseline for a medication based on post-dose data.
     * Typically called after a confirmed dose log.
     */
    suspend fun updateBaseline(medicationId: String, doseTimestamp: Instant) {
        val windowEnd = doseTimestamp.plus(2, ChronoUnit.HOURS)
        val hrLogs = biometricDao.getLogsSince(BiometricType.HEART_RATE, doseTimestamp)
            .filter { it.timestamp.isBefore(windowEnd) }
        val hrvLogs = biometricDao.getLogsSince(BiometricType.HRV, doseTimestamp)
            .filter { it.timestamp.isBefore(windowEnd) }

        if (hrLogs.isEmpty() || hrvLogs.isEmpty()) return

        val avgHr = hrLogs.map { it.value }.average()
        val avgHrv = hrvLogs.map { it.value }.average()

        val existingBaseline = biometricDao.getBaselineForMedication(medicationId)
        if (existingBaseline == null) {
            biometricDao.updateBaseline(
                BiometricBaseline(
                    medicationId = medicationId,
                    averageHrPostDose = avgHr,
                    averageHrvPostDose = avgHrv,
                    sampleSize = 1
                )
            )
        } else {
            val newSize = existingBaseline.sampleSize + 1
            val updatedHr = ((existingBaseline.averageHrPostDose * existingBaseline.sampleSize) + avgHr) / newSize
            val updatedHrv = ((existingBaseline.averageHrvPostDose * existingBaseline.sampleSize) + avgHrv) / newSize
            
            biometricDao.updateBaseline(
                existingBaseline.copy(
                    averageHrPostDose = updatedHr,
                    averageHrvPostDose = updatedHrv,
                    sampleSize = newSize,
                    lastUpdated = Instant.now()
                )
            )
        }
    }

    /**
     * Predicts the expected heart rate shift for a medication.
     */
    suspend fun getExpectedResponse(medicationId: String): Pair<Double, Double>? {
        val baseline = biometricDao.getBaselineForMedication(medicationId) ?: return null
        return Pair(baseline.averageHrPostDose, baseline.averageHrvPostDose)
    }

    /**
     * Checks if current biometric data is an anomaly compared to the digital twin.
     */
    fun isAnomaly(currentValue: Double, expectedValue: Double, threshold: Double = 0.20): Boolean {
        val diff = Math.abs(currentValue - expectedValue)
        return (diff / expectedValue) > threshold
    }
}
