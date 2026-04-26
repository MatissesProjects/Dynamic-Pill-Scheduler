package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.phos.core.data.model.GaitLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface GaitDao {
    @Insert
    suspend fun insertLog(log: GaitLog)

    @Query("SELECT * FROM gait_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<GaitLog>>

    @Query("SELECT * FROM gait_logs WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getLogsSince(since: Instant): List<GaitLog>

    @Query("SELECT AVG(strideLengthMeters) FROM gait_logs WHERE timestamp >= :since")
    suspend fun getAverageStrideLength(since: Instant): Double?
}
