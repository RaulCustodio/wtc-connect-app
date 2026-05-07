package br.com.fiap.wtcconnect.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcconnect.data.ChatRepository
import br.com.fiap.wtcconnect.data.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// UI state para a tela de chat
data class ChatUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val error: String? = null
)

class ChatViewModel(private val repository: ChatRepository, private val conversationId: String, private val currentUserId: String = "me") : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.getMessages(conversationId).collectLatest { msgs ->
                    _uiState.value = ChatUiState(isLoading = false, messages = msgs, error = null)
                }
            } catch (t: Throwable) {
                _uiState.value = ChatUiState(isLoading = false, messages = emptyList(), error = t.message)
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.sendMessage(conversationId, content, currentUserId)
            if (result.isSuccess) {
                // o fluxo de mensagens já será atualizado pelo repositório
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}

class ChatViewModelFactory(private val repository: ChatRepository, private val conversationId: String, private val currentUserId: String = "me") : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(repository, conversationId, currentUserId) as T
    }
}

