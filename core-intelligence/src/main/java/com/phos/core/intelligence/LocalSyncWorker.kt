package com.phos.core.intelligence

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.phos.core.data.db.PhosDatabase
import com.phos.core.data.datastore.phosDataStore
import com.phos.core.intelligence.proto.RelayRequest
import com.phos.core.intelligence.proto.SymptomDelta
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Worker responsible for offloading biometric data to a local LLM node during overnight charging.
 */
class LocalSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("LocalSyncWorker", "Starting overnight local sync...")
        
        val relayManager = BiometricRelayManager(applicationContext)
        val database = Room.databaseBuilder(
            applicationContext,
            PhosDatabase::class.java,
            "phos-db"
        ).build()

        return try {
            // 1. Discovery
            relayManager.startDiscovery()
            // Wait for discovery (timeout 30s)
            val node = withTimeoutOrNull(30000L) {
                while (relayManager.discoveredNode.value == null) {
                    delay(1000L)
                }
                relayManager.discoveredNode.value
            }
            relayManager.stopDiscovery()

            if (node == null) {
                Log.w("LocalSyncWorker", "No local node found. Skipping sync.")
                return Result.retry()
            }

            // 2. Auth
            if (!relayManager.authenticate(node)) {
                Log.e("LocalSyncWorker", "Authentication failed.")
                return Result.failure()
            }

            // 3. Gather Data (Last 24h of symptoms)
            val since = Instant.now().minus(Duration.ofDays(1))
            val symptoms = database.intelligenceDao().getRecentSymptoms(since)
            val request = RelayRequest.newBuilder()
                .setDeviceId(android.os.Build.ID)
                .setTimestamp(System.currentTimeMillis())
                .addAllSymptoms(symptoms.map { 
                    SymptomDelta.newBuilder()
                        .setType(it.symptomName)
                        .setTimestamp(it.timestamp.toEpochMilli())
                        .setSeverity(it.severity)
                        .setValue(it.notes ?: "")
                        .build()
                })
                .build()

            // 4. Offload
            val response = relayManager.offloadData(request)
            
            if (response != null) {
                Log.i("LocalSyncWorker", "Sync successful: ${response.insightSummary}")
                
                // 5. Update Database with adjustments
                database.medicationDao().let { dao ->
                    response.adjustmentsList.forEach { adjustment ->
                        val current = dao.getActiveMedicationById(adjustment.medicationId)
                        if (current != null) {
                            Log.d("LocalSyncWorker", "Applying adjustment for ${current.name}: new offset ${adjustment.suggestedTime}")
                            dao.updateMedication(current.copy(
                                id = 0, // Reset ID for new version
                                frequencyOffset = adjustment.suggestedTime
                            ))
                        }
                    }
                }

                // 6. Save Insight to DataStore
                applicationContext.phosDataStore.updateData { currentState ->
                    currentState.toBuilder()
                        .setLastAiInsight(response.insightSummary)
                        .build()
                }

                // 7. Trigger Notification (Morning Insight)
                NotificationHelper.showMorningInsight(applicationContext, response.insightSummary)
                
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("LocalSyncWorker", "Sync failed", e)
            Result.retry()
        } finally {
            database.close()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build()

            // Calculate delay to 02:00 AM
            val now = LocalDateTime.now()
            var nextTwoAM = now.with(LocalTime.of(2, 0))
            if (now.isAfter(nextTwoAM)) {
                nextTwoAM = nextTwoAM.plusDays(1)
            }
            val delay = Duration.between(now, nextTwoAM)

            val workRequest = OneTimeWorkRequestBuilder<LocalSyncWorker>()
                .setConstraints(constraints)
                .setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES)
                .addTag("local_sync")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.i("LocalSyncWorker", "Scheduled for $nextTwoAM (Delay: ${delay.toMinutes()} mins)")
        }
    }
}
