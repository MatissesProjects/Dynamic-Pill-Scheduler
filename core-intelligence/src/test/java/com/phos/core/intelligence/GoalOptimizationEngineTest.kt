package com.phos.core.intelligence

import com.phos.core.data.model.HealthGoal
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.proto.MealPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GoalOptimizationEngineTest {

    private val engine = GoalOptimizationEngine()

    @Test
    fun `test nocturia optimization with diuretics`() {
        val medications = listOf(
            MedicationRecord(medicationId = "med_1", name = "Furosemide", dosage = "20mg", frequencyOffset = 10 * 3600000L, validFrom = 0L)
        )
        val mealPreferences: MealPreferences = mock()
        
        val result = engine.evaluateGoals(
            goals = emptyList(),
            medications = medications,
            mealPreferences = mealPreferences,
            tWakeEpoch = 0L,
            nocturiaCount = 2
        )

        assertEquals(1, result.size)
        assertTrue(result[0].description.contains("bathroom breaks"))
        assertEquals(1, result[0].suggestedMedicationShifts.size)
        assertEquals(4 * 3600000L, result[0].suggestedMedicationShifts["med_1"])
    }

    @Test
    fun `test stomach irritation optimization with late meds`() {
        val goals = listOf(
            HealthGoal(id = 1, description = "Test", targetSymptom = "Stomach Pain", targetTimeOffset = null, targetTimeOfDay = null, isActive = true)
        )
        val medications = listOf(
            MedicationRecord(medicationId = "med_2", name = "Aspirin", dosage = "100mg", frequencyOffset = 14 * 3600000L, validFrom = 0L)
        )
        val mealPreferences: MealPreferences = mock()
        
        val result = engine.evaluateGoals(
            goals = goals,
            medications = medications,
            mealPreferences = mealPreferences,
            tWakeEpoch = 0L,
            nocturiaCount = 0
        )

        assertEquals(1, result.size)
        assertTrue(result[0].description.contains("evening medications earlier"))
        assertEquals(12 * 3600000L, result[0].suggestedMedicationShifts["med_2"])
    }

    @Test
    fun `test stomach irritation with late dinner`() {
        val goals = listOf(
            HealthGoal(id = 1, description = "Test", targetSymptom = "Gas", targetTimeOffset = null, targetTimeOfDay = null, isActive = true)
        )
        val mealPreferences: MealPreferences = mock()
        whenever(mealPreferences.hasDinnerStartOffset()).thenReturn(true)
        whenever(mealPreferences.dinnerStartOffset).thenReturn(16 * 3600000L)
        
        val result = engine.evaluateGoals(
            goals = goals,
            medications = emptyList(),
            mealPreferences = mealPreferences,
            tWakeEpoch = 0L,
            nocturiaCount = 0
        )

        assertEquals(1, result.size)
        assertTrue(result[0].description.contains("late dinner"))
    }

    @Test
    fun `test no suggestions when constraints not met`() {
        val goals = emptyList<HealthGoal>()
        val medications = listOf(
            MedicationRecord(medicationId = "med_1", name = "Vitamin D", dosage = "1000IU", frequencyOffset = 2 * 3600000L, validFrom = 0L)
        )
        val mealPreferences: MealPreferences = mock()
        
        val result = engine.evaluateGoals(
            goals = goals,
            medications = medications,
            mealPreferences = mealPreferences,
            tWakeEpoch = 0L,
            nocturiaCount = 0
        )

        assertTrue(result.isEmpty())
    }
}
