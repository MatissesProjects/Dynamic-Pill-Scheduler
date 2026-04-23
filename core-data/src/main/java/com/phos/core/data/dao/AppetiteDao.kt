package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.phos.core.data.model.AppetiteLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface AppetiteDao {
    @Insert
    suspend fun insertAppetiteLog(log: AppetiteLog)

    @Query("SELECT * FROM appetite_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getAppetiteLogsSince(since: Instant): Flow<List<AppetiteLog>>

    @Query("SELECT * FROM appetite_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentAppetiteLogs(limit: Int): List<AppetiteLog>
}
