package com.e17kapps.iepinpersonal.ui.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.ui.components.LoadingState
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.toCurrency
import com.e17kapps.iepinpersonal.utils.toDateTimeString


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    paymentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit = {},
    onNavigateToEmployee: (String) -> Unit = {},
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val selectedPayment by viewModel.selectedPayment.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Obtener empleado relacionado
    val relatedEmployee = remember(selectedPayment, employees) {
        selectedPayment?.let { payment ->
            employees.firstOrNull { it.id == payment.employeeId }
        }
    }

    // Cargar datos al iniciar
    LaunchedEffect(paymentId) {
        viewModel.getPaymentById(paymentId)
        viewModel.refreshEmployees()
    }

    // Observar mensajes
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
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
                Text(
                    text = "Detalle del Pago",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar Pago") },
                        onClick = {
                            showMenu = false
                            onNavigateToEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Eliminar Pago") },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = AppColors.error
                            )
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        when {
            uiState.isLoading && selectedPayment == null -> {
                LoadingState(
                    message = "Cargando detalles del pago...",
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
                            text = "Error al cargar el pago",
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
                            onClick = { viewModel.getPaymentById(paymentId) }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            selectedPayment == null -> {
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
                            text = "Pago no encontrado",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "No se pudo encontrar el pago solicitado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            else -> {
                PaymentDetailContent(
                    payment = selectedPayment!!,
                    relatedEmployee = relatedEmployee,
                    onNavigateToEmployee = onNavigateToEmployee,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Dialog de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Pago") },
            text = {
                Text("¿Estás seguro de que deseas eliminar este pago? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePayment(paymentId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PaymentDetailContent(
    payment: Payment,
    relatedEmployee: Employee?,
    onNavigateToEmployee: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Payment Amount Header
        PaymentAmountCard(payment = payment)

        // Employee Information
        relatedEmployee?.let { employee ->
            EmployeeInfoCard(
                employee = employee,
                onClick = { onNavigateToEmployee(employee.id) }
            )
        }

        // Payment Information
        PaymentInfoCard(payment = payment)

        // Payment Method Details
        PaymentMethodDetailsCard(payment = payment)

        // Payment Period
        PaymentPeriodCard(payment = payment)

        // Additional Information
        if (payment.notes.isNotBlank() || payment.discounts.isNotEmpty() || payment.advances.isNotEmpty()) {
            AdditionalInfoCard(payment = payment)
        }
    }
}

@Composable
private fun PaymentAmountCard(payment: Payment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = payment.amount.toCurrency(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = AppColors.primary
            )

            Text(
                text = "Monto del Pago",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            PaymentStatusChip(status = payment.status)
        }
    }
}

@Composable
private fun EmployeeInfoCard(
    employee: Employee,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppColors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.getInitials(),
                    style = MaterialTheme.typography.titleMedium.copy(
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

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver empleado",
                tint = AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun PaymentInfoCard(payment: Payment) {
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
                text = "Información del Pago",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                label = "Fecha de Pago",
                value = payment.paymentDate.toDateTimeString(),
                icon = Icons.Default.CalendarToday
            )

            InfoRow(
                label = "ID del Pago",
                value = payment.id.takeIf { it.isNotBlank() } ?: "N/A",
                icon = Icons.Default.Tag
            )

            InfoRow(
                label = "Fecha de Registro",
                value = payment.createdAt.toDateTimeString(),
                icon = Icons.Default.Schedule
            )

            if (payment.createdBy.isNotBlank()) {
                InfoRow(
                    label = "Registrado por",
                    value = payment.createdBy,
                    icon = Icons.Default.Person
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodDetailsCard(payment: Payment) {
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
                text = "Método de Pago",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(getPaymentMethodColor(payment.paymentMethod).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPaymentMethodIcon(payment.paymentMethod),
                        contentDescription = null,
                        tint = getPaymentMethodColor(payment.paymentMethod),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = payment.paymentMethod.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = AppColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detalles específicos del método de pago
            when (payment.paymentMethod) {
                PaymentMethod.BANK_TRANSFER -> {
                    payment.bankDetails?.let { details ->
                        BankDetailsSection(details)
                    }
                }
                PaymentMethod.YAPE, PaymentMethod.PLIN, PaymentMethod.OTHER_DIGITAL -> {
                    payment.digitalWalletDetails?.let { details ->
                        DigitalWalletDetailsSection(details)
                    }
                }
                PaymentMethod.CASH -> {
                    Text(
                        text = "Pago realizado en efectivo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BankDetailsSection(bankDetails: BankDetails) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow(
            label = "Banco",
            value = bankDetails.bankName,
            icon = Icons.Default.AccountBalance
        )

        if (bankDetails.accountNumber.isNotBlank()) {
            InfoRow(
                label = "Número de Cuenta",
                value = bankDetails.accountNumber,
                icon = Icons.Default.CreditCard
            )
        }

        InfoRow(
            label = "Número de Operación",
            value = bankDetails.operationNumber,
            icon = Icons.Default.Receipt
        )

        InfoRow(
            label = "Fecha de Transferencia",
            value = bankDetails.transferDate.toDateTimeString(),
            icon = Icons.Default.Schedule
        )
    }
}

@Composable
private fun DigitalWalletDetailsSection(walletDetails: DigitalWalletDetails) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow(
            label = "Número de Teléfono",
            value = walletDetails.phoneNumber,
            icon = Icons.Default.Phone
        )

        InfoRow(
            label = "Número de Operación",
            value = walletDetails.operationNumber,
            icon = Icons.Default.Receipt
        )

        if (walletDetails.transactionId.isNotBlank()) {
            InfoRow(
                label = "ID de Transacción",
                value = walletDetails.transactionId,
                icon = Icons.Default.Tag
            )
        }
    }
}

@Composable
private fun PaymentPeriodCard(payment: Payment) {
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
                text = "Período de Pago",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                label = "Período",
                value = payment.paymentPeriod.getDisplayText(),
                icon = Icons.Default.DateRange
            )

            InfoRow(
                label = "Mes",
                value = getMonthName(payment.paymentPeriod.month),
                icon = Icons.Default.CalendarMonth
            )

            InfoRow(
                label = "Año",
                value = payment.paymentPeriod.year.toString(),
                icon = Icons.Default.CalendarToday
            )
        }
    }
}

@Composable
private fun AdditionalInfoCard(payment: Payment) {
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
                text = "Información Adicional",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notas
            if (payment.notes.isNotBlank()) {
                InfoRow(
                    label = "Notas",
                    value = payment.notes,
                    icon = Icons.AutoMirrored.Filled.Note
                )
            }

            // Descuentos
            if (payment.discounts.isNotEmpty()) {
                InfoRow(
                    label = "Descuentos",
                    value = "${payment.discounts.size} descuento(s) - ${payment.totalDiscounts.toCurrency()}",
                    icon = Icons.Default.RemoveCircle
                )
            }

            // Adelantos
            if (payment.advances.isNotEmpty()) {
                InfoRow(
                    label = "Adelantos",
                    value = "${payment.advances.size} adelanto(s) - ${payment.totalAdvances.toCurrency()}",
                    icon = Icons.Default.ArrowUpward
                )
            }

            // Monto neto
            if (payment.totalDiscounts > 0 || payment.totalAdvances > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(
                    label = "Monto Neto",
                    value = payment.netAmount.toCurrency(),
                    icon = Icons.Default.Calculate,
                    valueColor = AppColors.primary,
                    valueWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = AppColors.TextPrimary,
    valueWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = valueWeight
                ),
                color = valueColor
            )
        }
    }
}

@Composable
private fun PaymentStatusChip(status: PaymentStatus) {
    val (color, containerColor) = when (status) {
        PaymentStatus.COMPLETED -> AppColors.PaymentCompleted to AppColors.PaymentCompleted.copy(alpha = 0.2f)
        PaymentStatus.PENDING -> AppColors.PaymentPending to AppColors.PaymentPending.copy(alpha = 0.2f)
        PaymentStatus.CANCELLED -> AppColors.TextSecondary to AppColors.TextSecondary.copy(alpha = 0.2f)
        PaymentStatus.FAILED -> AppColors.PaymentFailed to AppColors.PaymentFailed.copy(alpha = 0.2f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// Funciones auxiliares
private fun getPaymentMethodIcon(method: PaymentMethod): ImageVector {
    return when (method) {
        PaymentMethod.CASH -> Icons.Default.AttachMoney
        PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
        PaymentMethod.YAPE, PaymentMethod.PLIN, PaymentMethod.OTHER_DIGITAL -> Icons.Default.PhoneAndroid
    }
}

private fun getPaymentMethodColor(method: PaymentMethod): Color {
    return when (method) {
        PaymentMethod.CASH -> AppColors.CashColor
        PaymentMethod.BANK_TRANSFER -> AppColors.TransferColor
        PaymentMethod.YAPE -> AppColors.YapeColor
        PaymentMethod.PLIN -> AppColors.PlinColor
        PaymentMethod.OTHER_DIGITAL -> AppColors.secondary
    }
}

private fun getMonthName(month: Int): String {
    val months = arrayOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    return if (month in 1..12) months[month - 1] else "Mes $month"
}

// Extension function for Employee initials
private fun Employee.getInitials(): String {
    return fullName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
}