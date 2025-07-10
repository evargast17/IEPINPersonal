package com.e17kapps.iepinpersonal.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import kotlinx.coroutines.delay
import android.util.Log
import androidx.compose.foundation.layout.padding

@Composable
fun SplashScreen() {

    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "SplashScreen iniciado")
    }

    val alphaAnimation = remember { Animatable(0f) }
    val scaleAnimation = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "Iniciando animaciones...")

        // Animación de entrada
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        // 🔥 AUMENTAR el delay para ver mejor el splash
        Log.d("SplashScreen", "Esperando 3 segundos...")
        delay(3000) // Cambiar de 1500 a 3000ms

        Log.d("SplashScreen", "SplashScreen terminado")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.GradientStart,
                        AppColors.GradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnimation.value)
                .scale(scaleAnimation.value)
        ) {
            // Logo placeholder (puedes reemplazar con tu logo)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👥",
                    fontSize = 48.sp
                )
            }

            // Título de la app
            Text(
                text = "IEPIN Personal",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )

            // Subtítulo
            Text(
                text = "Gestión de Personal",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            // 🔥 DEBUG: Indicador visual
            Text(
                text = "Cargando...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}