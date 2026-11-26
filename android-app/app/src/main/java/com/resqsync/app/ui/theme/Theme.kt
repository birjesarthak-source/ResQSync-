package com.resqsync.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ResQSyncPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = ResQSyncBlueContainer,
    onPrimaryContainer = ResQSyncPrimaryBlue,
    secondary = ResQSyncSuccessGreen,
    onSecondary = Color.White,
    secondaryContainer = ResQSyncSuccessContainer,
    onSecondaryContainer = ResQSyncSuccessGreen,
    tertiary = ResQSyncWarningOrange,
    onTertiary = Color.White,
    background = ResQSyncLightBg,
    onBackground = ResQSyncTextDark,
    surface = ResQSyncLightSurface,
    onSurface = ResQSyncTextDark,
    surfaceVariant = ResQSyncLightBg,
    onSurfaceVariant = ResQSyncTextMuted,
    error = ResQSyncEmergencyRed,
    onError = Color.White,
    errorContainer = ResQSyncRedContainer,
    onErrorContainer = ResQSyncEmergencyRed,
    outline = ResQSyncLightBorder
)

@Composable
fun ResQSyncTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
