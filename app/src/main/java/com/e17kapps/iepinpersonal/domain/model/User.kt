package com.e17kapps.iepinpersonal.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val emailVerified: Boolean = false,

    // NUEVOS CAMPOS PARA EL SISTEMA DE ROLES
    val role: UserRole = UserRole.OPERATOR,
    val isActive: Boolean = true,
    val department: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val lastLogin: Long? = null,
    val updatedAt: Long?
)

enum class UserRole(val displayName: String, val description: String) {
    ADMIN(
        displayName = "Administrador",
        description = "Acceso completo a toda la aplicación"
    ),
    OPERATOR(
        displayName = "Operador",
        description = "Puede registrar y consultar pagos, descuentos y adelantos"
    )
}

// Funciones útiles
fun User.isAdmin(): Boolean = role == UserRole.ADMIN
fun User.isOperator(): Boolean = role == UserRole.OPERATOR
fun User.canManageEmployees(): Boolean = role == UserRole.ADMIN
fun User.canViewStatistics(): Boolean = role == UserRole.ADMIN
fun User.canManageUsers(): Boolean = role == UserRole.ADMIN
fun User.canRegisterPayments(): Boolean = role == UserRole.ADMIN || role == UserRole.OPERATOR
fun User.canRegisterDiscounts(): Boolean = role == UserRole.ADMIN || role == UserRole.OPERATOR
fun User.canRegisterAdvances(): Boolean = role == UserRole.ADMIN || role == UserRole.OPERATOR