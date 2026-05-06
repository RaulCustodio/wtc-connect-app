package br.com.fiap.wtcconnect.notifications

sealed class InAppEvent {
    data class NewMessage(val chatId: String?, val messageId: String?, val title: String?, val body: String?) : InAppEvent()
}

