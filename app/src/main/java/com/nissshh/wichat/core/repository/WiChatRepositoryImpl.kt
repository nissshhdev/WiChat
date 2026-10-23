package com.nissshh.wichat.core.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.nissshh.wichat.core.database.ConnectionRequestEntity
import com.nissshh.wichat.core.database.ContactEntity
import com.nissshh.wichat.core.database.ConversationEntity
import com.nissshh.wichat.core.database.MessageEntity
import com.nissshh.wichat.core.database.ProfileEntity
import com.nissshh.wichat.core.database.StoryEntity
import com.nissshh.wichat.core.database.WiChatDatabase
import com.nissshh.wichat.core.model.ChatMessage
import com.nissshh.wichat.core.model.ConnectionRequest
import com.nissshh.wichat.core.model.ConversationItem
import com.nissshh.wichat.core.model.ConversationType
import com.nissshh.wichat.core.model.DiscoveredPeer
import com.nissshh.wichat.core.model.MessageContentType
import com.nissshh.wichat.core.model.MessageDeliveryStatus
import com.nissshh.wichat.core.model.NetworkDiagnosticInfo
import com.nissshh.wichat.core.model.RequestStatus
import com.nissshh.wichat.core.model.StoryItem
import com.nissshh.wichat.core.model.UserProfile
import com.nissshh.wichat.core.network.nsd.WiChatNsdManager
import com.nissshh.wichat.core.network.protocol.ProtocolMessageType
import com.nissshh.wichat.core.network.protocol.WiChatPacket
import com.nissshh.wichat.core.network.transport.WiChatClient
import com.nissshh.wichat.core.network.transport.WiChatServer
import com.nissshh.wichat.core.network.transport.WiChatUdpDiscovery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class WiChatRepositoryImpl(
    private val context: Context,
    private val database: WiChatDatabase
) : WiChatRepository {
    companion object {
        const val TAG = "WiChatRepo"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dao = database.wiChatDao()

    private val nsdManager = WiChatNsdManager(context)
    private val client = WiChatClient()
    private var server: WiChatServer? = null

    private val nsdPeersMap = MutableStateFlow<Map<String, DiscoveredPeer>>(emptyMap())
    private val udpPeersMap = MutableStateFlow<Map<String, DiscoveredPeer>>(emptyMap())

    private val _discoveredPeers = MutableStateFlow<List<DiscoveredPeer>>(emptyList())
    override val discoveredPeers: StateFlow<List<DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    private val udpDiscovery = WiChatUdpDiscovery(context, scope) { peer ->
        val myId = _localProfile.value?.peerId
        if (myId != null && peer.peerId == myId) return@WiChatUdpDiscovery
        val updated = udpPeersMap.value.toMutableMap()
        updated[peer.peerId] = peer
        udpPeersMap.value = updated
        recomputeDiscoveredPeers()
    }

    private val _localProfile = MutableStateFlow<UserProfile?>(null)
    override val localProfile: StateFlow<UserProfile?> = _localProfile.asStateFlow()

    override val isScanning: StateFlow<Boolean> = nsdManager.isScanning

    private val _networkDiagnostics = MutableStateFlow(
        NetworkDiagnosticInfo(
            isWifiConnected = false,
            isScanningActive = false,
            discoveredPeerCount = 0,
            activePort = 0,
            isMulticastLockAcquired = false
        )
    )
    override val networkDiagnostics: StateFlow<NetworkDiagnosticInfo> = _networkDiagnostics.asStateFlow()

    init {
        scope.launch {
            loadInitialProfile()
            observeNsdPeers()
        }
        startNetworkServer()
    }

    private suspend fun loadInitialProfile() {
        val entity = dao.getLocalProfile()
        if (entity != null) {
            val prof = UserProfile(
                peerId = entity.peerId,
                displayName = entity.displayName,
                bio = entity.bio,
                avatarUri = entity.avatarUri,
                publicKeyString = entity.publicKeyString,
                createdAt = entity.createdAt
            )
            _localProfile.value = prof
            restartAdvertising(prof)
        }
    }

    private fun startNetworkServer() {
        server = WiChatServer(scope) { packet, remoteIp ->
            handleIncomingPacket(packet, remoteIp)
        }
        val port = server?.start() ?: 0
        updateDiagnostics(port = port)
    }

    private fun observeNsdPeers() {
        scope.launch {
            nsdManager.discoveredPeers.collect { map ->
                nsdPeersMap.value = map
                recomputeDiscoveredPeers()
            }
        }
    }

    private fun recomputeDiscoveredPeers() {
        val myId = _localProfile.value?.peerId
        val combined = HashMap<String, DiscoveredPeer>()
        // Put NSD peers first, then overlay UDP peers
        combined.putAll(nsdPeersMap.value)
        combined.putAll(udpPeersMap.value)
        if (myId != null) {
            combined.remove(myId)
        }
        val list = combined.values.filter { it.peerId != myId }
        _discoveredPeers.value = list
        updateDiagnostics(peerCount = list.size)
    }

    private fun restartAdvertising(profile: UserProfile) {
        val port = server?.boundPort ?: return
        nsdManager.stopAdvertising()
        nsdManager.startAdvertising(profile.peerId, profile.displayName, port)
        nsdManager.startDiscovery(profile.peerId)
        udpDiscovery.start(profile.peerId, profile.displayName, port)
        updateDiagnostics(isWifi = checkWifiState())
    }

    private fun checkWifiState(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun updateDiagnostics(
        isWifi: Boolean? = null,
        peerCount: Int? = null,
        port: Int? = null
    ) {
        val current = _networkDiagnostics.value
        _networkDiagnostics.value = current.copy(
            isWifiConnected = isWifi ?: checkWifiState(),
            isScanningActive = nsdManager.isScanning.value,
            discoveredPeerCount = peerCount ?: current.discoveredPeerCount,
            activePort = port ?: current.activePort
        )
    }

    override fun getAllConversations(): Flow<List<ConversationItem>> {
        return dao.getAllConversations().map { entities ->
            entities.map {
                ConversationItem(
                    conversationId = it.conversationId,
                    type = if (it.type == "GROUP") ConversationType.GROUP else ConversationType.DIRECT,
                    title = it.title,
                    subtitle = it.subtitle,
                    lastMessage = it.lastMessage,
                    lastTimestamp = it.lastTimestamp,
                    unreadCount = it.unreadCount,
                    peerId = it.peerId,
                    isOnline = _discoveredPeers.value.any { p -> p.peerId == it.peerId }
                )
            }
        }
    }


    override fun getMessagesForConversation(convId: String): Flow<List<ChatMessage>> {
        return dao.getMessagesForConversation(convId).map { entities ->
            entities.map {
                ChatMessage(
                    messageId = it.messageId,
                    conversationId = it.conversationId,
                    senderPeerId = it.senderPeerId,
                    isOutgoing = it.isOutgoing,
                    contentType = MessageContentType.valueOf(it.contentType),
                    textContent = it.textContent,
                    fileName = it.fileName,
                    fileSize = it.fileSize,
                    fileMimeType = it.fileMimeType,
                    localUri = it.localUri,
                    timestamp = it.timestamp,
                    deliveryStatus = MessageDeliveryStatus.valueOf(it.deliveryStatus)
                )
            }
        }
    }

    override fun getAllRequests(): Flow<List<ConnectionRequest>> {
        return dao.getAllRequests().map { entities ->
            entities.map {
                ConnectionRequest(
                    requestId = it.requestId,
                    senderPeerId = it.senderPeerId,
                    senderDisplayName = it.senderDisplayName,
                    senderBio = it.senderBio,
                    senderPublicKey = it.senderPublicKey,
                    introText = it.introText,
                    timestamp = it.timestamp,
                    status = RequestStatus.valueOf(it.status)
                )
            }
        }
    }

    override fun getActiveStories(): Flow<List<StoryItem>> {
        return dao.getActiveStories().map { entities ->
            entities.map {
                StoryItem(
                    storyId = it.storyId,
                    ownerPeerId = it.ownerPeerId,
                    ownerDisplayName = it.ownerDisplayName,
                    textCaption = it.textCaption,
                    backgroundHex = it.backgroundHex,
                    createdAt = it.createdAt,
                    expiresAt = it.expiresAt,
                    isSeen = it.isSeen
                )
            }
        }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        val entity = ProfileEntity(
            peerId = profile.peerId,
            displayName = profile.displayName,
            bio = profile.bio,
            avatarUri = profile.avatarUri,
            publicKeyString = profile.publicKeyString,
            createdAt = profile.createdAt
        )
        dao.saveProfile(entity)
        _localProfile.value = profile
        restartAdvertising(profile)
    }

    override suspend fun sendConnectionRequest(peer: DiscoveredPeer, introText: String): Boolean {
        val myProfile = _localProfile.value ?: return false
        val reqId = UUID.randomUUID().toString()

        val payload = JSONObject().apply {
            put("reqId", reqId)
            put("name", myProfile.displayName)
            put("bio", myProfile.bio)
            put("key", myProfile.publicKeyString)
            put("intro", introText)
        }

        val packet = WiChatPacket(
            type = ProtocolMessageType.CONNECTION_REQUEST,
            packetId = reqId,
            senderPeerId = myProfile.peerId,
            payloadJson = payload.toString()
        )

        return client.sendPacket(peer.hostAddress, peer.port, packet)
    }

    private fun getDirectConversationId(peerA: String, peerB: String): String {
        val sorted = listOf(peerA, peerB).sorted()
        return "conv_direct_${sorted[0]}_${sorted[1]}"
    }

    override suspend fun startDirectConversation(peer: DiscoveredPeer): String {
        val myProfile = _localProfile.value ?: return ""
        val convId = getDirectConversationId(myProfile.peerId, peer.peerId)
        val existing = dao.getConversationById(convId)
        if (existing == null) {
            dao.insertConversation(
                ConversationEntity(
                    conversationId = convId,
                    type = "DIRECT",
                    title = peer.displayName,
                    subtitle = "Available on Wi-Fi",
                    lastMessage = "Started conversation with @${peer.displayName}",
                    lastTimestamp = System.currentTimeMillis(),
                    peerId = peer.peerId
                )
            )
        }
        return convId
    }

    override suspend fun createGroupConversation(groupTitle: String, memberPeerIds: List<String>): String {
        val myProfile = _localProfile.value ?: return ""
        val groupId = "group_" + UUID.randomUUID().toString().take(8)
        dao.insertConversation(
            ConversationEntity(
                conversationId = groupId,
                type = "GROUP",
                title = groupTitle,
                subtitle = "${memberPeerIds.size + 1} members on Wi-Fi",
                lastMessage = "Created group $groupTitle",
                lastTimestamp = System.currentTimeMillis(),
                peerId = null
            )
        )

        // Notify member peers over Wi-Fi
        val payload = JSONObject().apply {
            put("convId", groupId)
            put("title", groupTitle)
            put("members", JSONArray(memberPeerIds + myProfile.peerId))
            put("creatorName", myProfile.displayName)
            put("text", "Created group $groupTitle")
        }
        val packet = WiChatPacket(
            type = ProtocolMessageType.CHAT_MESSAGE,
            packetId = UUID.randomUUID().toString(),
            senderPeerId = myProfile.peerId,
            payloadJson = payload.toString()
        )

        memberPeerIds.forEach { mId ->
            val peer = _discoveredPeers.value.find { it.peerId == mId }
            if (peer != null) {
                scope.launch {
                    client.sendPacket(peer.hostAddress, peer.port, packet)
                }
            }
        }
        return groupId
    }

    override suspend fun clearChat(conversationId: String) {
        dao.clearMessagesForConversation(conversationId)
        val conv = dao.getConversationById(conversationId)
        if (conv != null) {
            dao.insertConversation(
                conv.copy(
                    lastMessage = "Chat cleared",
                    lastTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        dao.deleteMessageById(messageId)
    }

    override suspend fun deleteConversation(conversationId: String) {
        dao.clearMessagesForConversation(conversationId)
        dao.deleteConversation(conversationId)
    }

    override suspend fun markConversationAsRead(conversationId: String) {
        dao.markConversationAsRead(conversationId)
    }


    override suspend fun updateProfile(displayName: String, bio: String, avatarUri: String?) {
        val current = _localProfile.value ?: return
        val updated = current.copy(
            displayName = displayName,
            bio = bio,
            avatarUri = avatarUri ?: current.avatarUri
        )
        saveProfile(updated)
    }


    override suspend fun respondToRequest(requestId: String, accept: Boolean) {
        val newStatus = if (accept) RequestStatus.ACCEPTED else RequestStatus.DECLINED
        dao.updateRequestStatus(requestId, newStatus.name)

        if (accept) {
            val req = dao.getRequestById(requestId)
            val peerId = req?.senderPeerId
            val title = req?.senderDisplayName ?: "Accepted Peer"
            val myProfile = _localProfile.value

            val convId = if (myProfile != null && peerId != null) {
                getDirectConversationId(myProfile.peerId, peerId)
            } else {
                "conv_$requestId"
            }

            val conv = ConversationEntity(
                conversationId = convId,
                type = "DIRECT",
                title = title,
                subtitle = "Available on Wi-Fi",
                lastMessage = "Connected over local network",
                lastTimestamp = System.currentTimeMillis(),
                peerId = peerId
            )
            dao.insertConversation(conv)

            // Notify requester that we accepted
            if (peerId != null) {
                val peer = _discoveredPeers.value.find { it.peerId == peerId }
                if (peer != null && myProfile != null) {
                    val payload = JSONObject().apply {
                        put("reqId", requestId)
                        put("accepted", true)
                        put("convId", convId)
                        put("name", myProfile.displayName)
                    }
                    val packet = WiChatPacket(
                        type = ProtocolMessageType.REQUEST_RESPONSE,
                        packetId = UUID.randomUUID().toString(),
                        senderPeerId = myProfile.peerId,
                        payloadJson = payload.toString()
                    )
                    client.sendPacket(peer.hostAddress, peer.port, packet)
                }
            }
        }
    }

    override suspend fun blockPeer(peerId: String) {
        dao.blockContact(peerId)
    }

    override suspend fun sendMessage(conversationId: String, text: String, peerId: String?): Boolean {
        val myProfile = _localProfile.value ?: return false
        val msgId = UUID.randomUUID().toString()

        val conv = dao.getConversationById(conversationId)
        val isGroup = conv?.type == "GROUP"

        // For direct chats: resolve target peer
        var targetPeerId = peerId ?: conv?.peerId
        if (targetPeerId == null && !isGroup) {
            // Check if conversation ID encodes the peer ID
            if (conversationId.startsWith("conv_direct_")) {
                val parts = conversationId.removePrefix("conv_direct_").split("_")
                targetPeerId = parts.find { it != myProfile.peerId }
            }
        }

        val msgEntity = MessageEntity(
            messageId = msgId,
            conversationId = conversationId,
            senderPeerId = myProfile.peerId,
            isOutgoing = true,
            contentType = MessageContentType.TEXT.name,
            textContent = text,
            fileName = null,
            fileSize = 0,
            fileMimeType = null,
            localUri = null,
            timestamp = System.currentTimeMillis(),
            deliveryStatus = MessageDeliveryStatus.SENDING.name
        )
        dao.insertMessage(msgEntity)

        // Also update last message preview in conversations table
        if (conv != null) {
            dao.insertConversation(
                conv.copy(
                    lastMessage = text,
                    lastTimestamp = System.currentTimeMillis()
                )
            )
        }

        val payload = JSONObject().apply {
            put("convId", conversationId)
            put("text", text)
            put("senderName", myProfile.displayName)
            put("isGroup", isGroup)
            if (conv != null) {
                put("convTitle", conv.title)
            }
        }

        val packet = WiChatPacket(
            type = ProtocolMessageType.CHAT_MESSAGE,
            packetId = msgId,
            senderPeerId = myProfile.peerId,
            payloadJson = payload.toString()
        )

        if (isGroup) {
            // Broadcast/multicast to all discovered peers
            var deliveredAtLeastOnce = false
            _discoveredPeers.value.forEach { peer ->
                val sent = client.sendPacket(peer.hostAddress, peer.port, packet)
                if (sent) deliveredAtLeastOnce = true
            }
            val status = if (deliveredAtLeastOnce) MessageDeliveryStatus.SENT_TO_DEVICE else MessageDeliveryStatus.QUEUED
            dao.updateMessageStatus(msgId, status.name)
            return deliveredAtLeastOnce
        } else {
            val peer = if (targetPeerId != null) {
                _discoveredPeers.value.find { it.peerId == targetPeerId }
            } else {
                _discoveredPeers.value.firstOrNull()
            }

            if (peer != null) {
                val success = client.sendPacket(peer.hostAddress, peer.port, packet)
                val finalStatus = if (success) MessageDeliveryStatus.SENT_TO_DEVICE else MessageDeliveryStatus.FAILED
                dao.updateMessageStatus(msgId, finalStatus.name)
                return success
            } else {
                dao.updateMessageStatus(msgId, MessageDeliveryStatus.QUEUED.name)
                return false
            }
        }
    }

    override suspend fun publishStory(textCaption: String, backgroundHex: String) {
        val myProfile = _localProfile.value ?: return
        val storyId = UUID.randomUUID().toString()
        val story = StoryEntity(
            storyId = storyId,
            ownerPeerId = myProfile.peerId,
            ownerDisplayName = myProfile.displayName,
            textCaption = textCaption,
            backgroundHex = backgroundHex,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000L
        )
        dao.insertStory(story)

        // Broadcast story to all peers discovered on the local Wi-Fi
        val payload = JSONObject().apply {
            put("storyId", storyId)
            put("ownerId", myProfile.peerId)
            put("ownerName", myProfile.displayName)
            put("text", textCaption)
            put("bg", backgroundHex)
            put("createdAt", story.createdAt)
            put("expiresAt", story.expiresAt)
        }
        val packet = WiChatPacket(
            type = ProtocolMessageType.STORY_UPDATE,
            packetId = UUID.randomUUID().toString(),
            senderPeerId = myProfile.peerId,
            payloadJson = payload.toString()
        )
        _discoveredPeers.value.forEach { peer ->
            client.sendPacket(peer.hostAddress, peer.port, packet)
        }
    }

    override suspend fun deleteStory(storyId: String) {
        dao.deleteStoryById(storyId)
    }

    override suspend fun triggerDiscoveryScan() {

        val myProfile = _localProfile.value ?: return
        udpPeersMap.value = emptyMap()
        nsdPeersMap.value = emptyMap()
        nsdManager.stopDiscovery()
        nsdManager.clearDiscoveredPeers()
        nsdManager.startDiscovery(myProfile.peerId)
        val port = server?.boundPort ?: 0
        if (port > 0) {
            udpDiscovery.start(myProfile.peerId, myProfile.displayName, port)
        }
        recomputeDiscoveredPeers()
    }

    private fun handleIncomingPacket(packet: WiChatPacket, remoteIp: String) {
        scope.launch {
            when (packet.type) {
                ProtocolMessageType.CONNECTION_REQUEST -> {
                    try {
                        val json = JSONObject(packet.payloadJson)
                        val reqId = json.getString("reqId")
                        val name = json.optString("name", "Nearby Peer")
                        val bio = json.optString("bio", "")
                        val key = json.optString("key", "")
                        val intro = json.optString("intro", "Hey, let's connect on WiChat!")

                        val entity = ConnectionRequestEntity(
                            requestId = reqId,
                            senderPeerId = packet.senderPeerId,
                            senderDisplayName = name,
                            senderBio = bio,
                            senderPublicKey = key,
                            introText = intro,
                            timestamp = packet.timestamp,
                            status = RequestStatus.PENDING.name
                        )
                        dao.insertRequest(entity)

                        // Trigger Local Notification for incoming request
                        com.nissshh.wichat.core.notification.WiChatNotificationHelper.showRequestNotification(
                            context = context,
                            senderName = name,
                            introText = intro
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing incoming request", e)
                    }
                }
                ProtocolMessageType.REQUEST_RESPONSE -> {
                    try {
                        val json = JSONObject(packet.payloadJson)
                        val accepted = json.optBoolean("accepted", false)
                        val name = json.optString("name", "Connected Peer")
                        val myProfile = _localProfile.value
                        val convId = if (myProfile != null) {
                            getDirectConversationId(myProfile.peerId, packet.senderPeerId)
                        } else {
                            json.optString("convId", "conv_${packet.packetId}")
                        }

                        if (accepted) {
                            val conv = ConversationEntity(
                                conversationId = convId,
                                type = "DIRECT",
                                title = name,
                                subtitle = "Available on Wi-Fi",
                                lastMessage = "Connected over local network",
                                lastTimestamp = System.currentTimeMillis(),
                                peerId = packet.senderPeerId
                            )
                            dao.insertConversation(conv)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling request response", e)
                    }
                }
                ProtocolMessageType.CHAT_MESSAGE -> {
                    try {
                        val json = JSONObject(packet.payloadJson)
                        val rawConvId = json.getString("convId")
                        val text = json.getString("text")
                        val senderName = json.optString("senderName", "")
                        val isGroup = json.optBoolean("isGroup", false)
                        val convTitle = json.optString("convTitle", "")

                        val myProfile = _localProfile.value
                        // If direct conversation, normalize to deterministic conversationId
                        val convId = if (!isGroup && myProfile != null) {
                            getDirectConversationId(myProfile.peerId, packet.senderPeerId)
                        } else {
                            rawConvId
                        }

                        val msg = MessageEntity(
                            messageId = packet.packetId,
                            conversationId = convId,
                            senderPeerId = packet.senderPeerId,
                            isOutgoing = false,
                            contentType = MessageContentType.TEXT.name,
                            textContent = text,
                            fileName = null,
                            fileSize = 0,
                            fileMimeType = null,
                            localUri = null,
                            timestamp = packet.timestamp,
                            deliveryStatus = MessageDeliveryStatus.DELIVERED.name
                        )
                        dao.insertMessage(msg)

                        // Update or insert conversation so it appears in the Chats tab
                        val existingConv = dao.getConversationById(convId)
                        val peer = _discoveredPeers.value.find { it.peerId == packet.senderPeerId }
                        val effectiveTitle = if (isGroup) {
                            if (convTitle.isNotBlank()) convTitle else existingConv?.title ?: "Wi-Fi Group"
                        } else {
                            if (senderName.isNotBlank()) senderName else (peer?.displayName ?: existingConv?.title ?: "Nearby Peer")
                        }

                        val currentUnread = (existingConv?.unreadCount ?: 0) + 1
                        dao.insertConversation(
                            existingConv?.copy(
                                title = effectiveTitle,
                                lastMessage = text,
                                lastTimestamp = packet.timestamp,
                                unreadCount = currentUnread
                            ) ?: ConversationEntity(
                                conversationId = convId,
                                type = if (isGroup) "GROUP" else "DIRECT",
                                title = effectiveTitle,
                                subtitle = if (isGroup) "Wi-Fi Group Chat" else "Available on Wi-Fi",
                                lastMessage = text,
                                lastTimestamp = packet.timestamp,
                                peerId = if (isGroup) null else packet.senderPeerId,
                                unreadCount = currentUnread
                            )
                        )


                        // Trigger Local Notification for incoming message
                        com.nissshh.wichat.core.notification.WiChatNotificationHelper.showMessageNotification(
                            context = context,
                            senderName = effectiveTitle,
                            messageText = text,
                            conversationId = convId
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling incoming chat message", e)
                    }

                }
                ProtocolMessageType.STORY_UPDATE -> {
                    try {
                        val json = JSONObject(packet.payloadJson)
                        val storyId = json.getString("storyId")
                        val ownerId = json.getString("ownerId")
                        val ownerName = json.optString("ownerName", "Nearby Peer")
                        val text = json.getString("text")
                        val bg = json.optString("bg", "#168653")
                        val createdAt = json.optLong("createdAt", System.currentTimeMillis())
                        val expiresAt = json.optLong("expiresAt", System.currentTimeMillis() + 86400000L)

                        val story = StoryEntity(
                            storyId = storyId,
                            ownerPeerId = ownerId,
                            ownerDisplayName = ownerName,
                            textCaption = text,
                            backgroundHex = bg,
                            createdAt = createdAt,
                            expiresAt = expiresAt,
                            isSeen = false
                        )
                        dao.insertStory(story)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling incoming story update", e)
                    }
                }
                else -> {
                    Log.d(TAG, "Received packet type ${packet.type}")
                }

            }
        }
    }
}
