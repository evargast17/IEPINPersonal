package com.e17kapps.iepinpersonal.data.repository

import com.e17kapps.iepinpersonal.data.model.UserDocument
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")
    private var currentUserId: String? = null

    fun setCurrentUser(userId: String?) {
        currentUserId = userId
    }

    override suspend fun createUser(user: User, password: String): Result<String> {
        return try {
            // Verificar si ya existe un usuario con este email
            val existingUser = usersCollection
                .whereEqualTo("email", user.email.lowercase())
                .get()
                .await()

            if (existingUser.documents.isNotEmpty()) {
                return Result.failure(Exception("Ya existe un usuario con este email"))
            }

            val newUserId = usersCollection.document().id
            val hashedPassword = hashPassword(password)

            val userData = user.copy(
                uid = newUserId,
                email = user.email.lowercase(),
                password = hashedPassword,
                createdBy = currentUserId ?: "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val userDocument = UserDocument.fromDomain(userData)
            usersCollection.document(newUserId)
                .set(userDocument)
                .await()

            Result.success(newUserId)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear usuario: ${e.message}"))
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userDocument = UserDocument.fromDomain(
                user.copy(updatedAt = System.currentTimeMillis())
            )
            usersCollection.document(user.uid)
                .set(userDocument)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar usuario: ${e.message}"))
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Marcar como inactivo en lugar de eliminar
            usersCollection.document(userId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar usuario: ${e.message}"))
        }
    }

    override suspend fun getUser(userId: String): Result<User> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                val userDocument = doc.toObject(UserDocument::class.java)
                if (userDocument != null) {
                    Result.success(userDocument.toDomain())
                } else {
                    Result.failure(Exception("Error al procesar datos del usuario"))
                }
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuario: ${e.message}"))
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val currentUserId = this.currentUserId
            if (currentUserId != null) {
                getUser(currentUserId)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuario actual: ${e.message}"))
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .orderBy("displayName", Query.Direction.ASCENDING)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserDocument::class.java)?.toDomain()
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuarios: ${e.message}"))
        }
    }

    override suspend fun getUsersByRole(role: UserRole): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("role", role.name)
                .whereEqualTo("isActive", true)
                .orderBy("displayName", Query.Direction.ASCENDING)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserDocument::class.java)?.toDomain()
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuarios por rol: ${e.message}"))
        }
    }

    override suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update(
                    mapOf(
                        "role" to role.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar rol: ${e.message}"))
        }
    }

    override suspend fun activateUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update(
                    mapOf(
                        "isActive" to true,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al activar usuario: ${e.message}"))
        }
    }

    override suspend fun deactivateUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al desactivar usuario: ${e.message}"))
        }
    }

    override fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .orderBy("displayName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(UserDocument::class.java)?.toDomain()
                } ?: emptyList()

                trySend(users)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateLastLogin(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("lastLogin", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar último login: ${e.message}"))
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}