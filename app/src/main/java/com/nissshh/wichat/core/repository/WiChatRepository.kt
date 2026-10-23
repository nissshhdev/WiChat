package com.nissshh.wichat.core.repository

import com.nissshh.wichat.core.model.ChatMessage
import com.nissshh.wichat.core.model.ConnectionRequest
import com.nissshh.wichat.core.model.ConversationItem
import com.nissshh.wichat.core.model.DiscoveredPeer
import com.nissshh.wichat.core.model.NetworkDiagnosticInfo
import com.nissshh.wichat.core.model.RequestStatus
import com.nissshh.wichat.core.model.StoryItem
import com.nissshh.wichat.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WiChatRepository {
    val localProfile: StateFlow<UserProfile?>
    val discoveredPeers: StateFlow<List<DiscoveredPeer>>
    val isScanning: StateFlow<Boolean>
    val networkDiagnostics: StateFlow<NetworkDiagnosticInfo>

    fun getAllConversations(): Flow<List<ConversationItem>>
    fun getMessagesForConversation(convId: String): Flow<List<ChatMessage>>
    fun getAllRequests(): Flow<List<ConnectionRequest>>
    fun getActiveStories(): Flow<List<StoryItem>>

    suspend fun saveProfile(profile: UserProfile)
    suspend fun sendConnectionRequest(peer: DiscoveredPeer, introText: String): Boolean
    suspend fun respondToRequest(requestId: String, accept: Boolean)
    suspend fun blockPeer(peerId: String)
    suspend fun sendMessage(conversationId: String, text: String, peerId: String?): Boolean
    suspend fun publishStory(textCaption: String, backgroundHex: String)
    suspend fun deleteStory(storyId: String)
    suspend fun triggerDiscoveryScan()

    suspend fun startDirectConversation(peer: DiscoveredPeer): String
    suspend fun createGroupConversation(groupTitle: String, memberPeerIds: List<String>): String
    suspend fun clearChat(conversationId: String)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteConversation(conversationId: String)
    suspend fun markConversationAsRead(conversationId: String)
    suspend fun updateProfile(displayName: String, bio: String, avatarUri: String? = null)
}


