package com.nissshh.wichat.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nissshh.wichat.MainActivity
import com.nissshh.wichat.R

object WiChatNotificationHelper {

    const val CHANNEL_MESSAGES = "wichat_messages_channel"
    const val CHANNEL_REQUESTS = "wichat_requests_channel"

    private const val NOTIFICATION_ID_MESSAGES = 1001
    private const val NOTIFICATION_ID_REQUESTS = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Messages Channel
            val msgChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "WiChat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Direct Wi-Fi messages and group chat notifications"
                enableVibration(true)
                setShowBadge(true)
            }

            // Connection Requests Channel
            val reqChannel = NotificationChannel(
                CHANNEL_REQUESTS,
                "Connection Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Invites and incoming connections from local peers"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(msgChannel)
            notificationManager.createNotificationChannel(reqChannel)
        }
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        conversationId: String
    ) {
        // Message notifications should NOT appear when the app is already open
        if (com.nissshh.wichat.WiChatApplication.isAppInForeground) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversationId", conversationId)
            putExtra("title", senderName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            conversationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                conversationId.hashCode(),
                notification
            )
        } catch (ignored: SecurityException) {}
    }

    fun showRequestNotification(
        context: Context,
        senderName: String,
        introText: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_REQUESTS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = if (introText.isNotBlank()) introText else "Wants to connect with you over Wi-Fi"

        val notification = NotificationCompat.Builder(context, CHANNEL_REQUESTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Connection Request: $senderName")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_REQUESTS,
                notification
            )
        } catch (ignored: SecurityException) {}
    }
}
