package com.nissshh.wichat.feature.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.nissshh.wichat.core.model.ConversationItem
import com.nissshh.wichat.core.model.ConversationType
import com.nissshh.wichat.core.model.DiscoveredPeer
import com.nissshh.wichat.core.repository.WiChatRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsScreen(
    repository: WiChatRepository,
    onOpenConversation: (conversationId: String, title: String) -> Unit
) {
    val conversations by repository.getAllConversations().collectAsState(initial = emptyList())
    val peers by repository.discoveredPeers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<ConversationItem?>(null) }
    val scope = rememberCoroutineScope()

    val filtered = conversations.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat or Group"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Direct messages & groups on local Wi-Fi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (conversations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No conversations yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap the + button to start a chat with any peer discovered on your Wi-Fi, or create a group.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showNewChatDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Chat")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.conversationId }) { conv ->
                        ConversationRowItem(
                            conversation = conv,
                            onClick = { onOpenConversation(conv.conversationId, conv.title) },
                            onDeleteClick = { conversationToDelete = conv }
                        )
                    }
                }
            }
        }
    }

    if (conversationToDelete != null) {
        val target = conversationToDelete!!
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Delete conversation?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete the chat with \"${target.title}\"? All messages will be permanently removed from this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = target.conversationId
                        conversationToDelete = null
                        scope.launch {
                            repository.deleteConversation(id)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { conversationToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showNewChatDialog) {
        NewChatModalDialog(
            discoveredPeers = peers,
            onDismiss = { showNewChatDialog = false },
            onStartDirectChat = { peer ->
                showNewChatDialog = false
                scope.launch {
                    val convId = repository.startDirectConversation(peer)
                    onOpenConversation(convId, peer.displayName)
                }
            },
            onCreateGroupChat = { groupTitle, selectedPeers ->
                showNewChatDialog = false
                scope.launch {
                    val convId = repository.createGroupConversation(groupTitle, selectedPeers.map { it.peerId })
                    onOpenConversation(convId, groupTitle)
                }
            }
        )
    }
}

@Composable
fun NewChatModalDialog(
    discoveredPeers: List<DiscoveredPeer>,
    onDismiss: () -> Unit,
    onStartDirectChat: (DiscoveredPeer) -> Unit,
    onCreateGroupChat: (groupTitle: String, selectedPeers: List<DiscoveredPeer>) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Direct Chat, 1: Group Chat
    var groupName by remember { mutableStateOf("") }
    val selectedPeersForGroup = remember { mutableStateListOf<DiscoveredPeer>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (selectedTab == 0) "New Conversation" else "Create Group Chat",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Direct") },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Group") },
                        icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Direct chat list
                    if (discoveredPeers.isEmpty()) {
                        Text(
                            text = "No Wi-Fi peers discovered yet. Ensure other phones have WiChat open on this same network.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Select a peer to start messaging:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(discoveredPeers) { peer ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onStartDirectChat(peer) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = peer.displayName.take(1).uppercase(),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = peer.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "@${peer.displayName} • ${peer.hostAddress}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Group chat setup
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        placeholder = { Text("e.g. Hackathon Team, Living Room") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select members (${selectedPeersForGroup.size} selected):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (discoveredPeers.isEmpty()) {
                        Text(
                            text = "No Wi-Fi peers discovered to add to group.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.height(160.dp)) {
                            items(discoveredPeers) { peer ->
                                val isSelected = selectedPeersForGroup.contains(peer)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) selectedPeersForGroup.remove(peer)
                                            else selectedPeersForGroup.add(peer)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedPeersForGroup.add(peer)
                                            else selectedPeersForGroup.remove(peer)
                                        }
                                    )
                                    Text(
                                        text = peer.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "@${peer.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = {
                        val name = groupName.trim()
                        if (name.isNotEmpty()) {
                            onCreateGroupChat(name, selectedPeersForGroup.toList())
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = groupName.isNotBlank()
                ) {
                    Text("Create Group", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}


@Composable
fun ConversationRowItem(
    conversation: ConversationItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.nissshh.wichat.core.designsystem.component.WiChatAvatar(
                name = conversation.title,
                avatarUri = null,
                size = 48.dp,
                isGroup = conversation.type == ConversationType.GROUP
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val timeString = timeFormat.format(Date(conversation.lastTimestamp))
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (conversation.unreadCount > 99) "99+" else "${conversation.unreadCount}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete chat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

        }
    }
}
