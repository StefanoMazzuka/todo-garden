package com.todogarden.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun GardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF466A42), onPrimary = Color.White,
        primaryContainer = Color(0xFFE1EACF), onPrimaryContainer = Color(0xFF263C24),
        secondary = Color(0xFF826C47), background = Color(0xFFF5F3E9),
        surface = Color(0xFFF5F3E9), surfaceVariant = Color(0xFFE8E8DB),
        onBackground = Color(0xFF293D2B), onSurface = Color(0xFF293D2B)
    ), content = content)
}
