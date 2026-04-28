package com.phos.core.intelligence

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class BiometricRelayManagerTest {

    private lateinit var context: Context
    private lateinit var nsdManager: NsdManager
    private lateinit var relayManager: BiometricRelayManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        context = mock()
        nsdManager = mock()
        whenever(context.getSystemService(Context.NSD_SERVICE)).thenReturn(nsdManager)
        relayManager = BiometricRelayManager(context, testDispatcher)
    }

    @Test
    fun `test startDiscovery calls nsdManager discoverServices`() {
        relayManager.startDiscovery()
        verify(nsdManager).discoverServices(
            eq("_phos-llm._tcp."),
            eq(NsdManager.PROTOCOL_DNS_SD),
            any()
        )
    }

    @Test
    fun `test stopDiscovery calls nsdManager stopServiceDiscovery`() {
        // We need to capture the listener to stop it
        val listenerCaptor = argumentCaptor<NsdManager.DiscoveryListener>()
        relayManager.startDiscovery()
        verify(nsdManager).discoverServices(any<String>(), any<Int>(), listenerCaptor.capture())
        
        relayManager.stopDiscovery()
        verify(nsdManager).stopServiceDiscovery(listenerCaptor.firstValue)
    }

    @Test
    fun `test authenticate returns true`() = runTest(testDispatcher) {
        val serviceInfo = mock<NsdServiceInfo>()
        val result = relayManager.authenticate(serviceInfo)
        assert(result)
    }
}
