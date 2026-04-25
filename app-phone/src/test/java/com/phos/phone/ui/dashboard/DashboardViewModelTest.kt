package com.phos.phone.ui.dashboard

import android.app.Application
import com.phos.core.data.dao.*
import com.phos.core.data.db.PhosDatabase
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.sync.DataLayerRepository
import com.phos.core.data.proto.PhosState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var application: Application
    private lateinit var db: PhosDatabase
    private lateinit var dataLayerRepository: DataLayerRepository
    private lateinit var voiceManager: VoiceManager
    
    private lateinit var medicationDao: MedicationDao
    private lateinit var doseLogDao: DoseLogDao
    private lateinit var interactionDao: InteractionDao
    private lateinit var dismissedInsightDao: DismissedInsightDao
    private lateinit var prnDao: PRNDao
    private lateinit var temporalAnchorDao: TemporalAnchorDao
    private lateinit var appetiteDao: AppetiteDao
    private lateinit var allergenDao: AllergenDao
    private lateinit var goalDao: GoalDao
    private lateinit var nocturiaDao: NocturiaDao
    private lateinit var sleepSubjectiveDao: SleepSubjectiveDao
    private lateinit var nutrientDao: NutrientDao

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = mock()
        db = mock()
        dataLayerRepository = mock()
        voiceManager = mock()
        
        medicationDao = mock()
        interactionDao = mock()
        dismissedInsightDao = mock()
        prnDao = mock()
        temporalAnchorDao = mock()
        appetiteDao = mock()
        allergenDao = mock()
        goalDao = mock()
        nocturiaDao = mock()
        sleepSubjectiveDao = mock()
        nutrientDao = mock()
        doseLogDao = mock()

        whenever(db.medicationDao()).thenReturn(medicationDao)
        whenever(db.interactionDao()).thenReturn(interactionDao)
        whenever(db.dismissedInsightDao()).thenReturn(dismissedInsightDao)
        whenever(db.prnDao()).thenReturn(prnDao)
        whenever(db.temporalAnchorDao()).thenReturn(temporalAnchorDao)
        whenever(db.appetiteDao()).thenReturn(appetiteDao)
        whenever(db.allergenDao()).thenReturn(allergenDao)
        whenever(db.goalDao()).thenReturn(goalDao)
        whenever(db.nocturiaDao()).thenReturn(nocturiaDao)
        whenever(db.sleepSubjectiveDao()).thenReturn(sleepSubjectiveDao)
        whenever(db.nutrientDao()).thenReturn(nutrientDao)
        whenever(db.doseLogDao()).thenReturn(doseLogDao)
        whenever(db.biometricDao()).thenReturn(mock())
        whenever(db.intelligenceDao()).thenReturn(mock())

        // Mock basic flows
        whenever(medicationDao.getAllActiveMedicationsFlow()).thenReturn(flowOf(emptyList<MedicationRecord>()))
        whenever(prnDao.getAllActivePRNMedicationsFlow()).thenReturn(flowOf(emptyList()))
        whenever(dataLayerRepository.phosStateFlow).thenReturn(flowOf(PhosState.getDefaultInstance()))
        whenever(interactionDao.getAllRules()).thenReturn(flowOf(emptyList()))
        whenever(interactionDao.getAllAbsorptionRules()).thenReturn(flowOf(emptyList()))
        whenever(interactionDao.getAllSideEffectRules()).thenReturn(flowOf(emptyList()))
        whenever(dismissedInsightDao.getAllDismissedIds()).thenReturn(flowOf(emptyList<String>()))
        whenever(temporalAnchorDao.getLatestAnchorFlow()).thenReturn(flowOf(null))
        whenever(appetiteDao.getAppetiteLogsSince(any())).thenReturn(flowOf(emptyList()))
        whenever(allergenDao.getAllergensFlow()).thenReturn(flowOf(emptyList()))
        whenever(goalDao.getActiveGoalsFlow()).thenReturn(flowOf(emptyList()))
        whenever(nocturiaDao.getNocturiaLogsSince(any())).thenReturn(flowOf(emptyList()))
        whenever(sleepSubjectiveDao.getAllLogsFlow()).thenReturn(flowOf(emptyList()))
        whenever(nutrientDao.getAllDepletions()).thenReturn(flowOf(emptyList<com.phos.core.data.model.MedicationInducedDepletion>()))
        whenever(nutrientDao.getAllReferences()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test medications state flow`() = runTest {
        val meds = listOf(
            MedicationRecord(medicationId = "med1", name = "Med 1", dosage = "10mg", frequencyOffset = 0L, validFrom = 0L)
        )
        whenever(medicationDao.getAllActiveMedicationsFlow()).thenReturn(flowOf(meds))
        
        val viewModel = DashboardViewModel(application, db, dataLayerRepository, voiceManager)
        
        // Start collecting to trigger WhileSubscribed
        backgroundScope.launch { viewModel.medications.collect { } }
        
        advanceUntilIdle()
        
        assertEquals(meds, viewModel.medications.value)
    }

    @Test
    fun `test updateWakeTime calls repository`() = runTest {
        val viewModel = DashboardViewModel(application, db, dataLayerRepository, voiceManager)
        val tWake = 1000L
        
        viewModel.updateWakeTime(tWake)
        advanceUntilIdle()
        
        verify(dataLayerRepository).updateTWake(tWake)
    }

    @Test
    fun `test deleteMedication calls dao`() = runTest {
        val viewModel = DashboardViewModel(application, db, dataLayerRepository, voiceManager)
        val id = 123L
        
        viewModel.deleteMedication(id)
        advanceUntilIdle()
        
        verify(medicationDao).deletePermanently(id)
    }
}
