package com.ortiz.centrotareas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Mejora IA 1: esquema oscuro con la paleta propia
private val DarkColorScheme = darkColorScheme(
    primary = AzulClaro,
    secondary = NaranjaClaro,
    tertiary = TurquesaClaro,
    onPrimary = AzulOscuro
)

// Mejora IA 1: esquema claro con la paleta propia (azul + naranja)
private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    secondary = Naranja,
    tertiary = Turquesa,
    onPrimary = Color.White,
    onSecondary = Color.White,
    surfaceVariant = AzulFondoTarjeta
)

@Composable
fun CentroTareasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Mejora IA 1: sin color dinámico, para que siempre se use la paleta propia
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}