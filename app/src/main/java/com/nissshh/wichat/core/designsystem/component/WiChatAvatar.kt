package com.nissshh.wichat.core.designsystem.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun WiChatAvatar(
    name: String,
    avatarUri: String? = null,
    size: Dp = 48.dp,
    isGroup: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(avatarUri) {
        if (!avatarUri.isNullOrBlank()) {
            try {
                val file = File(avatarUri)
                if (file.exists() && file.canRead()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (isGroup) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = name,
                tint = contentColor,
                modifier = Modifier.size(size * 0.55f)
            )
        } else if (name.trim().isNotEmpty()) {
            val initial = name.trim().take(1).uppercase()
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (size.value * 0.42f).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = name,
                tint = contentColor,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
