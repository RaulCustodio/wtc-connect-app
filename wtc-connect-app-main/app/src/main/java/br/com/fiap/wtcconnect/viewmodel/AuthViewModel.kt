package br.com.fiap.wtcconnect.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcconnect.AppContainer
import br.com.fiap.wtcconnect.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userType: UserType = UserType.CLIENT,
    val userId: String? = null,
    val userEmail: String? = null,
    val token: String? = null,
    val errorMessage: String? = null
)

enum class UserType {
    OPERATOR, CLIENT
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository: AuthRepository

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        AppContainer.init(application)
        authRepository = AppContainer.provideAuthRepository()
        restoreSession()
    }

    private fun restoreSession() {
        val session = authRepository.getCurrentSession() ?: return
        _authState.value = AuthState(
            isAuthenticated = true,
            userType = session.role.toUserType(),
            userId = session.userId,
            userEmail = session.email,
            token = session.token
        )
    }

    fun login(email: String, password: String, isOperator: Boolean) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Por favor, preencha todos os campos")
            return
        }

        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                authRepository.login(email = email.trim(), password = password)
            }.onSuccess { response ->
                val userType = response.role.toUserType()
                if (isOperator && userType != UserType.OPERATOR) {
                    authRepository.logout()
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = "Este usuário não possui perfil de operador"
                    )
                    return@onSuccess
                }

                _authState.value = AuthState(
                    isLoading = false,
                    isAuthenticated = true,
                    userType = userType,
                    userId = response.userId,
                    userEmail = response.email,
                    token = response.token
                )
            }.onFailure { throwable ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Erro ao fazer login"
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState()
    }

    fun register(email: String, password: String, confirmPassword: String, isOperator: Boolean) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Por favor, preencha todos os campos")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = _authState.value.copy(errorMessage = "Formato de e-mail inválido")
            return
        }

        if (password.length < 6) {
            _authState.value = _authState.value.copy(errorMessage = "A senha deve ter pelo menos 6 caracteres")
            return
        }

        if (password != confirmPassword) {
            _authState.value = _authState.value.copy(errorMessage = "As senhas não coincidem")
            return
        }

        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                authRepository.register(
                    email = email.trim(),
                    password = password,
                    role = if (isOperator) "Operator" else "Client"
                )
            }.onSuccess { response ->
                _authState.value = AuthState(
                    isLoading = false,
                    isAuthenticated = true,
                    userType = response.role.toUserType(),
                    userId = response.userId,
                    userEmail = response.email,
                    token = response.token
                )
            }.onFailure { throwable ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Erro ao criar conta"
                )
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
}

private fun String.toUserType(): UserType {
    return if (equals("Operator", ignoreCase = true)) UserType.OPERATOR else UserType.CLIENT
}
