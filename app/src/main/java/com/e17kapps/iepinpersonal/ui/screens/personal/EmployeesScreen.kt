package com.e17kapps.iepinpersonal.ui.screens.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.ui.components.EmployeeCard
import com.e17kapps.iepinpersonal.ui.components.EmptyState
import com.e17kapps.iepinpersonal.ui.components.LoadingState
import com.e17kapps.iepinpersonal.ui.components.SearchBar
import com.e17kapps.iepinpersonal.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    onNavigateToAddEmployee: () -> Unit,
    onNavigateToEmployeeDetail: (String) -> Unit,
    viewModel: EmployeeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshEmployees()
    }

    // Mostrar mensajes de error o éxito
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            // Aquí podrías mostrar un Snackbar
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            // Aquí podrías mostrar un Snackbar de éxito
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::searchEmployees,
                placeholder = "Buscar empleado...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Estadísticas rápidas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    title = "Total",
                    value = uiState.employees.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Activos",
                    value = uiState.employees.count { it.isActive }.toString(),
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Nómina",
                    value = formatCurrency(uiState.employees.filter { it.isActive }.sumOf { it.baseSalary }),
                    modifier = Modifier.weight(1f)
                )
            }

            // Lista de empleados
            when {
                isLoading && uiState.employees.isEmpty() -> {
                    LoadingState(
                        message = "Cargando empleados...",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.employees.isEmpty() && !isLoading -> {
                    EmptyState(
                        icon = "👥",
                        title = "Sin empleados",
                        message = "Agrega tu primer empleado para comenzar",
                        actionText = "Agregar Empleado",
                        onAction = onNavigateToAddEmployee,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.filteredEmployees.isEmpty() && uiState.searchQuery.isNotBlank() -> {
                    EmptyState(
                        icon = "🔍",
                        title = "Sin resultados",
                        message = "No se encontraron empleados con '${uiState.searchQuery}'",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // Espacio para FAB
                    ) {
                        val employeesToShow = if (uiState.searchQuery.isBlank()) {
                            uiState.employees
                        } else {
                            uiState.filteredEmployees
                        }

                        items(employeesToShow) { employee ->
                            EmployeeCard(
                                employee = employee,
                                onClick = { onNavigateToEmployeeDetail(employee.id) },
                                onToggleStatus = {
                                    if (employee.isActive) {
                                        viewModel.deactivateEmployee(employee.id)
                                    } else {
                                        viewModel.reactivateEmployee(employee.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToAddEmployee,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF667EEA), // GradientStart
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar empleado"
            )
        }

        // Loading overlay
        if (isLoading && uiState.employees.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF667EEA)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Actualizando...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF374151) // TextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280), // TextSecondary
                textAlign = TextAlign.Center
            )
        }
    }
}

