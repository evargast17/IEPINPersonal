package com.e17kapps.iepinpersonal.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.Employee
import com.e17kapps.iepinpersonal.domain.model.PaymentMethod
import com.e17kapps.iepinpersonal.ui.components.LoadingButton
import com.e17kapps.iepinpersonal.utils.formatCurrency


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    onNavigateBack: () -> Unit,
    onPaymentAdded: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val employees by viewModel.employees.collectAsState()

    // Variables locales para el estado de la UI
    var employeeSearchQuery by remember { mutableStateOf("") }
    var showEmployeeResults by remember { mutableStateOf(false) }
    var showPaymentMethodDropdown by remember { mutableStateOf(false) }

    // Observar el éxito
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onPaymentAdded()
        }
    }

    // Limpiar formulario al entrar y recargar empleados
    LaunchedEffect(Unit) {
        viewModel.resetForm()
        viewModel.refreshEmployees()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar"
                )
            }

            Text(
                text = "Registrar Pago",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 8.dp, top = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selección de empleado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Empleado",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF374151)
                    )

                    // Mostrar diferentes estados
                    when {
                        uiState.isLoading -> {
                            // Estado de carga
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF667EEA)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cargando empleados...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        employees.isEmpty() && !uiState.isLoading -> {
                            // No hay empleados
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFECA57).copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "⚠️ No hay empleados disponibles",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color(0xFFFECA57)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Primero agrega empleados en la sección 'Personal' antes de registrar pagos.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { viewModel.refreshEmployees() }
                                    ) {
                                        Text("🔄 Recargar empleados")
                                    }
                                }
                            }
                        }

                        else -> {
                            // Campo de búsqueda de empleados
                            OutlinedTextField(
                                value = employeeSearchQuery,
                                onValueChange = {
                                    employeeSearchQuery = it
                                    showEmployeeResults = it.isNotEmpty()
                                },
                                label = { Text("Buscar empleado (${employees.size} disponibles)") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Escribe el nombre del empleado...") }
                            )

                            // Mostrar empleado seleccionado
                            if (uiState.selectedEmployee != null) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = uiState.selectedEmployee!!.fullName,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = Color(0xFF667EEA)
                                            )
                                            Text(
                                                text = "Cargo: ${uiState.selectedEmployee!!.position}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF6B7280)
                                            )
                                            Text(
                                                text = "Sueldo: ${formatCurrency(uiState.selectedEmployee!!.baseSalary)}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = Color(0xFF10B981)
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                viewModel.selectEmployee(Employee()) // Limpiar selección
                                                employeeSearchQuery = ""
                                            }
                                        ) {
                                            Text("Cambiar")
                                        }
                                    }
                                }
                            }

                            // Lista de resultados de búsqueda
                            if (showEmployeeResults && uiState.selectedEmployee == null) {
                                val filteredEmployees = employees.filter { employee ->
                                    employee.fullName.contains(employeeSearchQuery, ignoreCase = true) ||
                                            employee.dni.contains(employeeSearchQuery, ignoreCase = true) ||
                                            employee.position.contains(employeeSearchQuery, ignoreCase = true)
                                }

                                if (filteredEmployees.isNotEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            filteredEmployees.take(5).forEach { employee ->
                                                TextButton(
                                                    onClick = {
                                                        viewModel.selectEmployee(employee)
                                                        employeeSearchQuery = employee.fullName
                                                        showEmployeeResults = false
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.weight(1f),
                                                            horizontalAlignment = Alignment.Start
                                                        ) {
                                                            Text(
                                                                text = employee.fullName,
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    fontWeight = FontWeight.Medium
                                                                ),
                                                                color = Color(0xFF374151)
                                                            )
                                                            Text(
                                                                text = "${employee.position} - ${formatCurrency(employee.baseSalary)}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Color(0xFF6B7280)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Información del pago (solo si hay empleado seleccionado)
            if (uiState.selectedEmployee != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Información del Pago",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF374151)
                        )

                        OutlinedTextField(
                            value = uiState.amount,
                            onValueChange = viewModel::updateAmount,
                            label = { Text("Monto a Pagar") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            prefix = { Text("S/ ") },
                            singleLine = true
                        )

                        // Método de pago
                        Text(
                            text = "Método de Pago",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF374151)
                        )

                        // Dropdown para método de pago
                        ExposedDropdownMenuBox(
                            expanded = showPaymentMethodDropdown,
                            onExpandedChange = { showPaymentMethodDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.paymentMethod.displayName,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Seleccionar método") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = showPaymentMethodDropdown,
                                onDismissRequest = { showPaymentMethodDropdown = false }
                            ) {
                                PaymentMethod.values().forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method.displayName) },
                                        onClick = {
                                            viewModel.updatePaymentMethod(method)
                                            showPaymentMethodDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Detalles específicos del método de pago
                when (uiState.paymentMethod) {
                    PaymentMethod.BANK_TRANSFER -> {
                        BankDetailsCard(
                            bankDetails = uiState.bankDetails,
                            onUpdateBankDetails = viewModel::updateBankDetails
                        )
                    }
                    PaymentMethod.YAPE, PaymentMethod.PLIN -> {
                        DigitalWalletDetailsCard(
                            walletDetails = uiState.digitalWalletDetails,
                            onUpdateWalletDetails = viewModel::updateDigitalWalletDetails
                        )
                    }
                    else -> { /* No additional details needed */ }
                }

                // Notas adicionales
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Observaciones",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF374151)
                        )

                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = viewModel::updateNotes,
                            label = { Text("Notas adicionales (Opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            placeholder = { Text("Detalles adicionales del pago...") }
                        )
                    }
                }
            }

            // Mensaje de error
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF6B6B).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancelar")
                }

                LoadingButton(
                    onClick = { viewModel.processPayment() },
                    modifier = Modifier.weight(1f),
                    isLoading = uiState.isLoading,
                    enabled = !uiState.isLoading &&
                            uiState.selectedEmployee != null &&
                            uiState.amount.isNotBlank() &&
                            uiState.amount.toDoubleOrNull() != null &&
                            uiState.amount.toDoubleOrNull()!! > 0
                ) {
                    Text("Procesar Pago")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BankDetailsCard(
    bankDetails: com.e17kapps.iepinpersonal.domain.model.BankDetails?,
    onUpdateBankDetails: (String?, String?, String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detalles de Transferencia",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF374151)
            )

            OutlinedTextField(
                value = bankDetails?.bankName ?: "",
                onValueChange = { onUpdateBankDetails(it, null, null) },
                label = { Text("Banco") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = bankDetails?.operationNumber ?: "",
                onValueChange = { onUpdateBankDetails(null, null, it) },
                label = { Text("Número de Operación") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = bankDetails?.accountNumber ?: "",
                onValueChange = { onUpdateBankDetails(null, it, null) },
                label = { Text("Cuenta Destino (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )
        }
    }
}

@Composable
private fun DigitalWalletDetailsCard(
    walletDetails: com.e17kapps.iepinpersonal.domain.model.DigitalWalletDetails?,
    onUpdateWalletDetails: (String?, String?, String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detalles de Billetera Digital",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF374151)
            )

            OutlinedTextField(
                value = walletDetails?.phoneNumber ?: "",
                onValueChange = { onUpdateWalletDetails(it, null, null) },
                label = { Text("Número de Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = walletDetails?.operationNumber ?: "",
                onValueChange = { onUpdateWalletDetails(null, it, null) },
                label = { Text("Número de Operación") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = walletDetails?.transactionId ?: "",
                onValueChange = { onUpdateWalletDetails(null, null, it) },
                label = { Text("ID de Transacción (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

