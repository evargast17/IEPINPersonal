package com.e17kapps.iepinpersonal.data.repository

import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun createUser(user: User, password: String): Result<String> {
        return try {
            // Crear usuario en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val newUserId = authResult.user?.uid ?: throw Exception("Error al crear usuario en Auth")

            // Crear documento del usuario en Firestore
            val userData = user.copy(
                uid = newUserId,
                createdBy = auth.currentUser?.uid ?: "",
            )

            usersCollection.document(newUserId)
                .set(userData.toMap())
                .await()

            Result.success(newUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid)
                .set(user.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Marcar como inactivo en lugar de eliminar
            usersCollection.document(userId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUser(userId: String): Result<User> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                val user = doc.toUser()
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                getUser(currentUserId)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val querySnapshot = usersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val users = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toUser()
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsersByRole(role: UserRole): Result<List<User>> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("role", role.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val users = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toUser()
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("role", role.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun activateUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("isActive", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivateUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listenerRegistration = usersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = querySnapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toUser()
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(users)
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun updateLastLogin(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("lastLogin", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Extension functions para conversión
    private fun User.toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "role" to role.name,
            "isActive" to isActive,
            "department" to department,
            "createdAt" to createdAt,
            "createdBy" to createdBy,
            "lastLogin" to lastLogin,
            "photoUrl" to photoUrl
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User {
        return User(
            uid = getString("uid") ?: "",
            email = getString("email") ?: "",
            displayName = getString("displayName") ?: "",
            photoUrl = getString("photoUrl"),
            role = try {
                UserRole.valueOf(getString("role") ?: "OPERATOR")
            } catch (e: Exception) {
                UserRole.OPERATOR
            },
            isActive = getBoolean("isActive") ?: true,
            department = getString("department") ?: "",
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            createdBy = getString("createdBy") ?: "",
            lastLogin = getLong("lastLogin"),
            updatedAt = getLong("updatedAt")
        )
    }
}