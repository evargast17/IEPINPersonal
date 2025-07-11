package com.e17kapps.iepinpersonal.ui.navigation

/**
 * Rutas de navegación centralizadas para toda la aplicación
 */
sealed class NavigationRoutes(val route: String) {

    // ============================================================================
    // AUTH ROUTES - Rutas de autenticación
    // ============================================================================

    object Splash : NavigationRoutes("splash")
    object Login : NavigationRoutes("login")
    object Register : NavigationRoutes("register")
    object ForgotPassword : NavigationRoutes("forgot_password")

    // ============================================================================
    // MAIN APP ROUTES - Rutas principales de la aplicación
    // ============================================================================

    object Dashboard : NavigationRoutes("dashboard")

    // ============================================================================
    // EMPLOYEE ROUTES - Gestión de empleados
    // ============================================================================

    object Employees : NavigationRoutes("employees")
    object AddEmployee : NavigationRoutes("add_employee")

    object EditEmployee : NavigationRoutes("edit_employee/{employeeId}") {
        fun createRoute(employeeId: String) = "edit_employee/$employeeId"
    }

    object EmployeeDetail : NavigationRoutes("employee_detail/{employeeId}") {
        fun createRoute(employeeId: String) = "employee_detail/$employeeId"
    }

    object EmployeeProfile : NavigationRoutes("employee_profile/{employeeId}") {
        fun createRoute(employeeId: String) = "employee_profile/$employeeId"
    }

    // ============================================================================
    // PAYMENT ROUTES - Gestión de pagos
    // ============================================================================

    object Payments : NavigationRoutes("payments")
    object AddPayment : NavigationRoutes("add_payment")

    object PaymentDetail : NavigationRoutes("payment_detail/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_detail/$paymentId"
    }

    object EditPayment : NavigationRoutes("edit_payment/{paymentId}") {
        fun createRoute(paymentId: String) = "edit_payment/$paymentId"
    }

    object PaymentHistory : NavigationRoutes("payment_history/{employeeId}") {
        fun createRoute(employeeId: String) = "payment_history/$employeeId"
    }

    object PaymentReceipt : NavigationRoutes("payment_receipt/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_receipt/$paymentId"
    }

    // ============================================================================
    // STATISTICS ROUTES - Reportes y estadísticas
    // ============================================================================

    object Statistics : NavigationRoutes("statistics")

    object EmployeeStatistics : NavigationRoutes("employee_statistics/{employeeId}") {
        fun createRoute(employeeId: String) = "employee_statistics/$employeeId"
    }

    object MonthlyReport : NavigationRoutes("monthly_report/{month}/{year}") {
        fun createRoute(month: Int, year: Int) = "monthly_report/$month/$year"
    }

    object YearlyReport : NavigationRoutes("yearly_report/{year}") {
        fun createRoute(year: Int) = "yearly_report/$year"
    }

    object PaymentMethodReport : NavigationRoutes("payment_method_report")
    object SalaryReport : NavigationRoutes("salary_report")

    // ============================================================================
    // DISCOUNT & ADVANCE ROUTES - Descuentos y adelantos
    // ============================================================================

    object Discounts : NavigationRoutes("discounts")
    object AddDiscount : NavigationRoutes("add_discount/{employeeId}") {
        fun createRoute(employeeId: String) = "add_discount/$employeeId"
    }

    object Advances : NavigationRoutes("advances")
    object AddAdvance : NavigationRoutes("add_advance/{employeeId}") {
        fun createRoute(employeeId: String) = "add_advance/$employeeId"
    }

    object AdvanceDetail : NavigationRoutes("advance_detail/{advanceId}") {
        fun createRoute(advanceId: String) = "advance_detail/$advanceId"
    }

    // ============================================================================
    // PROFILE & SETTINGS ROUTES - Perfil y configuración
    // ============================================================================

    object Profile : NavigationRoutes("profile")
    object Settings : NavigationRoutes("settings")
    object About : NavigationRoutes("about")
    object Help : NavigationRoutes("help")
    object PrivacyPolicy : NavigationRoutes("privacy_policy")
    object TermsOfService : NavigationRoutes("terms_of_service")

    // ============================================================================
    // BACKUP & EXPORT ROUTES - Respaldo y exportación
    // ============================================================================

    object Backup : NavigationRoutes("backup")
    object Export : NavigationRoutes("export")
    object Import : NavigationRoutes("import")

    // ============================================================================
    // NOTIFICATION ROUTES - Notificaciones
    // ============================================================================

    object Notifications : NavigationRoutes("notifications")
    object NotificationSettings : NavigationRoutes("notification_settings")
}

// ============================================================================
// BOTTOM NAVIGATION ITEMS - Elementos de navegación inferior
// ============================================================================

/**
 * Items de navegación inferior
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String,
    val selectedIcon: String = icon
) {
    object Dashboard : BottomNavItem(
        route = NavigationRoutes.Dashboard.route,
        title = "Inicio",
        icon = "🏠",
        selectedIcon = "🏡"
    )

    object Employees : BottomNavItem(
        route = NavigationRoutes.Employees.route,
        title = "Personal",
        icon = "👥",
        selectedIcon = "👨‍👩‍👧‍👦"
    )

    object Payments : BottomNavItem(
        route = NavigationRoutes.Payments.route,
        title = "Pagos",
        icon = "💰",
        selectedIcon = "💸"
    )

    object Statistics : BottomNavItem(
        route = NavigationRoutes.Statistics.route,
        title = "Reportes",
        icon = "📊",
        selectedIcon = "📈"
    )

    object Profile : BottomNavItem(
        route = NavigationRoutes.Profile.route,
        title = "Perfil",
        icon = "👤",
        selectedIcon = "👨‍💼"
    )
}

/**
 * Lista de items de navegación inferior
 */
val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Employees,
    BottomNavItem.Payments,
    BottomNavItem.Statistics,
    BottomNavItem.Profile
)

// ============================================================================
// DRAWER NAVIGATION ITEMS - Items del menú lateral (opcional)
// ============================================================================

/**
 * Items del drawer/menú lateral
 */
sealed class DrawerItem(
    val route: String,
    val title: String,
    val icon: String,
    val description: String = ""
) {
    object Dashboard : DrawerItem(
        route = NavigationRoutes.Dashboard.route,
        title = "Dashboard",
        icon = "🏠",
        description = "Vista general del sistema"
    )

    object Employees : DrawerItem(
        route = NavigationRoutes.Employees.route,
        title = "Empleados",
        icon = "👥",
        description = "Gestión de personal"
    )

    object Payments : DrawerItem(
        route = NavigationRoutes.Payments.route,
        title = "Pagos",
        icon = "💰",
        description = "Procesamiento de sueldos"
    )

    object Statistics : DrawerItem(
        route = NavigationRoutes.Statistics.route,
        title = "Reportes",
        icon = "📊",
        description = "Estadísticas y análisis"
    )

    object Discounts : DrawerItem(
        route = NavigationRoutes.Discounts.route,
        title = "Descuentos",
        icon = "📉",
        description = "Gestión de descuentos"
    )

    object Advances : DrawerItem(
        route = NavigationRoutes.Advances.route,
        title = "Adelantos",
        icon = "💳",
        description = "Adelantos de sueldo"
    )

    object Settings : DrawerItem(
        route = NavigationRoutes.Settings.route,
        title = "Configuración",
        icon = "⚙️",
        description = "Ajustes de la aplicación"
    )

    object Backup : DrawerItem(
        route = NavigationRoutes.Backup.route,
        title = "Respaldo",
        icon = "☁️",
        description = "Backup y sincronización"
    )

    object Help : DrawerItem(
        route = NavigationRoutes.Help.route,
        title = "Ayuda",
        icon = "❓",
        description = "Soporte y documentación"
    )

    object About : DrawerItem(
        route = NavigationRoutes.About.route,
        title = "Acerca de",
        icon = "ℹ️",
        description = "Información de la app"
    )
}

/**
 * Items principales del drawer
 */
val mainDrawerItems = listOf(
    DrawerItem.Dashboard,
    DrawerItem.Employees,
    DrawerItem.Payments,
    DrawerItem.Statistics,
    DrawerItem.Discounts,
    DrawerItem.Advances
)

/**
 * Items secundarios del drawer
 */
val secondaryDrawerItems = listOf(
    DrawerItem.Settings,
    DrawerItem.Backup,
    DrawerItem.Help,
    DrawerItem.About
)

// ============================================================================
// NAVIGATION CONSTANTS - Constantes de navegación
// ============================================================================

object NavigationConstants {
    const val EMPLOYEE_ID_ARG = "employeeId"
    const val PAYMENT_ID_ARG = "paymentId"
    const val ADVANCE_ID_ARG = "advanceId"
    const val DISCOUNT_ID_ARG = "discountId"
    const val MONTH_ARG = "month"
    const val YEAR_ARG = "year"

    // Animaciones de transición
    const val TRANSITION_DURATION = 300
    const val FADE_DURATION = 150
}

// ============================================================================
// NAVIGATION HELPERS - Funciones auxiliares
// ============================================================================

/**
 * Verifica si una ruta es de navegación inferior
 */
fun String.isBottomNavRoute(): Boolean {
    return bottomNavItems.any { it.route == this }
}

/**
 * Verifica si una ruta requiere autenticación
 */
fun String.requiresAuth(): Boolean {
    val authRoutes = listOf(
        NavigationRoutes.Splash.route,
        NavigationRoutes.Login.route,
        NavigationRoutes.Register.route,
        NavigationRoutes.ForgotPassword.route
    )
    return !authRoutes.contains(this)
}

/**
 * Obtiene el título de la pantalla basado en la ruta
 */
fun getScreenTitle(route: String?): String {
    return when (route) {
        NavigationRoutes.Dashboard.route -> "IEPIN Personal"
        NavigationRoutes.Employees.route -> "Personal"
        NavigationRoutes.AddEmployee.route -> "Agregar Empleado"
        NavigationRoutes.Payments.route -> "Pagos"
        NavigationRoutes.AddPayment.route -> "Registrar Pago"
        NavigationRoutes.Statistics.route -> "Reportes"
        NavigationRoutes.Profile.route -> "Perfil"
        NavigationRoutes.Settings.route -> "Configuración"
        NavigationRoutes.Discounts.route -> "Descuentos"
        NavigationRoutes.Advances.route -> "Adelantos"
        NavigationRoutes.Backup.route -> "Respaldo"
        NavigationRoutes.Help.route -> "Ayuda"
        NavigationRoutes.About.route -> "Acerca de"
        else -> "IEPIN Personal"
    }
}