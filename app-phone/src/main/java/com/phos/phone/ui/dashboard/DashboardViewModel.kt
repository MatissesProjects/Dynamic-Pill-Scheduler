package com.phos.phone.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phos.core.data.proto.PhosState
import com.phos.core.data.sync.DataLayerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: DataLayerRepository
) : ViewModel() {

    val uiState: StateFlow<PhosState> = repository.phosStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PhosState.getDefaultInstance()
        )
}
