package br.com.fiap.wtcconnect.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import br.com.fiap.wtcconnect.AppContainer
import br.com.fiap.wtcconnect.data.auth.AuthRepository
import br.com.fiap.wtcconnect.data.auth.UserSession
import br.com.fiap.wtcconnect.network.AuthResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var application: Application
    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        mockkObject(AppContainer)
        every { AppContainer.init(any()) } just runs
        every { AppContainer.provideAuthRepository() } returns authRepository
        every { authRepository.getCurrentSession() } returns null
        authViewModel = AuthViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `restoreSession autentica usuario existente`() = runTest {
        val session = UserSession(
            token = "token-1",
            userId = "user-1",
            email = "cliente@wtc.com",
            role = "Client"
        )
        every { authRepository.getCurrentSession() } returns session

        authViewModel = AuthViewModel(application)

        val state = authViewModel.authState.first()
        assertTrue(state.isAuthenticated)
        assertEquals("user-1", state.userId)
        assertEquals("cliente@wtc.com", state.userEmail)
        assertEquals(UserType.CLIENT, state.userType)
    }

    @Test
    fun `login com campos vazios retorna erro local`() = runTest {
        authViewModel.login("", "password123", false)

        val state = authViewModel.authState.first()
        assertFalse(state.isAuthenticated)
        assertEquals("Por favor, preencha todos os campos", state.errorMessage)
    }

    @Test
    fun `login com sucesso autentica cliente`() = runTest {
        coEvery { authRepository.login("cliente@wtc.com", "123456") } returns AuthResponse(
            token = "token-1",
            userId = "user-1",
            email = "cliente@wtc.com",
            role = "Client"
        )

        authViewModel.login("cliente@wtc.com", "123456", false)
        advanceUntilIdle()

        val state = authViewModel.authState.first()
        assertTrue(state.isAuthenticated)
        assertEquals(UserType.CLIENT, state.userType)
        assertEquals("user-1", state.userId)
        assertEquals("token-1", state.token)
        assertNull(state.errorMessage)
    }

    @Test
    fun `login de operador com perfil client retorna erro`() = runTest {
        coEvery { authRepository.login("cliente@wtc.com", "123456") } returns AuthResponse(
            token = "token-1",
            userId = "user-1",
            email = "cliente@wtc.com",
            role = "Client"
        )
        every { authRepository.logout() } just runs

        authViewModel.login("cliente@wtc.com", "123456", true)
        advanceUntilIdle()

        val state = authViewModel.authState.first()
        assertFalse(state.isAuthenticated)
        assertEquals("Este usuário não possui perfil de operador", state.errorMessage)
        verify { authRepository.logout() }
    }

    @Test
    fun `login com http 401 traduz mensagem da api`() = runTest {
        val errorBody = "{\"message\":\"Credenciais inválidas\"}".toResponseBody("application/json".toMediaType())
        coEvery { authRepository.login("user@wtc.com", "senha") } throws HttpException(
            Response.error<Any>(401, errorBody)
        )

        authViewModel.login("user@wtc.com", "senha", false)
        advanceUntilIdle()

        val state = authViewModel.authState.first()
        assertFalse(state.isAuthenticated)
        assertEquals("Credenciais inválidas", state.errorMessage)
    }

    @Test
    fun `logout limpa estado autenticado`() = runTest {
        every { authRepository.getCurrentSession() } returns UserSession(
            token = "token-1",
            userId = "user-1",
            email = "cliente@wtc.com",
            role = "Client"
        )
        every { authRepository.logout() } just runs
        authViewModel = AuthViewModel(application)

        authViewModel.logout()

        val state = authViewModel.authState.first()
        assertFalse(state.isAuthenticated)
        assertNull(state.userId)
        assertNull(state.userEmail)
        verify { authRepository.logout() }
    }

    @Test
    fun `clearError remove mensagem atual`() = runTest {
        authViewModel.login("", "password", false)
        assertEquals("Por favor, preencha todos os campos", authViewModel.authState.first().errorMessage)

        authViewModel.clearError()

        assertNull(authViewModel.authState.first().errorMessage)
    }

    @Test
    fun `register com campos vazios retorna erro local`() = runTest {
        authViewModel.register("", "123456", "123456", false)

        val state = authViewModel.authState.first()
        assertFalse(state.isAuthenticated)
        assertEquals("Por favor, preencha todos os campos", state.errorMessage)
    }
}

