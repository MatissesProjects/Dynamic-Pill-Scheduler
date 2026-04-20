package com.phos.wear.complications

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Wear OS 5 Complication for zero-tap medication status.
 */
class MedicationStatusComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) return null
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("9:00").build(),
            contentDescription = PlainComplicationText.Builder("Next Medication").build()
        ).setTitle(PlainComplicationText.Builder("Next").build())
            .build()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        // In a real scenario, we would pull the latest state from DataLayerRepository
        // For now, return a placeholder
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("10:30").build(),
            contentDescription = PlainComplicationText.Builder("Next Medication").build()
        ).setTitle(PlainComplicationText.Builder("Next").build())
            .build()
    }
}
