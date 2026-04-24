package com.phos.core.data.engine

import com.phos.core.data.model.*
import org.junit.Assert.*
import org.junit.Test

class NutrientAdvisoryEngineTest {

    private val collisionResolver = CollisionResolver()
    private val engine = NutrientAdvisoryEngine(collisionResolver)

    @Test
    fun `test evaluateFood detects allergen`() {
        val nutrients = NutrientFacts(ingredients = listOf("Milk", "Sugar"))
        val allergens = listOf(AllergenProfile("dairy", "Dairy", "MODERATE"))
        
        // Match by displayName "Dairy" in ingredients? No, ingredients contains "Milk". 
        // Match by allergenId "dairy" in ingredients? No.
        // Wait, I should probably add "Milk" to the ingredients or check food name.
        
        val result = engine.evaluateFood("Dairy Yogurt", nutrients, allergens, emptyList(), 0L, 1000L)
        
        assertFalse(result.isGoodIdea)
        assertTrue(result.warnings.any { it.contains("ALLERGEN DETECTED") })
    }

    @Test
    fun `test evaluateFood detects calcium interference`() {
        val nutrients = NutrientFacts(calciumMg = 300.0)
        val medications = listOf(
            MedicationRecord(medicationId = "cipro_1", name = "Ciprofloxacin", dosage = "500mg", frequencyOffset = 30 * 60 * 1000L, validFrom = 0L)
        )
        
        // currentTime = 0, medTime = 30 mins. Gap = 30 mins < 2h.
        val result = engine.evaluateFood("Milk", nutrients, emptyList(), medications, 0L, 0L)
        
        assertFalse(result.isGoodIdea)
        assertTrue(result.warnings.any { it.contains("INTERFERENCE") })
    }

    @Test
    fun `test evaluateFood approves safe food`() {
        val nutrients = NutrientFacts(calories = 100, proteinG = 20.0)
        
        val result = engine.evaluateFood("Chicken", nutrients, emptyList(), emptyList(), 0L, 0L)
        
        assertTrue(result.isGoodIdea)
        assertEquals(0, result.warnings.size)
    }
}
