package com.e17kapps.iepinpersonal.ui.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.ui.components.LoadingState
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.toCurrency
import com.e17kapps.iepinpersonal.utils.toDateString
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    employeeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPaymentDetail: (String) -> Unit = {},
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val payments by viewModel.payments.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val employees by viewModel.employees.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var showFilterOptions by remember { mutableStateOf(false) }

    // Obtener empleado actual
    val currentEmployee = remember(employees, employeeId) {
        employees.firstOrNull { it.id == employeeId }
    }

    // Filtrar pagos
    val filteredPayments = remember(payments, selectedPaymentMethod, selectedYear) {
        payments.filter { payment ->
            val methodMatch = selectedPaymentMethod?.let { payment.paymentMethod == it } ?: true
            val yearMatch = selectedYear?.let {
                val calendar = Calendar.getInstance().apply { timeInMillis = payment.paymentDate }
                calendar.get(Calendar.YEAR) == it
            } ?: true
            methodMatch && yearMatch
        }.sortedByDescending { it.paymentDate }
    }

    // Cargar datos al iniciar
    LaunchedEffect(employeeId) {
        viewModel.getPaymentsByEmployee(employeeId)
        viewModel.refreshEmployees()
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
                    text = "Historial de Pagos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = if (selectedPaymentMethod != null || selectedYear != null)
                            AppColors.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Employee Info Header
        currentEmployee?.let { employee ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AppColors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = employee.getInitials(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = employee.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = employee.position,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = "Salario: ${employee.baseSalary.toCurrency()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = AppColors.primary
                        )
                    }
                }
            }
        }

        // Filters Section
        if (showFilterOptions) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Payment Method Filter
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextSecondary
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { selectedPaymentMethod = null },
                                label = { Text("Todos") },
                                selected = selectedPaymentMethod == null
                            )
                        }
                        items(PaymentMethod.values()) { method ->
                            FilterChip(
                                onClick = {
                                    selectedPaymentMethod = if (selectedPaymentMethod == method) null else method
                                },
                                label = { Text(method.displayName) },
                                selected = selectedPaymentMethod == method
                            )
                        }
                    }

                    // Year Filter
                    Text(
                        text = "Año",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextSecondary
                    )

                    val availableYears = remember(payments) {
                        payments.map { payment ->
                            Calendar.getInstance().apply { timeInMillis = payment.paymentDate }
                                .get(Calendar.YEAR)
                        }.distinct().sorted().reversed()
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { selectedYear = null },
                                label = { Text("Todos") },
                                selected = selectedYear == null
                            )
                        }
                        items(availableYears) { year ->
                            FilterChip(
                                onClick = {
                                    selectedYear = if (selectedYear == year) null else year
                                },
                                label = { Text(year.toString()) },
                                selected = selectedYear == year
                            )
                        }
                    }

                    // Clear Filters Button
                    if (selectedPaymentMethod != null || selectedYear != null) {
                        TextButton(
                            onClick = {
                                selectedPaymentMethod = null
                                selectedYear = null
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpiar Filtros")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Summary Card
        if (filteredPayments.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        title = "Total Pagado",
                        value = filteredPayments.sumOf { it.amount }.toCurrency(),
                        icon = Icons.Default.AttachMoney,
                        color = AppColors.primary
                    )

                    StatItem(
                        title = "Pagos",
                        value = filteredPayments.size.toString(),
                        icon = Icons.Default.Receipt,
                        color = AppColors.tertiary
                    )

                    StatItem(
                        title = "Último Pago",
                        value = filteredPayments.firstOrNull()?.paymentDate?.toDateString() ?: "N/A",
                        icon = Icons.Default.CalendarToday,
                        color = AppColors.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Content
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = "Cargando historial de pagos...",
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = AppColors.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error al cargar pagos",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.getPaymentsByEmployee(employeeId) }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            filteredPayments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedPaymentMethod != null || selectedYear != null)
                                "No hay pagos con los filtros aplicados"
                            else "No hay pagos registrados",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (selectedPaymentMethod != null || selectedYear != null)
                                "Intenta cambiar o limpiar los filtros"
                            else "Los pagos aparecerán aquí cuando se registren",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPayments) { payment ->
                        PaymentHistoryItem(
                            payment = payment,
                            onClick = { onNavigateToPaymentDetail(payment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun PaymentHistoryItem(
    payment: Payment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Payment Method Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getPaymentMethodColor(payment.paymentMethod).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPaymentMethodIcon(payment.paymentMethod),
                    contentDescription = null,
                    tint = getPaymentMethodColor(payment.paymentMethod),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Payment Period and Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = payment.paymentPeriod.getDisplayText(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = payment.paymentMethod.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }

                    Text(
                        text = payment.amount.toCurrency(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Payment Date and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = payment.paymentDate.toDateString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )

                    PaymentStatusChip(status = payment.status)
                }

                // Notes (if available)
                if (payment.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = payment.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = "Ver detalles",
                tint = AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun PaymentStatusChip(status: PaymentStatus) {
    val (color, containerColor) = when (status) {
        PaymentStatus.COMPLETED -> AppColors.tertiary to AppColors.tertiary.copy(alpha = 0.2f)
        PaymentStatus.PENDING -> AppColors.warning to AppColors.warning.copy(alpha = 0.2f)
        PaymentStatus.CANCELLED -> AppColors.TextSecondary to AppColors.TextSecondary.copy(alpha = 0.2f)
        PaymentStatus.FAILED -> AppColors.error to AppColors.error.copy(alpha = 0.2f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getPaymentMethodIcon(method: PaymentMethod): ImageVector {
    return when (method) {
        PaymentMethod.CASH -> Icons.Default.AttachMoney
        PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
        PaymentMethod.YAPE, PaymentMethod.PLIN, PaymentMethod.OTHER_DIGITAL -> Icons.Default.PhoneAndroid
    }
}

private fun getPaymentMethodColor(method: PaymentMethod): Color {
    return when (method) {
        PaymentMethod.CASH -> AppColors.tertiary
        PaymentMethod.BANK_TRANSFER -> AppColors.primary
        PaymentMethod.YAPE -> Color(0xFF722F8F) // YAPE purple
        PaymentMethod.PLIN -> Color(0xFF00BCD4) // PLIN teal
        PaymentMethod.OTHER_DIGITAL -> AppColors.secondary
    }
}

// Extension function for Employee initials
private fun Employee.getInitials(): String {
    return fullName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
}