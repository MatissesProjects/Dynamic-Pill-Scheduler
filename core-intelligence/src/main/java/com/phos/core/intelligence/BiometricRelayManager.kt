package com.phos.core.intelligence

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.phos.core.intelligence.proto.RelayRequest
import com.phos.core.intelligence.proto.RelayResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.protobuf.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Manages the secure relay of biometric data to a local LLM node.
 * Handles mDNS discovery, authentication, and data offloading.
 */
@OptIn(ExperimentalSerializationApi::class)
class BiometricRelayManager(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val TAG = "BiometricRelayManager"
    private val SERVICE_TYPE = "_phos-llm._tcp."
    
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            protobuf()
        }
    }

    private val _discoveredNode = MutableStateFlow<NsdServiceInfo?>(null)
    val discoveredNode = _discoveredNode.asStateFlow()

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "Service found: ${service.serviceName}")
            if (service.serviceType == SERVICE_TYPE) {
                @Suppress("DEPRECATION")
                nsdManager.resolveService(service, resolveListener)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e(TAG, "Service lost: ${service.serviceName}")
            if (_discoveredNode.value?.serviceName == service.serviceName) {
                _discoveredNode.value = null
            }
        }

        override fun onDiscoveryStopped(regType: String) {
            Log.i(TAG, "Discovery stopped: $regType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery stop failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed: Error code:$errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolve Succeeded. $serviceInfo")
            _discoveredNode.value = serviceInfo
        }
    }

    fun startDiscovery() {
        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start discovery", e)
        }
    }

    fun stopDiscovery() {
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop discovery", e)
        }
    }

    /**
     * Placeholder for the authentication handshake with the local node.
     */
    suspend fun authenticate(serviceInfo: NsdServiceInfo): Boolean = withContext(ioDispatcher) {
        // TODO: Implement mTLS or token-based handshake
        @Suppress("DEPRECATION")
        Log.d(TAG, "Authenticating with node at ${serviceInfo.host}:${serviceInfo.port}")
        delay(500) // Simulate network trip
        true
    }

    /**
     * Offloads biometric deltas to the discovered node.
     */
    suspend fun offloadData(request: RelayRequest): RelayResponse? = withContext(ioDispatcher) {
        val node = _discoveredNode.value ?: return@withContext null
        try {
            @Suppress("DEPRECATION")
            Log.i(TAG, "Offloading data to ${node.host}:${node.port}")
            val response: HttpResponse = client.post("http://${node.host}:${node.port}/sync") {
                contentType(ContentType.Application.ProtoBuf)
                setBody(request)
            }
            
            if (response.status == HttpStatusCode.OK) {
                response.body<RelayResponse>()
            } else {
                Log.e(TAG, "Server returned error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Data offload failed", e)
            null
        }
    }
}
