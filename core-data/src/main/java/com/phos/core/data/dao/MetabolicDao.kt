package com.phos.core.data.dao

import androidx.room.*
import com.phos.core.data.model.MetabolicLoadLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface MetabolicDao {
    @Insert
    suspend fun insertLog(log: MetabolicLoadLog)

    @Query("SELECT * FROM metabolic_load_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<MetabolicLoadLog>>

    @Query("SELECT * FROM metabolic_load_logs WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getLogsSince(since: Instant): List<MetabolicLoadLog>

    @Query("SELECT MAX(trimpScore) FROM metabolic_load_logs WHERE timestamp >= :since")
    suspend fun getMaxTrimpSince(since: Instant): Double?
}
