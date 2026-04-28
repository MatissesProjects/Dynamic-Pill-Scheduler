package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.DreamLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DreamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DreamLog): Long

    @Query("SELECT * FROM dream_logs WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<DreamLog>

    @Query("SELECT * FROM dream_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<DreamLog>>
    
    @Query("SELECT * FROM dream_logs WHERE date >= :startDate ORDER BY timestamp DESC")
    suspend fun getLogsSince(startDate: String): List<DreamLog>
}
