package br.com.fiap.wtcconnect.data

import kotlinx.coroutines.flow.Flow

// Interface do repositório para permitir implementação fake/in-memory e futura injeção
@Suppress("unused")
interface ChatRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun getMessages(conversationId: String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, content: String, senderId: String): Result<Unit>

    // Novas APIs para grupos
    fun getGroups(): Flow<List<Group>>
    fun getGroupMembers(groupId: String): Flow<List<User>>
    suspend fun addUserToGroupByEmail(groupId: String, userEmail: String): Result<Unit>
    suspend fun removeUserFromGroup(groupId: String, userId: String): Result<Unit>

    // Busca usuários (server-side in real repo) — aqui podemos filtrar por grupo
    fun searchUsers(query: String, withinGroupId: String? = null): Flow<List<User>>

    // retorna groupId associado ao usuário (null se não associado)
    fun getUserGroupId(userId: String): Flow<String?>

    // Recupera um usuário por id (flow reativo)
    fun getUserById(userId: String): Flow<User?>
}
