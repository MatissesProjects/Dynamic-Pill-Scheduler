package com.phos.core.intelligence

import android.graphics.Bitmap

/**
 * Interface to wrap on-device AI operations, enabling easier testing and abstraction from specific SDKs.
 */
interface PhosAiClient {
    suspend fun generateResponse(prompt: String): String?
    suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String?
}
