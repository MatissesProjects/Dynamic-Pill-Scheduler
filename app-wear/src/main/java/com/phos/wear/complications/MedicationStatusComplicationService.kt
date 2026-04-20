package com.phos.wear.complications

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.phos.core.data.datastore.phosDataStore
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first

class MedicationStatusComplicationService : SuspendingComplicationDataSourceService() {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) return null
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("09:00").build(),
            contentDescription = PlainComplicationText.Builder("Next Medication").build()
        ).setTitle(PlainComplicationText.Builder("Next").build()).build()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val state = applicationContext.phosDataStore.data.first()
        val nextMed = state.medicationsList.firstOrNull { it.status == "PENDING" }
        
        val displayTime = if (nextMed != null) {
            timeFormatter.format(Instant.ofEpochMilli(nextMed.scheduledTime))
        } else {
            "--:--"
        }

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(displayTime).build(),
            contentDescription = PlainComplicationText.Builder("Next Medication").build()
        ).setTitle(PlainComplicationText.Builder("Next").build()).build()
    }
}
