package id.alpine12.vehiclecare.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue80, onPrimary = Blue20, primaryContainer = Blue40, onPrimaryContainer = Blue80,
    secondary = Orange80, onSecondary = Orange40, tertiary = Green80, onTertiary = Green40,
    background = SurfaceDark, surface = SurfaceDark, onBackground = Gray90, onSurface = Gray90,
    error = ErrorDark, onError = ErrorLight
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40, onPrimary = Gray95, primaryContainer = Blue80, onPrimaryContainer = Blue20,
    secondary = Orange40, onSecondary = Gray95, secondaryContainer = Orange80, onSecondaryContainer = Orange40,
    tertiary = Green40, onTertiary = Gray95, background = SurfaceLight, surface = SurfaceLight,
    onBackground = Gray10, onSurface = Gray10, error = ErrorLight, onError = Gray95
)

@Composable
fun VehicleCareTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}