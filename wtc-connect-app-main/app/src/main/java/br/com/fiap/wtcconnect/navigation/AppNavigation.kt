package br.com.fiap.wtcconnect.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.fiap.wtcconnect.screens.login.LoginScreen
import br.com.fiap.wtcconnect.screens.RegisterScreen
import br.com.fiap.wtcconnect.screens.main.MainScreen
import br.com.fiap.wtcconnect.viewmodel.AuthViewModel
import br.com.fiap.wtcconnect.viewmodel.UserType

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val OPERATOR = "operator"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState = authViewModel.authState.collectAsState()

    // Determine starting destination based on authentication state
    val startDestination = if (authState.value.isAuthenticated) {
        Routes.MAIN
    } else {
        Routes.LOGIN
    }

    // Listen to authentication state changes for logout
    LaunchedEffect(authState.value.isAuthenticated) {
        if (!authState.value.isAuthenticated &&
            navController.currentDestination?.route != Routes.LOGIN &&
            navController.currentDestination?.route != Routes.REGISTER) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { userType ->
                    val destination = if (userType == UserType.OPERATOR) {
                        Routes.OPERATOR
                    } else {
                        Routes.MAIN
                    }
                    navController.navigate(destination) {
                        // Limpa a pilha de navegação para que o usuário não volte para o login
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { userType ->
                    val destination = if (userType == UserType.OPERATOR) {
                        Routes.OPERATOR
                    } else {
                        Routes.MAIN
                    }
                    navController.navigate(destination) {
                        // Limpa a pilha de navegação
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.MAIN) {
            MainScreen(authViewModel = authViewModel)
        }
        composable(Routes.OPERATOR) {
            // TODO: Implementar tela de operador quando necessário
            MainScreen(authViewModel = authViewModel)
        }
    }
}
