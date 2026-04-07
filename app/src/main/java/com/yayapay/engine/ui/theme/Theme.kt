package com.yayapay.engine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = Color(0xFF00C853),
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF444746),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1F1F1F),
    outline = Color(0xFF747775)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFF69F0AE),
    onSecondary = Color(0xFF003919),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFC4C7C5),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE3E3E3),
    outline = Color(0xFF8E918F)
)

@Composable
fun YayaPayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
