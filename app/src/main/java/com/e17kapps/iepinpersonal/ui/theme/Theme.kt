package com.e17kapps.iepinpersonal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================================
// COLORES PRINCIPALES DE LA APP
// ============================================================================
val PrimaryBlue = Color(0xFF667EEA)
val PrimaryPurple = Color(0xFF764BA2)
val SecondaryTeal = Color(0xFF4ECDC4)
val AccentGreen = Color(0xFF96CEB4)
val ErrorRed = Color(0xFFFF6B6B)
val WarningOrange = Color(0xFFFECA57)

// ============================================================================
// COLORES DE SUPERFICIE
// ============================================================================
val SurfaceLight = Color(0xFFF8F9FF)
val SurfaceDark = Color(0xFF1A1A1A)
val BackgroundLight = Color(0xFFF1F4FF)
val BackgroundDark = Color(0xFF121212)

// ============================================================================
// ESQUEMAS DE COLOR PARA MATERIAL DESIGN 3
// ============================================================================
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

// ============================================================================
// TEMA PRINCIPAL DE LA APLICACIÓN
// ============================================================================
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

// ============================================================================
// COLORES ADICIONALES PARA COMPONENTES ESPECÍFICOS
// ============================================================================
object AppColors {
    // ========================================================================
    // COLORES PRIMARIOS Y GRADIENTES
    // ========================================================================
    val primary = PrimaryBlue
    val secondary = SecondaryTeal
    val tertiary = AccentGreen
    val GradientStart = PrimaryBlue
    val GradientEnd = PrimaryPurple

    // ========================================================================
    // COLORES DE FONDO Y SUPERFICIES
    // ========================================================================
    val surface = SurfaceLight
    val background = BackgroundLight
    val CardBackground = Color.White
    val CardBackgroundDark = Color(0xFF2A2A2A)

    // ========================================================================
    // COLORES DE TEXTO
    // ========================================================================
    val TextPrimary = Color(0xFF374151)
    val TextSecondary = Color(0xFF6B7280)
    val TextHint = Color(0xFF9CA3AF)
    val onPrimary = Color.White
    val onSecondary = Color.White
    val onSurface = Color.Black
    val onBackground = Color.Black

    // ========================================================================
    // DIVISORES
    // ========================================================================
    val DividerLight = Color(0xFFE5E7EB)
    val DividerDark = Color(0xFF374151)

    // ========================================================================
    // COLORES DE ESTADO
    // ========================================================================
    val success = Color(0xFF10B981)
    val info = Color(0xFF3B82F6)
    val warning = WarningOrange
    val error = ErrorRed

    // ========================================================================
    // COLORES ESPECÍFICOS PARA ESTADOS DE PAGOS
    // ========================================================================
    val PaymentCompleted = Color(0xFF10B981)  // Verde - mismo que success
    val PaymentPending = WarningOrange        // Naranja - mismo que warning
    val PaymentCancelled = ErrorRed           // Rojo - mismo que error
    val PaymentFailed = Color(0xFFEF4444)     // Rojo más intenso

    // ========================================================================
    // COLORES PARA MÉTODOS DE PAGO
    // ========================================================================
    val CashColor = Color(0xFF059669)         // Verde para efectivo
    val TransferColor = Color(0xFF3B82F6)     // Azul para transferencias
    val YapeColor = Color(0xFF722F8F)         // Morado oficial de YAPE
    val PlinColor = Color(0xFF00BCD4)         // Celeste oficial de PLIN

}