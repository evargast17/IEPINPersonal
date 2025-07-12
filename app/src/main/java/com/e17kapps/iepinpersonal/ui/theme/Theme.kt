package com.e17kapps.iepinpersonal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores principales de la app
val PrimaryBlue = Color(0xFF667EEA)
val PrimaryPurple = Color(0xFF764BA2)
val SecondaryTeal = Color(0xFF4ECDC4)
val AccentGreen = Color(0xFF96CEB4)
val ErrorRed = Color(0xFFFF6B6B)
val WarningOrange = Color(0xFFFECA57)

// Surface colors
val SurfaceLight = Color(0xFFF8F9FF)
val SurfaceDark = Color(0xFF1A1A1A)
val BackgroundLight = Color(0xFFF1F4FF)
val BackgroundDark = Color(0xFF121212)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryTeal,
    tertiary = AccentGreen,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryTeal,
    tertiary = AccentGreen,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

@Composable
fun IEPINPersonalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Colores adicionales para componentes específicos
object AppColors {
    // Colores primarios y gradientes
    val primary = PrimaryBlue
    val GradientStart = PrimaryBlue
    val GradientEnd = PrimaryPurple

    // Colores de fondo
    val CardBackground = Color.White
    val CardBackgroundDark = Color(0xFF2A2A2A)

    // Colores de texto
    val TextPrimary = Color(0xFF374151)
    val TextSecondary = Color(0xFF6B7280)
    val TextHint = Color(0xFF9CA3AF)

    // Divisores
    val DividerLight = Color(0xFFE5E7EB)
    val DividerDark = Color(0xFF374151)

    // Colores de estado
    val success = Color(0xFF10B981)
    val SuccessGreen = Color(0xFF10B981)
    val info = Color(0xFF3B82F6)
    val InfoBlue = Color(0xFF3B82F6)
    val warning = WarningOrange
    val WarningYellow = WarningOrange
    val error = ErrorRed
    val DangerRed = ErrorRed

    // Colores específicos para estados de pagos
    val PaymentPending = WarningOrange
    val PaymentCompleted = Color(0xFF10B981)
    val PaymentCancelled = ErrorRed
    val PaymentFailed = Color(0xFFEF4444)

    // Colores para métodos de pago
    val CashColor = Color(0xFF059669)
    val TransferColor = Color(0xFF3B82F6)
    val YapeColor = Color(0xFF00A651)
    val PlinColor = Color(0xFF1E3A8A)

    // Colores adicionales para la UI optimizada
    val secondary = SecondaryTeal
    val tertiary = AccentGreen
    val surface = SurfaceLight
    val background = BackgroundLight
    val onPrimary = Color.White
    val onSecondary = Color.White
    val onSurface = Color.Black
    val onBackground = Color.Black

}