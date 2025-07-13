package com.e17kapps.iepinpersonal.domain.manager

import com.e17kapps.iepinpersonal.domain.model.*
import com.e17kapps.iepinpersonal.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleManager @Inject constructor(
    private val userRepository: UserRepository
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun loadCurrentUser(): Result<User?> {
        _isLoading.value = true
        return try {
            userRepository.getCurrentUser()
                .onSuccess { user ->
                    _currentUser.value = user
                    // Actualizar último login si el usuario existe
                    user?.let {
                        userRepository.updateLastLogin(it.uid)
                    }
                }
                .onFailure {
                    _currentUser.value = null
                }
        } finally {
            _isLoading.value = false
        }
    }

    fun getCurrentUserRole(): UserRole? {
        return _currentUser.value?.role
    }

    fun isCurrentUserAdmin(): Boolean {
        return _currentUser.value?.role == UserRole.ADMIN
    }

    fun isCurrentUserOperator(): Boolean {
        return _currentUser.value?.role == UserRole.OPERATOR
    }

    // Funciones de verificación de permisos
    fun canManageEmployees(): Boolean {
        return _currentUser.value?.canManageEmployees() ?: false
    }

    fun canViewStatistics(): Boolean {
        return _currentUser.value?.canViewStatistics() ?: false
    }

    fun canManageUsers(): Boolean {
        return _currentUser.value?.canManageUsers() ?: false
    }

    fun canRegisterPayments(): Boolean {
        return _currentUser.value?.canRegisterPayments() ?: false
    }

    fun canRegisterDiscounts(): Boolean {
        return _currentUser.value?.canRegisterDiscounts() ?: false
    }

    fun canRegisterAdvances(): Boolean {
        return _currentUser.value?.canRegisterAdvances() ?: false
    }

    // Función para verificar si puede acceder a una ruta específica
    fun canAccessRoute(route: String): Boolean {
        val currentUser = _currentUser.value ?: return false

        return when {
            // Rutas que solo ADMIN puede acceder
            route.startsWith("dashboard") -> currentUser.canViewStatistics()
            route.startsWith("statistics") -> currentUser.canViewStatistics()
            route.startsWith("add_employee") -> currentUser.canManageEmployees()
            route.startsWith("edit_employee") -> currentUser.canManageEmployees()
            route.startsWith("settings") -> currentUser.canManageUsers()
            route.startsWith("user_management") -> currentUser.canManageUsers()

            // Rutas que ADMIN y OPERATOR pueden acceder
            route.startsWith("employees") -> true // Solo lectura para OPERATOR
            route.startsWith("employee_detail") -> true
            route.startsWith("payments") -> currentUser.canRegisterPayments()
            route.startsWith("add_payment") -> currentUser.canRegisterPayments()
            route.startsWith("payment_detail") -> currentUser.canRegisterPayments()
            route.startsWith("payment_history") -> currentUser.canRegisterPayments()
            route.startsWith("discounts") -> currentUser.canRegisterDiscounts()
            route.startsWith("add_discount") -> currentUser.canRegisterDiscounts()
            route.startsWith("advances") -> currentUser.canRegisterAdvances()
            route.startsWith("add_advance") -> currentUser.canRegisterAdvances()

            // Rutas públicas para usuarios autenticados
            route.startsWith("profile") -> true

            else -> false
        }
    }

    fun clearCurrentUser() {
        _currentUser.value = null
    }

    // Función para obtener las rutas de navegación según el rol
    fun getAvailableNavigationRoutes(): List<NavigationItem> {
        val currentUser = _currentUser.value ?: return emptyList()

        return buildList {
            if (currentUser.canViewStatistics()) {
                add(NavigationItem.Dashboard)
            }

            // Empleados (siempre disponible, pero con diferentes permisos)
            add(NavigationItem.Employees)

            // Pagos
            if (currentUser.canRegisterPayments()) {
                add(NavigationItem.Payments)
            }

            // Descuentos
            if (currentUser.canRegisterDiscounts()) {
                add(NavigationItem.Discounts)
            }

            // Adelantos
            if (currentUser.canRegisterAdvances()) {
                add(NavigationItem.Advances)
            }

            // Estadísticas (solo admin)
            if (currentUser.canViewStatistics()) {
                add(NavigationItem.Statistics)
            }

            // Configuración (solo admin)
            if (currentUser.canManageUsers()) {
                add(NavigationItem.Settings)
            }
        }
    }
}

// Clase para items de navegación
sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: String,
    val requiredRole: UserRole?
) {
    object Dashboard : NavigationItem("dashboard", "Inicio", "🏠", UserRole.ADMIN)
    object Employees : NavigationItem("employees", "Personal", "👥", null)
    object Payments : NavigationItem("payments", "Pagos", "💰", null)
    object Discounts : NavigationItem("discounts", "Descuentos", "📉", null)
    object Advances : NavigationItem("advances", "Adelantos", "⬆️", null)
    object Statistics : NavigationItem("statistics", "Reportes", "📊", UserRole.ADMIN)
    object Settings : NavigationItem("settings", "Configuración", "⚙️", UserRole.ADMIN)
}