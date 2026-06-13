package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CalculatorColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = BaseBackground,
    primaryContainer = OperatorContainer,
    onPrimaryContainer = OnOperatorContainer,
    secondary = UtilityContainer,
    onSecondary = OnUtilityContainer,
    background = BaseBackground,
    onBackground = OnSurface,
    surface = BaseBackground,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorColor,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force our custom high-utility Kinetic palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CalculatorColorScheme,
        typography = Typography,
        content = content
    )
}
