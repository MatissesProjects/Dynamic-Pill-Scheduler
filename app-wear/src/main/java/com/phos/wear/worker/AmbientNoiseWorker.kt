package com.phos.wear.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phos.core.data.datastore.phosDataStore
import com.phos.core.data.sync.DataLayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.math.sqrt

class AmbientNoiseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val dataLayerRepository = DataLayerRepository(context, context.phosDataStore)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext Result.failure()
        }

        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            return@withContext Result.retry()
        }

        try {
            audioRecord.startRecording()
            val buffer = ShortArray(bufferSize)
            var totalRms = 0.0
            val readCount = 10 // Read 10 buffers (approx 0.5s - 1s)

            for (i in 0 until readCount) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    var sum = 0.0
                    for (j in 0 until read) {
                        sum += buffer[j] * buffer[j]
                    }
                    totalRms += sqrt(sum / read)
                }
            }

            val avgRms = totalRms / readCount
            // Convert RMS to dB (Simplified)
            // dB = 20 * log10(RMS / referenceValue)
            // Using 1.0 as a reference for normalized short range
            val db = if (avgRms > 0) 20 * log10(avgRms / 32767.0) + 90 else 0.0
            
            dataLayerRepository.updateAcousticDb(db)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }
}
