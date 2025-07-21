package com.e17kapps.iepinpersonal.ui.screens.personal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.EmergencyContact
import com.e17kapps.iepinpersonal.ui.components.LoadingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeScreen(
    onNavigateBack: () -> Unit,
    onEmployeeAdded: () -> Unit,
    viewModel: EmployeeViewModel = hiltViewModel()
) {
    val employeeForm by viewModel.employeeForm.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val focusManager = LocalFocusManager.current

    // Observar el éxito
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onEmployeeAdded()
        }
    }

    // Limpiar formulario al entrar
    LaunchedEffect(Unit) {
        viewModel.clearSelection()
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
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Regresar"
                )
            }

            Text(
                text = "Agregar Empleado",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 8.dp, top = 12.dp)
            )
        }

        // Formulario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información Personal
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
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF374151)
                    )

                    OutlinedTextField(
                        value = employeeForm.dni,
                        onValueChange = { viewModel.updateEmployeeForm(dni = it) },
                        label = { Text("DNI") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        supportingText = { Text("8 dígitos") }
                    )

                    OutlinedTextField(
                        value = employeeForm.name,
                        onValueChange = { viewModel.updateEmployeeForm(name = it) },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = employeeForm.lastName,
                        onValueChange = { viewModel.updateEmployeeForm(lastName = it) },
                        label = { Text("Apellidos") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = employeeForm.position,
                        onValueChange = { viewModel.updateEmployeeForm(position = it) },
                        label = { Text("Cargo/Puesto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                }
            }

            // Información Laboral
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
                        text = "Información Laboral",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF374151)
                    )

                    OutlinedTextField(
                        value = if (employeeForm.baseSalary > 0) employeeForm.baseSalary.toString() else "",
                        onValueChange = {
                            val salary = it.toDoubleOrNull() ?: 0.0
                            viewModel.updateEmployeeForm(baseSalary = salary)
                        },
                        label = { Text("Sueldo Base") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        prefix = { Text("S/ ") }
                    )

                    OutlinedTextField(
                        value = employeeForm.bankAccount,
                        onValueChange = { viewModel.updateEmployeeForm(bankAccount = it) },
                        label = { Text("Cuenta Bancaria (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                }
            }

            // Información de Contacto
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
                        text = "Información de Contacto",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF374151)
                    )

                    OutlinedTextField(
                        value = employeeForm.phone,
                        onValueChange = { viewModel.updateEmployeeForm(phone = it) },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = employeeForm.email,
                        onValueChange = { viewModel.updateEmployeeForm(email = it) },
                        label = { Text("Email (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = employeeForm.address,
                        onValueChange = { viewModel.updateEmployeeForm(address = it) },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        maxLines = 2
                    )
                }
            }

            // Contacto de Emergencia
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
                        text = "Contacto de Emergencia (Opcional)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF374151)
                    )

                    OutlinedTextField(
                        value = employeeForm.emergencyContact?.name ?: "",
                        onValueChange = {
                            val current = employeeForm.emergencyContact ?: EmergencyContact()
                            viewModel.updateEmployeeForm(
                                emergencyContact = current.copy(name = it)
                            )
                        },
                        label = { Text("Nombre del Contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = employeeForm.emergencyContact?.phone ?: "",
                        onValueChange = {
                            val current = employeeForm.emergencyContact ?: EmergencyContact()
                            viewModel.updateEmployeeForm(
                                emergencyContact = current.copy(phone = it)
                            )
                        },
                        label = { Text("Teléfono del Contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = employeeForm.emergencyContact?.relationship ?: "",
                        onValueChange = {
                            val current = employeeForm.emergencyContact ?: EmergencyContact()
                            viewModel.updateEmployeeForm(
                                emergencyContact = current.copy(relationship = it)
                            )
                        },
                        label = { Text("Parentesco") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        placeholder = { Text("Ej: Padre, Madre, Hermano/a, etc.") }
                    )
                }
            }

            // Notas Adicionales
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
                        text = "Notas Adicionales",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF374151)
                    )

                    OutlinedTextField(
                        value = employeeForm.notes,
                        onValueChange = { viewModel.updateEmployeeForm(notes = it) },
                        label = { Text("Notas (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        maxLines = 3,
                        placeholder = { Text("Información adicional del empleado...") }
                    )
                }
            }

            // Mensaje de error
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF6B6B).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = errorMessage!!,
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
                    enabled = !isLoading
                ) {
                    Text("Cancelar")
                }

                LoadingButton(
                    onClick = { viewModel.addEmployee() },
                    modifier = Modifier.weight(1f),
                    isLoading = isLoading,
                    enabled = !isLoading &&
                            employeeForm.dni.isNotBlank() &&
                            employeeForm.name.isNotBlank() &&
                            employeeForm.lastName.isNotBlank() &&
                            employeeForm.position.isNotBlank() &&
                            employeeForm.baseSalary > 0
                ) {
                    Text("Guardar Empleado")
                }
            }

            // Espacio adicional al final
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}