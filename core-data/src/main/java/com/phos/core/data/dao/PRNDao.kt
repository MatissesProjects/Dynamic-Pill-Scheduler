package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.phos.core.data.model.PRNMedication
import kotlinx.coroutines.flow.Flow

@Dao
interface PRNDao {
    @Query("SELECT * FROM prn_medications WHERE validTo = :maxLong")
    fun getAllActivePRNMedicationsFlow(maxLong: Long = Long.MAX_VALUE): Flow<List<PRNMedication>>

    @Query("SELECT * FROM prn_medications WHERE validTo = :maxLong")
    suspend fun getAllActivePRNMedications(maxLong: Long = Long.MAX_VALUE): List<PRNMedication>

    @Query("SELECT * FROM prn_medications WHERE medicationId = :medicationId AND validTo = :maxLong")
    suspend fun getActivePRNMedicationById(medicationId: String, maxLong: Long = Long.MAX_VALUE): PRNMedication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prnMedication: PRNMedication): Long

    @Transaction
    suspend fun updatePRNMedication(newRecord: PRNMedication) {
        val currentTime = System.currentTimeMillis()
        val oldRecord = getActivePRNMedicationById(newRecord.medicationId)
        if (oldRecord != null) {
            markAsInactive(oldRecord.id, currentTime)
        }
        insert(newRecord.copy(validFrom = currentTime, validTo = Long.MAX_VALUE))
    }

    @Query("UPDATE prn_medications SET validTo = :timestamp WHERE id = :id")
    suspend fun markAsInactive(id: Long, timestamp: Long)

    @Query("DELETE FROM prn_medications WHERE id = :id")
    suspend fun deletePermanently(id: Long)
}
