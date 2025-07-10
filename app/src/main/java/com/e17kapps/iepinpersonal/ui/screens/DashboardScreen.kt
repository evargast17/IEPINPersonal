package com.e17kapps.iepinpersonal.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.DashboardStatistics
import com.e17kapps.iepinpersonal.domain.model.UiState
import com.e17kapps.iepinpersonal.ui.components.StatCard
import com.e17kapps.iepinpersonal.ui.components.QuickActionCard
import com.e17kapps.iepinpersonal.ui.components.ActivityItemCard
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.formatCurrency
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAddEmployee: (() -> Unit)? = null,
    onNavigateToAddPayment: (() -> Unit)? = null,
    onNavigateToDiscounts: (() -> Unit)? = null,
    onNavigateToAdvances: (() -> Unit)? = null
) {
    val statisticsState by viewModel.statisticsState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Refrescar datos al entrar
    LaunchedEffect(Unit) {
        Log.d("DashboardScreen", "🚀 Iniciando DashboardScreen")
        viewModel.refreshData()
    }

    // Debug de estados
    LaunchedEffect(statisticsState) {
        when (val currentState = statisticsState) {
            is UiState.Loading -> Log.d("DashboardScreen", "⏳ Estado: Cargando...")
            is UiState.Success -> {
                val stats = currentState.data
                Log.d("DashboardScreen", "✅ Estado: Éxito - ${stats.totalEmployees} empleados, ${formatCurrency(stats.currentMonthPayments)} este mes")
            }
            is UiState.Error -> Log.e("DashboardScreen", "❌ Estado: Error - ${currentState.message}")
            is UiState.Empty -> Log.d("DashboardScreen", "📭 Estado: Vacío")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val currentState = statisticsState) {
            is UiState.Loading -> {
                LoadingSection()
            }

            is UiState.Error -> {
                ErrorSection(
                    message = currentState.message,
                    onRetry = {
                        Log.d("DashboardScreen", "🔄 Usuario solicitó reintentar")
                        viewModel.refreshData()
                    }
                )
            }

            is UiState.Success -> {
                DashboardContent(
                    statistics = currentState.data,
                    onRefresh = {
                        Log.d("DashboardScreen", "🔄 Usuario solicitó refresh manual")
                        viewModel.refreshData()
                    },
                    isRefreshing = uiState.isLoading,
                    onNavigateToAddEmployee = onNavigateToAddEmployee,
                    onNavigateToAddPayment = onNavigateToAddPayment,
                    onNavigateToDiscounts = onNavigateToDiscounts,
                    onNavigateToAdvances = onNavigateToAdvances
                )
            }

            is UiState.Empty -> {
                EmptyDashboard(
                    onRefresh = {
                        Log.d("DashboardScreen", "🔄 Usuario solicitó comenzar desde pantalla vacía")
                        viewModel.refreshData()
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AppColors.GradientStart,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando estadísticas...",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun DashboardContent(
    statistics: DashboardStatistics,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    onNavigateToAddEmployee: (() -> Unit)?,
    onNavigateToAddPayment: (() -> Unit)?,
    onNavigateToDiscounts: (() -> Unit)?,
    onNavigateToAdvances: (() -> Unit)?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con saludo y botón de refresh
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "¡Hola! 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "Aquí tienes el resumen de hoy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        Log.d("DashboardContent", "🔄 Botón de refresh presionado")
                        onRefresh()
                    },
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = AppColors.GradientStart
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = AppColors.GradientStart
                        )
                    }
                }
            }
        }



        // Estadísticas principales
        item {
            Text(
                text = "Resumen Financiero",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    StatCard(
                        title = "Por Pagar",
                        value = formatCurrency(statistics.totalPendingAmount),
                        icon = "💰",
                        color = AppColors.DangerRed,
                        change = null
                    )
                }

                item {
                    StatCard(
                        title = "Este Mes",
                        value = formatCurrency(statistics.currentMonthPayments),
                        icon = "📅",
                        color = AppColors.SuccessGreen,
                        change = statistics.monthlyComparison.percentageChange
                    )
                }

                item {
                    StatCard(
                        title = "Empleados",
                        value = statistics.totalEmployees.toString(),
                        icon = if (statistics.totalEmployees > 0) "👥✨" else "👥",
                        color = if (statistics.totalEmployees > 0) AppColors.SuccessGreen else AppColors.InfoBlue,
                        change = null
                    )
                }

                item {
                    StatCard(
                        title = "Pagos Hoy",
                        value = statistics.todayPayments.toString(),
                        icon = "✅",
                        color = AppColors.SuccessGreen,
                        change = null
                    )
                }
            }
        }

        // Acciones rápidas
        item {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Agregar Personal",
                    icon = "👥",
                    color = AppColors.GradientStart,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Log.d("DashboardContent", "🏃 Navegando a agregar empleado")
                        onNavigateToAddEmployee?.invoke()
                    }
                )

                QuickActionCard(
                    title = "Registrar Pago",
                    icon = "💰",
                    color = AppColors.SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Log.d("DashboardContent", "💰 Navegando a registrar pago")
                        onNavigateToAddPayment?.invoke()
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Descuentos",
                    icon = "📉",
                    color = AppColors.WarningYellow,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Log.d("DashboardContent", "📉 Navegando a descuentos")
                        onNavigateToDiscounts?.invoke()
                    }
                )

                QuickActionCard(
                    title = "Adelantos",
                    icon = "💳",
                    color = AppColors.InfoBlue,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Log.d("DashboardContent", "💳 Navegando a adelantos")
                        onNavigateToAdvances?.invoke()
                    }
                )
            }
        }

        // Actividad reciente
        if (statistics.recentActivity.isNotEmpty()) {
            item {
                Text(
                    text = "Actividad Reciente",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }

            items(statistics.recentActivity.take(5)) { activity ->
                ActivityItemCard(
                    activity = activity,
                    onClick = {
                        Log.d("DashboardContent", "📋 Click en actividad: ${activity.title}")
                    }
                )
            }

            if (statistics.recentActivity.size > 5) {
                item {
                    TextButton(
                        onClick = {
                            Log.d("DashboardContent", "📋 Ver toda la actividad")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ver toda la actividad",
                            color = AppColors.GradientStart
                        )
                    }
                }
            }
        } else {
            // Mostrar mensaje cuando no hay actividad reciente
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📋",
                            fontSize = 32.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sin actividad reciente",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.TextPrimary
                        )

                        Text(
                            text = "Registra pagos y gestiona empleados para ver la actividad aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Espacio adicional al final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ErrorSection(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error al cargar datos",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.GradientStart
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyDashboard(
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📊",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sin datos disponibles",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Agrega empleados y registra pagos para ver las estadísticas",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.GradientStart
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Comenzar")
        }
    }
}

