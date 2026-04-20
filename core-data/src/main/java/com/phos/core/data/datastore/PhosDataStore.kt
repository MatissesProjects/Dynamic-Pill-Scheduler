package com.phos.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.phos.core.data.proto.PhosState
import com.phos.core.data.serializer.PhosStateSerializer

val Context.phosDataStore: DataStore<PhosState> by dataStore(
    fileName = "phos_state.pb",
    serializer = PhosStateSerializer
)
