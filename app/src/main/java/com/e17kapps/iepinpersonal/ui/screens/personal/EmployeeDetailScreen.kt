package com.e17kapps.iepinpersonal.ui.screens.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.Employee
import com.e17kapps.iepinpersonal.ui.components.LoadingState
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.formatCurrency
import com.e17kapps.iepinpersonal.utils.toDateString
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
    employeeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToPaymentHistory: () -> Unit,
    viewModel: EmployeeViewModel = hiltViewModel()
) {
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Cargar empleado al iniciar
    LaunchedEffect(employeeId) {
        viewModel.selectEmployeeById(employeeId)
    }

    // Mostrar mensajes
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            // Mostrar snackbar de éxito
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            // Mostrar snackbar de error
        }
    }

    if (isLoading && selectedEmployee == null) {
        LoadingState(
            message = "Cargando información del empleado...",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    selectedEmployee?.let { employee ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar personalizado
            TopAppBar(
                title = { Text("Detalles del Empleado") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
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
                                text = { Text("Editar") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Historial de Pagos") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToPaymentHistory()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Payment, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(if (employee.isActive) "Desactivar" else "Activar")
                                },
                                onClick = {
                                    showMenu = false
                                    if (employee.isActive) {
                                        showDeactivateDialog = true
                                    } else {
                                        viewModel.toggleEmployeeStatus(employee.id, true)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        if (employee.isActive) Icons.Default.PersonOff else Icons.Default.PersonAdd,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header con foto y info básica
                EmployeeHeaderCard(employee = employee)

                // Información personal
                PersonalInfoCard(employee = employee)

                // Información laboral
                WorkInfoCard(employee = employee)

                // Información de contacto
                ContactInfoCard(employee = employee)

                // Contacto de emergencia (si existe)
                employee.emergencyContact?.let { emergencyContact ->
                    EmergencyContactCard(emergencyContact = emergencyContact)
                }

                // Estadísticas rápidas
                QuickStatsCard(employee = employee)

                // Acciones rápidas
                QuickActionsCard(
                    employee = employee,
                    onPaymentHistory = onNavigateToPaymentHistory,
                    onEdit = onNavigateToEdit
                )

                // Notas adicionales (si existen)
                if (employee.notes.isNotBlank()) {
                    NotesCard(notes = employee.notes)
                }

                // Información del sistema
                SystemInfoCard(employee = employee)
            }
        }

        // Dialog de confirmación para desactivar
        if (showDeactivateDialog) {
            AlertDialog(
                onDismissRequest = { showDeactivateDialog = false },
                title = { Text("Desactivar Empleado") },
                text = {
                    Text("¿Estás seguro de que deseas desactivar a ${employee.fullName}? Esta acción se puede revertir.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.toggleEmployeeStatus(employee.id, false)
                            showDeactivateDialog = false
                        }
                    ) {
                        Text("Desactivar", color = AppColors.DangerRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeactivateDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    } ?: run {
        // Estado cuando no se encuentra el empleado
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
                text = "Empleado no encontrado",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.TextPrimary
            )

            Text(
                text = "El empleado que buscas no existe o fue eliminado",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onNavigateBack) {
                Text("Regresar")
            }
        }
    }
}

@Composable
private fun EmployeeHeaderCard(employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar del empleado
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = if (employee.isActive)
                            AppColors.GradientStart.copy(alpha = 0.1f)
                        else
                            AppColors.TextSecondary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        color = if (employee.isActive)
                            AppColors.GradientStart
                        else
                            AppColors.TextSecondary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    ),
                    color = if (employee.isActive)
                        AppColors.GradientStart
                    else
                        AppColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre completo
            Text(
                text = employee.fullName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            // Cargo
            Text(
                text = employee.position,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Estado y salario
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estado
                StatusChip(isActive = employee.isActive)

                // Salario
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.SuccessGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = formatCurrency(employee.baseSalary),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.SuccessGreen,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val (backgroundColor, textColor, text, icon) = if (isActive) {
        listOf(
            AppColors.SuccessGreen.copy(alpha = 0.1f),
            AppColors.SuccessGreen,
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon as String, fontSize = 12.sp)
            Text(
                text = text as String,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = textColor as Color
            )
        }
    }
}

@Composable
private fun PersonalInfoCard(employee: Employee) {
    InfoCard(
        title = "Información Personal",
        icon = Icons.Default.Person
    ) {
        InfoRow(label = "DNI", value = employee.dni)
        InfoRow(label = "Nombre", value = employee.name)
        InfoRow(label = "Apellidos", value = employee.lastName)
    }
}

@Composable
private fun WorkInfoCard(employee: Employee) {
    InfoCard(
        title = "Información Laboral",
        icon = Icons.Default.Work
    ) {
        InfoRow(label = "Cargo", value = employee.position)
        InfoRow(label = "Salario Base", value = formatCurrency(employee.baseSalary))
        InfoRow(label = "Fecha de Ingreso", value = employee.startDate.toDateString())
        if (employee.bankAccount.isNotBlank()) {
            InfoRow(label = "Cuenta Bancaria", value = employee.bankAccount)
        }
    }
}

@Composable
private fun ContactInfoCard(employee: Employee) {
    InfoCard(
        title = "Información de Contacto",
        icon = Icons.Default.ContactPhone
    ) {
        InfoRow(label = "Teléfono", value = employee.phone)
        if (employee.email.isNotBlank()) {
            InfoRow(label = "Email", value = employee.email)
        }
        InfoRow(label = "Dirección", value = employee.address)
    }
}

@Composable
private fun EmergencyContactCard(emergencyContact: com.e17kapps.iepinpersonal.domain.model.EmergencyContact) {
    InfoCard(
        title = "Contacto de Emergencia",
        icon = Icons.Default.Emergency
    ) {
        InfoRow(label = "Nombre", value = emergencyContact.name)
        InfoRow(label = "Teléfono", value = emergencyContact.phone)
        InfoRow(label = "Parentesco", value = emergencyContact.relationship)
    }
}

@Composable
private fun QuickStatsCard(employee: Employee) {
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
                text = "Estadísticas Rápidas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    title = "Antigüedad",
                    value = calculateEmployeeAntiquity(employee.startDate),
                    icon = "📅"
                )
                QuickStatItem(
                    title = "Sueldo Mensual",
                    value = formatCurrency(employee.monthlyNet),
                    icon = "💰"
                )
                QuickStatItem(
                    title = "Estado",
                    value = if (employee.isActive) "Activo" else "Inactivo",
                    icon = if (employee.isActive) "✅" else "⏸️"
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    title: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
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
private fun QuickActionsCard(
    employee: Employee,
    onPaymentHistory: () -> Unit,
    onEdit: () -> Unit
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
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    title = "Editar",
                    icon = Icons.Default.Edit,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )

                ActionButton(
                    title = "Historial",
                    icon = Icons.Default.Payment,
                    onClick = onPaymentHistory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.GradientStart
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(title)
    }
}

@Composable
private fun NotesCard(notes: String) {
    InfoCard(
        title = "Notas Adicionales",
        icon = Icons.Default.Note
    ) {
        Text(
            text = notes,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun SystemInfoCard(employee: Employee) {
    InfoCard(
        title = "Información del Sistema",
        icon = Icons.Default.Info
    ) {
        InfoRow(label = "Creado", value = employee.createdAt.toDateString("dd/MM/yyyy HH:mm"))
        InfoRow(label = "Actualizado", value = employee.updatedAt.toDateString("dd/MM/yyyy HH:mm"))
        if (employee.createdBy.isNotBlank()) {
            InfoRow(label = "Creado por", value = employee.createdBy)
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.GradientStart,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.TextPrimary
                )
            }

            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(2f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}

private fun calculateEmployeeAntiquity(startDate: Long): String {
    val startCalendar = Calendar.getInstance().apply {
        timeInMillis = startDate
    }
    val currentCalendar = Calendar.getInstance()

    val years = currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
    val months = currentCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)

    val totalMonths = years * 12 + months

    return when {
        totalMonths < 1 -> "Menos de 1 mes"
        totalMonths < 12 -> "$totalMonths meses"
        years == 1 && months == 0 -> "1 año"
        years == 1 -> "1 año y $months meses"
        months == 0 -> "$years años"
        else -> "$years años y $months meses"
    }
}