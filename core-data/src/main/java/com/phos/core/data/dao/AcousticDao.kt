package com.phos.core.data.dao

import androidx.room.*
import com.phos.core.data.model.AcousticLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AcousticDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AcousticLog)

    @Query("SELECT * FROM acoustic_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<AcousticLog>>

    @Query("SELECT * FROM acoustic_logs WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getLogsSince(since: Long): List<AcousticLog>

    @Query("DELETE FROM acoustic_logs WHERE timestamp < :threshold")
    suspend fun deleteOldLogs(threshold: Long)
}
