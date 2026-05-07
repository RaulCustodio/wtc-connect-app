package br.com.fiap.wtcconnect.realtime

import br.com.fiap.wtcconnect.data.auth.SessionManager
import br.com.fiap.wtcconnect.network.MessageDto
import br.com.fiap.wtcconnect.network.NetworkConfig
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignalRManager(
    private val sessionManager: SessionManager
) {
    private var connection: HubConnection? = null

    suspend fun start(
        onMessageReceived: (MessageDto) -> Unit,
        onMessageStatusUpdated: (MessageDto) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (connection != null) {
            return@withContext
        }

        val token = sessionManager.getSession()?.token ?: return@withContext

        val hubConnection = HubConnectionBuilder
            .create(NetworkConfig.HUB_URL)
            .withAccessTokenProvider(Single.defer { Single.just(token) })
            .build()

        hubConnection.on("messageReceived", { message ->
            onMessageReceived(message)
        }, MessageDto::class.java)

        hubConnection.on("messageStatusUpdated", { message ->
            onMessageStatusUpdated(message)
        }, MessageDto::class.java)

        hubConnection.start().blockingAwait()
        connection = hubConnection
    }

    suspend fun joinCustomerInbox(customerId: String) = withContext(Dispatchers.IO) {
        connection?.send("JoinCustomerInbox", customerId)
    }

    suspend fun leaveCustomerInbox(customerId: String) = withContext(Dispatchers.IO) {
        connection?.send("LeaveCustomerInbox", customerId)
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        connection?.stop()?.blockingAwait()
        connection = null
    }
}
