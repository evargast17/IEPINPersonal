package com.e17kapps.iepinpersonal.ui.screens.statistics

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.ui.components.StatCard
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateToEmployeeStats: (String) -> Unit,
    onNavigateToMonthlyReport: (Int, Int) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedPeriod by remember { mutableStateOf("monthly") }
    var showExportDialog by remember { mutableStateOf(false) }

    // Inicializar datos al entrar
    LaunchedEffect(Unit) {
        Log.d("StatisticsScreen", "🚀 Iniciando StatisticsScreen")
        viewModel.refreshStatistics()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Estadísticas",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            actions = {
                IconButton(
                    onClick = { showExportDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Exportar",
                        tint = AppColors.GradientStart
                    )
                }

                IconButton(
                    onClick = {
                        Log.d("StatisticsScreen", "🔄 Refresh solicitado")
                        viewModel.refreshStatistics()
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
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
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = AppColors.TextPrimary
            )
        )

        // Contenido principal
        when (val currentState = dashboardStats) {
            is UiState.Loading -> {
                LoadingSection()
            }

            is UiState.Error -> {
                ErrorSection(
                    message = currentState.message,
                    onRetry = {
                        Log.d("StatisticsScreen", "🔄 Usuario solicitó reintentar")
                        viewModel.refreshStatistics()
                    }
                )
            }

            is UiState.Success -> {
                StatisticsContent(
                    statistics = currentState.data,
                    employees = employees,
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { selectedPeriod = it },
                    onMonthYearChange = { month, year ->
                        viewModel.selectMonth(month, year)
                    },
                    onNavigateToEmployeeStats = onNavigateToEmployeeStats,
                    onNavigateToMonthlyReport = onNavigateToMonthlyReport,
                    onRefresh = { viewModel.refreshStatistics() }
                )
            }

            is UiState.Empty -> {
                EmptyStatistics(
                    onRefresh = { viewModel.refreshStatistics() }
                )
            }
        }
    }

    // Dialog de exportación
    if (showExportDialog) {
        com.e17kapps.iepinpersonal.ui.components.ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                Log.d("StatisticsScreen", "📊 Exportando en formato: $format")
                viewModel.exportData(format)
                showExportDialog = false
            }
        )
    }

    // Mostrar error como snackbar
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            Log.e("StatisticsScreen", "❌ Error: $error")
            // TODO: Mostrar snackbar
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
            text = "⚠️",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error al cargar estadísticas",
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
private fun StatisticsContent(
    statistics: DashboardStatistics,
    employees: List<Employee>,
    selectedMonth: Int,
    selectedYear: Int,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    onMonthYearChange: (Int, Int) -> Unit,
    onNavigateToEmployeeStats: (String) -> Unit,
    onNavigateToMonthlyReport: (Int, Int) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con selector de período
        item {
            com.e17kapps.iepinpersonal.ui.components.PeriodSelector(
                selectedPeriod = selectedPeriod,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onPeriodChange = onPeriodChange,
                onMonthYearChange = onMonthYearChange
            )
        }

        // Estadísticas principales
        item {
            Text(
                text = "Resumen General",
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
                        title = "Total Empleados",
                        value = statistics.totalEmployees.toString(),
                        icon = "👥",
                        color = AppColors.info,
                        change = null
                    )
                }

                item {
                    StatCard(
                        title = "Pagos del Mes",
                        value = formatCurrency(statistics.currentMonthPayments),
                        icon = "💰",
                        color = AppColors.success,
                        change = statistics.monthlyComparison.percentageChange
                    )
                }

                item {
                    StatCard(
                        title = "Pendiente",
                        value = formatCurrency(statistics.totalPendingAmount),
                        icon = "⏳",
                        color = AppColors.warning,
                        change = null
                    )
                }

                item {
                    StatCard(
                        title = "Pagos Hoy",
                        value = statistics.todayPayments.toString(),
                        icon = "✅",
                        color = AppColors.success,
                        change = null
                    )
                }
            }
        }

        // Gráfico de métodos de pago
        if (statistics.paymentMethodDistribution.isNotEmpty()) {
            item {
                com.e17kapps.iepinpersonal.ui.components.PaymentMethodChart(
                    distribution = statistics.paymentMethodDistribution
                )
            }
        }

        // Comparación mensual
        item {
            com.e17kapps.iepinpersonal.ui.components.MonthlyComparisonCard(
                comparison = statistics.monthlyComparison
            )
        }

        // Empleados por rendimiento
        if (employees.isNotEmpty()) {
            item {
                Text(
                    text = "Empleados",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }

            items(employees.take(5)) { employee ->
                com.e17kapps.iepinpersonal.ui.components.EmployeeStatCard(
                    employee = employee,
                    onClick = {
                        Log.d("StatisticsContent", "📊 Navegando a estadísticas de ${employee.fullName}")
                        onNavigateToEmployeeStats(employee.id)
                    }
                )
            }

            if (employees.size > 5) {
                item {
                    TextButton(
                        onClick = {
                            Log.d("StatisticsContent", "👥 Ver todos los empleados")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ver todos los empleados",
                            color = AppColors.GradientStart
                        )
                    }
                }
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
                com.e17kapps.iepinpersonal.ui.components.ActivityCard(activity = activity)
            }
        }

        // Botones de acción
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        Log.d("StatisticsContent", "📊 Generar reporte mensual")
                        onNavigateToMonthlyReport(selectedMonth, selectedYear)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reporte Mensual")
                }

                Button(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.GradientStart
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar")
                }
            }
        }
    }
}

@Composable
private fun EmptyStatistics(
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
            text = "Sin estadísticas disponibles",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Agrega empleados y registra pagos para generar estadísticas",
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