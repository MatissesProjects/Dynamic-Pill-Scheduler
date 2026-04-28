package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class MetabolicClearanceEngineTest {

    private val engine = MetabolicClearanceEngine()
    private val maleProfile = UserProfile(weightKg = 80.0, gender = Gender.MALE)

    @Test
    fun `calculateEBAC returns zero for no logs`() {
        val ebac = engine.calculateEBAC(emptyList(), maleProfile)
        assertEquals(0.0, ebac, 0.001)
    }

    @Test
    fun `calculateEBAC detects rising BAC after drinks`() {
        val now = Instant.now()
        val logs = listOf(
            AlcoholLog(timestamp = now, beverageType = BeverageType.SPIRITS, abv = 40.0, volumeMl = 44.0), // 1 shot
            AlcoholLog(timestamp = now, beverageType = BeverageType.SPIRITS, abv = 40.0, volumeMl = 44.0)  // 2nd shot
        )
        
        val ebac = engine.calculateEBAC(logs, maleProfile, now)
        // (0.806 * 2.5 * 1.2) / (0.58 * 80) = 2.418 / 46.4 = 0.052
        // Wait, 1 shot (44ml at 40%) is (44 * 0.4) / 14 = 1.25 drinks.
        // 2 shots = 2.5 drinks.
        assertEquals(0.052, ebac, 0.005)
    }

    @Test
    fun `calculateEBAC accounts for metabolic reduction over time`() {
        val startTime = Instant.now().minusSeconds(7200) // 2 hours ago
        val logs = listOf(
            AlcoholLog(timestamp = startTime, beverageType = BeverageType.BEER, abv = 5.0, volumeMl = 500.0) // ~1.8 drinks
        )
        
        val ebac = engine.calculateEBAC(logs, maleProfile, Instant.now())
        // Gross: (0.806 * 1.78 * 1.2) / 46.4 = 1.72 / 46.4 = 0.037
        // Reduction: 0.015 * 2 = 0.030
        // Net: 0.007
        assertEquals(0.007, ebac, 0.005)
    }

    @Test
    fun `calculateEffectiveHalfLife increases for high BAC`() {
        val baseHalfLife = 360 // 6 hours
        val adjusted = engine.calculateEffectiveHalfLife(baseHalfLife, 0.08, "CYP3A4")
        assertEquals(540, adjusted) // 50% increase
    }

    @Test
    fun `estimateTimeToSobriety returns correct duration`() {
        val duration = engine.estimateTimeToSobriety(0.03)
        assertEquals(120, duration.toMinutes()) // 0.03 / 0.015 = 2 hours
    }
}
