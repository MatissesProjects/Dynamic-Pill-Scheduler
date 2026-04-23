package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.DoseLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs ORDER BY scheduledTime DESC")
    fun getAllLogs(): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId AND status = 'TAKEN' AND actualTime >= :since ORDER BY actualTime DESC")
    suspend fun getRecentTakenDoses(medicationId: String, since: Long): List<DoseLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DoseLog): Long

    @Query("UPDATE dose_logs SET status = :status, actualTime = :actualTime WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, actualTime: Long)
}
