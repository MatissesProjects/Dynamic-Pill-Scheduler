package com.phos.core.intelligence

import com.phos.core.data.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GIProtectionEngineTest {

    private val engine = GIProtectionEngine()

    @Test
    fun `detectChelationRisk identifies conflict with calcium-rich food`() {
        val medication = MedicationRecord(medicationId = "synthroid", name = "Levothyroxine", dosage = "100mcg", frequencyOffset = 0, validFrom = 0)
        val rule = ChelationRule(medicationId = "synthroid", mineralType = "CALCIUM", requiredGapMinutes = 120, advice = "Calcium binds to Levothyroxine, reducing effectiveness.")
        
        val foodLogs = listOf(
            FoodLog(foodId = "milk", name = "Almond Milk (Fortified)", timestamp = System.currentTimeMillis() - 30 * 60000, 
                nutrients = NutrientFacts(calories = 40, calciumMg = 300.0))
        )
        
        val insight = engine.detectChelationRisk(medication, foodLogs, listOf(rule))
        
        assertNotNull(insight)
        assertEquals("CALCIUM", insight?.mineralType)
        assertTrue(insight?.conflictingFoodName!!.contains("Almond Milk"))
    }

    @Test
    fun `detectChelationRisk ignores food outside gap window`() {
        val medication = MedicationRecord(medicationId = "synthroid", name = "Levothyroxine", dosage = "100mcg", frequencyOffset = 0, validFrom = 0)
        val rule = ChelationRule(medicationId = "synthroid", mineralType = "CALCIUM", requiredGapMinutes = 120, advice = "Calcium binds to Levothyroxine.")
        
        val foodLogs = listOf(
            FoodLog(foodId = "milk", name = "Almond Milk", timestamp = System.currentTimeMillis() - 150 * 60000, 
                nutrients = NutrientFacts(calories = 40, calciumMg = 300.0))
        )
        
        val insight = engine.detectChelationRisk(medication, foodLogs, listOf(rule))
        
        assertNull(insight)
    }
}
