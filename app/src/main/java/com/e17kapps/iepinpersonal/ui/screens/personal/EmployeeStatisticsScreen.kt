package com.e17kapps.iepinpersonal.ui.screens.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.ui.components.LoadingState
import com.e17kapps.iepinpersonal.ui.screens.statistics.StatisticsViewModel
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.formatCurrency
import com.e17kapps.iepinpersonal.utils.toDateString
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeStatisticsScreen(
    employeeId: String,
    onNavigateBack: () -> Unit,
    employeeViewModel: EmployeeViewModel = hiltViewModel(),
    statisticsViewModel: StatisticsViewModel = hiltViewModel()
) {
    val selectedEmployee by employeeViewModel.selectedEmployee.collectAsState()
    val employeeStats by statisticsViewModel.employeeStats.collectAsState()
    val isLoading by statisticsViewModel.isLoading.collectAsState()

    var selectedTimeframe by remember { mutableStateOf(StatisticsTimeframe.YEAR) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Cargar empleado y estadísticas
    LaunchedEffect(employeeId) {
        val employees = employeeViewModel.uiState.value.employees
        val employee = employees.find { it.id == employeeId }
        employee?.let {
            employeeViewModel.selectEmployee(it)
            statisticsViewModel.selectEmployee(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text("Estadísticas del Empleado")
                    selectedEmployee?.let { employee ->
                        Text(
                            text = employee.fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Exportar"
                    )
                }

                IconButton(onClick = { statisticsViewModel.refreshStatistics() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        when {
            isLoading -> {
                LoadingState(
                    message = "Cargando estadísticas...",
                    modifier = Modifier.fillMaxSize()
                )
            }

            selectedEmployee == null -> {
                ErrorState(
                    message = "Empleado no encontrado",
                    onRetry = onNavigateBack
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Header del empleado
                        EmployeeStatsHeader(employee = selectedEmployee!!)
                    }

                    item {
                        // Selector de período
                        TimeframeSelectorCard(
                            selectedTimeframe = selectedTimeframe,
                            onTimeframeChange = { selectedTimeframe = it }
                        )
                    }

                    item {
                        // Estadísticas principales
                        when (val statsState = employeeStats) {
                            is UiState.Success -> {
                                MainStatisticsCard(
                                    stats = statsState.data,
                                    timeframe = selectedTimeframe
                                )
                            }
                            is UiState.Error -> {
                                ErrorCard(message = statsState.message)
                            }
                            else -> {
                                LoadingCard()
                            }
                        }
                    }

                    item {
                        // Gráfico de tendencias (simulado)
                        PaymentTrendCard(
                            employee = selectedEmployee!!,
                            timeframe = selectedTimeframe
                        )
                    }

                    item {
                        // Comparativa con otros empleados
                        ComparisonCard(
                            employee = selectedEmployee!!,
                            timeframe = selectedTimeframe
                        )
                    }

                    item {
                        // Historial reciente
                        when (val statsState = employeeStats) {
                            is UiState.Success -> {
                                RecentActivityCard(stats = statsState.data)
                            }
                            else -> {
                                // Loading o error ya manejado arriba
                            }
                        }
                    }

                    item {
                        // Insights y recomendaciones
                        InsightsCard(
                            employee = selectedEmployee!!,
                            timeframe = selectedTimeframe
                        )
                    }
                }
            }
        }
    }

    // Dialog de exportación
    if (showExportDialog) {
        ExportDialog(
            employee = selectedEmployee!!,
            onExport = { format ->
                // Implementar exportación
                showExportDialog = false
            },
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun EmployeeStatsHeader(employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar grande
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = AppColors.GradientStart.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = AppColors.GradientStart
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Información del empleado
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.fullName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.TextPrimary
                )

                Text(
                    text = employee.position,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Desde ${employee.startDate.toDateString("MMM yyyy")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.success
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCurrency(employee.baseSalary),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.success
                    )
                }
            }

            // Estado del empleado
            Column(
                horizontalAlignment = Alignment.End
            ) {
                StatusChip(isActive = employee.isActive)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = calculateEmployeeAntiquity(employee.startDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun TimeframeSelectorCard(
    selectedTimeframe: StatisticsTimeframe,
    onTimeframeChange: (StatisticsTimeframe) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Período de análisis",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(StatisticsTimeframe.values()) { timeframe ->
                    TimeframeChip(
                        timeframe = timeframe,
                        isSelected = selectedTimeframe == timeframe,
                        onClick = { onTimeframeChange(timeframe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeframeChip(
    timeframe: StatisticsTimeframe,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { Text(timeframe.displayName) },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.GradientStart,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun MainStatisticsCard(
    stats: EmployeeStatistics,
    timeframe: StatisticsTimeframe
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resumen Financiero",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid de estadísticas principales
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem(
                        title = "Total Pagado",
                        value = formatCurrency(stats.totalPayments),
                        icon = Icons.Default.AttachMoney,
                        color = AppColors.success
                    )

                    StatisticItem(
                        title = "Número de Pagos",
                        value = stats.paymentHistory.size.toString(),
                        icon = Icons.Default.Receipt,
                        color = AppColors.info
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem(
                        title = "Total Descuentos",
                        value = formatCurrency(stats.totalDiscounts),
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        color = AppColors.error
                    )

                    StatisticItem(
                        title = "Total Adelantos",
                        value = formatCurrency(stats.totalAdvances),
                        icon = Icons.Default.CreditCard,
                        color = AppColors.warning
                    )
                }

                if (stats.pendingAmount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.warning.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AppColors.warning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pago Pendiente",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = AppColors.warning
                                )
                            }

                            Text(
                                text = formatCurrency(stats.pendingAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AppColors.warning
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AppColors.TextPrimary
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PaymentTrendCard(
    employee: Employee,
    timeframe: StatisticsTimeframe
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tendencia de Pagos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.TextPrimary
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = AppColors.success
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulación de gráfico simple
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(6) { index ->
                    val height = (40 + (index * 10) + (0..20).random()).dp
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(height)
                            .background(
                                color = AppColors.GradientStart.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Últimos 6 meses • Promedio: ${formatCurrency(employee.baseSalary)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ComparisonCard(
    employee: Employee,
    timeframe: StatisticsTimeframe
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Comparativa",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Comparaciones simuladas
            ComparisonItem(
                title = "Vs. Promedio del cargo",
                value = "+12%",
                isPositive = true
            )

            ComparisonItem(
                title = "Vs. Mes anterior",
                value = "0%",
                isPositive = null
            )

            ComparisonItem(
                title = "Puntualidad en pagos",
                value = "100%",
                isPositive = true
            )
        }
    }
}

@Composable
private fun ComparisonItem(
    title: String,
    value: String,
    isPositive: Boolean?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (isPositive) {
                true -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = AppColors.success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                false -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = AppColors.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                null -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingFlat,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = when (isPositive) {
                    true -> AppColors.success
                    false -> AppColors.error
                    null -> AppColors.TextSecondary
                }
            )
        }
    }
}

@Composable
private fun RecentActivityCard(stats: EmployeeStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Historial Reciente",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (stats.paymentHistory.isNotEmpty()) {
                stats.paymentHistory.take(3).forEach { payment ->
                    ActivityHistoryItem(payment = payment)
                }

                if (stats.paymentHistory.size > 3) {
                    TextButton(
                        onClick = { /* Navegar a historial completo */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver historial completo (${stats.paymentHistory.size} pagos)")
                    }
                }
            } else {
                Text(
                    text = "Sin pagos registrados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityHistoryItem(payment: Payment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = getStatusColor(payment.status).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getStatusIcon(payment.status),
                    contentDescription = null,
                    tint = getStatusColor(payment.status),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = formatCurrency(payment.amount),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.TextPrimary
                )

                Text(
                    //text = "${getMonthName(payment.month)} ${payment.year}",
                    text = "FALTA CORREGIR AQUI",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        Text(
            text = payment.paymentDate.toDateString("dd/MM"),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun InsightsCard(
    employee: Employee,
    timeframe: StatisticsTimeframe
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = AppColors.warning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Insights y Recomendaciones",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.TextPrimary
                )
            }

            // Insights automáticos basados en datos
            val insights = generateInsights(employee, timeframe)

            insights.forEach { insight ->
                InsightItem(insight = insight)
            }
        }
    }
}

@Composable
private fun InsightItem(insight: EmployeeInsight) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = insight.type.color.copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = insight.type.icon,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = insight.type.color
                )

                if (insight.description.isNotBlank()) {
                    Text(
                        text = insight.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.error.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = AppColors.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.error
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = AppColors.GradientStart
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AppColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Regresar")
        }
    }
}

@Composable
private fun ExportDialog(
    employee: Employee,
    onExport: (ExportFormat) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exportar Estadísticas") },
        text = {
            Column {
                Text(
                    text = "Selecciona el formato de exportación para ${employee.fullName}:",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ExportFormat.values().forEach { format ->
                    TextButton(
                        onClick = { onExport(format) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = format.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(format.displayName)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Clases de datos y enums auxiliares

private enum class StatisticsTimeframe(val displayName: String) {
    MONTH("Este mes"),
    QUARTER("Trimestre"),
    YEAR("Este año"),
    ALL_TIME("Todo el tiempo")
}

private enum class ExportFormat(
    val displayName: String,
    val icon: ImageVector
) {
    PDF("Reporte PDF", Icons.Default.PictureAsPdf),
    EXCEL("Hoja de Excel", Icons.Default.TableChart),
    CSV("Datos CSV", Icons.AutoMirrored.Filled.InsertDriveFile)
}

private data class EmployeeInsight(
    val title: String,
    val description: String,
    val type: InsightType
)

private enum class InsightType(
    val icon: String,
    val color: Color
) {
    POSITIVE("✅", AppColors.success),
    WARNING("⚠️", AppColors.warning),
    INFO("ℹ️", AppColors.info),
    CRITICAL("🚨", AppColors.error)
}

// Funciones auxiliares

private fun generateInsights(
    employee: Employee,
    timeframe: StatisticsTimeframe
): List<EmployeeInsight> {
    val insights = mutableListOf<EmployeeInsight>()

    // Insight sobre antigüedad
    val antiquityMonths = getAntiquityInMonths(employee.startDate)
    when {
        antiquityMonths >= 12 -> {
            insights.add(
                EmployeeInsight(
                    title = "Empleado consolidado",
                    description = "Más de 1 año en la empresa. Considera beneficios adicionales.",
                    type = InsightType.POSITIVE
                )
            )
        }
        antiquityMonths >= 6 -> {
            insights.add(
                EmployeeInsight(
                    title = "Período de adaptación completado",
                    description = "6+ meses en la empresa. Buen momento para evaluación.",
                    type = InsightType.INFO
                )
            )
        }
        else -> {
            insights.add(
                EmployeeInsight(
                    title = "Empleado nuevo",
                    description = "Menos de 6 meses. Seguimiento cercano recomendado.",
                    type = InsightType.WARNING
                )
            )
        }
    }

    // Insight sobre estado
    if (!employee.isActive) {
        insights.add(
            EmployeeInsight(
                title = "Empleado inactivo",
                description = "Revisar motivo de inactivación y considerar reactivación.",
                type = InsightType.CRITICAL
            )
        )
    }

    // Insight sobre salario
    if (employee.baseSalary < 1000) {
        insights.add(
            EmployeeInsight(
                title = "Salario bajo el promedio",
                description = "Considerar ajuste salarial para retención del talento.",
                type = InsightType.WARNING
            )
        )
    }

    return insights
}

private fun getAntiquityInMonths(startDate: Long): Int {
    val startCalendar = Calendar.getInstance().apply { timeInMillis = startDate }
    val currentCalendar = Calendar.getInstance()

    val years = currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
    val months = currentCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)

    return years * 12 + months
}

private fun calculateEmployeeAntiquity(startDate: Long): String {
    val months = getAntiquityInMonths(startDate)

    return when {
        months < 1 -> "Menos de 1 mes"
        months < 12 -> "$months meses"
        months == 12 -> "1 año"
        else -> {
            val years = months / 12
            val remainingMonths = months % 12
            when {
                remainingMonths == 0 -> "$years años"
                years == 1 -> "1 año y $remainingMonths meses"
                else -> "$years años y $remainingMonths meses"
            }
        }
    }
}

private fun getStatusColor(status: PaymentStatus): Color {
    return when (status) {
        PaymentStatus.COMPLETED -> AppColors.success
        PaymentStatus.PENDING -> AppColors.warning
        PaymentStatus.CANCELLED -> AppColors.TextSecondary
        PaymentStatus.FAILED -> AppColors.error
    }
}

private fun getStatusIcon(status: PaymentStatus): ImageVector {
    return when (status) {
        PaymentStatus.COMPLETED -> Icons.Default.CheckCircle
        PaymentStatus.PENDING -> Icons.Default.Schedule
        PaymentStatus.CANCELLED -> Icons.Default.Cancel
        PaymentStatus.FAILED -> Icons.Default.Error
    }
}

private fun getMonthName(month: Int): String {
    val monthNames = arrayOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    return monthNames.getOrNull(month - 1) ?: "Mes $month"
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val (backgroundColor, textColor, text, icon) = if (isActive) {
        listOf(
            AppColors.success.copy(alpha = 0.1f),
            AppColors.success,
            "Activo",
            "✅"
        )
    } else {
        listOf(
            AppColors.TextSecondary.copy(alpha = 0.1f),
            AppColors.TextSecondary,
            "Inactivo",
            "⏸️"
        )
    }

    Card(colors = CardDefaults.cardColors(containerColor = backgroundColor as Color)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon as String, fontSize = 10.sp)
            Text(
                text = text as String,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = textColor as Color
            )
        }
    }
}