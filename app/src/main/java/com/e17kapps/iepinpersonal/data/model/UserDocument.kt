package com.e17kapps.iepinpersonal.data.model

import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole

data class UserDocument(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val password: String = "",
    val photoUrl: String? = null,
    val emailVerified: Boolean = false,
    val role: String = UserRole.OPERATOR.name,
    val isActive: Boolean = true,
    val department: String = "",
    val createdAt: Long = 0L,
    val createdBy: String = "",
    val lastLogin: Long? = null,
    val updatedAt: Long? = null
) {
    fun toDomain(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName,
            password = password,
            photoUrl = photoUrl,
            emailVerified = emailVerified,
            role = try {
                UserRole.valueOf(role)
            } catch (e: Exception) {
                UserRole.OPERATOR
            },
            isActive = isActive,
            department = department,
            createdAt = createdAt,
            createdBy = createdBy,
            lastLogin = lastLogin,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(user: User): UserDocument {
            return UserDocument(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                password = user.password,
                photoUrl = user.photoUrl,
                emailVerified = user.emailVerified,
                role = user.role.name,
                isActive = user.isActive,
                department = user.department,
                createdAt = user.createdAt,
                createdBy = user.createdBy,
                lastLogin = user.lastLogin,
                updatedAt = user.updatedAt
            )
        }
    }
}