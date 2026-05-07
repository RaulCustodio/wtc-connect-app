package br.com.fiap.wtcconnect.screens

import androidx.compose.runtime.Composable
import br.com.fiap.wtcconnect.screens.profile.ProfileScreen as ProfileScreenImpl
import br.com.fiap.wtcconnect.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel? = null,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    // Delegate to the ProfileScreen implementation in the profile package
    ProfileScreenImpl(
        authViewModel = authViewModel,
        onNavigateToChangePassword = onNavigateToChangePassword,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToHelp = onNavigateToHelp
    )
}
