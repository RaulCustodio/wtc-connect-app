package br.com.fiap.wtcconnect.data.auth

import android.content.Context
import br.com.fiap.wtcconnect.network.AuthResponse

data class UserSession(
    val token: String,
    val userId: String,
    val email: String,
    val role: String
)

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("wtc_session", Context.MODE_PRIVATE)

    fun saveSession(response: AuthResponse) {
        preferences.edit()
            .putString(KEY_TOKEN, response.token)
            .putString(KEY_USER_ID, response.userId)
            .putString(KEY_EMAIL, response.email)
            .putString(KEY_ROLE, response.role)
            .apply()
    }

    fun getSession(): UserSession? {
        val token = preferences.getString(KEY_TOKEN, null) ?: return null
        val userId = preferences.getString(KEY_USER_ID, null) ?: return null
        val email = preferences.getString(KEY_EMAIL, null) ?: return null
        val role = preferences.getString(KEY_ROLE, null) ?: return null

        return UserSession(token = token, userId = userId, email = email, role = role)
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }
}
