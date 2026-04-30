package com.phos.core.data.engine

import com.phos.core.data.model.AppetiteLog
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class EndocrineAlignmentTest {

    @Test
    fun testThyroidAbsorptionWarning_WhenMealLogged() {
        val engine = HormonalSyncEngine()
        val wakeTime = Instant.parse("2024-01-01T06:00:00Z").toEpochMilli()
        val anchor = TemporalAnchor("2024-01-01", wakeTime, "HealthConnect")

        val thyroidMed = MedicationRecord(
            id = 1,
            medicationId = "med_thyroid_1",
            name = "Levothyroxine",
            dosage = "50mcg",
            frequencyOffset = 0, // taken immediately at wake
            category = "Thyroid",
            validFrom = wakeTime
        )

        // Meal logged 15 mins after wake time
        val mealTime = Instant.ofEpochMilli(wakeTime + 15 * 60 * 1000L)
        val mealLogs = listOf(
            AppetiteLog(hungerLevel = 8, difficultyLevel = 1, timestamp = mealTime)
        )

        val report = engine.evaluateHormonalHarmony(listOf(thyroidMed), anchor, mealLogs)

        val warning = report.alerts.find { it.type == "ABSORPTION_WARNING" }
        assertTrue("Expected ABSORPTION_WARNING alert", warning != null)
        assertEquals("Thyroid medication taken too close to a meal. Absorption may be impaired.", warning?.message)
    }

    @Test
    fun testTWakeShift_ProactivelyReschedules_AndFlagsMisaligned() {
        val engine = HormonalSyncEngine()
        val originalWakeTime = Instant.parse("2024-01-01T06:00:00Z").toEpochMilli()
        
        // Cortisol aligned with original 6 AM wake (35 mins post-wake)
        val cortisolMed = MedicationRecord(
            id = 2,
            medicationId = "med_cortisol_1",
            name = "Hydrocortisone",
            dosage = "10mg",
            frequencyOffset = 35 * 60 * 1000L,
            category = "Cortisol",
            validFrom = originalWakeTime
        )

        // Shift T-Wake by +4 hours (e.g. 10 AM wake up on weekend)
        val shiftedWakeTime = originalWakeTime + (4 * 60 * 60 * 1000L)
        val shiftedAnchor = TemporalAnchor("2024-01-01", shiftedWakeTime, "HealthConnect")

        // Because frequencyOffset is fixed to 35 mins, the dose "proactively reschedules" relative to the new T-Wake.
        // It stays aligned with the CAR. 
        var report = engine.evaluateHormonalHarmony(listOf(cortisolMed), shiftedAnchor)
        assertEquals("Should be perfectly aligned with CAR", 100, report.alignmentScore)
        assertTrue("No alerts expected", report.alerts.isEmpty())
        
        // But what if T-Wake shifts so drastically that an evening dose becomes late-night?
        val eveningCortisol = MedicationRecord(
            id = 3,
            medicationId = "med_cortisol_2",
            name = "Hydrocortisone (Evening)",
            dosage = "5mg",
            frequencyOffset = 12 * 60 * 60 * 1000L, // 12 hours post wake
            category = "Cortisol",
            validFrom = originalWakeTime
        )
        
        // If wake up is 10 AM + 12 hours = 10 PM. 
        // 10 PM is late night and close to dayEnd (10 AM + 16h = 2 AM).
        // Let's test a very late shift
        val veryLateAnchor = TemporalAnchor("2024-01-01", originalWakeTime + (6 * 60 * 60 * 1000L), "HealthConnect")
        // Wake up 12 PM. Dose is 12 hours later -> 12 AM.
        val reportLate = engine.evaluateHormonalHarmony(listOf(eveningCortisol), veryLateAnchor)
        val lateWarning = reportLate.alerts.find { it.type == "SYNC_ALERT" }
        
        assertTrue("Expected late night cortisol warning", lateWarning != null)
    }

    @Test
    fun testDawnPhenomenonAlert() {
        val engine = HormonalSyncEngine()
        val wakeTime = Instant.parse("2024-01-01T06:00:00Z").toEpochMilli()
        val anchor = TemporalAnchor("2024-01-01", wakeTime, "HealthConnect")

        val insulinMed = MedicationRecord(
            id = 4,
            medicationId = "med_insulin_1",
            name = "Insulin",
            dosage = "10u",
            frequencyOffset = 120 * 60 * 1000L, // 2 hours post wake
            category = "Insulin",
            validFrom = wakeTime
        )

        val report = engine.evaluateHormonalHarmony(listOf(insulinMed), anchor)

        val dawnAlert = report.alerts.find { it.type == "DAWN_PHENOMENON" }
        assertTrue("Expected DAWN_PHENOMENON alert", dawnAlert != null)
    }
}
