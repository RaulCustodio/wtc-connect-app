// file: app/src/main/java/br/com/fiap/wtcconnect/ui/theme/Theme.kt

package br.com.fiap.wtcconnect.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme // Add this import
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun Color.isDark() = this.luminance() < 0.5f

// For now, DarkColorScheme can be the same as the light one.
// When you're ready, just update the colors here.
private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue, // Placeholder, you would change this for a real dark theme
    secondary = AccentBlue,
    tertiary = AccentOrange,
    background = Black,
    surface = Black,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = White,
    onSurface = White,
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    secondary = AccentBlue,
    tertiary = AccentOrange,
    background = LightGray,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Black,
    onSurface = Black,
)

@Composable
fun WtcCrmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Select the correct color scheme based on the system setting.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !colorScheme.primary.isDark()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}