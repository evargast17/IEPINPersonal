package com.e17kapps.iepinpersonal.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.e17kapps.iepinpersonal.domain.model.ActivityItem
import com.e17kapps.iepinpersonal.domain.model.DashboardStatistics
import com.e17kapps.iepinpersonal.domain.model.MonthlyComparison
import com.e17kapps.iepinpersonal.domain.model.UiState
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.formatCurrency
import kotlinx.coroutines.delay

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

    // Estados para optimización de rendering
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Manejo optimizado de refresh
    val handleRefresh = remember {
        {
            if (!isRefreshing) {
                isRefreshing = true
                viewModel.refreshData()
            }
        }
    }

    // Refrescar datos solo cuando sea necesario
    LaunchedEffect(Unit) {
        Log.d("DashboardScreen", "🚀 Iniciando DashboardScreen Optimizado")
        viewModel.refreshData()
    }

    // Reset del estado de refresh
    LaunchedEffect(statisticsState) {
        if (statisticsState is UiState.Success || statisticsState is UiState.Error) {
            delay(500) // Pequeño delay para UX
            isRefreshing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val currentState = statisticsState) {
            is UiState.Loading -> {
                if (!isRefreshing) {
                    OptimizedLoadingContent()
                } else {
                    // Si estamos haciendo refresh, mostrar contenido con loading indicator
                    currentState.takeIf { false }?.let {
                        OptimizedDashboardContent(
                            statistics = DashboardStatistics(),
                            onRefresh = handleRefresh,
                            isRefreshing = true,
                            listState = listState,
                            onNavigateToAddEmployee = onNavigateToAddEmployee,
                            onNavigateToAddPayment = onNavigateToAddPayment,
                            onNavigateToDiscounts = onNavigateToDiscounts,
                            onNavigateToAdvances = onNavigateToAdvances
                        )
                    }
                }
            }

            is UiState.Success -> {
                OptimizedDashboardContent(
                    statistics = currentState.data,
                    onRefresh = handleRefresh,
                    isRefreshing = isRefreshing,
                    listState = listState,
                    onNavigateToAddEmployee = onNavigateToAddEmployee,
                    onNavigateToAddPayment = onNavigateToAddPayment,
                    onNavigateToDiscounts = onNavigateToDiscounts,
                    onNavigateToAdvances = onNavigateToAdvances
                )
            }

            is UiState.Error -> {
                OptimizedErrorContent(
                    error = currentState.message,
                    onRetry = handleRefresh,
                    isRetrying = isRefreshing
                )
            }

            is UiState.Empty -> {
                OptimizedEmptyContent(
                    onNavigateToAddEmployee = onNavigateToAddEmployee
                )
            }
        }
    }
}

@Composable
private fun OptimizedLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = AppColors.primary
            )
            Text(
                text = "Cargando estadísticas...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OptimizedDashboardContent(
    statistics: DashboardStatistics,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    listState: LazyListState,
    onNavigateToAddEmployee: (() -> Unit)?,
    onNavigateToAddPayment: (() -> Unit)?,
    onNavigateToDiscounts: (() -> Unit)?,
    onNavigateToAdvances: (() -> Unit)?
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header optimizado
        item {
            OptimizedDashboardHeader(
                onRefresh = onRefresh,
                isRefreshing = isRefreshing
            )
        }

        // Stats Cards optimizadas
        item {
            OptimizedStatsSection(statistics = statistics)
        }

        // Quick Actions optimizadas
        item {
            OptimizedQuickActionsSection(
                onNavigateToAddEmployee = onNavigateToAddEmployee,
                onNavigateToAddPayment = onNavigateToAddPayment,
                onNavigateToDiscounts = onNavigateToDiscounts,
                onNavigateToAdvances = onNavigateToAdvances
            )
        }

        // Monthly Comparison optimizada
        item {
            OptimizedMonthlyComparisonSection(
                monthlyComparison = statistics.monthlyComparison
            )
        }

        // Activity section (solo si hay datos)
        if (statistics.recentActivity.isNotEmpty()) {
            item {
                OptimizedActivitySection(
                    activities = statistics.recentActivity.take(5) // Limitar a 5 items
                )
            }
        }
    }
}

@Composable
private fun OptimizedDashboardHeader(
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Resumen de actividades",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar",
                    tint = AppColors.primary
                )
            }
        }
    }
}

@Composable
private fun OptimizedStatsSection(statistics: DashboardStatistics) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Estadísticas Principales",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Grid de stats cards con animaciones más suaves
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                OptimizedStatCard(
                    title = "Empleados Activos",
                    value = statistics.totalEmployees.toString(),
                    subtitle = "Total registrados",
                    icon = "👥",
                    color = AppColors.primary
                )
            }

            item {
                OptimizedStatCard(
                    title = "Pagos Hoy",
                    value = statistics.todayPayments.toString(),
                    subtitle = "Procesados",
                    icon = "💰",
                    color = AppColors.success
                )
            }

            item {
                OptimizedStatCard(
                    title = "Pendientes",
                    value = formatCurrency(statistics.totalPendingAmount),
                    subtitle = "Por pagar",
                    icon = "⏰",
                    color = AppColors.warning
                )
            }

            item {
                OptimizedStatCard(
                    title = "Este Mes",
                    value = formatCurrency(statistics.currentMonthPayments),
                    subtitle = "Pagado",
                    icon = "📊",
                    color = AppColors.info
                )
            }
        }
    }
}

@Composable
private fun OptimizedStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OptimizedQuickActionsSection(
    onNavigateToAddEmployee: (() -> Unit)?,
    onNavigateToAddPayment: (() -> Unit)?,
    onNavigateToDiscounts: (() -> Unit)?,
    onNavigateToAdvances: (() -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Acciones Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Grid de acciones con diseño optimizado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            onNavigateToAddEmployee?.let { navigate ->
                OptimizedQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Agregar Empleado",
                    icon = "👤",
                    color = AppColors.primary,
                    onClick = navigate
                )
            }

            onNavigateToAddPayment?.let { navigate ->
                OptimizedQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Nuevo Pago",
                    icon = "💵",
                    color = AppColors.success,
                    onClick = navigate
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            onNavigateToDiscounts?.let { navigate ->
                OptimizedQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Descuentos",
                    icon = "📉",
                    color = AppColors.warning,
                    onClick = navigate
                )
            }

            onNavigateToAdvances?.let { navigate ->
                OptimizedQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Adelantos",
                    icon = "📈",
                    color = AppColors.info,
                    onClick = navigate
                )
            }
        }
    }
}

@Composable
private fun OptimizedQuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun OptimizedMonthlyComparisonSection(
    monthlyComparison: MonthlyComparison
) {
    if (monthlyComparison.currentMonth.totalPayments > 0 || monthlyComparison.previousMonth.totalPayments > 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Comparación Mensual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mes Actual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(monthlyComparison.currentMonth.totalPayments),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.primary
                        )
                    }

                    val isPositive = monthlyComparison.percentageChange >= 0
                    val percentageColor = if (isPositive) AppColors.success else AppColors.error
                    val percentageIcon = if (isPositive) "📈" else "📉"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = percentageIcon,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "${String.format("%.1f", monthlyComparison.percentageChange)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = percentageColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptimizedActivitySection(
    activities: List<ActivityItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Actividad Reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            activities.forEach { activity ->
                OptimizedActivityItem(activity = activity)
            }
        }
    }
}

@Composable
private fun OptimizedActivityItem(
    activity: ActivityItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = activity.icon,
                fontSize = 20.sp
            )
            Column {
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = activity.employeeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = formatCurrency(activity.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppColors.primary
        )
    }
}

@Composable
private fun OptimizedErrorContent(
    error: String,
    onRetry: () -> Unit,
    isRetrying: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 48.sp
                )

                Text(
                    text = "Error al cargar datos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onRetry,
                    enabled = !isRetrying,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary
                    )
                ) {
                    if (isRetrying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Reintentar")
                }
            }
        }
    }
}

@Composable
private fun OptimizedEmptyContent(
    onNavigateToAddEmployee: (() -> Unit)?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "👋",
                    fontSize = 48.sp
                )

                Text(
                    text = "¡Bienvenido!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Comienza agregando tu primer empleado para ver las estadísticas aquí.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                onNavigateToAddEmployee?.let { navigate ->
                    Button(
                        onClick = navigate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Empleado")
                    }
                }
            }
        }
    }
}