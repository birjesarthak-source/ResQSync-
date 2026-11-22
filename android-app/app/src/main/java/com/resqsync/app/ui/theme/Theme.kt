package com.resqsync.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ResQSyncCyan,
    onPrimary = ResQSyncDarkNavy,
    primaryContainer = ResQSyncNavySurface,
    onPrimaryContainer = ResQSyncCyan,
    secondary = ResQSyncCyanLight,
    onSecondary = ResQSyncDarkNavy,
    secondaryContainer = ResQSyncNavyCard,
    onSecondaryContainer = ResQSyncTextPrimary,
    tertiary = ResQSyncSuccessGreen,
    onTertiary = Color.White,
    background = ResQSyncDarkNavy,
    onBackground = ResQSyncTextPrimary,
    surface = ResQSyncNavySurface,
    onSurface = ResQSyncTextPrimary,
    surfaceVariant = ResQSyncNavyCard,
    onSurfaceVariant = ResQSyncTextSecondary,
    error = ResQSyncEmergencyRed,
    onError = Color.White,
    errorContainer = ResQSyncRedContainer,
    onErrorContainer = Color.White
)

@Composable
fun ResQSyncTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
