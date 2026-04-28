package com.phos.core.data.dao

import androidx.room.*
import com.phos.core.data.model.PostureLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface PostureDao {
    @Query("SELECT * FROM posture_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<PostureLog>>

    @Query("SELECT * FROM posture_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getLogsSince(since: Instant): List<PostureLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PostureLog)
}
