package com.phos.phone.ui.widget

import com.phos.core.data.proto.Medication
import com.phos.core.data.proto.PhosState
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetStateMappingTest {

    @Test
    fun `test getNextMedicationText with pending meds`() {
        val med = Medication.newBuilder().setName("Lisinopril").setStatus("PENDING").build()
        val state = PhosState.newBuilder().addMedications(med).build()
        
        val text = BiometricDashboardWidget.getNextMedicationText(state)
        assertEquals("Next: Lisinopril", text)
    }

    @Test
    fun `test getNextMedicationText with no pending meds`() {
        val med = Medication.newBuilder().setName("Lisinopril").setStatus("TAKEN").build()
        val state = PhosState.newBuilder().addMedications(med).build()
        
        val text = BiometricDashboardWidget.getNextMedicationText(state)
        assertEquals("All caught up!", text)
    }

    @Test
    fun `test getNextMedicationText with empty list`() {
        val state = PhosState.getDefaultInstance()
        val text = BiometricDashboardWidget.getNextMedicationText(state)
        assertEquals("All caught up!", text)
    }
}
