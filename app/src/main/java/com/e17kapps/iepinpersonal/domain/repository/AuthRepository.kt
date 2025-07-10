package com.e17kapps.iepinpersonal.domain.repository

import com.e17kapps.iepinpersonal.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    fun isUserLoggedIn(): Boolean
    suspend fun deleteAccount(): Result<Unit>
}