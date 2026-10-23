package com.nissshh.wichat.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nissshh.wichat.core.model.UserProfile
import com.nissshh.wichat.core.security.SecurityManager
import java.util.UUID

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProfileCreationScreen(
    onProfileCreated: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatarUriString by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Copy selected image to app private storage so it persists permanently
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = java.io.File(context.filesDir, "avatar_profile_${System.currentTimeMillis()}.jpg")
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                avatarUriString = file.absolutePath
            } catch (e: Exception) {
                avatarUriString = uri.toString()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Create Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This is how you will appear to other users on the local Wi-Fi. No phone number or cloud email is required.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Avatar Selection Box
                val avatarBitmap = remember(avatarUriString) {
                    avatarUriString?.let { path ->
                        try {
                            android.graphics.BitmapFactory.decodeFile(path)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    // Camera badge overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Pick Image",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to choose photo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Display Name Input
                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. Aarav, Maya, or Nishant") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                if (isError) {
                    Text(
                        text = "Display name is required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bio Input
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 80) bio = it },
                    label = { Text("Status / Short Bio (Optional)") },
                    placeholder = { Text("e.g. Working in cafe • On Wi-Fi") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    supportingText = {
                        Text(
                            text = "${bio.length}/80",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Button(
                onClick = {
                    if (displayName.isBlank()) {
                        isError = true
                    } else {
                        val keyPair = SecurityManager.getOrCreateIdentityKeyPair()
                        val publicKeyStr = SecurityManager.getPublicKeyBase64(keyPair)
                        val newProfile = UserProfile(
                            peerId = UUID.randomUUID().toString().take(8),
                            displayName = displayName.trim(),
                            bio = bio.trim().ifEmpty { "Available on local Wi-Fi" },
                            avatarUri = avatarUriString,
                            publicKeyString = publicKeyStr
                        )
                        onProfileCreated(newProfile)
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Finish Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
