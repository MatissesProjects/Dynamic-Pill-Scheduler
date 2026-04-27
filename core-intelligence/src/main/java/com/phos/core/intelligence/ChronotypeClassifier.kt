package com.phos.core.intelligence

import com.phos.core.data.model.Chronotype
import com.phos.core.data.model.ChronotypeRecord
import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ChronotypeClassifier {

    /**
     * Calculates MSFsc (Mid-sleep on free days, sleep-debt corrected)
     * Formula: MSFsc = MSF - (SDf - SDw) / 2
     */
    fun classify(records: List<SleepSessionRecord>): ChronotypeRecord {
        if (records.size < 7) {
            return ChronotypeRecord(type = Chronotype.UNKNOWN, midSleepTimeMillis = 0L, confidence = 0.1f)
        }

        val zoneId = ZoneId.systemDefault()
        
        // Group by day of week (1-5 = Work, 6-7 = Free)
        val workDaySleep = mutableListOf<Duration>()
        val freeDaySleep = mutableListOf<Duration>()
        val freeDayMidPoints = mutableListOf<Long>() // Millis relative to midnight

        records.forEach { record ->
            val start = ZonedDateTime.ofInstant(record.startTime, zoneId)
            val duration = Duration.between(record.startTime, record.endTime)
            val isFreeDay = start.dayOfWeek.value >= 6 // Sat/Sun

            if (isFreeDay) {
                freeDaySleep.add(duration)
                // Mid point calculation
                val midPoint = record.startTime.plus(duration.dividedBy(2))
                val midZoned = ZonedDateTime.ofInstant(midPoint, zoneId)
                val millisSinceMidnight = midZoned.toLocalTime().toNanoOfDay() / 1_000_000
                freeDayMidPoints.add(millisSinceMidnight)
            } else {
                workDaySleep.add(duration)
            }
        }

        if (freeDaySleep.isEmpty()) {
            return ChronotypeRecord(type = Chronotype.UNKNOWN, midSleepTimeMillis = 0L, confidence = 0.2f)
        }

        val avgSDf = freeDaySleep.map { it.toMillis().toDouble() }.average()
        val avgSDw = if (workDaySleep.isNotEmpty()) workDaySleep.map { it.toMillis().toDouble() }.average() else avgSDf
        val avgMSF = freeDayMidPoints.map { it.toDouble() }.average()

        // MSFsc Calculation (Corrected for oversleep on weekends)
        val msfSc = avgMSF - (avgSDf - avgSDw) / 2.0
        
        // Convert msfSc (millis from midnight) to a Chronotype
        // Thresholds: Lark < 3 AM (10,800,000ms), Owl > 5 AM (18,000,000ms)
        val type = when {
            msfSc < 10800000 -> Chronotype.LARK
            msfSc > 18000000 -> Chronotype.OWL
            else -> Chronotype.NEUTRAL
        }

        return ChronotypeRecord(
            type = type,
            midSleepTimeMillis = msfSc.toLong(),
            confidence = (records.size.coerceAtMost(14) / 14f)
        )
    }
}
