package com.e17kapps.iepinpersonal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e17kapps.iepinpersonal.domain.model.Employee
import com.e17kapps.iepinpersonal.utils.formatCurrency


// ============================================================================
// COMPONENTES ORIGINALES DE EMPLEADOS
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF9CA3AF) // TextHint
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF6B7280) // TextSecondary
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE5E7EB), // DividerLight
            focusedBorderColor = Color(0xFF667EEA) // GradientStart
        )
    )
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
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
            // Avatar
            EmployeeAvatar(
                employee = employee,
                size = 48.dp,
                backgroundColor = if (employee.isActive)
                    Color(0xFF667EEA).copy(alpha = 0.1f)
                else
                    Color(0xFF6B7280).copy(alpha = 0.1f),
                textColor = if (employee.isActive)
                    Color(0xFF667EEA)
                else
                    Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del empleado
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = employee.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF374151), // TextPrimary
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (employee.isActive)
                                    Color(0xFF10B981)
                                else
                                    Color(0xFF6B7280),
                                shape = CircleShape
                            )
                    )
                }

                Text(
                    text = employee.position,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280), // TextSecondary
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DNI: ${employee.dni}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF), // TextHint
                        modifier = Modifier.weight(1f)
                    )

                    if (employee.phone.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF9CA3AF) // TextHint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Salario y menú
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCurrency(employee.baseSalary),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF10B981) // success
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF6B7280) // TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (employee.isActive) "Desactivar" else "Activar"
                                )
                            },
                            onClick = {
                                onToggleStatus()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// NUEVOS COMPONENTES DE BÚSQUEDA DE EMPLEADOS
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<Employee>,
    isSearching: Boolean,
    showResults: Boolean,
    onEmployeeSelected: (Employee) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar empleado por nombre o DNI",
    minCharacters: Int = 3,
    maxResults: Int = 5,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        // Campo de búsqueda
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Buscar Empleado") },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF6B7280)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }
            ),
            singleLine = true
        )

        // Resultados de búsqueda
        if (showResults) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                when {
                    isSearching -> {
                        SearchLoadingState()
                    }

                    query.length < minCharacters -> {
                        SearchHint(minCharacters = minCharacters)
                    }

                    searchResults.isEmpty() -> {
                        EmptySearchResults(query = query)
                    }

                    else -> {
                        SearchResultsList(
                            results = searchResults.take(maxResults),
                            totalResults = searchResults.size,
                            maxResults = maxResults,
                            onEmployeeSelected = onEmployeeSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeSelectionCard(
    employee: Employee,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            EmployeeAvatar(
                employee = employee,
                size = 40.dp,
                backgroundColor = Color(0xFF667EEA).copy(alpha = 0.2f),
                textColor = Color(0xFF667EEA)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del empleado
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF374151)
                )
                Text(
                    text = "${employee.position} • ${formatCurrency(employee.baseSalary)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "DNI: ${employee.dni}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF)
                )
            }

            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Cambiar empleado",
                    tint = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun EmployeeAvatar(
    employee: Employee,
    size: androidx.compose.ui.unit.Dp,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = employee.name.take(1).uppercase(),
            style = when {
                size <= 32.dp -> MaterialTheme.typography.labelLarge
                size <= 40.dp -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleLarge
            }.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@Composable
private fun SearchLoadingState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = Color(0xFF667EEA)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Buscando...",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun SearchHint(minCharacters: Int) {
    Text(
        text = "Escribe al menos $minCharacters caracteres para buscar",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF6B7280),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun EmptySearchResults(query: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔍",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No se encontraron empleados",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF374151),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Intenta con \"${query.take(10)}${if (query.length > 10) "..." else ""}\" o revisa la ortografía",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchResultsList(
    results: List<Employee>,
    totalResults: Int,
    maxResults: Int,
    onEmployeeSelected: (Employee) -> Unit
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 250.dp)
    ) {
        items(results) { employee ->
            EmployeeSearchResultItem(
                employee = employee,
                onClick = { onEmployeeSelected(employee) }
            )
        }

        if (totalResults > maxResults) {
            item {
                Text(
                    text = "Y ${totalResults - maxResults} resultados más...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmployeeSearchResultItem(
    employee: Employee,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        EmployeeAvatar(
            employee = employee,
            size = 32.dp,
            backgroundColor = Color(0xFF667EEA).copy(alpha = 0.1f),
            textColor = Color(0xFF667EEA)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Información del empleado
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = employee.fullName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF374151),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${employee.position} • DNI: ${employee.dni}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Salario
        Text(
            text = formatCurrency(employee.baseSalary),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF10B981)
        )
    }
}

// ============================================================================
// COMPONENTES DE ESTADO GENERALES
// ============================================================================

@Composable
fun EmptyState(
    icon: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF374151), // TextPrimary
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280), // TextSecondary
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 32.dp)
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA) // GradientStart
                )
            ) {
                Text(text = actionText)
            }
        }
    }
}

@Composable
fun LoadingState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF667EEA), // GradientStart
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280), // TextSecondary
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================================
// VERSIONES COMPACTAS Y ESPECIALIZADAS
// ============================================================================

@Composable
fun CompactEmployeeSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<Employee>,
    isSearching: Boolean,
    showResults: Boolean,
    onEmployeeSelected: (Employee) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmployeeSearchField(
        query = query,
        onQueryChange = onQueryChange,
        searchResults = searchResults,
        isSearching = isSearching,
        showResults = showResults,
        onEmployeeSelected = onEmployeeSelected,
        onClearQuery = onClearQuery,
        modifier = modifier,
        placeholder = "Buscar...",
        maxResults = 3
    )
}

