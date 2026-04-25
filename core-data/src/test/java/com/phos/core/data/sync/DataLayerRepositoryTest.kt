package com.phos.core.data.sync

import androidx.datastore.core.DataStore
import com.phos.core.data.proto.PhosState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class DataLayerRepositoryTest {

    private lateinit var dataStore: DataStore<PhosState>
    private lateinit var repository: DataLayerRepository

    @Before
    fun setup() {
        dataStore = mock()
        repository = DataLayerRepository(mock(), dataStore)
    }

    @Test
    fun `test updateTWake transforms state correctly`() = runBlocking {
        val initialState = PhosState.getDefaultInstance()
        val tWake = 123456789L
        
        // Capture the lambda passed to updateData
        val transformCaptor = argumentCaptor<suspend (PhosState) -> PhosState>()
        
        repository.updateTWake(tWake, true)
        
        verify(dataStore).updateData(transformCaptor.capture())
        
        val transformedState = transformCaptor.firstValue.invoke(initialState)
        assertEquals(tWake, transformedState.tWakeEpoch)
        assertEquals(true, transformedState.wasInterrupted)
    }

    @Test
    fun `test addMedication appends to list`() = runBlocking {
        val initialState = PhosState.getDefaultInstance()
        val transformCaptor = argumentCaptor<suspend (PhosState) -> PhosState>()
        
        repository.addMedication("med_1", "Test Med", 5000L)
        
        verify(dataStore).updateData(transformCaptor.capture())
        
        val transformedState = transformCaptor.firstValue.invoke(initialState)
        assertEquals(1, transformedState.medicationsCount)
        assertEquals("med_1", transformedState.getMedications(0).id)
        assertEquals("Test Med", transformedState.getMedications(0).name)
        assertEquals(5000L, transformedState.getMedications(0).scheduledTime)
    }

    @Test
    fun `test updateMealPreferences updates preferences`() = runBlocking {
        val initialState = PhosState.getDefaultInstance()
        val transformCaptor = argumentCaptor<suspend (PhosState) -> PhosState>()
        
        repository.updateMealPreferences(1000L, 2000L, 3000L, 4000L, 5000L, 6000L)
        
        verify(dataStore).updateData(transformCaptor.capture())
        
        val transformedState = transformCaptor.firstValue.invoke(initialState)
        val prefs = transformedState.mealPreferences
        assertEquals(1000L, prefs.breakfastStartOffset)
        assertEquals(2000L, prefs.breakfastEndOffset)
        assertEquals(3000L, prefs.lunchStartOffset)
        assertEquals(4000L, prefs.lunchEndOffset)
        assertEquals(5000L, prefs.dinnerStartOffset)
        assertEquals(6000L, prefs.dinnerEndOffset)
    }
}
