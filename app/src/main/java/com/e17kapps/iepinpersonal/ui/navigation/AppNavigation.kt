package com.e17kapps.iepinpersonal.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.e17kapps.iepinpersonal.domain.model.AuthState
import com.e17kapps.iepinpersonal.ui.screens.auth.AuthViewModel
import com.e17kapps.iepinpersonal.ui.screens.auth.LoginScreen
import com.e17kapps.iepinpersonal.ui.screens.auth.RegisterScreen
import com.e17kapps.iepinpersonal.ui.screens.auth.SplashScreen
import com.e17kapps.iepinpersonal.ui.screens.discounts.AddDiscountScreen
import com.e17kapps.iepinpersonal.ui.screens.discounts.DiscountScreen
import com.e17kapps.iepinpersonal.ui.screens.main.MainScreen
import com.e17kapps.iepinpersonal.ui.screens.payments.PaymentDetailScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    // Navegación automática basada en el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate(NavigationRoutes.Dashboard.route) {
                    popUpTo(NavigationRoutes.Splash.route) {
                        inclusive = true
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(NavigationRoutes.Login.route) {
                    popUpTo(NavigationRoutes.Splash.route) {
                        inclusive = true
                    }
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
                onNavigateToRegister = {
                    navController.navigate(NavigationRoutes.Register.route)
                },
                onNavigateToForgotPassword = {
                    // TODO: Implementar navegación a forgot password
                },
                onLoginSuccess = {
                    navController.navigate(NavigationRoutes.Dashboard.route) {
                        popUpTo(NavigationRoutes.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(NavigationRoutes.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(NavigationRoutes.Dashboard.route) {
                        popUpTo(NavigationRoutes.Register.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Main App Flow (después de autenticación)
        composable(NavigationRoutes.Dashboard.route) {
            MainScreen(navController = navController)
        }

        // Otras rutas se manejarán dentro de MainScreen
    }
}

@Composable
fun MainAppNavigation(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Dashboard.route,
        modifier = modifier
    ) {
        // Dashboard - OPTIMIZADO con launchSingleTop
        composable(NavigationRoutes.Dashboard.route) {
            com.e17kapps.iepinpersonal.ui.screens.DashboardScreen(
                onNavigateToAddEmployee = {
                    navController.navigate(NavigationRoutes.AddEmployee.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAddPayment = {
                    navController.navigate(NavigationRoutes.AddPayment.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToDiscounts = {
                    navController.navigate(NavigationRoutes.Discounts.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAdvances = {
                    navController.navigate(NavigationRoutes.Advances.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Employee Routes - OPTIMIZADO
        composable(NavigationRoutes.Employees.route) {
            com.e17kapps.iepinpersonal.ui.screens.personal.EmployeesScreen(
                onNavigateToAddEmployee = {
                    navController.navigate(NavigationRoutes.AddEmployee.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToEmployeeDetail = { employeeId ->
                    navController.navigate(NavigationRoutes.EmployeeDetail.createRoute(employeeId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavigationRoutes.AddEmployee.route) {
            com.e17kapps.iepinpersonal.ui.screens.personal.AddEmployeeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEmployeeAdded = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationRoutes.EditEmployee.route) { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            EditEmployeeScreen(
                employeeId = employeeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEmployeeUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationRoutes.EmployeeDetail.route) { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            EmployeeDetailScreen(
                employeeId = employeeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(NavigationRoutes.EditEmployee.createRoute(employeeId)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToPaymentHistory = {
                    navController.navigate(NavigationRoutes.PaymentHistory.createRoute(employeeId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Payment Routes - OPTIMIZADO
        composable(NavigationRoutes.Payments.route) {
            com.e17kapps.iepinpersonal.ui.screens.payments.PaymentsScreen(
                onNavigateToAddPayment = {
                    navController.navigate(NavigationRoutes.AddPayment.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToPaymentDetail = { paymentId ->
                    navController.navigate(NavigationRoutes.PaymentDetail.createRoute(paymentId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavigationRoutes.AddPayment.route) {
            com.e17kapps.iepinpersonal.ui.screens.payments.AddPaymentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPaymentAdded = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationRoutes.PaymentDetail.route) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            PaymentDetailScreen(
                paymentId = paymentId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(NavigationRoutes.EditPayment.createRoute(paymentId))
                },
                onNavigateToEmployee = { employeeId ->
                    navController.navigate(NavigationRoutes.EmployeeDetail.createRoute(employeeId))
                }
            )
        }

        composable(NavigationRoutes.PaymentHistory.route) { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            PaymentHistoryScreen(
                employeeId = employeeId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


        // Statistics Routes - OPTIMIZADO
        composable(NavigationRoutes.Statistics.route) {
            StatisticsScreen(
                onNavigateToEmployeeStats = { employeeId ->
                    navController.navigate(NavigationRoutes.EmployeeStatistics.createRoute(employeeId)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToMonthlyReport = { month, year ->
                    navController.navigate(NavigationRoutes.MonthlyReport.createRoute(month, year)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavigationRoutes.EmployeeStatistics.route) { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            EmployeeStatisticsScreen(
                employeeId = employeeId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationRoutes.MonthlyReport.route) { backStackEntry ->
            val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: 1
            val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: 2024
            MonthlyReportScreen(
                month = month,
                year = year,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Profile Routes - OPTIMIZADO
        composable(NavigationRoutes.Profile.route) {
            com.e17kapps.iepinpersonal.ui.screens.profile.ProfileScreen(
                onLogout = onLogout,
                onNavigateToSettings = {
                    navController.navigate(NavigationRoutes.Settings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavigationRoutes.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // NUEVAS RUTAS OPTIMIZADAS para Descuentos y Adelantos
        // Discount Routes
        composable(NavigationRoutes.Discounts.route) {
            DiscountScreen(
                onNavigateToAddDiscount = { employeeId ->
                    val route = if (employeeId.isNotBlank()) {
                        NavigationRoutes.AddDiscount.createRoute(employeeId)
                    } else {
                        NavigationRoutes.AddDiscount.createRoute("") // Sin empleado predeterminado
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToDiscountDetail = { discountId ->
                    navController.navigate(NavigationRoutes.DiscountDetail.createRoute(discountId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavigationRoutes.AddDiscount.route) { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            AddDiscountScreen(
                employeeId = employeeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDiscountAdded = {
                    navController.popBackStack()
                }
            )
        }

        /* TODO: Implementar DiscountDetailScreen y EditDiscountScreen
        composable(NavigationRoutes.DiscountDetail.route) { backStackEntry ->
            val discountId = backStackEntry.arguments?.getString("discountId") ?: ""
            DiscountDetailScreen(
                discountId = discountId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(NavigationRoutes.EditDiscount.createRoute(discountId))
                }
            )
        }
        */

        composable(NavigationRoutes.Advances.route) {
            PlaceholderScreen(
                title = "Gestión de Adelantos",
                icon = "📈",
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// WRAPPER FUNCTIONS SIN CAMBIOS - Mantengo tus funciones actuales
@Composable
fun EditEmployeeScreen(
    employeeId: String,
    onNavigateBack: () -> Unit,
    onEmployeeUpdated: () -> Unit
) {
    com.e17kapps.iepinpersonal.ui.screens.personal.EditEmployeeScreen(
        employeeId = employeeId,
        onNavigateBack = onNavigateBack,
        onEmployeeUpdated = onEmployeeUpdated
    )
}

@Composable
fun EmployeeDetailScreen(
    employeeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToPaymentHistory: () -> Unit
) {
    com.e17kapps.iepinpersonal.ui.screens.personal.EmployeeDetailScreen(
        employeeId = employeeId,
        onNavigateBack = onNavigateBack,
        onNavigateToEdit = onNavigateToEdit,
        onNavigateToPaymentHistory = onNavigateToPaymentHistory
    )
}

@Composable
fun PaymentDetailScreen(
    paymentId: String,
    onNavigateBack: () -> Unit
) {
    // MEJORADO - Pantalla temporal más bonita mientras implementas la real
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Detalle Pago: $paymentId\n(Implementar pantalla)")
    }
}

@Composable
fun PaymentHistoryScreen(
    employeeId: String,
    onNavigateBack: () -> Unit
) {
    com.e17kapps.iepinpersonal.ui.screens.payments.PaymentHistoryScreen(
        employeeId = employeeId,
        onNavigateBack = onNavigateBack
    )
}


@Composable
fun StatisticsScreen(
    onNavigateToEmployeeStats: (String) -> Unit,
    onNavigateToMonthlyReport: (Int, Int) -> Unit
) {
    com.e17kapps.iepinpersonal.ui.screens.statistics.StatisticsScreen(
        onNavigateToEmployeeStats = onNavigateToEmployeeStats,
        onNavigateToMonthlyReport = onNavigateToMonthlyReport
    )
}

@Composable
fun EmployeeStatisticsScreen(
    employeeId: String,
    onNavigateBack: () -> Unit
) {
    com.e17kapps.iepinpersonal.ui.screens.personal.EmployeeStatisticsScreen(
        employeeId = employeeId,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun MonthlyReportScreen(
    month: Int,
    year: Int,
    onNavigateBack: () -> Unit
) {
    com.e17kapps.iepinpersonal.ui.screens.statistics.MonthlyReportScreen(
        month = month,
        year = year,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    com.e17kapps.iepinpersonal.ui.screens.settings.SettingsScreen(
        onNavigateBack = onNavigateBack
    )
}

// NUEVA FUNCIÓN - Pantalla placeholder simple y limpia
@Composable
private fun PlaceholderScreen(
    title: String,
    icon: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("$icon $title\n(Funcionalidad pendiente)")
    }
}