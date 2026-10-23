package com.nissshh.wichat.core.network.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.nissshh.wichat.core.model.DiscoveredPeer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

class WiChatNsdManager(private val context: Context) {
    companion object {
        const val TAG = "WiChatNSD"
        // Standard Android NsdManager service type format: without trailing dot
        const val SERVICE_TYPE = "_wichat._tcp"
        const val SERVICE_NAME_PREFIX = "WiChat_"
    }

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var multicastLock: WifiManager.MulticastLock? = null

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private val _discoveredPeers = MutableStateFlow<Map<String, DiscoveredPeer>>(emptyMap())
    val discoveredPeers: StateFlow<Map<String, DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    var activePort: Int = 0
        private set

    // Queue to prevent concurrent resolve requests which cause Android NSD failure code 3 (FAILURE_ALREADY_ACTIVE)
    private val resolveQueue = ConcurrentLinkedQueue<NsdServiceInfo>()
    @Volatile
    private var isResolving = false

    fun startAdvertising(localPeerId: String, displayName: String, port: Int) {
        activePort = port
        acquireMulticastLock()

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "$SERVICE_NAME_PREFIX$localPeerId"
            serviceType = SERVICE_TYPE
            setPort(port)
            setAttribute("dn", displayName)
            setAttribute("id", localPeerId)
            setAttribute("ver", "1")
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(service: NsdServiceInfo?) {
                Log.d(TAG, "Service registered successfully: ${service?.serviceName} on port $port")
            }

            override fun onRegistrationFailed(service: NsdServiceInfo?, errorCode: Int) {
                Log.e(TAG, "Service registration failed with code: $errorCode")
            }

            override fun onServiceUnregistered(service: NsdServiceInfo?) {
                Log.d(TAG, "Service unregistered")
            }

            override fun onUnregistrationFailed(service: NsdServiceInfo?, errorCode: Int) {
                Log.e(TAG, "Service unregistration failed: $errorCode")
            }
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering NSD service", e)
        }
    }

    private var currentPeerId: String = ""

    fun startDiscovery(myPeerId: String) {
        currentPeerId = myPeerId
        acquireMulticastLock()
        if (_isScanning.value) {
            stopDiscovery()
        }

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String?) {
                Log.d(TAG, "NSD discovery started for $regType")
                _isScanning.value = true
            }

            override fun onServiceFound(service: NsdServiceInfo?) {
                Log.d(TAG, "onServiceFound: name=${service?.serviceName}, type=${service?.serviceType}")
                if (service != null && service.serviceType.contains("wichat", ignoreCase = true)) {
                    val name = service.serviceName ?: ""
                    // Skip resolving our own advertised service
                    if (currentPeerId.isNotEmpty() && name.contains(currentPeerId)) {
                        Log.d(TAG, "Skipping resolve of own service: $name")
                        return
                    }
                    queueResolve(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo?) {
                val serviceName = service?.serviceName ?: return
                Log.d(TAG, "onServiceLost: $serviceName")
                val matchedPeer = _discoveredPeers.value.values.find { serviceName.contains(it.peerId) }
                if (matchedPeer != null) {
                    val updated = _discoveredPeers.value.toMutableMap()
                    updated.remove(matchedPeer.peerId)
                    _discoveredPeers.value = updated
                }
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.d(TAG, "NSD discovery stopped")
                _isScanning.value = false
            }

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Start discovery failed: $errorCode")
                _isScanning.value = false
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed: $errorCode")
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering NSD services", e)
            _isScanning.value = false
        }
    }

    private fun queueResolve(serviceInfo: NsdServiceInfo) {
        resolveQueue.add(serviceInfo)
        processNextResolve()
    }

    @Synchronized
    private fun processNextResolve() {
        if (isResolving) return
        val nextService = resolveQueue.poll() ?: return
        isResolving = true

        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(service: NsdServiceInfo?, errorCode: Int) {
                Log.w(TAG, "Resolve failed for service ${service?.serviceName}: $errorCode")
                isResolving = false
                processNextResolve()
            }

            override fun onServiceResolved(resolved: NsdServiceInfo?) {
                try {
                    if (resolved != null) {
                        val host = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            resolved.hostAddresses.firstOrNull()?.hostAddress ?: resolved.host?.hostAddress
                        } else {
                            resolved.host?.hostAddress
                        }

                        if (host != null) {
                            val port = resolved.port
                            val attributes = resolved.attributes
                            val peerId = attributes["id"]?.let { String(it) } 
                                ?: resolved.serviceName.removePrefix(SERVICE_NAME_PREFIX)
                            val displayName = attributes["dn"]?.let { String(it) } ?: "Nearby WiChat User"

                            // Never add self as discovered peer
                            if (currentPeerId.isNotEmpty() && (peerId == currentPeerId || resolved.serviceName.contains(currentPeerId))) {
                                Log.d(TAG, "Ignored own service resolution for peerId: $peerId")
                                return
                            }

                            Log.d(TAG, "Service successfully resolved: $displayName ($peerId) at $host:$port")

                            val peer = DiscoveredPeer(
                                peerId = peerId,
                                displayName = displayName,
                                bio = "Available on local Wi-Fi",
                                hostAddress = host,
                                port = port,
                                publicKeyString = "",
                                lastSeenTimestamp = System.currentTimeMillis(),
                                isReachable = true
                            )

                            val updated = _discoveredPeers.value.toMutableMap()
                            updated[peerId] = peer
                            _discoveredPeers.value = updated
                        }
                    }
                } finally {
                    isResolving = false
                    processNextResolve()
                }
            }
        }

        try {
            nsdManager.resolveService(nextService, resolveListener)
        } catch (e: Exception) {
            Log.w(TAG, "Exception initiating resolve for ${nextService.serviceName}", e)
            isResolving = false
            processNextResolve()
        }
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping discovery", e)
            }
        }
        discoveryListener = null
        _isScanning.value = false
    }

    fun stopAdvertising() {
        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering service", e)
            }
        }
        registrationListener = null
        releaseMulticastLock()
    }

    private fun acquireMulticastLock() {
        if (multicastLock == null) {
            multicastLock = wifiManager.createMulticastLock("WiChatMulticastLock").apply {
                setReferenceCounted(true)
                acquire()
            }
            Log.d(TAG, "Wi-Fi MulticastLock acquired")
        }
    }

    private fun releaseMulticastLock() {
        multicastLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "Wi-Fi MulticastLock released")
            }
        }
        multicastLock = null
    }

    fun clearDiscoveredPeers() {
        _discoveredPeers.value = emptyMap()
    }
}
