package br.com.fiap.wtcconnect.network

import br.com.fiap.wtcconnect.data.auth.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private fun createHttpClient(sessionManager: SessionManager, withAuth: Boolean): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (withAuth) {
            builder.addInterceptor(AuthInterceptor(sessionManager))
        }

        return builder.build()
    }

    private fun createRetrofit(sessionManager: SessionManager, withAuth: Boolean): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(createHttpClient(sessionManager, withAuth))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createAuthApi(sessionManager: SessionManager): AuthApi =
        createRetrofit(sessionManager, withAuth = false).create(AuthApi::class.java)

    fun createMessageApi(sessionManager: SessionManager): MessageApi =
        createRetrofit(sessionManager, withAuth = true).create(MessageApi::class.java)

    fun createCustomerApi(sessionManager: SessionManager): CustomerApi =
        createRetrofit(sessionManager, withAuth = true).create(CustomerApi::class.java)

    fun createCampaignApi(sessionManager: SessionManager): CampaignApi =
        createRetrofit(sessionManager, withAuth = true).create(CampaignApi::class.java)

    fun createSegmentApi(sessionManager: SessionManager): SegmentApi =
        createRetrofit(sessionManager, withAuth = true).create(SegmentApi::class.java)
}
