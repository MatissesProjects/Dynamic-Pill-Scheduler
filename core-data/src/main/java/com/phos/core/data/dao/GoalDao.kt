package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.HealthGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM health_goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveGoalsFlow(): Flow<List<HealthGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: HealthGoal)

    @Query("UPDATE health_goals SET isActive = 0 WHERE id = :goalId")
    suspend fun markGoalInactive(goalId: Long)
}
