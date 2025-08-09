package com.example.car.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme()

@Composable
fun Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}