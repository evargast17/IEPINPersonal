package com.e17kapps.iepinpersonal.data.repository

import com.e17kapps.iepinpersonal.data.model.UserDocument
import com.e17kapps.iepinpersonal.data.remote.FirebaseConfig
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) : AuthRepository {

    private val firestore: FirebaseFirestore = firebaseConfig.firestore
    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val hashedPassword = hashPassword(password)

            val userSnapshot = firestore.collection(FirebaseConfig.USERS_COLLECTION)
                .whereEqualTo("email", email.lowercase())
                .whereEqualTo("password", hashedPassword)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            if (userSnapshot.documents.isNotEmpty()) {
                val userDocument = userSnapshot.documents.first().toObject(UserDocument::class.java)
                if (userDocument != null) {
                    val user = userDocument.toDomain()
                    currentUser = user

                    // Actualizar último login
                    updateLastLogin(user.uid)

                    Result.success(user)
                } else {
                    Result.failure(Exception("Error al obtener datos del usuario"))
                }
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            // Verificar si ya existe un usuario con este email
            val existingUser = firestore.collection(FirebaseConfig.USERS_COLLECTION)
                .whereEqualTo("email", email.lowercase())
                .get()
                .await()

            if (existingUser.documents.isNotEmpty()) {
                return Result.failure(Exception("Ya existe un usuario con este email"))
            }

            val uid = firestore.collection(FirebaseConfig.USERS_COLLECTION).document().id
            val hashedPassword = hashPassword(password)

            val user = User(
                uid = uid,
                email = email.lowercase(),
                displayName = name,
                role = UserRole.OPERATOR, // Por defecto OPERATOR
                password = hashedPassword,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                createdBy = currentUser?.uid ?: "",
                updatedAt = System.currentTimeMillis()
            )

            val userDocument = UserDocument.fromDomain(user)
            firestore.collection(FirebaseConfig.USERS_COLLECTION)
                .document(uid)
                .set(userDocument)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear usuario: ${e.message}"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            currentUser = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return currentUser
    }

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        if (currentUser != null) {
            val userDocRef = firestore.collection(FirebaseConfig.USERS_COLLECTION)
                .document(currentUser!!.uid)

            val listener = userDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(UserDocument::class.java)?.toDomain()
                currentUser = user
                trySend(user)
            }

            awaitClose { listener.remove() }
        } else {
            trySend(null)
            awaitClose { }
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            val userDocument = UserDocument.fromDomain(user.copy(updatedAt = System.currentTimeMillis()))
            firestore.collection(FirebaseConfig.USERS_COLLECTION)
                .document(user.uid)
                .set(userDocument)
                .await()

            if (currentUser?.uid == user.uid) {
                currentUser = user
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            // En un sistema real, aquí enviarías un email de recuperación
            // Para este ejemplo, retornamos éxito
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return currentUser != null
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            currentUser?.let { user ->
                firestore.collection(FirebaseConfig.USERS_COLLECTION)
                    .document(user.uid)
                    .update("isActive", false)
                    .await()
                currentUser = null
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateLastLogin(userId: String) {
        try {
            firestore.collection(FirebaseConfig.USERS_COLLECTION)
                .document(userId)
                .update("lastLogin", System.currentTimeMillis())
                .await()
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}