package com.e17kapps.iepinpersonal.ui.screens.statistics

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.ui.components.StatCard
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.formatCurrency
import com.e17kapps.iepinpersonal.utils.toDateString
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    month: Int,
    year: Int,
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }

    // Cargar datos del mes seleccionado
    LaunchedEffect(month, year) {
        Log.d("MonthlyReportScreen", "🚀 Cargando reporte para $month/$year")
        viewModel.selectMonth(month, year)
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
                    text = "Reporte ${getMonthName(month)} $year",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }
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
                        Log.d("MonthlyReportScreen", "🔄 Refresh solicitado")
                        viewModel.selectMonth(month, year)
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
        when (val currentState = monthlyStats) {
            is UiState.Loading -> {
                LoadingSection()
            }

            is UiState.Error -> {
                ErrorSection(
                    message = currentState.message,
                    onRetry = {
                        Log.d("MonthlyReportScreen", "🔄 Usuario solicitó reintentar")
                        viewModel.selectMonth(month, year)
                    }
                )
            }

            is UiState.Success -> {
                MonthlyReportContent(
                    stats = currentState.data,
                    month = month,
                    year = year,
                    onRefresh = { viewModel.selectMonth(month, year) }
                )
            }

            is UiState.Empty -> {
                EmptyMonthlyReport(
                    month = month,
                    year = year,
                    onRefresh = { viewModel.selectMonth(month, year) }
                )
            }
        }
    }

    // Dialog de exportación
    if (showExportDialog) {
        com.e17kapps.iepinpersonal.ui.components.ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                Log.d("MonthlyReportScreen", "📊 Exportando reporte mensual en formato: $format")
                viewModel.exportData(format)
                showExportDialog = false
            }
        )
    }

    // Mostrar error como snackbar
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            Log.e("MonthlyReportScreen", "❌ Error: $error")
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
                text = "Generando reporte mensual...",
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
            text = "Error al generar reporte",
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
private fun MonthlyReportContent(
    stats: MonthlyStats,
    month: Int,
    year: Int,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header del reporte
        item {
            ReportHeaderCard(
                month = month,
                year = year,
                stats = stats
            )
        }

        // Estadísticas principales
        item {
            Text(
                text = "Resumen del Mes",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Pagos",
                    value = formatCurrency(stats.totalPayments),
                    icon = "💰",
                    color = AppColors.SuccessGreen,
                    change = null,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Número de Pagos",
                    value = stats.paymentCount.toString(),
                    icon = "📊",
                    color = AppColors.InfoBlue,
                    change = null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Descuentos",
                    value = formatCurrency(stats.totalDiscounts),
                    icon = "📉",
                    color = AppColors.WarningYellow,
                    change = null,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Total Adelantos",
                    value = formatCurrency(stats.totalAdvances),
                    icon = "💳",
                    color = AppColors.InfoBlue,
                    change = null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Promedio de pagos
        if (stats.paymentCount > 0) {
            item {
                AveragePaymentCard(averagePayment = stats.averagePayment)
            }
        }

        // Análisis temporal
        item {
            TimeAnalysisCard(
                month = month,
                year = year,
                stats = stats
            )
        }

        // Recomendaciones
        item {
            RecommendationsCard(stats = stats)
        }

        // Acciones del reporte
        item {
            ReportActionsCard(
                onRefresh = onRefresh,
                onExport = { /* Se maneja en el parent */ }
            )
        }
    }
}

@Composable
private fun ReportHeaderCard(
    month: Int,
    year: Int,
    stats: MonthlyStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.GradientStart
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(AppColors.GradientStart, AppColors.GradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Reporte Mensual",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                Text(
                    text = "${getMonthName(month)} $year",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Generado el ${System.currentTimeMillis().toDateString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AveragePaymentCard(
    averagePayment: Double
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
                text = "Promedio de Pagos",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatCurrency(averagePayment),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.SuccessGreen
                    )

                    Text(
                        text = "Por pago realizado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = AppColors.SuccessGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun TimeAnalysisCard(
    month: Int,
    year: Int,
    stats: MonthlyStats
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
                text = "Análisis Temporal",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Información del período
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimeInfoItem(
                    label = "Mes",
                    value = getMonthName(month)
                )

                TimeInfoItem(
                    label = "Año",
                    value = year.toString()
                )

                TimeInfoItem(
                    label = "Días",
                    value = getDaysInMonth(month, year).toString()
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = AppColors.DividerLight
            )

            // Promedios
            if (stats.paymentCount > 0) {
                val daysInMonth = getDaysInMonth(month, year)
                val avgPerDay = stats.totalPayments / daysInMonth

                Text(
                    text = "Promedio diario: ${formatCurrency(avgPerDay)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary
                )

                Text(
                    text = "Pagos por día: ${String.format("%.1f", stats.paymentCount.toDouble() / daysInMonth)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TimeInfoItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AppColors.TextPrimary
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun RecommendationsCard(
    stats: MonthlyStats
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
                text = "Recomendaciones",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val recommendations = generateRecommendations(stats)

            recommendations.forEach { recommendation ->
                RecommendationItem(
                    text = recommendation.text,
                    type = recommendation.type,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    text: String,
    type: RecommendationType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = when (type) {
                RecommendationType.INFO -> Icons.Default.Info
                RecommendationType.WARNING -> Icons.Default.Warning
                RecommendationType.SUCCESS -> Icons.Default.CheckCircle
            },
            contentDescription = null,
            tint = when (type) {
                RecommendationType.INFO -> AppColors.InfoBlue
                RecommendationType.WARNING -> AppColors.WarningYellow
                RecommendationType.SUCCESS -> AppColors.SuccessGreen
            },
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReportActionsCard(
    onRefresh: () -> Unit,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualizar")
            }

            Button(
                onClick = onExport,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.GradientStart
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar")
            }
        }
    }
}

@Composable
private fun EmptyMonthlyReport(
    month: Int,
    year: Int,
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
            text = "Sin datos para este mes",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "No hay información disponible para ${getMonthName(month)} $year",
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
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Intentar de nuevo")
        }
    }
}

// Funciones auxiliares
private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Enero"
        2 -> "Febrero"
        3 -> "Marzo"
        4 -> "Abril"
        5 -> "Mayo"
        6 -> "Junio"
        7 -> "Julio"
        8 -> "Agosto"
        9 -> "Septiembre"
        10 -> "Octubre"
        11 -> "Noviembre"
        12 -> "Diciembre"
        else -> "Mes $month"
    }
}

private fun getDaysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, 1) // Calendar.MONTH es 0-based
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun generateRecommendations(stats: MonthlyStats): List<Recommendation> {
    val recommendations = mutableListOf<Recommendation>()

    // Analizar los datos y generar recomendaciones
    if (stats.totalPayments == 0.0) {
        recommendations.add(
            Recommendation(
                "No se registraron pagos este mes. Considera verificar si hay pagos pendientes.",
                RecommendationType.WARNING
            )
        )
    } else {
        recommendations.add(
            Recommendation(
                "Se procesaron ${stats.paymentCount} pagos exitosamente este mes.",
                RecommendationType.SUCCESS
            )
        )
    }

    if (stats.averagePayment > 0) {
        recommendations.add(
            Recommendation(
                "El promedio de pago (${formatCurrency(stats.averagePayment)}) puede usarse como referencia para presupuestos futuros.",
                RecommendationType.INFO
            )
        )
    }

    if (stats.totalDiscounts > stats.totalPayments * 0.1) {
        recommendations.add(
            Recommendation(
                "Los descuentos representan más del 10% de los pagos totales. Revisa las políticas de descuentos.",
                RecommendationType.WARNING
            )
        )
    }

    if (stats.totalAdvances > 0) {
        recommendations.add(
            Recommendation(
                "Se otorgaron adelantos por ${formatCurrency(stats.totalAdvances)}. Monitorea el impacto en el flujo de caja.",
                RecommendationType.INFO
            )
        )
    }

    // Si no hay recomendaciones específicas, agregar una general
    if (recommendations.isEmpty()) {
        recommendations.add(
            Recommendation(
                "Los datos del mes lucen estables. Continúa monitoreando las tendencias.",
                RecommendationType.INFO
            )
        )
    }

    return recommendations
}

// Clases de datos auxiliares
data class Recommendation(
    val text: String,
    val type: RecommendationType
)

enum class RecommendationType {
    INFO,
    WARNING,
    SUCCESS
}