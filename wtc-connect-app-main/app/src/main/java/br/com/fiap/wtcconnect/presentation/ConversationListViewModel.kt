package br.com.fiap.wtcconnect.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcconnect.data.ChatRepository
import br.com.fiap.wtcconnect.data.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// UI state para a lista de conversas
data class ConversationListUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val query: String = "",
    val error: String? = null
)

class ConversationListViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationListUiState(isLoading = true))
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    init {
        // Observa o fluxo de conversas e atualiza o state
        viewModelScope.launch {
            try {
                repository.getConversations().collectLatest { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, conversations = list, error = null)
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = t.message ?: "Erro ao carregar")
            }
        }
    }

    @Suppress("unused")
    fun onQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
    }

    // Retorna lista filtrada por query
    @Suppress("unused")
    fun filteredList(): List<Conversation> {
        val q = uiState.value.query.trim().lowercase()
        if (q.isEmpty()) return uiState.value.conversations
        return uiState.value.conversations.filter { it.peerUser.name.lowercase().contains(q) }
    }

    // Chamado quando usuário seleciona conversa; a navegação é realizada na camada de UI.
    @Suppress("unused", "UNUSED_PARAMETER")
    fun onSelectConversation(conversationId: String, peerUserId: String) {
        // No futuro podemos sinalizar analytics / marcar como lida etc.
    }
}

// Factory simples para permitir injeção do repositório fake
@Suppress("unused")
class ConversationListViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationListViewModel(repository) as T
    }
}

