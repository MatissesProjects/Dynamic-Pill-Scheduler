package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.phos.core.data.model.CaffeineLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CaffeineDao {
    @Insert
    suspend fun insertLog(log: CaffeineLog)

    @Query("SELECT * FROM caffeine_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<CaffeineLog>>

    @Query("SELECT * FROM caffeine_logs WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getLogsSince(since: Instant): List<CaffeineLog>
}
