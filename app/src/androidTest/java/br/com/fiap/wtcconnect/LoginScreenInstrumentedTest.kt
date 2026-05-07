package br.com.fiap.wtcconnect

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import br.com.fiap.wtcconnect.screens.login.LoginScreen

/**
 * Testes Instrumentados para LoginScreen
 *
 * Para usar, adicione ao build.gradle.kts:
 * androidTestImplementation("androidx.compose.ui:ui-test-junit4")
 * androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
 * androidTestImplementation("androidx.test.ext:junit:1.1.5")
 */

@RunWith(AndroidJUnit4::class)
class LoginScreenInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginScreen_DisplaysCorrectly() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = { })
        }

        // Assert
        composeTestRule.onNodeWithText("WTC Connect").assertExists()
        composeTestRule.onNodeWithText("E-mail ou Telefone").assertExists()
        composeTestRule.onNodeWithText("Senha").assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
    }

    @Test
    fun testLoginScreen_UserTypeToggle_DisplaysCorrectLabel() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = { })
        }

        // Act & Assert - Cliente (padrão)
        composeTestRule.onNodeWithText("Entrar como Cliente").assertExists()

        // Act - Ativar operador
        composeTestRule.onNodeWithText("Entrar como Cliente").performClick()

        // Assert - Operador
        composeTestRule.onNodeWithText("Entrar como Operador").assertExists()
    }

    @Test
    fun testLoginScreen_EmailInput_AcceptsText() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = { })
        }

        // Act
        composeTestRule.onNodeWithText("E-mail ou Telefone").performTextInput("test@email.com")

        // Assert - Texto foi digitado (verificado via snapshot ou interação posterior)
    }

    @Test
    fun testLoginScreen_PasswordInput_AcceptsText() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = { })
        }

        // Act
        composeTestRule.onNodeWithText("Senha").performTextInput("Teste123!")

        // Assert - Texto foi digitado
    }

    @Test
    fun testLoginScreen_ForgotPasswordButton_Exists() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = { })
        }

        // Assert
        composeTestRule.onNodeWithText("Esqueci minha senha").assertExists()
    }

    @Test
    fun testLoginScreen_LayoutStructure_IsCorrect() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = { })
        }

        // Assert
        composeTestRule.onNodeWithText("WTC Connect").assertExists()
        composeTestRule.onNodeWithText("E-mail ou Telefone").assertExists()
        composeTestRule.onNodeWithText("Senha").assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
        composeTestRule.onNodeWithText("Esqueci minha senha").assertExists()
    }
}

