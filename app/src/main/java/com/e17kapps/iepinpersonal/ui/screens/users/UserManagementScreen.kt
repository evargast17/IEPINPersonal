package com.e17kapps.iepinpersonal.ui.screens.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val users by viewModel.users.collectAsState()
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showUserDetailsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateUserDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Crear Usuario")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateUserDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Usuario")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Estadísticas rápidas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.primary.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        title = "Total",
                        value = users.size.toString(),
                        icon = Icons.Default.People
                    )
                    StatCard(
                        title = "Activos",
                        value = users.count { it.isActive }.toString(),
                        icon = Icons.Default.CheckCircle
                    )
                    StatCard(
                        title = "Admins",
                        value = users.count { it.role == UserRole.ADMIN }.toString(),
                        icon = Icons.Default.AdminPanelSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de usuarios
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserCard(
                            user = user,
                            onUserClick = {
                                selectedUser = user
                                showUserDetailsDialog = true
                            },
                            onToggleStatus = { viewModel.toggleUserStatus(user.uid, !user.isActive) },
                            onChangeRole = { newRole ->
                                viewModel.updateUserRole(user.uid, newRole)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog para crear usuario
    if (showCreateUserDialog) {
        CreateUserDialog(
            onDismiss = { showCreateUserDialog = false },
            onCreateUser = { name, email, password, role ->
                viewModel.createUser(name, email, password, role)
                showCreateUserDialog = false
            }
        )
    }

    // Dialog para detalles del usuario
    if (showUserDetailsDialog && selectedUser != null) {
        UserDetailsDialog(
            user = selectedUser!!,
            onDismiss = {
                showUserDetailsDialog = false
                selectedUser = null
            }
        )
    }

    // Mostrar mensajes
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Mostrar snackbar de error
        }
    }

    uiState.successMessage?.let { success ->
        LaunchedEffect(success) {
            // Mostrar snackbar de éxito
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AppColors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
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
    onUserClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onChangeRole: (UserRole) -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onUserClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge {
                            Text(
                                text = user.role.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (!user.isActive) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = "Inactivo",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Menú de acciones
                    IconButton(onClick = { showRoleMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (user.isActive) "Desactivar" else "Activar") },
                            onClick = {
                                onToggleStatus()
                                showRoleMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                            }
                        )

                        if (user.isActive) {
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Cambiar a Admin") },
                                onClick = {
                                    onChangeRole(UserRole.ADMIN)
                                    showRoleMenu = false
                                },
                                enabled = user.role != UserRole.ADMIN,
                                leadingIcon = {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cambiar a Operador") },
                                onClick = {
                                    onChangeRole(UserRole.OPERATOR)
                                    showRoleMenu = false
                                },
                                enabled = user.role != UserRole.OPERATOR,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreateUser: (String, String, String, UserRole) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.OPERATOR) }
    var showPassword by remember { mutableStateOf(false) }

    val isFormValid = remember(name, email, password, confirmPassword) {
        name.isNotBlank() &&
                email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                password.length >= 6 &&
                password == confirmPassword
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Usuario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword)
                        androidx.compose.ui.text.input.VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPassword.isNotBlank() && password != confirmPassword,
                    supportingText = if (confirmPassword.isNotBlank() && password != confirmPassword) {
                        { Text("Las contraseñas no coinciden") }
                    } else null,
                    singleLine = true
                )

                // Selector de rol
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Rol del usuario",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                onClick = { selectedRole = UserRole.OPERATOR },
                                label = { Text("Operador") },
                                selected = selectedRole == UserRole.OPERATOR,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                }
                            )
                            FilterChip(
                                onClick = { selectedRole = UserRole.ADMIN },
                                label = { Text("Admin") },
                                selected = selectedRole == UserRole.ADMIN,
                                leadingIcon = {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                }
                            )
                        }

                        Text(
                            text = selectedRole.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onCreateUser(name, email, password, selectedRole)
                    }
                },
                enabled = isFormValid
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun UserDetailsDialog(
    user: User,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Usuario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("Nombre", user.displayName)
                DetailRow("Email", user.email)
                DetailRow("Rol", user.role.displayName)
                DetailRow("Estado", if (user.isActive) "Activo" else "Inactivo")
                DetailRow("Departamento", user.department.ifBlank { "No asignado" })

                user.lastLogin?.let {
                    DetailRow("Último acceso", java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it)))
                }

                DetailRow("Creado", java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(user.createdAt)))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

// Estado UI para gestión de usuarios
data class UserManagementUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isCreatingUser: Boolean = false
)