package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.phos.core.data.model.MedicationRecord

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE validTo = :maxLong")
    suspend fun getAllActiveMedications(maxLong: Long = Long.MAX_VALUE): List<MedicationRecord>

    @Query("SELECT * FROM medications WHERE medicationId = :medicationId AND validTo = :maxLong")
    suspend fun getActiveMedicationById(medicationId: String, maxLong: Long = Long.MAX_VALUE): MedicationRecord?

    @Insert
    suspend fun insert(record: MedicationRecord): Long

    @Transaction
    suspend fun updateMedication(newRecord: MedicationRecord) {
        val currentTime = System.currentTimeMillis()
        val oldRecord = getActiveMedicationById(newRecord.medicationId)
        if (oldRecord != null) {
            markAsInactive(oldRecord.id, currentTime)
        }
        insert(newRecord.copy(validFrom = currentTime, validTo = Long.MAX_VALUE))
    }

    @Query("UPDATE medications SET validTo = :timestamp WHERE id = :id")
    suspend fun markAsInactive(id: Long, timestamp: Long)
}
