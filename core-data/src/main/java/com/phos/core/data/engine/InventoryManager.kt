package com.phos.core.data.engine

import com.phos.core.data.dao.InventoryDao
import com.phos.core.data.model.InventoryRecord
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class InventoryManager @Inject constructor(
    private val inventoryDao: InventoryDao
) {
    /**
     * Decrements the pill count for a medication after a dose is taken.
     */
    suspend fun processDoseTaken(medicationId: String) {
        val record = inventoryDao.getInventoryForMedication(medicationId) ?: return
        inventoryDao.decrementPillCount(medicationId, record.pillsPerDose)
    }

    /**
     * Predicts the date when the medication will run out.
     * accounts for doses per day and current inventory.
     */
    suspend fun predictDepletionDate(medicationId: String, dosesPerDay: Int): Instant? {
        val record = inventoryDao.getInventoryForMedication(medicationId) ?: return null
        if (dosesPerDay <= 0) return null
        
        val daysRemaining = record.currentPillCount / (record.pillsPerDose * dosesPerDay)
        return Instant.now().plus(daysRemaining.toLong(), ChronoUnit.DAYS)
    }

    /**
     * Checks if a refill is needed based on the threshold.
     */
    suspend fun isRefillNeeded(medicationId: String): Boolean {
        val record = inventoryDao.getInventoryForMedication(medicationId) ?: return false
        return record.currentPillCount <= record.refillThreshold
    }
}
