package br.com.fiap.wtcconnect

import android.content.Context
import br.com.fiap.wtcconnect.data.auth.AuthRepository
import br.com.fiap.wtcconnect.data.auth.SessionManager
import br.com.fiap.wtcconnect.data.repository.CampaignRepository
import br.com.fiap.wtcconnect.data.repository.CustomerRepository
import br.com.fiap.wtcconnect.data.repository.RemoteChatRepository
import br.com.fiap.wtcconnect.network.ApiClient
import br.com.fiap.wtcconnect.realtime.SignalRManager

object AppContainer {
    private var initialized = false

    lateinit var sessionManager: SessionManager
        private set

    fun init(context: Context) {
        if (initialized) {
            return
        }

        sessionManager = SessionManager(context.applicationContext)
        initialized = true
    }

    fun provideAuthRepository(): AuthRepository {
        check(initialized) { "AppContainer.init must be called before use." }
        return AuthRepository(
            authApi = ApiClient.createAuthApi(sessionManager),
            sessionManager = sessionManager
        )
    }

    fun provideChatRepository(): RemoteChatRepository {
        check(initialized) { "AppContainer.init must be called before use." }
        return RemoteChatRepository(
            messageApi = ApiClient.createMessageApi(sessionManager),
            groupApi = ApiClient.createGroupApi(sessionManager),
            sessionManager = sessionManager,
            signalRManager = SignalRManager(sessionManager)
        )
    }

    fun provideCampaignRepository(): CampaignRepository {
        check(initialized) { "AppContainer.init must be called before use." }
        return CampaignRepository(
            campaignApi = ApiClient.createCampaignApi(sessionManager)
        )
    }

    fun provideCustomerRepository(): CustomerRepository {
        check(initialized) { "AppContainer.init must be called before use." }
        return CustomerRepository(
            customerApi = ApiClient.createCustomerApi(sessionManager)
        )
    }
}
