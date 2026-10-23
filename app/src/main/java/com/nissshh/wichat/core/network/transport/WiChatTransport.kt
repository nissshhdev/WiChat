package com.nissshh.wichat.core.network.transport

import android.util.Log
import com.nissshh.wichat.core.network.protocol.ProtocolMessageType
import com.nissshh.wichat.core.network.protocol.WiChatPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class WiChatServer(
    private val scope: CoroutineScope,
    private val onPacketReceived: (WiChatPacket, String) -> Unit
) {
    companion object {
        const val TAG = "WiChatServer"
    }

    private var serverSocket: ServerSocket? = null
    private var listenJob: Job? = null
    var boundPort: Int = 0
        private set

    fun start(): Int {
        val ss = ServerSocket(0) // bind to any free port
        serverSocket = ss
        boundPort = ss.localPort

        listenJob = scope.launch(Dispatchers.IO) {
            Log.d(TAG, "WiChat Server listening on port $boundPort")
            while (isActive && !ss.isClosed) {
                try {
                    val clientSocket = ss.accept()
                    launch(Dispatchers.IO) {
                        handleClient(clientSocket)
                    }
                } catch (e: Exception) {
                    if (isActive) Log.w(TAG, "Accept error", e)
                }
            }
        }
        return boundPort
    }

    private fun handleClient(socket: Socket) {
        val remoteIp = socket.inetAddress.hostAddress ?: "unknown"
        try {
            socket.soTimeout = 15000 // 15s read timeout
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val line = reader.readLine()
            if (!line.isNullOrBlank()) {
                val packet = WiChatPacket.deserialize(line)
                if (packet != null) {
                    onPacketReceived(packet, remoteIp)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error handling client packet", e)
        } finally {
            try { socket.close() } catch (ignored: Exception) {}
        }
    }

    fun stop() {
        listenJob?.cancel()
        try {
            serverSocket?.close()
        } catch (ignored: Exception) {}
        serverSocket = null
    }
}

class WiChatClient {
    companion object {
        const val TAG = "WiChatClient"
        const val CONNECT_TIMEOUT_MS = 5000
    }

    suspend fun sendPacket(host: String, port: Int, packet: WiChatPacket): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(java.net.InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
                socket.soTimeout = 8000
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(packet.serialize())
                socket.close()
                true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send packet to $host:$port", e)
                false
            }
        }
    }
}
