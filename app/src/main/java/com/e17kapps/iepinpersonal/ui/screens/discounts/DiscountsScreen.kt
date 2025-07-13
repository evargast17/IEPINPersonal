package com.e17kapps.iepinpersonal.ui.screens.discounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountScreen(
    onNavigateToAddDiscount: (String) -> Unit,
    onNavigateToDiscountDetail: (String) -> Unit = {},
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val discounts by viewModel.discounts.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedEmployeeFilter by remember { mutableStateOf<Employee?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<DiscountType?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var showActiveOnly by remember { mutableStateOf(false) }

    // Filtrar descuentos
    val filteredDiscounts = remember(discounts, selectedEmployeeFilter, selectedTypeFilter, showActiveOnly) {
        discounts.filter { discount ->
            val employeeMatch = selectedEmployeeFilter?.let { discount.employeeId == it.id } ?: true
            val typeMatch = selectedTypeFilter?.let { discount.type == it } ?: true
            val activeMatch = if (showActiveOnly) discount.isActive else true
            employeeMatch && typeMatch && activeMatch
        }
    }

    // Estadísticas
    val totalDiscounts = remember(filteredDiscounts) {
        filteredDiscounts.filter { it.isActive }.sumOf { it.amount }
    }

    val activeDiscountsCount = remember(filteredDiscounts) {
        filteredDiscounts.count { it.isActive }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header con estadísticas
        DiscountStatsHeader(
            totalAmount = totalDiscounts,
            activeCount = activeDiscountsCount,
            totalCount = filteredDiscounts.size
        )

        // Controles y filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Descuentos Registrados",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )

            Row {
                OutlinedButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filtros")
                }
            }
        }

        // Panel de filtros
        if (showFilters) {
            FilterPanel(
                employees = employees,
                selectedEmployee = selectedEmployeeFilter,
                selectedType = selectedTypeFilter,
                showActiveOnly = showActiveOnly,
                onEmployeeSelected = { selectedEmployeeFilter = it },
                onTypeSelected = { selectedTypeFilter = it },
                onActiveOnlyChanged = { showActiveOnly = it },
                onClearFilters = {
                    selectedEmployeeFilter = null
                    selectedTypeFilter = null
                    showActiveOnly = false
                }
            )
        }

        // Lista de descuentos
        when {
            uiState.isLoading && discounts.isEmpty() -> {
                LoadingState(
                    message = "Cargando descuentos...",
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
                            text = "Error al cargar descuentos",
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
                        Button(onClick = { viewModel.loadDiscounts() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            filteredDiscounts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircle,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedEmployeeFilter != null || selectedTypeFilter != null)
                                "No hay descuentos con los filtros aplicados"
                            else "No hay descuentos registrados",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (selectedEmployeeFilter != null || selectedTypeFilter != null)
                                "Intenta cambiar o limpiar los filtros"
                            else "Los descuentos aparecerán aquí cuando se registren",
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
                    items(filteredDiscounts) { discount ->
                        DiscountCard(
                            discount = discount,
                            onClick = { onNavigateToDiscountDetail(discount.id) },
                            onToggleStatus = { viewModel.toggleDiscountStatus(discount.id, !discount.isActive) }
                        )
                    }
                }
            }
        }
    }

    // FAB para agregar descuento
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = {
                // Navegar a agregar descuento con empleado predeterminado si hay filtro
                onNavigateToAddDiscount(selectedEmployeeFilter?.id ?: "")
            },
            modifier = Modifier.padding(16.dp),
            containerColor = AppColors.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar Descuento",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun DiscountStatsHeader(
    totalAmount: Double,
    activeCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                title = "Total Descuentos",
                value = totalAmount.toCurrency(),
                icon = Icons.Default.RemoveCircle,
                color = AppColors.error
            )

            StatItem(
                title = "Activos",
                value = activeCount.toString(),
                icon = Icons.Default.CheckCircle,
                color = AppColors.success
            )

            StatItem(
                title = "Total",
                value = totalCount.toString(),
                icon = Icons.Default.List,
                color = AppColors.info
            )
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
private fun FilterPanel(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    selectedType: DiscountType?,
    showActiveOnly: Boolean,
    onEmployeeSelected: (Employee?) -> Unit,
    onTypeSelected: (DiscountType?) -> Unit,
    onActiveOnlyChanged: (Boolean) -> Unit,
    onClearFilters: () -> Unit
) {
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

            // Filtro por empleado
            Text(
                text = "Empleado",
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
                        onClick = { onEmployeeSelected(null) },
                        label = { Text("Todos") },
                        selected = selectedEmployee == null
                    )
                }
                items(employees) { employee ->
                    FilterChip(
                        onClick = {
                            onEmployeeSelected(if (selectedEmployee == employee) null else employee)
                        },
                        label = { Text(employee.fullName) },
                        selected = selectedEmployee == employee
                    )
                }
            }

            // Filtro por tipo
            Text(
                text = "Tipo de Descuento",
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
                        onClick = { onTypeSelected(null) },
                        label = { Text("Todos") },
                        selected = selectedType == null
                    )
                }
                items(DiscountType.values()) { type ->
                    FilterChip(
                        onClick = {
                            onTypeSelected(if (selectedType == type) null else type)
                        },
                        label = { Text(type.displayName) },
                        selected = selectedType == type
                    )
                }
            }

            // Switch para mostrar solo activos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solo descuentos activos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary
                )
                Switch(
                    checked = showActiveOnly,
                    onCheckedChange = onActiveOnlyChanged
                )
            }

            // Botón para limpiar filtros
            if (selectedEmployee != null || selectedType != null || showActiveOnly) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onClearFilters,
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

@Composable
private fun DiscountCard(
    discount: Discount,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit
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
            // Icono del tipo de descuento
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getDiscountTypeColor(discount.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getDiscountTypeIcon(discount.type),
                    contentDescription = null,
                    tint = getDiscountTypeColor(discount.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Empleado y monto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = discount.employeeName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = discount.type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }

                    Text(
                        text = discount.amount.toCurrency(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Razón y fecha
                Text(
                    text = discount.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Desde: ${discount.startDate.toDateString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (discount.isRecurring) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Recurrente",
                                tint = AppColors.info,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        DiscountStatusChip(
                            isActive = discount.isActive,
                            onClick = onToggleStatus
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscountStatusChip(
    isActive: Boolean,
    onClick: () -> Unit
) {
    val (color, containerColor, text) = if (isActive) {
        Triple(AppColors.success, AppColors.success.copy(alpha = 0.2f), "Activo")
    } else {
        Triple(AppColors.TextSecondary, AppColors.TextSecondary.copy(alpha = 0.2f), "Inactivo")
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Funciones auxiliares
private fun getDiscountTypeIcon(type: DiscountType): ImageVector {
    return when (type) {
        DiscountType.TARDINESS -> Icons.Default.Schedule
        DiscountType.ABSENCE -> Icons.Default.PersonOff
        DiscountType.LOAN_PAYMENT -> Icons.Default.CreditCard
        DiscountType.ADVANCE_DEDUCTION -> Icons.Default.ArrowDownward
        DiscountType.UNIFORM -> Icons.Default.ShoppingBag
        DiscountType.EQUIPMENT -> Icons.Default.Build
        DiscountType.INSURANCE -> Icons.Default.Security
        DiscountType.OTHER -> Icons.Default.RemoveCircle
    }
}

private fun getDiscountTypeColor(type: DiscountType): Color {
    return when (type) {
        DiscountType.TARDINESS -> Color(0xFFFF9800) // Orange
        DiscountType.ABSENCE -> Color(0xFFF44336) // Red
        DiscountType.LOAN_PAYMENT -> Color(0xFF9C27B0) // Purple
        DiscountType.ADVANCE_DEDUCTION -> Color(0xFF673AB7) // Deep Purple
        DiscountType.UNIFORM -> Color(0xFF2196F3) // Blue
        DiscountType.EQUIPMENT -> Color(0xFF607D8B) // Blue Grey
        DiscountType.INSURANCE -> Color(0xFF4CAF50) // Green
        DiscountType.OTHER -> Color(0xFF795548) // Brown
    }
}