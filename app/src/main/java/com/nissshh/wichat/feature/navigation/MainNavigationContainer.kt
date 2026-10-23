package com.nissshh.wichat.feature.navigation

import androidx.activity.compose.BackHandler

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nissshh.wichat.core.datastore.PreferencesRepository
import com.nissshh.wichat.core.designsystem.theme.MotionTokens
import com.nissshh.wichat.core.model.RequestStatus
import com.nissshh.wichat.core.repository.WiChatRepository
import com.nissshh.wichat.feature.chats.ChatsScreen
import com.nissshh.wichat.feature.chats.OneToOneChatScreen
import com.nissshh.wichat.feature.devices.DevicesScreen
import com.nissshh.wichat.feature.diagnostics.NetworkDiagnosticsScreen
import com.nissshh.wichat.feature.profile.ProfileSettingsScreen
import com.nissshh.wichat.feature.requests.RequestsScreen
import com.nissshh.wichat.feature.stories.StoriesScreen

enum class MainDestination(val label: String, val icon: ImageVector) {
    CHATS("Chats", Icons.Default.ChatBubbleOutline),
    REQUESTS("Requests", Icons.Default.MarkEmailRead),
    DEVICES("Devices", Icons.Default.Wifi),
    STORIES("Stories", Icons.Outlined.AutoAwesome),
    YOU("You", Icons.Default.Person)
}

@Composable
fun MainNavigationContainer(
    repository: WiChatRepository,
    preferencesRepository: PreferencesRepository
) {
    var currentDestination by remember { mutableStateOf(MainDestination.CHATS) }
    var activeConversationId by remember { mutableStateOf<String?>(null) }
    var activeConversationTitle by remember { mutableStateOf("") }
    var showDiagnostics by remember { mutableStateOf(false) }

    val requests by repository.getAllRequests().collectAsState(initial = emptyList())
    val pendingCount = requests.count { it.status == RequestStatus.PENDING }

    val conversations by repository.getAllConversations().collectAsState(initial = emptyList())
    val unreadMessagesCount = conversations.sumOf { it.unreadCount }

    // Intercept hardware/system back gesture:
    // 1. If in diagnostics, close diagnostics
    // 2. If in a chat thread, close chat thread
    // 3. If on any tab other than CHATS, switch back to CHATS instead of exiting the app!
    BackHandler(enabled = activeConversationId != null || showDiagnostics || currentDestination != MainDestination.CHATS) {
        if (showDiagnostics) {
            showDiagnostics = false
        } else if (activeConversationId != null) {
            activeConversationId = null
        } else if (currentDestination != MainDestination.CHATS) {
            currentDestination = MainDestination.CHATS
        }
    }

    if (showDiagnostics) {
        NetworkDiagnosticsScreen(
            repository = repository,
            onBackPress = { showDiagnostics = false }
        )
    } else if (activeConversationId != null) {
        OneToOneChatScreen(
            conversationId = activeConversationId!!,
            title = activeConversationTitle,
            repository = repository,
            onBackPress = { activeConversationId = null }
        )
    } else {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    MainDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                if (destination == MainDestination.CHATS && unreadMessagesCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(if (unreadMessagesCount > 99) "99+" else "$unreadMessagesCount")
                                            }
                                        }
                                    ) {
                                        Icon(destination.icon, contentDescription = destination.label)
                                    }
                                } else if (destination == MainDestination.REQUESTS && pendingCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text("$pendingCount")
                                            }
                                        }
                                    ) {
                                        Icon(destination.icon, contentDescription = destination.label)
                                    }
                                } else {
                                    Icon(destination.icon, contentDescription = destination.label)
                                }
                            },

                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = {
                        val duration = MotionTokens.DurationScreen
                        if (targetState.ordinal > initialState.ordinal) {
                            // Slide left when switching to a tab further to the right
                            (slideInHorizontally(animationSpec = tween(duration)) { width -> width } +
                                    fadeIn(animationSpec = tween(duration))).togetherWith(
                                slideOutHorizontally(animationSpec = tween(duration)) { width -> -width } +
                                        fadeOut(animationSpec = tween(duration))
                            )
                        } else {
                            // Slide right when switching to a tab to the left
                            (slideInHorizontally(animationSpec = tween(duration)) { width -> -width } +
                                    fadeIn(animationSpec = tween(duration))).togetherWith(
                                slideOutHorizontally(animationSpec = tween(duration)) { width -> width } +
                                        fadeOut(animationSpec = tween(duration))
                            )
                        }
                    },
                    label = "tab_switch"
                ) { target ->

                    when (target) {
                        MainDestination.CHATS -> ChatsScreen(
                            repository = repository,
                            onOpenConversation = { id, title ->
                                activeConversationId = id
                                activeConversationTitle = title
                            }
                        )
                        MainDestination.REQUESTS -> RequestsScreen(
                            repository = repository,
                            onOpenConversation = { id, title ->
                                activeConversationId = id
                                activeConversationTitle = title
                            }
                        )
                        MainDestination.DEVICES -> DevicesScreen(
                            repository = repository,
                            onOpenDiagnostics = { showDiagnostics = true },
                            onOpenConversation = { id, title ->
                                activeConversationId = id
                                activeConversationTitle = title
                            }
                        )
                        MainDestination.STORIES -> StoriesScreen(repository = repository)
                        MainDestination.YOU -> ProfileSettingsScreen(
                            repository = repository,
                            preferencesRepository = preferencesRepository,
                            onOpenDiagnostics = { showDiagnostics = true }
                        )
                    }
                }
            }
        }
    }
}
