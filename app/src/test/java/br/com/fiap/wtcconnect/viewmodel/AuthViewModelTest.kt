package br.com.fiap.wtcconnect.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Testes Unitários para AuthViewModel
 *
 * Este arquivo demonstra como testar a lógica de autenticação
 *
 * Para usar, adicione ao build.gradle.kts:
 * testImplementation("junit:junit:4.13.2")
 * testImplementation("androidx.arch.core:core-testing:2.1.0")
 * testImplementation("io.mockk:mockk:1.13.7")
 * testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
 */

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockFirebaseAuth = mockk(relaxed = true)
        mockFirebaseUser = mockk(relaxed = true)

        // Mock de FirebaseAuth.getInstance()
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `testCheckCurrentUser_UserLoggedIn_ReturnsAuthenticatedState`() = runTest {
        // Arrange
        every { mockFirebaseUser.uid } returns "test_uid"
        every { mockFirebaseUser.email } returns "test@email.com"
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // Act
        authViewModel = AuthViewModel()

        // Assert
        val state = authViewModel.authState.first()
        assert(state.isAuthenticated)
        assert(state.userId == "test_uid")
        assert(state.userEmail == "test@email.com")
    }

    @Test
    fun `testCheckCurrentUser_UserNotLoggedIn_ReturnsUnauthenticatedState`() = runTest {
        // Arrange
        every { mockFirebaseAuth.currentUser } returns null

        // Act
        authViewModel = AuthViewModel()

        // Assert
        val state = authViewModel.authState.first()
        assert(!state.isAuthenticated)
        assert(state.userId == null)
        assert(state.userEmail == null)
    }

    @Test
    fun `testLogin_EmptyEmail_ReturnsError`() = runTest {
        // Arrange
        authViewModel = AuthViewModel()

        // Act
        authViewModel.login("", "password123", false)

        // Assert
        val state = authViewModel.authState.first()
        assert(!state.isAuthenticated)
        assert(state.errorMessage == "Por favor, preencha todos os campos")
    }

    @Test
    fun `testLogin_EmptyPassword_ReturnsError`() = runTest {
        // Arrange
        authViewModel = AuthViewModel()

        // Act
        authViewModel.login("test@email.com", "", false)

        // Assert
        val state = authViewModel.authState.first()
        assert(!state.isAuthenticated)
        assert(state.errorMessage == "Por favor, preencha todos os campos")
    }

    @Test
    fun `testLogin_InvalidEmail_ReturnsError`() = runTest {
        // Arrange
        authViewModel = AuthViewModel()
        val exception = FirebaseAuthInvalidCredentialsException(
            "The email address is badly formatted.",
            Throwable()
        )
        val task = mockk<com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult>>()
        every { task.isSuccessful } returns false
        every { task.exception } returns exception
        every { task.addOnCompleteListener(any()) } answers {
            val callback = firstArg<com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>>()
            callback.onComplete(task)
            task
        }
        every { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } returns task

        // Act
        authViewModel.login("invalidemail", "password123", false)
        advanceUntilIdle()

        // Assert
        val state = authViewModel.authState.first()
        assert(!state.isAuthenticated)
        assert(state.errorMessage?.contains("Formato de e-mail inválido") == true)
    }

    @Test
    fun `testLogin_UserNotFound_ReturnsError`() = runTest {
        // Arrange
        authViewModel = AuthViewModel()
        val exception = FirebaseAuthInvalidUserException("There is no user record", Throwable())
        val task = mockk<com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult>>()
        every { task.isSuccessful } returns false
        every { task.exception } returns exception
        every { task.addOnCompleteListener(any()) } answers {
            val callback = firstArg<com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>>()
            callback.onComplete(task)
            task
        }
        every { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } returns task

        // Act
        authViewModel.login("notfound@email.com", "password123", false)
        advanceUntilIdle()

        // Assert
        val state = authViewModel.authState.first()
        assert(!state.isAuthenticated)
        assert(state.errorMessage?.contains("não encontrado") == true)
    }

    @Test
    fun `testLogout_ClearsAuthState`() = runTest {
        // Arrange
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        authViewModel = AuthViewModel()

        var initialState = authViewModel.authState.first()
        every { mockFirebaseAuth.signOut() } just Runs
        every { mockFirebaseAuth.currentUser } returns null

        // Act
        authViewModel.logout()

        // Assert
        verify { mockFirebaseAuth.signOut() }
        val finalState = authViewModel.authState.first()
        assert(!finalState.isAuthenticated)
        assert(finalState.userId == null)
        assert(finalState.userEmail == null)
    }

    @Test
    fun `testClearError_RemovesErrorMessage`() = runTest {
        // Arrange
        authViewModel = AuthViewModel()
        // Simular um erro
        authViewModel.login("", "password", false)

        var state = authViewModel.authState.first()
        assert(state.errorMessage != null)

        // Act
        authViewModel.clearError()

        // Assert
        state = authViewModel.authState.first()
        assert(state.errorMessage == null)
    }

    @Test
    fun `testLogin_ClientType_SetsUserTypeToClient`() = runTest {
        // Arrange
        authViewModel = AuthViewModel()
        every { mockFirebaseUser.uid } returns "test_uid"
        every { mockFirebaseUser.email } returns "client@email.com"
        val authResult = mockk<com.google.firebase.auth.AuthResult>()
        every { authResult.user } returns mockFirebaseUser
        val task = mockk<com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult>>()
        every { task.isSuccessful } returns true
        every { task.result } returns authResult
        every { task.addOnCompleteListener(any()) } answers {
            val callback = firstArg<com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>>()
            callback.onComplete(task)
            task
        }
        every { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } returns task
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // Act
        authViewModel.login("client@email.com", "password123", false)
        advanceUntilIdle()

        // Assert
        val state = authViewModel.authState.first()
        assert(state.isAuthenticated)
        assert(state.userType == UserType.CLIENT)
    }

    @Test
    fun `testLogin_OperatorType_SetsUserTypeToOperator`() = runTest {
        // Arrange
        authViewModel = AuthViewModel()
        every { mockFirebaseUser.uid } returns "operator_uid"
        every { mockFirebaseUser.email } returns "operator@email.com"
        val authResult = mockk<com.google.firebase.auth.AuthResult>()
        every { authResult.user } returns mockFirebaseUser
        val task = mockk<com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult>>()
        every { task.isSuccessful } returns true
        every { task.result } returns authResult
        every { task.addOnCompleteListener(any()) } answers {
            val callback = firstArg<com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>>()
            callback.onComplete(task)
            task
        }
        every { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } returns task
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // Act
        authViewModel.login("operator@email.com", "password123", true)
        advanceUntilIdle()

        // Assert
        val state = authViewModel.authState.first()
        assert(state.isAuthenticated)
        assert(state.userType == UserType.OPERATOR)
    }
}

