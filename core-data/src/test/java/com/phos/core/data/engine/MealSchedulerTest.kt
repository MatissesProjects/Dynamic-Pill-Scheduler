package com.phos.core.data.engine

import com.phos.core.data.model.AppetiteLog
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import com.phos.core.data.proto.MealPreferences
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class MealSchedulerTest {

    private val scheduler = MealScheduler()
    private val anchor = TemporalAnchor(date = "2026-04-25", wakeTime = 1000000L, source = "Manual")

    @Test
    fun `test findOptimalEatingWindows with simple gap`() {
        val medications = listOf(
            MedicationRecord(medicationId = "1", name = "Med A", dosage = "10mg", frequencyOffset = 1 * 3600000L, validFrom = 0L),
            MedicationRecord(medicationId = "2", name = "Med B", dosage = "10mg", frequencyOffset = 4 * 3600000L, validFrom = 0L)
        )
        
        val result = scheduler.findOptimalEatingWindows(medications, anchor, emptyList())
        
        assertTrue(result.any { it.reason.contains("Uninterrupted gap") })
    }

    @Test
    fun `test with food requirement creates specific window`() {
        val medications = listOf(
            MedicationRecord(medicationId = "1", name = "Med A", dosage = "10mg", frequencyOffset = 2 * 3600000L, validFrom = 0L, foodRequirement = "WITH_FOOD")
        )
        
        val result = scheduler.findOptimalEatingWindows(medications, anchor, emptyList())
        
        assertTrue(result.any { it.reason.contains("Optimal window to take Med A with food") })
    }

    @Test
    fun `test empty stomach requirement shrinks gap`() {
        val medications = listOf(
            MedicationRecord(medicationId = "1", name = "Med A", dosage = "10mg", frequencyOffset = 1 * 3600000L, validFrom = 0L),
            MedicationRecord(medicationId = "2", name = "Med B", dosage = "10mg", frequencyOffset = 3 * 3600000L, validFrom = 0L, foodRequirement = "EMPTY_STOMACH")
        )
        
        val result = scheduler.findOptimalEatingWindows(medications, anchor, emptyList())
        
        assertFalse(result.any { it.reason.contains("Uninterrupted gap") })
    }

    @Test
    fun `test high difficulty appetite creates sacred window`() {
        val medications = listOf(
            MedicationRecord(medicationId = "1", name = "Med A", dosage = "10mg", frequencyOffset = 1 * 3600000L, validFrom = 0L),
            MedicationRecord(medicationId = "2", name = "Med B", dosage = "10mg", frequencyOffset = 5 * 3600000L, validFrom = 0L)
        )
        val appetiteHistory = listOf(
            AppetiteLog(hungerLevel = 5, difficultyLevel = 9, timestamp = Instant.now())
        )
        
        val result = scheduler.findOptimalEatingWindows(medications, anchor, appetiteHistory)
        
        assertTrue(result.any { it.isSacred })
    }

    @Test
    fun `test meal preferences boost score`() {
        val medications = listOf(
            MedicationRecord(medicationId = "1", name = "Med A", dosage = "10mg", frequencyOffset = 1 * 3600000L, validFrom = 0L),
            MedicationRecord(medicationId = "2", name = "Med B", dosage = "10mg", frequencyOffset = 5 * 3600000L, validFrom = 0L)
        )
        val mealPreferences: MealPreferences = mock()
        whenever(mealPreferences.hasLunchStartOffset()).thenReturn(true)
        whenever(mealPreferences.hasLunchEndOffset()).thenReturn(true)
        whenever(mealPreferences.lunchStartOffset).thenReturn(3 * 3600000L)
        whenever(mealPreferences.lunchEndOffset).thenReturn(4 * 3600000L)
        
        val result = scheduler.findOptimalEatingWindows(medications, anchor, emptyList(), mealPreferences)
        
        val window = result.first { it.reason.contains("Uninterrupted gap") }
        assertEquals(10, window.score)
    }
}
