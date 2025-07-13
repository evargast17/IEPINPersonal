package com.e17kapps.iepinpersonal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.e17kapps.iepinpersonal.domain.manager.RoleManager
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.ui.components.RoleBasedBottomNavigation
import com.e17kapps.iepinpersonal.ui.components.RoleBasedDrawerContent
import com.e17kapps.iepinpersonal.ui.screens.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    roleManager: RoleManager = hiltViewModel()
) {
    val currentUser by roleManager.currentUser.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Función para manejar navegación
    val handleNavigation = { route: String ->
        when (route) {
            "logout" -> {
                authViewModel.logout()
            }
            else -> {
                if (roleManager.canAccessRoute(route)) {
                    navController.navigate(route) {
                        // Evitar múltiples copias del mismo destino
                        launchSingleTop = true
                        // Restaurar estado si regresamos a un destino anterior
                        restoreState = true
                    }
                }
            }
        }
    }

    // Determinar si mostrar el drawer
    val showDrawer = currentUser != null &&
            !currentRoute.startsWith("login") &&
            !currentRoute.startsWith("splash")

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    RoleBasedDrawerContent(
                        currentRoute = currentRoute,
                        onNavigate = handleNavigation,
                        onCloseDrawer = {
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        roleManager = roleManager
                    )
                }
            }
        ) {
            MainContent(
                navController = navController,
                currentRoute = currentRoute,
                onNavigate = handleNavigation,
                onOpenDrawer = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                roleManager = roleManager
            )
        }
    } else {
        MainContent(
            navController = navController,
            currentRoute = currentRoute,
            onNavigate = handleNavigation,
            onOpenDrawer = null,
            roleManager = roleManager
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    navController: NavHostController,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onOpenDrawer: (() -> Unit)?,
    roleManager: RoleManager
) {
    val currentUser by roleManager.currentUser.collectAsState()

    Scaffold(
        topBar = {
            if (onOpenDrawer != null && currentUser != null) {
                TopAppBar(
                    title = {
                        Text(getScreenTitle(currentRoute))
                    },
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"
                            )
                        }
                    },
                    actions = {
                        // Indicador de rol del usuario
                        currentUser?.let { user ->
                            AssistChip(
                                onClick = { onNavigate("profile") },
                                label = {
                                    Text(
                                        text = user.role.displayName,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (user.role) {
                                            UserRole.ADMIN ->Icons.Default.AdminPanelSettings
                                            UserRole.OPERATOR ->Icons.Default.Person
                                        },
                                        contentDescription = user.role.displayName,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentUser != null &&
                !currentRoute.startsWith("login") &&
                !currentRoute.startsWith("splash")) {
                RoleBasedBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    roleManager = roleManager
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // El contenido de navegación se maneja en AppNavigation
            com.e17kapps.iepinpersonal.ui.navigation.AppNavigation(
                navController = navController
            )
        }
    }
}

private fun getScreenTitle(route: String): String {
    return when {
        route.startsWith("dashboard") -> "Dashboard"
        route.startsWith("employees") -> "Personal"
        route.startsWith("add_employee") -> "Agregar Empleado"
        route.startsWith("edit_employee") -> "Editar Empleado"
        route.startsWith("employee_detail") -> "Detalle Empleado"
        route.startsWith("payments") -> "Pagos"
        route.startsWith("add_payment") -> "Registrar Pago"
        route.startsWith("payment_detail") -> "Detalle Pago"
        route.startsWith("discounts") -> "Descuentos"
        route.startsWith("add_discount") -> "Registrar Descuento"
        route.startsWith("advances") -> "Adelantos"
        route.startsWith("add_advance") -> "Registrar Adelanto"
        route.startsWith("statistics") -> "Estadísticas"
        route.startsWith("user_management") -> "Gestión de Usuarios"
        route.startsWith("profile") -> "Mi Perfil"
        route.startsWith("settings") -> "Configuración"
        else -> "IEPIN Personal"
    }
}