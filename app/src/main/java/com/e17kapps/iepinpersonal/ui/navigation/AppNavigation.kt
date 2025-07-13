package com.e17kapps.iepinpersonal.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.e17kapps.iepinpersonal.domain.manager.RoleManager
import com.e17kapps.iepinpersonal.domain.model.AuthState
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.ui.screens.auth.AuthViewModel
import com.e17kapps.iepinpersonal.ui.screens.auth.LoginScreen
import com.e17kapps.iepinpersonal.ui.screens.DashboardScreen
import com.e17kapps.iepinpersonal.ui.screens.personal.EmployeesScreen
import com.e17kapps.iepinpersonal.ui.screens.personal.AddEditEmployeeScreen
import com.e17kapps.iepinpersonal.ui.screens.payments.PaymentsScreen
import com.e17kapps.iepinpersonal.ui.screens.payments.AddEditPaymentScreen
import com.e17kapps.iepinpersonal.ui.screens.statistics.StatisticsScreen
import com.e17kapps.iepinpersonal.ui.screens.users.UserManagementScreen
import com.e17kapps.iepinpersonal.ui.screens.auth.SplashScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    roleManager: RoleManager = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by roleManager.currentUser.collectAsState()

    // Observar cambios en el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (currentUser?.role == UserRole.ADMIN) {
                    navController.navigate(NavigationRoutes.Dashboard.route) {
                        popUpTo(NavigationRoutes.Login.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavigationRoutes.Employees.route) {
                        popUpTo(NavigationRoutes.Login.route) { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(NavigationRoutes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {
                // Loading state, mantener en splash
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Splash.route,
        modifier = Modifier.fillMaxSize()
    ) {
        // Splash Screen
        composable(NavigationRoutes.Splash.route) {
            SplashScreen()
        }

        // Auth Flow
        composable(NavigationRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // La navegación se maneja en el LaunchedEffect
                }
            )
        }

        // Main App Routes - Solo para usuarios autenticados
        composable(NavigationRoutes.Dashboard.route) {
            if (roleManager.canViewStatistics()) {
                DashboardScreen(
                    onNavigateToAddEmployee = {
                        navController.navigate(NavigationRoutes.AddEmployee.route)
                    },
                    onNavigateToAddPayment = {
                        navController.navigate(NavigationRoutes.AddPayment.route)
                    },
                    onNavigateToStatistics = {
                        navController.navigate(NavigationRoutes.Statistics.route)
                    },
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        // Employee Management
        composable(NavigationRoutes.Employees.route) {
            EmployeesScreen(
                canManageEmployees = roleManager.canManageEmployees(),
                onNavigateToAddEmployee = {
                    if (roleManager.canManageEmployees()) {
                        navController.navigate(NavigationRoutes.AddEmployee.route)
                    }
                },
                onNavigateToEditEmployee = { employeeId ->
                    if (roleManager.canManageEmployees()) {
                        navController.navigate(NavigationRoutes.EditEmployee.createRoute(employeeId))
                    }
                },
                onNavigateToEmployeeDetail = { employeeId ->
                    navController.navigate(NavigationRoutes.EmployeeDetail.createRoute(employeeId))
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
        }

        composable(NavigationRoutes.AddEmployee.route) {
            if (roleManager.canManageEmployees()) {
                AddEditEmployeeScreen(
                    employeeId = null,
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onEmployeeSaved = {
                        navController.navigateUp()
                    }
                )
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        composable(NavigationRoutes.EditEmployee.route) { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: return@composable
            if (roleManager.canManageEmployees()) {
                AddEditEmployeeScreen(
                    employeeId = employeeId,
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onEmployeeSaved = {
                        navController.navigateUp()
                    }
                )
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        // Payment Management
        composable(NavigationRoutes.Payments.route) {
            if (roleManager.canRegisterPayments()) {
                PaymentsScreen(
                    onNavigateToAddPayment = {
                        navController.navigate(NavigationRoutes.AddPayment.route)
                    },
                    onNavigateToPaymentDetail = { paymentId ->
                        navController.navigate(NavigationRoutes.PaymentDetail.createRoute(paymentId))
                    },
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        composable(NavigationRoutes.AddPayment.route) {
            if (roleManager.canRegisterPayments()) {
                AddEditPaymentScreen(
                    paymentId = null,
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onPaymentSaved = {
                        navController.navigateUp()
                    }
                )
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        // Statistics (Solo Admin)
        composable(NavigationRoutes.Statistics.route) {
            if (roleManager.canViewStatistics()) {
                StatisticsScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onNavigateToEmployeeStats = { employeeId ->
                        // Navegar a estadísticas específicas del empleado
                    },
                    onNavigateToMonthlyReport = { month, year ->
                        // Navegar a reporte mensual
                    }
                )
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        // User Management (Solo Admin)
        composable("user_management") {
            if (roleManager.canManageUsers()) {
                UserManagementScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        // Discounts (Admin y Operator)
        composable("discounts") {
            if (roleManager.canRegisterDiscounts()) {
                // DiscountsScreen - Implementar según necesidades
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text("Gestión de Descuentos - Por implementar")
                }
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        // Advances (Admin y Operator)
        composable("advances") {
            if (roleManager.canRegisterAdvances()) {
                // AdvancesScreen - Implementar según necesidades
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text("Gestión de Adelantos - Por implementar")
                }
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }

        // Profile (Todos los usuarios)
        composable("profile") {
            // ProfileScreen - Implementar según necesidades
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text("Perfil de Usuario - Por implementar")
            }
        }

        // Settings (Solo Admin)
        composable("settings") {
            if (roleManager.canManageUsers()) {
                // SettingsScreen - Implementar según necesidades
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text("Configuración - Por implementar")
                }
            } else {
                UnauthorizedScreen {
                    navController.navigateUp()
                }
            }
        }
    }
}

@Composable
private fun UnauthorizedScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                contentDescription = "Sin acceso",
                modifier = Modifier.size(64.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
            androidx.compose.material3.Text(
                text = "Sin permisos de acceso",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )
            androidx.compose.material3.Text(
                text = "No tienes permisos para acceder a esta sección",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.material3.Button(
                onClick = onNavigateBack
            ) {
                androidx.compose.material3.Text("Volver")
            }
        }
    }
}