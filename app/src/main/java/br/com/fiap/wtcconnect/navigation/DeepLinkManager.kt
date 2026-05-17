package br.com.fiap.wtcconnect.navigation

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatDeepLink(
    val conversationId: String,
    val peerUserId: String
)

object DeepLinkManager {
    private val _pendingChatLink = MutableStateFlow<ChatDeepLink?>(null)
    val pendingChatLink: StateFlow<ChatDeepLink?> = _pendingChatLink.asStateFlow()

    fun update(uri: Uri?) {
        _pendingChatLink.value = uri?.toChatDeepLink()
    }

    fun consume() {
        _pendingChatLink.value = null
    }
}

private fun Uri.toChatDeepLink(): ChatDeepLink? {
    if (scheme != "wtcconnect" || host != "chat") {
        return null
    }

    val conversationId = getQueryParameter("conversationId") ?: lastPathSegment
    val peerUserId = getQueryParameter("peerUserId") ?: conversationId

    if (conversationId.isNullOrBlank() || peerUserId.isNullOrBlank()) {
        return null
    }

    return ChatDeepLink(conversationId = conversationId, peerUserId = peerUserId)
}