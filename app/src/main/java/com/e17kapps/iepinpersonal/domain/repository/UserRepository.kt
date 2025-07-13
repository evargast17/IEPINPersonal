package com.e17kapps.iepinpersonal.domain.repository


import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createUser(user: User, password: String): Result<String>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun getUser(userId: String): Result<User>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun getAllUsers(): Result<List<User>>
    suspend fun getUsersByRole(role: UserRole): Result<List<User>>
    suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit>
    suspend fun activateUser(userId: String): Result<Unit>
    suspend fun deactivateUser(userId: String): Result<Unit>
    fun getUsersFlow(): Flow<List<User>>
    suspend fun updateLastLogin(userId: String): Result<Unit>
}