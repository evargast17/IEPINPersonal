package com.e17kapps.iepinpersonal.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.e17kapps.iepinpersonal.ui.navigation.MainAppNavigation
import com.e17kapps.iepinpersonal.ui.navigation.bottomNavItems
import com.e17kapps.iepinpersonal.ui.screens.auth.AuthViewModel
import com.e17kapps.iepinpersonal.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = getScreenTitle(currentRoute),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = AppColors.TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Text(
                                text = item.icon,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.GradientStart,
                            selectedTextColor = AppColors.GradientStart,
                            indicatorColor = AppColors.GradientStart.copy(alpha = 0.1f),
                            unselectedIconColor = AppColors.TextSecondary,
                            unselectedTextColor = AppColors.TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        MainAppNavigation(
            navController = bottomNavController,
            onLogout = {
                authViewModel.logout()
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

private fun getScreenTitle(currentRoute: String?): String {
    return when (currentRoute) {
        "dashboard" -> "IEPIN Personal"
        "employees" -> "Personal"
        "payments" -> "Pagos"
        "statistics" -> "Reportes"
        "profile" -> "Perfil"
        "settings" -> "Configuración"
        "add_employee" -> "Agregar Empleado"
        "add_payment" -> "Registrar Pago"
        else -> "IEPIN Personal"
    }
}