package com.phos.core.data.dao

import androidx.room.*
import com.phos.core.data.model.AlcoholLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface AlcoholDao {
    @Query("SELECT * FROM alcohol_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<AlcoholLog>>

    @Query("SELECT * FROM alcohol_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getLogsSince(since: Instant): List<AlcoholLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AlcoholLog)
}
