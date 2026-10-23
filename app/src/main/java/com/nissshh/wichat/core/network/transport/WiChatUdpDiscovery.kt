package com.nissshh.wichat.core.network.transport

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.nissshh.wichat.core.model.DiscoveredPeer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface

/**
 * Resilient UDP Subnet Broadcast fallback.
 * Works seamlessly across home Wi-Fi routers that filter or throttle mDNS multicast (224.0.0.251).
 */
class WiChatUdpDiscovery(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onPeerDiscovered: (DiscoveredPeer) -> Unit
) {
    companion object {
        private const val TAG = "WiChatUdpDiscovery"
        const val UDP_BROADCAST_PORT = 54321
        private const val BROADCAST_INTERVAL_MS = 3000L
    }

    private var socket: DatagramSocket? = null
    private var broadcastJob: Job? = null
    private var listenJob: Job? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    fun start(myPeerId: String, myDisplayName: String, myTcpPort: Int) {
        stop()

        try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifi?.createMulticastLock("WiChatMulticastLock")?.apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire MulticastLock: ${e.message}")
        }

        try {
            val s = DatagramSocket(null).apply {
                reuseAddress = true
                broadcast = true
                bind(InetSocketAddress(UDP_BROADCAST_PORT))
            }
            socket = s

            // 1. Listen for incoming UDP broadcasts from other peers
            listenJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(2048)
                val packet = DatagramPacket(buffer, buffer.size)

                while (isActive && !s.isClosed) {
                    try {
                        s.receive(packet)
                        val data = String(packet.data, packet.offset, packet.length)
                        val senderIp = packet.address.hostAddress ?: continue

                        val json = JSONObject(data)
                        val peerId = json.optString("id", "")
                        val name = json.optString("name", "Nearby Device")
                        val tcpPort = json.optInt("port", 0)

                        if (peerId.isNotEmpty() && peerId != myPeerId && tcpPort > 0) {
                            val peer = DiscoveredPeer(
                                peerId = peerId,
                                displayName = name,
                                bio = "Discovered via Local Wi-Fi",
                                hostAddress = senderIp,
                                port = tcpPort,
                                publicKeyString = "",
                                isReachable = true
                            )
                            onPeerDiscovered(peer)
                        }
                    } catch (e: Exception) {
                        if (isActive && !s.isClosed) {
                            Log.w(TAG, "UDP receive error: ${e.message}")
                        }
                    }
                }
            }

            // 2. Broadcast our presence to 255.255.255.255 AND active subnet broadcast addresses
            broadcastJob = scope.launch(Dispatchers.IO) {
                while (isActive && !s.isClosed) {
                    try {
                        val payload = JSONObject().apply {
                            put("id", myPeerId)
                            put("name", myDisplayName)
                            put("port", myTcpPort)
                            put("ts", System.currentTimeMillis())
                        }.toString().toByteArray(Charsets.UTF_8)

                        val targets = getBroadcastAddresses()
                        for (target in targets) {
                            try {
                                val sendPacket = DatagramPacket(payload, payload.size, target, UDP_BROADCAST_PORT)
                                s.send(sendPacket)
                            } catch (ignored: Exception) {}
                        }
                    } catch (e: Exception) {
                        if (isActive) Log.w(TAG, "UDP broadcast send error: ${e.message}")
                    }
                    kotlinx.coroutines.delay(BROADCAST_INTERVAL_MS)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not initialize WiChat UDP Discovery", e)
        }
    }

    private fun getBroadcastAddresses(): List<InetAddress> {
        val list = mutableListOf<InetAddress>()
        try {
            list.add(InetAddress.getByName("255.255.255.255"))
        } catch (ignored: Exception) {}

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val netIf = interfaces.nextElement()
                if (netIf.isLoopback || !netIf.isUp) continue

                for (addr in netIf.interfaceAddresses) {
                    val broadcast = addr.broadcast
                    if (broadcast != null && !list.contains(broadcast)) {
                        list.add(broadcast)
                    }
                }
            }
        } catch (ignored: Exception) {}

        return list
    }

    fun stop() {
        broadcastJob?.cancel()
        broadcastJob = null

        listenJob?.cancel()
        listenJob = null

        try {
            socket?.close()
        } catch (ignored: Exception) {}
        socket = null

        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (ignored: Exception) {}
        multicastLock = null
    }
}
