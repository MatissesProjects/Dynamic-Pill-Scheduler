package com.phos.core.intelligence

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phos.core.data.db.PhosDatabase
import com.phos.core.data.datastore.phosDataStore
import androidx.room.Room
import kotlinx.coroutines.flow.first

class IntelligenceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = Room.databaseBuilder(
            applicationContext,
            PhosDatabase::class.java,
            "phos-db"
        ).build()
        
        val engine = SymptomCorrelationEngine(database.intelligenceDao())
        
        return try {
            val insight = engine.analyzeCorrelations()
            
            // Save to DataStore
            applicationContext.phosDataStore.updateData { currentState ->
                currentState.toBuilder()
                    .setLastAiInsight(insight)
                    .build()
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
