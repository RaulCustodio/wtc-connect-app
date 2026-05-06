package br.com.fiap.wtcconnect.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

// Implementação em memória simples para desenvolvimento e testes manuais.
class FakeChatRepository(private val currentUserId: String = "me", private val currentUserEmail: String? = null) : ChatRepository {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _messages = mutableMapOf<String, MutableStateFlow<List<Message>>>()

    // Grupos e usuários
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    private val _users = MutableStateFlow<List<User>>(emptyList())

    // Associação usuário -> groupId (um usuário por vez neste modelo simples)
    private val userGroupMap = mutableMapOf<String, String?>()

    init {
        // Preenche com alguns dados fake
        val userMe = User(id = "me", name = "Eu (local)", email = currentUserEmail, avatarUrl = null)
        val user1 = User(id = "u1", name = "Leonardo", email = "leo@example.com", avatarUrl = null)
        val user2 = User(id = "u2", name = "Raul", email = "raul@example.com", avatarUrl = null)
        val user3 = User(id = "u3", name = "Cynthia", email = "cynthia@example.com", avatarUrl = null)
        val user4 = User(id = "u4", name = "Ana", email = "ana@example.com", avatarUrl = null)

        // Grupos: criamos um grupo default que conecta todos os usuários
        val groupDefault = Group(id = "g0", name = "WTC Connect")
        val groupA = Group(id = "g1", name = "Comercial")
        val groupB = Group(id = "g2", name = "Eventos")

        _groups.value = listOf(groupDefault, groupA, groupB)

        // Cria lista inicial de usuários. Se currentUserId não estiver entre os seeds,
        // adicionamos esse usuário usando currentUserEmail (se houver) para que ele
        // pertença ao grupo padrão e apareça na lista de membros.
        val initialUsers = mutableListOf(userMe, user1, user2, user3, user4)
        if (currentUserId != null && initialUsers.none { it.id == currentUserId }) {
            val autoName = currentUserEmail ?: "Usuário"
            initialUsers.add(User(id = currentUserId, name = autoName, email = currentUserEmail))
        }
        _users.value = initialUsers

        // Atribuições iniciais: todos os usuários seed pertencem ao grupo WTC Connect
        userGroupMap[userMe.id] = groupDefault.id
        userGroupMap[user1.id] = groupDefault.id
        userGroupMap[user2.id] = groupDefault.id
        userGroupMap[user3.id] = groupDefault.id
        userGroupMap[user4.id] = groupDefault.id
        // Garantir que o currentUserId (se diferente) também esteja associado ao grupo default
        if (currentUserId != null) {
            userGroupMap[currentUserId] = groupDefault.id
        }

        // Conversas 1:1 apenas entre membros do mesmo grupo (apenas para demo)
        val conv1 = Conversation(
            id = "c1",
            peerUser = user1,
            lastMessage = "Podemos conversar?",
            lastTimestamp = System.currentTimeMillis() - 1000L * 60 * 60,
            unreadCount = 1
        )

        val conv2 = Conversation(
            id = "c2",
            peerUser = user2,
            lastMessage = "Preciso da sua ajuda",
            lastTimestamp = System.currentTimeMillis() - 1000L * 60 * 30,
            unreadCount = 0
        )

        val conv3 = Conversation(
            id = "c3",
            peerUser = user3,
            lastMessage = "Reunião amanhã às 11:00",
            lastTimestamp = System.currentTimeMillis() - 1000L * 60 * 5,
            unreadCount = 2
        )

        // Somente conversas 1:1; o usuário local verá apenas conversas com membros do seu grupo
        // Além disso criamos uma conversa de grupo para o grupo default (group_g0)
        val groupConv = Conversation(
            id = "group_${groupDefault.id}",
            peerUser = User(id = "group_${groupDefault.id}", name = groupDefault.name, email = null),
            lastMessage = "Bem-vindo ao grupo ${groupDefault.name}",
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0
        )

        _conversations.value = listOf(conv1, conv2, conv3, groupConv)

        _messages[conv1.id] = MutableStateFlow(
            listOf(
                Message(id = "m1", customerId = conv1.id, senderId = user1.id, senderRole = "Client", content = "Oi", status = MessageStatus.Delivered, createdAt = System.currentTimeMillis() - 1000L * 60 * 60, deliveredAt = System.currentTimeMillis() - 1000L * 60 * 59),
                Message(id = "m2", customerId = conv1.id, senderId = currentUserId, senderRole = "Client", content = "Tudo bem?", status = MessageStatus.Delivered, createdAt = System.currentTimeMillis() - 1000L * 60 * 50, deliveredAt = System.currentTimeMillis() - 1000L * 60 * 49)
            )
        )

        _messages[conv2.id] = MutableStateFlow(
            listOf(
                Message(id = "m3", customerId = conv2.id, senderId = user2.id, senderRole = "Client", content = "Olá, pode me ajudar?", status = MessageStatus.Delivered, createdAt = System.currentTimeMillis() - 1000L * 60 * 30, deliveredAt = System.currentTimeMillis() - 1000L * 60 * 29)
            )
        )

        _messages[conv3.id] = MutableStateFlow(
            listOf(
                Message(id = "m4", customerId = conv3.id, senderId = user3.id, senderRole = "Client", content = "Agenda enviada", status = MessageStatus.Delivered, createdAt = System.currentTimeMillis() - 1000L * 60 * 10, deliveredAt = System.currentTimeMillis() - 1000L * 60 * 9)
            )
        )

        // Mensagens iniciais do chat de grupo
        _messages[groupConv.id] = MutableStateFlow(
            listOf(
                Message(id = "gm1", customerId = groupConv.id, senderId = user1.id, senderRole = "Client", content = "Bem-vindos ao WTC Connect!", status = MessageStatus.Delivered, createdAt = System.currentTimeMillis() - 1000L * 60 * 60, deliveredAt = System.currentTimeMillis() - 1000L * 60 * 59),
                Message(id = "gm2", customerId = groupConv.id, senderId = user3.id, senderRole = "Client", content = "Olá a todos!", status = MessageStatus.Delivered, createdAt = System.currentTimeMillis() - 1000L * 60 * 30, deliveredAt = System.currentTimeMillis() - 1000L * 60 * 29)
            )
        )
    }

    override fun getConversations(): kotlinx.coroutines.flow.Flow<List<Conversation>> = _conversations.asStateFlow()

    override fun getMessages(conversationId: String): kotlinx.coroutines.flow.Flow<List<Message>> {
        val flow = _messages.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        return flow
    }

    override suspend fun sendMessage(conversationId: String, content: String, senderId: String): Result<Unit> {
        // Simula atraso de rede e adiciona a mensagem localmente
        val now = System.currentTimeMillis()
        val msg = Message(
            id = UUID.randomUUID().toString(),
            customerId = conversationId,
            senderId = senderId,
            senderRole = "Client",
            content = content,
            status = MessageStatus.Delivered,
            createdAt = now,
            deliveredAt = now
        )

        val flow = _messages.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        val newList = flow.value + msg
        flow.value = newList

        // atualiza conversation lastMessage
        // Para chats de grupo, se a conversa não existir na lista, não precisa criar uma entrada
        // pois grupos têm sua própria UI separada
        val updated = _conversations.value.map { conv ->
            if (conv.id == conversationId) conv.copy(lastMessage = content, lastTimestamp = now, unreadCount = 0)
            else conv
        }

        // Se não encontrou a conversa (ex: chat de grupo novo), adiciona ela
        if (updated.none { it.id == conversationId }) {
            // Para grupos: cria uma entrada com um User placeholder do grupo
            if (conversationId.startsWith("group_")) {
                val groupId = conversationId.removePrefix("group_")
                val group = _groups.value.find { it.id == groupId }
                if (group != null) {
                    // Cria um User placeholder para representar o grupo
                    val groupUser = User(id = "group:$groupId", name = group.name, email = null, avatarUrl = null)
                    val groupConversation = Conversation(
                        id = conversationId,
                        peerUser = groupUser,
                        lastMessage = content,
                        lastTimestamp = now,
                        unreadCount = 0
                    )
                    _conversations.value = _conversations.value + groupConversation
                }
            }
        } else {
            _conversations.value = updated
        }

        return Result.success(Unit)
    }

    // Grupos
    override fun getGroups(): kotlinx.coroutines.flow.Flow<List<Group>> = _groups.asStateFlow()

    override fun getGroupMembers(groupId: String): kotlinx.coroutines.flow.Flow<List<User>> {
        val members = _users.value.filter { userGroupMap[it.id] == groupId }
        return MutableStateFlow(members).asStateFlow()
    }

    override suspend fun addUserToGroupByEmail(groupId: String, userEmail: String): Result<Unit> {
        // Se usuário existir, associa ao grupo. Caso contrário, cria um usuário simples e associa.
        val existing = _users.value.find { it.email?.equals(userEmail, ignoreCase = true) == true }
        return if (existing != null) {
            userGroupMap[existing.id] = groupId
            Result.success(Unit)
        } else {
            // cria usuário automático
            val newId = UUID.randomUUID().toString()
            val newName = userEmail.substringBefore("@").replace('.', ' ').capitalize()
            val newUser = User(id = newId, name = newName, email = userEmail, avatarUrl = null)
            _users.value = _users.value + newUser
            userGroupMap[newUser.id] = groupId
            Result.success(Unit)
        }
    }

    override suspend fun removeUserFromGroup(groupId: String, userId: String): Result<Unit> {
        val current = userGroupMap[userId]
        if (current != groupId) return Result.failure(Exception("Usuário não pertence a este grupo"))
        userGroupMap[userId] = null
        return Result.success(Unit)
    }

    override fun searchUsers(query: String, withinGroupId: String?): kotlinx.coroutines.flow.Flow<List<User>> {
        val q = query.trim()
        val filtered = _users.value.filter { user ->
            val matchesQuery = q.isEmpty() || user.name.contains(q, ignoreCase = true) || (user.email?.contains(q, ignoreCase = true) ?: false)
            val inGroup = withinGroupId?.let { userGroupMap[user.id] == it } ?: true
            matchesQuery && inGroup
        }
        return MutableStateFlow(filtered).asStateFlow()
    }

    override fun getUserGroupId(userId: String): kotlinx.coroutines.flow.Flow<String?> {
        // Retorna o grupo do usuário, ou o grupo padrão "g0" se não tiver grupo atribuído
        val groupId = userGroupMap[userId] ?: "g0"
        return MutableStateFlow(groupId).asStateFlow()
    }

    override fun getUserById(userId: String): kotlinx.coroutines.flow.Flow<User?> {
        val user = _users.value.find { it.id == userId }
        return MutableStateFlow(user).asStateFlow()
    }
}
