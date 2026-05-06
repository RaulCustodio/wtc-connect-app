package br.com.fiap.wtcconnect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcconnect.data.repository.ChatMessage
import br.com.fiap.wtcconnect.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val chatId: String, private val repo: ChatRepository = ChatRepository()) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        viewModelScope.launch {
            repo.observeMessages(chatId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun sendMessage(text: String, senderId: String, senderName: String?) {
        viewModelScope.launch {
            try {
                repo.sendMessage(chatId, text, senderId, senderName, null)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage failed", e)
            }
        }
    }

    fun sendInteractiveMessage(text: String, senderId: String, senderName: String?, interactive: Map<String, Any>) {
        viewModelScope.launch {
            try {
                repo.sendMessage(chatId, text, senderId, senderName, interactive)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendInteractiveMessage failed", e)
            }
        }
    }

    class Factory(private val chatId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatId) as T
        }
    }
}
