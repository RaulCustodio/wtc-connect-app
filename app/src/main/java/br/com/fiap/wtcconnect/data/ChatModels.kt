package br.com.fiap.wtcconnect.data

// Modelos de domínio simples usados pelo módulo de chat

data class User(
    val id: String,
    val name: String,
    val email: String? = null,
    val avatarUrl: String? = null
)

@Suppress("unused")
data class Conversation(
    val id: String,
    val peerUser: User,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int = 0
)

@Suppress("unused")
data class Message(
    val id: String,
    val customerId: String,
    val senderId: String,
    val senderRole: String = "Client",
    val content: String,
    val status: MessageStatus = MessageStatus.Sent,
    val campaignId: String? = null,
    val createdAt: Long,
    val deliveredAt: Long? = null,
    val readAt: Long? = null
)

enum class MessageStatus {
    Sent,
    Delivered,
    Read
}

// Grupo simples que contém id e nome; usuários são associados por id em FakeChatRepository
data class Group(
    val id: String,
    val name: String
)
