package com.e17kapps.iepinpersonal.ui.screens.discounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.DiscountType
import com.e17kapps.iepinpersonal.domain.model.Employee
import com.e17kapps.iepinpersonal.ui.components.LoadingButton
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiscountScreen(
    employeeId: String = "",
    onNavigateBack: () -> Unit,
    onDiscountAdded: () -> Unit,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val employees by viewModel.employees.collectAsState()

    // Variables locales para el estado de la UI
    var employeeSearchQuery by remember { mutableStateOf("") }
    var showEmployeeResults by remember { mutableStateOf(false) }
    var showDiscountTypeDropdown by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Preseleccionar empleado si se pasa un ID
    LaunchedEffect(employeeId, employees) {
        if (employeeId.isNotBlank() && employees.isNotEmpty()) {
            val employee = employees.firstOrNull { it.id == employeeId }
            employee?.let { viewModel.selectEmployee(it) }
        }
    }

    // Observar el éxito
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onDiscountAdded()
        }
    }

    // Limpiar formulario al entrar
    LaunchedEffect(Unit) {
        viewModel.resetForm()
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
                    text = "Agregar Descuento",
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selección de empleado
            EmployeeSelectionCard(
                employees = employees,
                selectedEmployee = uiState.selectedEmployee,
                searchQuery = employeeSearchQuery,
                onSearchQueryChange = {
                    employeeSearchQuery = it
                    showEmployeeResults = it.isNotEmpty()
                    viewModel.updateEmployeeSearchQuery(it)
                },
                onEmployeeSelected = { employee ->
                    viewModel.selectEmployee(employee)
                    employeeSearchQuery = employee.fullName
                    showEmployeeResults = false
                },
                showResults = showEmployeeResults,
                onResultsVisibilityChange = { showEmployeeResults = it }
            )

            // Información del descuento
            if (uiState.selectedEmployee != null) {
                DiscountInformationCard(
                    uiState = uiState,
                    onAmountChange = viewModel::updateAmount,
                    onDiscountTypeChange = viewModel::updateDiscountType,
                    onReasonChange = viewModel::updateReason,
                    onDescriptionChange = viewModel::updateDescription,
                    showDiscountTypeDropdown = showDiscountTypeDropdown,
                    onDropdownVisibilityChange = { showDiscountTypeDropdown = it }
                )

                // Configuración de fechas
                DateConfigurationCard(
                    uiState = uiState,
                    onIsRecurringChange = viewModel::updateIsRecurring,
                    onStartDateChange = viewModel::updateStartDate,
                    onEndDateChange = viewModel::updateEndDate,
                    showStartDatePicker = showStartDatePicker,
                    showEndDatePicker = showEndDatePicker,
                    onStartDatePickerVisibilityChange = { showStartDatePicker = it },
                    onEndDatePickerVisibilityChange = { showEndDatePicker = it }
                )

                // Botón para agregar
                LoadingButton(
                    onClick = { viewModel.addDiscount() },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState.isLoading,
                    enabled = uiState.selectedEmployee != null &&
                            uiState.amount.isNotBlank() &&
                            uiState.reason.isNotBlank()
                ) {
                    Text("Agregar Descuento")
                }
            }

            // Mostrar mensaje de error si existe
            uiState.errorMessage?.let { errorMessage ->
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeSelectionCard(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEmployeeSelected: (Employee) -> Unit,
    showResults: Boolean,
    onResultsVisibilityChange: (Boolean) -> Unit
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
                text = "Seleccionar Empleado",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )

            when {
                employees.isEmpty() -> {
                    Text(
                        text = "No hay empleados disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary
                    )
                    TextButton(
                        onClick = { /* TODO: Recargar empleados */ }
                    ) {
                        Text("🔄 Recargar empleados")
                    }
                }

                selectedEmployee != null -> {
                    // Mostrar empleado seleccionado
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.primary.copy(alpha = 0.1f)
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
                                    text = selectedEmployee.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = AppColors.primary
                                )
                                Text(
                                    text = "Cargo: ${selectedEmployee.position}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextSecondary
                                )
                            }

                            TextButton(
                                onClick = { onEmployeeSelected(Employee()) }
                            ) {
                                Text("Cambiar")
                            }
                        }
                    }
                }

                else -> {
                    // Campo de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("Buscar empleado (${employees.size} disponibles)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Escribe el nombre del empleado...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    )

                    // Resultados de búsqueda
                    if (showResults && searchQuery.isNotEmpty()) {
                        val filteredEmployees = employees.filter { employee ->
                            employee.fullName.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredEmployees.isNotEmpty()) {
                            Column {
                                filteredEmployees.take(5).forEach { employee ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        onClick = { onEmployeeSelected(employee) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = employee.fullName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                                Text(
                                                    text = employee.position,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = AppColors.TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No se encontraron empleados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscountInformationCard(
    uiState: DiscountUiState,
    onAmountChange: (String) -> Unit,
    onDiscountTypeChange: (DiscountType) -> Unit,
    onReasonChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    showDiscountTypeDropdown: Boolean,
    onDropdownVisibilityChange: (Boolean) -> Unit
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
                text = "Información del Descuento",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )

            // Monto del descuento
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = onAmountChange,
                label = { Text("Monto del Descuento") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                prefix = { Text("S/ ") },
                singleLine = true
            )

            // Tipo de descuento
            ExposedDropdownMenuBox(
                expanded = showDiscountTypeDropdown,
                onExpandedChange = onDropdownVisibilityChange
            ) {
                OutlinedTextField(
                    value = uiState.discountType.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tipo de Descuento") },
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
                    expanded = showDiscountTypeDropdown,
                    onDismissRequest = { onDropdownVisibilityChange(false) }
                ) {
                    DiscountType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                onDiscountTypeChange(type)
                                onDropdownVisibilityChange(false)
                            }
                        )
                    }
                }
            }

            // Razón del descuento
            OutlinedTextField(
                value = uiState.reason,
                onValueChange = onReasonChange,
                label = { Text("Razón del Descuento *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Tardanza del 15 de enero") }
            )

            // Descripción adicional
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Descripción Adicional (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Detalles adicionales sobre el descuento...") },
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun DateConfigurationCard(
    uiState: DiscountUiState,
    onIsRecurringChange: (Boolean) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    showStartDatePicker: Boolean,
    showEndDatePicker: Boolean,
    onStartDatePickerVisibilityChange: (Boolean) -> Unit,
    onEndDatePickerVisibilityChange: (Boolean) -> Unit
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
                text = "Configuración de Fechas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )

            // Switch para descuento recurrente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Descuento Recurrente",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "Se aplicará automáticamente cada mes",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
                Switch(
                    checked = uiState.isRecurring,
                    onCheckedChange = onIsRecurringChange
                )
            }

            // Fecha de inicio
            OutlinedTextField(
                value = formatDate(uiState.startDate),
                onValueChange = { },
                label = { Text("Fecha de Inicio") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { onStartDatePickerVisibilityChange(true) }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                }
            )

            // Fecha de fin (opcional)
            if (!uiState.isRecurring) {
                OutlinedTextField(
                    value = uiState.endDate?.let { formatDate(it) } ?: "",
                    onValueChange = { },
                    label = { Text("Fecha de Fin (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    placeholder = { Text("Sin fecha de fin") },
                    trailingIcon = {
                        Row {
                            if (uiState.endDate != null) {
                                IconButton(onClick = { onEndDateChange(null) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar fecha")
                                }
                            }
                            IconButton(onClick = { onEndDatePickerVisibilityChange(true) }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            }
                        }
                    }
                )
            }

            // Información adicional
            if (uiState.isRecurring) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.info.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AppColors.info,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Este descuento se aplicará automáticamente cada mes hasta que lo desactives.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.info
                        )
                    }
                }
            }
        }
    }

    // Date Pickers (aquí puedes implementar DatePickerDialog cuando esté disponible)
    // Por ahora, dejamos la funcionalidad básica
}

// Función auxiliar para formatear fechas
private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}