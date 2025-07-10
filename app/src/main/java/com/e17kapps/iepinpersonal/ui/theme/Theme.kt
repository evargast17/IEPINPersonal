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
    val GradientStart = PrimaryBlue
    val GradientEnd = PrimaryPurple

    val CardBackground = Color.White
    val CardBackgroundDark = Color(0xFF2A2A2A)

    val TextPrimary = Color(0xFF374151)
    val TextSecondary = Color(0xFF6B7280)
    val TextHint = Color(0xFF9CA3AF)

    val DividerLight = Color(0xFFE5E7EB)
    val DividerDark = Color(0xFF374151)

    val SuccessGreen = Color(0xFF10B981)
    val InfoBlue = Color(0xFF3B82F6)
    val WarningYellow = WarningOrange
    val DangerRed = ErrorRed

    // Status colors for payments
    val PaymentPending = WarningOrange
    val PaymentCompleted = Color(0xFF10B981)
    val PaymentCancelled = ErrorRed
    val PaymentFailed = Color(0xFFEF4444)

    // Method colors
    val CashColor = Color(0xFF059669)
    val TransferColor = Color(0xFF3B82F6)
    val YapeColor = Color(0xFF00A651)
    val PlinColor = Color(0xFF1E3A8A)
}