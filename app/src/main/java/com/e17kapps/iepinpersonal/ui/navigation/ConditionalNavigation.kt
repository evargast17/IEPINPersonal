package com.e17kapps.iepinpersonal.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e17kapps.iepinpersonal.domain.manager.RoleManager
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole

@Composable
fun ConditionalNavigation(
    roleManager: RoleManager = hiltViewModel()
) {
    val currentUser by roleManager.currentUser.collectAsState()
    val isLoading by roleManager.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        roleManager.loadCurrentUser()
    }

    when {
        isLoading -> {
            // Pantalla de carga mientras se verifica el rol
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        currentUser == null -> {
            // Usuario no encontrado, redirigir al login
            LaunchedEffect(Unit) {
                // Navegar al login
            }
        }

        else -> {
            // Mostrar navegación según el rol
            when (currentUser!!.role) {
                UserRole.ADMIN -> AdminNavigation()
                UserRole.OPERATOR -> OperatorNavigation()
            }
        }
    }
}

@Composable
private fun AdminNavigation() {
    // Bottom Navigation completa para ADMIN
    val bottomNavItems = listOf(
        BottomNavItem(
            route = "dashboard",
            title = "Inicio",
            icon = "🏠"
        ),
        BottomNavItem(
            route = "employees",
            title = "Personal",
            icon = "👥"
        ),
        BottomNavItem(
            route = "payments",
            title = "Pagos",
            icon = "💰"
        ),
        BottomNavItem(
            route = "statistics",
            title = "Reportes",
            icon = "📊"
        ),
        BottomNavItem(
            route = "settings",
            title = "Config",
            icon = "⚙️"
        )
    )

    // Implementar navegación con estos items
    RoleBasedBottomNavigation(items = bottomNavItems)
}

@Composable
private fun OperatorNavigation() {
    // Bottom Navigation limitada para OPERATOR
    val bottomNavItems = listOf(
        BottomNavItem(
            route = "employees",
            title = "Personal",
            icon = "👥"
        ),
        BottomNavItem(
            route = "payments",
            title = "Pagos",
            icon = "💰"
        ),
        BottomNavItem(
            route = "discounts",
            title = "Descuentos",
            icon = "📉"
        ),
        BottomNavItem(
            route = "advances",
            title = "Adelantos",
            icon = "⬆️"
        )
    )

    // Implementar navegación con estos items
    RoleBasedBottomNavigation(items = bottomNavItems)
}

@Composable
private fun RoleBasedBottomNavigation(
    items: List<BottomNavItem>
) {
    // Aquí implementarías tu bottom navigation
    // usando los items proporcionados según el rol

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Text(item.icon) },
                label = { Text(item.title) },
                selected = false, // Implementar lógica de selección
                onClick = {
                    // Implementar navegación
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
)

// Composable para verificar permisos antes de mostrar contenido
@Composable
fun PermissionProtectedContent(
    requiredRole: UserRole? = null,
    requiresPermission: (User) -> Boolean = { true },
    content: @Composable () -> Unit,
    fallback: @Composable () -> Unit = {
        Text("No tienes permisos para ver este contenido")
    }
) {
    val roleManager: RoleManager = hiltViewModel()
    val currentUser by roleManager.currentUser.collectAsState()

    when {
        currentUser == null -> fallback()
        requiredRole != null && currentUser!!.role != requiredRole -> fallback()
        !requiresPermission(currentUser!!) -> fallback()
        else -> content()
    }
}

// Función auxiliar para verificar rutas
@Composable
fun ProtectedRoute(
    route: String,
    content: @Composable () -> Unit
) {
    val roleManager: RoleManager = hiltViewModel()

    if (roleManager.canAccessRoute(route)) {
        content()
    } else {
        // Mostrar mensaje de acceso denegado o redirigir
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚫",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Acceso Denegado",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "No tienes permisos para acceder a esta sección",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}