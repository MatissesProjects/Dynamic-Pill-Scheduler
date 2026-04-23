package com.phos.core.data.engine

import com.phos.core.data.dao.BiometricDao
import com.phos.core.data.dao.DoseLogDao
import com.phos.core.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit

class PRNAdvisorTest {

    private lateinit var doseLogDao: DoseLogDao
    private lateinit var biometricDao: BiometricDao
    private lateinit var collisionResolver: CollisionResolver
    private lateinit var advisor: PRNAdvisor

    @Before
    fun setup() {
        doseLogDao = mock()
        biometricDao = mock()
        collisionResolver = mock()
        advisor = PRNAdvisor(doseLogDao, biometricDao, collisionResolver)
    }

    @Test
    fun `test evaluateRequest approves safe dose`() = runBlocking {
        val prnMed = PRNMedication(
            medicationId = "ibu_prn",
            name = "Ibuprofen",
            dosage = "400mg",
            maxDosesPer24h = 4,
            minGapMinutes = 240,
            validFrom = 0L
        )
        
        whenever(doseLogDao.getRecentTakenDoses(eq("ibu_prn"), any())).thenReturn(emptyList())

        val result = advisor.evaluateRequest(prnMed, emptyList(), emptyList())

        assertTrue(result.isApproved)
        assertTrue(result.reason.contains("Safe to take"))
    }

    @Test
    fun `test evaluateRequest rejects when too soon`() = runBlocking {
        val prnMed = PRNMedication(
            medicationId = "ibu_prn",
            name = "Ibuprofen",
            dosage = "400mg",
            maxDosesPer24h = 4,
            minGapMinutes = 240,
            validFrom = 0L
        )
        
        val now = Instant.now()
        val recentDose = DoseLog(
            medicationId = "ibu_prn",
            scheduledTime = now.minus(1, ChronoUnit.HOURS).toEpochMilli(),
            actualTime = now.minus(1, ChronoUnit.HOURS).toEpochMilli(),
            status = "TAKEN"
        )
        
        whenever(doseLogDao.getRecentTakenDoses(eq("ibu_prn"), any())).thenReturn(listOf(recentDose))

        val result = advisor.evaluateRequest(prnMed, emptyList(), emptyList())

        assertFalse(result.isApproved)
        assertTrue(result.reason.contains("Too soon"))
        assertEquals(180, result.suggestedWaitMinutes) // 240 - 60
    }

    @Test
    fun `test evaluateRequest rejects when daily limit reached`() = runBlocking {
        val prnMed = PRNMedication(
            medicationId = "ibu_prn",
            name = "Ibuprofen",
            dosage = "400mg",
            maxDosesPer24h = 2,
            minGapMinutes = 60,
            validFrom = 0L
        )
        
        val now = Instant.now()
        val doses = listOf(
            DoseLog(medicationId = "ibu_prn", scheduledTime = now.minus(5, ChronoUnit.HOURS).toEpochMilli(), actualTime = now.minus(5, ChronoUnit.HOURS).toEpochMilli(), status = "TAKEN"),
            DoseLog(medicationId = "ibu_prn", scheduledTime = now.minus(2, ChronoUnit.HOURS).toEpochMilli(), actualTime = now.minus(2, ChronoUnit.HOURS).toEpochMilli(), status = "TAKEN")
        )
        
        whenever(doseLogDao.getRecentTakenDoses(eq("ibu_prn"), any())).thenReturn(doses)

        val result = advisor.evaluateRequest(prnMed, emptyList(), emptyList())

        assertFalse(result.isApproved)
        assertTrue(result.reason.contains("Maximum daily limit"))
    }

    @Test
    fun `test evaluateRequest rejects for high HR with stimulant`() = runBlocking {
        val prnMed = PRNMedication(
            medicationId = "alb_prn",
            name = "Albuterol Inhaler",
            dosage = "2 puffs",
            maxDosesPer24h = 8,
            minGapMinutes = 15,
            validFrom = 0L
        )
        
        whenever(doseLogDao.getRecentTakenDoses(any(), any())).thenReturn(emptyList())
        
        val highHr = listOf(BiometricLog(type = BiometricType.HEART_RATE, value = 110.0, timestamp = Instant.now()))
        whenever(biometricDao.getLogsSince(eq(BiometricType.HEART_RATE), any())).thenReturn(highHr)

        val result = advisor.evaluateRequest(prnMed, emptyList(), emptyList())

        assertFalse(result.isApproved)
        assertTrue(result.reason.contains("heart rate is currently elevated"))
    }
}
