package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.TemporalAnchor

@Dao
interface TemporalAnchorDao {
    @Query("SELECT * FROM temporal_anchors WHERE date = :date")
    suspend fun getAnchorForDate(date: String): TemporalAnchor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchor(anchor: TemporalAnchor)

    @Query("SELECT * FROM temporal_anchors ORDER BY wakeTime DESC LIMIT 1")
    fun getLatestAnchorFlow(): kotlinx.coroutines.flow.Flow<TemporalAnchor?>

    @Query("SELECT * FROM temporal_anchors ORDER BY wakeTime DESC LIMIT 1")
    suspend fun getLatestAnchor(): TemporalAnchor?
}
