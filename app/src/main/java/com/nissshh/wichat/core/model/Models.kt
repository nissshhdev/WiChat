package com.nissshh.wichat.core.model

data class UserProfile(
    val peerId: String,
    val displayName: String,
    val bio: String,
    val avatarUri: String? = null,
    val publicKeyString: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class DiscoveredPeer(
    val peerId: String,
    val displayName: String,
    val bio: String,
    val hostAddress: String,
    val port: Int,
    val publicKeyString: String,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isReachable: Boolean = true
)

enum class RequestStatus {
    PENDING, ACCEPTED, DECLINED, BLOCKED
}

data class ConnectionRequest(
    val requestId: String,
    val senderPeerId: String,
    val senderDisplayName: String,
    val senderBio: String,
    val senderPublicKey: String,
    val introText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING
)

enum class ConversationType {
    DIRECT, GROUP
}

data class ConversationItem(
    val conversationId: String,
    val type: ConversationType,
    val title: String,
    val subtitle: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int = 0,
    val peerId: String? = null,
    val isOnline: Boolean = false
)

enum class MessageDeliveryStatus {
    QUEUED, SENDING, SENT_TO_DEVICE, DELIVERED, READ, FAILED
}

enum class MessageContentType {
    TEXT, IMAGE, FILE
}

data class ChatMessage(
    val messageId: String,
    val conversationId: String,
    val senderPeerId: String,
    val isOutgoing: Boolean,
    val contentType: MessageContentType = MessageContentType.TEXT,
    val textContent: String = "",
    val fileName: String? = null,
    val fileSize: Long = 0L,
    val fileMimeType: String? = null,
    val localUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.SENT_TO_DEVICE
)

data class StoryItem(
    val storyId: String,
    val ownerPeerId: String,
    val ownerDisplayName: String,
    val textCaption: String,
    val backgroundHex: String = "#168653",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L, // 24 hours
    val isSeen: Boolean = false
)

data class GroupInfo(
    val groupId: String,
    val title: String,
    val memberCount: Int,
    val memberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class NetworkDiagnosticInfo(
    val isWifiConnected: Boolean,
    val isScanningActive: Boolean,
    val discoveredPeerCount: Int,
    val activePort: Int,
    val isMulticastLockAcquired: Boolean,
    val routerIsolationSuspected: Boolean = false,
    val localIpAddress: String = "192.168.1.xxx"
)
