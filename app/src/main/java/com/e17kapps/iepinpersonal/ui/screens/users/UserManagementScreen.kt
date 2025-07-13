package com.e17kapps.iepinpersonal.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.ui.components.LoadingState
import com.e17kapps.iepinpersonal.ui.theme.AppColors
import com.e17kapps.iepinpersonal.utils.toDateString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showUserDetailDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
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
                    text = "Gestión de Usuarios",
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
                IconButton(onClick = { showAddUserDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Agregar Usuario"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Header con estadísticas
        UserStatsHeader(users = users)

        // Lista de usuarios
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = "Cargando usuarios...",
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
                            text = "Error al cargar usuarios",
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
                        Button(onClick = { viewModel.loadUsers() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            users.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay usuarios registrados",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "Agrega el primer usuario operador",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserCard(
                            user = user,
                            onClick = {
                                selectedUser = user
                                showUserDetailDialog = true
                            },
                            onToggleStatus = { viewModel.toggleUserStatus(user.uid, !user.isActive) }
                        )
                    }
                }
            }
        }
    }

    // Dialog para agregar usuario
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { email, displayName, password, role, department ->
                viewModel.createUser(email, displayName, password, role, department)
                showAddUserDialog = false
            }
        )
    }

    // Dialog de detalle de usuario
    if (showUserDetailDialog && selectedUser != null) {
        UserDetailDialog(
            user = selectedUser!!,
            onDismiss = {
                showUserDetailDialog = false
                selectedUser = null
            },
            onUpdateRole = { newRole ->
                viewModel.updateUserRole(selectedUser!!.uid, newRole)
            },
            onDeactivate = {
                viewModel.toggleUserStatus(selectedUser!!.uid, false)
                showUserDetailDialog = false
                selectedUser = null
            }
        )
    }

    // Mostrar mensajes
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            // Aquí puedes mostrar un snackbar de éxito
        }
    }
}

@Composable
private fun UserStatsHeader(users: List<User>) {
    val adminCount = users.count { it.role == UserRole.ADMIN }
    val operatorCount = users.count { it.role == UserRole.OPERATOR }
    val activeCount = users.count { it.isActive }

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
                title = "Administradores",
                value = adminCount.toString(),
                icon = Icons.Default.AdminPanelSettings,
                color = AppColors.primary
            )

            StatItem(
                title = "Operadores",
                value = operatorCount.toString(),
                icon = Icons.Default.Person,
                color = AppColors.secondary
            )

            StatItem(
                title = "Activos",
                value = activeCount.toString(),
                icon = Icons.Default.CheckCircle,
                color = AppColors.success
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun UserCard(
    user: User,
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
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (user.role == UserRole.ADMIN)
                            AppColors.primary.copy(alpha = 0.2f)
                        else
                            AppColors.secondary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (user.role == UserRole.ADMIN)
                        Icons.Default.AdminPanelSettings
                    else
                        Icons.Default.Person,
                    contentDescription = null,
                    tint = if (user.role == UserRole.ADMIN)
                        AppColors.primary
                    else
                        AppColors.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }

                    RoleChip(role = user.role)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (user.department.isNotBlank()) user.department else "Sin departamento",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Último login: ${user.lastLogin?.toDateString() ?: "Nunca"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        StatusChip(
                            isActive = user.isActive,
                            onClick = onToggleStatus
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleChip(role: UserRole) {
    val (color, containerColor) = when (role) {
        UserRole.ADMIN -> AppColors.primary to AppColors.primary.copy(alpha = 0.2f)
        UserRole.OPERATOR -> AppColors.secondary to AppColors.secondary.copy(alpha = 0.2f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = role.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatusChip(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, UserRole, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.OPERATOR) }
    var department by remember { mutableStateOf("") }
    var showRoleDropdown by remember { mutableStateOf(false) }

    val isFormValid = email.isNotBlank() &&
            displayName.isNotBlank() &&
            password.length >= 6 &&
            password == confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Usuario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = confirmPassword.isNotBlank() && password != confirmPassword
                )

                ExposedDropdownMenuBox(
                    expanded = showRoleDropdown,
                    onExpandedChange = { showRoleDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = showRoleDropdown,
                        onDismissRequest = { showRoleDropdown = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(role.displayName)
                                        Text(
                                            text = role.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedRole = role
                                    showRoleDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Departamento (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(email, displayName, password, selectedRole, department)
                },
                enabled = isFormValid
            ) {
                Text("Crear Usuario")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDetailDialog(
    user: User,
    onDismiss: () -> Unit,
    onUpdateRole: (UserRole) -> Unit,
    onDeactivate: () -> Unit
) {
    var showRoleDropdown by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Usuario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Información básica
                InfoRow(label = "Nombre", value = user.displayName)
                InfoRow(label = "Email", value = user.email)
                InfoRow(label = "Departamento", value = user.department.ifBlank { "Sin asignar" })
                InfoRow(label = "Estado", value = if (user.isActive) "Activo" else "Inactivo")
                InfoRow(label = "Creado", value = user.createdAt.toDateString())
                InfoRow(label = "Último login", value = user.lastLogin?.toDateString() ?: "Nunca")

                Spacer(modifier = Modifier.height(8.dp))

                // Cambiar rol
                Text(
                    text = "Cambiar Rol",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = showRoleDropdown,
                    onExpandedChange = { showRoleDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Rol actual") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = showRoleDropdown,
                        onDismissRequest = { showRoleDropdown = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(role.displayName)
                                        Text(
                                            text = role.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedRole = role
                                    showRoleDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (selectedRole != user.role) {
                    TextButton(
                        onClick = {
                            onUpdateRole(selectedRole)
                            onDismiss()
                        }
                    ) {
                        Text("Actualizar Rol")
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            if (user.isActive) {
                TextButton(
                    onClick = onDeactivate,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.error
                    )
                ) {
                    Text("Desactivar")
                }
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = AppColors.TextPrimary
        )
    }
}