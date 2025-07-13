package com.e17kapps.iepinpersonal.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val password: String = "", // Para autenticación sin Firebase Auth
    val photoUrl: String? = null,
    val emailVerified: Boolean = false,

    // CAMPOS PARA EL SISTEMA DE ROLES
    val role: UserRole = UserRole.OPERATOR,
    val isActive: Boolean = true,
    val department: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val lastLogin: Long? = null,
    val updatedAt: Long? = null
) {
    // Función para convertir a Map para Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "password" to password,
            "photoUrl" to photoUrl,
            "emailVerified" to emailVerified,
            "role" to role.name,
            "isActive" to isActive,
            "department" to department,
            "createdAt" to createdAt,
            "createdBy" to createdBy,
            "lastLogin" to lastLogin,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any?>): User {
            return User(
                uid = data["uid"] as? String ?: "",
                email = data["email"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                password = data["password"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String,
                emailVerified = data["emailVerified"] as? Boolean ?: false,
                role = UserRole.valueOf(data["role"] as? String ?: UserRole.OPERATOR.name),
                isActive = data["isActive"] as? Boolean ?: true,
                department = data["department"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                createdBy = data["createdBy"] as? String ?: "",
                lastLogin = (data["lastLogin"] as? Number)?.toLong(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong()
            )
        }
    }
}

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

// Funciones de extensión para permisos
fun User.isAdmin(): Boolean = role == UserRole.ADMIN
fun User.isOperator(): Boolean = role == UserRole.OPERATOR
fun User.canManageEmployees(): Boolean = role == UserRole.ADMIN
fun User.canViewStatistics(): Boolean = role == UserRole.ADMIN
fun User.canManageUsers(): Boolean = role == UserRole.ADMIN
fun User.canRegisterPayments(): Boolean = role == UserRole.ADMIN || role == UserRole.OPERATOR
fun User.canRegisterDiscounts(): Boolean = role == UserRole.ADMIN || role == UserRole.OPERATOR
fun User.canRegisterAdvances(): Boolean = role == UserRole.ADMIN || role == UserRole.OPERATOR
fun User.canViewDashboard(): Boolean = role == UserRole.ADMIN
fun User.canViewReports(): Boolean = role == UserRole.ADMIN
fun User.canExportData(): Boolean = role == UserRole.ADMIN