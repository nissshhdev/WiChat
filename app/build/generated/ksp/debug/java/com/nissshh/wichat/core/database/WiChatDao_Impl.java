package com.nissshh.wichat.core.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WiChatDao_Impl implements WiChatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProfileEntity> __insertionAdapterOfProfileEntity;

  private final EntityInsertionAdapter<ConnectionRequestEntity> __insertionAdapterOfConnectionRequestEntity;

  private final EntityInsertionAdapter<ConversationEntity> __insertionAdapterOfConversationEntity;

  private final EntityInsertionAdapter<MessageEntity> __insertionAdapterOfMessageEntity;

  private final EntityInsertionAdapter<StoryEntity> __insertionAdapterOfStoryEntity;

  private final EntityInsertionAdapter<ContactEntity> __insertionAdapterOfContactEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateRequestStatus;

  private final SharedSQLiteStatement __preparedStmtOfMarkConversationAsRead;

  private final SharedSQLiteStatement __preparedStmtOfClearMessagesForConversation;

  private final SharedSQLiteStatement __preparedStmtOfDeleteConversation;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageStatus;

  private final SharedSQLiteStatement __preparedStmtOfDeleteStoryById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredStories;

  private final SharedSQLiteStatement __preparedStmtOfBlockContact;

  public WiChatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProfileEntity = new EntityInsertionAdapter<ProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `profiles` (`peerId`,`displayName`,`bio`,`avatarUri`,`publicKeyString`,`createdAt`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProfileEntity entity) {
        statement.bindString(1, entity.getPeerId());
        statement.bindString(2, entity.getDisplayName());
        statement.bindString(3, entity.getBio());
        if (entity.getAvatarUri() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAvatarUri());
        }
        statement.bindString(5, entity.getPublicKeyString());
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfConnectionRequestEntity = new EntityInsertionAdapter<ConnectionRequestEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `connection_requests` (`requestId`,`senderPeerId`,`senderDisplayName`,`senderBio`,`senderPublicKey`,`introText`,`timestamp`,`status`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ConnectionRequestEntity entity) {
        statement.bindString(1, entity.getRequestId());
        statement.bindString(2, entity.getSenderPeerId());
        statement.bindString(3, entity.getSenderDisplayName());
        statement.bindString(4, entity.getSenderBio());
        statement.bindString(5, entity.getSenderPublicKey());
        statement.bindString(6, entity.getIntroText());
        statement.bindLong(7, entity.getTimestamp());
        statement.bindString(8, entity.getStatus());
      }
    };
    this.__insertionAdapterOfConversationEntity = new EntityInsertionAdapter<ConversationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `conversations` (`conversationId`,`type`,`title`,`subtitle`,`lastMessage`,`lastTimestamp`,`peerId`,`isMuted`,`isPinned`,`unreadCount`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ConversationEntity entity) {
        statement.bindString(1, entity.getConversationId());
        statement.bindString(2, entity.getType());
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getSubtitle());
        statement.bindString(5, entity.getLastMessage());
        statement.bindLong(6, entity.getLastTimestamp());
        if (entity.getPeerId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPeerId());
        }
        final int _tmp = entity.isMuted() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.isPinned() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getUnreadCount());
      }
    };
    this.__insertionAdapterOfMessageEntity = new EntityInsertionAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`messageId`,`conversationId`,`senderPeerId`,`isOutgoing`,`contentType`,`textContent`,`fileName`,`fileSize`,`fileMimeType`,`localUri`,`timestamp`,`deliveryStatus`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getMessageId());
        statement.bindString(2, entity.getConversationId());
        statement.bindString(3, entity.getSenderPeerId());
        final int _tmp = entity.isOutgoing() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindString(5, entity.getContentType());
        statement.bindString(6, entity.getTextContent());
        if (entity.getFileName() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getFileName());
        }
        statement.bindLong(8, entity.getFileSize());
        if (entity.getFileMimeType() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getFileMimeType());
        }
        if (entity.getLocalUri() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getLocalUri());
        }
        statement.bindLong(11, entity.getTimestamp());
        statement.bindString(12, entity.getDeliveryStatus());
      }
    };
    this.__insertionAdapterOfStoryEntity = new EntityInsertionAdapter<StoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `stories` (`storyId`,`ownerPeerId`,`ownerDisplayName`,`textCaption`,`backgroundHex`,`createdAt`,`expiresAt`,`isSeen`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StoryEntity entity) {
        statement.bindString(1, entity.getStoryId());
        statement.bindString(2, entity.getOwnerPeerId());
        statement.bindString(3, entity.getOwnerDisplayName());
        statement.bindString(4, entity.getTextCaption());
        statement.bindString(5, entity.getBackgroundHex());
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getExpiresAt());
        final int _tmp = entity.isSeen() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__insertionAdapterOfContactEntity = new EntityInsertionAdapter<ContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `contacts` (`peerId`,`displayName`,`bio`,`publicKeyString`,`isAccepted`,`isBlocked`,`lastSeenTimestamp`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ContactEntity entity) {
        statement.bindString(1, entity.getPeerId());
        statement.bindString(2, entity.getDisplayName());
        statement.bindString(3, entity.getBio());
        statement.bindString(4, entity.getPublicKeyString());
        final int _tmp = entity.isAccepted() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final int _tmp_1 = entity.isBlocked() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        statement.bindLong(7, entity.getLastSeenTimestamp());
      }
    };
    this.__preparedStmtOfUpdateRequestStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE connection_requests SET status = ? WHERE requestId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkConversationAsRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE conversations SET unreadCount = 0 WHERE conversationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearMessagesForConversation = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE conversationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteConversation = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM conversations WHERE conversationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateMessageStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET deliveryStatus = ? WHERE messageId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteStoryById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM stories WHERE storyId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpiredStories = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM stories WHERE expiresAt <= ?";
        return _query;
      }
    };
    this.__preparedStmtOfBlockContact = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE contacts SET isBlocked = 1 WHERE peerId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object saveProfile(final ProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProfileEntity.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertRequest(final ConnectionRequestEntity request,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfConnectionRequestEntity.insert(request);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertConversation(final ConversationEntity conv,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfConversationEntity.insert(conv);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStory(final StoryEntity story, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStoryEntity.insert(story);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertContact(final ContactEntity contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfContactEntity.insert(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRequestStatus(final String requestId, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateRequestStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindString(_argIndex, requestId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateRequestStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markConversationAsRead(final String convId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkConversationAsRead.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, convId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkConversationAsRead.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearMessagesForConversation(final String convId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearMessagesForConversation.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, convId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearMessagesForConversation.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteConversation(final String convId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteConversation.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, convId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteConversation.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageStatus(final String messageId, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindString(_argIndex, messageId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateMessageStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteStoryById(final String storyId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteStoryById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, storyId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteStoryById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpiredStories(final long now, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredStories.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, now);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExpiredStories.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object blockContact(final String peerId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfBlockContact.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, peerId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfBlockContact.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getLocalProfile(final Continuation<? super ProfileEntity> $completion) {
    final String _sql = "SELECT * FROM profiles LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProfileEntity>() {
      @Override
      @Nullable
      public ProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "peerId");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfAvatarUri = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarUri");
          final int _cursorIndexOfPublicKeyString = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKeyString");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpPeerId;
            _tmpPeerId = _cursor.getString(_cursorIndexOfPeerId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpBio;
            _tmpBio = _cursor.getString(_cursorIndexOfBio);
            final String _tmpAvatarUri;
            if (_cursor.isNull(_cursorIndexOfAvatarUri)) {
              _tmpAvatarUri = null;
            } else {
              _tmpAvatarUri = _cursor.getString(_cursorIndexOfAvatarUri);
            }
            final String _tmpPublicKeyString;
            _tmpPublicKeyString = _cursor.getString(_cursorIndexOfPublicKeyString);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ProfileEntity(_tmpPeerId,_tmpDisplayName,_tmpBio,_tmpAvatarUri,_tmpPublicKeyString,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ConnectionRequestEntity>> getAllRequests() {
    final String _sql = "SELECT * FROM connection_requests ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"connection_requests"}, new Callable<List<ConnectionRequestEntity>>() {
      @Override
      @NonNull
      public List<ConnectionRequestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "requestId");
          final int _cursorIndexOfSenderPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPeerId");
          final int _cursorIndexOfSenderDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDisplayName");
          final int _cursorIndexOfSenderBio = CursorUtil.getColumnIndexOrThrow(_cursor, "senderBio");
          final int _cursorIndexOfSenderPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPublicKey");
          final int _cursorIndexOfIntroText = CursorUtil.getColumnIndexOrThrow(_cursor, "introText");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<ConnectionRequestEntity> _result = new ArrayList<ConnectionRequestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ConnectionRequestEntity _item;
            final String _tmpRequestId;
            _tmpRequestId = _cursor.getString(_cursorIndexOfRequestId);
            final String _tmpSenderPeerId;
            _tmpSenderPeerId = _cursor.getString(_cursorIndexOfSenderPeerId);
            final String _tmpSenderDisplayName;
            _tmpSenderDisplayName = _cursor.getString(_cursorIndexOfSenderDisplayName);
            final String _tmpSenderBio;
            _tmpSenderBio = _cursor.getString(_cursorIndexOfSenderBio);
            final String _tmpSenderPublicKey;
            _tmpSenderPublicKey = _cursor.getString(_cursorIndexOfSenderPublicKey);
            final String _tmpIntroText;
            _tmpIntroText = _cursor.getString(_cursorIndexOfIntroText);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new ConnectionRequestEntity(_tmpRequestId,_tmpSenderPeerId,_tmpSenderDisplayName,_tmpSenderBio,_tmpSenderPublicKey,_tmpIntroText,_tmpTimestamp,_tmpStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getRequestById(final String requestId,
      final Continuation<? super ConnectionRequestEntity> $completion) {
    final String _sql = "SELECT * FROM connection_requests WHERE requestId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, requestId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ConnectionRequestEntity>() {
      @Override
      @Nullable
      public ConnectionRequestEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "requestId");
          final int _cursorIndexOfSenderPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPeerId");
          final int _cursorIndexOfSenderDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderDisplayName");
          final int _cursorIndexOfSenderBio = CursorUtil.getColumnIndexOrThrow(_cursor, "senderBio");
          final int _cursorIndexOfSenderPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPublicKey");
          final int _cursorIndexOfIntroText = CursorUtil.getColumnIndexOrThrow(_cursor, "introText");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final ConnectionRequestEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpRequestId;
            _tmpRequestId = _cursor.getString(_cursorIndexOfRequestId);
            final String _tmpSenderPeerId;
            _tmpSenderPeerId = _cursor.getString(_cursorIndexOfSenderPeerId);
            final String _tmpSenderDisplayName;
            _tmpSenderDisplayName = _cursor.getString(_cursorIndexOfSenderDisplayName);
            final String _tmpSenderBio;
            _tmpSenderBio = _cursor.getString(_cursorIndexOfSenderBio);
            final String _tmpSenderPublicKey;
            _tmpSenderPublicKey = _cursor.getString(_cursorIndexOfSenderPublicKey);
            final String _tmpIntroText;
            _tmpIntroText = _cursor.getString(_cursorIndexOfIntroText);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _result = new ConnectionRequestEntity(_tmpRequestId,_tmpSenderPeerId,_tmpSenderDisplayName,_tmpSenderBio,_tmpSenderPublicKey,_tmpIntroText,_tmpTimestamp,_tmpStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getConversationById(final String convId,
      final Continuation<? super ConversationEntity> $completion) {
    final String _sql = "SELECT * FROM conversations WHERE conversationId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, convId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ConversationEntity>() {
      @Override
      @Nullable
      public ConversationEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubtitle = CursorUtil.getColumnIndexOrThrow(_cursor, "subtitle");
          final int _cursorIndexOfLastMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessage");
          final int _cursorIndexOfLastTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTimestamp");
          final int _cursorIndexOfPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "peerId");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final ConversationEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubtitle;
            _tmpSubtitle = _cursor.getString(_cursorIndexOfSubtitle);
            final String _tmpLastMessage;
            _tmpLastMessage = _cursor.getString(_cursorIndexOfLastMessage);
            final long _tmpLastTimestamp;
            _tmpLastTimestamp = _cursor.getLong(_cursorIndexOfLastTimestamp);
            final String _tmpPeerId;
            if (_cursor.isNull(_cursorIndexOfPeerId)) {
              _tmpPeerId = null;
            } else {
              _tmpPeerId = _cursor.getString(_cursorIndexOfPeerId);
            }
            final boolean _tmpIsMuted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp != 0;
            final boolean _tmpIsPinned;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_1 != 0;
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            _result = new ConversationEntity(_tmpConversationId,_tmpType,_tmpTitle,_tmpSubtitle,_tmpLastMessage,_tmpLastTimestamp,_tmpPeerId,_tmpIsMuted,_tmpIsPinned,_tmpUnreadCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ConversationEntity>> getAllConversations() {
    final String _sql = "SELECT * FROM conversations ORDER BY lastTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"conversations"}, new Callable<List<ConversationEntity>>() {
      @Override
      @NonNull
      public List<ConversationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSubtitle = CursorUtil.getColumnIndexOrThrow(_cursor, "subtitle");
          final int _cursorIndexOfLastMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessage");
          final int _cursorIndexOfLastTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTimestamp");
          final int _cursorIndexOfPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "peerId");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final List<ConversationEntity> _result = new ArrayList<ConversationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ConversationEntity _item;
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSubtitle;
            _tmpSubtitle = _cursor.getString(_cursorIndexOfSubtitle);
            final String _tmpLastMessage;
            _tmpLastMessage = _cursor.getString(_cursorIndexOfLastMessage);
            final long _tmpLastTimestamp;
            _tmpLastTimestamp = _cursor.getLong(_cursorIndexOfLastTimestamp);
            final String _tmpPeerId;
            if (_cursor.isNull(_cursorIndexOfPeerId)) {
              _tmpPeerId = null;
            } else {
              _tmpPeerId = _cursor.getString(_cursorIndexOfPeerId);
            }
            final boolean _tmpIsMuted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp != 0;
            final boolean _tmpIsPinned;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_1 != 0;
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            _item = new ConversationEntity(_tmpConversationId,_tmpType,_tmpTitle,_tmpSubtitle,_tmpLastMessage,_tmpLastTimestamp,_tmpPeerId,_tmpIsMuted,_tmpIsPinned,_tmpUnreadCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MessageEntity>> getMessagesForConversation(final String convId) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, convId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPeerId");
          final int _cursorIndexOfIsOutgoing = CursorUtil.getColumnIndexOrThrow(_cursor, "isOutgoing");
          final int _cursorIndexOfContentType = CursorUtil.getColumnIndexOrThrow(_cursor, "contentType");
          final int _cursorIndexOfTextContent = CursorUtil.getColumnIndexOrThrow(_cursor, "textContent");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfFileMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "fileMimeType");
          final int _cursorIndexOfLocalUri = CursorUtil.getColumnIndexOrThrow(_cursor, "localUri");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDeliveryStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "deliveryStatus");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpMessageId;
            _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderPeerId;
            _tmpSenderPeerId = _cursor.getString(_cursorIndexOfSenderPeerId);
            final boolean _tmpIsOutgoing;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOutgoing);
            _tmpIsOutgoing = _tmp != 0;
            final String _tmpContentType;
            _tmpContentType = _cursor.getString(_cursorIndexOfContentType);
            final String _tmpTextContent;
            _tmpTextContent = _cursor.getString(_cursorIndexOfTextContent);
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final String _tmpFileMimeType;
            if (_cursor.isNull(_cursorIndexOfFileMimeType)) {
              _tmpFileMimeType = null;
            } else {
              _tmpFileMimeType = _cursor.getString(_cursorIndexOfFileMimeType);
            }
            final String _tmpLocalUri;
            if (_cursor.isNull(_cursorIndexOfLocalUri)) {
              _tmpLocalUri = null;
            } else {
              _tmpLocalUri = _cursor.getString(_cursorIndexOfLocalUri);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpDeliveryStatus;
            _tmpDeliveryStatus = _cursor.getString(_cursorIndexOfDeliveryStatus);
            _item = new MessageEntity(_tmpMessageId,_tmpConversationId,_tmpSenderPeerId,_tmpIsOutgoing,_tmpContentType,_tmpTextContent,_tmpFileName,_tmpFileSize,_tmpFileMimeType,_tmpLocalUri,_tmpTimestamp,_tmpDeliveryStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<StoryEntity>> getActiveStories(final long now) {
    final String _sql = "SELECT * FROM stories WHERE expiresAt > ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"stories"}, new Callable<List<StoryEntity>>() {
      @Override
      @NonNull
      public List<StoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfStoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "storyId");
          final int _cursorIndexOfOwnerPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerPeerId");
          final int _cursorIndexOfOwnerDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerDisplayName");
          final int _cursorIndexOfTextCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "textCaption");
          final int _cursorIndexOfBackgroundHex = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundHex");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfIsSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "isSeen");
          final List<StoryEntity> _result = new ArrayList<StoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StoryEntity _item;
            final String _tmpStoryId;
            _tmpStoryId = _cursor.getString(_cursorIndexOfStoryId);
            final String _tmpOwnerPeerId;
            _tmpOwnerPeerId = _cursor.getString(_cursorIndexOfOwnerPeerId);
            final String _tmpOwnerDisplayName;
            _tmpOwnerDisplayName = _cursor.getString(_cursorIndexOfOwnerDisplayName);
            final String _tmpTextCaption;
            _tmpTextCaption = _cursor.getString(_cursorIndexOfTextCaption);
            final String _tmpBackgroundHex;
            _tmpBackgroundHex = _cursor.getString(_cursorIndexOfBackgroundHex);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final boolean _tmpIsSeen;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSeen);
            _tmpIsSeen = _tmp != 0;
            _item = new StoryEntity(_tmpStoryId,_tmpOwnerPeerId,_tmpOwnerDisplayName,_tmpTextCaption,_tmpBackgroundHex,_tmpCreatedAt,_tmpExpiresAt,_tmpIsSeen);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ContactEntity>> getAllContacts() {
    final String _sql = "SELECT * FROM contacts";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"contacts"}, new Callable<List<ContactEntity>>() {
      @Override
      @NonNull
      public List<ContactEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPeerId = CursorUtil.getColumnIndexOrThrow(_cursor, "peerId");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfPublicKeyString = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKeyString");
          final int _cursorIndexOfIsAccepted = CursorUtil.getColumnIndexOrThrow(_cursor, "isAccepted");
          final int _cursorIndexOfIsBlocked = CursorUtil.getColumnIndexOrThrow(_cursor, "isBlocked");
          final int _cursorIndexOfLastSeenTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeenTimestamp");
          final List<ContactEntity> _result = new ArrayList<ContactEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ContactEntity _item;
            final String _tmpPeerId;
            _tmpPeerId = _cursor.getString(_cursorIndexOfPeerId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpBio;
            _tmpBio = _cursor.getString(_cursorIndexOfBio);
            final String _tmpPublicKeyString;
            _tmpPublicKeyString = _cursor.getString(_cursorIndexOfPublicKeyString);
            final boolean _tmpIsAccepted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAccepted);
            _tmpIsAccepted = _tmp != 0;
            final boolean _tmpIsBlocked;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsBlocked);
            _tmpIsBlocked = _tmp_1 != 0;
            final long _tmpLastSeenTimestamp;
            _tmpLastSeenTimestamp = _cursor.getLong(_cursorIndexOfLastSeenTimestamp);
            _item = new ContactEntity(_tmpPeerId,_tmpDisplayName,_tmpBio,_tmpPublicKeyString,_tmpIsAccepted,_tmpIsBlocked,_tmpLastSeenTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
