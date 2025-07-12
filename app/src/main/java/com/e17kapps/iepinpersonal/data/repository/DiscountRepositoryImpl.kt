package com.e17kapps.iepinpersonal.data.repository

import com.e17kapps.iepinpersonal.domain.model.Discount
import com.e17kapps.iepinpersonal.domain.model.DiscountType
import com.e17kapps.iepinpersonal.domain.repository.DiscountRepository
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
class DiscountRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : DiscountRepository {

    private val currentUser get() = auth.currentUser
    private val discountsCollection get() = firestore.collection("users")
        .document(currentUser?.uid ?: "")
        .collection("discounts")

    override suspend fun addDiscount(discount: Discount): Result<String> {
        return try {
            val discountData = discount.copy(
                id = "", // Firestore generará el ID
                createdAt = System.currentTimeMillis(),
                createdBy = currentUser?.email ?: ""
            )

            val docRef = discountsCollection.add(discountData.toMap()).await()

            // Actualizar el documento con su propio ID
            discountsCollection.document(docRef.id)
                .update("id", docRef.id)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDiscount(discount: Discount): Result<Unit> {
        return try {
            discountsCollection.document(discount.id)
                .set(discount.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDiscount(discountId: String): Result<Unit> {
        return try {
            discountsCollection.document(discountId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiscount(discountId: String): Result<Discount> {
        return try {
            val doc = discountsCollection.document(discountId)
                .get()
                .await()

            if (doc.exists()) {
                val discount = doc.toDiscount()
                Result.success(discount)
            } else {
                Result.failure(Exception("Descuento no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiscountsByEmployee(employeeId: String): Result<List<Discount>> {
        return try {
            val querySnapshot = discountsCollection
                .whereEqualTo("employeeId", employeeId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val discounts = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toDiscount()
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(discounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveDiscounts(): Result<List<Discount>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val querySnapshot = discountsCollection
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val discounts = querySnapshot.documents.mapNotNull { doc ->
                try {
                    val discount = doc.toDiscount()
                    // Filtrar descuentos que no han expirado
                    if (discount.endDate == null || discount.endDate > currentTime) {
                        discount
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(discounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiscountsByType(type: DiscountType): Result<List<Discount>> {
        return try {
            val querySnapshot = discountsCollection
                .whereEqualTo("type", type.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val discounts = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toDiscount()
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(discounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiscountsByDateRange(startDate: Long, endDate: Long): Result<List<Discount>> {
        return try {
            val querySnapshot = discountsCollection
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("startDate", endDate)
                .orderBy("startDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val discounts = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toDiscount()
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(discounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivateDiscount(discountId: String): Result<Unit> {
        return try {
            discountsCollection.document(discountId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun activateDiscount(discountId: String): Result<Unit> {
        return try {
            discountsCollection.document(discountId)
                .update("isActive", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDiscountsFlow(): Flow<List<Discount>> = callbackFlow {
        val listenerRegistration = discountsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val discounts = querySnapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toDiscount()
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(discounts)
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun getRecurringDiscounts(): Result<List<Discount>> {
        return try {
            val querySnapshot = discountsCollection
                .whereEqualTo("isRecurring", true)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val discounts = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toDiscount()
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(discounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExpiredDiscounts(): Result<List<Discount>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val querySnapshot = discountsCollection
                .whereNotEqualTo("endDate", null)
                .whereLessThan("endDate", currentTime)
                .orderBy("endDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val discounts = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toDiscount()
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(discounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateTotalDiscounts(startDate: Long, endDate: Long): Result<Double> {
        return try {
            val discountsResult = getDiscountsByDateRange(startDate, endDate)
            discountsResult.fold(
                onSuccess = { discounts ->
                    val total = discounts.filter { it.isActive }.sumOf { it.amount }
                    Result.success(total)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateEmployeeDiscounts(employeeId: String, startDate: Long, endDate: Long): Result<Double> {
        return try {
            val discountsResult = getDiscountsByEmployee(employeeId)
            discountsResult.fold(
                onSuccess = { discounts ->
                    val total = discounts.filter { discount ->
                        discount.isActive &&
                                discount.startDate >= startDate &&
                                discount.startDate <= endDate
                    }.sumOf { it.amount }
                    Result.success(total)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Extension functions para conversión
    private fun Discount.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "employeeId" to employeeId,
            "employeeName" to employeeName,
            "amount" to amount,
            "type" to type.name,
            "reason" to reason,
            "description" to description,
            "isRecurring" to isRecurring,
            "startDate" to startDate,
            "endDate" to endDate,
            "isActive" to isActive,
            "appliedInPaymentId" to appliedInPaymentId,
            "createdAt" to createdAt,
            "createdBy" to createdBy
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toDiscount(): Discount {
        return Discount(
            id = getString("id") ?: "",
            employeeId = getString("employeeId") ?: "",
            employeeName = getString("employeeName") ?: "",
            amount = getDouble("amount") ?: 0.0,
            type = try {
                DiscountType.valueOf(getString("type") ?: "OTHER")
            } catch (e: Exception) {
                DiscountType.OTHER
            },
            reason = getString("reason") ?: "",
            description = getString("description") ?: "",
            isRecurring = getBoolean("isRecurring") ?: false,
            startDate = getLong("startDate") ?: System.currentTimeMillis(),
            endDate = getLong("endDate"),
            isActive = getBoolean("isActive") ?: true,
            appliedInPaymentId = getString("appliedInPaymentId"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            createdBy = getString("createdBy") ?: ""
        )
    }
}