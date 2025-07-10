package com.e17kapps.iepinpersonal.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserRole(val displayName: String) {
    ADMIN("Administrador"),
    USER("Usuario"),
    VIEWER("Solo Lectura")
}