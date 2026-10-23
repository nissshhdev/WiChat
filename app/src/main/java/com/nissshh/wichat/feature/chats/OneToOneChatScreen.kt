package com.nissshh.wichat.feature.chats

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nissshh.wichat.core.designsystem.component.WiChatAvatar
import com.nissshh.wichat.core.model.ChatMessage
import com.nissshh.wichat.core.model.MessageDeliveryStatus
import com.nissshh.wichat.core.repository.WiChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MediaPreviewTarget(
    val filePath: String,
    val isVideo: Boolean,
    val title: String
)

data class FileDownloadTarget(
    val sourcePath: String,
    val displayName: String,
    val isVideo: Boolean
)

suspend fun downloadFileToPublicStorage(
    context: Context,
    target: FileDownloadTarget
): Boolean = withContext(Dispatchers.IO) {
    val sourceFile = File(target.sourcePath)
    if (!sourceFile.exists() || sourceFile.length() == 0L) {
        return@withContext false
    }

    val notificationId = (System.currentTimeMillis() % 100000).toInt() + 2000
    val rawName = target.displayName
    val extension = when {
        rawName.contains(".") -> rawName.substringAfterLast('.').lowercase()
        target.isVideo -> "mp4"
        else -> "jpg"
    }

    val fileName = if (rawName.contains(".")) {
        rawName
    } else {
        "${rawName}_${System.currentTimeMillis()}.$extension"
    }

    val mimeType = when (extension) {
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "mov", "quicktime" -> "video/quicktime"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "zip" -> "application/zip"
        else -> if (target.isVideo) "video/mp4" else "image/jpeg"
    }

    com.nissshh.wichat.core.notification.WiChatNotificationHelper.showDownloadProgressNotification(
        context = context,
        notificationId = notificationId,
        fileName = fileName,
        progress = 0,
        max = 100
    )

    var success = false
    try {
        val totalBytes = sourceFile.length()
        val resolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/WiChat")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri, "w")?.use { out ->
                    FileInputStream(sourceFile).use { input ->
                        val buffer = ByteArray(64 * 1024)
                        var bytesRead: Int
                        var copied: Long = 0
                        var lastProgressReport = 0

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            out.write(buffer, 0, bytesRead)
                            copied += bytesRead
                            if (totalBytes > 0) {
                                val currentPct = ((copied * 100) / totalBytes).toInt()
                                if (currentPct - lastProgressReport >= 10 || currentPct == 100) {
                                    lastProgressReport = currentPct
                                    com.nissshh.wichat.core.notification.WiChatNotificationHelper.showDownloadProgressNotification(
                                        context = context,
                                        notificationId = notificationId,
                                        fileName = fileName,
                                        progress = currentPct,
                                        max = 100
                                    )
                                }
                            }
                        }
                        out.flush()
                    }
                }

                // Clear IS_PENDING so other apps can immediately see and open the complete file
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                success = true
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val wiChatDir = File(downloadsDir, "WiChat").apply { mkdirs() }
            val destFile = File(wiChatDir, fileName)

            FileOutputStream(destFile).use { out ->
                FileInputStream(sourceFile).use { input ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    var copied: Long = 0
                    var lastProgressReport = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        copied += bytesRead
                        if (totalBytes > 0) {
                            val currentPct = ((copied * 100) / totalBytes).toInt()
                            if (currentPct - lastProgressReport >= 10 || currentPct == 100) {
                                lastProgressReport = currentPct
                                com.nissshh.wichat.core.notification.WiChatNotificationHelper.showDownloadProgressNotification(
                                    context = context,
                                    notificationId = notificationId,
                                    fileName = fileName,
                                    progress = currentPct,
                                    max = 100
                                )
                            }
                        }
                    }
                    out.flush()
                }
            }
            success = true
        }
    } catch (e: Exception) {
        success = false
    } finally {
        com.nissshh.wichat.core.notification.WiChatNotificationHelper.showDownloadCompleteNotification(
            context = context,
            notificationId = notificationId,
            fileName = fileName,
            success = success
        )
    }

    success
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneToOneChatScreen(
    conversationId: String,
    title: String,
    repository: WiChatRepository,
    onBackPress: () -> Unit
) {
    val context = LocalContext.current
    val messages by repository.getMessagesForConversation(conversationId).collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val discoveredPeers by repository.discoveredPeers.collectAsState()
    val isGroup = conversationId.startsWith("group_")

    var previewMedia by remember { mutableStateOf<MediaPreviewTarget?>(null) }
    var fileToDownload by remember { mutableStateOf<FileDownloadTarget?>(null) }

    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val cr = context.contentResolver
                    val mime = cr.getType(uri) ?: ""
                    val isVid = mime.startsWith("video") || uri.toString().contains(".mp4")
                    val ext = if (isVid) ".mp4" else ".jpg"
                    val file = File(context.filesDir, "media_${System.currentTimeMillis()}$ext")
                    cr.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val label = if (isVid) "🎬 Video: ${file.name}" else "📷 Photo: ${file.name}"
                    repository.sendMessage(conversationId, "$label\n[local_media:${file.absolutePath}]", null)
                } catch (e: Exception) {
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "attachment"
                    repository.sendMessage(conversationId, "📎 Shared media: $fileName", null)
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(conversationId) {
        repository.markConversationAsRead(conversationId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // App Bar
            var showClearChatDialog by remember { mutableStateOf(false) }

            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WiChatAvatar(
                            name = title,
                            avatarUri = null,
                            size = 38.dp,
                            isGroup = isGroup
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGroup) "Wi-Fi Group Chat • Local Subnet" else "Direct LAN connection • End-to-end local",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClearChatDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (showClearChatDialog) {
                AlertDialog(
                    onDismissRequest = { showClearChatDialog = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Text(
                            text = "Clear all messages?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "All messages in this chat will be permanently deleted from this device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showClearChatDialog = false
                                scope.launch {
                                    repository.clearChat(conversationId)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Clear", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showClearChatDialog = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }

            // Message List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = false,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(messages, key = { it.messageId }) { msg ->
                    val senderPeer = discoveredPeers.find { it.peerId == msg.senderPeerId }
                    val senderName = senderPeer?.displayName ?: (if (msg.isOutgoing) "You" else "Peer")
                    MessageBubble(
                        message = msg,
                        senderName = senderName,
                        showSenderName = isGroup || !msg.isOutgoing,
                        onMediaClick = { target ->
                            previewMedia = target
                        },
                        onMediaLongClick = { target ->
                            fileToDownload = target
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Bottom Composer
            ChatComposer(
                text = inputText,
                onTextChange = { inputText = it },
                onAttachClick = {
                    attachmentPickerLauncher.launch("*/*")
                },
                onSend = {
                    val text = inputText.trim()
                    if (text.isNotEmpty()) {
                        inputText = ""
                        scope.launch {
                            repository.sendMessage(conversationId, text, null)
                        }
                    }
                }
            )
        }
    }

    // Inbuilt Image and Video Preview Dialog
    if (previewMedia != null) {
        val target = previewMedia!!
        InbuiltMediaViewerDialog(
            target = target,
            onDismiss = { previewMedia = null }
        )
    }

    // Long press file download confirmation dialog
    if (fileToDownload != null) {
        val target = fileToDownload!!
        AlertDialog(
            onDismissRequest = { fileToDownload = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Download to Phone?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Save \"${target.displayName}\" to your device's Downloads folder?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentTarget = fileToDownload
                        fileToDownload = null
                        if (currentTarget != null) {
                            scope.launch {
                                val success = downloadFileToPublicStorage(context, currentTarget)
                                val msg = if (success) "Saved to Downloads/WiChat" else "Failed to save file"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Download", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { fileToDownload = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun InbuiltMediaViewerDialog(
    target: MediaPreviewTarget,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Media Preview Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val file = File(target.filePath)
                    if (target.isVideo) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    val mc = MediaController(ctx)
                                    mc.setAnchorView(this)
                                    setMediaController(mc)
                                    setVideoPath(target.filePath)
                                    setOnPreparedListener { start() }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val bmp = remember(target.filePath) {
                            try {
                                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Preview Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Unable to load image file.",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    senderName: String = "",
    showSenderName: Boolean = false,
    onMediaClick: (MediaPreviewTarget) -> Unit = {},
    onMediaLongClick: (FileDownloadTarget) -> Unit = {}
) {
    val isOutgoing = message.isOutgoing
    val alignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isOutgoing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val textColor = if (isOutgoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    // Check if message has an attached local media path
    val rawText = message.textContent
    val hasLocalMedia = rawText.contains("[local_media:")
    val mediaPath = if (hasLocalMedia) {
        rawText.substringAfter("[local_media:").substringBefore("]")
    } else null
    val displayContent = if (hasLocalMedia) {
        rawText.substringBefore("\n[local_media:")
    } else {
        rawText
    }
    val isVideo = mediaPath?.endsWith(".mp4") == true || displayContent.contains("Video")
    val isImage = mediaPath?.endsWith(".jpg") == true || mediaPath?.endsWith(".png") == true || displayContent.contains("Photo")

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOutgoing) 16.dp else 4.dp,
                        bottomEnd = if (isOutgoing) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (mediaPath != null) {
                            val cleanName = File(mediaPath).name
                            onMediaLongClick(
                                FileDownloadTarget(
                                    sourcePath = mediaPath,
                                    displayName = cleanName,
                                    isVideo = isVideo
                                )
                            )
                        }
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (!isOutgoing && showSenderName && senderName.isNotBlank()) {
                Text(
                    text = "@$senderName",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Inbuilt Media Card inside bubble if image or video
            if (mediaPath != null && (isImage || isVideo)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(bottom = 6.dp)
                        .combinedClickable(
                            onClick = {
                                onMediaClick(
                                    MediaPreviewTarget(
                                        filePath = mediaPath,
                                        isVideo = isVideo,
                                        title = displayContent
                                    )
                                )
                            },
                            onLongClick = {
                                val cleanName = File(mediaPath).name
                                onMediaLongClick(
                                    FileDownloadTarget(
                                        sourcePath = mediaPath,
                                        displayName = cleanName,
                                        isVideo = isVideo
                                    )
                                )
                            }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val previewBmp = remember(mediaPath) {
                            try {
                                val file = File(mediaPath)
                                if (file.exists() && isImage) BitmapFactory.decodeFile(file.absolutePath) else null
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (previewBmp != null) {
                            Image(
                                bitmap = previewBmp.asImageBitmap(),
                                contentDescription = "Media Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isVideo) Icons.Default.PlayCircle else Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isVideo) "Tap to play video" else "Tap to view photo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = displayContent,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeString = timeFormat.format(Date(message.timestamp))

                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = textColor.copy(alpha = 0.6f)
                )

                if (isOutgoing) {
                    Spacer(modifier = Modifier.width(4.dp))
                    when (message.deliveryStatus) {
                        MessageDeliveryStatus.QUEUED -> {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Queued",
                                tint = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        MessageDeliveryStatus.SENDING -> {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Sending",
                                tint = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        MessageDeliveryStatus.SENT_TO_DEVICE -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        MessageDeliveryStatus.DELIVERED, MessageDeliveryStatus.READ -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        MessageDeliveryStatus.FAILED -> {
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onAttachClick: () -> Unit = {},
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachClick) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach File, Photo or Video",
                    tint = MaterialTheme.colorScheme.primary
                )
            }


            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Message on Wi-Fi...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            val canSend = text.isNotBlank()

            IconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (canSend) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
