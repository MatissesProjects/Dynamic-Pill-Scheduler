package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Stores sentiment analysis results from voice logs using Gemini Nano.
 */
@Entity(tableName = "sentiment_logs")
data class SentimentLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val score: Float, // -1.0 to 1.0
    val primaryEmotion: String,
    val intensity: Int, // 1-10
    val timestamp: Instant = Instant.now()
)
