package br.com.fiap.wtcconnect.data.repository

import br.com.fiap.wtcconnect.data.ChatRepository
import br.com.fiap.wtcconnect.data.Conversation
import br.com.fiap.wtcconnect.data.Group
import br.com.fiap.wtcconnect.data.Message
import br.com.fiap.wtcconnect.data.MessageStatus
import br.com.fiap.wtcconnect.data.User
import br.com.fiap.wtcconnect.data.auth.SessionManager
import br.com.fiap.wtcconnect.network.AddGroupMemberRequest
import br.com.fiap.wtcconnect.network.GroupApi
import br.com.fiap.wtcconnect.network.GroupDto
import br.com.fiap.wtcconnect.network.GroupMemberDto
import br.com.fiap.wtcconnect.network.MessageApi
import br.com.fiap.wtcconnect.network.MessageDto
import br.com.fiap.wtcconnect.network.SendGroupMessageRequest
import br.com.fiap.wtcconnect.network.SendMessageRequest
import br.com.fiap.wtcconnect.network.UpdateMessageStatusRequest
import br.com.fiap.wtcconnect.realtime.SignalRManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.Instant

class RemoteChatRepository(
    private val messageApi: MessageApi,
    private val groupApi: GroupApi,
    private val sessionManager: SessionManager,
    private val signalRManager: SignalRManager
) : ChatRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val messageFlows = mutableMapOf<String, MutableStateFlow<List<Message>>>()
    private val joinedCustomerIds = mutableSetOf<String>()
    private val joinedGroupIds = mutableSetOf<String>()
    private var realtimeStarted = false

    override fun getConversations(): Flow<List<Conversation>> {
        ensureRealtime()
        refreshCurrentUserConversation()
        return conversations.asStateFlow()
    }

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        ensureRealtime()
        val flow = messageFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }

        scope.launch {
            runCatching {
                val messages = if (conversationId.startsWith("group_")) {
                    val groupId = conversationId.removePrefix("group_")
                    joinGroup(groupId)
                    groupApi.getGroupMessages(groupId)
                } else {
                    joinInbox(conversationId)
                    messageApi.getInbox(conversationId)
                }
                    .map { it.toDomain() }
                    .sortedBy { it.createdAt }

                flow.value = messages
                upsertConversation(conversationId, messages)

                if (!conversationId.startsWith("group_")) {
                    markIncomingMessagesAsRead(messages)
                }
            }
        }

        return flow.asStateFlow()
    }

    override suspend fun sendMessage(conversationId: String, content: String, senderId: String): Result<Unit> {
        return runCatching {
            val message = if (conversationId.startsWith("group_")) {
                val groupId = conversationId.removePrefix("group_")
                groupApi.sendGroupMessage(groupId, SendGroupMessageRequest(content = content))
            } else {
                messageApi.sendMessage(
                    SendMessageRequest(
                        customerId = conversationId,
                        content = content
                    )
                )
            }.toDomain()

            upsertMessage(message)
        }
    }

    override fun getGroups(): Flow<List<Group>> {
        val flow = MutableStateFlow<List<Group>>(emptyList())
        scope.launch {
            runCatching {
                groupApi.getGroups().map { it.toDomain() }
            }.onSuccess {
                flow.value = it
            }
        }
        return flow.asStateFlow()
    }

    override fun getGroupMembers(groupId: String): Flow<List<User>> {
        val flow = MutableStateFlow<List<User>>(emptyList())
        scope.launch {
            runCatching {
                groupApi.getGroupMembers(groupId).map { it.toDomainUser() }
            }.onSuccess {
                flow.value = it
            }
        }
        return flow.asStateFlow()
    }

    override suspend fun addUserToGroupByEmail(groupId: String, userEmail: String): Result<Unit> {
        return runCatching {
            groupApi.addGroupMember(groupId, AddGroupMemberRequest(email = userEmail))
            Unit
        }
    }

    override suspend fun removeUserFromGroup(groupId: String, userId: String): Result<Unit> {
        return runCatching {
            groupApi.removeGroupMember(groupId, userId)
            Unit
        }
    }

    override fun searchUsers(query: String, withinGroupId: String?): Flow<List<User>> {
        val flow = MutableStateFlow<List<User>>(emptyList())
        scope.launch {
            runCatching {
                groupApi.searchUsers(query, withinGroupId).map { it.toDomainUser() }
            }.onSuccess {
                flow.value = it
            }
        }
        return flow.asStateFlow()
    }

    override fun getUserGroupId(userId: String): Flow<String?> {
        return flow {
            emit(
                runCatching {
                    groupApi.getUserGroup(userId).groupId
                }.getOrNull()
            )
        }
    }

    override fun getUserById(userId: String): Flow<User?> {
        val flow = MutableStateFlow<User?>(null)
        scope.launch {
            val session = sessionManager.getSession()
            if (session != null && userId == session.userId) {
                flow.value = User(id = session.userId, name = session.email.substringBefore('@'), email = session.email)
                return@launch
            }

            runCatching {
                groupApi.getUser(userId).toDomainUser()
            }.onSuccess {
                flow.value = it
            }.onFailure {
                flow.value = User(id = userId, name = "WTC Connect", email = null)
            }
        }
        return flow.asStateFlow()
    }

    private fun refreshCurrentUserConversation() {
        val session = sessionManager.getSession() ?: return
        scope.launch {
            runCatching {
                val messages = messageApi.getInbox(session.userId)
                    .map { it.toDomain() }
                    .sortedBy { it.createdAt }
                messageFlows.getOrPut(session.userId) { MutableStateFlow(emptyList()) }.value = messages
                upsertConversation(session.userId, messages)
            }
        }
    }

    private fun ensureRealtime() {
        if (realtimeStarted) {
            return
        }

        realtimeStarted = true
        scope.launch {
            signalRManager.start(
                onMessageReceived = { messageDto -> upsertMessage(messageDto.toDomain()) },
                onMessageStatusUpdated = { messageDto -> upsertMessage(messageDto.toDomain()) }
            )
        }
    }

    private suspend fun joinInbox(customerId: String) {
        if (joinedCustomerIds.add(customerId)) {
            signalRManager.joinCustomerInbox(customerId)
        }
    }

    private suspend fun joinGroup(groupId: String) {
        if (joinedGroupIds.add(groupId)) {
            signalRManager.joinGroupChat(groupId)
        }
    }

    private fun upsertMessage(message: Message) {
        val flow = messageFlows.getOrPut(message.customerId) { MutableStateFlow(emptyList()) }
        val updated = flow.value
            .filterNot { it.id == message.id }
            .plus(message)
            .sortedBy { it.createdAt }
        flow.value = updated
        upsertConversation(message.customerId, updated)
    }

    private fun upsertConversation(customerId: String, messages: List<Message>) {
        val lastMessage = messages.lastOrNull()
        val conversation = Conversation(
            id = customerId,
            peerUser = buildPeerUser(customerId, lastMessage),
            lastMessage = lastMessage?.content.orEmpty(),
            lastTimestamp = lastMessage?.createdAt ?: System.currentTimeMillis(),
            unreadCount = messages.count {
                it.senderId != sessionManager.getSession()?.userId && it.status != MessageStatus.Read
            }
        )

        conversations.value = conversations.value
            .filterNot { it.id == customerId }
            .plus(conversation)
            .sortedByDescending { it.lastTimestamp }
    }

    private fun buildPeerUser(customerId: String, lastMessage: Message?): User {
        val session = sessionManager.getSession()
        val isCurrentUserConversation = session?.userId == customerId
        val name = when {
            customerId.startsWith("group_") -> "Grupo"
            isCurrentUserConversation -> "WTC Connect"
            lastMessage?.senderRole == "Operator" -> "Operador"
            else -> "Cliente $customerId"
        }

        return User(
            id = customerId,
            name = name,
            email = if (isCurrentUserConversation) session?.email else null
        )
    }

    private fun markIncomingMessagesAsRead(messages: List<Message>) {
        val currentUserId = sessionManager.getSession()?.userId ?: return

        messages.filter {
            it.id.isNotBlank() && it.senderId != currentUserId && it.status != MessageStatus.Read
        }.forEach { message ->
            scope.launch {
                runCatching {
                    messageApi.updateMessageStatus(
                        id = message.id,
                        request = UpdateMessageStatusRequest(MessageStatus.Read)
                    )
                }
            }
        }
    }
}

private fun GroupDto.toDomain(): Group {
    return Group(
        id = id.orEmpty(),
        name = name
    )
}

private fun GroupMemberDto.toDomainUser(): User {
    return User(
        id = userId,
        name = name.ifBlank { email?.substringBefore('@') ?: userId },
        email = email
    )
}

private fun MessageDto.toDomain(): Message {
    return Message(
        id = id.orEmpty(),
        customerId = customerId,
        senderId = senderId,
        senderRole = senderRole,
        content = content,
        status = status,
        campaignId = campaignId,
        createdAt = createdAt.toEpochMillis(),
        deliveredAt = deliveredAt?.toEpochMillis(),
        readAt = readAt?.toEpochMillis()
    )
}

private fun String.toEpochMillis(): Long {
    return Instant.parse(this).toEpochMilli()
}
