package com.phos.core.data.dao

import androidx.room.*
import com.phos.core.data.model.EnvironmentalLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface EnvironmentalDao {
    @Query("SELECT * FROM environmental_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<EnvironmentalLog>>

    @Query("SELECT * FROM environmental_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getLogsSince(since: Instant): List<EnvironmentalLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: EnvironmentalLog)
}
