package com.e17kapps.iepinpersonal.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.manager.RoleManager
import com.e17kapps.iepinpersonal.domain.model.UserRole

@Composable
fun RoleBasedBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    roleManager: RoleManager = hiltViewModel()
) {
    val currentUser by roleManager.currentUser.collectAsState()

    val navigationItems = remember(currentUser) {
        buildList {
            // Items para ADMIN
            if (currentUser?.role == UserRole.ADMIN) {
                add(NavigationItem("dashboard", "Inicio", Icons.Default.Dashboard))
                add(NavigationItem("employees", "Personal", Icons.Default.People))
                add(NavigationItem("payments", "Pagos", Icons.Default.Payment))
                add(NavigationItem("statistics", "Reportes", Icons.Default.Analytics))
                add(NavigationItem("settings", "Config", Icons.Default.Settings))
            }
            // Items para OPERATOR
            else if (currentUser?.role == UserRole.OPERATOR) {
                add(NavigationItem("employees", "Personal", Icons.Default.People))
                add(NavigationItem("payments", "Pagos", Icons.Default.Payment))
                add(NavigationItem("discounts", "Descuentos", Icons.Default.Remove))
                add(NavigationItem("advances", "Adelantos", Icons.Default.TrendingUp))
                add(NavigationItem("profile", "Perfil", Icons.Default.Person))
            }
        }
    }

    if (navigationItems.isNotEmpty()) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
        ) {
            navigationItems.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) },
                    selected = currentRoute.startsWith(item.route),
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
fun RoleBasedDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    roleManager: RoleManager = hiltViewModel()
) {
    val currentUser by roleManager.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        // Header del drawer
        currentUser?.let { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Usuario",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = user.role.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Items de navegación según el rol
        currentUser?.let { user ->
            when (user.role) {
                UserRole.ADMIN -> {
                    DrawerNavigationItem(
                        icon = Icons.Default.Dashboard,
                        label = "Dashboard",
                        route = "dashboard",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("dashboard")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.People,
                        label = "Gestión de Personal",
                        route = "employees",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("employees")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.Payment,
                        label = "Gestión de Pagos",
                        route = "payments",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("payments")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.Analytics,
                        label = "Estadísticas y Reportes",
                        route = "statistics",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("statistics")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.SupervisorAccount,
                        label = "Gestión de Usuarios",
                        route = "user_management",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("user_management")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.Settings,
                        label = "Configuración",
                        route = "settings",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("settings")
                            onCloseDrawer()
                        }
                    )
                }

                UserRole.OPERATOR -> {
                    DrawerNavigationItem(
                        icon = Icons.Default.People,
                        label = "Personal (Consulta)",
                        route = "employees",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("employees")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.Payment,
                        label = "Registrar Pagos",
                        route = "payments",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("payments")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.Remove,
                        label = "Registrar Descuentos",
                        route = "discounts",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("discounts")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.TrendingUp,
                        label = "Registrar Adelantos",
                        route = "advances",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("advances")
                            onCloseDrawer()
                        }
                    )
                    DrawerNavigationItem(
                        icon = Icons.Default.Person,
                        label = "Mi Perfil",
                        route = "profile",
                        currentRoute = currentRoute,
                        onClick = {
                            onNavigate("profile")
                            onCloseDrawer()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón de cerrar sesión
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                onNavigate("logout")
                onCloseDrawer()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Cerrar Sesión"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    route: String,
    currentRoute: String,
    onClick: () -> Unit
) {
    val isSelected = currentRoute.startsWith(route)

    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)