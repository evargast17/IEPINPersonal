package com.e17kapps.iepinpersonal.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.formatCurrency
import com.e17kapps.iepinpersonal.utils.toDateString


@Composable
fun PeriodSelector(
    selectedPeriod: String,
    selectedMonth: Int,
    selectedYear: Int,
    onPeriodChange: (String) -> Unit,
    onMonthYearChange: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Período de Análisis",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Selector de tipo de período
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodChip(
                    text = "Mensual",
                    isSelected = selectedPeriod == "monthly",
                    onClick = { onPeriodChange("monthly") }
                )
                PeriodChip(
                    text = "Trimestral",
                    isSelected = selectedPeriod == "quarterly",
                    onClick = { onPeriodChange("quarterly") }
                )
                PeriodChip(
                    text = "Anual",
                    isSelected = selectedPeriod == "yearly",
                    onClick = { onPeriodChange("yearly") }
                )
            }

            if (selectedPeriod == "monthly") {
                Spacer(modifier = Modifier.height(12.dp))

                // Selector de mes y año
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${getMonthName(selectedMonth)} $selectedYear",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextPrimary
                    )

                    Row {
                        IconButton(
                            onClick = {
                                val newMonth = if (selectedMonth == 1) 12 else selectedMonth - 1
                                val newYear = if (selectedMonth == 1) selectedYear - 1 else selectedYear
                                onMonthYearChange(newMonth, newYear)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Mes anterior",
                                tint = AppColors.GradientStart
                            )
                        }

                        IconButton(
                            onClick = {
                                val newMonth = if (selectedMonth == 12) 1 else selectedMonth + 1
                                val newYear = if (selectedMonth == 12) selectedYear + 1 else selectedYear
                                onMonthYearChange(newMonth, newYear)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Mes siguiente",
                                tint = AppColors.GradientStart
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { Text(text) },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.GradientStart,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = AppColors.TextSecondary
        )
    )
}

@Composable
fun PaymentMethodChart(
    distribution: List<PaymentMethodStats>
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
                text = "Métodos de Pago",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            distribution.forEach { methodStats ->
                PaymentMethodItem(
                    methodStats = methodStats,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(
    methodStats: PaymentMethodStats,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (methodStats.percentage / 100f).toFloat(),
        label = "progress"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = getPaymentMethodColor(methodStats.method),
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = methodStats.method.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${String.format("%.1f", methodStats.percentage)}%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.TextPrimary
                )

                Text(
                    text = formatCurrency(methodStats.totalAmount),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Barra de progreso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    color = AppColors.DividerLight,
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .background(
                        color = getPaymentMethodColor(methodStats.method),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
fun MonthlyComparisonCard(
    comparison: MonthlyComparison
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
                text = "Comparación Mensual",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mes actual
                ComparisonColumn(
                    title = "Este Mes",
                    amount = comparison.currentMonth.totalPayments,
                    month = comparison.currentMonth.month,
                    year = comparison.currentMonth.year,
                    isCurrentMonth = true
                )

                // Indicador de cambio
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val changeColor = when {
                        comparison.percentageChange > 0 -> AppColors.success
                        comparison.percentageChange < 0 -> AppColors.error
                        else -> AppColors.TextSecondary
                    }

                    val changeIcon = when {
                        comparison.percentageChange > 0 -> Icons.AutoMirrored.Filled.TrendingUp
                        comparison.percentageChange < 0 -> Icons.AutoMirrored.Filled.TrendingDown
                        else -> Icons.AutoMirrored.Filled.TrendingFlat
                    }

                    Icon(
                        imageVector = changeIcon,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(32.dp)
                    )

                    Text(
                        text = "${String.format("%.1f", kotlin.math.abs(comparison.percentageChange))}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = changeColor
                    )

                    Text(
                        text = if (comparison.percentageChange >= 0) "más" else "menos",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }

                // Mes anterior
                ComparisonColumn(
                    title = "Mes Anterior",
                    amount = comparison.previousMonth.totalPayments,
                    month = comparison.previousMonth.month,
                    year = comparison.previousMonth.year,
                    isCurrentMonth = false
                )
            }
        }
    }
}

@Composable
private fun ComparisonColumn(
    title: String,
    amount: Double,
    month: Int,
    year: Int,
    isCurrentMonth: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )

        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (isCurrentMonth) AppColors.GradientStart else AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${getMonthName(month)} $year",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
fun EmployeeStatCard(
    employee: Employee,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del empleado
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = AppColors.GradientStart.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.GradientStart
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del empleado
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = employee.fullName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = employee.position,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Salario: ${formatCurrency(employee.baseSalary)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextHint
                )
            }

            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (employee.isActive) AppColors.success else AppColors.error,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Flecha de navegación
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver estadísticas",
                tint = AppColors.TextHint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ActivityCard(
    activity: ActivityItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de la actividad
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getActivityColor(activity.type).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.icon,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la actividad
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (activity.description.isNotBlank()) {
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = activity.timestamp.toDateString("dd/MM/yyyy HH:mm"),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextHint
                )
            }

            // Monto si está disponible
            if (activity.amount != null) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCurrency(activity.amount),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.success
                    )
                }
            }
        }
    }
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Exportar Estadísticas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Selecciona el formato de exportación:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Opciones de exportación
                ExportOption(
                    title = "Excel (.xlsx)",
                    description = "Hoja de cálculo con todas las estadísticas",
                    icon = Icons.Default.TableChart,
                    onClick = { onExport("xlsx") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExportOption(
                    title = "PDF",
                    description = "Reporte completo en formato PDF",
                    icon = Icons.Default.PictureAsPdf,
                    onClick = { onExport("pdf") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExportOption(
                    title = "CSV",
                    description = "Datos en formato separado por comas",
                    icon = Icons.Default.Description,
                    onClick = { onExport("csv") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar",
                    color = AppColors.TextSecondary
                )
            }
        }
    )
}

@Composable
private fun ExportOption(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.GradientStart,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = AppColors.TextPrimary
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.TextHint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Funciones auxiliares
private fun getPaymentMethodColor(method: PaymentMethod): Color {
    return when (method) {
        PaymentMethod.CASH -> AppColors.CashColor
        PaymentMethod.BANK_TRANSFER -> AppColors.TransferColor
        PaymentMethod.YAPE -> AppColors.YapeColor
        PaymentMethod.PLIN -> AppColors.PlinColor
        PaymentMethod.OTHER_DIGITAL -> AppColors.info
    }
}

private fun getActivityColor(type: ActivityType): Color {
    return when (type) {
        ActivityType.PAYMENT -> AppColors.success
        ActivityType.EMPLOYEE_ADDED -> AppColors.info
        ActivityType.DISCOUNT_APPLIED -> AppColors.warning
        ActivityType.ADVANCE_REQUESTED -> AppColors.info
        ActivityType.ADVANCE_APPROVED -> AppColors.success
        ActivityType.EMPLOYEE_UPDATED -> AppColors.info
    }
}

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