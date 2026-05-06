package br.com.fiap.wtcconnect.data.auth

import br.com.fiap.wtcconnect.network.AuthApi
import br.com.fiap.wtcconnect.network.AuthResponse
import br.com.fiap.wtcconnect.network.LoginRequest
import br.com.fiap.wtcconnect.network.RegisterRequest

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): AuthResponse {
        val response = authApi.login(LoginRequest(email = email, password = password))
        sessionManager.saveSession(response)
        return response
    }

    suspend fun register(email: String, password: String, role: String): AuthResponse {
        val response = authApi.register(
            RegisterRequest(
                email = email,
                password = password,
                role = role
            )
        )
        sessionManager.saveSession(response)
        return response
    }

    fun getCurrentSession(): UserSession? = sessionManager.getSession()

    fun logout() {
        sessionManager.clear()
    }
}
