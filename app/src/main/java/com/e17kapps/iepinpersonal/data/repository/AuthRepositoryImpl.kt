package com.e17kapps.iepinpersonal.data.repository

import com.e17kapps.iepinpersonal.data.model.UserDocument
import com.e17kapps.iepinpersonal.data.remote.FirebaseConfig
import com.e17kapps.iepinpersonal.domain.model.User
import com.e17kapps.iepinpersonal.domain.model.UserRole
import com.e17kapps.iepinpersonal.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) : AuthRepository {

    private val auth: FirebaseAuth = firebaseConfig.auth
    private val firestore: FirebaseFirestore = firebaseConfig.firestore

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val userDoc = firestore.collection(FirebaseConfig.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    val userDocument = userDoc.toObject(UserDocument::class.java)
                    if (userDocument != null && userDocument.isActive) {
                        Result.success(userDocument.toDomain())
                    } else {
                        auth.signOut()
                        Result.failure(Exception("Usuario inactivo o no encontrado"))
                    }
                } else {
                    // Si no existe el documento del usuario, crearlo
                    val newUser = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        name = firebaseUser.displayName ?: "",
                        role = UserRole.USER
                    )
                    createUserDocument(newUser)
                    Result.success(newUser)
                }
            } else {
                Result.failure(Exception("Error de autenticación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    name = name,
                    role = UserRole.USER
                )

                createUserDocument(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Error al crear la cuenta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val userDoc = firestore.collection(FirebaseConfig.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                userDoc.toObject(UserDocument::class.java)?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Escuchar cambios en el documento del usuario
                val userDocRef = firestore.collection(FirebaseConfig.USERS_COLLECTION)
                    .document(firebaseUser.uid)

                userDocRef.get().addOnSuccessListener { snapshot ->
                    val user = snapshot.toObject(UserDocument::class.java)?.toDomain()
                    trySend(user)
                }.addOnFailureListener {
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            val userDocument = UserDocument.fromDomain(user.copy(updatedAt = System.currentTimeMillis()))
            firestore.collection(FirebaseConfig.USERS_COLLECTION)
                .document(user.id)
                .set(userDocument)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                // Eliminar documento del usuario
                firestore.collection(FirebaseConfig.USERS_COLLECTION)
                    .document(user.uid)
                    .delete()
                    .await()

                // Eliminar cuenta de Firebase Auth
                user.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createUserDocument(user: User) {
        val userDocument = UserDocument.fromDomain(user)
        firestore.collection(FirebaseConfig.USERS_COLLECTION)
            .document(user.id)
            .set(userDocument)
            .await()
    }
}