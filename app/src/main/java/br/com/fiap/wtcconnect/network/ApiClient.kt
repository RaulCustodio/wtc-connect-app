package br.com.fiap.wtcconnect.network

import br.com.fiap.wtcconnect.data.auth.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private fun createHttpClient(sessionManager: SessionManager, withAuth: Boolean): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)

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

    fun createAuthApi(sessionManager: SessionManager): AuthApi {
        return createRetrofit(sessionManager, withAuth = false).create(AuthApi::class.java)
    }

    fun createMessageApi(sessionManager: SessionManager): MessageApi {
        return createRetrofit(sessionManager, withAuth = true).create(MessageApi::class.java)
    }
}
