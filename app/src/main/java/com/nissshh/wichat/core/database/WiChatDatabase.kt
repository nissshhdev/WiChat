package com.nissshh.wichat.core.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val peerId: String,
    val displayName: String,
    val bio: String,
    val avatarUri: String?,
    val publicKeyString: String,
    val createdAt: Long
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val peerId: String,
    val displayName: String,
    val bio: String,
    val publicKeyString: String,
    val isAccepted: Boolean,
    val isBlocked: Boolean = false,
    val lastSeenTimestamp: Long
)

@Entity(tableName = "connection_requests")
data class ConnectionRequestEntity(
    @PrimaryKey val requestId: String,
    val senderPeerId: String,
    val senderDisplayName: String,
    val senderBio: String,
    val senderPublicKey: String,
    val introText: String,
    val timestamp: Long,
    val status: String // PENDING, ACCEPTED, DECLINED, BLOCKED
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val senderPeerId: String,
    val isOutgoing: Boolean,
    val contentType: String,
    val textContent: String,
    val fileName: String?,
    val fileSize: Long,
    val fileMimeType: String?,
    val localUri: String?,
    val timestamp: Long,
    val deliveryStatus: String
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val type: String, // DIRECT, GROUP
    val title: String,
    val subtitle: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val peerId: String?,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0
)


@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val storyId: String,
    val ownerPeerId: String,
    val ownerDisplayName: String,
    val textCaption: String,
    val backgroundHex: String,
    val createdAt: Long,
    val expiresAt: Long,
    val isSeen: Boolean = false
)

@Dao
interface WiChatDao {
    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getLocalProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ProfileEntity)

    @Query("SELECT * FROM connection_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<ConnectionRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ConnectionRequestEntity)

    @Query("SELECT * FROM connection_requests WHERE requestId = :requestId LIMIT 1")
    suspend fun getRequestById(requestId: String): ConnectionRequestEntity?

    @Query("UPDATE connection_requests SET status = :status WHERE requestId = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String)

    @Query("SELECT * FROM conversations WHERE conversationId = :convId LIMIT 1")
    suspend fun getConversationById(convId: String): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY lastTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conv: ConversationEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>>

    @Query("UPDATE conversations SET unreadCount = 0 WHERE conversationId = :convId")
    suspend fun markConversationAsRead(convId: String)

    @Query("DELETE FROM messages WHERE conversationId = :convId")
    suspend fun clearMessagesForConversation(convId: String)


    @Query("DELETE FROM conversations WHERE conversationId = :convId")
    suspend fun deleteConversation(convId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("UPDATE messages SET deliveryStatus = :status WHERE messageId = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("SELECT * FROM stories WHERE expiresAt > :now ORDER BY createdAt DESC")
    fun getActiveStories(now: Long = System.currentTimeMillis()): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Query("DELETE FROM stories WHERE storyId = :storyId")
    suspend fun deleteStoryById(storyId: String)

    @Query("DELETE FROM stories WHERE expiresAt <= :now")
    suspend fun deleteExpiredStories(now: Long = System.currentTimeMillis())


    @Query("SELECT * FROM contacts")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isBlocked = 1 WHERE peerId = :peerId")
    suspend fun blockContact(peerId: String)
}

@Database(
    entities = [
        ProfileEntity::class,
        ContactEntity::class,
        ConnectionRequestEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        StoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WiChatDatabase : RoomDatabase() {
    abstract fun wiChatDao(): WiChatDao
}
